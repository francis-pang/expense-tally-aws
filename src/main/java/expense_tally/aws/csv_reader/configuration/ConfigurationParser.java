package expense_tally.aws.csv_reader.configuration;

import expense_tally.aws.aurora.AuroraConfigurationParser;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.AppStartUpException;
import expense_tally.aws.AppConfigEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ConfigurationParser {
  private ConfigurationParser() {
  }

  public static AppConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration =
        AuroraConfigurationParser.parseSystemEnvironmentVariableConfiguration();

    Optional<String> csvFilePath = parseSingleConfiguration(AppConfigEnum.CSV_FILE_PATH.key());
    return csvFilePath.map(s -> AppConfiguration.create(s, auroraDatabaseConfiguration))
        .orElseGet(() -> AppConfiguration.create(auroraDatabaseConfiguration));
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = System.getenv(keyName);
    return Optional.ofNullable(nullableValue);
  }
}


