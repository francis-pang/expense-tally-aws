package expense_tally.aws.aurora;

import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringJoiner;

public class AuroraDatabaseConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(AuroraDatabaseConfiguration.class);

  private final String hostUrl;
  private final String databaseName;
  private final String username;
  private final String password;
  private final String environmentId;
  private final int connectionTimeout;

  private AuroraDatabaseConfiguration(String hostUrl,
                                     String databaseName,
                                     String username,
                                     String password,
                                     String environmentId,
                                     int connectionTimeout) {
    this.hostUrl = hostUrl;
    this.databaseName = databaseName;
    this.username = username;
    this.password = password;
    this.environmentId = environmentId;
    this.connectionTimeout = connectionTimeout;
  }

  public String getHostUrl() {
    return hostUrl;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEnvironmentId() {
    return environmentId;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuroraDatabaseConfiguration that = (AuroraDatabaseConfiguration) o;
    return new EqualsBuilder()
        .append(connectionTimeout, that.connectionTimeout)
        .append(hostUrl, that.hostUrl)
        .append(databaseName, that.databaseName)
        .append(username, that.username)
        .append(password, that.password)
        .append(environmentId, that.environmentId)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(hostUrl)
        .append(databaseName)
        .append(username)
        .append(password)
        .append(environmentId)
        .append(connectionTimeout)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AuroraDatabaseConfiguration.class.getSimpleName() + "[", "]")
        .add("hostUrl='" + hostUrl + "'")
        .add("databaseName='" + databaseName + "'")
        .add("username='" + username + "'")
        .add("password='" + "*****" + "'")
        .add("environmentId='" + environmentId + "'")
        .add("connectionTimeout=" + connectionTimeout)
        .toString();
  }

  public static class Builder {
    private static final String DEFAULT_DATABASE_NAME = "expense_manager";
    private static final String DEFAULT_ENVIRONMENT_ID = "mysql";
    private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;

    private String hostUrl;
    private String databaseName;
    private String username;
    private String password;
    private String environmentId;
    private int connectionTimeout;

    public Builder(String hostUrl) {
      if (StringUtils.isBlank(hostUrl)) {
        LOGGER.atWarn().log("DatabaseHostUrl is blank:{}",
            StringResolver.resolveNullableString(hostUrl));
        throw new IllegalArgumentException("Database host URL cannot be null or blank.");
      }
      this.hostUrl = hostUrl;
      this.databaseName = DEFAULT_DATABASE_NAME;
      this.environmentId = DEFAULT_ENVIRONMENT_ID;
      this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    }

    public Builder databaseName(String databaseName) {
      if (StringUtils.isBlank(databaseName)) {
        LOGGER.atWarn().log("databaseName is blank:{}",
            StringResolver.resolveNullableString(databaseName));
        throw new IllegalArgumentException("Database name cannot be null or blank.");
      }
      this.databaseName = databaseName;
      return this;
    }

    public Builder credential(String username, String password) {
      if (StringUtils.isBlank(username)) {
        LOGGER.atWarn().log("username is blank:{}",
            StringResolver.resolveNullableString(username));
        throw new IllegalArgumentException("Database username cannot be null or blank.");
      }
      if (StringUtils.isBlank(password)) {
        LOGGER.atWarn().log("password is blank:{}",
            StringResolver.resolveNullableString(password));
        throw new IllegalArgumentException("Database password cannot be null or blank.");
      }
      this.username = username;
      this.password = password;
      return this;
    }

    public Builder environmentId(String environmentId) {
      if (StringUtils.isBlank(environmentId)) {
        LOGGER.atWarn().log("environmentId is blank:{}",
            StringResolver.resolveNullableString(environmentId));
        throw new IllegalArgumentException("Database environment ID cannot be null or blank.");
      }
      this.environmentId = environmentId;
      return this;
    }

    public Builder connectionTimeout(int connectionTimeout) {
      if (connectionTimeout <= 0) {
        LOGGER.atWarn().log("connectionTimeout is non-positive:{}", connectionTimeout);
        throw new IllegalArgumentException("Connection timeout must be positive.");
      }
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    public AuroraDatabaseConfiguration build() {
      return new AuroraDatabaseConfiguration(
          hostUrl,
          databaseName,
          username,
          password,
          environmentId,
          connectionTimeout
      );
    }
  }
}
