package expense_tally.aws.em_change_processor;

import java.io.IOException;

public class FilePermissionException extends IOException {
  public FilePermissionException(String message) {
    super(message);
  }
}
