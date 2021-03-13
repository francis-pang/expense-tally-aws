package expense_tally.aws.aurora;

import expense_tally.aws.AppStartUpException;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration.Builder;
import expense_tally.aws.config.SystemProxy;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * This class is used to parse all possible sources of configuration for an AWS Aurora database server.
 * <p>
 *   The different configurable options is stated in the {@link AuroraConfigurationEnum}.<br/>
 *   In this set-up, both username and password need to be provided or not at all.
 * </p>
 * @see AuroraConfigurationEnum
 */
public class AuroraConfigurationParser {
  private static final Logger LOGGER = LogManager.getLogger(AuroraConfigurationParser.class);

  /**
   * Default constructor. Made private to disallow object initialisation.
   */
  private AuroraConfigurationParser() {
  }

  /**
   * Parses all the database configuration through system environment variables.
   * @return a {@link AuroraDatabaseConfiguration} containing all the database configuration
   * @throws AppStartUpException if compulsory parameters are not provided, or provided configuration is of wrong
   * data type
   */
  public static AuroraDatabaseConfiguration parseSystemEnvironmentVariableConfigurations() throws AppStartUpException {
    Builder auroraDatabaseConfigBuilder = parseDatabaseUrl();
    auroraDatabaseConfigBuilder = parseDatabaseName(auroraDatabaseConfigBuilder);
    auroraDatabaseConfigBuilder = parseEnvironmentId(auroraDatabaseConfigBuilder);
    auroraDatabaseConfigBuilder = parseCredential(auroraDatabaseConfigBuilder);
    auroraDatabaseConfigBuilder = parseConnectionTimeout(auroraDatabaseConfigBuilder);
    return auroraDatabaseConfigBuilder.build();
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = SystemProxy.getEnvironmentVariable(keyName);
    return Optional.ofNullable(nullableValue);
  }

  private static Builder parseDatabaseUrl() throws AppStartUpException {
    Optional<String> databaseUrl = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
    if (databaseUrl.isEmpty()) {
      String errorMessage = String.format("Compulsory configuration %s is missing",
          AuroraConfigurationEnum.AURORA_DATABASE_URL.key());
      throw new AppStartUpException(errorMessage);
    }
    return new Builder(databaseUrl.get());
  }

  private static Builder parseEnvironmentId(Builder appConfigurationBuilder) {
    Optional<String> environmentId = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_ENVIRONMENTAL_ID.key());
    if (environmentId.isPresent()) {
      return appConfigurationBuilder.environmentId(environmentId.get());
    }
    return appConfigurationBuilder;
  }

  private static Builder parseDatabaseName(Builder appConfigurationBuilder) {
    Optional<String> databaseName =
        parseSingleConfiguration(AuroraConfigurationEnum.EXPENSE_MANAGER_DATABASE_NAME.key());
    if (databaseName.isPresent()) {
      return appConfigurationBuilder.databaseName(databaseName.get());
    }
    return appConfigurationBuilder;
  }

  private static Builder parseCredential(Builder appConfigurationBuilder) {
    Optional<String> username = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_USERNAME.key());
    Optional<String> password = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_PASSWORD.key());
    if (username.isPresent() && password.isPresent()) {
      return appConfigurationBuilder.credential(username.get(), password.get());
    } else if (username.isPresent()) {
      LOGGER.atInfo().log("Username {} is provided without password.", username.get());
    } else if (password.isPresent()) {
      LOGGER.atInfo().log("Password is provided without username.");
    }
    return appConfigurationBuilder;
  }

  private static Builder parseConnectionTimeout(Builder appConfigurationBuilder) throws AppStartUpException {
    Optional<String> connectionTimeout = parseSingleConfiguration(AuroraConfigurationEnum.AURORA_CONNECT_TIMEOUT
        .key());
    if (connectionTimeout.isEmpty()) {
      return appConfigurationBuilder;
    }
    String connectionTimeoutString = connectionTimeout.get();
    if (!NumberUtils.isDigits(connectionTimeoutString)) {
      LOGGER.atWarn().log("connectionTimeoutString is not number: {}", connectionTimeoutString);
      throw new AppStartUpException(AuroraConfigurationEnum.AURORA_CONNECT_TIMEOUT.key() + " is not numeric.");
    }
    int connectionTimeoutMilliseconds = Integer.parseInt(connectionTimeoutString);
    return appConfigurationBuilder.connectionTimeout(connectionTimeoutMilliseconds);
  }

}
