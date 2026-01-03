package tanin.jmigrate;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

public class AlreadyMigratedScriptService {
  public DatabaseConnection connection;

  AlreadyMigratedScriptService(DatabaseConnection connection) {
    this.connection = connection;
  }

  public AlreadyMigratedScript[] getAll() throws SQLException {
    var result = new ArrayList<AlreadyMigratedScript>();

    connection.executeQuery(
      "SELECT id, up_script, down_script, applied_at FROM jmigrate_already_migrated_script ORDER BY id ASC",
      rs -> {
        while (rs.next()) {
          result.add(new AlreadyMigratedScript(
            rs.getInt("id"),
            rs.getString("up_script"),
            rs.getString("down_script"),
            rs.getTimestamp("applied_at").toInstant()
          ));
        }
      }
    );

    return result.toArray(new AlreadyMigratedScript[0]);
  }

  public void insert(MigrateScript migrateScript) throws SQLException {
    connection.execute(
      "INSERT INTO \"jmigrate_already_migrated_script\" (\"id\", \"up_script\", \"down_script\", \"applied_at\") VALUES (?, ?, ?, ?);",
      new Object[]{
        migrateScript.id(),
        migrateScript.up(),
        migrateScript.down(),
        new Timestamp(Instant.now().toEpochMilli())
      }
    );
  }

  public void remove(AlreadyMigratedScript alreadyMigratedScript) throws SQLException {
    connection.execute(
      "DELETE FROM \"jmigrate_already_migrated_script\" WHERE \"id\" = ?",
      new Object[]{
        alreadyMigratedScript.id()
      }
    );
  }
}
