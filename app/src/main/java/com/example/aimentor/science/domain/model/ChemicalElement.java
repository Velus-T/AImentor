package com.example.aimentor.science.domain.model;

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

    public int getAtomicNumber() { return atomicNumber; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getAtomicMass() { return atomicMass; }
    public int getGroup() { return group; }
    public int getPeriod() { return period; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getCommonUse() { return commonUse; }
}
