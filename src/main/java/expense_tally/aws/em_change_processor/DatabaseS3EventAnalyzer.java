package expense_tally.aws.em_change_processor;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.em_change_processor.log.ObjectToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Analyze on incoming S3 event for specific criteria to fit the database information
 */
public class DatabaseS3EventAnalyzer {
  private static final Logger LOGGER = LogManager.getLogger(DatabaseS3EventAnalyzer.class);

  /**
   * Returns the {@link S3ObjectId} mentioned in the S3 event.
   * @return the {@link S3ObjectId} mentioned in the S3 event.
   */
  public S3ObjectId extractChangedS3ObjectId() {
    return null; //stub
  }
}
