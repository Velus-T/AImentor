package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.ChemicalElement;

import java.util.List;

public interface PeriodicTableRepository {
        /*
 * Responsibility: Retrieves the data/state required by `getAll` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
List<ChemicalElement> getAll();
        /*
 * Responsibility: Performs the repository operation `search` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
List<ChemicalElement> search(String keyword);
        /*
 * Responsibility: Retrieves the data/state required by `findByAtomicNumber` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
ChemicalElement findByAtomicNumber(int atomicNumber);
}
