package integration.annotation;

import com.miguelrodriguez19.safecube.SafeCubeBackendApplication;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public @interface JpaIntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {SafeCubeBackendApplication.class};

  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {};
}