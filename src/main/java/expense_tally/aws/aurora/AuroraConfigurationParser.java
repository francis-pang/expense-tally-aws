package expense_tally.aws.aurora;

import expense_tally.aws.AppStartUpException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AuroraConfigurationParser {
  private static final Logger LOGGER = LogManager.getLogger(AuroraConfigurationParser.class);

  private AuroraConfigurationParser() {
  }

  public static AuroraDatabaseConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    Optional<String> databaseUrl = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
    if (databaseUrl.isEmpty()) {
      String errorMessage = String.format("Compulsory configuration %s is missing",
          AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
      throw new AppStartUpException(errorMessage);
    }
    AuroraDatabaseConfiguration.Builder appConfigurationBuilder =
        new AuroraDatabaseConfiguration.Builder(databaseUrl.get());
    Optional<String> databaseName =
        parseSingleConfiguration(AuroraConfigurationEnum.EXPENSE_MANAGER_DATABASE_NAME.key());
    if (databaseName.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.databaseName(databaseName.get());
    }
    Optional<String> environmentId =
        parseSingleConfiguration(AuroraConfigurationEnum.AURORA_ENVIRONMENTAL_ID.key());
    if (environmentId.isPresent()) {
      appConfigurationBuilder =
          appConfigurationBuilder.environmentId(environmentId.get());
    }
    Optional<String> username = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_USERNAME.key());
    Optional<String> password = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_PASSWORD.key());
    if (username.isPresent() && password.isPresent()) {
      appConfigurationBuilder = appConfigurationBuilder.credential(username.get(),
          password.get());
    } else if (username.isPresent()) {
      LOGGER.atInfo().log("Username {} is provided without password.", username.get());
    } else if (password.isPresent()) {
      LOGGER.atInfo().log("Password is provided without username.");
    }
    Optional<String> connectionTimeout = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_CONNECT_TIMEOUT
        .key());
    if (connectionTimeout.isPresent()) {
      String connectionTimeoutString = connectionTimeout.get();
      if (!NumberUtils.isDigits(connectionTimeoutString)) {
        LOGGER.atWarn().log("connectionTimeoutString is not number: {}", connectionTimeoutString);
        throw new AppStartUpException(AuroraConfigurationEnum.AURORA_CONNECT_TIMEOUT.key() + " is not numeric.");
      }
      int connectionTimeoutMilliseconds = Integer.parseInt(connectionTimeoutString);
      appConfigurationBuilder.setConnectionTimeout(connectionTimeoutMilliseconds);
    }
    return appConfigurationBuilder.build();
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = System.getenv(keyName);
    return Optional.ofNullable(nullableValue);
  }
}
