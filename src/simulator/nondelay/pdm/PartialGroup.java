/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm;

import simulator.nondelay.pdm.cr.CRPartialNodeBlock;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import model.Species;
import utils.ComputingMachine;

/**
 * PDM: Data structure for PDM
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class PartialGroup {    
    private Species species; //groupping species
        
    //array of partial propensities
    private ArrayList<PartialNode> partialNodeList;
    
    //sum of partial node list value
    private double groupSumPartialValue;
    
    //data structure for CR
    private int maxGroupExponent;
    private int minGroupExponent;
    
    private LinkedList<CRPartialNodeBlock> crnode_list;
    
    public PartialGroup()
    {
        species = null;
        partialNodeList = new ArrayList<PartialNode>();
        
        groupSumPartialValue = 0;
    }
    
    public PartialGroup(Species s)
    {
        species = s;
        partialNodeList = new ArrayList<PartialNode>();
        
        groupSumPartialValue = 0;
    }
    
    public Species getSpecies()
    {
        return species;
    }
    
    public int addPartialNode(PartialNode node)
    {
        partialNodeList.add(node);
        groupSumPartialValue += node.getPartialValue();
        
        return partialNodeList.size() - 1;
    }
    
    public PartialNode getPartialNode(int nodeIndex)
    {
        return partialNodeList.get(nodeIndex);
    }
    
    public void setPartialNode(int nodeIndex, PartialNode newNode)
    {
        partialNodeList.set(nodeIndex, newNode);
    }
    
    public ArrayList<PartialNode> getAllPartialNode()
    {
        return partialNodeList;
    }
    
    public double getGroupSumPartialValue()
    {
        return groupSumPartialValue;
    }
    
    public void updateGroupSumPartialValue(double amount)
    {
        groupSumPartialValue += amount;
    }

    public int findNodeBySequentialSearch(double value) {
//        System.out.println(" search in group for value " + value);        
        PartialNode node;
        double partialSum = 0;
        
        int nodeIndex = -1;
        for(nodeIndex = 0; nodeIndex < partialNodeList.size(); nodeIndex++)
        {
//            System.out.println(" process node " + nodeIndex);
            node = partialNodeList.get(nodeIndex);
            partialSum += node.getPartialValue();
            
            if(partialSum >= value){
//                System.out.println("  => found node " + nodeIndex);
                break;
            }
        }
        return nodeIndex;
    }
    
    public int findNodeByCRSearch(Random rand) {
        int blockIndex = -1;
        int nodeIndex = -1;     

        //select a group via linear search
        double searchValue = rand.nextDouble()*groupSumPartialValue;
        double partialBlockSum = 0;
        for(blockIndex = 0; blockIndex < crnode_list.size(); blockIndex++){
            partialBlockSum += crnode_list.get(blockIndex).getBlockSum();
            if(partialBlockSum >= searchValue) {
                break;
            }
        }
//        System.out.println(" + found group " + blockIndex);  

        //select reaction in group via rejection
        CRPartialNodeBlock g = crnode_list.get(blockIndex);
        ArrayList<Integer> nodeIndexInGroup = g.getNodeIndexInGroup();

        double maxGroupPropensity = g.getMaxBlockValue();

        int nodeIndexInBlock = -1;
        double randomCheckingValue;

        if (nodeIndexInGroup.size() == 1) { //if there is only one reaction in the group, choose it
            nodeIndex = nodeIndexInGroup.get(0);
        } else {
            while (true) {
                //generate an integer index between 0 and size of group-1 as tentative reaction index 
                nodeIndexInBlock = (int) (rand.nextDouble() * nodeIndexInGroup.size());

                //generate a continuous number within the group's propensity range
                randomCheckingValue = rand.nextDouble() * maxGroupPropensity;
                //if randomCheckingValue is less than the propensity of tentativeReaction propensity then accept the reaction
                nodeIndex = nodeIndexInGroup.get(nodeIndexInBlock);
                if (randomCheckingValue <= partialNodeList.get(nodeIndex).getPartialValue()) {
//                        System.out.println(" + found reaction " + reactionIndex);
                   break;
                }
            }
        }
        return nodeIndex;
    }
    
    public boolean isEmptyCRPartialNodeBlock()
    {
        return crnode_list.isEmpty();
    }
    
    public void buildCRPartialNodeList() {
//        System.out.println(" --- Build group ---");        
        crnode_list = new LinkedList<CRPartialNodeBlock>();

        boolean oneNonZeroPropensity = false;

        for (int nodeIndex = 0; nodeIndex < partialNodeList.size(); nodeIndex++) {
            PartialNode node = partialNodeList.get(nodeIndex);
            double value = node.getPartialValue();

            if (!oneNonZeroPropensity) {
                if (value > 0.0) {
//                    System.out.println(" (This is first non-zero value) "); 
                    oneNonZeroPropensity = true;

                    int exponent = ComputingMachine.computeGroupExponent(value);
            
                    minGroupExponent = exponent;
                    maxGroupExponent = exponent;

                    CRPartialNodeBlock newGroup = new CRPartialNodeBlock(exponent);
                    newGroup.insert(nodeIndex, value);

//                    System.out.println(" put it to group " + getGroupIndex(value)); 
                    crnode_list.addFirst(newGroup);                    
                }
            } else {
                int newGroupIndex = getGroupIndex(value);                
//                System.out.println(" tentative group index " + newGroupIndex); 
                if (newGroupIndex == -1) {
                    //either need to add new group or do nothing (value = 0)
                    if (value != 0.0) {
                        addGroup(ComputingMachine.computeGroupExponent(value));
                        newGroupIndex = getGroupIndex(value);//group index changed
                        crnode_list.get(newGroupIndex).insert(nodeIndex, value);
                        
//                        System.out.println(" since it does not exist => create a new group " + newGroupIndex); 
                    }
                }else { // insert new reaction into group
//                    System.out.println(" since it exists => put to the group " + newGroupIndex); 
                    crnode_list.get(newGroupIndex).insert(nodeIndex, value);
                }                
            }
        }
        
        //ghost Block
        if (!oneNonZeroPropensity) {
            minGroupExponent = 0;
            maxGroupExponent = 0;

            CRPartialNodeBlock newGroup = new CRPartialNodeBlock(0);
//                    System.out.println(" put it to group " + getGroupIndex(value)); 
            crnode_list.addFirst(newGroup);
        }
    }

    public void updateCRPartialNodeList(int nodeIndex, double oldValue) {
//        System.out.println(" --- update node : " + nodeIndex);
        double newValue = partialNodeList.get(nodeIndex).getPartialValue();

        int oldGroupIndex;
        if(oldValue <= 0.0){
            if(crnode_list.isEmpty()){
                maxGroupExponent = 0;
                minGroupExponent = 0;
                CRPartialNodeBlock newGroup = new CRPartialNodeBlock(0);
                crnode_list.addFirst(newGroup);
            }
            oldGroupIndex = -1;            
        }else{
            oldGroupIndex = getGroupIndex(oldValue);
        }
                    
        int newGroupIndex = getGroupIndex(newValue);
                
//        System.out.println(" old value: " + oldValue + " (group index: " + oldGroupIndex +")" + " to: " + newValue + " (group index: " + newGroupIndex +")");
        
        if (newGroupIndex == oldGroupIndex) {
            if (newGroupIndex == -1) {                
                //either need to add new group or do nothing (value = 0)
                if (newValue != 0.0) {
                    addGroup(ComputingMachine.computeGroupExponent(newValue));
                    newGroupIndex = getGroupIndex(newValue);//group index changed
                    
//                    System.out.println(" => create new group " + newGroupIndex);
                    
                    crnode_list.get(newGroupIndex).insert(nodeIndex, newValue);
                }
            } else {
//                System.out.println(" => update group " + newGroupIndex);
                //did not change group, simple update
                crnode_list.get(newGroupIndex).update(oldValue, newValue);
            }
        } else {//changed group
            //remove from old group
            if (oldGroupIndex != -1) {
//                System.out.println(" remove from group index " + oldGroupIndex);
                crnode_list.get(oldGroupIndex).remove(nodeIndex, oldValue);
                //groups[oldGroupIndex].remove(nodeIndex,withinGroupIndexes[nodeIndex],withinGroupIndexes);
            }

            //add to new group
//            System.out.println(" to new group index " + newGroupIndex);
            if (newGroupIndex == -1) {
                if (newValue > 0) {
                    //need to add a group
                    addGroup(ComputingMachine.computeGroupExponent(newValue));
                    newGroupIndex = getGroupIndex(newValue);//group index changed
                    
//                    System.out.println("  => create new group " + newGroupIndex);
                    
                    crnode_list.get(newGroupIndex).insert(nodeIndex, newValue);
                }
            } else {
//                System.out.println("  => insert reaction to group " + newGroupIndex);
                crnode_list.get(newGroupIndex).insert(nodeIndex, newValue);
            }
        }
    }    

    private void addGroup(int newGroupExponent) {
        while (newGroupExponent < minGroupExponent) {
            CRPartialNodeBlock newGroup = new CRPartialNodeBlock(--minGroupExponent);
            crnode_list.addLast(newGroup);
        }
        while (newGroupExponent > maxGroupExponent) {
            CRPartialNodeBlock newGroup = new CRPartialNodeBlock(++maxGroupExponent);
            crnode_list.addFirst(newGroup);
        }
    }
    
    private int getGroupIndex(double propensityValue) {
        if (propensityValue == 0.0) {
            return -1;
        } else {
            int exponent = ComputingMachine.computeGroupExponent(propensityValue);
            if (exponent >= minGroupExponent && exponent <= maxGroupExponent) {
                return maxGroupExponent - exponent;
            } else {
                return -1;
            }
        }
    }    
}
