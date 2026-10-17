package com.example.modulelaptrinh;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;

/** Account-scoped note editor. Back retains autosave, but never exits on a failed write. */
public class NoteEditorActivity extends AppCompatActivity {
  private EditText titleInput, contentInput;
  private TextView status;
  private ProgrammingDbHelper database;
  private int noteId = -1, userId;
  private String originalTitle = "", originalContent = "";

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_note_editor);
    database = new ProgrammingDbHelper(this);
    userId = Math.max(0, getIntent().getIntExtra(MainActivity.EXTRA_USER_ID, 0));
    noteId = getIntent().getIntExtra("NOTE_ID", -1);
    titleInput = findViewById(R.id.et_note_title);
    contentInput = findViewById(R.id.et_note_content);
    status = findViewById(R.id.note_save_status);
    originalTitle = value(getIntent().getStringExtra("NOTE_TITLE"));
    originalContent = value(getIntent().getStringExtra("NOTE_CONTENT"));
    if (state == null) {
      titleInput.setText(originalTitle);
      contentInput.setText(originalContent);
    }
    ((TextView) findViewById(R.id.tv_toolbar_title))
        .setText(noteId == -1 ? R.string.note_new_title : R.string.note_edit_title);
    findViewById(R.id.btn_save)
        .setOnClickListener(
            v -> {
              if (persist()) {
                Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
              }
            });
    findViewById(R.id.iv_back).setOnClickListener(v -> leave());
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                leave();
              }
            });
  }

  private static String value(String text) {
    return text == null ? "" : text;
  }

  private void leave() {
    String title = titleInput.getText().toString().trim();
    String content = contentInput.getText().toString().trim();
    if ((noteId == -1 && title.isEmpty() && content.isEmpty())
        || (title.equals(originalTitle.trim()) && content.equals(originalContent.trim()))) {
      finish();
    } else if (persist()) {
      setResult(RESULT_OK);
      finish();
    } else {
      new AlertDialog.Builder(this)
          .setTitle(R.string.note_unsaved_title)
          .setMessage(R.string.note_unsaved_message)
          .setPositiveButton(R.string.note_keep_editing, null)
          .setNegativeButton(R.string.note_discard_changes, (d, w) -> finish())
          .show();
    }
  }

  private boolean persist() {
    String title = titleInput.getText().toString().trim();
    String content = contentInput.getText().toString().trim();
    if (title.isEmpty() && content.isEmpty()) {
      status.setText(R.string.note_empty_warning);
      return false;
    }
    boolean saved;
    try {
      if (noteId == -1) {
        long inserted = database.insertNote(userId, title, content);
        saved = inserted > 0;
        if (saved) noteId = (int) inserted;
      } else saved = database.updateNote(userId, noteId, title, content);
    } catch (android.database.SQLException error) {
      saved = false;
    }
    if (!saved) {
      status.setText(R.string.note_write_failed);
      return false;
    }
    originalTitle = title;
    originalContent = content;
    status.setText("");
    return true;
  }

  @Override
  protected void onDestroy() {
    if (database != null) database.close();
    super.onDestroy();
  }
}
