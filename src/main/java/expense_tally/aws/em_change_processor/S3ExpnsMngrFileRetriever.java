package expense_tally.aws.em_change_processor;

import com.amazonaws.services.s3.model.S3ObjectId;

/**
 * This class assists to download the Expense Manager file from Amazon Web Service Simple Storage Service.
 */
public final class S3ExpnsMngrFileRetriever {
  /**
   * Download the file specified by <i>s3ObjectId</i> onto the <i>destinationFilePath</i>
   * @param s3ObjectId An S3 object identifier of the expense manager database file
   * @param destinationFilePath location of the expense manager database file to be stored, include the file name.
   */
  public void downloadFile(S3ObjectId s3ObjectId, String destinationFilePath) {

  }
}
