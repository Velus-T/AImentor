package com.brightpath.science.presentation.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ScienceNote;
import com.brightpath.science.domain.repository.ScienceNoteRepository;
import com.brightpath.science.presentation.PeriodicTableActivity;
import com.brightpath.science.presentation.adapter.ScienceModernAdapters;
import com.brightpath.science.util.ScienceUserSession;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Formula/reference screen plus personal Science notes.
 *
 * <p>review MAP — NOTE FLOW:</p>
 * <pre>
 * Add/Edit button
 *   -> showNoteDialog()
 *   -> ScienceNoteRepository.insert()/update()
 *   -> SQLiteScienceNoteRepository
 *   -> science_notes table in science_ai.db
 *
 * Reopen Formula tab
 *   -> refreshNotes()
 *   -> ScienceNoteRepository.getAll(userId)
 *   -> render note cards
 * </pre>
 *
 * <p>The Fragment depends on the domain {@link ScienceNoteRepository} interface. The concrete
 * SQLite implementation is created centrally by {@link ScienceModule}. That is the Science
 * module's Dependency Inversion pattern in a small, easy-to-demonstrate flow.</p>
 */
public final class ScienceFormulaFragment extends Fragment {
    private RecyclerView formulaRecycler;
    private LinearLayout notesContainer;
    private TextView emptyNotes;
    private ScienceNoteRepository noteRepository;
    private int userId;

    private final List<ScienceModernAdapters.FormulaItem> formulas = new ArrayList<>();

        /*
 * Responsibility: Initializes ScienceFormulaFragment and receives or prepares the dependencies/state required by this Science UI tab/fragment.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceFormulaFragment() {
        super(R.layout.fragment_science_formula);
    }

        /*
 * Responsibility: Binds the Fragment views after layout creation and configures adapters, listeners, and initial data.
 * Purpose: Connects the XML layout to Fragment behavior while keeping setup in the correct lifecycle stage.
 */
@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = new ScienceUserSession(requireContext()).getUserId();
        noteRepository = ScienceModule.get(requireContext()).noteRepository();

        bindViews(view);
        bindActions(view);
        initFormulaList();

        String allLabel = getString(R.string.science_all);
        showFormulas(allLabel);
        bindFilterChip(view, R.id.chipFormulaAll, allLabel);
        bindFilterChip(view, R.id.chipFormulaPhysics, getString(R.string.science_subj_physics));
        bindFilterChip(view, R.id.chipFormulaChemistry, getString(R.string.science_subj_chemistry));
        bindFilterChip(view, R.id.chipFormulaBiology, getString(R.string.science_subj_biology));

        refreshNotes();
        editorState.restore(savedInstanceState == null ? null : savedInstanceState.getBundle("editor"));
        if (editorState.hasDraft()) {
            ScienceNote existing = null;
            for (ScienceNote note : noteRepository.getAll(userId)) if (note.getId() == editorState.recordId()) existing = note;
            showNoteDialog(existing);
        }
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    public void onResume() {
        super.onResume();
        if (notesContainer != null) {
            refreshNotes();
        }
    }

    // region Screen setup

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindViews`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindViews(View view) {
        notesContainer = view.findViewById(R.id.containerScienceNotes);
        emptyNotes = view.findViewById(R.id.textScienceNotesEmpty);
        formulaRecycler = view.findViewById(R.id.recyclerScienceFormulas);
        formulaRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindActions`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindActions(View view) {
        view.findViewById(R.id.buttonScienceFormulaBack)
                .setOnClickListener(v -> requireActivity().finish());

        view.findViewById(R.id.buttonScienceFormulaAction)
                .setOnClickListener(v -> startActivity(
                        new Intent(requireContext(), PeriodicTableActivity.class)));

        view.findViewById(R.id.buttonAddScienceFormula)
                .setOnClickListener(v -> showNoteDialog(null));
    }

