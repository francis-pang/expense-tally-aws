package expense_tally.aws.em_change_processor.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.em_change_processor.S3ExpenseManagerUpdater;
import expense_tally.aws.em_change_processor.S3ExpnsMngrFileRetriever;
import expense_tally.aws.em_change_processor.log.ObjectToString;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import expense_tally.expense_manager.persistence.database.DatabaseConnectable;
import expense_tally.expense_manager.persistence.database.DatabaseSessionFactoryBuilder;
import expense_tally.expense_manager.persistence.database.ExpenseManagerTransactionDatabaseProxy;
import expense_tally.expense_manager.persistence.database.ExpenseReportDatabaseReader;
import expense_tally.expense_manager.persistence.database.mysql.MySqlConnection;
import expense_tally.expense_manager.persistence.database.sqlite.SqLiteConnection;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class App implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;

  public App() {
    try {
      init();
    } catch (SQLException throwables) {
      LOGGER
          .atFatal()
          .withThrowable(throwables)
          .log("Unable to initialise class.");
    }
  }

  private void init() throws SQLException {
    AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
    S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever = S3ExpnsMngrFileRetriever.create(amazonS3);
    final String EXPENSE_MANAGER_FILE_PATH = ""; //FIXME
    DatabaseConnectable expenseReportDatabaseConnectable = SqLiteConnection.create(EXPENSE_MANAGER_FILE_PATH);
    SqlSessionFactoryBuilder expenseReportSqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    DatabaseSessionFactoryBuilder expenseReportDatabaseSessionFactoryBuilder =
        new DatabaseSessionFactoryBuilder(expenseReportSqlSessionFactoryBuilder);
    final String EXPENSE_REPORT_ENVIRONMENTAL_ID = ""; //FIXME
    ExpenseReportReadable expenseReportReadable = new ExpenseReportDatabaseReader(expenseReportDatabaseConnectable,
        expenseReportDatabaseSessionFactoryBuilder, EXPENSE_REPORT_ENVIRONMENTAL_ID);
    final String AURORA_DATABASE_URL = ""; //FIXME
    final String EXPENSE_MANAGER_DATABASE_NAME = ""; //FIXME
    final String AURORA_USERNAME = ""; //FIXME
    final String AURORA_PASSWORD = ""; //FIXME
    DatabaseConnectable auroraDatabaseConnectable = MySqlConnection.create(AURORA_DATABASE_URL,
        EXPENSE_MANAGER_DATABASE_NAME, AURORA_USERNAME, AURORA_PASSWORD);
    SqlSessionFactoryBuilder auroraSqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    DatabaseSessionFactoryBuilder auroraDatabaseSessionFactoryBuilder =
        new DatabaseSessionFactoryBuilder(auroraSqlSessionFactoryBuilder);
    final String AURORA_ENVIRONMENTAL_ID = ""; //FIXME
    ExpenseUpdatable expenseUpdatable = new ExpenseManagerTransactionDatabaseProxy(auroraDatabaseConnectable,
        auroraDatabaseSessionFactoryBuilder, AURORA_ENVIRONMENTAL_ID);
    File expenseManagerFile = new File(EXPENSE_MANAGER_FILE_PATH);
    s3ExpenseManagerUpdater = S3ExpenseManagerUpdater.create(s3ExpnsMngrFileRetriever, expenseReportReadable,
        expenseUpdatable, expenseManagerFile);
  }

  @Override
  public Void handleRequest(S3Event event, Context context) {
    LOGGER.atInfo().log("Received a new S3 event: {}", ObjectToString.extractStringFromObject(event));
    // Since we have already put a restriction on the SAM template on the database name, there is no need to check
    // for the file name anymore, we can safely assume that all the S3 event is meant what we need to handle.
    try {
      s3ExpenseManagerUpdater.updateExpenseManager(event);
    } catch (Exception exception) {
      LOGGER
          .atError()
          .withThrowable(exception)
          .log("Unable to handle s3 event. event:{}", ObjectToString.extractStringFromObject(event));
    }
    return null;
  }
}
