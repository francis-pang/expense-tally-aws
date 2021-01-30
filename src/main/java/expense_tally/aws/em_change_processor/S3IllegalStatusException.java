package expense_tally.aws.em_change_processor;

public class S3IllegalStatusException extends IllegalStateException {
  public S3IllegalStatusException(String message) {
    super(message);
  }
}
