package expense_tally.aws.aurora;

import expense_tally.aws.em_change_processor.configuration.configuration.AppConfiguration;
import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.StringJoiner;

public class AuroraDatabaseConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(AuroraDatabaseConfiguration.class);

  private final String destinationDbHostUrl;
  private final String destinationDbName;
  private String dstntnDbUsername;
  private String dstntnDbPassword;
  private final String destinationDbEnvId;

  private AuroraDatabaseConfiguration(String destinationDbHostUrl,
                                     String destinationDbName,
                                     String dstntnDbUsername,
                                     String dstntnDbPassword,
                                     String destinationDbEnvId) {
    this.destinationDbHostUrl = destinationDbHostUrl;
    this.destinationDbName = destinationDbName;
    this.dstntnDbUsername = dstntnDbUsername;
    this.dstntnDbPassword = dstntnDbPassword;
    this.destinationDbEnvId = destinationDbEnvId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    AuroraDatabaseConfiguration that = (AuroraDatabaseConfiguration) o;

    return new EqualsBuilder()
        .append(destinationDbHostUrl, that.destinationDbHostUrl)
        .append(destinationDbName, that.destinationDbName)
        .append(dstntnDbUsername, that.dstntnDbUsername)
        .append(dstntnDbPassword, that.dstntnDbPassword)
        .append(destinationDbEnvId, that.destinationDbEnvId)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(destinationDbHostUrl)
        .append(destinationDbName)
        .append(dstntnDbUsername)
        .append(dstntnDbPassword)
        .append(destinationDbEnvId)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AuroraDatabaseConfiguration.class.getSimpleName() + "[", "]")
        .add("destinationDbHostUrl='" + destinationDbHostUrl + "'")
        .add("destinationDbName='" + destinationDbName + "'")
        .add("dstntnDbUsername='" + dstntnDbUsername + "'")
        .add("dstntnDbPassword='" + "*****" + "'")
        .add("destinationDbEnvId='" + destinationDbEnvId + "'")
        .toString();
  }

  public static class Builder {
    private static final String DEFAULT_DESTINATION_DATABASE_NAME = "expense_manager";
    private static final String DEFAULT_DESTINATION_DATABASE_ENVIRONMENT_ID = "mysql";

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
      this.destinationDbHostUrl = destinationDbHostUrl;
      this.destinationDbName = DEFAULT_DESTINATION_DATABASE_NAME;
      this.destinationDbEnvId = DEFAULT_DESTINATION_DATABASE_ENVIRONMENT_ID;
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

    public AuroraDatabaseConfiguration build() {
      return new AuroraDatabaseConfiguration(
          destinationDbHostUrl,
          destinationDbName,
          dstntnDbUsername,
          dstntnDbPassword,
          destinationDbEnvId
      );
    }
  }
}
