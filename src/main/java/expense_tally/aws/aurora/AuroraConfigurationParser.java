package expense_tally.aws.aurora;

import expense_tally.aws.AppStartUpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AuroraConfigurationParser {
  private static final Logger LOGGER = LogManager.getLogger(AuroraConfigurationParser.class);

  private AuroraConfigurationParser() {
  }

  public static AuroraDatabaseConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    Optional<String> destinationDbUrl = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
    if (destinationDbUrl.isEmpty()) {
      String errorMessage = String.format("Compulsory configuration %s is missing",
          AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
      throw new AppStartUpException(errorMessage);
    }
    AuroraDatabaseConfiguration.Builder appConfigurationBuilder =
        new AuroraDatabaseConfiguration.Builder(destinationDbUrl.get());
    Optional<String> destinationDbName =
        parseSingleConfiguration(AuroraConfigurationEnum.EXPENSE_MANAGER_DATABASE_NAME.key());
    if (destinationDbName.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.destinationDbName(destinationDbName.get());
    }
    Optional<String> destinationDbEnvId =
        parseSingleConfiguration(AuroraConfigurationEnum.AURORA_ENVIRONMENTAL_ID.key());
    if (destinationDbEnvId.isPresent()) {
      appConfigurationBuilder =
          appConfigurationBuilder.destinationDbEnvId(destinationDbEnvId.get());
    }
    Optional<String> dstntnDbUsername = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_USERNAME.key());
    Optional<String> dstntnDbPassword = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_PASSWORD.key());
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
