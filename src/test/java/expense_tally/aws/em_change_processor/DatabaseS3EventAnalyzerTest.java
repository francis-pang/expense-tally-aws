package expense_tally.aws.em_change_processor;


import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.s3.DatabaseS3EventAnalyzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DatabaseS3EventAnalyzerTest {
  @Mock
  private S3Event mockS3Event;

  @Test
  void extractChangedS3ObjectId_success() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEqualTo(mockS3ObjectId);
  }

  @Test
  void extractChangedS3ObjectId_null() {
    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Event cannot be null.");
  }
}