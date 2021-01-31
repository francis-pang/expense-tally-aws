package expense_tally.aws.csv_reader.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import expense_tally.aws.s3.DatabaseS3EventAnalyzer;
import expense_tally.model.csv.AbstractCsvTransaction;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;

import java.io.File;
import java.util.List;

public class App implements RequestHandler<S3Event, Void> {
  @Override
  public Void handleRequest(S3Event input, Context context) {
    // Detect file change
    // Get file name
    // Download file
    // Read from database file
    // Download entries from remote database
    // Reconcile records

    return null;
  }

  private File downloadFile() {
    return null;
  }

  private List<? extends AbstractCsvTransaction> extractCsvTransactionsFromFile() {

  }

  private List<ExpenseManagerTransaction> retrieveExpenseManagerTransactionsFromDatabase() {

  }


}
