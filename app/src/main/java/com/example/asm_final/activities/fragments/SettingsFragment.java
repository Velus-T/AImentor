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

        return view;
    }

    /*
     * Responsibility: Configures `setup edit profile` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupEditProfile(View view) {
        LinearLayout layoutEditProfile = view.findViewById(R.id.setting_edit_profile);
        layoutEditProfile.setOnClickListener(v -> {
            ((com.example.asm_final.activities.HomeActivity) requireActivity()).navigateTo(R.id.nav_profile, true);
        });
    }

    /*
     * Responsibility: Configures `setup reminders` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupReminders(View view) {
        SwitchMaterial switchReminders = view.findViewById(R.id.switch_reminders);
        switchReminders.setChecked(ReminderHelper.isReminderEnabled(requireContext()));
        
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
            
            ReminderHelper.setReminderEnabled(requireContext(), isChecked);
            
            int status = isChecked
                    ? R.string.settings_status_on
                    : R.string.settings_status_off;
            String message = getString(
                    R.string.settings_option_status,
                    getString(R.string.settings_reminders),
                    getString(status));
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
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
