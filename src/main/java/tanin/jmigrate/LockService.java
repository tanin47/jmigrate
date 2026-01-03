package tanin.jmigrate;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class LockService {
  private static final Logger logger = Logger.getLogger(LockService.class.getName());
  public DatabaseConnection connection;
  public int timeoutInSeconds = 10;

  LockService(DatabaseConnection connection) {
    this.connection = connection;
  }

  public interface Process {
    void process() throws SQLException, JMigrate.ExecutingDownScriptForbiddenException;
  }

  void lock(Process process) throws SQLException, InterruptedException, TimeoutException, JMigrate.ExecutingDownScriptForbiddenException {
    var salt = java.util.UUID.randomUUID().toString();
    try {
      acquireLock(salt);
      process.process();
    } finally {
      releaseLock(salt);
    }
  }

  public void acquireLock(String salt) throws SQLException, InterruptedException, TimeoutException {
    logger.info("Acquiring lock for JMigrate");
    var deadline = Instant.now().plus(timeoutInSeconds, ChronoUnit.SECONDS);
    while (true) {
      if ((Instant.now().isAfter(deadline))) {
        throw new TimeoutException("Unable to acquire the lock for JMigrate. If you are certain no process is running, please clear the table `jmigrate_lock` and try again.");
      }

      try {
        connection.execute(
          "INSERT INTO \"jmigrate_lock\" (\"locked\", \"salt\", \"acquired_at\") VALUES (?, ?, ?);",
          new Object[]{
            true,
            salt,
            new Timestamp(Instant.now().toEpochMilli())
          }
        );
        break;
      } catch (SQLException e) {
        if (e.getSQLState().equals("23505")) {
          logger.info("Unable to acquire the lock for JMigrate. Sleeping for 1 second...");
          Thread.sleep(1000);
        } else {
          throw e;
        }
      }
    }
    logger.info("Acquired lock for JMigrate");
  }

  public void releaseLock(String salt) throws SQLException {
    logger.info("Releasing lock for JMigrate");
    connection.execute(
      "DELETE FROM \"jmigrate_lock\" WHERE \"salt\" = ?",
      new Object[]{salt}
    );
    logger.info("Released lock for JMigrate");
  }
}
