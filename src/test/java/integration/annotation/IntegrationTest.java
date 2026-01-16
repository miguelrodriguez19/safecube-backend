package integration.annotation;

import com.miguelrodriguez19.safecube.SafeCubeBackendApplication;
import integration.annotation.support.PostgresSQLInitializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles
@ContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {SafeCubeBackendApplication.class};

  @AliasFor(annotation = ContextConfiguration.class, attribute = "initializers")
  Class<?>[] initializers() default {PostgresSQLInitializer.class};

  @AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
  String[] profiles() default {"integration"};
}
