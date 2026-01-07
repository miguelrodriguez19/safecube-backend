package architecture.application;

import static architecture.support.ArchUnitConditions.havePublicExecuteMethod;
import static architecture.support.ArchUnitConditions.notDeclareCheckedExceptions;
import static architecture.support.ArchitectureConstants.APPLICATION_PACKAGE;
import static architecture.support.ArchitectureConstants.BASE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * ARC-A02
 * <p>
 * UseCase contract rules.
 */
class UseCaseContractTest {

    @Test
    void usecases_should_expose_a_public_execute_method() {
        final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

        final var rule = classes()
            .that()
                .resideInAPackage(APPLICATION_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("UseCase")
            .should(havePublicExecuteMethod())
            .because("UseCases must expose a public execute(...) method");

        rule.check(classes);
    }

    @Test
    void usecases_should_not_throw_business_exceptions() {
        final var classes = new ClassFileImporter()
            .importPackages(BASE_PACKAGE);

    final var rule =
        classes()
            .that()
            .resideInAPackage(APPLICATION_PACKAGE)
            .and()
            .haveSimpleNameEndingWith("UseCase")
            .should(notDeclareCheckedExceptions())
            .because("business errors must not be modeled as exceptions");

        rule.check(classes);
    }
}
