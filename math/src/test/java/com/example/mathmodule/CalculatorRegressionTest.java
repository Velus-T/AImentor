package com.example.mathmodule;
import org.junit.Test;import static org.junit.Assert.*;
import com.example.mathmodule.calculator.CalculatorEngine;
import com.example.mathmodule.util.MathCategory;
public class CalculatorRegressionTest {
 @Test public void standardOperatorPrecedence() {assertEquals("14",CalculatorEngine.calculate("2+3×4"));assertEquals("20",CalculatorEngine.calculate("(2+3)×4"));}
 @Test public void decimalAndLocalizedComma() {assertEquals("0.3",CalculatorEngine.calculate("0.1+0.2"));assertEquals("3.75",CalculatorEngine.calculate("1,5+2,25"));}
 @Test public void powersAssociateToTheRight() {assertEquals("512",CalculatorEngine.calculate("2^3^2"));assertEquals("-4",CalculatorEngine.calculate("-2^2"));}
 @Test public void percentIsExplicitlyPostfix() {assertEquals("0.5",CalculatorEngine.calculate("50%"));assertEquals("100",CalculatorEngine.calculate("200×50%"));}
 @Test public void rootsAndNegativeNumbers() {assertEquals("7",CalculatorEngine.calculate("sqrt(9)+4"));assertEquals("2",CalculatorEngine.calculate("-3+5"));}
 @Test(expected=IllegalArgumentException.class) public void divisionByZeroIsRejected() {CalculatorEngine.calculate("1÷0");}
 @Test(expected=IllegalArgumentException.class) public void negativeRootIsRejected() {CalculatorEngine.calculate("sqrt(-1)");}
 @Test(expected=IllegalArgumentException.class) public void incompleteExpressionIsRejected() {CalculatorEngine.calculate("2+");}
 @Test(expected=IllegalArgumentException.class) public void overflowIsRejected() {CalculatorEngine.calculate("10^1000");}
 @Test public void categoryKeysDoNotDependOnLanguage() {assertEquals(MathCategory.canonical("Đại số"),MathCategory.canonical("Algebra"));assertEquals(MathCategory.canonical("Hình học"),MathCategory.canonical("Geometry"));}
}
