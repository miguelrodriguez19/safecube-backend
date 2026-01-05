package integration.annotation;

import com.miguelrodriguez19.safecube.SafeCubeBackendApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public @interface IntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {SafeCubeBackendApplication.class};

  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {};
}