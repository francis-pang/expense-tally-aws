package expense_tally.aws.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObjectToString {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger LOGGER = LogManager.getLogger(ObjectToString.class);

  /**
   * Make implicit constructor private as there is no need to initialise class
   */
  private ObjectToString() {
  }

  public static String extractStringFromObject(Object object) {
    objectMapper.registerModule(new JodaModule());
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException jsonProcessingException) {
      LOGGER
          .atWarn()
          .withThrowable(jsonProcessingException)
          .log("Unable to serialise object: {}", () -> getObjectClassName(object));
      return "";
    }
  }

  private static String getObjectClassName(Object object) {
    return (object == null) ? "NULL" : object.getClass().getCanonicalName();
  }
}
