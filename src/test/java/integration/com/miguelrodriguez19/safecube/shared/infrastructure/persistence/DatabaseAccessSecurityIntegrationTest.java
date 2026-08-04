package integration.com.miguelrodriguez19.safecube.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import integration.annotation.IntegrationTest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

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

  @Autowired private DataSource dataSource;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void shouldEnableRls_givenApplicationTables() {
    final var tableNames =
        jdbcTemplate.queryForList(
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
        jdbcTemplate.queryForList(
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
        jdbcTemplate.queryForMap(
            """
            select rolcanlogin, rolsuper, rolcreatedb, rolcreaterole,
                   rolreplication, rolbypassrls
              from pg_roles
             where rolname = 'safecube_app'
            """);

    final var owners =
        jdbcTemplate.queryForList(
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
    try (final var connection = dataSource.getConnection()) {
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
