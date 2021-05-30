package expense_tally.aws.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationErrorCodeTest {

  @Test
  void resolve_positive() {
    assertThat(ApplicationErrorCode.resolve(400))
        .isNotNull()
        .hasValueSatisfying(ApplicationErrorCode.KNOWN_EXCEPTION::equals);
  }

  @Test
  void resolve_negative() {
    assertThat(ApplicationErrorCode.resolve(100))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void value_position() {
    assertThat(ApplicationErrorCode.KNOWN_EXCEPTION.value())
        .isEqualTo(400);
  }
}