/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulator.nondelay.rssa;

/**
 *
 * @author HongThanh
 */
public class RSSANode {
    private int reactionIndex;
    private double min_propensity; //reflecting change 
    private double max_propensity; //no change
    
    public RSSANode(int reactionIndex, double min_propensity, double max_propensity)
    {
        this.reactionIndex = reactionIndex;
        this.min_propensity = min_propensity;
        this.max_propensity = max_propensity;
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public void setReactionIndex(int reactionIndex)
    {
        this.reactionIndex = reactionIndex;
    }
    
    public double getMinPropensity()
    {
        return min_propensity;
    }
    
    public void setMinPropensity(double min_propensity)
    {
        this.min_propensity = min_propensity;
    }
    
    public double getMaxPropensity()
    {
        return max_propensity;
    }
    
    public void setMaxPropensity(double max_propensity)
    {
        this.max_propensity = max_propensity;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RSSANode) {
            return ((RSSANode)o).reactionIndex == reactionIndex;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return reactionIndex;
    }
}
