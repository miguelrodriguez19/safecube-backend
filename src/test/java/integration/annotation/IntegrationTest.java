package integration.annotation;

import com.miguelrodriguez19.safecube.SafeCubeBackendApplication;
import integration.annotation.support.PostgreSQLInitializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ContextConfiguration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public @interface IntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {SafeCubeBackendApplication.class};

  @AliasFor(annotation = ContextConfiguration.class, attribute = "initializers")
  Class<?>[] initializers() default {PostgreSQLInitializer.class};
}
