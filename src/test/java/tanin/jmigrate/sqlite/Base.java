package tanin.jmigrate.sqlite;

import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;

public class Base extends tanin.jmigrate.Base {
  public static final Path dbFile;
  public static final String DATABASE_URL;

  static {
    try {
      dbFile = Files.createTempFile("jmigrate_test", ".db");
      DATABASE_URL = "jdbc:sqlite:" + dbFile.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  void resetDatabase() throws Exception {
    Files.deleteIfExists(dbFile);
  }

  @BeforeEach
  void setUp() throws Exception {
    resetDatabase();
  }
}
