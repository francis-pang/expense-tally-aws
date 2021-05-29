package expense_tally.aws.aurora;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuroraDatabaseConfigurationTest {

  @Test
  void testEquals_sameObject() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    assertThat(auroraDatabaseConfiguration.equals(auroraDatabaseConfiguration)).isTrue();
  }

  @Test
  void testEquals_sameAttribute() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    AuroraDatabaseConfiguration rebuild = builder.build();
    assertThat(auroraDatabaseConfiguration).isEqualTo(rebuild);
  }

  @Test
  void testEquals_differentObject() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    String string = "";
    assertThat(auroraDatabaseConfiguration.equals(string)).isFalse();
  }

  @Test
  void testEquals_null() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    assertThat(auroraDatabaseConfiguration.equals(null)).isFalse();
  }

  @Test
  void testHashCode() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    assertThat(auroraDatabaseConfiguration.hashCode()).isEqualTo(auroraDatabaseConfiguration.hashCode());
  }

  @Test
  void testToString() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    AuroraDatabaseConfiguration auroraDatabaseConfiguration = builder.build();
    assertThat(auroraDatabaseConfiguration.toString()).isEqualTo("AuroraDatabaseConfiguration[hostUrl='test.com', databaseName='expense_manager', username='null', password='*****', environmentId='mysql', connectionTimeout=1000]");
  }

  @Test
  void testBuilderCredential_usernameIsNull() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    assertThatThrownBy(() -> builder.credential(null, "pw"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Database username cannot be null or blank.");
  }

  @Test
  void testBuilderCredential_passwordIsNull() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    assertThatThrownBy(() -> builder.credential("user", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Database password cannot be null or blank.");
  }

  @Test
  void testBuilderConnectionTimeout_negative() {
    AuroraDatabaseConfiguration.Builder builder = new AuroraDatabaseConfiguration.Builder("test.com");
    assertThatThrownBy(() -> builder.connectionTimeout(-10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Connection timeout must be positive.");
  }
}