        /*
 * Responsibility: Configures the dependencies, UI elements, listeners, or initial state required by `initFormulaList`.
 * Purpose: Keeps setup logic grouped and makes the main lifecycle/business method easier to read.
 */
private void initFormulaList() {
        formulas.clear();
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_newton),
                "F = m × a",
                getString(R.string.science_subj_physics)));
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_kinetic),
                "Wđ = ½mv²",
                getString(R.string.science_subj_physics)));
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_electric),
                "A = U × I × t",
                getString(R.string.science_subj_physics)));
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_moles),
                "n = m / M",
                getString(R.string.science_subj_chemistry)));
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_concentration),
                "Cₘ = n / V",
                getString(R.string.science_subj_chemistry)));
        formulas.add(new ScienceModernAdapters.FormulaItem(
                getString(R.string.science_formula_photosynthesis),
                "6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂",
                getString(R.string.science_subj_biology)));
    }

    // endregion

    // region Personal notes CRUD

        /*
 * Responsibility: Prepares and presents the data/state handled by `showNoteDialog` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private final com.example.mentor.ui.NoteEditorState editorState = new com.example.mentor.ui.NoteEditorState();

private void showNoteDialog(@Nullable ScienceNote note) {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding / 2, padding, 0);

        EditText titleInput = new EditText(requireContext());
        titleInput.setHint(R.string.science_note_title_hint);

        EditText contentInput = new EditText(requireContext());
        contentInput.setHint(R.string.science_note_content_hint);
        contentInput.setMinLines(4);
        contentInput.setGravity(Gravity.TOP);

        Spinner subjectSpinner = new Spinner(requireContext());
        String[] subjects = {
                getString(R.string.science_subj_physics),
                getString(R.string.science_subj_chemistry),
                getString(R.string.science_subj_biology),
                getString(R.string.science_subj_others)
        };
        subjectSpinner.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                subjects));

        if (note != null) {
            titleInput.setText(note.getTitle());
            contentInput.setText(note.getContent());
            subjectSpinner.setSelection(com.brightpath.science.util.ScienceNoteSubject.index(note.getSubject()));
        }

        form.addView(titleInput);
        form.addView(subjectSpinner);
        form.addView(contentInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(note == null ? R.string.science_add_note : R.string.science_edit_note)
                .setView(form)
                .setNegativeButton(R.string.science_cancel, null)
                .setPositiveButton(R.string.science_save, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> saveNoteFromDialog(
                        dialog,
                        note,
                        titleInput,
                        contentInput,
                        subjectSpinner)));

        editorState.bind(dialog, note == null ? 0 : note.getId(), titleInput, contentInput,
                () -> com.brightpath.science.util.ScienceNoteSubject.code(subjectSpinner.getSelectedItemPosition()),
                category -> subjectSpinner.setSelection(com.brightpath.science.util.ScienceNoteSubject.index(category)));
        dialog.show();
    }

        /*
 * Responsibility: Persists the data supplied to `saveNoteFromDialog` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
private void saveNoteFromDialog(AlertDialog dialog,
                                    @Nullable ScienceNote note,
                                    EditText titleInput,
                                    EditText contentInput,
                                    Spinner subjectSpinner) {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.science_note_error_title));
            return;
        }
        if (content.isEmpty()) {
            contentInput.setError(getString(R.string.science_note_error_content));
            return;
        }

        String subject = com.brightpath.science.util.ScienceNoteSubject.code(subjectSpinner.getSelectedItemPosition());
        boolean saved;
        try {
            saved = note == null ? noteRepository.insert(userId, title, content, subject) > 0
                    : noteRepository.update(note.getId(), userId, title, content, subject);
        } catch (RuntimeException error) { saved = false; }

        if (!saved) {
            Toast.makeText(requireContext(), R.string.science_note_save_error, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        dialog.dismiss();
        refreshNotes();
        Toast.makeText(
                requireContext(),
                note == null ? R.string.science_note_added : R.string.science_note_updated,
                Toast.LENGTH_SHORT)
                .show();
    }

        /*
 * Responsibility: Handles the `refreshNotes` UI interaction/state operation for ScienceFormulaFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void refreshNotes() {
        List<ScienceNote> notes;
        try {
            notes = noteRepository.getAll(userId);
        } catch (Exception error) {
            Toast.makeText(requireContext(), com.example.mentor.ui.R.string.mentor_data_read_error, Toast.LENGTH_LONG).show();
            return;
        }

        notesContainer.removeAllViews();
        emptyNotes.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);

        for (ScienceNote note : notes) {
            notesContainer.addView(createNoteCard(note));
        }
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `createNoteCard`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private View createNoteCard(ScienceNote note) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = Math.round(10 * getResources().getDisplayMetrics().density);
        card.setLayoutParams(cardParams);
        card.setRadius(16 * getResources().getDisplayMetrics().density);
        card.setCardElevation(2 * getResources().getDisplayMetrics().density);
        card.setContentPadding(18, 14, 18, 14);

        LinearLayout body = new LinearLayout(requireContext());
        body.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(requireContext());
        int[] labels = {R.string.science_subj_physics, R.string.science_subj_chemistry,
                R.string.science_subj_biology, R.string.science_subj_others};
        title.setText(note.getTitle() + " • " + getString(labels[com.brightpath.science.util.ScienceNoteSubject.index(note.getSubject())]));
        title.setTextSize(16);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.science_text_primary));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView content = new TextView(requireContext());
        content.setText(note.getContent());
        content.setTextSize(14);
        content.setTextColor(ContextCompat.getColor(requireContext(), R.color.science_text_secondary));
        content.setPadding(0, 8, 0, 8);

        TextView time = new TextView(requireContext());
        time.setText(getString(
                R.string.science_note_updated_at,
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(note.getUpdatedAt()))));
        time.setTextSize(14);
        time.setTextColor(ContextCompat.getColor(requireContext(), R.color.science_text_secondary));

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setGravity(Gravity.END);
        TextView edit = actionText(getString(R.string.science_edit));
        TextView delete = actionText(getString(R.string.science_delete));
        edit.setOnClickListener(v -> showNoteDialog(note));
        delete.setOnClickListener(v -> confirmDelete(note));
        actions.addView(edit);
        actions.addView(delete);

        body.addView(title);
        body.addView(content);
        body.addView(time);
        body.addView(actions);
        card.addView(body);
        return card;
    }

        /*
 * Responsibility: Handles the `confirmDelete` UI interaction/state operation for ScienceFormulaFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void confirmDelete(ScienceNote note) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.science_note_delete_title)
                .setMessage(getString(R.string.science_note_delete_msg, note.getTitle()))
                .setNegativeButton(R.string.science_cancel, null)
                .setPositiveButton(R.string.science_delete, (dialog, which) -> {
                    boolean deleted;
                    try { deleted = noteRepository.delete(note.getId(), userId); }
                    catch (RuntimeException error) { deleted = false; }
                    if (deleted) {
                        refreshNotes();
                        Toast.makeText(
                                requireContext(),
                                R.string.science_note_delete_success,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }

        /*
 * Responsibility: Changes the active selection/state handled by `selectSpinnerValue`.
 * Purpose: Keeps the UI and subject-specific Science context synchronized.
 */
