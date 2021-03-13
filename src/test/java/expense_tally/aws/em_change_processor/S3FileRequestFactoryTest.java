package expense_tally.aws.em_change_processor;

import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.s3.S3FileRequestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class S3FileRequestFactoryTest {
  @Mock
  private S3ObjectId mockS3ObjectId;

  @Test
  void createRequest_positive() {
    Mockito.when(mockS3ObjectId.getBucket()).thenReturn("test bucket");
    Mockito.when(mockS3ObjectId.getKey()).thenReturn("test key");
    assertThat(S3FileRequestFactory.createRequest(mockS3ObjectId));
  }

  @Test
  void createRequest_versionId() {
    Mockito.when(mockS3ObjectId.getBucket()).thenReturn("test bucket");
    Mockito.when(mockS3ObjectId.getKey()).thenReturn("test key");
    Mockito.when(mockS3ObjectId.getVersionId()).thenReturn("id");
    assertThat(S3FileRequestFactory.createRequest(mockS3ObjectId));
  }

  @Test
  void createRequest_null() {
    assertThatThrownBy(() -> S3FileRequestFactory.createRequest(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Object ID cannot be null.");
  }

  @Test
  void createRequest_noBucket() {
    Mockito.when(mockS3ObjectId.getKey()).thenReturn("test key");
    assertThatThrownBy(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Object bucket cannot be null/ empty.");
  }

  @Test
  void createRequest_noKey() {
    assertThatThrownBy(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Object key cannot be null/ empty.");
  }
}