package integration.annotation.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresSQLInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final String POSTGRES_IMAGE = "postgres:17-alpine";
  private static final String APPLICATION_USERNAME = "safecube_app";
  private static final String APPLICATION_PASSWORD = "app-test-pass";
  private static final String MIGRATOR_USERNAME = "safecube_migrator";
  private static final String MIGRATOR_PASSWORD = "migrator-test-pass";

  private static PostgreSQLContainer postgres;

  @Override
  public void initialize(@NotNull final ConfigurableApplicationContext context) {
    if (Objects.isNull(postgres)) {
      postgres =
          new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
              .withDatabaseName("testdb")
              .withUsername("testuser")
              .withPassword("testpass");
    }

    if (!postgres.isRunning()) {
      postgres.start();
    }

    initializeDatabase();

    TestPropertyValues.of(
            // Postgres
            "spring.datasource.url=" + postgres.getJdbcUrl(),
            "spring.datasource.username=" + APPLICATION_USERNAME,
            "spring.datasource.password=" + APPLICATION_PASSWORD,
            "safecube.test.admin-datasource.url=" + postgres.getJdbcUrl(),
            "safecube.test.admin-datasource.username=" + postgres.getUsername(),
            "safecube.test.admin-datasource.password=" + postgres.getPassword())
        .applyTo(context.getEnvironment());
  }

  public static Flyway flyway() {
    if (Objects.isNull(postgres) || !postgres.isRunning()) {
      throw new IllegalStateException("PostgreSQL test container is not running");
    }

    return Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), MIGRATOR_USERNAME, MIGRATOR_PASSWORD)
        .locations("classpath:db/migrations")
        .schemas("safecube_meta", "public")
        .defaultSchema("safecube_meta")
        .table("flyway_schema_history")
        .cleanDisabled(true)
        .baselineOnMigrate(false)
        .outOfOrder(false)
        .load();
  }

  private void initializeDatabase() {
    try (final var connection = postgres.createConnection("")) {
      executeScript(connection, "database/supabase-security-test-roles.sql");
      executeScript(connection, "database/supabase-security.sql");
      setRolePasswords(connection);
      flyway().migrate();
    } catch (final Exception exception) {
      throw new IllegalStateException("Could not initialize PostgreSQL test database", exception);
    }
  }

  private void setRolePasswords(final Connection connection) throws SQLException {
    try (final var statement = connection.createStatement()) {
      statement.execute(
          "ALTER ROLE safecube_app PASSWORD '"
              + APPLICATION_PASSWORD
              + "'; "
              + "ALTER ROLE safecube_migrator PASSWORD '"
              + MIGRATOR_PASSWORD
              + "';");
    }
  }

  private void executeScript(final Connection connection, final String path)
      throws IOException, SQLException {
    final var resource = new ClassPathResource(path);
    final var script = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    try (final var statement = connection.createStatement()) {
      statement.execute(script);
    }
  }
}
