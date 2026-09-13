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
