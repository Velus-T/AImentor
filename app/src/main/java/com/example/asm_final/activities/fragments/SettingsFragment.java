package com.example.asm_final.activities.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.asm_final.R;
import com.example.asm_final.utils.LocaleHelper;
import com.example.asm_final.utils.ReminderHelper;
import com.example.asm_final.utils.ThemeHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

/*
 * Role: Fragment for App Shell, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class SettingsFragment extends Fragment {

    private String username;
    private SwitchMaterial reminderSwitch;
    private boolean updatingReminderSwitch;
    private final androidx.activity.result.ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                    granted -> { if (isAdded()) {
                        applyReminderChoice(granted);
                        if (!granted) Toast.makeText(requireContext(), R.string.reminder_permission_needed, Toast.LENGTH_LONG).show();
                    } });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        if (getArguments() != null) {
            username = getArguments().getString("USERNAME");
        }

        setupEditProfile(view);
        setupReminders(view);
        setupThemeSwitch(view);
        setupLanguagePicker(view);
        setupShare(view);
        setupAbout(view);
        setupLogout(view);

        if (getArguments()!=null && getArguments().getBoolean("embedded",false)) view.findViewById(R.id.settings_heading).setVisibility(View.GONE);
        return view;
    }

    /*
     * Responsibility: Configures `setup edit profile` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupEditProfile(View view) {
        LinearLayout layoutEditProfile = view.findViewById(R.id.setting_edit_profile);
        layoutEditProfile.setOnClickListener(v -> {
            ((com.example.asm_final.activities.HomeActivity) requireActivity()).navigateTo(R.id.nav_edit_profile, true);
        });
    }

    /*
     * Responsibility: Configures `setup reminders` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupReminders(View view) {
        reminderSwitch = view.findViewById(R.id.switch_reminders);
        reminderSwitch.setChecked(ReminderHelper.isReminderEnabled(requireContext()));
        reminderSwitch.setOnCheckedChangeListener((button, enabled) -> {
            if (updatingReminderSwitch) return;
            if (enabled && Build.VERSION.SDK_INT >= 33 && requireContext().checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                updatingReminderSwitch = true;
                reminderSwitch.setChecked(false);
                updatingReminderSwitch = false;
                notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
            applyReminderChoice(enabled);
        });
    }

    private void applyReminderChoice(boolean requested) {
        boolean enabled = requested && androidx.core.app.NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        ReminderHelper.setReminderEnabled(requireContext(), enabled);
        if (reminderSwitch != null) {
            updatingReminderSwitch = true;
            reminderSwitch.setChecked(enabled);
            updatingReminderSwitch = false;
        }
        int message = requested && !enabled ? R.string.reminder_permission_needed
                : enabled ? R.string.reminder_enabled_time : R.string.reminder_disabled;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override public void onResume() {
        super.onResume();
        if (reminderSwitch != null) {
            updatingReminderSwitch = true;
            reminderSwitch.setChecked(ReminderHelper.isReminderEnabled(requireContext()));
            updatingReminderSwitch = false;
        }
    }

    @Override public void onDestroyView() {
        reminderSwitch = null;
        super.onDestroyView();
    }

    /*
     * Responsibility: Configures `setup theme switch` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupThemeSwitch(View view) {
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        // Read the stored value instead of relying on AppCompat's process-only state.
        switchDarkMode.setChecked(ThemeHelper.isDarkMode(requireContext()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeHelper.setDarkMode(requireContext(), isChecked));
    }

    /*
     * Responsibility: Configures `setup language picker` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupLanguagePicker(View view) {
        LinearLayout layoutLanguage = view.findViewById(R.id.setting_language);
        TextView txtCurrentLang = view.findViewById(R.id.txt_current_language);
        updateLanguageLabel(txtCurrentLang);

        layoutLanguage.setOnClickListener(v -> {
            String[] languages = {
                    getString(R.string.language_vietnamese),
                    getString(R.string.language_english)
            };
            String[] codes = {
                    LocaleHelper.LANGUAGE_VIETNAMESE,
                    LocaleHelper.LANGUAGE_ENGLISH
            };
            int selected = LocaleHelper.isEnglish(requireContext()) ? 1 : 0;

            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_language)
                    .setSingleChoiceItems(languages, selected, (dialog, which) -> {
                        String chosenLanguage = codes[which];
                        if (!chosenLanguage.equals(LocaleHelper.getLanguage(requireContext()))) {
                            LocaleHelper.setLocale(requireContext(), chosenLanguage);
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    /*
     * Responsibility: Updates or synchronizes `update language label` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateLanguageLabel(TextView textView) {
        textView.setText(LocaleHelper.isEnglish(requireContext())
                ? R.string.language_english
                : R.string.language_vietnamese);
    }

    /*
     * Responsibility: Configures `setup share` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupShare(View view) {
        LinearLayout layoutShare = view.findViewById(R.id.setting_share);
        layoutShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_share));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text));
            startActivity(Intent.createChooser(
                    intent,
                    getString(R.string.settings_share_chooser)));
        });
    }

    /*
     * Responsibility: Configures `setup about` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupAbout(View view) {
        LinearLayout layoutAbout = view.findViewById(R.id.setting_about);
        layoutAbout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_about)
                .setMessage(R.string.settings_about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }

    /*
     * Responsibility: Configures `setup logout` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupLogout(View view) {
        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            ((com.example.asm_final.activities.HomeActivity) requireActivity()).logout();
            Toast.makeText(requireContext(), R.string.menu_logout, Toast.LENGTH_SHORT).show();
        });
    }
}
