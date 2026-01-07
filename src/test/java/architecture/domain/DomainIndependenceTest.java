package architecture.domain;

import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static architecture.support.ArchitectureConstants.DOMAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-D01, ARC-D02
 *
 * <p>Domain must be completely independent of: - Spring Framework - Infrastructure layer
 */
class DomainIndependenceTest {

  @Test
  void domain_should_not_depend_on_spring() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .because("domain must not depend on Spring Framework");

    rule.check(classes);
  }

  @Test
  void domain_should_not_depend_on_infrastructure() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..")
            .because("domain must not depend on infrastructure");

    rule.check(classes);
  }
}
