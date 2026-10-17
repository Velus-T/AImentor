package com.example.modulelaptrinh.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.mentor.ui.*;
import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.models.*;
import com.example.modulelaptrinh.utils.*;

/** Programming adapter for common quiz setup, answers, result and offline practice. */
public class QuizFragment extends Fragment {
  private static final String[] LEVELS = {"Trung học cơ sở", "Trung học phổ thông", "Đại học"};
  private QuizSetupView setup;
  private QuizFormView form;
  private ProgrammingDbHelper database;
  private ProgrammingQuizGenerator generator;
  private int userId, generation;
  private String activeTopic, activeLevel, activeScope = "Lập trình";
  private Bundle retained;

  public static QuizFragment newInstance(String subject, String topic, String level, int count) {
    QuizFragment fragment = new QuizFragment();
    Bundle args = new Bundle();
    args.putString("subject", subject);
    args.putString("topic", topic);
    args.putString("level", level);
    args.putInt("count", count);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
    return inflater.inflate(com.example.mentor.ui.R.layout.mentor_quiz_page, parent, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    super.onViewCreated(view, state);
    generation++;
    userId = ((MainActivity) requireActivity()).getCurrentUserId();
    database = new ProgrammingDbHelper(requireContext());
    generator = new ProgrammingQuizGenerator(requireContext());
    setup = view.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup);
    form = view.findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
    int[] counts = com.example.mentor.data.QuizPolicy.questionCounts();
    setup.configure(
        new String[] {getString(R.string.programming_app_name)},
        new String[] {
          getString(com.example.mentor.ui.R.string.mentor_quiz_middle),
          getString(com.example.mentor.ui.R.string.mentor_quiz_high),
          getString(com.example.mentor.ui.R.string.mentor_quiz_university)
        },
        counts);
    if (getArguments() != null) {
      setup.topic.setText(getArguments().getString("topic", ""));
      setup.count.setSelection(Math.max(0, Math.min(2, getArguments().getInt("count", 5) / 5 - 1)));
      for (int i = 0; i < LEVELS.length; i++)
        if (LEVELS[i].equals(getArguments().getString("level"))) setup.level.setSelection(i);
    }
    setup.create.setOnClickListener(v -> form.confirmReplacement(this::generate));
    setup.attachBank(
        userId,
        "programming",
        (set, cards) ->
            form.confirmReplacement(
                () -> {
                  activeTopic = set.topic;
                  activeLevel = set.level;
                  activeScope = set.scope;
                  form.bind(cards, this::save);
                  setup.notice(com.example.mentor.ui.R.string.mentor_quiz_bank_source);
                }));
    Bundle restore = state != null ? state : retained;
    if (restore != null) {
      setup.restore(restore.getBundle("setup"));
      activeTopic = restore.getString("topic");
      activeLevel = restore.getString("level");
      activeScope = restore.getString("scope", "Lập trình");
      form.restore(restore.getBundle("form"), this::save);
    }
    view.findViewById(com.example.mentor.ui.R.id.mentor_quiz_back).setOnClickListener(v -> leave());
    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(
            getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                leave();
              }
            });
  }

  private void leave() {
    form.confirmReplacement(
        () -> {
          if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
          else requireActivity().finish();
        });
  }

  private void generate() {
    final String topic = setup.topic().isEmpty() ? "Kiến thức lập trình" : setup.topic();
    final String level = LEVELS[setup.level.getSelectedItemPosition()];
    final QuizRequest request = new QuizRequest("Lập trình", level, topic, setup.count());
    final int requestOwner = userId;
    final int ownerGeneration = generation;
    final android.app.Activity activity = requireActivity();
    final ProgrammingQuizGenerator source = generator;
    setup.loading(true);
    new Thread(
            () -> {
              try {
                QuizResult quiz = source.generateQuiz(request, requestOwner);
                activity.runOnUiThread(
                    () -> {
                      if (!isAdded()
                          || getView() == null
                          || generation != ownerGeneration
                          || com.example.mentor.data.ActiveUser.get(activity) != requestOwner)
                        return;
                      setup.loading(false);
                      activeTopic = topic;
                      activeLevel = level;
                      activeScope = "Lập trình";
                      form.bind(QuizCards.from(quiz.getQuestions()), this::save);
                      setup.notice(com.example.mentor.ui.R.string.mentor_quiz_new_ai);
                    });
              } catch (Exception error) {
                activity.runOnUiThread(
                    () -> {
                      if (isAdded() && getView() != null && generation == ownerGeneration)
                        setup.error();
                    });
              }
            },
            "programming-quiz-generation")
        .start();
  }

  private void save(int correct, int count, int exp, QuizFormView.Completion completion) {
    long id =
        database.saveQuizResult(userId, activeScope, activeLevel, activeTopic, count, correct, exp);
    completion.complete(id > 0, database.getTotalExpAllSubjects(userId));
  }

  private Bundle snapshot() {
    Bundle out = new Bundle();
    out.putBundle("setup", setup.state());
    out.putBundle("form", form.state());
    out.putString("topic", activeTopic);
    out.putString("level", activeLevel);
    out.putString("scope", activeScope);
    return out;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    if (form != null) out.putAll(snapshot());
  }

  @Override
  public void onDestroyView() {
    generation++;
    if (form != null) retained = snapshot();
    if (database != null) database.close();
    setup = null;
    form = null;
    super.onDestroyView();
  }
}
