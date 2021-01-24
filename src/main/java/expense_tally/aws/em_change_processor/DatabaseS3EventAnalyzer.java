package expense_tally.aws.em_change_processor;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Analyze on incoming S3 event for specific criteria to fit the database information
 */
public class DatabaseS3EventAnalyzer {
  private static final Logger LOGGER = LogManager.getLogger(DatabaseS3EventAnalyzer.class);

  /**
   *
   * @param s3Event
   * @return the {@link S3ObjectId} mentioned in the S3 event.
   */
  public static Optional<S3ObjectId> extractChangedS3ObjectId(S3Event s3Event) {
    if (s3Event == null) {
      LOGGER.atError().log("s3Event is null.");
      throw new IllegalArgumentException("S3 Event cannot be null.");
    }
    List<S3EventNotificationRecord> s3EventNotificationRecords = s3Event.getRecords();
    if (s3EventNotificationRecords == null) {
      LOGGER.atInfo().log("s3EventNotificationRecords is null.");
      return Optional.empty();
    }
    if (s3EventNotificationRecords.isEmpty()) {
      LOGGER.atInfo().log("There are no S3EventNotificationRecord.");
      return Optional.empty();
    }
    if (s3EventNotificationRecords.size() > 1) {
      LOGGER.atInfo().log("There are more than 1 S3EventNotificationRecord. size:{}",
          s3EventNotificationRecords.size());
    }
    // We only read the first record because the AWS template has defined to read PUT object reject for the database
    // file explicitly.
    S3EventNotificationRecord firstRecord = s3EventNotificationRecords.get(0);
    return extractS3ObjectId(firstRecord);
  }

  private static Optional<S3ObjectId> extractS3ObjectId(S3EventNotificationRecord s3EventNotificationRecord) {
    S3Entity s3Entity = s3EventNotificationRecord.getS3();
    if (s3Entity == null) {
      LOGGER.atDebug().log("s3Entity is null.");
      return Optional.empty();
    }
    S3EventNotification.S3ObjectEntity s3ObjectEntity = s3Entity.getObject();
    if (s3ObjectEntity == null) {
      LOGGER.atDebug().log("s3ObjectEntity is null.");
      return Optional.empty();
    }
    String s3ObjectKey = s3ObjectEntity.getKey();
    String s3ObjectVersionId = s3ObjectEntity.getVersionId();
    s3ObjectVersionId = (StringUtils.isBlank(s3ObjectVersionId) ? s3ObjectVersionId : null);
    String s3BucketName = extractS3BucketName(s3Entity);
    validateS3InformationState(s3BucketName, s3ObjectKey);
    LOGGER.atDebug().log("Creating s3ObjectId. s3BucketName:{}, s3ObjectKey:{}, s3ObjectVersionId:{}",
        StringResolver.resolveNullableString(s3BucketName),
        StringResolver.resolveNullableString(s3ObjectKey),
        StringResolver.resolveNullableString(s3ObjectVersionId));
    S3ObjectId s3ObjectId = new S3ObjectId(s3BucketName, s3ObjectKey, s3ObjectVersionId);
    return Optional.of(s3ObjectId);
  }

  private static String extractS3BucketName(S3Entity s3Entity) {
    S3EventNotification.S3BucketEntity s3BucketEntity = s3Entity.getBucket();
    if (s3BucketEntity == null) {
      LOGGER.atDebug().log("s3ObjectEntity is null.");
      return null;
    }
    return s3BucketEntity.getName();
  }

  private static void validateS3InformationState(String s3BucketName, String s3ObjectKey) {
    boolean isS3KeyBlank = StringUtils.isBlank(s3ObjectKey);
    boolean isS3BucketNameBlank = StringUtils.isBlank(s3BucketName);
    if ((isS3BucketNameBlank && !isS3KeyBlank) ||
        (!isS3BucketNameBlank && isS3KeyBlank)) {
      LOGGER.atError().log("Only either of s3ObjectKey or s3BucketName is blank. s3ObjectKey:{}; s3BucketName:{}",
          StringResolver.resolveNullableString(s3ObjectKey),
          StringResolver.resolveNullableString(s3BucketName));
      throw new S3IllegalStatusException("S3 bucket name and object key need to be present or absent together.");
    }
  }
}
