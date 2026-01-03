package tanin.jmigrate.postgres;

import org.junit.jupiter.api.BeforeEach;
import tanin.jmigrate.DatabaseConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base extends tanin.jmigrate.Base {
  static String DATABASE_URL = "postgres://jmigrate_test_user:test@127.0.0.1:5432/jmigrate_test";

  void resetDatabase() throws Exception {
    try (var conn = new DatabaseConnection(DATABASE_URL)) {
      conn.execute("DROP SCHEMA IF EXISTS public CASCADE");
      conn.execute("CREATE SCHEMA public");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    resetDatabase();
  }
}
