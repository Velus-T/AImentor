package com.example.aimentor.science.data.repository;

import android.content.Context;

import com.example.aimentor.science.domain.model.ChemicalElement;
import com.example.aimentor.science.domain.repository.PeriodicTableRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AssetPeriodicTableRepository implements PeriodicTableRepository {
    private final Context context;
    private List<ChemicalElement> cachedElements;

    public AssetPeriodicTableRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public synchronized List<ChemicalElement> getAll() {
        if (cachedElements == null) cachedElements = loadElements();
        return Collections.unmodifiableList(cachedElements);
    }

    @Override
    public List<ChemicalElement> search(String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return getAll();
        List<ChemicalElement> result = new ArrayList<>();
        for (ChemicalElement element : getAll()) {
            if (element.getName().toLowerCase(Locale.ROOT).contains(query)
                    || element.getSymbol().toLowerCase(Locale.ROOT).contains(query)
                    || String.valueOf(element.getAtomicNumber()).equals(query)
                    || element.getCategory().toLowerCase(Locale.ROOT).contains(query)) {
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public ChemicalElement findByAtomicNumber(int atomicNumber) {
        for (ChemicalElement element : getAll()) {
            if (element.getAtomicNumber() == atomicNumber) return element;
        }
        return null;
    }

    private List<ChemicalElement> loadElements() {
        List<ChemicalElement> result = new ArrayList<>();
        try (InputStream stream = context.getAssets().open("science_periodic_elements.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            JSONArray array = new JSONArray(output.toString(StandardCharsets.UTF_8.name()));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                result.add(new ChemicalElement(
                        item.getInt("atomicNumber"),
                        item.getString("symbol"),
                        item.getString("name"),
                        item.getString("atomicMass"),
                        item.getInt("group"),
                        item.getInt("period"),
                        item.getString("category"),
                        item.getString("description"),
                        item.getString("commonUse")
                ));
            }
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load periodic table data.", error);
        }
        return result;
    }
}
