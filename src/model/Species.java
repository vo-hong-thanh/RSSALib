package model;

import java.util.HashSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Hong Thanh
 */
public class Species {
    private String name;
    private boolean isProductOnly;
    
    private HashSet<Integer> affectReactions = new HashSet<Integer>();
    
    public Species(String name) {
        this.name = name;
        this.isProductOnly = true;
    }
     
    public String getName()
    {
        return name;
    }
    
    public boolean isProductOnly()
    {
        return isProductOnly;
    }
    
    public void setIsProductOnly(boolean value)
    {
        this.isProductOnly = value;
    }
    
    public void addAffectReaction(int ractionIndex) {
        affectReactions.add(ractionIndex);
    }

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
