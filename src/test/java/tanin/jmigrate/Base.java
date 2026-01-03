package tanin.jmigrate;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base {

  void assertSameScript(MigrateScript script, AlreadyMigratedScript alreadyMigratedScript) {
    assertEquals(script.id(), alreadyMigratedScript.id());
    assertEquals(script.up(), alreadyMigratedScript.up());
    assertEquals(script.down(), alreadyMigratedScript.down());
  }

  protected void assertSameScripts(MigrateScript[] scripts, AlreadyMigratedScript[] alreadyMigratedScripts) {
    assertEquals(scripts.length, alreadyMigratedScripts.length);

    for (int i = 0; i < scripts.length; i++) {
      assertSameScript(scripts[i], alreadyMigratedScripts[i]);
    }
  }
}
