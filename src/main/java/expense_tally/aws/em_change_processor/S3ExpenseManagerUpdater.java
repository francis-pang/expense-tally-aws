package expense_tally.aws.em_change_processor;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;

import java.util.List;

public class S3ExpenseManagerUpdater {
  private S3ExpenseManagerUpdater() {
  }

  public static S3ExpenseManagerUpdater create() {
    return new S3ExpenseManagerUpdater();
  }

  public void updateExpenseManager(S3Event s3Event) {
    // Read the S3 Event
    S3ObjectId expenseManagerS3ObjectId = getS3ObjectId(s3Event);
    // Extract the file information

    // Assemble a GetFileRequest
    // Send Request
    // Process response into a file
    // Read database records
    // Store into remote Aurora database
  }

  private S3ObjectId getS3ObjectId(S3Event s3Event) {
    return null; // stub
  }

  private List<ExpenseManagerTransaction> retrieveTransactionRecords() {
    return null; // stub
  }

  private void updateTransactionRecords(List<ExpenseManagerTransaction> expenseManagerTransactions) {

  }

}
