package com.example.aimentor.science.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimentor.R;
import com.example.aimentor.science.domain.model.ScienceHistory;
import com.example.aimentor.science.util.ScienceDateFormatter;

import java.util.ArrayList;
import java.util.List;

public final class ScienceHistoryAdapter extends RecyclerView.Adapter<ScienceHistoryAdapter.Holder> {
    public interface Listener { void onHistorySelected(ScienceHistory item); }

    private final List<ScienceHistory> items = new ArrayList<>();
    private final Listener listener;
    private final ScienceDateFormatter dateFormatter = new ScienceDateFormatter();

    public ScienceHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<ScienceHistory> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_science_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ScienceHistory item = items.get(position);
        holder.subject.setText(item.getSubject().getDisplayName() + " • " + item.getEducationLevel());
        holder.title.setText(item.getQuestion());
        holder.preview.setText(item.getAnswer());
        holder.date.setText(dateFormatter.format(item.getCreatedAt()));
        holder.itemView.setOnClickListener(view -> listener.onHistorySelected(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView subject;
        final TextView title;
        final TextView preview;
        final TextView date;

        Holder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.tvRecordSubject);
            title = itemView.findViewById(R.id.tvRecordTitle);
            preview = itemView.findViewById(R.id.tvRecordPreview);
            date = itemView.findViewById(R.id.tvRecordDate);
        }
    }
}
