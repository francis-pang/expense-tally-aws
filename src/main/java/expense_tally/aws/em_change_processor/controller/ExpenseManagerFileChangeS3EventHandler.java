package expense_tally.aws.em_change_processor.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.database.SqlSessionFactory;
import expense_tally.aws.em_change_processor.configuration.configuration.AppConfiguration;
import expense_tally.aws.em_change_processor.configuration.configuration.ConfigurationParser;
import expense_tally.aws.AppStartUpException;
import expense_tally.aws.em_change_processor.S3ExpenseManagerUpdater;
import expense_tally.aws.log.ObjectToString;
import expense_tally.aws.s3.S3FileRetriever;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import expense_tally.expense_manager.persistence.database.DatabaseEnvironmentId;
import expense_tally.expense_manager.persistence.database.ExpenseManagerTransactionDatabaseProxy;
import expense_tally.expense_manager.persistence.database.ExpenseReportDatabaseReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ExpenseManagerFileChangeS3EventHandler implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(ExpenseManagerFileChangeS3EventHandler.class);
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;
  private AppConfiguration appConfiguration;
  private static final int KNOWN_EXCEPTION_ERROR_CODE = 400;
  private static final int UNKNOWN_EXCEPTION_ERROR_CODE = 500;

  public ExpenseManagerFileChangeS3EventHandler() {
    try {
      init();
    } catch (SQLException| AppStartUpException | IOException exception) {
      LOGGER
          .atFatal()
          .withThrowable(exception)
          .log("Unable to initialise class.");
      System.exit(KNOWN_EXCEPTION_ERROR_CODE);
    } catch (Exception exception) {
      LOGGER
          .atFatal()
          .withThrowable(exception)
          .log("Unable to initialise class.");
      System.exit(UNKNOWN_EXCEPTION_ERROR_CODE);
    }
  }

  private void init() throws SQLException, AppStartUpException, IOException {
    LOGGER.atDebug().log("Initialising application");
    LOGGER.atDebug().log("Reading application configuration.");
    appConfiguration = ConfigurationParser.parseSystemEnvironmentVariableConfiguration();
    LOGGER.atDebug().log("Application configuration is loaded. appConfiguration:{}", appConfiguration);
    s3ExpenseManagerUpdater = assembleS3ExpenseManagerUpdater();
  }

  private AmazonS3 retrieveAmazonS3() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  private File assembleExpenseManagerFile() throws AppStartUpException {
    final String EXPENSE_MANAGER_FILE_PATH = retrieveExpenseManagerFilePath();
    return new File(EXPENSE_MANAGER_FILE_PATH);
  }

  private ExpenseUpdatable assembleExpenseUpdatable() throws AppStartUpException,
      SQLException, IOException {
    final String AURORA_DATABASE_URL = retrieveAuroraDatabaseUrl();
    final String EXPENSE_MANAGER_DATABASE_NAME = retrieveExpenseManagerDatabaseName();
    final String AURORA_USERNAME = retrieveAuroraUsername();
    final String AURORA_PASSWORD = retrieveAuroraPassword();
    final int AURORA_CONNECTION_TIMEOUT = retrieveAuroraConnectionTimeout();
    SqlSession sqlSession = SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, AURORA_DATABASE_URL,
        EXPENSE_MANAGER_DATABASE_NAME, AURORA_USERNAME, AURORA_PASSWORD, AURORA_CONNECTION_TIMEOUT);
    return new ExpenseManagerTransactionDatabaseProxy(sqlSession);
  }

  private ExpenseReportReadable assembleExpenseReportReadable()
      throws AppStartUpException, IOException, SQLException {
    final String EXPENSE_MANAGER_FILE_PATH = retrieveExpenseManagerFilePath();
    SqlSession sqlSession = SqlSessionFactory.constructSqLiteSession(EXPENSE_MANAGER_FILE_PATH);
    return new ExpenseReportDatabaseReader(sqlSession);
  }

  private S3ExpenseManagerUpdater assembleS3ExpenseManagerUpdater()
      throws AppStartUpException, SQLException, IOException {
    AmazonS3 amazonS3 = retrieveAmazonS3();
    S3FileRetriever s3FileRetriever = S3FileRetriever.create(amazonS3);
    ExpenseReportReadable expenseReportReadable = assembleExpenseReportReadable();
    ExpenseUpdatable expenseUpdatable = assembleExpenseUpdatable();
    File expenseManagerFile = assembleExpenseManagerFile();
    return S3ExpenseManagerUpdater.create(s3FileRetriever, expenseReportReadable,
        expenseUpdatable, expenseManagerFile);
  }

  private String retrieveAuroraPassword() {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = appConfiguration.getAuroraDatabaseConfiguration();
    return auroraDatabaseConfiguration.getPassword();
  }

  private int retrieveAuroraConnectionTimeout() {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = appConfiguration.getAuroraDatabaseConfiguration();
    return auroraDatabaseConfiguration.getConnectionTimeout();
  }

  private String retrieveAuroraUsername() {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = appConfiguration.getAuroraDatabaseConfiguration();
    return auroraDatabaseConfiguration.getUsername();
  }

  private String retrieveExpenseManagerDatabaseName() throws AppStartUpException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = appConfiguration.getAuroraDatabaseConfiguration();
    String databaseName = auroraDatabaseConfiguration.getDatabaseName();
    if (StringUtils.isBlank(databaseName)) {
      throw new AppStartUpException("Expense Manager database name cannot be blank.");
    }
    return databaseName;
  }

  private String retrieveAuroraDatabaseUrl() throws AppStartUpException {
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = appConfiguration.getAuroraDatabaseConfiguration();
    String hostUrl = auroraDatabaseConfiguration.getHostUrl();
    if (StringUtils.isBlank(hostUrl)) {
      throw new AppStartUpException("Aurora Database URL cannot be blank.");
    }
    return hostUrl;
  }

  private String retrieveExpenseManagerFilePath() throws AppStartUpException {
    String expenseManagerFilePath = appConfiguration.getLocalDbFilePath();
    if (StringUtils.isBlank(expenseManagerFilePath)) {
      throw new AppStartUpException("Expense Manager File Path cannot be blank.");
    }
    return expenseManagerFilePath;
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
    LOGGER.atInfo().log("Processed this S3 event: {}", ObjectToString.extractStringFromObject(event));
    return null;
  }
}
