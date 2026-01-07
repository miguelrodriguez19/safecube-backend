package acceptance;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.karate.Runner;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class RunAcceptanceTest {
    private static final String ACCEPTANCE_TEST_DIR = "classpath:acceptance/";
    private static final String TARGET_DIR = "target";

    private static final String[] TAGS = {"@test", "~@disabled"};

    @Test
    void allAcceptanceTests() {
        final var results =
                Runner.path(ACCEPTANCE_TEST_DIR + "features")
                        .configDir(ACCEPTANCE_TEST_DIR + "resources/config")
                        .hooks(List.of())
                        .outputCucumberJson(true)
                        .backupReportDir(false)
                        .tags(TAGS)
                        .parallel(1);

        generateReport(results.getReportDir());

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    private void generateReport(String karateOutputPath) {
        final var configuration = new Configuration(new File(TARGET_DIR), "Safecube");

        final var jsonFiles =
                FileUtils.listFiles(new File(karateOutputPath), new String[] {"json"}, true);

        final var jsonPaths = new ArrayList<String>(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));

        final var reportBuilder = new ReportBuilder(jsonPaths, configuration);
        reportBuilder.generateReports();
    }
}

