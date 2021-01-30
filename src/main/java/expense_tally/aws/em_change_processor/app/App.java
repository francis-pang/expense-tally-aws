package expense_tally.aws.em_change_processor.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import expense_tally.aws.em_change_processor.configuration.AppConfiguration;
import expense_tally.aws.em_change_processor.AppStartUpException;
import expense_tally.aws.em_change_processor.configuration.ConfigurationParser;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

public class App implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;
  private static final int KNOWN_EXCEPTION_ERROR_CODE = 400;
  private static final int UNKNOWN_EXCEPTION_ERROR_CODE = 500;

  public App() {
    try {
      init();
    } catch (SQLException| AppStartUpException exception) {
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

  private void init() throws SQLException, AppStartUpException {
    LOGGER.atDebug().log("Initialising application");
    LOGGER.atDebug().log("Reading application configuration.");
    AppConfiguration appConfiguration = ConfigurationParser.parseSystemEnvironmentVariableConfiguration();
    LOGGER.atDebug().log("Application configuration is loaded. appConfiguration:{}", appConfiguration);
    s3ExpenseManagerUpdater = assembleS3ExpenseManagerUpdater(appConfiguration);
  }

  private AmazonS3 retrieveAmazonS3() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  private File assembleExpenseManagerFile(AppConfiguration appConfiguration) throws AppStartUpException {
    final String EXPENSE_MANAGER_FILE_PATH = retrieveExpenseManagerFilePath(appConfiguration);
    return new File(EXPENSE_MANAGER_FILE_PATH);
  }

  private ExpenseUpdatable assembleExpenseUpdatable(AppConfiguration appConfiguration) throws AppStartUpException,
      SQLException {
    final String AURORA_DATABASE_URL = retrieveAuroraDatabaseUrl(appConfiguration);
    final String EXPENSE_MANAGER_DATABASE_NAME = retrieveExpenseManagerDatabaseName(appConfiguration);
    final String AURORA_USERNAME = retrieveAuroraUsername(appConfiguration);
    final String AURORA_PASSWORD = retrieveAuroraPassword(appConfiguration);
    DatabaseConnectable auroraDatabaseConnectable = MySqlConnection.create(AURORA_DATABASE_URL,
        EXPENSE_MANAGER_DATABASE_NAME, AURORA_USERNAME, AURORA_PASSWORD);
    SqlSessionFactoryBuilder auroraSqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    DatabaseSessionFactoryBuilder auroraDatabaseSessionFactoryBuilder =
        new DatabaseSessionFactoryBuilder(auroraSqlSessionFactoryBuilder);
    final String AURORA_ENVIRONMENTAL_ID = retrieveAuroraEnvironmentalId(appConfiguration);
    return new ExpenseManagerTransactionDatabaseProxy(auroraDatabaseConnectable,
        auroraDatabaseSessionFactoryBuilder, AURORA_ENVIRONMENTAL_ID);
  }

  private ExpenseReportReadable assembleExpenseReportReadable(AppConfiguration appConfiguration)
      throws AppStartUpException {
    final String EXPENSE_MANAGER_FILE_PATH = retrieveExpenseManagerFilePath(appConfiguration);
    DatabaseConnectable expenseReportDatabaseConnectable = SqLiteConnection.create(EXPENSE_MANAGER_FILE_PATH);
    SqlSessionFactoryBuilder expenseReportSqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    DatabaseSessionFactoryBuilder expenseReportDatabaseSessionFactoryBuilder =
        new DatabaseSessionFactoryBuilder(expenseReportSqlSessionFactoryBuilder);
    final String EXPENSE_REPORT_ENVIRONMENTAL_ID = retrieveExpenseReportEnvironmentalId(appConfiguration);
    return new ExpenseReportDatabaseReader(expenseReportDatabaseConnectable,
        expenseReportDatabaseSessionFactoryBuilder, EXPENSE_REPORT_ENVIRONMENTAL_ID);
  }

  private S3ExpenseManagerUpdater assembleS3ExpenseManagerUpdater(AppConfiguration appConfiguration)
      throws AppStartUpException, SQLException {
    AmazonS3 amazonS3 = retrieveAmazonS3();
    S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever = S3ExpnsMngrFileRetriever.create(amazonS3);
    ExpenseReportReadable expenseReportReadable = assembleExpenseReportReadable(appConfiguration);
    ExpenseUpdatable expenseUpdatable = assembleExpenseUpdatable(appConfiguration);
    File expenseManagerFile = assembleExpenseManagerFile(appConfiguration);
    return S3ExpenseManagerUpdater.create(s3ExpnsMngrFileRetriever, expenseReportReadable,
        expenseUpdatable, expenseManagerFile);
  }

  private String retrieveAuroraEnvironmentalId(AppConfiguration appConfiguration) throws AppStartUpException {
    String destinationDbEnvId = appConfiguration.getDestinationDbEnvId();
    if (StringUtils.isBlank(destinationDbEnvId)) {
      throw new AppStartUpException("Aurora environment ID cannot be blank.");
    }
    return destinationDbEnvId;
  }

  private String retrieveAuroraPassword(AppConfiguration appConfiguration) {
    return appConfiguration.getDstntnDbPassword();
  }

  private String retrieveAuroraUsername(AppConfiguration appConfiguration) {
    return appConfiguration.getDstntnDbUsername();
  }

  private String retrieveExpenseManagerDatabaseName(AppConfiguration appConfiguration) throws AppStartUpException {
    String destinationDbName = appConfiguration.getDestinationDbName();
    if (StringUtils.isBlank(destinationDbName)) {
      throw new AppStartUpException("Expense Manager database name cannot be blank.");
    }
    return destinationDbName;
  }

  private String retrieveAuroraDatabaseUrl(AppConfiguration appConfiguration) throws AppStartUpException {
    String destinationDbHostUrl = appConfiguration.getDestinationDbHostUrl();
    if (StringUtils.isBlank(destinationDbHostUrl)) {
      throw new AppStartUpException("Aurora Database URL cannot be blank.");
    }
    return destinationDbHostUrl;
  }

  private String retrieveExpenseReportEnvironmentalId(AppConfiguration appConfiguration) throws AppStartUpException {
    String sourceDbEnvId = appConfiguration.getSourceDbEnvId();
    if (StringUtils.isBlank(sourceDbEnvId)) {
      throw new AppStartUpException("Expense Report Environmental ID cannot be blank.");
    }
    return sourceDbEnvId;
  }

  private String retrieveExpenseManagerFilePath(AppConfiguration appConfiguration) throws AppStartUpException {
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
