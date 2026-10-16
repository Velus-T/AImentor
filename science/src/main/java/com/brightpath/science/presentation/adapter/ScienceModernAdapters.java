package com.brightpath.science.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.util.ScienceDateFormatter;

import java.util.ArrayList;
import java.util.List;

public final class ScienceModernAdapters {
        /*
 * Responsibility: Initializes ScienceModernAdapters and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ScienceModernAdapters() { }

    public interface HistoryClickListener {     /*
 * Responsibility: Performs the `onClick` operation for HistoryClickListener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onClick(ScienceHistory item); }
    public interface QuizClickListener {     /*
 * Responsibility: Performs the `onClick` operation for QuizClickListener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onClick(ScienceSubject subject); }

    public static final class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
        private final List<ScienceHistory> items;
        private final boolean recentStyle;
        private final HistoryClickListener listener;
        private final ScienceDateFormatter formatter = new ScienceDateFormatter();

                /*
 * Responsibility: Initializes HistoryAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public HistoryAdapter(List<ScienceHistory> items, boolean recentStyle, HistoryClickListener listener) {
            this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
            this.recentStyle = recentStyle;
            this.listener = listener;
        }

                /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = recentStyle ? R.layout.item_science_recent_question_modern : R.layout.item_science_history_modern;
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), recentStyle);
        }

                /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            ScienceHistory item = items.get(position);
            holder.title.setText(item.getQuestion());
            holder.preview.setText(item.getAnswer());
            holder.time.setText(formatter.format(item.getCreatedAt()));
            holder.icon.setImageResource(iconFor(item.getSubject()));
            holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(item); });
        }

                /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override public int getItemCount() { return items.size(); }

                /*
 * Responsibility: Performs the adapter operation `iconFor` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
private int iconFor(ScienceSubject subject) {
            if (subject == ScienceSubject.CHEMISTRY) return R.drawable.ic_science_chemistry;
            if (subject == ScienceSubject.BIOLOGY) return R.drawable.ic_science_biology;
            return R.drawable.ic_science_physics;
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;
            final TextView preview;
            final TextView time;
                        /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(View itemView, boolean recent) {
                super(itemView);
                icon = itemView.findViewById(recent ? R.id.imageRecentQuestionSubject : R.id.imageHistorySubject);
                title = itemView.findViewById(recent ? R.id.textRecentQuestionTitle : R.id.textHistoryTitle);
                preview = itemView.findViewById(recent ? R.id.textRecentQuestionPreview : R.id.textHistoryPreview);
                time = itemView.findViewById(recent ? R.id.textRecentQuestionTime : R.id.textHistoryTime);
            }
        }
    }

    public static final class FormulaItem {
        public final String title;
        public final String expression;
        public final String category;
                /*
 * Responsibility: Initializes FormulaItem and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public FormulaItem(String title, String expression, String category) {
            this.title = title;
            this.expression = expression;
            this.category = category;
        }
    }

    public static final class FormulaAdapter extends RecyclerView.Adapter<FormulaAdapter.Holder> {
        private final List<FormulaItem> items;
        private final List<Boolean> saved;
                /*
 * Responsibility: Initializes FormulaAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public FormulaAdapter(List<FormulaItem> items) {
            this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
            this.saved = new ArrayList<>();
            for (int i = 0; i < this.items.size(); i++) saved.add(false);
        }

                /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_science_formula_modern, parent, false));
        }

                /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            FormulaItem item = items.get(position);
            holder.title.setText(item.title);
            holder.expression.setText(item.expression);
            holder.category.setText(item.category);
            holder.bookmark.setAlpha(saved.get(position) ? 1f : 0.45f);
            holder.bookmark.setOnClickListener(v -> {
                boolean newValue = !saved.get(holder.getBindingAdapterPosition());
                saved.set(holder.getBindingAdapterPosition(), newValue);
                holder.bookmark.setAlpha(newValue ? 1f : 0.45f);
            });
            holder.more.setOnClickListener(v -> { });
        }

                /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override public int getItemCount() { return items.size(); }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView title, expression, category;
            final ImageButton bookmark, more;
                        /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(View view) {
                super(view);
                title = view.findViewById(R.id.textFormulaTitle);
                expression = view.findViewById(R.id.textFormulaExpression);
                category = view.findViewById(R.id.textFormulaCategory);
                bookmark = view.findViewById(R.id.buttonFormulaBookmark);
                more = view.findViewById(R.id.buttonFormulaMore);
            }
        }
    }

    public static final class QuizTopic {
        public final String title;
        public final ScienceSubject subject;
        public final int attemptCount;
        public final int totalQuestions;
        public final int correctAnswers;

                /*
 * Responsibility: Initializes QuizTopic and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizTopic(String title, ScienceSubject subject, int attemptCount,
                         int totalQuestions, int correctAnswers) {
            this.title = title;
            this.subject = subject;
            this.attemptCount = Math.max(0, attemptCount);
            this.totalQuestions = Math.max(0, totalQuestions);
            this.correctAnswers = Math.max(0, Math.min(correctAnswers, this.totalQuestions));
        }

                /*
 * Responsibility: Checks the condition represented by `hasData` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean hasData() {
            return attemptCount > 0 && totalQuestions > 0;
        }

                /*
 * Responsibility: Retrieves the data/state required by `getAccuracyPercent` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getAccuracyPercent() {
            if (!hasData()) return 0;
            return (int) Math.round(correctAnswers * 100.0 / totalQuestions);
        }
    }

    public static final class QuizTopicAdapter extends RecyclerView.Adapter<QuizTopicAdapter.Holder> {
        private final List<QuizTopic> items;
        private final QuizClickListener listener;
                /*
 * Responsibility: Initializes QuizTopicAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizTopicAdapter(List<QuizTopic> items, QuizClickListener listener) {
            this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
            this.listener = listener;
        }

                /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_science_quiz_topic, parent, false));
        }

                /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            QuizTopic item = items.get(position);
            holder.title.setText(item.title);

            if (item.hasData()) {
                int accuracy = item.getAccuracyPercent();
                holder.count.setText(holder.itemView.getContext().getString(R.string.science_quiz_attempts_format, item.attemptCount, item.totalQuestions)
                        + " • " + holder.itemView.getContext().getString(R.string.science_quiz_score_format_alt, item.correctAnswers, item.totalQuestions));
                holder.progress.setProgress(accuracy);
                holder.percent.setText(accuracy + "%");
            } else {
                holder.count.setText(R.string.science_quiz_not_started);
                holder.progress.setProgress(0);
                holder.percent.setText("--");
            }

            holder.icon.setImageResource(item.subject == ScienceSubject.CHEMISTRY
                    ? R.drawable.ic_science_chemistry
                    : item.subject == ScienceSubject.BIOLOGY
                    ? R.drawable.ic_science_biology
                    : R.drawable.ic_science_physics);

            holder.itemView.setContentDescription(item.hasData()
                    ? holder.itemView.getContext().getString(R.string.science_quiz_desc_accuracy, item.title, item.getAccuracyPercent())
                    : holder.itemView.getContext().getString(R.string.science_quiz_desc_no_data, item.title));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item.subject);
            });
        }

                /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override public int getItemCount() { return items.size(); }

        static final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, count, percent;
            final ProgressBar progress;
                        /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(View view) {
                super(view);
                icon = view.findViewById(R.id.imageQuizTopic);
                title = view.findViewById(R.id.textQuizTopicTitle);
                count = view.findViewById(R.id.textQuizTopicCount);
                progress = view.findViewById(R.id.progressQuizTopic);
                percent = view.findViewById(R.id.textQuizTopicPercent);
            }
        }
    }

    public static final class RecentQuiz {
        public final String title, count, score, percent;
                /*
 * Responsibility: Initializes RecentQuiz and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public RecentQuiz(String title, String count, String score, String percent) {
            this.title = title; this.count = count; this.score = score; this.percent = percent;
        }
    }

    public static final class RecentQuizAdapter extends RecyclerView.Adapter<RecentQuizAdapter.Holder> {
        private final List<RecentQuiz> items;
                /*
 * Responsibility: Initializes RecentQuizAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public RecentQuizAdapter(List<RecentQuiz> items) { this.items = items == null ? new ArrayList<>() : new ArrayList<>(items); }
                /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_science_recent_quiz, parent, false));
        }
                /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            RecentQuiz item = items.get(position);
            holder.title.setText(item.title);
            holder.count.setText(item.count);
            holder.score.setText(item.score);
            holder.percent.setText(item.percent);
        }
                /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override public int getItemCount() { return items.size(); }
        static final class Holder extends RecyclerView.ViewHolder {
            final TextView title, count, score, percent;
                        /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(View view) {
                super(view);
                title = view.findViewById(R.id.textRecentQuizTitle);
                count = view.findViewById(R.id.textRecentQuizCount);
                score = view.findViewById(R.id.textRecentQuizScore);
                percent = view.findViewById(R.id.textRecentQuizPercent);
            }
        }
    }
}
