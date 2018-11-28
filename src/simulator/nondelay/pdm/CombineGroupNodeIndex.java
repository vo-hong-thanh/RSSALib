/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm;

/**
 *
 * @author vo
 */
public class CombineGroupNodeIndex {
    private int groupIndex;
    private int nodeIndex;
    
    public CombineGroupNodeIndex(int groupIndex, int nodeIndex)
    {
        this.groupIndex = groupIndex;
        this.nodeIndex = nodeIndex;
    }
    
    public int getGroupIndex()
    {
        return groupIndex;
    }
    
    public void setGroupIndex(int newGroupIndex)
    {
        groupIndex = newGroupIndex;
    }
    
    public int getNodeIndex()
    {
        return nodeIndex;
    }
    
    public void setNodeIndex(int newNodeIndex)
    {
        nodeIndex = newNodeIndex;
    }
    
    @Override
    public String toString()
    {
        return "( group = " + groupIndex +
               ". pos in group = " + nodeIndex + " )";
    }
}
