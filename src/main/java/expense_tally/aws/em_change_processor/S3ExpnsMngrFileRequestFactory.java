package expense_tally.aws.em_change_processor;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectId;

/**
 * This class is a factory that provide the requested {@link GetObjectRequest} based on the different input.
 * <p>
 *   {@link GetObjectRequest} is a request to retrieve S3 object. S3 Client will need this request to download the
 *   object based on the content of the request.
 * </p>
 */
public final class S3ExpnsMngrFileRequestFactory {

  /**
   * Creates a {@link GetObjectRequest} based on <i>s3ObjectId</i>
   * @param s3ObjectId An Immutable S3 object identifier. Used to uniquely identify an S3 object.
   * @return a {@link GetObjectRequest} based on <i>s3ObjectId</i>
   */
  public static GetObjectRequest createRequest(S3ObjectId s3ObjectId) {
    return new GetObjectRequest(s3ObjectId);
  }
}
