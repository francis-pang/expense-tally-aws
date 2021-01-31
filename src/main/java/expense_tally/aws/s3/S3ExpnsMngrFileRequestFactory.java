package expense_tally.aws.s3;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.exception.StringResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a factory that provide the requested {@link GetObjectRequest} based on the different input.
 * <p>
 *   {@link GetObjectRequest} is a request to retrieve S3 object. S3 Client will need this request to download the
 *   object based on the content of the request.
 * </p>
 */
public final class S3ExpnsMngrFileRequestFactory {
  private static final Logger LOGGER = LogManager.getLogger(S3ExpnsMngrFileRequestFactory.class);

  /**
   * Creates a {@link GetObjectRequest} based on <i>s3ObjectId</i>
   * @param s3ObjectId An Immutable S3 object identifier. Used to uniquely identify an S3 object.
   * @return a {@link GetObjectRequest} based on <i>s3ObjectId</i>
   */
  public static GetObjectRequest createRequest(S3ObjectId s3ObjectId) {
    if (s3ObjectId == null) {
      LOGGER.atError().log("s3ObjectId is null.");
      throw new IllegalArgumentException("S3 Object ID cannot be null.");
    }
    String key = s3ObjectId.getKey();
    if (StringUtils.isBlank(key)) {
      LOGGER.atError().log("key is blank:{}", StringResolver.resolveNullableString(key));
      throw new IllegalArgumentException("S3 Object key cannot be null/ empty.");
    }
    String bucket = s3ObjectId.getBucket();
    if (StringUtils.isBlank(bucket)) {
      LOGGER.atError().log("bucket is blank:{}", StringResolver.resolveNullableString(bucket));
      throw new IllegalArgumentException("S3 Object bucket cannot be null/ empty.");
    }
    String versionId = s3ObjectId.getVersionId();
    if (StringUtils.isBlank(versionId)) {
      LOGGER.atInfo().log("versionId is blank:{}", StringResolver.resolveNullableString(versionId));
    }
    return new GetObjectRequest(s3ObjectId);
  }
}
