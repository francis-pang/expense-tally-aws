package expense_tally.aws.s3;

public class S3IllegalStatusException extends IllegalStateException {
  public S3IllegalStatusException(String message) {
    super(message);
  }
}
