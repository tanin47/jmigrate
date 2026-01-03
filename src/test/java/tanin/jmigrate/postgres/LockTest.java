package tanin.jmigrate.postgres;

import org.junit.jupiter.api.Test;
import tanin.jmigrate.DatabaseConnection;
import tanin.jmigrate.JMigrate;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class LockTest extends Base {
  @Test
  void throwTimeout() throws Exception {
    var scriptDir = new JMigrate.MigrateScriptDir(LockTest.class, "/postgres/timeoutsql");
    var thread = new Thread(() -> {
      try {
        var migrate = new JMigrate(DATABASE_URL, scriptDir, false);
        migrate.migrate();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    thread.start();
    Thread.sleep(500); // Ensure the thread starts running...

    var ex = assertThrows(TimeoutException.class, () -> {
      var migrate = new JMigrate(DATABASE_URL, scriptDir, false);
      migrate.lockService.timeoutInSeconds = 3;
      migrate.migrate();
    });
    assertEquals(
      "Unable to acquire the lock for JMigrate. If you are certain no process is running, please clear the table `jmigrate_lock` and try again.",
      ex.getMessage()
    );
    thread.interrupt();

    var conn = new DatabaseConnection(DATABASE_URL);
    conn.executeQuery("SELECT * FROM jmigrate_lock", rs -> {
      assertTrue(rs.next()); // The lock still exists because Thread.interrupt() skips the finally block.
      assertFalse(rs.next()); // Only one record of lock exists.
    });
  }
}
