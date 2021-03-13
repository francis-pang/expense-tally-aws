package expense_tally.aws.s3;


import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DatabaseS3EventAnalyzerTest {
  @Mock
  private S3Event mockS3Event;

  @Test
  void extractChangedS3ObjectId_success() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = "testKey";
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    String testS3Bucket = "testBucket";
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(testS3Bucket);

    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isNotEmpty()
        .hasValueSatisfying(s3ObjectId -> {
          assertThat(testS3Key.equals(s3ObjectId.getKey()));
          assertThat(testS3Bucket.equals(s3ObjectId.getBucket()));
        });
  }

  @Test
  void extractChangedS3ObjectId_versionIdIsNotNull() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = "testKey";
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    String testS3Bucket = "testBucket";
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(testS3Bucket);
    String testVersionId = "testVersionId";
    Mockito.when(mockS3ObjectEntity.getVersionId()).thenReturn(testVersionId);

    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isNotEmpty()
        .hasValueSatisfying(s3ObjectId -> {
          assertThat(testS3Key.equals(s3ObjectId.getKey()));
          assertThat(testS3Bucket.equals(s3ObjectId.getBucket()));
          assertThat(testVersionId.equals(s3ObjectId.getVersionId()));
        });
  }

  @Test
  void extractChangedS3ObjectId_null() {
    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Event cannot be null.");
  }

  @Test
  void extractChangedS3ObjectId_S3EventNotificationRecordIsEmpty() {
    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void extractChangedS3ObjectId_S3EventNotificationRecordIsNull() {
    Mockito.when(mockS3Event.getRecords()).thenReturn(null);
    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void extractChangedS3ObjectId_s3EntityIsNull() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    S3EventNotification.S3EventNotificationRecord mockSecondRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        List.of(mockFirstRecord, mockSecondRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void extractChangedS3ObjectId_S3ObjectEntityIsNull() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void extractChangedS3ObjectId_s3ObjectKeyIsNull() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    String testS3Bucket = "testBucket";
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(testS3Bucket);
    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isInstanceOf(S3IllegalStatusException.class)
        .hasMessage("S3 bucket name and object key need to be present or absent together.");
  }

  @Test
  void extractChangedS3ObjectId_s3ObjectKeyIsEmpty() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = StringUtils.EMPTY;
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    String testS3Bucket = "testBucket";
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(testS3Bucket);

    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isInstanceOf(S3IllegalStatusException.class)
        .hasMessage("S3 bucket name and object key need to be present or absent together.");
  }

  @Test
  void extractChangedS3ObjectId_s3BucketIsNull() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = "testS3key";
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isInstanceOf(S3IllegalStatusException.class)
        .hasMessage("S3 bucket name and object key need to be present or absent together.");
  }

  @Test
  void extractChangedS3ObjectId_s3BucketIsEmpty() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = "testS3key";
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(StringUtils.EMPTY);

    assertThatThrownBy(() -> DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isInstanceOf(S3IllegalStatusException.class)
        .hasMessage("S3 bucket name and object key need to be present or absent together.");
  }

  @Test
  void extractChangedS3ObjectId_s3BucketAndKeyAreEmpty() {
    S3EventNotification.S3EventNotificationRecord mockFirstRecord =
        Mockito.mock(S3EventNotification.S3EventNotificationRecord.class);
    List<S3EventNotification.S3EventNotificationRecord> mockS3EventNotificationRecords =
        Collections.singletonList(mockFirstRecord);
    Mockito.when(mockS3Event.getRecords()).thenReturn(mockS3EventNotificationRecords);
    S3EventNotification.S3Entity mockS3Entity = Mockito.mock(S3EventNotification.S3Entity.class);
    Mockito.when(mockFirstRecord.getS3()).thenReturn(mockS3Entity);
    S3EventNotification.S3ObjectEntity mockS3ObjectEntity = Mockito.mock(S3EventNotification.S3ObjectEntity.class);
    Mockito.when(mockS3Entity.getObject()).thenReturn(mockS3ObjectEntity);
    String testS3Key = StringUtils.EMPTY;
    Mockito.when(mockS3ObjectEntity.getKey()).thenReturn(testS3Key);
    S3EventNotification.S3BucketEntity mockS3BucketEntity = Mockito.mock(S3EventNotification.S3BucketEntity.class);
    Mockito.when(mockS3Entity.getBucket()).thenReturn(mockS3BucketEntity);
    String testS3Bucket = StringUtils.EMPTY;
    Mockito.when(mockS3BucketEntity.getName()).thenReturn(testS3Bucket);

    assertThat(DatabaseS3EventAnalyzer.extractChangedS3ObjectId(mockS3Event))
        .isNotNull()
        .isEmpty();
  }
}