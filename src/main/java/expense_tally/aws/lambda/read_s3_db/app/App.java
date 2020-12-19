package expense_tally.aws.lambda.read_s3_db.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import expense_tally.aws.lambda.read_s3_db.log.ObjectToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App implements RequestHandler<S3Event, String> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);

  @Override
  public String handleRequest(S3Event event, Context context) {
    LOGGER.atInfo().log("Received a new S3 event: {}", ObjectToString.extractStringFromObject(event));
    // Since we have already put a restriction on the SAM template on the database name, there is no need to check
    // for the file name anymore, we can safely assume that all the S3 event is meant what we need to handle.

    // Read file content
    return null;
  }
}
