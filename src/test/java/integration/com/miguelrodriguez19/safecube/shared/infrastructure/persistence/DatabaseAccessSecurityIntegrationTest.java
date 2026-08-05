package integration.com.miguelrodriguez19.safecube.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import integration.annotation.IntegrationTest;
import integration.annotation.support.PostgresSQLInitializer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@IntegrationTest(profiles = {"jpa"})
class DatabaseAccessSecurityIntegrationTest {

  private static final List<String> APPLICATION_TABLES =
      List.of(
          "auth_accounts",
          "auth_refresh_tokens",
          "user_profiles",
          "vault_item_change_cursors",
          "vault_items",
          "vault_item_mutations",
          "vault_key_material");

  private static final Map<String, Set<String>> REQUIRED_PRIVILEGES =
      Map.ofEntries(
          Map.entry("auth_accounts", Set.of("SELECT", "INSERT", "UPDATE")),
          Map.entry("auth_refresh_tokens", Set.of("SELECT", "INSERT", "UPDATE")),
          Map.entry("user_profiles", Set.of("SELECT", "INSERT", "UPDATE")),
          Map.entry("vault_item_change_cursors", Set.of("SELECT", "INSERT", "UPDATE")),
          Map.entry("vault_items", Set.of("SELECT", "INSERT", "UPDATE")),
          Map.entry("vault_item_mutations", Set.of("SELECT", "INSERT")),
          Map.entry("vault_key_material", Set.of("SELECT", "INSERT", "UPDATE")));

  @Value("${safecube.test.admin-datasource.url}")
  private String adminUrl;

  @Value("${safecube.test.admin-datasource.username}")
  private String adminUsername;

  @Value("${safecube.test.admin-datasource.password}")
  private String adminPassword;

  private JdbcTemplate adminJdbcTemplate;

