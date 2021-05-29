package expense_tally.aws.config;

import java.util.Optional;

public enum ApplicationErrorCode {
  KNOWN_EXCEPTION(400),
  UNKNOWN_EXCEPTION(500)
  ;

  private int value;

  ApplicationErrorCode(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }

  public static Optional<ApplicationErrorCode> resolve(int value) {
    for (ApplicationErrorCode applicationErrorCode : values()) {
      if (applicationErrorCode.value == value) {
        return Optional.of(applicationErrorCode);
      }
    }
    return Optional.empty();
  }
}
