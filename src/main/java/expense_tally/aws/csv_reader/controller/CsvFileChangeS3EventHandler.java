package expense_tally.aws.csv_reader.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import expense_tally.aws.AppStartUpException;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.config.ApplicationErrorCode;
import expense_tally.aws.csv_reader.BankTransactionReader;
import expense_tally.aws.csv_reader.configuration.AppConfiguration;
import expense_tally.aws.csv_reader.configuration.ConfigurationParser;
import expense_tally.aws.database.SqlSessionFactory;
import expense_tally.aws.log.ObjectToString;
import expense_tally.aws.s3.S3FileRetriever;
import expense_tally.expense_manager.persistence.ExpenseReadable;
import expense_tally.expense_manager.persistence.database.DatabaseEnvironmentId;
import expense_tally.expense_manager.persistence.database.ExpenseManagerTransactionDatabaseProxy;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CsvFileChangeS3EventHandler implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(CsvFileChangeS3EventHandler.class);

  private BankTransactionReader bankTransactionReader;
  private AppConfiguration appConfiguration;

  public CsvFileChangeS3EventHandler() {
    try {
      init();
    } catch (AppStartUpException | IOException | SQLException exception) {
      LOGGER
          .atFatal()
          .withThrowable(exception)
          .log("Unable to initialise class.");
      System.exit(ApplicationErrorCode.KNOWN_EXCEPTION.value());
    } catch (Exception exception) {
      LOGGER
          .atFatal()
          .withThrowable(exception)
          .log("Unable to initialise class.");
      System.exit(ApplicationErrorCode.UNKNOWN_EXCEPTION.value());
    }
  }

  private void init() throws AppStartUpException, IOException, SQLException {
    LOGGER.atDebug().log("Initialising application");
    LOGGER.atDebug().log("Reading application configuration.");
    appConfiguration = ConfigurationParser.parseSystemEnvironmentVariableConfiguration();
    LOGGER.atDebug().log("Application configuration is loaded. appConfiguration:{}", appConfiguration);
    S3FileRetriever s3FileRetriever = assembleS3FileRetriever();
    ExpenseReadable expenseReadable = assembleExpenseReadable();
    File csvFile = retrieveCsvFile();
    bankTransactionReader = BankTransactionReader.create(s3FileRetriever, expenseReadable, csvFile);
  }

  @Override
  public Void handleRequest(S3Event s3Event, Context context) {
    try {
      bankTransactionReader.reconcile(s3Event);
    } catch (Exception exception) {
      LOGGER
          .atError()
          .withThrowable(exception)
          .log("Unable to handle s3 event. event:{}", ObjectToString.extractStringFromObject(s3Event));
    }
    LOGGER.atInfo().log("Processed this S3 event: {}", ObjectToString.extractStringFromObject(s3Event));
    return null;
  }

  private S3FileRetriever assembleS3FileRetriever() {
    AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
    return S3FileRetriever.create(amazonS3);
  }

  private File retrieveCsvFile() {
    return appConfiguration.getCsvFile();
  }

  private ExpenseReadable assembleExpenseReadable() throws IOException, SQLException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = retrieveAuroraDatabaseConfiguration();
    final String AURORA_DATABASE_URL = auroraDatabaseConfiguration.getHostUrl();
    final String EXPENSE_MANAGER_DATABASE_NAME = auroraDatabaseConfiguration.getDatabaseName();
    final String AURORA_USERNAME = auroraDatabaseConfiguration.getUsername();
    final String AURORA_PASSWORD = auroraDatabaseConfiguration.getPassword();
    final int AURORA_CONNECTION_TIMEOUT = auroraDatabaseConfiguration.getConnectionTimeout();
    SqlSession sqlSession = SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, AURORA_DATABASE_URL,
        EXPENSE_MANAGER_DATABASE_NAME, AURORA_USERNAME, AURORA_PASSWORD, AURORA_CONNECTION_TIMEOUT);
    return new ExpenseManagerTransactionDatabaseProxy(sqlSession);
  }

  private AuroraDatabaseConfiguration retrieveAuroraDatabaseConfiguration() {
    return appConfiguration.getAuroraDatabaseConfiguration();
  }
}
