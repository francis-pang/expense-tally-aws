package expense_tally.aws.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemProxyTest {
  @Test
  void getEnvironmentVariable_envNotExist() {
    assertThat(SystemProxy.getEnvironmentVariable("not-exist"))
        .isNull();
  }
}