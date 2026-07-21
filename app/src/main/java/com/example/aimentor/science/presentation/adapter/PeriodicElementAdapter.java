package com.example.aimentor.science.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimentor.R;
import com.example.aimentor.science.domain.model.ChemicalElement;

import java.util.ArrayList;
import java.util.List;

public final class PeriodicElementAdapter extends RecyclerView.Adapter<PeriodicElementAdapter.Holder> {
    public interface Listener { void onElementSelected(ChemicalElement element); }

    private final List<ChemicalElement> elements = new ArrayList<>();
    private final Listener listener;

    public PeriodicElementAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChemicalElement> newElements) {
        elements.clear();
        if (newElements != null) elements.addAll(newElements);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_science_chemical_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChemicalElement element = elements.get(position);
        holder.number.setText(String.valueOf(element.getAtomicNumber()));
        holder.symbol.setText(element.getSymbol());
        holder.name.setText(element.getName());
        holder.itemView.setOnClickListener(view -> listener.onElementSelected(element));
    }

    @Override
    public int getItemCount() { return elements.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView symbol;
        final TextView name;

        Holder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.tvElementNumber);
            symbol = itemView.findViewById(R.id.tvElementSymbol);
            name = itemView.findViewById(R.id.tvElementName);
        }
    }
}
