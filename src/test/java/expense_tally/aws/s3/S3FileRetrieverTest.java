package expense_tally.aws.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectId;
import expense_tally.aws.log.ObjectToString;
import expense_tally.aws.s3.S3FileRequestFactory;
import expense_tally.aws.s3.S3FileRetriever;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class S3FileRetrieverTest {
  @Mock
  private AmazonS3 mockAmazonS3;

  @Mock
  private File mockFile;

  @InjectMocks
  private S3FileRetriever s3FileRetriever;

  @Test
  void create_success() {
    assertThat(S3FileRetriever.create(mockAmazonS3))
        .isNotNull();
  }

  @Test
  void create_nullAmazonS3() {
    assertThatThrownBy(() -> S3FileRetriever.create(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void downloadFile_fileNotExist() throws IOException {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(false);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenReturn(mockFilePath);
      try (MockedStatic<S3FileRequestFactory> mockS3ExpnsMngrFileRequestFactory =
               Mockito.mockStatic(S3FileRequestFactory.class)) {
        GetObjectRequest mockGetObjectRequest = Mockito.mock(GetObjectRequest.class);
        mockS3ExpnsMngrFileRequestFactory.when(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
            .thenReturn(mockGetObjectRequest);
        Mockito.when(mockAmazonS3.getObject(mockGetObjectRequest, mockFile)).thenReturn(Mockito.mock(ObjectMetadata.class));
        assertThat(s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
            .isTrue();
      }
    }
  }

  @Test
  void downloadFile_fileExists() throws IOException {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(true);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenReturn(mockFilePath);
      try (MockedStatic<S3FileRequestFactory> mockS3ExpnsMngrFileRequestFactory =
               Mockito.mockStatic(S3FileRequestFactory.class)) {
        GetObjectRequest mockGetObjectRequest = Mockito.mock(GetObjectRequest.class);
        mockS3ExpnsMngrFileRequestFactory.when(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
            .thenReturn(mockGetObjectRequest);
        Mockito.when(mockAmazonS3.getObject(mockGetObjectRequest, mockFile)).thenReturn(Mockito.mock(ObjectMetadata.class));
        assertThat(s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
            .isTrue();
      }
    }
  }

  @Test
  void downloadFile_noFileWritePermission() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(false);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenThrow(new
          SecurityException("No permission to create file."));
      assertThatThrownBy(() -> s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
          .isInstanceOf(SecurityException.class)
          .hasMessage("No permission to create file.");
    }
  }

  @Test
  void downloadFile_s3ObjectIdIsNull() {
    assertThatThrownBy(() -> s3FileRetriever.downloadFile(null, mockFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("S3 Object ID cannot be null.");
  }

  @Test
  void downloadFile_deleteFileFail() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(true);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.delete(mockFilePath)).thenThrow(new SecurityException("Cannot delete file."));
      assertThatThrownBy(() -> s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
          .isInstanceOf(SecurityException.class)
          .hasMessage("Cannot delete file.");
    }
  }

  @Test
  void downloadFile_createS3RequestFails() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(false);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenReturn(mockFilePath);
      assertThatThrownBy(() -> s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("S3 Object key cannot be null/ empty.");
    }
  }

  @Test
  void downloadFile_sendS3RequestFails() {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(false);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenReturn(mockFilePath);
      try (MockedStatic<S3FileRequestFactory> mockS3ExpnsMngrFileRequestFactory =
               Mockito.mockStatic(S3FileRequestFactory.class)) {
        GetObjectRequest mockGetObjectRequest = Mockito.mock(GetObjectRequest.class);
        mockS3ExpnsMngrFileRequestFactory.when(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
            .thenReturn(mockGetObjectRequest);
        Mockito.when(mockAmazonS3.getObject(mockGetObjectRequest, mockFile)).thenThrow(
            new SdkClientException("client error"));
        assertThatThrownBy(() -> s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
            .isInstanceOf(SdkClientException.class)
            .hasMessage("client error");
      }
    }
  }

  @Test
  void downloadFile_noResponse() throws IOException {
    S3ObjectId mockS3ObjectId = Mockito.mock(S3ObjectId.class);
    Path mockFilePath = Mockito.mock(Path.class);
    Mockito.when(mockFile.toPath()).thenReturn(mockFilePath);
    Mockito.when(mockFile.exists()).thenReturn(false);
    GetObjectRequest mockGetObjectRequest = Mockito.mock(GetObjectRequest.class);
    Mockito.when(mockAmazonS3.getObject(mockGetObjectRequest, mockFile)).thenReturn(null);
    try (MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class)) {
      mockFiles.when(() -> Files.createFile(mockFilePath)).thenReturn(mockFilePath);
      try (MockedStatic<S3FileRequestFactory> mockS3ExpnsMngrFileRequestFactory =
               Mockito.mockStatic(S3FileRequestFactory.class)) {
        mockS3ExpnsMngrFileRequestFactory.when(() -> S3FileRequestFactory.createRequest(mockS3ObjectId))
            .thenReturn(mockGetObjectRequest);
        try (MockedStatic<ObjectToString> mockObjectToString = Mockito.mockStatic(ObjectToString.class)) {
          mockObjectToString.when(() -> ObjectToString.extractStringFromObject(Mockito.any(Object.class)))
              .thenReturn("");
          assertThat(s3FileRetriever.downloadFile(mockS3ObjectId, mockFile))
              .isFalse();

        }
      }
    }
  }
}