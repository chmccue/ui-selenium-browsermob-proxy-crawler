package suites;

import courgette.api.CucumberOptions;
import courgette.api.CourgetteOptions;
import courgette.api.CourgetteRunLevel;
import courgette.api.junit.Courgette;
import org.junit.runner.RunWith;

@RunWith(Courgette.class)
@CourgetteOptions(
    threads = 1,
    runLevel = CourgetteRunLevel.SCENARIO,
    rerunFailedScenarios = true,
    rerunAttempts = 1,
    reportTargetDir = "build/chrome",
    plugin = "extentreports",
    cucumberOptions = @CucumberOptions(
        features = "src/test/resources/featureFiles",
        glue = "steps",
        dryRun = true,
        tags = {"@crawler-csp-high-priority and not (@ignore)"}
    ))

public class ChromeTestSuite {
}
