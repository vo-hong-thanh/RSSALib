/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.rates;

import model.MStateList;
import model.StateList;

/**
 * IRateLaw: kinetic rate of reaction
 * @author Vo Hong Thanh
 * @version 1.0
*/
public interface IRateLaw {
    /**
 * @return a value of the rate
 * @param states: current state of the biochemical system
 *  
*/
    public double evaluateRate(StateList states);
    
        /**
 * @return a value of the rate 
 * @param states: multi-states
 * @param position: position of states in the multi-states 
 *  
*/
     public double evaluateRate(MStateList states, int position);
}
