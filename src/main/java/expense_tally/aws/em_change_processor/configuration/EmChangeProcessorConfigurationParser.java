package expense_tally.aws.em_change_processor.configuration;

import expense_tally.aws.AppConfigEnum;
import expense_tally.aws.AppStartUpException;
import expense_tally.aws.aurora.AuroraConfigurationParser;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.config.SystemProxy;

import java.util.Optional;

public class EmChangeProcessorConfigurationParser {

  /**
   * Make implicit constructor private as there is no need to initialise class
   */
  private EmChangeProcessorConfigurationParser() {
  }

  public static EmChangeProcessorConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration =
        AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
    EmChangeProcessorConfiguration.Builder appConfigurationBuilder = new EmChangeProcessorConfiguration.Builder(auroraDatabaseConfiguration);

    Optional<String> sourceDbFilePath = parseSingleConfiguration(AppConfigEnum.EXPENSE_MANAGER_FILE_PATH.key());
    if (sourceDbFilePath.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.localDbFilePath(sourceDbFilePath.get());
    }
    Optional<String> sourceDbEnvId =
        parseSingleConfiguration(AppConfigEnum.EXPENSE_REPORT_ENVIRONMENTAL_ID.key());
    if (sourceDbEnvId.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.sourceDbEnvId(sourceDbEnvId.get());
    }
    return appConfigurationBuilder.build();
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = SystemProxy.getEnvironmentVariable(keyName);
    return Optional.ofNullable(nullableValue);
  }
}