  @BeforeEach
  void configureAdminJdbcTemplate() {
    final var dataSource = new DriverManagerDataSource(adminUrl, adminUsername, adminPassword);
    adminJdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Test
  void shouldEnableRls_givenApplicationTables() {
    final var tableNames =
        adminJdbcTemplate.queryForList(
            """
            select tablename
              from pg_tables
             where schemaname = 'public'
               and tablename in ('auth_accounts', 'auth_refresh_tokens', 'user_profiles',
                                 'vault_item_change_cursors', 'vault_items',
                                 'vault_item_mutations', 'vault_key_material')
            """,
            String.class);

    final var tablesWithoutRls =
        adminJdbcTemplate.queryForList(
            """
            select tablename
              from pg_tables
             where schemaname = 'public'
               and not rowsecurity
               and tablename in ('auth_accounts', 'auth_refresh_tokens', 'user_profiles',
                                 'vault_item_change_cursors', 'vault_items',
                                 'vault_item_mutations', 'vault_key_material')
            """,
            String.class);

    assertThat(tableNames).containsExactlyInAnyOrderElementsOf(APPLICATION_TABLES);
    assertThat(tablesWithoutRls).isEmpty();
  }

  @Test
  void shouldDenyTableAccess_givenAnonRole() {
    assertTableAccessDenied("anon");
  }

  @Test
  void shouldDenyTableAccess_givenAuthenticatedRole() {
    assertTableAccessDenied("authenticated");
  }

  @Test
  void shouldDenyTableAccess_givenServiceRole() {
    assertTableAccessDenied("service_role");
  }

  @Test
  void shouldAllowRequiredOperations_givenSafeCubeApplicationRole() {
    executeAsRole(
        "safecube_app",
        connection -> {
          assertThat(hasSchemaPrivilege(connection, "USAGE"))
              .as("safecube_app must use the public schema")
              .isTrue();

          for (final var entry : REQUIRED_PRIVILEGES.entrySet()) {
            for (final var privilege : entry.getValue()) {
              assertThat(hasTablePrivilege(connection, entry.getKey(), privilege))
                  .as("safecube_app %s on %s", privilege, entry.getKey())
                  .isTrue();
            }
          }
        });
  }

  @Test
  void shouldRejectUnnecessaryDelete_givenSafeCubeApplicationRole() {
    executeAsRole(
        "safecube_app",
        connection -> {
          for (final var table : APPLICATION_TABLES) {
            assertThat(hasTablePrivilege(connection, table, "DELETE"))
                .as("safecube_app DELETE on %s", table)
                .isFalse();
          }
        });
  }

  @Test
  void shouldConfigureApplicationRoleWithBypassRlsWithoutOwnership() {
    final var role =
        adminJdbcTemplate.queryForMap(
            """
            select rolcanlogin, rolsuper, rolcreatedb, rolcreaterole,
                   rolreplication, rolbypassrls
              from pg_roles
             where rolname = 'safecube_app'
            """);

    final var owners =
        adminJdbcTemplate.queryForList(
            """
            select tableowner
              from pg_tables
             where schemaname = 'public'
               and tablename in ('auth_accounts', 'auth_refresh_tokens', 'user_profiles',
                                 'vault_item_change_cursors', 'vault_items',
                                 'vault_item_mutations', 'vault_key_material')
            """,
            String.class);

    assertThat(role)
        .containsEntry("rolcanlogin", true)
        .containsEntry("rolsuper", false)
        .containsEntry("rolcreatedb", false)
        .containsEntry("rolcreaterole", false)
        .containsEntry("rolreplication", false)
        .containsEntry("rolbypassrls", true);
    assertThat(owners).doesNotContain("safecube_app");
  }

  @Test
  void shouldKeepMigrationHistoryAndBeIdempotent() {
    final var result = PostgresSQLInitializer.flyway().migrate();
    final var appliedMigrations =
        adminJdbcTemplate.queryForObject(
            "select count(*) from safecube_meta.flyway_schema_history where success",
            Integer.class);

    assertThat(result.migrationsExecuted).isZero();
    assertThat(appliedMigrations).isEqualTo(2);
  }

  @Test
  void shouldRejectDdl_givenSafeCubeApplicationRole() {
    executeAsRole(
        "safecube_app",
        connection ->
            assertThatThrownBy(
                    () -> executeSql(connection, "CREATE TABLE public.application_probe (id INT)"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("permission denied"));
  }

  @Test
  void shouldAllowDdl_givenSafeCubeMigratorRole() {
    executeAsRole(
        "safecube_migrator",
        connection -> {
          executeSql(connection, "CREATE TABLE public.migrator_probe (id INT)");
          executeSql(connection, "DROP TABLE public.migrator_probe");
        });
  }

  private void assertTableAccessDenied(final String role) {
    executeAsRole(
        role,
        connection ->
            APPLICATION_TABLES.forEach(
                table ->
                    assertThatThrownBy(() -> selectFromTable(connection, table))
                        .as("%s must not read %s", role, table)
                        .isInstanceOf(SQLException.class)
                        .hasMessageContaining("permission denied")));
  }

  private void executeAsRole(final String role, final ConnectionOperation operation) {
    try (final var connection =
        DriverManager.getConnection(adminUrl, adminUsername, adminPassword)) {
      try {
        setRole(connection, role);
        operation.execute(connection);
      } finally {
        resetRole(connection);
      }
    } catch (final SQLException exception) {
      throw new IllegalStateException("Could not execute database security assertion", exception);
    }
  }

  private void setRole(final Connection connection, final String role) throws SQLException {
    try (final var statement = connection.createStatement()) {
      statement.execute("SET ROLE " + role);
    }
  }

  private void resetRole(final Connection connection) throws SQLException {
    try (final var statement = connection.createStatement()) {
      statement.execute("RESET ROLE");
    }
  }

  private void selectFromTable(final Connection connection, final String table)
      throws SQLException {
    try (final var statement = connection.createStatement();
        final var resultSet =
            statement.executeQuery("SELECT 1 FROM public." + table + " LIMIT 1")) {
      while (resultSet.next()) {
        // Consume the result so the statement can close cleanly.
      }
    }
  }

  private void executeSql(final Connection connection, final String sql) throws SQLException {
    try (final var statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private boolean hasSchemaPrivilege(final Connection connection, final String privilege)
      throws SQLException {
    try (final var statement =
        connection.prepareStatement("SELECT has_schema_privilege(current_user, 'public', ?)")) {
      statement.setString(1, privilege);
      try (final var resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getBoolean(1);
      }
    }
  }

  private boolean hasTablePrivilege(
      final Connection connection, final String table, final String privilege) throws SQLException {
    try (final var statement =
        connection.prepareStatement("SELECT has_table_privilege(current_user, ?, ?)")) {
      statement.setString(1, "public." + table);
      statement.setString(2, privilege);
      try (final var resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getBoolean(1);
      }
    }
  }

  @FunctionalInterface
  private interface ConnectionOperation {

    void execute(Connection connection) throws SQLException;
  }
}
