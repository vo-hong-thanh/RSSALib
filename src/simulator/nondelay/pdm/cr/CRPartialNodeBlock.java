/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm.cr;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author vo
 */
public class CRPartialNodeBlock {
    private double groupPropensitySum;
    
    private int groupExponent;
    
    private double maxPropensity;
    private double minPropensity;//lower limit is not included in this group

    private ArrayList<Integer> nodeIndexInGroup;
    private Hashtable<Integer, Integer> mapNodeIndexToBlockPosition;
    
    public CRPartialNodeBlock(int exponent) {
        groupPropensitySum = 0;
        groupExponent = exponent;
        maxPropensity = Math.pow(2.0, exponent);
        minPropensity = Math.pow(2.0, (exponent - 1));

        nodeIndexInGroup = new ArrayList<Integer>();
        mapNodeIndexToBlockPosition = new Hashtable<Integer, Integer>();
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
    
    public ArrayList<Integer> getNodeIndexInGroup()
    {
        return nodeIndexInGroup;
    }
    
    public void insert(int nodeIndex, double value) {
        groupPropensitySum += value;
        nodeIndexInGroup.add(nodeIndex);
        mapNodeIndexToBlockPosition.put(nodeIndex, nodeIndexInGroup.size() - 1);
    }
    
    public void update(double oldValue, double newValue) {
        groupPropensitySum += newValue - oldValue;
    }

    public void remove(int nodeIndex, double value) {
        int blockPosition = mapNodeIndexToBlockPosition.get(nodeIndex);
        groupPropensitySum -= value;

        int lastPosition = nodeIndexInGroup.size() - 1;
        int replaceNodeIndex = nodeIndexInGroup.get(lastPosition);
        nodeIndexInGroup.set(blockPosition, replaceNodeIndex);

        nodeIndexInGroup.remove(lastPosition);

        mapNodeIndexToBlockPosition.put(replaceNodeIndex, blockPosition);
    }   
}