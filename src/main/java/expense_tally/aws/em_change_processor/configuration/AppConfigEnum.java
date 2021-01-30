package expense_tally.aws.em_change_processor.configuration;

import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum AppConfigEnum {
  EXPENSE_MANAGER_FILE_PATH("expense_manager_local_file_path"),
  EXPENSE_REPORT_ENVIRONMENTAL_ID("expense_manager_local_file_db_environment_id"),
  AURORA_DATABASE_URL("expense_manager_remote_db_host_url"),
  EXPENSE_MANAGER_DATABASE_NAME("expense_manager_remote_db_name"),
  AURORA_USERNAME("expense_manager_remote_db_username"),
  AURORA_PASSWORD("expense_manager_remote_db_password"),
  AURORA_ENVIRONMENTAL_ID("expense_manager_remote_db_environment_id")
  ;

  private String key;

  AppConfigEnum(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("key cannot be blank:" + StringResolver.resolveNullableString(key));
    }
    this.key = key;
  }

  public String key() {
    return key;
  }

  public static Optional<AppConfigEnum> resolve(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("key cannot be blank:" + StringResolver.resolveNullableString(key));
    }
    for (AppConfigEnum appConfigEnum : values()) {
      if (key.equals(appConfigEnum.key)) {
        return Optional.of(appConfigEnum);
      }
    }
    return Optional.empty();
  }
}
