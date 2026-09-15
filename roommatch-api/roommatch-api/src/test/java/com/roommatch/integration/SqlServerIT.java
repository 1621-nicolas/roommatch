package com.roommatch.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SqlServerIT {
    @Container
    static final MSSQLServerContainer<?> SQL = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04").acceptLicense();

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws SQLException {
        createDatabase("roommatch_test");
        registry.add("spring.datasource.url", () -> url("roommatch_test"));
        registry.add("spring.datasource.username", SQL::getUsername);
        registry.add("spring.datasource.password", SQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.SQLServerDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired DataSource dataSource;
    @Autowired Flyway flyway;
    @Autowired com.roommatch.service.ReporteService reportes;
    @Autowired com.roommatch.service.ContactoUsuarioService contactos;
    @Autowired com.roommatch.service.PerfilConvivenciaService perfiles;
    @Autowired com.roommatch.service.LeadHabitacionService leads;
    @Autowired com.roommatch.service.SolicitudContactoService solicitudes;
    @Autowired com.roommatch.service.PropietarioService propietarios;
    @Autowired com.roommatch.service.HabitacionService habitaciones;
    @Autowired com.roommatch.service.MatchService matches;
    @Autowired com.roommatch.service.PublicacionRoomieService publicaciones;
    @Autowired com.roommatch.service.ImagenPublicacionService imagenesPublicacion;
    @Autowired com.roommatch.service.ImagenHabitacionService imagenesHabitacion;
    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Test
    void contactPagesAreBilateralPrivateAndUseAtMostThreeQueries() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int owner = addUser(jdbc), stranger = addUser(jdbc);
        var others = new java.util.ArrayList<Integer>();
        for (int i = 0; i < 51; i++) {
            int other = addUser(jdbc); others.add(other);
            int a = i % 2 == 0 ? owner : other, b = i % 2 == 0 ? other : owner;
            jdbc.update("INSERT INTO solicitud_contacto(id_usuario_emisor,id_usuario_receptor,estado) VALUES(?,?,'aceptada')", a, b);
            int request = jdbc.queryForObject("SELECT id_solicitud FROM solicitud_contacto WHERE id_usuario_emisor=? AND id_usuario_receptor=?", Integer.class, a, b);
            jdbc.update("INSERT INTO contacto_roomie(id_solicitud,id_usuario_a,id_usuario_b) VALUES(?,?,?)", request, a, b);
            if (i < 50) jdbc.update("INSERT INTO contacto_usuario(id_usuario,telefono,whatsapp,email_contacto,mostrar_whatsapp) VALUES(?,'999888777','51999888777','private@example.test',1)", other);
        }
        var stats = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        try {
            for (int size : new int[]{10, 20, 50}) {
                stats.clear();
                var page = contactos.listarDesbloqueados(owner, 0, size);
                long queries = stats.getPrepareStatementCount();
                System.out.printf("CONTACT_QUERIES size=%d queries=%d%n", size, queries);
                assertThat(queries).isLessThanOrEqualTo(3);
                assertThat(page.getContent()).hasSize(size);
                assertThat(page.getTotalElements()).isEqualTo(51);
                assertThat(page.getContent()).allSatisfy(row -> {
                    assertThat(others).contains(row.idUsuario());
                    if (row.contacto() != null) {
                        assertThat(row.contacto().getTelefono()).isNull();
                        assertThat(row.contacto().getEmailContacto()).isNull();
                        assertThat(row.contacto().getWhatsapp()).isEqualTo("51999888777");
                    }
                });
            }
        } finally { stats.setStatisticsEnabled(false); }
        assertThat(contactos.listarDesbloqueados(stranger, 0, 20)).isEmpty();
        for (int other : others.subList(0, 2)) {
            assertThat(contactos.listarDesbloqueados(other, 0, 20).getContent()).singleElement()
                    .satisfies(row -> assertThat(row.idUsuario()).isEqualTo(owner));
            assertThat(contactos.verContactoDesbloqueado(owner, other).getTelefono()).isNull();
            assertThat(contactos.verContactoDesbloqueado(other, other).getTelefono()).isEqualTo("999888777");
            assertThatThrownBy(() -> contactos.verContactoDesbloqueado(stranger, other)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        }
    }

    @Test
    void concurrentContactCreationAndStaleEditsCannotReopenPrivacy() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc);
        var create = new com.roommatch.dto.ContactoUsuarioRequest(); create.setEmailContacto("chosen@example.test");
        assertThat(race(() -> contactos.guardarOModificarMiContacto(user, create), () -> contactos.guardarOModificarMiContacto(user, create)))
                .containsExactlyInAnyOrder("success", "conflict");
        var before = contactos.obtenerMiContacto(user);
        assertThat(before.getMostrarEmail()).isFalse();
        var edit = new com.roommatch.dto.ContactoUsuarioRequest(); edit.setVersion(before.getVersion()); edit.setMostrarWhatsapp(true);
        var saved = contactos.guardarOModificarMiContacto(user, edit);
        assertThat(saved.getVersion()).isGreaterThan(before.getVersion());
        edit.setMostrarEmail(true);
        assertThatThrownBy(() -> contactos.guardarOModificarMiContacto(user, edit)).isInstanceOf(com.roommatch.exception.ConflictException.class);
        assertThat(contactos.obtenerMiContacto(user).getMostrarEmail()).isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM contacto_usuario WHERE id_usuario=?", Integer.class, user)).isEqualTo(1);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
    void reportQueueDeduplicatesAndConcurrentResolutionsPreserveAudit() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int reporter=addUser(jdbc), target=addUser(jdbc), admin=addUser(jdbc);
        jdbc.update("UPDATE usuario SET id_rol=(SELECT id_rol FROM rol WHERE nombre_rol='ADMIN') WHERE id_usuario=?",admin);
        var request = new com.roommatch.dto.ReporteRequest(); request.setMotivo("Prueba de moderación");
        assertThat(race(() -> reportes.reportarUsuario(reporter,target,request), () -> reportes.reportarUsuario(reporter,target,request)))
                .containsExactlyInAnyOrder("success","conflict");
        int id=jdbc.queryForObject("SELECT id_reporte FROM reporte_usuario WHERE id_usuario_reportante=? AND id_usuario_reportado=?",Integer.class,reporter,target);
        reportes.revisarReporteUsuario(admin,id,"revisado","Investigación iniciada");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO reporte_usuario(id_usuario_reportante,id_usuario_reportado,motivo) VALUES(?,?,'Duplicado')",reporter,target))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(race(() -> reportes.revisarReporteUsuario(admin,id,"rechazado","Evidencia insuficiente"), () -> reportes.sancionarUsuario(admin,id,"Incidencia comprobada")))
                .containsExactlyInAnyOrder("success","conflict");
        var history=reportes.historialUsuario(admin,id,org.springframework.data.domain.PageRequest.of(0,10));
        assertThat(history.getTotalElements()).isEqualTo(2);
        assertThat(history.getContent()).allSatisfy(e -> {assertThat(e.idAdmin()).isEqualTo(admin);assertThat(e.motivo()).isNotBlank();});
        String state=jdbc.queryForObject("SELECT estado FROM reporte_usuario WHERE id_reporte=?",String.class,id);
        if ("sancionado".equals(state)) {
            reportes.restaurarUsuario(admin,id,"Apelación revisada");
            assertThat(jdbc.queryForObject("SELECT estado FROM usuario WHERE id_usuario=?",String.class,target)).isEqualTo("activo");
            assertThat(reportes.historialUsuario(admin,id,org.springframework.data.domain.PageRequest.of(0,10)).getTotalElements()).isEqualTo(3);
        }
    }

    @Test
    void descriptionPatchPreservesPreferencesAndRejectsStaleVersion() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc); addProfile(jdbc, user);
        var before = perfiles.obtenerMiPerfil(user);
        var after = perfiles.actualizarDescripcion(user, new com.roommatch.dto.PerfilDescripcionRequest("Descripción nueva", before.getVersion()));
        assertThat(after.getVersion()).isGreaterThan(before.getVersion());
        assertThat(after.getPresupuestoMin()).isEqualByComparingTo(before.getPresupuestoMin());
        assertThat(after.getLimpieza()).isEqualTo(before.getLimpieza());
        assertThatThrownBy(() -> perfiles.actualizarDescripcion(user, new com.roommatch.dto.PerfilDescripcionRequest("Borrador obsoleto", before.getVersion())))
                .isInstanceOf(com.roommatch.exception.ConflictException.class);
        var obsoleteFullUpdate = new com.roommatch.dto.PerfilConvivenciaRequest(); obsoleteFullUpdate.setVersion(before.getVersion());
        assertThatThrownBy(() -> perfiles.actualizarMiPerfil(user, obsoleteFullUpdate)).isInstanceOf(com.roommatch.exception.ConflictException.class);
        assertThat(perfiles.obtenerMiPerfil(user).getDescripcionPersonal()).isEqualTo("Descripción nueva");
        assertThat(perfiles.actualizarDescripcion(user, new com.roommatch.dto.PerfilDescripcionRequest("", after.getVersion())).getDescripcionPersonal()).isNull();
    }

    @Test
    void publicationGallerySerializesQuotaPrimaryAndOrdering() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc), stranger = addUser(jdbc);
        var publication = new com.roommatch.dto.PublicacionRoomieRequest();
        publication.setTipoPublicacion("busco_roomie"); publication.setTitulo("Galería"); publication.setDescripcion("Imágenes"); publication.setDistrito("Lima");
        int id = publicaciones.crearPublicacion(user, publication).getIdPublicacion();
        var image = new com.roommatch.dto.ImagenPublicacionRequest(); image.setUrlImagen("https://example.test/image.jpg");
        for (int i = 0; i < 4; i++) imagenesPublicacion.agregarImagen(user, id, image);
        assertThat(race(() -> imagenesPublicacion.agregarImagen(user, id, image), () -> imagenesPublicacion.agregarImagen(user, id, image)))
                .containsExactlyInAnyOrder("success", "conflict");
        var rows = imagenesPublicacion.listarImagenesPorPublicacion(id, null);
        assertThat(rows).hasSize(5);
        int first = rows.get(0).getIdImagen(), second = rows.get(1).getIdImagen();
        assertThat(race(() -> imagenesPublicacion.marcarComoPrincipal(user, first), () -> imagenesPublicacion.marcarComoPrincipal(user, second)))
                .containsOnly("success");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM imagen_publicacion WHERE id_publicacion=? AND principal=1", Integer.class, id)).isEqualTo(1);
        assertThatThrownBy(() -> imagenesPublicacion.eliminarImagen(stranger, first)).isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        int primary = imagenesPublicacion.listarImagenesPorPublicacion(id, user).stream().filter(r -> r.getPrincipal()).findFirst().orElseThrow().getIdImagen();
        imagenesPublicacion.eliminarImagen(user, primary);
        image.setOrden(1); image.setPrincipal(true);
        imagenesPublicacion.agregarImagen(user, id, image);
        assertThat(imagenesPublicacion.listarImagenesPorPublicacion(id, user)).extracting(com.roommatch.dto.ImagenPublicacionResponse::getOrden).containsExactly(1,2,3,4,5);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO imagen_publicacion(id_publicacion,url_imagen,orden,principal) VALUES(?,'https://example.test/direct.jpg',6,0)", id))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void roomGalleryProtectsPrivateVisibilityAndConcurrentPrimarySelection() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc), stranger = addUser(jdbc);
        var owner = new com.roommatch.dto.PropietarioRequest(); owner.setTipoPropietario("persona");
        propietarios.convertirmeEnPropietario(user, owner);
        var request = new com.roommatch.dto.HabitacionRequest(); request.setTitulo("Galería habitación");
        request.setDescripcion("Privacidad"); request.setDistrito("Lima"); request.setPrecio(new java.math.BigDecimal("500"));
        int id = habitaciones.crearHabitacion(user, request).getIdHabitacion();
        var image = new com.roommatch.dto.ImagenHabitacionRequest(); image.setUrlImagen("https://example.test/room.jpg");
        for (int i = 0; i < 4; i++) imagenesHabitacion.agregarImagen(user, id, image);
        assertThat(race(() -> imagenesHabitacion.agregarImagen(user, id, image), () -> imagenesHabitacion.agregarImagen(user, id, image)))
                .containsExactlyInAnyOrder("success", "conflict");
        var rows = imagenesHabitacion.listarImagenesPorHabitacion(id, null);
        int first = rows.get(0).getIdImagen(), second = rows.get(1).getIdImagen();
        assertThat(race(() -> imagenesHabitacion.marcarComoPrincipal(user, first), () -> imagenesHabitacion.marcarComoPrincipal(user, second))).containsOnly("success");
        imagenesHabitacion.eliminarImagen(user, first);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM imagen_habitacion WHERE id_habitacion=? AND principal=1", Integer.class, id)).isEqualTo(1);
        assertThatThrownBy(() -> imagenesHabitacion.marcarComoPrincipal(stranger, second)).isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        habitaciones.pausarHabitacion(user, id);
        assertThatThrownBy(() -> imagenesHabitacion.listarImagenesPorHabitacion(id, null)).isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        assertThat(imagenesHabitacion.listarImagenesPorHabitacion(id, user)).hasSize(4);
    }

    @Test
    void inquiryEmailSurvivesDatabaseRoundTripAndIsScopedToOwner() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int ownerUser = addUser(jdbc), interested = addUser(jdbc), stranger = addUser(jdbc);
        var ownerRequest = new com.roommatch.dto.PropietarioRequest(); ownerRequest.setTipoPropietario("persona");
        propietarios.convertirmeEnPropietario(ownerUser, ownerRequest);
        propietarios.convertirmeEnPropietario(stranger, ownerRequest);
        var roomRequest = new com.roommatch.dto.HabitacionRequest();
        roomRequest.setTitulo("Consulta privada"); roomRequest.setDescripcion("Prueba de consentimiento");
        roomRequest.setDistrito("Lima"); roomRequest.setPrecio(new java.math.BigDecimal("500"));
        int room = habitaciones.crearHabitacion(ownerUser, roomRequest).getIdHabitacion();
        var request = new com.roommatch.dto.LeadHabitacionRequest();
        request.setMensaje("Quisiera coordinar una visita"); request.setEmailContacto("chosen@example.test");
        int id = leads.crearLead(interested, room, request).getIdLead();
        assertThat(leads.listarLeadsPropietario(ownerUser, null, org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
                .singleElement().satisfies(row -> assertThat(row.getEmailInteresado()).isEqualTo("chosen@example.test"));
        assertThatThrownBy(() -> leads.cambiarEstadoLead(stranger, id, "contactado"))
                .isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        jdbc.update("UPDATE lead_habitacion SET email_contacto=NULL WHERE id_lead=?", id);
        assertThat(leads.listarMisIntereses(interested)).singleElement().satisfies(row -> assertThat(row.getEmailInteresado()).isNull());
    }

    @Test
    void publicationPagesUseBoundedQueriesForTenTwentyAndFiftyCards() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int visitor = addUser(jdbc); addProfile(jdbc, visitor);
        var ownerRequest = new com.roommatch.dto.PropietarioRequest(); ownerRequest.setTipoPropietario("persona");
        propietarios.convertirmeEnPropietario(visitor, ownerRequest);
        var roomRequest = new com.roommatch.dto.HabitacionRequest();
        roomRequest.setTitulo("Referencia pública"); roomRequest.setDescripcion("Habitación para prueba de consultas");
        roomRequest.setDistrito("Lima"); roomRequest.setPrecio(new java.math.BigDecimal("500"));
        int room = habitaciones.crearHabitacion(visitor, roomRequest).getIdHabitacion();
        for (int i = 0; i < 50; i++) {
            int author = addUser(jdbc); addProfile(jdbc, author);
            jdbc.update("INSERT INTO publicacion_roomie(id_usuario,tipo_publicacion,titulo,descripcion,distrito,tipo_vinculacion_vivienda,id_habitacion) "
                    + "VALUES(?,'busco_compartir','Prueba batch','Prueba de 50 publicaciones','BatchDistrict',?,?)", author, i % 2 == 0 ? "roommatch" : null, i % 2 == 0 ? room : null);
        }
        var statistics = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        for (int size : new int[]{10, 20, 50}) {
            statistics.clear();
            var page = publicaciones.listarPublicaciones(visitor, null, "BatchDistrict", null, null, org.springframework.data.domain.PageRequest.of(0, size));
            long queries = statistics.getPrepareStatementCount();
            System.out.printf("PUBLICATION_QUERIES size=%d queries=%d%n", size, queries);
            assertThat(page.getContent()).hasSize(size);
            assertThat(queries).isLessThanOrEqualTo(4);
            assertThat(page.getContent()).allSatisfy(p -> assertThat(p.getPorcentajeCompatibilidad()).isEqualByComparingTo("100"));
        }
        statistics.setStatisticsEnabled(false);
    }

    @Test
    void logicalDeletePreservesImagesAndHidesThemFromVisitors() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc);
        var request = new com.roommatch.dto.PublicacionRoomieRequest();
        request.setTipoPublicacion("busco_roomie"); request.setTitulo("Historial"); request.setDescripcion("Conservar imágenes"); request.setDistrito("Lima");
        int id = publicaciones.crearPublicacion(user, request).getIdPublicacion();
        jdbc.update("INSERT INTO imagen_publicacion(id_publicacion,url_imagen,orden,principal) VALUES(?,'https://example.invalid/test.jpg',1,1)", id);
        publicaciones.eliminarPublicacion(user, id);
        assertThat(jdbc.queryForObject("SELECT estado FROM publicacion_roomie WHERE id_publicacion=?", String.class, id)).isEqualTo("eliminada");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM imagen_publicacion WHERE id_publicacion=?", Integer.class, id)).isEqualTo(1);
        assertThatThrownBy(() -> imagenesPublicacion.listarImagenesPorPublicacion(id, null)).isInstanceOf(com.roommatch.exception.ResourceNotFoundException.class);
        assertThat(imagenesPublicacion.listarImagenesPorPublicacion(id, user)).hasSize(1);
    }

    @Test
    void realProjectionUsesCurrentPreferencesEvenWhenHistoricalMatchExists() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int a = addUser(jdbc), b = addUser(jdbc);
        addProfile(jdbc, a); addProfile(jdbc, b);
        jdbc.update("INSERT INTO match_resultado(id_usuario_origen,id_usuario_destino,porcentaje) VALUES(?,?,0)", a, b);
        assertThat(matches.obtenerCompatibilidadEntreUsuarios(a, b).orElseThrow().getPorcentaje()).isEqualByComparingTo("100");
        jdbc.update("UPDATE perfil_convivencia SET limpieza=1 WHERE id_usuario=?", b);
        assertThat(matches.obtenerCompatibilidadEntreUsuarios(a, b).orElseThrow().getPorcentaje()).isEqualByComparingTo("88");
        assertThat(matches.listarMisMatches(a, java.math.BigDecimal.ZERO, org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
                .extracting(com.roommatch.dto.MatchResponse::getIdUsuarioDestino).contains(b);
        jdbc.update("UPDATE usuario SET estado='suspendido' WHERE id_usuario=?", b);
        assertThat(matches.obtenerCompatibilidadEntreUsuarios(a, b)).isEmpty();
        assertThat(matches.listarMisMatches(a, java.math.BigDecimal.ZERO, org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
                .extracting(com.roommatch.dto.MatchResponse::getIdUsuarioDestino).doesNotContain(b);
    }

    private static void addProfile(JdbcTemplate jdbc, int user) {
        jdbc.update("""
            INSERT INTO perfil_convivencia(id_usuario,presupuesto_min,presupuesto_max,distrito_preferido,fecha_mudanza,
                limpieza,ruido,sociabilidad,horario,visitas,mascotas,fumar,alcohol,gastos,convivencia)
            VALUES(?,500,900,'Lima','2026-10-01',5,3,3,'mañana','moderadas','no','no','no','divididos','tranquila')
            """, user);
    }

    @Test
    void concurrentRoomCreationCannotExceedOneSlotPlan() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int user = addUser(jdbc);
        var ownerRequest = new com.roommatch.dto.PropietarioRequest();
        ownerRequest.setTipoPropietario("persona");
        var owner = propietarios.convertirmeEnPropietario(user, ownerRequest);
        var room = new com.roommatch.dto.HabitacionRequest();
        room.setTitulo("Habitación de prueba"); room.setDescripcion("Prueba del límite del plan");
        room.setDistrito("Lima"); room.setPrecio(new java.math.BigDecimal("500.00"));
        assertThat(race(() -> habitaciones.crearHabitacion(user, room), () -> habitaciones.crearHabitacion(user, room)))
                .containsExactlyInAnyOrder("success", "conflict");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM habitacion WHERE id_propietario=?", Integer.class, owner.getIdPropietario())).isEqualTo(1);
        // Search runs its actual production query, with subscription and moderation visibility.
        var page = habitaciones.listarHabitacionesPublicas("Lima", null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 20));
        assertThat(page.getContent()).extracting(com.roommatch.dto.HabitacionResponse::getIdPropietario).contains(owner.getIdPropietario());
        jdbc.update("UPDATE suscripcion_propietario SET fecha_inicio=DATEADD(day,-2,SYSDATETIME()),fecha_fin=DATEADD(day,-1,SYSDATETIME()) WHERE id_propietario=?", owner.getIdPropietario());
        assertThat(habitaciones.listarHabitacionesPublicas("Lima", null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 20)).getContent())
                .extracting(com.roommatch.dto.HabitacionResponse::getIdPropietario).doesNotContain(owner.getIdPropietario());
    }

    @Test
    void reciprocalConcurrentSendsCreateOnlyOnePendingRequest() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int a = addUser(jdbc), b = addUser(jdbc);
        var request = new com.roommatch.dto.SolicitudContactoRequest();
        var outcomes = race(() -> solicitudes.enviarSolicitud(a, b, request), () -> solicitudes.enviarSolicitud(b, a, request));
        assertThat(outcomes).containsExactlyInAnyOrder("success", "conflict");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM solicitud_contacto WHERE usuario_menor=? AND usuario_mayor=? AND estado='pendiente'",
                Integer.class, Math.min(a, b), Math.max(a, b))).isEqualTo(1);
        // A direct SQL writer cannot bypass the same invariant in the opposite direction.
        assertThatThrownBy(() -> jdbc.update("INSERT INTO solicitud_contacto(id_usuario_emisor,id_usuario_receptor) "
                + "SELECT id_usuario_receptor,id_usuario_emisor FROM solicitud_contacto WHERE usuario_menor=? AND usuario_mayor=? AND estado='pendiente'", a, b))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void acceptAndRejectRaceHasOneTerminalStateAndContactMatchesThatState() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int a = addUser(jdbc), b = addUser(jdbc);
        var pending = solicitudes.enviarSolicitud(a, b, new com.roommatch.dto.SolicitudContactoRequest());
        assertThat(race(() -> solicitudes.aceptarSolicitud(b, pending.getIdSolicitud()),
                () -> solicitudes.rechazarSolicitud(b, pending.getIdSolicitud())))
                .containsExactlyInAnyOrder("success", "conflict");
        String state = jdbc.queryForObject("SELECT estado FROM solicitud_contacto WHERE id_solicitud=?", String.class, pending.getIdSolicitud());
        assertThat(state).isIn("aceptada", "rechazada");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM contacto_roomie WHERE id_solicitud=?", Integer.class, pending.getIdSolicitud()))
                .isEqualTo("aceptada".equals(state) ? 1 : 0);
    }

    @Test
    void cancelledRequestStaysInHistoryAndReverseSendWorks() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int a = addUser(jdbc), b = addUser(jdbc);
        var first = solicitudes.enviarSolicitud(a, b, new com.roommatch.dto.SolicitudContactoRequest());
        solicitudes.cancelarSolicitud(a, first.getIdSolicitud());
        var second = solicitudes.enviarSolicitud(b, a, new com.roommatch.dto.SolicitudContactoRequest());
        assertThat(second.getIdSolicitud()).isNotEqualTo(first.getIdSolicitud());
        assertThat(jdbc.queryForObject("SELECT estado FROM solicitud_contacto WHERE id_solicitud=?", String.class, first.getIdSolicitud())).isEqualTo("cancelada");
    }

    private static int addUser(JdbcTemplate jdbc) {
        String email = java.util.UUID.randomUUID() + "@example.invalid";
        jdbc.update("INSERT INTO usuario(id_rol,nombres,apellidos,email,password_hash,edad) "
                + "SELECT id_rol,'Race','Test',?,'unused',25 FROM rol WHERE nombre_rol='USUARIO'", email);
        return jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE email=?", Integer.class, email);
    }

    private static java.util.List<String> race(Runnable first, Runnable second) throws Exception {
        var executor = new org.springframework.security.concurrent.DelegatingSecurityContextExecutorService(java.util.concurrent.Executors.newFixedThreadPool(2));
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            var a = executor.submit(() -> outcome(first, start));
            var b = executor.submit(() -> outcome(second, start));
            start.countDown();
            return java.util.List.of(a.get(30, java.util.concurrent.TimeUnit.SECONDS), b.get(30, java.util.concurrent.TimeUnit.SECONDS));
        } finally { executor.shutdownNow(); }
    }

    private static String outcome(Runnable action, java.util.concurrent.CountDownLatch start) throws InterruptedException {
        start.await();
        try { action.run(); return "success"; }
        catch (com.roommatch.exception.ConflictException expected) { return "conflict"; }
    }

    @Test
    void cleanInstallMigratesAndHibernateValidatesRealSchema() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rol", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM plan_propietario WHERE nombre_plan='Gratis'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COL_LENGTH('imagen_publicacion', 'principal')", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT CAST(SERVERPROPERTY('ProductMajorVersion') AS INT)", Integer.class)).isEqualTo(16);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();
    }

    @Test
    void uniqueAndCheckConstraintsAreEnforcedBySqlServer() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO rol(nombre_rol) VALUES ('USUARIO')"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO usuario(id_rol,nombres,apellidos,email,password_hash,edad) "
                + "SELECT id_rol,'Test','Edad','age-test@example.invalid','unused',17 FROM rol WHERE nombre_rol='USUARIO'"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void explicitBaselinePreservesExistingRowsAndAppliesLaterMigrations() throws Exception {
        createDatabase("roommatch_legacy");
        try (Connection connection = connection("roommatch_legacy")) {
            String legacy = new String(getClass().getResourceAsStream("/db/migration/V1__legacy_schema.sql")
                    .readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            for (String batch : legacy.split("(?im)^GO\\s*$")) {
                if (!batch.isBlank()) connection.createStatement().execute(batch);
            }
            connection.createStatement().execute("INSERT INTO rol(nombre_rol) VALUES ('USUARIO')");
            connection.createStatement().execute("INSERT INTO usuario(id_rol,nombres,apellidos,email,password_hash,edad) "
                    + "VALUES(1,'Conservar','Historial','legacy@example.invalid','unused',25)");
            connection.createStatement().execute("INSERT INTO contacto_usuario(id_usuario,email_contacto) VALUES(1,'legacy-contact@example.invalid')");
        }
        Flyway upgrade = Flyway.configure().dataSource(url("roommatch_legacy"), SQL.getUsername(), SQL.getPassword())
                .baselineOnMigrate(false).cleanDisabled(true).baselineVersion("1").load();
        assertThatThrownBy(upgrade::migrate).isInstanceOf(org.flywaydb.core.api.FlywayException.class);
        upgrade.baseline();
        upgrade.migrate();
        upgrade.validate();
        try (Connection connection = connection("roommatch_legacy")) {
            var rows = connection.createStatement().executeQuery("SELECT nombres FROM usuario WHERE email='legacy@example.invalid'");
            assertThat(rows.next()).isTrue();
            assertThat(rows.getString(1)).isEqualTo("Conservar");
            assertThat(rows.next()).isFalse();
            var contact = connection.createStatement().executeQuery("SELECT mostrar_email,email_contacto FROM contacto_usuario WHERE id_usuario=1");
            assertThat(contact.next()).isTrue();
            assertThat(contact.getBoolean(1)).isTrue();
            assertThat(contact.getString(2)).isEqualTo("legacy-contact@example.invalid");
        }
    }

    private static void createDatabase(String name) throws SQLException {
        // Names are test constants, never HTTP input.
        try (Connection connection = DriverManager.getConnection(SQL.getJdbcUrl(), SQL.getUsername(), SQL.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE [" + name + "]");
        }
    }

    private static String url(String name) { return SQL.getJdbcUrl() + ";databaseName=" + name; }
    private static Connection connection(String name) throws SQLException {
        return DriverManager.getConnection(url(name), SQL.getUsername(), SQL.getPassword());
    }
}
