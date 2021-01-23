package expense_tally.aws.em_change_processor;

public class AppStartUpException extends Exception {
  public AppStartUpException() {
  }

  public AppStartUpException(String message) {
    super(message);
  }

  public AppStartUpException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppStartUpException(Throwable cause) {
    super(cause);
  }
}
