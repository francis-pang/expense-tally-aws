package expense_tally.aws.csv_reader.configuration;

import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.em_change_processor.configuration.EmChangeProcessorConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EmChangeProcessorConfigurationTest {
  @Mock
  private AuroraDatabaseConfiguration mockAuroraDatabaseConfiguration;

  @InjectMocks
  private EmChangeProcessorConfiguration testEmChangeProcessorConfiguration;

  @Test
  void getLocalDbFilePath() {
    var localTestEmChangeProcessorConfiguration = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration)
        .localDbFilePath("/local/file/path")
        .sourceDbEnvId("/source/db/envId")
        .build();

    assertThat(localTestEmChangeProcessorConfiguration.getLocalDbFilePath())
        .isNotNull()
        .isEqualTo("/local/file/path");
  }

  @Test
  void getSourceDbEnvId() {
    var localTestEmChangeProcessorConfiguration = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration)
        .localDbFilePath("/local/file/path")
        .sourceDbEnvId("/source/db/envId")
        .build();

    assertThat(localTestEmChangeProcessorConfiguration.getSourceDbEnvId())
        .isNotNull()
        .isEqualTo("/source/db/envId");
  }

  @Test
  void getAuroraDatabaseConfiguration() {
    assertThat(testEmChangeProcessorConfiguration.getAuroraDatabaseConfiguration())
        .isNotNull()
        .isEqualTo(mockAuroraDatabaseConfiguration);
  }

  @Test
  void equals_sameObject() {
    assertThat(testEmChangeProcessorConfiguration.equals(testEmChangeProcessorConfiguration))
        .isTrue();
  }

  @Test
  void equals_null() {
    assertThat(testEmChangeProcessorConfiguration.equals(null))
        .isFalse();
  }

  @Test
  void equals_differentClass() {
    assertThat(testEmChangeProcessorConfiguration.equals("af"))
        .isFalse();
  }

  @Test
  void equals_sameProperty() {
    var localTestEmChangeProcessorConfiguration1 = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration)
        .localDbFilePath("/local/file/path")
        .sourceDbEnvId("/source/db/envId")
        .build();

    var localTestEmChangeProcessorConfiguration2 = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration)
        .localDbFilePath("/local/file/path")
        .sourceDbEnvId("/source/db/envId")
        .build();

    assertThat(localTestEmChangeProcessorConfiguration1.equals(localTestEmChangeProcessorConfiguration2))
        .isTrue();
  }

  @Test
  void Builder_positive() {
    assertThat(new EmChangeProcessorConfiguration.Builder(mockAuroraDatabaseConfiguration))
        .isNotNull();

    // With these verification, we do not need to create test case to test the behaviour if any of the attributes
    // inside mockAuroraDatabaseConfiguration is null or in illegal state.
    Mockito.verifyNoInteractions(mockAuroraDatabaseConfiguration);
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getHostUrl();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getDatabaseName();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getUsername();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getPassword();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getEnvironmentId();
    Mockito.verify(mockAuroraDatabaseConfiguration, Mockito.never()).getConnectionTimeout();
  }

  @Test
  void Builder_nullAuroraDatabaseConfiguration() {
    assertThatThrownBy(() -> new EmChangeProcessorConfiguration.Builder(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void localDbFilePath_positive() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThat(testEmChangeProcessorConfigurationBuilder.localDbFilePath("/local/db/file/path"))
        .isNotNull()
        .isEqualTo(testEmChangeProcessorConfigurationBuilder);
  }

  @Test
  void localDbFilePath_nullLocalDbFilePath() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.localDbFilePath(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Local database file path cannot be null or blank.");
  }

  @Test
  void localDbFilePath_emptyLocalDbFilePath() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.localDbFilePath(StringUtils.EMPTY))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Local database file path cannot be null or blank.");
  }

  @Test
  void localDbFilePath_blankLocalDbFilePath() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.localDbFilePath(StringUtils.SPACE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Local database file path cannot be null or blank.");
  }

  @Test
  void sourceDbEnvId_positive() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThat(testEmChangeProcessorConfigurationBuilder.sourceDbEnvId("/source/db/env/id"))
        .isNotNull()
        .isEqualTo(testEmChangeProcessorConfigurationBuilder);
  }

  @Test
  void sourceDbEnvId_nullSourceDbEnvId() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.sourceDbEnvId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source database environment ID cannot be null or blank.");
  }

  @Test
  void sourceDbEnvId_emptySourceDbEnvId() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.sourceDbEnvId(StringUtils.EMPTY))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source database environment ID cannot be null or blank.");
  }

  @Test
  void sourceDbEnvId_blankSourceDbEnvId() {
    var testEmChangeProcessorConfigurationBuilder = new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration);
    assertThatThrownBy(() -> testEmChangeProcessorConfigurationBuilder.sourceDbEnvId(StringUtils.SPACE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source database environment ID cannot be null or blank.");
  }

  @Test
  void build_withAllDefault() {
    assertThat(new EmChangeProcessorConfiguration.Builder(mockAuroraDatabaseConfiguration).build())
        .isNotNull()
        .extracting(
            EmChangeProcessorConfiguration::getAuroraDatabaseConfiguration,
            EmChangeProcessorConfiguration::getLocalDbFilePath,
            EmChangeProcessorConfiguration::getSourceDbEnvId
        )
        .containsExactly(
            mockAuroraDatabaseConfiguration,
            "/tmp/expense_manager.db",
            "file_sqlite"
        );
  }

  @Test
  void build_withNonDefault() {
    assertThat(new EmChangeProcessorConfiguration
        .Builder(mockAuroraDatabaseConfiguration)
        .localDbFilePath("/local/db/file/path")
        .sourceDbEnvId("/source/db/env/id")
        .build())
        .isNotNull()
        .extracting(
            EmChangeProcessorConfiguration::getAuroraDatabaseConfiguration,
            EmChangeProcessorConfiguration::getLocalDbFilePath,
            EmChangeProcessorConfiguration::getSourceDbEnvId
        )
        .containsExactly(
            mockAuroraDatabaseConfiguration,
            "/local/db/file/path",
            "/source/db/env/id"
        );
  }
}