private void selectSpinnerValue(Spinner spinner, String[] values, String selectedValue) {
        for (int index = 0; index < values.length; index++) {
            if (values[index].equals(selectedValue)) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    // endregion

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out); out.putBundle("editor", editorState.save());
    }

    @Override public void onDestroyView() {
        editorState.close(); notesContainer = null; emptyNotes = null; formulaRecycler = null;
        super.onDestroyView();
    }

    // region Formula filtering

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindFilterChip`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindFilterChip(View root, int id, String category) {
        TextView chip = root.findViewById(id);
        chip.setOnClickListener(v -> {
            resetFilterChips(root);
            chip.setBackgroundResource(R.drawable.science_chip_orange);
            chip.setTextColor(Color.WHITE);
            showFormulas(category);
        });
    }

        /*
 * Responsibility: Handles the `resetFilterChips` UI interaction/state operation for ScienceFormulaFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void resetFilterChips(View root) {
        int[] ids = {
                R.id.chipFormulaAll,
                R.id.chipFormulaPhysics,
                R.id.chipFormulaChemistry,
                R.id.chipFormulaBiology
        };

        for (int id : ids) {
            TextView chip = root.findViewById(id);
            chip.setBackgroundResource(R.drawable.science_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(
                    requireContext(),
                    R.color.science_text_secondary));
        }
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showFormulas` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showFormulas(String category) {
        List<ScienceModernAdapters.FormulaItem> filtered = new ArrayList<>();
        String allLabel = getString(R.string.science_all);

        for (ScienceModernAdapters.FormulaItem item : formulas) {
            if (allLabel.equals(category) || category.equals(item.category)) {
                filtered.add(item);
            }
        }

        formulaRecycler.setAdapter(new ScienceModernAdapters.FormulaAdapter(filtered));
    }

        /*
 * Responsibility: Handles the `actionText` UI interaction/state operation for ScienceFormulaFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private TextView actionText(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.science_formula_color));
        view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        view.setPadding(24, 12, 8, 8);
        return view;
    }

    // endregion
}
