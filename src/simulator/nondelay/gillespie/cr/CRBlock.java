/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.gillespie.cr;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * CRBlock: Data structure for SSA-CR
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class CRBlock {
    private double groupPropensitySum;
    
    private int groupExponent;
    
    private double maxPropensity;
    private double minPropensity;//lower limit is not included in this group

    private ArrayList<Integer> reactionInGroup;
    private Hashtable<Integer, Integer> mapReactionIndexToGroupPosition;
    
    public CRBlock(int exponent) {
        groupPropensitySum = 0;
        groupExponent = exponent;
        maxPropensity = Math.pow(2.0, exponent);
        minPropensity = Math.pow(2.0, (exponent - 1));

        reactionInGroup = new ArrayList<Integer>();
        mapReactionIndexToGroupPosition = new Hashtable<Integer, Integer>();
    }

    public double getBlockSum() {
        return groupPropensitySum;
    }

    public int getBlockExponent() {
        return groupExponent;
    }
    
    public double getMaxBlockValue()
    {
        return maxPropensity;
    }
    
    public ArrayList<Integer> getReactionInBlock()
    {
        return reactionInGroup;
    }
    
    public void insert(int reactionIndex, double propensity) {
        groupPropensitySum += propensity;
        reactionInGroup.add(reactionIndex);
        mapReactionIndexToGroupPosition.put(reactionIndex, reactionInGroup.size() - 1);
    }
    
    public void update(double oldPropensity, double newPropensity) {
        groupPropensitySum += newPropensity - oldPropensity;
    }

    public void remove(int reactionIndex, double propensity) {
        int groupPosition = mapReactionIndexToGroupPosition.get(reactionIndex);
        groupPropensitySum -= propensity;

        int lastPosition = reactionInGroup.size() - 1;
        int replaceReactionIndex = reactionInGroup.get(lastPosition);
        reactionInGroup.set(groupPosition, replaceReactionIndex);

        reactionInGroup.remove(lastPosition);

        mapReactionIndexToGroupPosition.put(replaceReactionIndex, groupPosition);
    }   
}