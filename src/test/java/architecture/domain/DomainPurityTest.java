package architecture.domain;

import static architecture.support.ArchUnitConditions.haveAnyAnnotationInPackage;
import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static architecture.support.ArchitectureConstants.DOMAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-N01, ARC-N02
 *
 * <p>Negative / purity rules for domain layer.
 */
class DomainPurityTest {

  @Test
  void domain_should_not_use_lombok() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("lombok..")
            .because("domain must be explicit and free of Lombok magic");

    rule.check(classes);
  }

  @Test
  void domain_should_not_use_spring_annotations() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should(haveAnyAnnotationInPackage("org.springframework"))
            .because("domain must not use Spring annotations");

    rule.check(classes);
  }
}
