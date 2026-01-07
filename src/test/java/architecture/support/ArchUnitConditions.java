package architecture.support;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class ArchUnitConditions {

  public static ArchCondition<JavaClass> haveAnyAnnotationInPackage(
      final String annotationPackagePrefix) {
    return new ArchCondition<>(
        "have any annotation in package '%s..'".formatted(annotationPackagePrefix)) {

      @Override
      public void check(final JavaClass javaClass, final ConditionEvents events) {
        for (final JavaAnnotation<?> javaAnnotation : javaClass.getAnnotations()) {
          final var annotationName = javaAnnotation.getRawType().getName();

          if (annotationName.startsWith(annotationPackagePrefix)) {
            final var message = javaClass.getName() + " is annotated with @" + annotationName;
            events.add(SimpleConditionEvent.violated(javaClass, message));
          }
        }
      }
    };
  }

  public static ArchCondition<JavaClass> havePublicExecuteMethod() {

    return new ArchCondition<>("have a public execute(...) method") {
      @Override
      public void check(
          com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
        boolean found =
            javaClass.getMethods().stream()
                .anyMatch(
                    method ->
                        method.getName().equals("execute")
                            && method
                                .getModifiers()
                                .contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC));

        if (!found) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  javaClass.getName() + " does not declare a public execute(...) method"));
        }
      }
    };
  }

  public static ArchCondition<JavaClass> notDeclareCheckedExceptions() {

    return new ArchCondition<>("not declare checked exceptions") {
      @Override
      public void check(
          com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
        for (JavaMethod method : javaClass.getMethods()) {
          if (!method.getThrowsClause().isEmpty()) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    javaClass.getName() + " declares exceptions in method " + method.getName()));
          }
        }
      }
    };
  }

  public static ArchCondition<JavaClass> implementAtLeastOneInterface() {
    return new ArchCondition<>("implement at least one interface") {

      @Override
      public void check(final JavaClass javaClass, final ConditionEvents events) {
        if (javaClass.getInterfaces().isEmpty()) {
          final var message = javaClass.getName() + " does not implement any application port";

          events.add(SimpleConditionEvent.violated(javaClass, message));
        }
      }
    };
  }

  public static ArchCondition<JavaClass> notBeAbstract() {
    return new ArchCondition<>("not be abstract") {
      @Override
      public void check(final JavaClass javaClass, final ConditionEvents events) {
        if (javaClass.getModifiers().contains(JavaModifier.ABSTRACT)) {
          final var message = javaClass.getName() + " is abstract";
          events.add(SimpleConditionEvent.violated(javaClass, message));
        }
      }
    };
  }
}
