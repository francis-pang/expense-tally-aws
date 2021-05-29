package expense_tally.aws.em_change_processor.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import expense_tally.aws.aurora.AuroraDatabaseConfiguration;
import expense_tally.aws.em_change_processor.S3ExpenseManagerUpdater;
import expense_tally.aws.em_change_processor.configuration.configuration.EmChangeProcessorConfiguration;
import expense_tally.aws.em_change_processor.configuration.configuration.EmChangeProcessorConfigurationParser;
import expense_tally.aws.s3.S3FileRetriever;
import expense_tally.expense_manager.persistence.ExpenseReportReadable;
import expense_tally.expense_manager.persistence.ExpenseUpdatable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExpenseManagerFileChangeS3EventHandlerTest {
  @Mock
  private S3ExpenseManagerUpdater mockS3ExpenseManagerUpdater;

  @Mock
  private EmChangeProcessorConfiguration mockEmChangeProcessorConfiguration;

  @Mock
  private AmazonS3 mockAmazonS3;

  @Mock
  private AuroraDatabaseConfiguration mockAuroraDatabaseConfiguration;

  private ExpenseManagerFileChangeS3EventHandler expenseManagerFileChangeS3EventHandler;

  @BeforeEach
  void setUp() {
    MockedStatic<EmChangeProcessorConfigurationParser> mockConfigurationParser = null;
    MockedStatic<AmazonS3ClientBuilder> mockAmazonS3ClientBuilder = null;
    Mockito.when(mockEmChangeProcessorConfiguration.getLocalDbFilePath()).thenReturn("testDbpath");
    Mockito.when(mockEmChangeProcessorConfiguration.getAuroraDatabaseConfiguration()).thenReturn(mockAuroraDatabaseConfiguration);
    Mockito.when(mockAuroraDatabaseConfiguration.getHostUrl()).thenReturn("testHost");
    Mockito.when(mockAuroraDatabaseConfiguration.getDatabaseName()).thenReturn("testDb");
    MockedStatic<S3ExpenseManagerUpdater> mockS3ExpenseManagerUpdaterStatic = null;
    try {
      mockConfigurationParser = Mockito.mockStatic(EmChangeProcessorConfigurationParser.class);
      mockConfigurationParser.when(EmChangeProcessorConfigurationParser::parseSystemEnvironmentVariableConfiguration)
          .thenReturn(mockEmChangeProcessorConfiguration);
      mockAmazonS3ClientBuilder = Mockito.mockStatic(AmazonS3ClientBuilder.class);
      mockAmazonS3ClientBuilder.when(AmazonS3ClientBuilder::defaultClient).thenReturn(mockAmazonS3);
      mockS3ExpenseManagerUpdaterStatic = Mockito.mockStatic(S3ExpenseManagerUpdater.class);
      mockS3ExpenseManagerUpdaterStatic.when(() -> S3ExpenseManagerUpdater.create(Mockito.any(S3FileRetriever.class),
          Mockito.any(ExpenseReportReadable.class), Mockito.any(ExpenseUpdatable.class), Mockito.any(File.class)))
          .thenReturn(mockS3ExpenseManagerUpdater);
      expenseManagerFileChangeS3EventHandler = new ExpenseManagerFileChangeS3EventHandler();

    } finally {
      if (mockConfigurationParser != null) {
        mockConfigurationParser.close();
      }
      if (mockAmazonS3ClientBuilder != null) {
        mockAmazonS3ClientBuilder.close();
      }
      if (mockS3ExpenseManagerUpdaterStatic != null) {
        mockS3ExpenseManagerUpdaterStatic.close();
      }
    }
  }

  @Test
  void init_success() {
    MockedStatic<EmChangeProcessorConfigurationParser> mockConfigurationParser = null;
    MockedStatic<AmazonS3ClientBuilder> mockAmazonS3ClientBuilder = null;
    Mockito.when(mockEmChangeProcessorConfiguration.getLocalDbFilePath()).thenReturn("testDbpath");
    Mockito.when(mockEmChangeProcessorConfiguration.getAuroraDatabaseConfiguration()).thenReturn(mockAuroraDatabaseConfiguration);
    Mockito.when(mockAuroraDatabaseConfiguration.getHostUrl()).thenReturn("testHost");
    Mockito.when(mockAuroraDatabaseConfiguration.getDatabaseName()).thenReturn("testDb");
    try {
      mockConfigurationParser = Mockito.mockStatic(EmChangeProcessorConfigurationParser.class);
      mockConfigurationParser.when(EmChangeProcessorConfigurationParser::parseSystemEnvironmentVariableConfiguration)
          .thenReturn(mockEmChangeProcessorConfiguration);
      mockAmazonS3ClientBuilder = Mockito.mockStatic(AmazonS3ClientBuilder.class);
      mockAmazonS3ClientBuilder.when(AmazonS3ClientBuilder::defaultClient).thenReturn(mockAmazonS3);
      assertThat(new ExpenseManagerFileChangeS3EventHandler())
          .isNotNull();
    } finally {
      if (mockConfigurationParser != null) {
        mockConfigurationParser.close();
      }
      if (mockAmazonS3ClientBuilder != null) {
        mockAmazonS3ClientBuilder.close();
      }
    }
  }

  @Test
  void handleRequest_noRecordInEvent() {
    S3Event mockEmFileChangeEvent = Mockito.mock(S3Event.class);
    Context mockContext = Mockito.mock(Context.class);
    expenseManagerFileChangeS3EventHandler.handleRequest(mockEmFileChangeEvent, mockContext);
  }

  @Test
  void handleRequest_exception() throws IOException, SQLException {
    S3Event mockEmFileChangeEvent = Mockito.mock(S3Event.class);
    Context mockContext = Mockito.mock(Context.class);
    Mockito.doThrow(new IOException("test IOException"))
        .when(mockS3ExpenseManagerUpdater).updateExpenseManager(mockEmFileChangeEvent);
    expenseManagerFileChangeS3EventHandler.handleRequest(mockEmFileChangeEvent, mockContext);
  }
}