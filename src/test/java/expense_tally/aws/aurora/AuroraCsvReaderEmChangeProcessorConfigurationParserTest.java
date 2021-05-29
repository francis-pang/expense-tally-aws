package expense_tally.aws.aurora;

import expense_tally.aws.AppStartUpException;
import expense_tally.aws.config.SystemProxy;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AuroraCsvReaderEmChangeProcessorConfigurationParserTest {
  @Test
  void parseSystemEnvironmentVariableConfigurations_allFilled() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_name")).thenReturn("testdb");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_username")).thenReturn("user");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn("pw");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_environment_id")).thenReturn("id");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn("10");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(10);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("testdb");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("id");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isEqualTo("pw");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isEqualTo("user");
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_onlyFillCompulsory() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_urlIsNull() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn(null);
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(AppStartUpException.class)
          .hasMessage("Compulsory configuration expense_manager_db_host_url is missing");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_urlIsEmpty() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database host URL cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_urlIsBlank() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("   ");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database host URL cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_dbNameisNull() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_name")).thenReturn(null);
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_dbNameIsBlank() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_name")).thenReturn("   ");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database name cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_dbNameIsEmpty() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_name")).thenReturn("");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database name cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_usernameIsFilled() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_username")).thenReturn(
          "onlyyou");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_usernameIsNull() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_username")).thenReturn(null);
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_usernameIsBlank() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_username")).thenReturn("   ");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_usernameIsEmpty() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_username")).thenReturn("");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_passwordIsFilled() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn(
          "onlyyou");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_passwordIsNull() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn(null);
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_passwordIsBlank() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn("   ");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_passwordIsEmpty() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn("");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_environmentIdIsFilled() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_environment_id")).thenReturn(
          "onlyyou");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("onlyyou");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_environmentIdIsNull() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_environment_id")).thenReturn(null);
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_environmentIdIsBlank() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() ->
          SystemProxy.getEnvironmentVariable("expense_manager_db_host_url"))
          .thenReturn("test.com");
      mockSystemProxy.when(() ->
          SystemProxy.getEnvironmentVariable("expense_manager_db_environment_id"))
          .thenReturn("   ");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database environment ID cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_environmentIdIsEmpty() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url"))
          .thenReturn("test.com");
      mockSystemProxy.when(() ->
          SystemProxy.getEnvironmentVariable("expense_manager_db_environment_id")).thenReturn("");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database environment ID cannot be null or blank.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_onlyPasswordIsFilled() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_password")).thenReturn("pw");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutIsProvided() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn("10");
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(10);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isNull();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutIsNull() throws AppStartUpException {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn(null);
      SoftAssertions softAssertions = new SoftAssertions();
      AuroraDatabaseConfiguration testAuroraDatabaseConfiguration =
          AuroraConfigurationParser.parseSystemEnvironmentVariableConfigurations();
      softAssertions.assertThat(testAuroraDatabaseConfiguration).isNotNull();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getConnectionTimeout()).isEqualTo(1000);
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getDatabaseName()).isEqualTo("expense_manager");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getEnvironmentId()).isEqualTo("mysql");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getHostUrl()).isEqualTo("test.com");
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getPassword()).isBlank();
      softAssertions.assertThat(testAuroraDatabaseConfiguration.getUsername()).isBlank();
      softAssertions.assertAll();
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutIsEmpty() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn(StringUtils.EMPTY);
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(AppStartUpException.class)
          .hasMessage("expense_manager_db_connection_timeout is not numeric.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutIsBlank() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn(StringUtils.SPACE);
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(AppStartUpException.class)
          .hasMessage("expense_manager_db_connection_timeout is not numeric.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutHasNonNumericCharacter() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn("1abc");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(AppStartUpException.class)
          .hasMessage("expense_manager_db_connection_timeout is not numeric.");
    }
  }

  @Test
  void parseSystemEnvironmentVariableConfigurations_timeoutIsNegative() {
    try (MockedStatic<SystemProxy> mockSystemProxy = Mockito.mockStatic(SystemProxy.class)) {
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_host_url")).thenReturn("test.com");
      mockSystemProxy.when(() -> SystemProxy.getEnvironmentVariable("expense_manager_db_connection_timeout"))
          .thenReturn("-10");
      assertThatThrownBy(AuroraConfigurationParser::parseSystemEnvironmentVariableConfigurations)
          .isInstanceOf(AppStartUpException.class)
          .hasMessage("expense_manager_db_connection_timeout is not numeric.");
    }
  }
}