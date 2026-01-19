package acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

class RunAcceptanceTest {

  private static final String CONFIG_PATH = "classpath:acceptance/resources/config";
  private static final String FEATURES_PATH = "classpath:acceptance/features";

  @Test
  void allAcceptanceTests() {
    final var results =
        Runner.path(FEATURES_PATH)
            .configDir(CONFIG_PATH)
            .tags("@test", "~@disabled")
            .outputCucumberJson(true)
            .parallel(4);

    assertEquals(0, results.getFailCount(), results.getErrorMessages());
  }
}
