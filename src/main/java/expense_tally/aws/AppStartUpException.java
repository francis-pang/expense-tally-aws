package expense_tally.aws;

import java.io.Serializable;
/**
 *
 */
public class AppStartUpException extends Exception implements Serializable {
  public AppStartUpException(String message) {
    super(message);
  }
}
