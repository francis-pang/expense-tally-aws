package expense_tally.aws.em_change_processor;

import com.amazonaws.services.s3.model.S3ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.geom.IllegalPathStateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class S3ExpnsMngrFileRetrieverTest {
  @InjectMocks
  private S3ExpnsMngrFileRetriever s3ExpnsMngrFileRetriever;

  @Test
  void downloadFile_pass() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    assertThat(s3ExpnsMngrFileRetriever.downloadFile(mockS3ObjectId, "/tmp/em.db"))
      .isTrue();
  }

  @Test
  void downloadFile_fileIsNotFile() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    assertThatThrownBy(() -> s3ExpnsMngrFileRetriever.downloadFile(mockS3ObjectId, "/tmp/em.db"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("destinationFilePath is not a file path.");
  }

  @Test
  void downloadFile_noFileWritePermission() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    assertThatThrownBy(() -> s3ExpnsMngrFileRetriever.downloadFile(mockS3ObjectId, "/tmp/em.db"))
        .isInstanceOf(FilePermissionException.class)
        .hasMessage("No permission to create create at directory.");
  }

  @Test
  void downloadFile_s3ObjectIdIsNull() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    assertThatThrownBy(() -> s3ExpnsMngrFileRetriever.downloadFile(mockS3ObjectId, "/tmp/em.db"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Object ID cannot be null.");
  }
}