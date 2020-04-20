package model;

import java.util.HashSet;

/**
 * Species
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class Species {
    /**
    * name of species
    */
    private String name;
    private boolean isProductOnly;
    
    /**
    * list of reactions affected by the species
    */
    private HashSet<Integer> affectReactions = new HashSet<Integer>();
    
    /**
    * create the species
    * @param name
    */
    public Species(String name) {
        this.name = name;
        this.isProductOnly = true;
    }
     
    /**
    * @return name of species
    */
    public String getName()
    {
        return name;
    }
    
    /**
     * check the species if it is a product of a reaction
    * @return true if the species is a product only
    */    
    public boolean isProductOnly()
    {
        return isProductOnly;
    }
    
    /**
     * set the species to be a product
    * @param value
    */    
    public void setIsProductOnly(boolean value)
    {
        this.isProductOnly = value;
    }
    
    /**
     * add a reaction to the set of affected reaction list by the species
    * @param ractionIndex: index of reaction in the reaction list
    */  
    public void addAffectReaction(int ractionIndex) {
        affectReactions.add(ractionIndex);
    }

    /**     
    * @return list of reaction affected by the species
    */  
    public HashSet<Integer> getAffectReaction() {
        return affectReactions;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Species) {
            return ((Species)other).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
