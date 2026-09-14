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
    @Autowired com.roommatch.service.SolicitudContactoService solicitudes;
    @Autowired com.roommatch.service.PropietarioService propietarios;
    @Autowired com.roommatch.service.HabitacionService habitaciones;
    @Autowired com.roommatch.service.MatchService matches;
    @Autowired com.roommatch.service.PublicacionRoomieService publicaciones;
    @Autowired com.roommatch.service.ImagenPublicacionService imagenesPublicacion;
    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;

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
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
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
