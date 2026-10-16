package com.example.asm_final;

import static org.junit.Assert.*;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.mentor.data.ModuleDatabaseHelper;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseMigrationTest {
  private static final class Isolated extends ContextWrapper implements AutoCloseable {
    final File root;

    Isolated() {
      super(InstrumentationRegistry.getInstrumentation().getTargetContext());
      root = new File(getCacheDir(), "migration_fixture_" + System.nanoTime());
      assertTrue(root.mkdir());
    }

    @Override
    public Context getApplicationContext() {
      return this;
    }

    @Override
    public File getDatabasePath(String name) {
      return new File(root, name);
    }

    public void close() {
      for (File file : Objects.requireNonNull(root.listFiles())) assertTrue(file.delete());
      assertTrue(root.delete());
    }
  }

  private static final class Fixture extends ModuleDatabaseHelper {
    Fixture(Context c) {
      super(c, "fixture_legacy.db", null, 2);
    }

    protected Map<String, String> legacyTables() {
      return Collections.singletonMap("notes", "fixture_notes");
    }

    public void onCreate(SQLiteDatabase db) {
      db.execSQL(
          "CREATE TABLE fixture_notes(id INTEGER PRIMARY KEY,owner INTEGER,title TEXT,created_at"
              + " INTEGER,extra TEXT DEFAULT '')");
    }

    public void onUpgrade(SQLiteDatabase db, int old, int next) {}
  }

  private void seed(Isolated c, boolean conflicting) {
    try (SQLiteDatabase db =
        SQLiteDatabase.openOrCreateDatabase(c.getDatabasePath("fixture_legacy.db"), null)) {
      db.execSQL("CREATE TABLE notes(id INTEGER,owner INTEGER,title TEXT,created_at TEXT)");
      db.execSQL("INSERT INTO notes VALUES(1,42,'first','2024-01-01 00:00:00')");
      db.execSQL(
          "INSERT INTO notes VALUES(?,43,'second','2024-01-02 00:00:00')",
          new Object[] {conflicting ? 1 : 2});
      db.setVersion(1);
    }
  }

  private byte[] digest(File file) throws Exception {
    MessageDigest hash = MessageDigest.getInstance("SHA-256");
    try (InputStream stream = new FileInputStream(file)) {
      byte[] data = new byte[4096];
      int read;
      while ((read = stream.read(data)) != -1) hash.update(data, 0, read);
    }
    return hash.digest();
  }

  @Test
  public void importsOncePreservesSourceAndSubsequentEdits() throws Exception {
    try (Isolated c = new Isolated()) {
      seed(c, false);
      byte[] original = digest(c.getDatabasePath("fixture_legacy.db"));
      try (Fixture helper = new Fixture(c)) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try (Cursor row =
            db.rawQuery("SELECT owner,title,created_at FROM fixture_notes WHERE id=1", null)) {
          assertTrue(row.moveToFirst());
          assertEquals(42, row.getInt(0));
          assertEquals("first", row.getString(1));
          assertEquals(1704067200000L, row.getLong(2));
        }
        db.execSQL("UPDATE fixture_notes SET title='edited after import' WHERE id=1");
      }
      try (Fixture helper = new Fixture(c);
          Cursor row =
              helper
                  .getReadableDatabase()
                  .rawQuery("SELECT title FROM fixture_notes WHERE id=1", null)) {
        assertTrue(row.moveToFirst());
        assertEquals("edited after import", row.getString(0));
      }
      assertArrayEquals(original, digest(c.getDatabasePath("fixture_legacy.db")));
    }
  }

  @Test
  public void failedVerificationRollsBackWithoutMarkingImportComplete() {
    try (Isolated c = new Isolated()) {
      seed(c, true);
      try (Fixture helper = new Fixture(c)) {
        try {
          helper.getWritableDatabase();
          fail("Conflicting IDs must not silently overwrite source rows");
        } catch (SQLiteException expected) {
        }
      }
      try (SQLiteDatabase target =
              SQLiteDatabase.openDatabase(
                  c.getDatabasePath(ModuleDatabaseHelper.DATABASE_NAME).getPath(),
                  null,
                  SQLiteDatabase.OPEN_READONLY);
          Cursor row = target.rawQuery("SELECT COUNT(*) FROM mentor_schema_versions", null)) {
        assertTrue(row.moveToFirst());
        assertEquals(0, row.getInt(0));
      }
      try (SQLiteDatabase old =
              SQLiteDatabase.openDatabase(
                  c.getDatabasePath("fixture_legacy.db").getPath(),
                  null,
                  SQLiteDatabase.OPEN_READONLY);
          Cursor row = old.rawQuery("SELECT COUNT(*) FROM notes", null)) {
        assertTrue(row.moveToFirst());
        assertEquals(2, row.getInt(0));
      }
    }
  }

  @Test
  public void freshInstallAllFiveSchemasCoexistInOneFile() {
    try (Isolated c = new Isolated();
        com.example.asm_final.activities.database.DatabaseHelper accounts =
            new com.example.asm_final.activities.database.DatabaseHelper(c);
        com.example.mathmodule.databases.SqliteDbHelper math =
            new com.example.mathmodule.databases.SqliteDbHelper(c);
        com.example.historymodule.databases.SqliteDbHelper history =
            new com.example.historymodule.databases.SqliteDbHelper(c);
        com.example.modulelaptrinh.database.ProgrammingDbHelper programming =
            new com.example.modulelaptrinh.database.ProgrammingDbHelper(c);
        com.brightpath.science.data.local.ScienceDbHelper science =
            new com.brightpath.science.data.local.ScienceDbHelper(c)) {
      String path = accounts.getWritableDatabase().getPath();
      for (ModuleDatabaseHelper module : Arrays.asList(math, history, programming, science))
        assertEquals(path, module.getWritableDatabase().getPath());
      try (Cursor row =
          accounts
              .getReadableDatabase()
              .rawQuery("SELECT COUNT(*) FROM mentor_schema_versions", null)) {
        assertTrue(row.moveToFirst());
        assertEquals(5, row.getInt(0));
      }
      for (String table :
          Arrays.asList(
              "users", "math_notes", "history_notes", "programming_notes", "science_notes"))
        try (Cursor row =
            accounts.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + table, null)) {
          assertTrue(row.moveToFirst());
        }
    }
  }
}
