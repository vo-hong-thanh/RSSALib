/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simulator.nondelay.nrm;

/**
 *
 * @author Hong Thanh
 */
public class NRMNode implements Comparable{
    private int reactionIndex; //key
    
    private int nodeIndex; //additional value
    private double propensity;  //additional value
    
    private double putativeTime; //value

    public NRMNode(int reaction_index, int heap_index, double propensity, double putative_time)
    {
        this.reactionIndex = reaction_index;
        this.nodeIndex = heap_index;
        this.propensity = propensity;
        this.putativeTime = putative_time;
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
    
    public double getPutativeTime() {
        return putativeTime;
    }
    
    public void setPutativeTime(double newValue) {
        putativeTime = newValue;
    }
    
    public double getPropensity()
    {
        return propensity;
    }
    
    public void setPropensity(double newValue)
    {
        this.propensity = newValue;
    }
    
    public void updateNodeValue(double newPropensity, double newPutativeTime)
    {
        this.propensity = newPropensity;
        this.putativeTime = newPutativeTime;
    }
    
    public int compareTo(Object o) {
        if(o instanceof NRMNode)
        {
            double o_putative_time = ((NRMNode)o).putativeTime;
            if(putativeTime >  o_putative_time)
                return 1;
            else if(putativeTime <  o_putative_time)
                return -1;
            else 
                return 0;
        }
        return -1;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof NRMNode)
        {
            return ((NRMNode)o).reactionIndex == reactionIndex;
        }
        return false;
    }
    
    public String toString()
    {
        return "NRM Heap Node ( reaction index: " + reactionIndex + ", time: " + putativeTime + ")";
    }
    
}
