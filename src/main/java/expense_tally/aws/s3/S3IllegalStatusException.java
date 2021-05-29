package expense_tally.aws.s3;

import java.io.Serializable;

public class S3IllegalStatusException extends IllegalStateException implements Serializable {
  public S3IllegalStatusException(String message) {
    super(message);
  }
}
