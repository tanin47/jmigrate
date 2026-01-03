package tanin.jmigrate.sqlite;

import org.junit.jupiter.api.Test;
import tanin.jmigrate.JMigrate;

import static org.junit.jupiter.api.Assertions.*;

public class MigrateTest extends Base {
  @Test
  void migrateAndAutoRollback() throws Exception {
    // We want to test the static method:
    JMigrate.migrate(DATABASE_URL, MigrateTest.class, "/sqlite/testsql", true);

    var scriptDir = new JMigrate.MigrateScriptDir(MigrateTest.class, "/sqlite/testsql");
    var migrate = new JMigrate(DATABASE_URL, scriptDir, true);
    var scripts = JMigrate.getMigrateScripts(scriptDir);
    var alreadyMigratedScripts = migrate.alreadyMigratedScriptService.getAll();
    assertSameScripts(scripts, alreadyMigratedScripts);

    // Simulate changing 2.sql
    migrate.databaseConnection.execute("UPDATE jmigrate_already_migrated_script SET up_script = 'something else' WHERE id = 2;");
    alreadyMigratedScripts = migrate.alreadyMigratedScriptService.getAll();
    assertEquals("something else", alreadyMigratedScripts[1].up());

    // The previous scripts are reverted, and the new scripts are applied.
    migrate.migrate();
    alreadyMigratedScripts = migrate.alreadyMigratedScriptService.getAll();
    assertSameScripts(scripts, alreadyMigratedScripts);

    migrate.databaseConnection.executeQuery("SELECT * FROM jmigrate_lock", rs -> {
      assertFalse(rs.next()); // no lock
    });

    // Simulate changing 2.sql but not disallowing rollback
    migrate.databaseConnection.execute("UPDATE jmigrate_already_migrated_script SET up_script = 'something else' WHERE id = 2;");
    alreadyMigratedScripts = migrate.alreadyMigratedScriptService.getAll();
    assertEquals("something else", alreadyMigratedScripts[1].up());

   var ex = assertThrows(
     JMigrate.ExecutingDownScriptForbiddenException.class,
     () -> JMigrate.migrate(DATABASE_URL, MigrateTest.class, "/sqlite/testsql", false)
   );
   assertTrue(ex.getMessage().contains("2.sql"));
  }

  @Test
  void getMigrateScripts() {
    var scripts = JMigrate.getMigrateScripts(new JMigrate.MigrateScriptDir(MigrateTest.class, "/sqlite/testsql"));
    assertEquals(3, scripts.length);

    assertEquals(1, scripts[0].id());
    assertEquals(
      """
        CREATE TABLE "jmigrate_test_user"
        (
            id INTEGER PRIMARY KEY,
            username TEXT NOT NULL,
            hashed_password TEXT NOT NULL,
            password_expired_at TIMESTAMP
        );
        """.trim(),
      scripts[0].up()
    );
    assertEquals(
      "DROP TABLE \"jmigrate_test_user\";",
      scripts[0].down()
    );

    assertEquals(2, scripts[1].id());
    assertEquals(
      "ALTER TABLE \"jmigrate_test_user\" DROP COLUMN \"password_expired_at\";",
      scripts[1].up()
    );
    assertEquals(
      "ALTER TABLE \"jmigrate_test_user\" ADD COLUMN \"password_expired_at\" TIMESTAMP;",
      scripts[1].down()
    );

    assertEquals(3, scripts[2].id());
    assertEquals(
      "ALTER TABLE \"jmigrate_test_user\" ADD COLUMN \"age\" INT;",
      scripts[2].up()
    );
    assertEquals(
      "ALTER TABLE \"jmigrate_test_user\" DROP COLUMN \"age\";",
      scripts[2].down()
    );
  }
}
