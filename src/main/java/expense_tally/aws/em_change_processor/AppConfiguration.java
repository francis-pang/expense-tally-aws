package expense_tally.aws.em_change_processor;


import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;


public class AppConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(AppConfiguration.class);

  private final String localDbFilePath;
  private final String sourceDbEnvId;
  private final String destinationDbHostUrl;
  private final String destinationDbName;
  private String dstntnDbUsername;
  private String dstntnDbPassword;
  private final String destinationDbEnvId;

  public AppConfiguration(String localDbFilePath,
                          String sourceDbEnvId,
                          String destinationDbHostUrl,
                          String destinationDbName,
                          String dstntnDbUsername,
                          String dstntnDbPassword,
                          String destinationDbEnvId) {
    this.localDbFilePath = localDbFilePath;
    this.sourceDbEnvId = sourceDbEnvId;
    this.destinationDbHostUrl = destinationDbHostUrl;
    this.destinationDbName = destinationDbName;
    this.dstntnDbUsername = dstntnDbUsername;
    this.dstntnDbPassword = dstntnDbPassword;
    this.destinationDbEnvId = destinationDbEnvId;
  }

  public String getLocalDbFilePath() {
    return localDbFilePath;
  }

  public String getSourceDbEnvId() {
    return sourceDbEnvId;
  }

  public String getDestinationDbHostUrl() {
    return destinationDbHostUrl;
  }

  public String getDestinationDbName() {
    return destinationDbName;
  }

  public String getDstntnDbUsername() {
    return dstntnDbUsername;
  }

  public String getDstntnDbPassword() {
    return dstntnDbPassword;
  }

  public String getDestinationDbEnvId() {
    return destinationDbEnvId;
  }

  public static class Builder {
    private static final String DEFAULT_LOCAL_DATABASE_FILE_PATH = "/tmp/expense_manager.db";
    private static final String DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID = "file_sqlite";
    private static final String DEFAULT_DESTINATION_DATABASE_NAME = "expense_manager";
    private static final String DEFAULT_DESTINATION_DATABASE_ENVIRONMENT_ID = "mysql";

    private String localDbFilePath;
    private String sourceDbEnvId;
    private String destinationDbHostUrl;
    private String destinationDbName;
    private String dstntnDbUsername;
    private String dstntnDbPassword;
    private String destinationDbEnvId;

    public Builder(String destinationDbHostUrl) {
      if (StringUtils.isBlank(destinationDbHostUrl)) {
        LOGGER.atWarn().log("destinationDatabaseHostUrl is blank:{}",
            StringResolver.resolveNullableString(destinationDbHostUrl));
        throw new IllegalArgumentException("Destination database host URL cannot be null or blank.");
      }
      this.localDbFilePath = DEFAULT_LOCAL_DATABASE_FILE_PATH;
      this.sourceDbEnvId = DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID;
      this.destinationDbHostUrl = destinationDbHostUrl;
      this.destinationDbName = DEFAULT_DESTINATION_DATABASE_NAME;
      this.destinationDbEnvId = DEFAULT_DESTINATION_DATABASE_ENVIRONMENT_ID;
    }

    public Builder localDbFilePath(String localDbFilePath) {
      if (StringUtils.isBlank(localDbFilePath)) {
        LOGGER.atWarn().log("localDbFilePath is blank:{}",
            StringResolver.resolveNullableString(localDbFilePath));
        throw new IllegalArgumentException("Local database file path cannot be null or blank.");
      }
      this.localDbFilePath = localDbFilePath;
      return this;
    }

    public Builder sourceDbEnvId(String sourceDbEnvId) {
      if (StringUtils.isBlank(sourceDbEnvId)) {
        LOGGER.atWarn().log("sourceDbEnvId is blank:{}",
            StringResolver.resolveNullableString(sourceDbEnvId));
        throw new IllegalArgumentException("Source database environment ID cannot be null or blank.");
      }
      this.sourceDbEnvId = sourceDbEnvId;
      return this;
    }

    public Builder destinationDbName(String destinationDbName) {
      if (StringUtils.isBlank(destinationDbName)) {
        LOGGER.atWarn().log("destinationDbName is blank:{}",
            StringResolver.resolveNullableString(destinationDbName));
        throw new IllegalArgumentException("Destination database name cannot be null or blank.");
      }
      this.destinationDbName = destinationDbName;
      return this;
    }

    public Builder destinationDbCredential(String username, String password) {
      if (StringUtils.isBlank(username)) {
        LOGGER.atWarn().log("username is blank:{}",
            StringResolver.resolveNullableString(username));
        throw new IllegalArgumentException("Destination database username cannot be null or blank.");
      }
      if (StringUtils.isBlank(password)) {
        LOGGER.atWarn().log("password is blank:{}",
            StringResolver.resolveNullableString(password));
        throw new IllegalArgumentException("Destination database password cannot be null or blank.");
      }
      this.dstntnDbUsername = username;
      this.dstntnDbPassword = password;
      return this;
    }

    public Builder destinationDbEnvId(String destinationDbEnvId) {
      if (StringUtils.isBlank(destinationDbEnvId)) {
        LOGGER.atWarn().log("destinationDbEnvId is blank:{}",
            StringResolver.resolveNullableString(destinationDbEnvId));
        throw new IllegalArgumentException("Destination database environment ID cannot be null or blank.");
      }
      this.destinationDbEnvId = destinationDbEnvId;
      return this;
    }

    public AppConfiguration build() {
      return new AppConfiguration(
          localDbFilePath,
          sourceDbEnvId,
          destinationDbHostUrl,
          destinationDbName,
          dstntnDbUsername,
          dstntnDbPassword,
          destinationDbEnvId
      );
    }
  }
}
