package expense_tally.aws.em_change_processor.configuration.configuration;


import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.StringJoiner;


public class AppConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(AppConfiguration.class);

  private final String localDbFilePath;
  private final String sourceDbEnvId;
  private final AuroraDatabaseConfiguration auroraDatabaseConfiguration;

  public AppConfiguration(String localDbFilePath,
                          String sourceDbEnvId,
                          AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
    this.localDbFilePath = localDbFilePath;
    this.sourceDbEnvId = sourceDbEnvId;
    this.auroraDatabaseConfiguration = auroraDatabaseConfiguration;
  }

  public String getLocalDbFilePath() {
    return localDbFilePath;
  }

  public String getSourceDbEnvId() {
    return sourceDbEnvId;
  }

  public AuroraDatabaseConfiguration getAuroraDatabaseConfiguration() {
    return auroraDatabaseConfiguration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    AppConfiguration that = (AppConfiguration) o;

    return new EqualsBuilder()
        .append(localDbFilePath, that.localDbFilePath)
        .append(sourceDbEnvId, that.sourceDbEnvId)
        .append(auroraDatabaseConfiguration, that.auroraDatabaseConfiguration)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(localDbFilePath)
        .append(sourceDbEnvId)
        .append(auroraDatabaseConfiguration)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
        .add("localDbFilePath='" + localDbFilePath + "'")
        .add("sourceDbEnvId='" + sourceDbEnvId + "'")
        .add("auroraDatabaseConfiguration=" + auroraDatabaseConfiguration)
        .toString();
  }

  public static class Builder {
    private static final String DEFAULT_LOCAL_DATABASE_FILE_PATH = "/tmp/expense_manager.db";
    private static final String DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID = "file_sqlite";

    private String localDbFilePath;
    private String sourceDbEnvId;
    private final AuroraDatabaseConfiguration auroraDatabaseConfiguration;

    public Builder(AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
      this.auroraDatabaseConfiguration = Objects.requireNonNull(auroraDatabaseConfiguration);
      this.localDbFilePath = DEFAULT_LOCAL_DATABASE_FILE_PATH;
      this.sourceDbEnvId = DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID;
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

    public AppConfiguration build() {
      return new AppConfiguration(
          localDbFilePath,
          sourceDbEnvId,
          auroraDatabaseConfiguration
      );
    }
  }
}
