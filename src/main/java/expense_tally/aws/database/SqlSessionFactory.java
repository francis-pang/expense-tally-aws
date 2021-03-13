package expense_tally.aws.database;

import expense_tally.expense_manager.persistence.database.DatabaseEnvironmentId;
import expense_tally.expense_manager.persistence.database.DatabaseSessionBuilder;
import expense_tally.expense_manager.persistence.database.mysql.MySqlConnection;
import expense_tally.expense_manager.persistence.database.sqlite.SqLiteConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public final class SqlSessionFactory {
  private static final int DEFAULT_SQLITE_CONNECTION_TIMEOUT = 1000;

  /**
   * Make implicit constructor private as there is no need to initialise class
   */
  private SqlSessionFactory() {
  }

  /**
   * Returns a {@link SqlSession} based on the provided parameters
   * @param databaseConnectionPath hostname of the database connection. Port is not needed, default to be 3006.
   * @return a {@link SqlSession} based on the provided parameters
   * @throws SQLException if database access error occurs
   * @throws IOException if there is issue to read the myBatis configuration resource
   */
  public static SqlSession constructSqLiteSession(String databaseConnectionPath) throws SQLException, IOException {
    return constructSqlSession(DatabaseEnvironmentId.SQLITE, databaseConnectionPath, StringUtils.EMPTY,
        StringUtils.EMPTY, StringUtils.EMPTY, DEFAULT_SQLITE_CONNECTION_TIMEOUT);
  }

  /**
   * Returns a {@link SqlSession} based on the provided parameters
   * @param databaseEnvironmentId environment ID for declaration of a SqlSession environment.
   * @param databaseConnectionPath hostname of the database connection. Port is not needed, default to be 3006.
   * @param databaseName name of the database to be connected
   * @param username username to login to database server. This needs to be provided together with password.
   * @param password password to login to database server. This needs to be provided together with username.
   * @param connectionTimeout maximum time in milliseconds that this data source can wait while attempting to connect
   *                          to a database.
   * @return a {@link SqlSession} based on the provided parameters
   * @throws SQLException if database access error occurs
   * @throws IOException if there is issue to read the myBatis configuration resource
   */
  public static SqlSession constructSqlSession(DatabaseEnvironmentId databaseEnvironmentId,
                                               String databaseConnectionPath,
                                               String databaseName,
                                               String username,
                                               String password,
                                               int connectionTimeout) throws SQLException, IOException {
    DataSource dataSource;
    switch (databaseEnvironmentId) {
      case MYSQL:
        dataSource = MySqlConnection.createDataSource(databaseConnectionPath, databaseName, username, password,
            connectionTimeout);
        break;
      case SQLITE:
        dataSource = SqLiteConnection.createDataSource(databaseConnectionPath, connectionTimeout);
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


}
