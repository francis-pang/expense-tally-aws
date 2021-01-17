package expense_tally.aws.em_change_processor.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.em_change_processor.S3ExpenseManagerUpdater;
import expense_tally.aws.em_change_processor.log.ObjectToString;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class App implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;

  public App() {
    init();
  }

  private void init() {
    s3ExpenseManagerUpdater = S3ExpenseManagerUpdater.create();
  }


  @Override
  public Void handleRequest(S3Event event, Context context) {
    LOGGER.atInfo().log("Received a new S3 event: {}", ObjectToString.extractStringFromObject(event));
    // Since we have already put a restriction on the SAM template on the database name, there is no need to check
    // for the file name anymore, we can safely assume that all the S3 event is meant what we need to handle.
    s3ExpenseManagerUpdater.updateExpenseManager(event);
    return null;
  }
}
