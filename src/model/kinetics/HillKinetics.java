/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.kinetics;

import model.MStateList;
import model.Species;
import model.StateList;

/**
 *
 * @author vot2
 */
public class HillKinetics implements IRateLaw {
    private double n;
    private double k;
    private Species protein;
        
    public HillKinetics(Species _protein, double _n, double _k){
        protein = _protein;
        n = _n;
        k = _k;        
    }   
    
    public Species getProtein() {
        return protein;
    }
    
    public double getN(){
        return n;
    }
    
    public double getK(){
        return k;
    }
    
    @Override
    public String toString()
    {
        return "HILL(" + " k = " + k + ", n = " + n + ", protein = " + protein + ")";
    }

    @Override
    public double evaluateRate(StateList states) {
        int pop = states.getPopulation(protein);
        return Math.pow(pop, n) / (Math.pow(k, n) + Math.pow(pop, n));
    }

    @Override
    public double evaluateRate(MStateList mstates, int position) {
        int pop = mstates.getPopulation(protein, position);
        return Math.pow(pop, n) / (Math.pow(k, n) + Math.pow(pop, n));
    }
}
