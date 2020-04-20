/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.rates;

import model.MStateList;
import model.StateList;

/**
 * MassActionKinetics: Mass-action kinetics
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class MassActionKinetics implements IRateLaw {
    private double k;

    public MassActionKinetics(double _k) {
        k = _k;
    }
    
    public double getK()
    {
        return k;
    }
    
    @Override
    public String toString()
    {
        return "MASS(" + k + ")";
    }

    @Override
    public double evaluateRate(StateList states) {
        return k;
    }

    @Override
    public double evaluateRate(MStateList mstates, int position) {
        return k;
    }
}
