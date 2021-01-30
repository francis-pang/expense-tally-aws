package expense_tally.aws.em_change_processor;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.em_change_processor.log.ObjectToString;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import expense_tally.expense_manager.transformation.ExpenseTransactionTransformer;
import expense_tally.model.persistence.database.ExpenseReport;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class S3ExpenseManagerUpdater {
  private static final Logger LOGGER = LogManager.getLogger(S3ExpenseManagerUpdater.class);
  private final S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever;
  private final ExpenseReportReadable expenseReportReadable;
  private final ExpenseUpdatable expenseUpdatable;
  private final File expenseManagerFile;

  public S3ExpenseManagerUpdater(S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever, 
                                 ExpenseReportReadable expenseReportReadable, 
                                 ExpenseUpdatable expenseUpdatable, 
                                 File expenseManagerFile) {
    this.s3ExpnsMngrFileRetriever = s3ExpnsMngrFileRetriever;
    this.expenseReportReadable = expenseReportReadable;
    this.expenseUpdatable = expenseUpdatable;
    this.expenseManagerFile = expenseManagerFile;
  }

  public static S3ExpenseManagerUpdater create(S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever,
                                               ExpenseReportReadable expenseReportReadable,
                                               ExpenseUpdatable expenseUpdatable,
                                               File expenseManagerFile) {
    return new S3ExpenseManagerUpdater(s3ExpnsMngrFileRetriever, expenseReportReadable, expenseUpdatable, 
        expenseManagerFile);
  }

  public void updateExpenseManager(S3Event s3Event) throws IOException, SQLException {
    // Read the S3 Event
    // Extract the file information
    Optional<S3ObjectId> optionalS3ObjectId = getS3ObjectId(s3Event);
    if (optionalS3ObjectId.isEmpty()) {
      LOGGER.atInfo().log("Unable to extract expenseManagerS3ObjectId. s3Event:{}",
          ObjectToString.extractStringFromObject(s3Event));
      return;
    }
    S3ObjectId expenseManagerS3ObjectId = optionalS3ObjectId.get();
    // Assemble a GetFileRequest
    // Send Request
    // Process response into a file
    boolean downloadSuccessful = downloadExpenseManagerFile(expenseManagerS3ObjectId);
    if (!downloadSuccessful) {
      LOGGER.atWarn().log("Unable to download expense manager file. s3Event:{}, expenseManagerS3ObjectId:{}," +
              " expenseManagerFile:{}",
          ObjectToString.extractStringFromObject(s3Event),
          ObjectToString.extractStringFromObject(expenseManagerS3ObjectId),
          ObjectToString.extractStringFromObject(expenseManagerFile));
      return;
    }
    // Read database records
    List<ExpenseManagerTransaction> expenseManagerTransactions = retrieveTransactionRecords(); 
    // Store into remote Aurora database
    updateTransactionRecords(expenseManagerTransactions);
  }

  private Optional<S3ObjectId> getS3ObjectId(S3Event s3Event) {
    LOGGER.atDebug().log("Analyzing S3 event. s3Event:{}", ObjectToString.extractStringFromObject(s3Event));
    return DatabaseS3EventAnalyzer.extractChangedS3ObjectId(s3Event);
  }
  
  private boolean downloadExpenseManagerFile(S3ObjectId expenseManagerS3ObjectId) throws IOException {
    LOGGER.atDebug().log("Downloading object from S3. expenseManagerS3ObjectId:{}",
        ObjectToString.extractStringFromObject(expenseManagerS3ObjectId));
    return s3ExpnsMngrFileRetriever.downloadFile(expenseManagerS3ObjectId, expenseManagerFile);
  }

  private List<ExpenseManagerTransaction> retrieveTransactionRecords() throws IOException, SQLException {
    List<ExpenseReport> expenseReports = expenseReportReadable.getExpenseTransactions();
    return ExpenseTransactionTransformer.mapExpenseReports(expenseReports);
  }

  private void updateTransactionRecords(List<ExpenseManagerTransaction> expenseManagerTransactions) 
      throws IOException, SQLException {
    LOGGER.atDebug().log("Clearing remote database table.");
    expenseUpdatable.clear();
    LOGGER.atDebug().log("Remote database table is cleared. Inserting new expense manager transactions entries.");
    for (ExpenseManagerTransaction expenseManagerTransaction : expenseManagerTransactions) {
      LOGGER.atTrace().log("Inserting entry.expenseManagerTransaction:{}", expenseManagerTransaction);
      expenseUpdatable.add(expenseManagerTransaction);
    }
  }
}
