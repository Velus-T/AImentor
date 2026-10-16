package com.brightpath.science.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.domain.model.ChemicalElement;

import java.util.ArrayList;
import java.util.List;

public final class PeriodicElementAdapter extends RecyclerView.Adapter<PeriodicElementAdapter.Holder> {
    public interface Listener {     /*
 * Responsibility: Performs the `onElementSelected` operation for Listener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onElementSelected(ChemicalElement element); }

    private final List<ChemicalElement> elements = new ArrayList<>();
    private final Listener listener;

        /*
 * Responsibility: Initializes PeriodicElementAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public PeriodicElementAdapter(Listener listener) {
        this.listener = listener;
    }

        /*
 * Responsibility: Performs the adapter operation `submitList` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
public void submitList(List<ChemicalElement> newElements) {
        elements.clear();
        if (newElements != null) elements.addAll(newElements);
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
                .inflate(R.layout.item_science_chemical_element, parent, false));
    }

        /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChemicalElement element = elements.get(position);
        holder.number.setText(String.valueOf(element.getAtomicNumber()));
        holder.symbol.setText(element.getSymbol());
        holder.name.setText(element.getName());
        holder.itemView.setOnClickListener(view -> listener.onElementSelected(element));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public int getItemCount() { return elements.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView symbol;
        final TextView name;

                /*
 * Responsibility: Initializes Holder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
Holder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.tvElementNumber);
            symbol = itemView.findViewById(R.id.tvElementSymbol);
            name = itemView.findViewById(R.id.tvElementName);
        }
    }
}
