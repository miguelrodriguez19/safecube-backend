package architecture.application;

import static architecture.support.ArchitectureConstants.APPLICATION_PACKAGE;
import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-A01, ARC-A03
 *
 * <p>Application layer dependency rules.
 */
class ApplicationDependencyTest {

  @Test
  void application_should_not_depend_on_infrastructure_layers() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(APPLICATION_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..infrastructure.persistence..",
                "..infrastructure.web..",
                "..infrastructure.client..")
            .because("application must not depend on infrastructure details");

    rule.check(classes);
  }

  @Test
  void application_should_not_use_jpa_or_entity_manager() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(APPLICATION_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "javax.persistence..", "jakarta.persistence..", "org.springframework.data.jpa..")
            .because("application must not depend on JPA or Spring Data");

    rule.check(classes);
  }
}
