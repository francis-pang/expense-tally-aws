package expense_tally.aws.em_change_processor;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.s3.DatabaseS3EventAnalyzer;
import expense_tally.aws.s3.S3FileRetriever;
import expense_tally.aws.s3.S3IllegalStatusException;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import expense_tally.expense_manager.transformation.ExpenseTransactionTransformer;
import expense_tally.model.persistence.database.ExpenseReport;
import expense_tally.model.persistence.transformation.ExpenseManagerTransaction;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class S3ExpenseManagerUpdaterTest {
  @Mock
  private S3FileRetriever mockS3FileRetriever;

  @Mock
  private ExpenseReportReadable mockExpenseReportReadable;

  @Mock
  private ExpenseUpdatable mockExpenseUpdatable;

  @Mock
  private File mockExpenseManagerFile;

  @InjectMocks
  private S3ExpenseManagerUpdater s3ExpenseManagerUpdater;

  @Test
  void create_positive() {
    assertThat(S3ExpenseManagerUpdater.create(mockS3FileRetriever, mockExpenseReportReadable, mockExpenseUpdatable,
        mockExpenseManagerFile))
        .isNotNull()
        .hasFieldOrPropertyWithValue("s3FileRetriever", mockS3FileRetriever)
        .hasFieldOrPropertyWithValue("expenseReportReadable", mockExpenseReportReadable)
        .hasFieldOrPropertyWithValue("expenseUpdatable", mockExpenseUpdatable)
        .hasFieldOrPropertyWithValue("expenseManagerFile", mockExpenseManagerFile);
  }

  @Test
  void create_s3FileRetrieverIsNull() {
    assertThatThrownBy(() -> S3ExpenseManagerUpdater.create(null, mockExpenseReportReadable, mockExpenseUpdatable,
        mockExpenseManagerFile))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("s3FileRetriever cannot be null");
  }

  @Test
  void create_expenseReportReadableIsNull() {
    assertThatThrownBy(() -> S3ExpenseManagerUpdater.create(mockS3FileRetriever, null, mockExpenseUpdatable,
        mockExpenseManagerFile))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("expenseReportReadable cannot be null");
  }

  @Test
  void create_expenseUpdatableIsNull() {
    assertThatThrownBy(() -> S3ExpenseManagerUpdater.create(mockS3FileRetriever, mockExpenseReportReadable, null,
        mockExpenseManagerFile))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("expenseUpdatable cannot be null");
  }

  @Test
  void create_expenseManagerFileIsNull() {
    assertThatThrownBy(() -> S3ExpenseManagerUpdater.create(mockS3FileRetriever, mockExpenseReportReadable,
        mockExpenseUpdatable,null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("expenseManagerFile cannot be null");
  }

  @Test
  void updateExpenseManager_positive() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(true);
    ExpenseReport mockExpenseReport = Mockito.mock(ExpenseReport.class);
    List<ExpenseReport> mockExpenseReports = Collections.singletonList(mockExpenseReport);
    Mockito.when(mockExpenseReportReadable.getExpenseTransactions()).thenReturn(mockExpenseReports);
    ExpenseManagerTransaction mockExpenseManagerTransaction = Mockito.mock(ExpenseManagerTransaction.class);
    List<ExpenseManagerTransaction> mockExpenseManagerTransactions =
        Collections.singletonList(mockExpenseManagerTransaction);
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    MockedStatic<ExpenseTransactionTransformer> mockExpenseTransactionTransformer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      mockExpenseTransactionTransformer = Mockito.mockStatic(ExpenseTransactionTransformer.class);
      mockExpenseTransactionTransformer.when(() -> ExpenseTransactionTransformer.mapExpenseReports(mockExpenseReports))
          .thenReturn(mockExpenseManagerTransactions);
      s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
      if (mockExpenseTransactionTransformer != null) {
        mockExpenseTransactionTransformer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_nullEvent() {
    assertThatThrownBy(() -> s3ExpenseManagerUpdater.updateExpenseManager(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Event cannot be null.");
  }

  @Test
  void updateExpenseManager_extractEmptyS3ObjectId() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.empty();

    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_extractChangedS3ObjectIdError() {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenThrow(new S3IllegalStatusException("test npe"));
      assertThatThrownBy(() -> s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification))
          .isInstanceOf(S3IllegalStatusException.class);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_downloadDbFileException() throws IOException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile))
        .thenThrow(new IOException("test ioException"));
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      assertThatThrownBy(() -> s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification))
          .isInstanceOf(IOException.class)
          .hasMessage("test ioException");
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_downloadFail() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(false);
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_noRecordInDbFile() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(true);
    Mockito.when(mockExpenseReportReadable.getExpenseTransactions()).thenReturn(Collections.emptyList());
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
    }
    Mockito.verifyNoInteractions(mockExpenseUpdatable);
  }

  @Test
  void updateExpenseManager_clearDbError() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(true);
    ExpenseReport mockExpenseReport = Mockito.mock(ExpenseReport.class);
    List<ExpenseReport> mockExpenseReports = Collections.singletonList(mockExpenseReport);
    Mockito.when(mockExpenseReportReadable.getExpenseTransactions()).thenReturn(mockExpenseReports);
    ExpenseManagerTransaction mockExpenseManagerTransaction = Mockito.mock(ExpenseManagerTransaction.class);
    List<ExpenseManagerTransaction> mockExpenseManagerTransactions =
        Collections.singletonList(mockExpenseManagerTransaction);
    Mockito.when(mockExpenseUpdatable.clear()).thenThrow(new SdkClientException("Cannot connect to S3."));
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    MockedStatic<ExpenseTransactionTransformer> mockExpenseTransactionTransformer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      mockExpenseTransactionTransformer = Mockito.mockStatic(ExpenseTransactionTransformer.class);
      mockExpenseTransactionTransformer.when(() -> ExpenseTransactionTransformer.mapExpenseReports(mockExpenseReports))
          .thenReturn(mockExpenseManagerTransactions);
      assertThatThrownBy(() -> s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification))
          .isInstanceOf(SdkClientException.class)
          .hasMessage("Cannot connect to S3.");
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
      if (mockExpenseTransactionTransformer != null) {
        mockExpenseTransactionTransformer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_expenseManagerTransactionInsertionFails() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(true);
    ExpenseReport mockExpenseReport = Mockito.mock(ExpenseReport.class);
    List<ExpenseReport> mockExpenseReports = Collections.singletonList(mockExpenseReport);
    Mockito.when(mockExpenseReportReadable.getExpenseTransactions()).thenReturn(mockExpenseReports);
    ExpenseManagerTransaction mockExpenseManagerTransaction = Mockito.mock(ExpenseManagerTransaction.class);
    List<ExpenseManagerTransaction> mockExpenseManagerTransactions =
        Collections.singletonList(mockExpenseManagerTransaction);
    Mockito.when(mockExpenseUpdatable.add(mockExpenseManagerTransaction)).thenReturn(false);
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    MockedStatic<ExpenseTransactionTransformer> mockExpenseTransactionTransformer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      mockExpenseTransactionTransformer = Mockito.mockStatic(ExpenseTransactionTransformer.class);
      mockExpenseTransactionTransformer.when(() -> ExpenseTransactionTransformer.mapExpenseReports(mockExpenseReports))
          .thenReturn(mockExpenseManagerTransactions);
      s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification);
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
      if (mockExpenseTransactionTransformer != null) {
        mockExpenseTransactionTransformer.close();
      }
    }
  }

  @Test
  void updateExpenseManager_expenseManagerTransactionInsertionError() throws IOException, SQLException {
    S3Event mockChangedEmDbFileNotification = Mockito.mock(S3Event.class);

    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Optional<S3ObjectId> mockOptionalS3ObjectId = Optional.of(mockS3ObjectId);

    Mockito.when(mockS3FileRetriever.downloadFile(mockS3ObjectId, mockExpenseManagerFile)).thenReturn(true);
    ExpenseReport mockExpenseReport = Mockito.mock(ExpenseReport.class);
    List<ExpenseReport> mockExpenseReports = Collections.singletonList(mockExpenseReport);
    Mockito.when(mockExpenseReportReadable.getExpenseTransactions()).thenReturn(mockExpenseReports);
    ExpenseManagerTransaction mockExpenseManagerTransaction = Mockito.mock(ExpenseManagerTransaction.class);
    List<ExpenseManagerTransaction> mockExpenseManagerTransactions =
        Collections.singletonList(mockExpenseManagerTransaction);
    Mockito.when(mockExpenseUpdatable.add(mockExpenseManagerTransaction)).thenThrow(new PersistenceException("Cannot " +
        "add."));
    MockedStatic<DatabaseS3EventAnalyzer> mockDatabaseS3EventAnalyzer = null;
    MockedStatic<ExpenseTransactionTransformer> mockExpenseTransactionTransformer = null;
    try {
      mockDatabaseS3EventAnalyzer = Mockito.mockStatic(DatabaseS3EventAnalyzer.class);
      mockDatabaseS3EventAnalyzer.when(() ->
          DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockChangedEmDbFileNotification))
          .thenReturn(mockOptionalS3ObjectId);
      mockExpenseTransactionTransformer = Mockito.mockStatic(ExpenseTransactionTransformer.class);
      mockExpenseTransactionTransformer.when(() -> ExpenseTransactionTransformer.mapExpenseReports(mockExpenseReports))
          .thenReturn(mockExpenseManagerTransactions);
      assertThatThrownBy(() -> s3ExpenseManagerUpdater.updateExpenseManager(mockChangedEmDbFileNotification))
          .isInstanceOf(PersistenceException.class)
          .hasMessage("Cannot add.");
    } finally {
      if (mockDatabaseS3EventAnalyzer != null) {
        mockDatabaseS3EventAnalyzer.close();
      }
      if (mockExpenseTransactionTransformer != null) {
        mockExpenseTransactionTransformer.close();
      }
    }
  }
}