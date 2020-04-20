/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulator.delay.delayed_mnrm;

/**
 * MNRMNode
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class MNRMNode implements Comparable{
    private int reactionIndex; //key
    
    private int nodeIndex; //additional value
    
    private double delta;
    
    private double propensity;  
    private double pk; 
    private double tk;

    public MNRMNode(int reaction_index, int node_index, double propensity, double pk, double tk)
    {
        this.reactionIndex = reaction_index;
        this.nodeIndex = node_index;
        this.propensity = propensity;
        this.pk = pk;
        this.tk = tk;
        
        this.delta = computeDelta();
    }
    
    public int getReactionIndex()
    {
        return reactionIndex;
    }
    
    public int getNodeIndex() {
        return nodeIndex;
    }
    
    public void setNodeIndex(int newValue) {
        nodeIndex = newValue;
    }
    
    public void updatePk(double randomPk)
    {
        pk += randomPk;
    }
    
    public void updateTk(double delta)
    {
        tk += propensity*delta;
    }
    
    public double getDelta()
    {
        return delta;
    }
    
    public void updateDelta()
    {
        delta = computeDelta();
    }
        
    public void updateDelta(double newPropensity)
    {
        propensity = newPropensity;        
        delta = computeDelta();
    }
    
    private double computeDelta()
    {
        if(propensity == 0.0)
            return Double.MAX_VALUE;
        
        return (pk - tk) / propensity;
    }
    
    public int compareTo(Object o) {
        if(o instanceof MNRMNode)
        {
            double o_delta = ((MNRMNode)o).delta;
            if(delta >  o_delta)
                return 1;
            else if(delta <  o_delta)
                return -1;
            else 
                return 0;
        }
        return -1;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof MNRMNode)
        {
            return ((MNRMNode)o).reactionIndex == reactionIndex;
        }
        return false;
    }
    
    public String toString()
    {
        return "Modified NRM Node ( reaction index: " + reactionIndex + ", time: " + delta + ")";
    }
    
}
