/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulator.nondelay.gillespie;

/**
 *
 * @author HongThanh
 */
public class DMNode implements Comparable{
    private int reactionIndex;
    private double propensity;
    private int nodeIndex;
    
    public DMNode(int reactionIndex, double propensity, int nodeIndex)
    {
        this.reactionIndex = reactionIndex;
        this.propensity = propensity;
        this.nodeIndex = nodeIndex;
    }
    
    public void setReactionIndex(int reactionIndex)
    {
        this.reactionIndex = reactionIndex;
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public void setNodeIndex(int nodeIndex)
    {
        this.nodeIndex = nodeIndex;
    }
    
    public int getNodeIndex()
    {
        return nodeIndex;
    }
    
    public double getPropensity()
    {
        return propensity;
    }
    
    public void setPropensity(double propensity)
    {
        this.propensity = propensity;
    }

    //decending sort
    public int compareTo(Object t) {
        if(t instanceof DMNode)
        {
            DMNode cast = (DMNode)t;
            if(propensity > cast.propensity)
                return 1;
            else if(propensity < cast.propensity)
                return -1;
            else return 0;
        }
        else return -1;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof DMNode)
        {
            return ((DMNode)o).reactionIndex == reactionIndex;
        }
        return false;
    }    
}
