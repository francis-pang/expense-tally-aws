package expense_tally.aws.csv_reader.configuration;

import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CsvReaderConfigurationTest {
  @Mock
  private File mockCsvFile;

  @Mock
  private AuroraDatabaseConfiguration mockAuroraDatabaseConfiguration;

  @InjectMocks
  private CsvReaderConfiguration testCsvReaderConfiguration;

  @Test
  void getCsvFile() {
    assertThat(testCsvReaderConfiguration.getCsvFile())
        .isEqualTo(mockCsvFile);
  }

  @Test
  void getAuroraDatabaseConfiguration() {
    assertThat(testCsvReaderConfiguration.getAuroraDatabaseConfiguration())
        .isEqualTo(mockAuroraDatabaseConfiguration);
  }

  @Test
  void create_positive() {
    final String testCsvFilePath = "/opt/invalid";
    assertThat(CsvReaderConfiguration.create(testCsvFilePath, mockAuroraDatabaseConfiguration))
        .isNotNull();

    // With these verification, we do not need to create test case to test the behaviour if any of the attributes
    // inside mockAuroraDatabaseConfiguration is null or in illegal state.
    Mockito.verifyNoInteractions(mockAuroraDatabaseConfiguration);
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getUsername();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getDatabaseName();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getEnvironmentId();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getConnectionTimeout();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getHostUrl();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never())
        .getPassword();
  }

  @Test
  void create_csvFilePathIsNull() {
    assertThatThrownBy(() -> CsvReaderConfiguration.create(null, mockAuroraDatabaseConfiguration))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_csvFilePathIsBlank() {
    assertThat(CsvReaderConfiguration.create(StringUtils.SPACE, mockAuroraDatabaseConfiguration))
        .isNotNull();
  }

  @Test
  void create_csvFilePathIsEmpty() {
    assertThat(CsvReaderConfiguration.create(StringUtils.EMPTY, mockAuroraDatabaseConfiguration))
        .isNotNull();
  }

  @Test
  void create_auroraDatabaseConfigurationIsNull() {
    final String testCsvFilePath = "/opt/invalid";
    assertThatThrownBy(() -> CsvReaderConfiguration.create(testCsvFilePath, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testEquals_sameObject() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.equals(testCsvReaderConfiguration))
        .isTrue();
  }

  @Test
  void testEquals_null() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.equals(null))
        .isFalse();
  }

  @Test
  void testEquals_differentClass() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.equals("ascawdc"))
        .isFalse();
  }

  @Test
  void testEquals_sameProperties() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    var expectedCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.equals(expectedCsvReaderConfiguration))
        .isTrue();
  }

  @Test
  void testHashCode() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.hashCode())
        .isNotZero();
  }

  @Test
  void testToString() {
    var testCsvReaderConfiguration = CsvReaderConfiguration.create(mockAuroraDatabaseConfiguration);
    assertThat(testCsvReaderConfiguration.toString())
      .hasToString("CsvReaderConfiguration[csvFile=/tmp/transaction.csv, " +
          "auroraDatabaseConfiguration=mockAuroraDatabaseConfiguration]");
  }
}