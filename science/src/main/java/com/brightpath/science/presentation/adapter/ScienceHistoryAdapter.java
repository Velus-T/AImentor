package com.brightpath.science.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.util.ScienceDateFormatter;

import java.util.ArrayList;
import java.util.List;

public final class ScienceHistoryAdapter extends RecyclerView.Adapter<ScienceHistoryAdapter.Holder> {
    public interface Listener {     /*
 * Responsibility: Performs the `onHistorySelected` operation for Listener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onHistorySelected(ScienceHistory item); }

    private final List<ScienceHistory> items = new ArrayList<>();
    private final Listener listener;
    private final ScienceDateFormatter dateFormatter = new ScienceDateFormatter();

        /*
 * Responsibility: Initializes ScienceHistoryAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

        /*
 * Responsibility: Performs the adapter operation `submitList` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
public void submitList(List<ScienceHistory> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

        /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_science_record, parent, false));
    }

        /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ScienceHistory item = items.get(position);
        holder.subject.setText(item.getSubject().getDisplayName() + " • " + item.getEducationLevel());
        holder.title.setText(item.getQuestion());
        holder.preview.setText(item.getAnswer());
        holder.date.setText(dateFormatter.format(item.getCreatedAt()));
        holder.itemView.setOnClickListener(view -> listener.onHistorySelected(item));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView subject;
        final TextView title;
        final TextView preview;
        final TextView date;

                /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.tvRecordSubject);
            title = itemView.findViewById(R.id.tvRecordTitle);
            preview = itemView.findViewById(R.id.tvRecordPreview);
            date = itemView.findViewById(R.id.tvRecordDate);
        }
    }
}
