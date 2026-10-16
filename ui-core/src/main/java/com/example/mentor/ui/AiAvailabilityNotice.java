package com.example.mentor.ui;

import android.view.View;
import android.widget.TextView;

/** Reports configuration availability without claiming that a remote connection was verified. */
public final class AiAvailabilityNotice {
    private AiAvailabilityNotice() {}
    public static void bind(View root, boolean configured) {
        TextView notice = root.findViewById(R.id.mentor_ai_availability);
        notice.setVisibility(configured ? View.GONE : View.VISIBLE);
    }
    public static boolean canSend(View root, boolean configured) {
        if (configured) return true;
        TextView notice = root.findViewById(R.id.mentor_ai_availability);
        notice.setVisibility(View.VISIBLE);
        notice.announceForAccessibility(notice.getText());
        return false;
    }
}
