/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.prssa;

import java.util.ArrayList;
import model.Species;

/**
 *
 * @author vo
 */
public class PartialRSSAGroup {    
    private Species species; //groupping species
        
    //array of partial propensities
    private ArrayList<PartialRSSANode> partialNodeList;
    
    //sum of partial node list value
    private double maxPartialValueSum;
    
    public PartialRSSAGroup()
    {
        species = null;
        partialNodeList = new ArrayList<PartialRSSANode>();
        
        maxPartialValueSum = 0;
    }
    
    public PartialRSSAGroup(Species s)
    {
        species = s;
        partialNodeList = new ArrayList<PartialRSSANode>();
        
        maxPartialValueSum = 0;
    }
    
    public Species getSpecies()
    {
        return species;
    }
    
    public int addPartialRSSANode(PartialRSSANode node)
    {
        partialNodeList.add(node);
        maxPartialValueSum += node.getMaxPartialValue();
        
        return partialNodeList.size() - 1;
    }
    
    public ArrayList<PartialRSSANode> getAllPartialRSSANodes()
    {
        return partialNodeList;
    }
    
    public PartialRSSANode getPartialRSSANode(int nodeIndex)
    {
        return partialNodeList.get(nodeIndex);
    }
    
    public void setPartialRSSANode(int nodeIndex, PartialRSSANode newNode)
    {
        partialNodeList.set(nodeIndex, newNode);
    }
    
    public double getMaxPartialValueSum()
    {
        return maxPartialValueSum;
    }
    
    public void updateMaxPartialValueSum(double amount)
    {
        maxPartialValueSum += amount;
    }

    public int findNodeBySequentialSearch(double value) {
//        System.out.println(" search in group for value " + value);        
        PartialRSSANode node;
        double partialSum = 0;
        
        int nodeIndex = -1;
        for(nodeIndex = 0; nodeIndex < partialNodeList.size(); nodeIndex++)
        {
//            System.out.println(" process node " + nodeIndex);
            node = partialNodeList.get(nodeIndex);
            partialSum += node.getMaxPartialValue();
            
            if(partialSum >= value){
//                System.out.println("  => found node " + nodeIndex);
                break;
            }
        }
        return nodeIndex;
    }   
}
