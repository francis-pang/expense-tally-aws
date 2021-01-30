package expense_tally.aws.em_change_processor.configuration;

import expense_tally.aws.em_change_processor.AppStartUpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ConfigurationParser {
  private static final Logger LOGGER = LogManager.getLogger(ConfigurationParser.class);

  private ConfigurationParser() {
  }

  public static AppConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    Optional<String> destinationDbUrl = parseSingleConfiguration(AppConfigEnum.AURORA_DATABASE_URL.key());
    if (destinationDbUrl.isEmpty()) {
      String errorMessage = String.format("Compulsory configuration %s is missing",
          AppConfigEnum.AURORA_DATABASE_URL.key());
      throw new AppStartUpException(errorMessage);
    }
    AppConfiguration.Builder appConfigurationBuilder = new AppConfiguration.Builder(destinationDbUrl.get());
    Optional<String> localDbFilePath = parseSingleConfiguration(AppConfigEnum.EXPENSE_MANAGER_FILE_PATH.key());
    if (localDbFilePath.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.localDbFilePath(localDbFilePath.get());
    }
    Optional<String> sourceDbEnvId =
        parseSingleConfiguration(AppConfigEnum.EXPENSE_REPORT_ENVIRONMENTAL_ID.key());
    if (sourceDbEnvId.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.sourceDbEnvId(sourceDbEnvId.get());
    }
    Optional<String> destinationDbName =
        parseSingleConfiguration(AppConfigEnum.EXPENSE_MANAGER_DATABASE_NAME.key());
    if (destinationDbName.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.destinationDbName(destinationDbName.get());
    }
    Optional<String> destinationDbEnvId =
        parseSingleConfiguration(AppConfigEnum.AURORA_ENVIRONMENTAL_ID.key());
    if (destinationDbEnvId.isPresent()) {
      appConfigurationBuilder =
          appConfigurationBuilder.destinationDbEnvId(destinationDbEnvId.get());
    }
    Optional<String> dstntnDbUsername = parseSingleConfiguration(AppConfigEnum.AURORA_USERNAME.key());
    Optional<String> dstntnDbPassword = parseSingleConfiguration(AppConfigEnum.AURORA_PASSWORD.key());
    if (dstntnDbUsername.isPresent() && dstntnDbPassword.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.destinationDbCredential(dstntnDbUsername.get(),
          dstntnDbPassword.get());
    } else if (dstntnDbUsername.isPresent()) {
      LOGGER.atInfo().log("Username {} is provided without password.", dstntnDbUsername.get());
    } else if (dstntnDbPassword.isPresent()) {
      LOGGER.atInfo().log("Password is provided without username.");
    }
    return appConfigurationBuilder.build();
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = System.getenv(keyName);
    return Optional.ofNullable(nullableValue);
  }
}
