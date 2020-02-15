/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.rates;

import model.MStateList;
import model.Species;
import model.StateList;

/**
 *
 * @author vot2
 */
public class MMKinetics implements IRateLaw{
    private double k;
    private double v;

    private Species substrate;
    
    public MMKinetics(Species _substrate, double _v, double _k){
        substrate = _substrate;
        v = _v;
        k = _k;        
    } 
    
    public Species getSubstrate() {
        return substrate;
    }
    
    public double getV(){
        return v;
    }
    
    public double getK(){
        return k;
    }
    
    @Override
    public String toString()
    {
        return "MM(" + " Vm = " + v + ", Km = " + k + ", Substrate = " + substrate + ")";
    }

    @Override
    public double evaluateRate(StateList states) {
        return v / (k + states.getPopulation(substrate));
    }

    @Override
    public double evaluateRate(MStateList mstates, int position) {
        return v / (k + mstates.getPopulation(substrate,position));        
    }
}
