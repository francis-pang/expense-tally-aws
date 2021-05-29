package expense_tally.aws.csv_reader.configuration;

import expense_tally.aws.AppConfigEnum;
import expense_tally.aws.AppStartUpException;
import expense_tally.aws.aurora.AuroraConfigurationParser;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;

import java.util.Optional;

public class CsvReaderConfigurationParser {

  /**
   * Make implicit constructor private as there is no need to initialise class
   */
  private CsvReaderConfigurationParser() {
  }

  public static CsvReaderConfiguration parseSystemEnvironmentVariableConfiguration() throws AppStartUpException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration =
        AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();

    Optional<String> csvFilePath = parseSingleConfiguration(AppConfigEnum.CSV_FILE_PATH.key());
    return csvFilePath.map(s -> CsvReaderConfiguration.create(s, auroraDatabaseConfiguration))
        .orElseGet(() -> CsvReaderConfiguration.create(auroraDatabaseConfiguration));
  }

  private static Optional<String> parseSingleConfiguration(String keyName) {
    String nullableValue = System.getenv(keyName);
    return Optional.ofNullable(nullableValue);
  }
}


