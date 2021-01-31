package expense_tally.aws.em_change_processor.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import expense_tally.aws.configuration.AppConfiguration;
import expense_tally.aws.em_change_processor.AppStartUpException;
import expense_tally.aws.configuration.ConfigurationParser;
import expense_tally.aws.em_change_processor.S3ExpenseManagerUpdater;
import expense_tally.aws.s3.S3ExpnsMngrFileRetriever;
import expense_tally.aws.log.ObjectToString;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import expense_tally.expense_manager.persistence.database.DatabaseEnvironmentId;
import expense_tally.expense_manager.persistence.database.DatabaseSessionBuilder;
import expense_tally.expense_manager.persistence.database.ExpenseManagerTransactionDatabaseProxy;
import expense_tally.expense_manager.persistence.database.ExpenseReportDatabaseReader;
import expense_tally.expense_manager.persistence.database.mysql.MySqlConnection;
import expense_tally.expense_manager.persistence.database.sqlite.SqLiteConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class App implements RequestHandler<S3Event, Void> {
  private static final Logger LOGGER = LogManager.getLogger(App.class);
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;
  private static final int KNOWN_EXCEPTION_ERROR_CODE = 400;
  private static final int UNKNOWN_EXCEPTION_ERROR_CODE = 500;

  public App() {
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
      SQLException, IOException {
    final String AURORA_DATABASE_URL = retrieveAuroraDatabaseUrl(appConfiguration);
    final String EXPENSE_MANAGER_DATABASE_NAME = retrieveExpenseManagerDatabaseName(appConfiguration);
    final String AURORA_USERNAME = retrieveAuroraUsername(appConfiguration);
    final String AURORA_PASSWORD = retrieveAuroraPassword(appConfiguration);
    SqlSession sqlSession = constructSqlSession(DatabaseEnvironmentId.MYSQL, AURORA_DATABASE_URL,
        EXPENSE_MANAGER_DATABASE_NAME, AURORA_USERNAME, AURORA_PASSWORD);
    return new ExpenseManagerTransactionDatabaseProxy(sqlSession);
  }

  private ExpenseReportReadable assembleExpenseReportReadable(AppConfiguration appConfiguration)
      throws AppStartUpException, IOException, SQLException {
    final String EXPENSE_MANAGER_FILE_PATH = retrieveExpenseManagerFilePath(appConfiguration);
    SqlSession sqlSession = constructSqlSession(DatabaseEnvironmentId.SQLITE, EXPENSE_MANAGER_FILE_PATH, null, null,
        null);
    return new ExpenseReportDatabaseReader(sqlSession);
  }

  private SqlSession constructSqlSession(DatabaseEnvironmentId databaseEnvironmentId,
                                         String databaseConnectionPath,
                                         String databaseName,
                                         String username,
                                         String password) throws SQLException, IOException {
    DataSource dataSource;
    switch (databaseEnvironmentId) {
      case MYSQL:
        dataSource = MySqlConnection.createDataSource(databaseConnectionPath, databaseName, username, password);
        break;
      case SQLITE:
        dataSource = SqLiteConnection.createDataSource(databaseConnectionPath);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + databaseEnvironmentId);
    }
    DatabaseSessionBuilder databaseSessionBuilder = DatabaseSessionBuilder.of(new SqlSessionFactoryBuilder());
    Environment environment = new Environment.Builder(databaseEnvironmentId.name())
        .dataSource(dataSource)
        .transactionFactory(new JdbcTransactionFactory())
        .build();
    return databaseSessionBuilder.buildSessionFactory(environment);
  }

  private S3ExpenseManagerUpdater assembleS3ExpenseManagerUpdater(AppConfiguration appConfiguration)
      throws AppStartUpException, SQLException, IOException {
    AmazonS3 amazonS3 = retrieveAmazonS3();
    S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever = S3ExpnsMngrFileRetriever.create(amazonS3);
    ExpenseReportReadable expenseReportReadable = assembleExpenseReportReadable(appConfiguration);
    ExpenseUpdatable expenseUpdatable = assembleExpenseUpdatable(appConfiguration);
    File expenseManagerFile = assembleExpenseManagerFile(appConfiguration);
    return S3ExpenseManagerUpdater.create(s3ExpnsMngrFileRetriever, expenseReportReadable,
        expenseUpdatable, expenseManagerFile);
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
