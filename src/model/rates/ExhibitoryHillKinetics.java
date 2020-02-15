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
public class ExhibitoryHillKinetics implements IRateLaw {
    private Species protein;
    private double baseRate;
    private double n;
    private double threshold;
            
    public ExhibitoryHillKinetics(Species _protein, double _baseRate, double _n, double _threshold){
        protein = _protein;
        baseRate = _baseRate;
        n = _n;
        threshold = _threshold;        
    }   
    
    public Species getProtein() {
        return protein;
    }
    
    public double getBaseRate(){
        return baseRate;
    }
    
    public double getN(){
        return n;
    }
    
    public double getThreshold(){
        return threshold;
    }
    
    @Override
    public String toString()
    {
        return "Exhibitory HILL( protein = " + protein + ", base rate = " + baseRate + ", n = " + n + ", threshold = " + threshold + ")";
    }

    @Override
    public double evaluateRate(StateList states) {
        int pop = states.getPopulation(protein);
        return (baseRate*Math.pow(pop, n)) / (Math.pow(threshold, n) + Math.pow(pop, n));
    }

    @Override
    public double evaluateRate(MStateList mstates, int position) {
        int pop = mstates.getPopulation(protein, position);
        return (baseRate*Math.pow(pop, n)) / (Math.pow(threshold, n) + Math.pow(pop, n));
    }
}
