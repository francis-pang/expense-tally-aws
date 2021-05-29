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
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class SqlSessionFactoryTest {
  private static final Random RANDOM = new Random();

  // Whitebox
  // MySqlConnection.createDataSource SQL exception
  // SqLiteConnection.createDataSource SQL exception

  @Test
  void constructSqlSession_databaseEnvironmentIdIsNull() {
    assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(null, "asdasd", "sdfsdf", null, null,
        100))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void constructSqlSession_databaseConnectionPathIsNull() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource(null, "sdfsdf", null, null,100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, null,
          "sdfsdf", null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Connection URL should not be null or blank.");
    }
  }

  @Test
  void constructSqlSession_databaseConnectionPathIsEmpty() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource(StringUtils.EMPTY, "sdfsdf", null, null, 100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL,
          StringUtils.EMPTY,"sdfsdf", null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Connection URL should not be null or blank.");
    }
  }

  @Test
  void constructSqlSession_databaseConnectionPathIsBlank() {
    try (MockedStatic<SqLiteConnection> mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class)) {
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource(StringUtils.SPACE,  100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.SQLITE,
          StringUtils.SPACE,null, null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("database file path cannot be blank.");
    }
  }

  @Test
  void constructSqlSession_databaseNameIsNullForMySql() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnectionString", null, null, null, 100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL,
          "testConnectionString",null, null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database name should not be null or blank.");
    }
  }

  @Test
  void constructSqlSession_databaseNameIsEmptyForMySql() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnectionString", StringUtils.EMPTY, null,
          null, 100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL,
          "testConnectionString", StringUtils.EMPTY, null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database name should not be null or blank.");
    }
  }

  @Test
  void constructSqlSession_databaseNameIsBlankForMySql() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnectionString", StringUtils.SPACE, null,
          null, 100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL,
          "testConnectionString", StringUtils.SPACE, null, null,100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Database name should not be null or blank.");
    }
  }

  @Test
  void constructSqlSession_databaseNameIsNullForSqlite() throws IOException, SQLException {
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSessionFactoryBuilderConstruction = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTransactionFactoryConstruction = null;
    MockedStatic<DatabaseSessionBuilder> mockDatabaseSessionBuilderStatic = null;
    MockedStatic<SqLiteConnection> mockSqLiteConnection = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class);
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource("testConnectionPath", 100))
          .thenCallRealMethod();
      mockSqlSessionFactoryBuilderConstruction = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTransactionFactoryConstruction = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDatabaseSessionBuilderStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDatabaseSessionBuilderStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.SQLITE, "testConnectionPath", null, null,
          null,100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockSqLiteConnection != null) {
        mockSqLiteConnection.close();
      }
      if (mockSqlSessionFactoryBuilderConstruction != null) {
        mockSqlSessionFactoryBuilderConstruction.close();
      }
      if (mockJdbcTransactionFactoryConstruction != null) {
        mockJdbcTransactionFactoryConstruction.close();
      }
      if (mockDatabaseSessionBuilderStatic != null) {
        mockDatabaseSessionBuilderStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_usernameAndPasswordAreNullForMySql() throws IOException, SQLException {
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnection", "dbName", null, null,100))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "testConnection", "dbName", null,
          null,100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_usernameAndPasswordAreEmptyForMySql() throws IOException, SQLException {
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnection", "dbName", StringUtils.EMPTY,
          StringUtils.EMPTY, 100))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "testConnection", "dbName",
          StringUtils.EMPTY, StringUtils.EMPTY, 100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_usernameAndPasswordAreBlankForMySql() throws IOException, SQLException {
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnection", "dbName", StringUtils.SPACE,
          StringUtils.SPACE, 100))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "testConnection", "dbName",
          StringUtils.SPACE, StringUtils.SPACE, 100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_onlyPasswordIsNullForMySql() throws IOException, SQLException {
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnection", "dbName", "username",
          null, 100))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "testConnection", "dbName",
          "username", null, 100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_onlyUsernameIsNullForMySql() {
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("testConnection", "dbName", null,
          "password", 100))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "testConnection",
          "dbName", null, "password", 100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Password needs to be accompanied by username.");
    }
  }

  @Test
  void constructSqlSession_onlyUsernameIsNullForSqLite() throws IOException, SQLException {
    MockedStatic<SqLiteConnection> mockSqLiteConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class);
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource("testConnection", 100))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);
      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.SQLITE, "testConnection", "dbName",
          "username", null, 100))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockSqLiteConnection != null) {
        mockSqLiteConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  // positive

  @Test
  void constructSqlSession_connectionTimeoutIsPositiveForMySql() throws IOException, SQLException {
    int timeout = RANDOM.nextInt(Integer.MAX_VALUE);
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("dbPath", "dbName", null, null, timeout))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);

      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "dbPath", "dbName", null, null,
          timeout))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_connectionTimeoutIsZeroForMySql() throws IOException, SQLException {
    int timeout = 0;
    MockedStatic<MySqlConnection> mockMySqlConnection = null;
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSssnFctryBldrCnstrctn = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTrnsctnFctryCnstrctn = null;
    MockedStatic<DatabaseSessionBuilder> mockDbSssnBldrStatic = null;
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(Mockito.any(Environment.class)))
        .thenReturn(mockSqlSession);
    try {
      mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class);
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("dbPath", "dbName", null, null, timeout))
          .thenCallRealMethod();
      mockSqlSssnFctryBldrCnstrctn = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTrnsctnFctryCnstrctn = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDbSssnBldrStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDbSssnBldrStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);

      assertThat(SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "dbPath", "dbName", null, null,
          timeout))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockMySqlConnection != null) {
        mockMySqlConnection.close();
      }
      if (mockSqlSssnFctryBldrCnstrctn != null) {
        mockSqlSssnFctryBldrCnstrctn.close();
      }
      if (mockJdbcTrnsctnFctryCnstrctn != null) {
        mockJdbcTrnsctnFctryCnstrctn.close();
      }
      if (mockDbSssnBldrStatic != null) {
        mockDbSssnBldrStatic.close();
      }
    }
  }

  @Test
  void constructSqlSession_connectionTimeoutIsNegativeForMySql() {
    int timeout;
    do {
      timeout = RANDOM.nextInt();
    } while (timeout >= 0);
    final int TIMEOUT_NEGATIVE = timeout;
    try (MockedStatic<MySqlConnection> mockMySqlConnection = Mockito.mockStatic(MySqlConnection.class)) {
      mockMySqlConnection.when(() -> MySqlConnection.createDataSource("dbPath", "dbName", null, null, TIMEOUT_NEGATIVE))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "dbPath", "dbName",
          null, null, TIMEOUT_NEGATIVE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Login time out value cannot be negative.");
    }
  }

  @Test
  void constructSqlSession_connectionTimeoutIsNegativeForSqLite() {
    int timeout;
    do {
      timeout = RANDOM.nextInt();
    } while (timeout >= 0);
    final int TIMEOUT_NEGATIVE = timeout;
    try (MockedStatic<SqLiteConnection> mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class)) {
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource("dbPath", TIMEOUT_NEGATIVE))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqlSession(DatabaseEnvironmentId.MYSQL, "dbPath", "dbName",
          null, null, TIMEOUT_NEGATIVE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Login time out value cannot be negative.");
    }
  }

  @Test
  void constructSqLiteSession_positive() throws IOException, SQLException {
    MockedConstruction<SqlSessionFactoryBuilder> mockSqlSessionFactoryBuilderConstruction = null;
    MockedConstruction<JdbcTransactionFactory> mockJdbcTransactionFactoryConstruction = null;
    MockedStatic<DatabaseSessionBuilder> mockDatabaseSessionBuilderStatic = null;
    MockedStatic<SqLiteConnection> mockSqLiteConnection = null;
    DataSource mockDataSource = Mockito.mock(DataSource.class);
    DatabaseSessionBuilder mockDatabaseSessionBuilder = Mockito.mock(DatabaseSessionBuilder.class);
    MockedConstruction<Environment.Builder> mockEnvBldrCnstrctn = null;
    Environment.Builder mockEnvironmentBuilder = Mockito.mock(Environment.Builder.class);
    Mockito.when(mockEnvironmentBuilder.dataSource(Mockito.any(DataSource.class)))
        .thenReturn(mockEnvironmentBuilder);
    Mockito.when(mockEnvironmentBuilder.transactionFactory(Mockito.any(JdbcTransactionFactory.class)))
        .thenReturn(mockEnvironmentBuilder);
    Environment mockEnvironment = Mockito.mock(Environment.class);
    Mockito.when(mockEnvironmentBuilder.build()).thenReturn(mockEnvironment);
    SqlSession mockSqlSession = Mockito.mock(SqlSession.class);
    Mockito.when(mockDatabaseSessionBuilder.buildSessionFactory(mockEnvironment))
        .thenReturn(mockSqlSession);
    try {
      mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class);
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource("/tmp/db.sql", 1000))
          .thenReturn(mockDataSource);
      mockSqlSessionFactoryBuilderConstruction = Mockito.mockConstruction(SqlSessionFactoryBuilder.class);
      mockJdbcTransactionFactoryConstruction = Mockito.mockConstruction(JdbcTransactionFactory.class);
      mockDatabaseSessionBuilderStatic = Mockito.mockStatic(DatabaseSessionBuilder.class);
      mockDatabaseSessionBuilderStatic.when(() -> DatabaseSessionBuilder.of(Mockito.any(SqlSessionFactoryBuilder.class)))
          .thenReturn(mockDatabaseSessionBuilder);

      mockEnvBldrCnstrctn = Mockito.mockConstruction(Environment.Builder.class, (mockedNewObject, mockSetting) ->
      {
        Mockito.when(mockedNewObject.dataSource(mockDataSource)).thenReturn(mockEnvironmentBuilder);
        Mockito.when(mockedNewObject.transactionFactory(Mockito.any(JdbcTransactionFactory.class))).thenReturn(mockEnvironmentBuilder);
      });
      assertThat(SqlSessionFactory.constructSqLiteSession("/tmp/db.sql"))
          .isNotNull()
          .isEqualTo(mockSqlSession);
    } finally {
      if (mockSqLiteConnection != null) {
        mockSqLiteConnection.close();
      }
      if (mockSqlSessionFactoryBuilderConstruction != null) {
        mockSqlSessionFactoryBuilderConstruction.close();
      }
      if (mockEnvBldrCnstrctn != null) {
        mockEnvBldrCnstrctn.close();
      }
      if (mockJdbcTransactionFactoryConstruction != null) {
        mockJdbcTransactionFactoryConstruction.close();
      }
      if (mockDatabaseSessionBuilderStatic != null) {
        mockDatabaseSessionBuilderStatic.close();
      }
    }
  }

  @Test
  void constructSqLiteSession_databaseConnectionPathIsNull() {
    try (MockedStatic<SqLiteConnection> mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class)) {
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource(null, 1000))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqLiteSession(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("database file path cannot be blank.");
    }
  }

  @Test
  void constructSqLiteSession_databaseConnectionPathIsEmpty() {
    try (MockedStatic<SqLiteConnection> mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class)) {
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource(StringUtils.EMPTY, 1000))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqLiteSession(StringUtils.EMPTY))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("database file path cannot be blank.");
    }
  }

  @Test
  void constructSqLiteSession_databaseConnectionPathIsBlank() {
    try (MockedStatic<SqLiteConnection> mockSqLiteConnection = Mockito.mockStatic(SqLiteConnection.class)) {
      mockSqLiteConnection.when(() -> SqLiteConnection.createDataSource(StringUtils.SPACE, 1000))
          .thenCallRealMethod();
      assertThatThrownBy(() -> SqlSessionFactory.constructSqLiteSession(StringUtils.SPACE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("database file path cannot be blank.");
    }
  }
}