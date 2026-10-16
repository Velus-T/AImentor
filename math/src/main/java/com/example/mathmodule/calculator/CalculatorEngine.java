package com.example.mathmodule.calculator;

import java.math.BigDecimal;
import java.math.MathContext;

public final class CalculatorEngine {
  private final String text;
  private int position;

  private CalculatorEngine(String expression) {
    text =
        expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
            .replace(" ", "");
  }

  public static String calculate(String expression) {
    if (expression == null || expression.length() > 256)
      throw new IllegalArgumentException("Invalid expression length");
    CalculatorEngine p = new CalculatorEngine(expression);
    double x = p.sum();
    if (p.position != p.text.length() || !Double.isFinite(x))
      throw new IllegalArgumentException("Invalid calculation");
    return x == 0
        ? "0"
        : BigDecimal.valueOf(x).round(new MathContext(12)).stripTrailingZeros().toPlainString();
  }

  private boolean take(char c) {
    if (position < text.length() && text.charAt(position) == c) {
      position++;
      return true;
    }
    return false;
  }

  private double sum() {
    double x = product();
    while (true) {
      if (take('+')) x += product();
      else if (take('-')) x -= product();
      else return x;
    }
  }

  private double product() {
    double x = unary();
    while (true) {
      if (take('*')) x *= unary();
      else if (take('/')) {
        double y = unary();
        if (y == 0) throw new IllegalArgumentException("Division by zero");
        x /= y;
      } else return x;
    }
  }

  private double unary() {
    if (take('+')) return unary();
    if (take('-')) return -unary();
    return power();
  }

  private double power() {
    double x = atom();
    while (take('%')) x /= 100;
    if (take('^')) x = Math.pow(x, unary());
    return x;
  }

  private double atom() {
    if (take('(')) {
      double x = sum();
      if (!take(')')) throw new IllegalArgumentException("Missing parenthesis");
      return x;
    }
    if (text.startsWith("sqrt(", position)) {
      position += 5;
      double x = sum();
      if (!take(')') || x < 0) throw new IllegalArgumentException("Invalid root");
      return Math.sqrt(x);
    }
    int begin = position;
    while (position < text.length()
        && (Character.isDigit(text.charAt(position)) || text.charAt(position) == '.')) position++;
    if (begin == position) throw new IllegalArgumentException("Missing number");
    return Double.parseDouble(text.substring(begin, position));
  }
}
