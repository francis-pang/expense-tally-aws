package expense_tally.aws.lambda.read_s3_db.write_db;

import org.apache.ibatis.annotations.Update;

public interface ExpenseManagerTransactionMapper {
  @Update(value = "TRUNCATE TABLE expense_manager_transaction")
  public boolean deleteAll();
}
