/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.rates;

import model.MStateList;
import model.StateList;

/**
 *
 * @author vot2
 */
public interface IRateLaw {
    public double evaluateRate(StateList states);
     public double evaluateRate(MStateList states, int position);
}
