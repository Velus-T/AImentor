package com.brightpath.science.domain.model;

public final class ChemicalElement {
    private final int atomicNumber;
    private final String symbol;
    private final String name;
    private final String atomicMass;
    private final int group;
    private final int period;
    private final String category;
    private final String description;
    private final String commonUse;

        /*
 * Responsibility: Initializes ChemicalElement and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ChemicalElement(int atomicNumber, String symbol, String name, String atomicMass,
                           int group, int period, String category, String description, String commonUse) {
        this.atomicNumber = atomicNumber;
        this.symbol = symbol;
        this.name = name;
        this.atomicMass = atomicMass;
        this.group = group;
        this.period = period;
        this.category = category;
        this.description = description;
        this.commonUse = commonUse;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getAtomicNumber` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getAtomicNumber() { return atomicNumber; }
        /*
 * Responsibility: Retrieves the data/state required by `getSymbol` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getSymbol() { return symbol; }
        /*
 * Responsibility: Retrieves the data/state required by `getName` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getName() { return name; }
        /*
 * Responsibility: Retrieves the data/state required by `getAtomicMass` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getAtomicMass() { return atomicMass; }
        /*
 * Responsibility: Retrieves the data/state required by `getGroup` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getGroup() { return group; }
        /*
 * Responsibility: Retrieves the data/state required by `getPeriod` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getPeriod() { return period; }
        /*
 * Responsibility: Retrieves the data/state required by `getCategory` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getCategory() { return category; }
        /*
 * Responsibility: Retrieves the data/state required by `getDescription` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getDescription() { return description; }
        /*
 * Responsibility: Retrieves the data/state required by `getCommonUse` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getCommonUse() { return commonUse; }
}
