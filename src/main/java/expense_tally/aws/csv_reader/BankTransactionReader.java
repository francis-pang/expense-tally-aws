package expense_tally.aws.csv_reader;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.log.ObjectToString;
import expense_tally.aws.s3.DatabaseS3EventAnalyzer;
import expense_tally.aws.s3.S3FileRetriever;
import expense_tally.csv.parser.CsvParser;
import expense_tally.expense_manager.persistence.ExpenseReadable;
import expense_tally.expense_manager.transformation.ExpenseTransactionTransformer;
import expense_tally.model.csv.AbstractCsvTransaction;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;
import expense_tally.model.persistence.transformation.PaymentMethod;
import expense_tally.reconciliation.DiscrepantTransaction;
import expense_tally.reconciliation.ExpenseReconciler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BankTransactionReader {
  private static Logger LOGGER = LogManager.getLogger(BankTransactionReader.class);

  private final S3FileRetriever s3FileRetriever;
  private final ExpenseReadable expenseReadable;
  private final File csvFile;

  public BankTransactionReader(S3FileRetriever s3FileRetriever, ExpenseReadable expenseReadable, File csvFile) {
    this.s3FileRetriever = s3FileRetriever;
    this.expenseReadable = expenseReadable;
    this.csvFile = csvFile;
  }

  public static BankTransactionReader create(S3FileRetriever s3FileRetriever,
                                             ExpenseReadable expenseReadable,
                                             File csvFile) {
    return new BankTransactionReader(s3FileRetriever, expenseReadable, csvFile);
  }

  public List<DiscrepantTransaction> reconcile(S3Event s3Event) throws IOException, SQLException {
    boolean downloadIsSuccessful = downloadFile(s3Event, csvFile);
    if (!downloadIsSuccessful) {
      LOGGER.atError().log("Unable to download bank transaction file from S3. s3Event:{}, csvFile:{}",
          ObjectToString.extractStringFromObject(s3Event),
          ObjectToString.extractStringFromObject(csvFile));
      return Collections.emptyList();
    }
    LOGGER.atTrace().log("Extracting transaction from CSV file now. destinationFilePath:{}", csvFile);
    List<AbstractCsvTransaction> csvTransactions = extractCsvTransactionsFromFile(csvFile);
    LOGGER.atDebug().log("Transactions are extracted from CSV file. csvTransactions:{} entry", csvTransactions.size());
    LOGGER.atTrace().log("Retrieving transaction from database. expenseReadable:{}", expenseReadable);
    List<ExpenseManagerTransaction> expenseManagerTransactions = getExpnsMngrTxnsFromDatabase();
    LOGGER.atDebug().log("Transactions is retrieved from database. expenseManagerTransactions:{} entry",
        expenseManagerTransactions.size());
    return reconcileTransaction(csvTransactions, expenseManagerTransactions);
  }

  private boolean downloadFile(S3Event s3Event, File destinationFile) throws IOException {
    Optional<S3ObjectId> optionalS3Id = DatabaseS3EventAnalyzer.extractChangedS3ObjectId(s3Event);
    return optionalS3Id.isPresent() && s3FileRetriever.downloadFile(optionalS3Id.get(), destinationFile);
  }

  private List<AbstractCsvTransaction> extractCsvTransactionsFromFile(File destinationFile) throws IOException {
    String destinationFilePath = destinationFile.getAbsolutePath();
    return CsvParser.parseCsvFile(destinationFilePath);
  }

  private List<ExpenseManagerTransaction> getExpnsMngrTxnsFromDatabase() throws IOException, SQLException {
    return expenseReadable.getAllExpenseManagerTransaction();
  }

  private List<DiscrepantTransaction> reconcileTransaction(List<AbstractCsvTransaction> csvTransactions,
                                                           List<ExpenseManagerTransaction> expenseManagerTransactions) {
    Map<Double, Map<PaymentMethod, List<ExpenseManagerTransaction>>> expensesByAmountAndPaymentMethod =
        ExpenseTransactionTransformer.convertToTableOfAmountAndPaymentMethod(expenseManagerTransactions);
    return ExpenseReconciler.reconcileBankData(csvTransactions, expensesByAmountAndPaymentMethod);
  }
}
