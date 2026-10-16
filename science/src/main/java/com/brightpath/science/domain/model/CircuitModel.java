package com.brightpath.science.domain.model;

/** Ideal DC source and two constant resistances; the lamp is represented by R2. */
public final class CircuitModel {
  public final double resistance, current, current1, current2, voltage1, voltage2, power;

  private CircuitModel(double r, double i, double i1, double i2, double u1, double u2, double p) {
    resistance = r;
    current = i;
    current1 = i1;
    current2 = i2;
    voltage1 = u1;
    voltage2 = u2;
    power = p;
  }

  public static CircuitModel calculate(
      double voltage, double r1, double r2, boolean parallel, boolean closed) {
    if (!Double.isFinite(voltage)
        || !Double.isFinite(r1)
        || !Double.isFinite(r2)
        || voltage < 0
        || r1 <= 0
        || r2 <= 0) throw new IllegalArgumentException("Invalid circuit values");
    double r = parallel ? 1 / (1 / r1 + 1 / r2) : r1 + r2;
    double i = closed ? voltage / r : 0,
        i1 = parallel ? (closed ? voltage / r1 : 0) : i,
        i2 = parallel ? (closed ? voltage / r2 : 0) : i;
    double u1 = i1 * r1, u2 = i2 * r2, p = voltage * i;
    if (!Double.isFinite(r) || !Double.isFinite(i) || !Double.isFinite(p) || r <= 0)
      throw new IllegalArgumentException("Circuit values out of range");
    return new CircuitModel(r, i, i1, i2, u1, u2, p);
  }
}
