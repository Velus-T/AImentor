package com.brightpath.science;
import org.junit.Test;import static org.junit.Assert.*;
import com.brightpath.science.domain.model.CircuitModel;
public class CircuitRegressionTest {
 @Test public void seriesRespectsVoltageAndCurrentLaws() {CircuitModel c=CircuitModel.calculate(6,10,20,false,true);assertEquals(30,c.resistance,1e-9);assertEquals(0.2,c.current,1e-9);assertEquals(c.current1,c.current2,1e-9);assertEquals(6,c.voltage1+c.voltage2,1e-9);}
 @Test public void parallelRespectsBranchCurrentLaw() {CircuitModel c=CircuitModel.calculate(6,10,20,true,true);assertEquals(0.9,c.current,1e-9);assertEquals(c.current,c.current1+c.current2,1e-9);assertEquals(6,c.voltage1,1e-9);assertEquals(6,c.voltage2,1e-9);}
 @Test public void openSwitchCarriesNoCurrentOrPower() {for(boolean p:new boolean[]{false,true}) {CircuitModel c=CircuitModel.calculate(6,10,20,p,false);assertEquals(0,c.current,0);assertEquals(0,c.power,0);}}
 @Test public void zeroSourceIsNotAnIlluminatedCircuit() {assertEquals(0,CircuitModel.calculate(0,10,20,true,true).current,0);}
 @Test(expected=IllegalArgumentException.class) public void invalidResistanceIsRejected() {CircuitModel.calculate(6,0,20,false,true);}
 @Test(expected=IllegalArgumentException.class) public void nonFiniteValuesAreRejected() {CircuitModel.calculate(Double.NaN,10,20,true,true);}
}
