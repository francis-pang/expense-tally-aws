package expense_tally.aws.csv_reader.configuration;

import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.StringJoiner;

public class AppConfiguration {
  private static final String DEFAULT_CSV_FILE_PATH = "/tmp/transaction.csv";

  private final File csvFile;
  private final AuroraDatabaseConfiguration auroraDatabaseConfiguration;

  private AppConfiguration(File csvFile, AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
    this.csvFile = csvFile;
    this.auroraDatabaseConfiguration = auroraDatabaseConfiguration;
  }

  public File getCsvFile() {
    return csvFile;
  }

  public AuroraDatabaseConfiguration getAuroraDatabaseConfiguration() {
    return auroraDatabaseConfiguration;
  }

  public static AppConfiguration create(AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
    File file = new File(DEFAULT_CSV_FILE_PATH);
    return new AppConfiguration(file, auroraDatabaseConfiguration);
  }

  public static AppConfiguration create(String csvFilePath,
                                        AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
    File file = new File(csvFilePath);
    return new AppConfiguration(file, auroraDatabaseConfiguration);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    AppConfiguration that = (AppConfiguration) o;

    return new EqualsBuilder()
        .append(csvFile, that.csvFile)
        .append(auroraDatabaseConfiguration, that.auroraDatabaseConfiguration)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(csvFile)
        .append(auroraDatabaseConfiguration)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
        .add("csvFile=" + csvFile)
        .add("auroraDatabaseConfiguration=" + auroraDatabaseConfiguration)
        .toString();
  }
}
