/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MStateList: a multi-lists of state, which is used for simultaneous RSSA simulation
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class MStateList {

    private ConcurrentHashMap<Species, ArrayList<Integer> > speciesCollection;

    /**
     * Create a a multi-lists of state
     */
    public MStateList() {
        speciesCollection = new ConcurrentHashMap<Species, ArrayList<Integer>>();
    }

    /**
     * @return a list of species in the state 
    */    
    public Species[] getSpeciesList()
    {
        Species[] list = new Species[speciesCollection.size()];
        int index = 0;
        for (Enumeration<Species> sp = speciesCollection.keys(); sp.hasMoreElements();) {
            list[index++] = sp.nextElement();
        }
        return list;
    }
    
    /**
     * @return a species with a given name 
    */ 
    public Species getSpecies(String name)
    {
        for (Enumeration<Species> sp = speciesCollection.keys(); sp.hasMoreElements();) {
            Species s = sp.nextElement();
            if(s.getName().equals(name)){
                return s;            
            }   
        }
        return null;
    }
    
    /**
     * add species with its population to the multi-list
     * @param s
     * @param populationList
    */ 
    public void addSpecies(Species s, ArrayList<Integer> populationList)
    {
        speciesCollection.put(s, populationList);
    }
    
    /**
     * update population of species s in multi-lists of state
     * @param s: species s
     * @param position: its position in the multi-lists 
     * @param newPopulation: its new population
    */ 
    public void updatePopulation(Species s, int position, int newPopulation)
    {
        speciesCollection.get(s).set(position, newPopulation);
    }
    
    /**
     * get population of species s in multi-lists of state
     * @param s: species s
     * @param position: its position in the multi-lists 
     * @return population of s
    */ 
    public int getPopulation(Species s, int position)
    {
        return speciesCollection.get(s).get(position);
    }

    /**
     * @return name of species in multi-lists
    */ 
    public String getSpeciesNameString()
    {
        StringBuilder result = new StringBuilder();
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            Species retrieveSpecies = s.nextElement();
            result.append(retrieveSpecies).append("\t");
        }
        return result.toString();
    }
    
    public String getSpeciesPopulationString()
    {
        StringBuilder result = new StringBuilder();
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            Species retrieveSpecies = s.nextElement();
            result.append(speciesCollection.get(retrieveSpecies)).append("\t");
        }
        return result.toString();
    }   
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            
            Species retrieveSpecies = s.nextElement();
            result.append("\n ").append(retrieveSpecies).append(" = ").append(speciesCollection.get(retrieveSpecies));
            
//            HashSet<Integer> affectReactions = retrieveSpecies.getAffectReaction();
//            result.append("\n affects reaction: " );
//            
//            if(!affectReactions.isEmpty())
//            {            
//                for(int dependencyReaction : affectReactions)
//                {
//                    result.append(dependencyReaction + ", ");
//                }
//            }else
//            {
//                result.append(" not defined , ");
//            }
//        
//            result.delete(result.length() - 2, result.length());
            
        }
        return result.toString();
    }
}
