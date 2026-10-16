package com.example.modulelaptrinh.util;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.modulelaptrinh.R;
import java.util.function.BooleanSupplier;

/** Confirms destructive local actions and reports their actual outcome. */
public final class ConfirmDeletion {
    private ConfirmDeletion() {}
    public static void show(Context context, int message, BooleanSupplier deletion, Runnable refresh) {
        new AlertDialog.Builder(context).setTitle(R.string.program_delete_title).setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.program_delete_action, (dialog, which) -> {
                boolean deleted;
                try { deleted = deletion.getAsBoolean(); }
                catch (android.database.SQLException error) { deleted = false; }
                Toast.makeText(context, deleted ? R.string.program_delete_done : R.string.program_delete_failed,
                        Toast.LENGTH_SHORT).show();
                refresh.run();
            }).show();
    }
}
