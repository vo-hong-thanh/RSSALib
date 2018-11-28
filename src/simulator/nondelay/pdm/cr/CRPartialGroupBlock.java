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
public class CRPartialGroupBlock {
    private double groupSum;
    
    private int groupExponent;
    
    private double maxPropensity;
    private double minPropensity;//lower limit is not included in this group

    private ArrayList<Integer> partialGroupIndexInCRPartialGroupBlock;
    private Hashtable<Integer, Integer> mapPartialGroupIndexToCRPartialGroupBlockPosition;
    
    public CRPartialGroupBlock(int exponent) {
        groupSum = 0;
        groupExponent = exponent;
        maxPropensity = Math.pow(2.0, exponent);
        minPropensity = Math.pow(2.0, (exponent - 1));

        partialGroupIndexInCRPartialGroupBlock = new ArrayList<Integer>();
        mapPartialGroupIndexToCRPartialGroupBlockPosition = new Hashtable<Integer, Integer>();
    }

    public double getBlockSum() {
        return groupSum;
    }

    public int getBlockExponent() {
        return groupExponent;
    }
    
    public double getMaxBlockValue()
    {
        return maxPropensity;
    }
    
    public ArrayList<Integer> getPartialGroupIndexInCRPartialGroupBlock()
    {
        return partialGroupIndexInCRPartialGroupBlock;
    }
    
    public void insert(int partialGroupIndex, double propensity) {
        groupSum += propensity;
        partialGroupIndexInCRPartialGroupBlock.add(partialGroupIndex);
        mapPartialGroupIndexToCRPartialGroupBlockPosition.put(partialGroupIndex, partialGroupIndexInCRPartialGroupBlock.size() - 1);
    }
    
    public void update(double oldPropensity, double newPropensity) {
        groupSum += newPropensity - oldPropensity;
    }

    public void remove(int partialGroupIndex, double propensity) {
        int blockPosition = mapPartialGroupIndexToCRPartialGroupBlockPosition.get(partialGroupIndex);
        groupSum -= propensity;

        int lastPosition = partialGroupIndexInCRPartialGroupBlock.size() - 1;
        int replaceIndex = partialGroupIndexInCRPartialGroupBlock.get(lastPosition);
        partialGroupIndexInCRPartialGroupBlock.set(blockPosition, replaceIndex);

        partialGroupIndexInCRPartialGroupBlock.remove(lastPosition);

        mapPartialGroupIndexToCRPartialGroupBlockPosition.put(replaceIndex, blockPosition);
    }   
}