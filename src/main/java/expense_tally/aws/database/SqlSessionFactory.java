package expense_tally.aws.database;

import expense_tally.expense_manager.persistence.database.DatabaseEnvironmentId;
import expense_tally.expense_manager.persistence.database.DatabaseSessionBuilder;
import expense_tally.expense_manager.persistence.database.mysql.MySqlConnection;
import expense_tally.expense_manager.persistence.database.sqlite.SqLiteConnection;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public final class SqlSessionFactory {
  public static SqlSession constructSqlSession(DatabaseEnvironmentId databaseEnvironmentId,
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
}
