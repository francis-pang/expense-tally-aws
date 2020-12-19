package expense_tally.aws.lambda.read_s3_db;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import expense_tally.aws.lambda.read_s3_db.log.ObjectToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Analyze on incoming S3 event for specific criteria to fit the database information
 */
public class DatabaseS3EventAnalyzer {
  private static final Logger LOGGER = LogManager.getLogger(DatabaseS3EventAnalyzer.class);
  private static final String S3_UPDATE_CREATE_EVENT_NAME = "ObjectCreated:Put";
  private String databaseFileName;

  public DatabaseS3EventAnalyzer(String databaseFileName) {
    this.databaseFileName = databaseFileName;
  }

  public boolean extractDatabaseObjectKey(S3Event s3Event) {
    List<S3EventNotificationRecord> s3EventRecords = s3Event.getRecords();
    s3EventRecords.forEach(s3EventNotificationRecord -> extractDatabaseObjectKey(s3EventNotificationRecord));
    return false;
  }

  private String extractDatabaseObjectKey(S3EventNotificationRecord s3EventNotificationRecord) {
    LOGGER.atInfo()
        .log("s3EventNotificationRecord: {}", ObjectToString.extractStringFromObject(s3EventNotificationRecord));

    if (!isUpdateOrCreateEvent(s3EventNotificationRecord)) {
      return "";
    }
    S3Entity s3Entity = s3EventNotificationRecord.getS3();
    if (s3Entity == null) {
      return "";
    }
    S3EventNotification.S3ObjectEntity s3ObjectEntity = s3Entity.getObject();
    if (s3ObjectEntity == null) {
      return "";
    }
    String s3ObjectEntityKey = s3ObjectEntity.getKey();
    return (isDatabaseFile(s3ObjectEntityKey)) ? s3ObjectEntityKey : "";
  }

  private boolean isDatabaseFile(String s3ObjectKey) {
    return databaseFileName.equals(s3ObjectKey);
  }

  private boolean isUpdateOrCreateEvent(S3EventNotificationRecord s3EventNotificationRecord) {
    return S3_UPDATE_CREATE_EVENT_NAME.equals(s3EventNotificationRecord.getEventName());
  }
}
