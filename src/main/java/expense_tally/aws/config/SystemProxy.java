package expense_tally.aws.config;

/**
 * This class is a proxy to the actual {@link System} class.
 *
 * <b>Implementation detail</b>
 * <p>
 *   This class is established because Mockito is unable to mock System class.
 * </p>
 */
public class SystemProxy {
  /**
   * Make implicit constructor private as there is no need to initialise class
   */
  private SystemProxy() {
  }

  /**
   * Gets the value of the specified environment variable.
   * @param environmentVariableName
   * @return the string value of the variable, or null if the variable is not defined in the system environment
   */
  public static String getEnvironmentVariable(String environmentVariableName) {
    return System.getenv(environmentVariableName);
  }
}
