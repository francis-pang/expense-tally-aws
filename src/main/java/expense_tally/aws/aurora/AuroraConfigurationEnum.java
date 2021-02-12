package expense_tally.aws.aurora;

import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum AuroraConfigurationEnum {
  AURORA_DATABASE_URL("expense_manager_db_host_url"),
  EXPENSE_MANAGER_DATABASE_NAME("expense_manager_db_name"),
  AURORA_USERNAME("expense_manager_db_username"),
  AURORA_PASSWORD("expense_manager_db_password"),
  AURORA_ENVIRONMENTAL_ID("expense_manager_db_environment_id"),
  AURORA_CONNECT_TIMEOUT("expense_manager_db_connection_timeout")
  ;

  private String key;

  AuroraConfigurationEnum(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("key cannot be blank:" + StringResolver.resolveNullableString(key));
    }
    this.key = key;
  }

  public String key() {
    return key;
  }

  public static Optional<AuroraConfigurationEnum> resolve(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("key cannot be blank:" + StringResolver.resolveNullableString(key));
    }
    for (AuroraConfigurationEnum auroraConfigurationEnum : values()) {
      if (key.equals(auroraConfigurationEnum.key)) {
        return Optional.of(auroraConfigurationEnum);
      }
    }
    return Optional.empty();
  }
}
