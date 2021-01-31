package expense_tally.aws;

import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum AppConfigEnum {
  CSV_FILE_PATH("csv_local_file_path"),
  EXPENSE_MANAGER_FILE_PATH("expense_manager_local_file_path"),
  EXPENSE_REPORT_ENVIRONMENTAL_ID("expense_manager_local_file_db_environment_id")
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
