package architecture.slices;

import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-S01
 *
 * <p>Enforces isolation between vertical slices at domain level.
 *
 * <p>Slices must not depend on each other's internal domain models.
 */
class SliceIsolationTest {

  @Test
  void auth_domain_should_not_depend_on_user_or_vault_domain() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage("..auth.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..user.domain..", "..vault.domain..")
            .because("auth domain must be isolated from user and vault domains");

    rule.check(classes);
  }

  @Test
  void user_domain_should_not_depend_on_auth_or_vault_domain() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage("..user.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..auth.domain..", "..vault.domain..")
            .because("user domain must be isolated from auth and vault domains");

    rule.check(classes);
  }

  @Test
  void vault_domain_should_not_depend_on_auth_or_user_domain() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage("..vault.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..auth.domain..", "..user.domain..")
            .because("vault domain must be isolated from auth and user domains");

    rule.check(classes);
  }
}
