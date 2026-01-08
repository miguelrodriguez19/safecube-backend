package architecture.infrastructure;

import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static architecture.support.ArchitectureConstants.WEB_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-W01
 *
 * <p>Controllers must act as thin HTTP adapters.
 */
class ControllerIsolationTest {

  @Test
  void controllers_should_not_access_repositories() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(WEB_PACKAGE)
            .should()
            .dependOnClassesThat()
            .haveSimpleNameEndingWith("Repository")
            .because("controllers must not access repositories directly");

    rule.check(classes);
  }

  @Test
  void controllers_should_not_contain_domain_logic() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(WEB_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..domain..")
            .because("controllers must not contain domain logic");

    rule.check(classes);
  }
}
