package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.ChemicalElement;

import java.util.List;

public interface PeriodicTableRepository {
    List<ChemicalElement> getAll();
    List<ChemicalElement> search(String keyword);
    ChemicalElement findByAtomicNumber(int atomicNumber);
}
