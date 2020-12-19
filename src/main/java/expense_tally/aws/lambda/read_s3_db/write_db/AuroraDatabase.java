package expense_tally.aws.lambda.read_s3_db.write_db;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class AuroraDatabase {
  private URL connectionAddress;

  public AuroraDatabase(URL connectionAddress) {
    this.connectionAddress = connectionAddress;
  }

  public AuroraDatabase(String connectionString) throws MalformedURLException {
    this.connectionAddress = new URL("HTTP", connectionString, 3306, "");
  }


}
