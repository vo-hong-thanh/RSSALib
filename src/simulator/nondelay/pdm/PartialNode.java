/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm;

import model.Species;

/**
 * PartialNode: Data structure for PDM
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class PartialNode {
    private Species species; //species and
    private int reactionIndex; //index of reaction reaction involved in propensity 
    
    private double partialValue; //partial propensity value
    
    public PartialNode(int rIndex, double pValue)
    {
        species = null;
        reactionIndex = rIndex;
        
        partialValue = pValue;
    }
    
    public PartialNode(Species s, int rIndex, double pValue)
    {
        species = s;
        reactionIndex = rIndex;
        
        partialValue = pValue;
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public Species getSpecies()
    {
        return species;
    }
    
    public double getPartialValue()
    {
        return partialValue;
    }
    
    public void updatePartialValue(double amount)
    {        
        partialValue += amount;
    }
    
    public void setPartialValue(double newValue)
    {        
        partialValue = newValue;
    }
    
    @Override
    public String toString()
    {
        return "Partial Node ( species = " + species + 
               ", reaction index = " + reactionIndex + 
               ", partial value = " + partialValue + " )";
    }
}
