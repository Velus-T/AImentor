package com.example.mathmodule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.R;
import com.example.mathmodule.databases.QuizSet;

import java.util.List;

/*
 * Role: Adapter for Math, responsible for receiving model data and binding it to RecyclerView/ListView items.
 * Purpose: The Adapter separates list data from presentation logic, making the UI easier to update and reuse.
 */
public class QuizSetAdapter extends RecyclerView.Adapter<QuizSetAdapter.QuizViewHolder> {

    private List<QuizSet> quizSets;
    private OnQuizSetClickListener listener;

    public interface OnQuizSetClickListener {
        void onQuizSetClick(QuizSet quizSet);
    }

    /*
     * Responsibility: Initializes the `QuizSetAdapter` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public QuizSetAdapter(List<QuizSet> quizSets, OnQuizSetClickListener listener) {
        this.quizSets = quizSets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_set, parent, false);
        return new QuizViewHolder(view);
    }

    /*
     * Responsibility: Handles the Android callback or event `onBindViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizSet set = quizSets.get(position);
        holder.txtTitle.setText(set.getTitle());
        holder.txtCategory.setText(set.getCategory());
        holder.txtDifficulty.setText("Độ khó: " + set.getDifficulty());
        holder.txtEducationLevel.setText("Cấp học: " + set.getEducationLevel());
        holder.itemView.setOnClickListener(v -> listener.onQuizSetClick(set));
    }

    /*
     * Responsibility: Reads or loads data for `get item count` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    @Override
    public int getItemCount() {
        return quizSets.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtCategory, txtDifficulty, txtEducationLevel;

        /*
         * Responsibility: Performs the `quiz view holder` responsibility of `QuizSetAdapter` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txt_set_title);
            txtCategory = itemView.findViewById(R.id.txt_set_category);
            txtDifficulty = itemView.findViewById(R.id.txt_set_difficulty);
            txtEducationLevel = itemView.findViewById(R.id.txt_set_education_level);
        }
    }
}
