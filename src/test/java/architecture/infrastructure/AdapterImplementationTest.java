package architecture.infrastructure;

import static architecture.support.ArchUnitConditions.implementAtLeastOneInterface;
import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static architecture.support.ArchitectureConstants.PERSISTENCE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-I01
 *
 * <p>Infrastructure adapters must implement application ports.
 */
class AdapterImplementationTest {

  @Test
  void persistence_adapters_should_implement_application_ports() {
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    final var rule =
        classes()
            .that()
            .resideInAPackage(PERSISTENCE_PACKAGE)
            .and()
            .areNotInterfaces()
            .and()
            .haveSimpleNameEndingWith("Adapter")
            .should(implementAtLeastOneInterface())
            .because("infrastructure adapters must implement application ports");

    rule.check(classes);
  }
}
