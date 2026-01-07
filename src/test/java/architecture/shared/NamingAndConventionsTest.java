package architecture.shared;

import static architecture.support.ArchUnitConditions.notBeAbstract;
import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * Cross-cutting conventions (shared).
 *
 * <p>Keeps naming and placement consistent to reduce architectural entropy.
 */
class NamingAndConventionsTest {

  @Test
  void application_repositories_should_be_interfaces_and_live_in_ports() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    // Any "*Repository" inside application must be an interface.
    final var rule =
        classes()
            .that()
            .resideInAPackage("..application..")
            .and()
            .haveSimpleNameEndingWith("Repository")
            .should()
            .beInterfaces()
            .andShould()
            .resideInAPackage("..application.port..")
            .because("repositories in application are ports and must be interfaces");

    rule.check(classes);
  }

  @Test
  void usecases_should_be_public_concrete_classes() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        classes()
            .that()
            .resideInAPackage("..application..")
            .and()
            .haveSimpleNameEndingWith("UseCase")
            .should()
            .bePublic()
            .andShould()
            .notBeInterfaces()
            .andShould(notBeAbstract())
            .because("use cases must be concrete public classes");

    rule.check(classes);
  }
}
