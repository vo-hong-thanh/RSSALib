/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.prssa;

import model.Species;

/**
 * PartialRSSANode: Data structure for PRSSA
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class PartialRSSANode {
    private Species species; //species and
    private int reactionIndex; //index of reaction reaction involved in propensity 
    
    private double maxPartialValue; //maximum partial propensity value
    private double minPartialValue; //minimum partial propensity value
    
    public PartialRSSANode(int rIndex, double max_pValue, double min_pValue)
    {
        species = null;
        reactionIndex = rIndex;
        
        maxPartialValue = max_pValue;
        minPartialValue = min_pValue;
    }
    
    public PartialRSSANode(Species s, int rIndex, double max_pValue, double min_pValue)
    {
        species = s;
        reactionIndex = rIndex;
        
        maxPartialValue = max_pValue;
        minPartialValue = min_pValue;
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public Species getSpecies()
    {
        return species;
    }
    
    public double getMaxPartialValue()
    {
        return maxPartialValue;
    }
    
    public void updateMaxPartialValue(double amount)
    {        
        maxPartialValue += amount;
    }
    
    public void setMaxPartialValue(double newValue)
    {        
        maxPartialValue = newValue;
    }
    
    public double getMinPartialValue()
    {
        return minPartialValue;
    }
    
    public void updateMinPartialValue(double amount)
    {        
        minPartialValue += amount;
    }
    
    public void setMinPartialValue(double newValue)
    {        
        minPartialValue = newValue;
    }
    
    @Override
    public String toString()
    {
        return "Partial Node ( species = " + species + 
               ", reaction index = " + reactionIndex + 
               ", max partial value = " + maxPartialValue +
               ", min partial value = " + minPartialValue +" )";
    }
}
