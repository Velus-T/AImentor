package com.example.mentor.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.app.Dialog;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Small dialog state holder; persistence remains the subject module's responsibility. */
public final class NoteEditorState {
  private Dialog dialog;
  private EditText title, body;
  private Supplier<String> category;
  private long id;
  private Bundle pending;
  public void restore(Bundle state) { pending = state; }
  public boolean hasDraft() { return pending != null; }
  public long recordId() { return pending == null ? 0 : pending.getLong("id"); }
  public void bind(Dialog dialog, long id, EditText title, EditText body,
      Supplier<String> category, Consumer<String> restoreCategory) {
    this.dialog=dialog; this.id=id; this.title=title; this.body=body; this.category=category;
    if(pending!=null) {
      title.setText(pending.getString("title","")); body.setText(pending.getString("body",""));
      restoreCategory.accept(pending.getString("category","")); pending=null;
    }
    dialog.setOnDismissListener(ignored -> { if(this.dialog==dialog) clear(); });
  }
  public Bundle save() {
    if(dialog==null || !dialog.isShowing()) return pending;
    Bundle state=new Bundle(); state.putLong("id",id);
    state.putString("title",title.getText().toString()); state.putString("body",body.getText().toString());
    state.putString("category",category.get()); return state;
  }
  private void clear() { dialog=null; title=null; body=null; category=null; }
  public void close() { if(dialog!=null) dialog.dismiss(); clear(); }
}
