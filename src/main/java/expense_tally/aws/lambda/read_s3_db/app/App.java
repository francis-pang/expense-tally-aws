package expense_tally.aws.lambda.read_s3_db.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import expense_tally.aws.lambda.read_s3_db.DatabaseS3EventAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App implements RequestHandler<S3Event, String> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);

  @Override
  public String handleRequest(S3Event event, Context context) {
    LOGGER.atInfo().log("Received a new S3 event: {}", event);
    // Check if the name of the resource changed is what I am looking out for
    DatabaseS3EventAnalyzer databaseS3EventAnalyzer = new DatabaseS3EventAnalyzer("personal.db");
    databaseS3EventAnalyzer.extractDatabaseObjectKey(event);
    // Read the type of S3 event

    // Read file content
    return null;
  }
}
