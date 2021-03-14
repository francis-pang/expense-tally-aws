package expense_tally.aws.em_change_processor;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.log.ObjectToString;
import expense_tally.aws.s3.DatabaseS3EventAnalyzer;
import expense_tally.aws.s3.S3FileRetriever;
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
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class S3ExpenseManagerUpdater {
  private static final Logger LOGGER = LogManager.getLogger(S3ExpenseManagerUpdater.class);
  private static final String NULL_ERROR_MSG_POSTFIX = " cannot be null";
  private final S3FileRetriever s3FileRetriever;
  private final ExpenseReportReadable expenseReportReadable;
  private final ExpenseUpdatable expenseUpdatable;
  private final File expenseManagerFile;

  /**
   * Private constructor
   * @param s3FileRetriever s3 file retriever
   * @param expenseReportReadable expense report readable
   * @param expenseUpdatable expense updatable
   * @param expenseManagerFile expense manager database file
   */
  private S3ExpenseManagerUpdater(S3FileRetriever s3FileRetriever,
                                 ExpenseReportReadable expenseReportReadable,
                                 ExpenseUpdatable expenseUpdatable,
                                 File expenseManagerFile) {
    this.s3FileRetriever = Objects.requireNonNull(s3FileRetriever, "s3FileRetriever" + NULL_ERROR_MSG_POSTFIX);
    this.expenseReportReadable = Objects.requireNonNull(expenseReportReadable,
        "expenseReportReadable" + NULL_ERROR_MSG_POSTFIX);
    this.expenseUpdatable = Objects.requireNonNull(expenseUpdatable, "expenseUpdatable" + NULL_ERROR_MSG_POSTFIX);
    this.expenseManagerFile = Objects.requireNonNull(expenseManagerFile, "expenseManagerFile" +
        NULL_ERROR_MSG_POSTFIX);
  }

  /**
   * Creates a new instance of S3ExpenseManagerUpdater
   * @param s3FileRetriever s3 file retriever
   * @param expenseReportReadable expense report readable
   * @param expenseUpdatable expense updatable
   * @param expenseManagerFile expense manager database file
   * @return a new instance of S3ExpenseManagerUpdater
   */
  public static S3ExpenseManagerUpdater create(S3FileRetriever s3FileRetriever,
                                               ExpenseReportReadable expenseReportReadable,
                                               ExpenseUpdatable expenseUpdatable,
                                               File expenseManagerFile) {
    return new S3ExpenseManagerUpdater(s3FileRetriever, expenseReportReadable, expenseUpdatable,
        expenseManagerFile);
  }

  /**
   * Update the expense manager remote database from the local changed file on S3
   * @param changedEmDatabaseFileNotification S3 event that notifies that the expense manager database file has changed.
   * @throws IOException if there is an error to create and read the database file.
   * @throws SQLException if there is an error to access the database file.
   */
  public void updateExpenseManager(S3Event changedEmDatabaseFileNotification) throws IOException, SQLException {
    // Read the S3 Event
    // Extract the file information
    Optional<S3ObjectId> optionalS3ObjectId = getS3ObjectId(changedEmDatabaseFileNotification);
    if (optionalS3ObjectId.isEmpty()) {
      LOGGER.atInfo().log("Unable to extract expenseManagerS3ObjectId. s3Event:{}",
          ObjectToString.extractStringFromObject(changedEmDatabaseFileNotification));
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
          ObjectToString.extractStringFromObject(changedEmDatabaseFileNotification),
          ObjectToString.extractStringFromObject(expenseManagerS3ObjectId),
          ObjectToString.extractStringFromObject(expenseManagerFile));
      return;
    }
    // Read database records
    List<ExpenseManagerTransaction> expenseManagerTransactions = retrieveTransactionRecords();
    LOGGER.atDebug().log("Expense Manager Transaction is retrieved. {} expenseManagerTransactions entry.",
        expenseManagerTransactions.size());
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
    return s3FileRetriever.downloadFile(expenseManagerS3ObjectId, expenseManagerFile);
  }

  private List<ExpenseManagerTransaction> retrieveTransactionRecords() throws IOException, SQLException {
    List<ExpenseReport> expenseReports = expenseReportReadable.getExpenseTransactions();
    return ExpenseTransactionTransformer.mapExpenseReports(expenseReports);
  }

  private void updateTransactionRecords(List<ExpenseManagerTransaction> expenseManagerTransactions) 
      throws IOException, SQLException {
    if (expenseManagerTransactions.isEmpty()) {
      return;
    }
    LOGGER.atDebug().log("Clearing remote database table.");
    expenseUpdatable.clear();
    LOGGER.atDebug().log("Remote database table is cleared. Inserting new expense manager transactions entries.");
    for (ExpenseManagerTransaction expenseManagerTransaction : expenseManagerTransactions) {
      LOGGER.atTrace().log("Inserting entry.expenseManagerTransaction:{}", expenseManagerTransaction);
      //TODO: Log error if fail
      expenseUpdatable.add(expenseManagerTransaction);
    }
    LOGGER.atDebug().log("Expense manager transactions entries are inserted. expenseManagerTransactions {} entry",
        expenseManagerTransactions.size());
  }
}
