package com.brightpath.science.data.repository;

import android.content.Context;

import com.brightpath.science.domain.model.ChemicalElement;
import com.brightpath.science.domain.repository.PeriodicTableRepository;

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

        /*
 * Responsibility: Initializes AssetPeriodicTableRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public AssetPeriodicTableRepository(Context context) {
        this.context = context.getApplicationContext();
    }

        /*
 * Responsibility: Retrieves the data/state required by `getAll` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public synchronized List<ChemicalElement> getAll() {
        if (cachedElements == null) cachedElements = loadElements();
        return Collections.unmodifiableList(cachedElements);
    }

        /*
 * Responsibility: Performs the repository operation `search` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
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

        /*
 * Responsibility: Retrieves the data/state required by `findByAtomicNumber` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public ChemicalElement findByAtomicNumber(int atomicNumber) {
        for (ChemicalElement element : getAll()) {
            if (element.getAtomicNumber() == atomicNumber) return element;
        }
        return null;
    }

        /*
 * Responsibility: Retrieves the data/state required by `loadElements` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
private List<ChemicalElement> loadElements() {
        List<ChemicalElement> result = new ArrayList<>();
        try (InputStream stream = context.getAssets().open("science/periodic_elements.json");
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
