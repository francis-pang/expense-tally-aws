package expense_tally.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.log.ObjectToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class assists to download the Expense Manager file from Amazon Simple Storage Service.
 */
public final class S3ExpnsMngrFileRetriever {
  private static final Logger LOGGER = LogManager.getLogger(S3ExpnsMngrFileRetriever.class);
  private final AmazonS3 amazonS3;

  /**
   * Default constructor
   * @param amazonS3 Client to interface with AWS Simple Storage Service
   */
  private S3ExpnsMngrFileRetriever(AmazonS3 amazonS3) {
    this.amazonS3 = Objects.requireNonNull(amazonS3);
  }

  /**
   * Create a new instance of <i>S3ExpnsMngrFileRetriever</i> based on <i>amazonS3</i>
   * @param amazonS3 Client to interface with AWS Simple Storage Service
   * @return a new instance of <i>S3ExpnsMngrFileRetriever</i> based on <i>amazonS3</i>
   */
  public static S3ExpnsMngrFileRetriever create(AmazonS3 amazonS3) {
    return new S3ExpnsMngrFileRetriever(amazonS3);
  }

  /**
   * Download the file specified by <i>s3ObjectId</i> onto the <i>destinationFilePath</i>
   * @param s3ObjectId An S3 object identifier of the downloading file
   * @param destinationFile the file path to be stored, include the file name.
   * @throws IllegalArgumentException if the <i>s3ObjectId</i> is null.
   * @throws IOException if cannot write to <i>destinationFile</i>
   * @throws com.amazonaws.SdkClientException if the application has issue with the S3 client
   * @throws com.amazonaws.AmazonServiceException if there is problem with Amazon S3 service
   */
  public boolean downloadFile(S3ObjectId s3ObjectId, File destinationFile) throws IOException {
    if (s3ObjectId == null) {
      LOGGER.atWarn().log("s3ObjectId is null");
      throw new IllegalArgumentException("S3 Object ID cannot be null.");
    }
    createFile(destinationFile);
    GetObjectRequest getObjectRequest = createS3Request(s3ObjectId);
    ObjectMetadata objectMetadata = sendS3Request(getObjectRequest, destinationFile);
    return analyzeResponse(objectMetadata);
  }

  private void createFile(File destinationFile) throws IOException {
    Path destinationFilePath = destinationFile.toPath();
    if (destinationFile.exists()) {
      LOGGER.atInfo().log("Download file already exists: {}", destinationFile.getAbsolutePath());
      LOGGER.atInfo().log("Deleting file {}", destinationFile.getAbsolutePath());
      try {
        Files.delete(destinationFile.toPath());
      } catch (RuntimeException runtimeException) {
        LOGGER.atWarn()
            .withThrowable(runtimeException)
            .log("Unable to delete file at {}", destinationFile.getAbsolutePath());
        throw runtimeException;
      }
    }
    Files.createFile(destinationFilePath);
    LOGGER.atInfo().log("The download file path has been created at {}", destinationFile.getAbsolutePath());
  }

  private GetObjectRequest createS3Request(S3ObjectId s3ObjectId) {
    return S3ExpnsMngrFileRequestFactory.createRequest(s3ObjectId);
  }

  private ObjectMetadata sendS3Request(GetObjectRequest getObjectRequest, File destinationFile) {
    LOGGER.atInfo().log("Sending S3 Request. expenseManagerS3Request:{}, expenseManagerFile:{}",
        ObjectToString.extractStringFromObject(getObjectRequest),
        ObjectToString.extractStringFromObject(destinationFile));
    return amazonS3.getObject(getObjectRequest, destinationFile);
  }

  private boolean analyzeResponse(ObjectMetadata objectMetadata) {
    if (objectMetadata == null) {
      LOGGER.atWarn().log("Unable to copy database file from S3.");
      return false;
    }
    LOGGER.atDebug().log("S3 request sent. objectMetadata:{}",
        ObjectToString.extractStringFromObject(objectMetadata));
    return true;
  }
}
