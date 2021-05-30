package expense_tally.aws.em_change_processor.configuration;

import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * This is the application configuration required for execution of
 * {@link expense_tally.aws.em_change_processor.controller.ExpenseManagerFileChangeS3EventHandler}.
 */
public class EmChangeProcessorConfiguration {
  private static final Logger LOGGER = LogManager.getLogger(EmChangeProcessorConfiguration.class);

  private final String localDbFilePath;
  private final String sourceDbEnvId;
  private final AuroraDatabaseConfiguration auroraDatabaseConfiguration;

  /**
   * <b>Implementation detail</b>
   * <br/>
   * Private constructor because Builder design pattern has been adopted.
   * @param localDbFilePath intermediate file path to store the expense manager database file
   * @param sourceDbEnvId environment ID of the expense manager database file
   * @param auroraDatabaseConfiguration database configuration of the Aurora database
   */
  private EmChangeProcessorConfiguration(String localDbFilePath,
                                        String sourceDbEnvId,
                                        AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
    this.localDbFilePath = localDbFilePath;
    this.sourceDbEnvId = sourceDbEnvId;
    this.auroraDatabaseConfiguration = auroraDatabaseConfiguration;
  }

  /**
   * Returns the intermediate file path to store the expense manager database file
   * @return the intermediate file path to store the expense manager database file
   */
  public String getLocalDbFilePath() {
    return localDbFilePath;
  }

  /**
   * Returns the environment ID of the expense manager database file
   * @return the environment ID of the expense manager database file
   */
  public String getSourceDbEnvId() {
    return sourceDbEnvId;
  }

  /**
   * Returns the database configuration of the Aurora database
   * @return the database configuration of the Aurora database
   */
  public AuroraDatabaseConfiguration getAuroraDatabaseConfiguration() {
    return auroraDatabaseConfiguration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmChangeProcessorConfiguration that = (EmChangeProcessorConfiguration) o;
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
    return new StringJoiner(", ", EmChangeProcessorConfiguration.class.getSimpleName() + "[", "]")
        .add("localDbFilePath='" + localDbFilePath + "'")
        .add("sourceDbEnvId='" + sourceDbEnvId + "'")
        .add("auroraDatabaseConfiguration=" + auroraDatabaseConfiguration)
        .toString();
  }

  /**
   * {@code EmChangeProcessorConfiguration.Builder} is used for creating a {@code EmChangeProcessorConfiguration} from
   * various parameters.
   *
   * <p>
   *    {@code AuroraDatabaseConfiguration} is the only mandatory field in the construction of {@code
   *    EmChangeProcessorConfiguration}. You may use the available methods to set the individual values.
   * </p>
   * <p>
   *   If there aren't enough field parameters to determine all the application configuration, application default
   *   values will be used when building a {@code EmChangeProcessorConfiguration}
   * </p>
   *
   * @see EmChangeProcessorConfiguration
   * @see AuroraDatabaseConfiguration
   */
  public static class Builder {
    private static final String DEFAULT_LOCAL_DATABASE_FILE_PATH = URI.create("/tmp/expense_manager.db").getPath();
    private static final String DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID = URI.create("file_sqlite").getPath();

    private String localDbFilePath;
    private String sourceDbEnvId;
    private final AuroraDatabaseConfiguration auroraDatabaseConfiguration;

    /**
     * Default constructor of {@code EmChangeProcessorConfiguration.Builder}
     * @param auroraDatabaseConfiguration database configuration of the Aurora database
     */
    public Builder(AuroraDatabaseConfiguration auroraDatabaseConfiguration) {
      this.auroraDatabaseConfiguration = Objects.requireNonNull(auroraDatabaseConfiguration);
      this.localDbFilePath = DEFAULT_LOCAL_DATABASE_FILE_PATH;
      this.sourceDbEnvId = DEFAULT_SOURCE_DATABASE_ENVIRONMENT_ID;
    }

    /**
     * Set the intermediate file path to store the expense manager database file
     * @param localDbFilePath intermediate file path to store the expense manager database file
     * @return this {@code EmChangeProcessorConfiguration.Builder}
     */
    public Builder localDbFilePath(String localDbFilePath) {
      if (StringUtils.isBlank(localDbFilePath)) {
        LOGGER.atWarn().log("localDbFilePath is blank:{}",
            StringResolver.resolveNullableString(localDbFilePath));
        throw new IllegalArgumentException("Local database file path cannot be null or blank.");
      }
      this.localDbFilePath = localDbFilePath;
      return this;
    }

    /**
     * Set the environment ID of the expense manager database file
     * @param sourceDbEnvId environment ID of the expense manager database file
     * @return this {@code EmChangeProcessorConfiguration.Builder}
     */
    public Builder sourceDbEnvId(String sourceDbEnvId) {
      if (StringUtils.isBlank(sourceDbEnvId)) {
        LOGGER.atWarn().log("sourceDbEnvId is blank:{}",
            StringResolver.resolveNullableString(sourceDbEnvId));
        throw new IllegalArgumentException("Source database environment ID cannot be null or blank.");
      }
      this.sourceDbEnvId = sourceDbEnvId;
      return this;
    }

    /**
     * Returns a {@code EmChangeProcessorConfiguration} built from the parameters set by the setter methods.
     * @return a {@code EmChangeProcessorConfiguration} built from the parameters set by the setter methods.
     */
    public EmChangeProcessorConfiguration build() {
      return new EmChangeProcessorConfiguration(
          localDbFilePath,
          sourceDbEnvId,
          auroraDatabaseConfiguration
      );
    }
  }
}
