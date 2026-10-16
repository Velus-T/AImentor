package com.example.asm_final.utils;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.asm_final.R;
import java.util.List;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
    private final List<RecentActivity> activities;

    public RecentActivityAdapter(List<RecentActivity> activities) {
        this.activities = activities;
    }

    /*
     * Responsibility: Handles the Android callback or event `onCreateViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    /*
     * Responsibility: Handles the Android callback or event `onBindViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDesc.setText(activity.getDescription());
        holder.tvTime.setText(activity.getTimestamp() <= 0 ? holder.itemView.getContext().getString(R.string.activity_time_unknown) : DateUtils.getRelativeTimeSpanString(activity.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

        // Set Icon and Background based on subject
        int iconRes = R.drawable.ic_cat_math;
        int bgColorRes = R.color.cat_math_bg;

        switch (activity.getSubject()) {
            case MATH:
                iconRes = R.drawable.ic_cat_math;
                bgColorRes = R.color.cat_math_bg;
                break;
            case SCIENCE:
                iconRes = R.drawable.ic_cat_science;
                bgColorRes = R.color.cat_science_bg;
                break;
            case HISTORY:
                iconRes = R.drawable.ic_cat_history;
                bgColorRes = R.color.cat_history_bg;
                break;
            case PROGRAMMING:
                iconRes = R.drawable.ic_cat_programming;
                bgColorRes = R.color.cat_it_bg;
                break;
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.cvIconBg.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), bgColorRes));
    }

    /*
     * Responsibility: Reads or loads data for `get item count` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDesc, tvTime;
        CardView cvIconBg;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDesc = itemView.findViewById(R.id.tv_activity_desc);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
            cvIconBg = itemView.findViewById(R.id.cv_activity_icon_bg);
        }
    }
}
