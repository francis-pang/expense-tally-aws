package expense_tally.aws.em_change_processor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectId;
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
   * @param expenseManagerS3ObjectId An S3 object identifier of the expense manager database file
   * @param destinationFile the expense manager database file to be stored, include the file name.
   * @throws IllegalArgumentException if the <i>s3ObjectId</i> is null.
   * @throws IOException if cannot write to <i>destinationFile</i>
   * @throws com.amazonaws.SdkClientException if the application has issue with the S3 client
   * @throws com.amazonaws.AmazonServiceException if there is problem with Amazon S3 service
   */
  public boolean downloadFile(S3ObjectId expenseManagerS3ObjectId, File destinationFile) throws IOException {
    if (expenseManagerS3ObjectId == null) {
      LOGGER.atWarn().log("s3ObjectId is null");
      throw new IllegalArgumentException("S3 Object ID cannot be null.");
    }
    validateDownloadDestinationFile(destinationFile);
    createFile(destinationFile);
    GetObjectRequest expenseManagerS3Request = createS3Request(expenseManagerS3ObjectId);
    ObjectMetadata expenseManagerMetadata = sendS3Request(expenseManagerS3Request, destinationFile);
    return analyzeResponse(expenseManagerMetadata);
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

  private void validateDownloadDestinationFile(File destinationFile) {
    if (!destinationFile.isFile()) {
      LOGGER.atWarn().log("destinationFile is not a file path:{}", destinationFile.getAbsolutePath());
      throw new IllegalArgumentException("Destination must be a file.");
    }
    File parentDirectory = destinationFile.getParentFile();
    if (!parentDirectory.exists()) {
      LOGGER.atWarn().log("parentDirectory is invalid:{}", parentDirectory.getAbsolutePath());
      throw new IllegalArgumentException("Download file path is invalid.");
    }
  }

  private GetObjectRequest createS3Request(S3ObjectId s3ObjectId) {
    return S3ExpnsMngrFileRequestFactory.createRequest(s3ObjectId);
  }

  private ObjectMetadata sendS3Request(GetObjectRequest expenseManagerS3Request, File expenseManagerFile) {
    return amazonS3.getObject(expenseManagerS3Request, expenseManagerFile);
  }

  private boolean analyzeResponse(ObjectMetadata expenseManagerObjectMetadata) {
    if (expenseManagerObjectMetadata == null) {
      LOGGER.atWarn().log("Unable to copy database file from S3.");
      return false;
    }
    return true;
  }
}
