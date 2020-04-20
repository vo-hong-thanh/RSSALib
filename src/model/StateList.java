/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * StateList: list of species and its population 
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class StateList {
    private Hashtable<Species, Integer> speciesCollection;

    /**
    * create a state    
    */
    public StateList() {
        speciesCollection = new Hashtable<Species, Integer>();
    }

    /**
    * add a new species and its population
    * @param s
    * @param newPopulation 
    */
    public void addSpecies(Species s, int newPopulation){
        speciesCollection.put(s, newPopulation);
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
    * @param name
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
     * update population of a species
    * @param s
    * @param newPopulation
    */    
    public void updateSpecies(Species s, int newPopulation){
        speciesCollection.put(s, newPopulation);
    }
    
    /**
     * @return population of a species
    * @param s
    */    
    public int getPopulation(Species s){
        return speciesCollection.get(s);
    }

    /**
     * @return create a deep clone of the current state 
     * @param source
    */    
    public static StateList makeClone(StateList source){
        StateList newStateList = new StateList();
               
        for (Enumeration<Species> sc = source.speciesCollection.keys(); sc.hasMoreElements();) {
            Species s = sc.nextElement();
            newStateList.updateSpecies(s, source.speciesCollection.get(s));
        }
        return newStateList;
    }
       
    /**
     * @return name of species in the state     
    */  
    public String getSpeciesNameString(){
        StringBuilder result = new StringBuilder();
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            Species retrieveSpecies = s.nextElement();
            result.append(retrieveSpecies + "\t");
        }
        return result.toString();
    }
    
    /**
     * @return name of species and it population in the state     
    */  
    public String getSpeciesPopulationString() {
        StringBuilder result = new StringBuilder();
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            Species retrieveSpecies = s.nextElement();
            result.append(speciesCollection.get(retrieveSpecies) +"\t");
        }
        return result.toString();
    }   
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            
            Species retrieveSpecies = s.nextElement();
            result.append("\n "+retrieveSpecies + " = " + speciesCollection.get(retrieveSpecies));
            
        }
        return result.toString();
    }
    
     public String toStringFull() {
        StringBuilder result = new StringBuilder();
        
        for (Enumeration<Species> speciesList = speciesCollection.keys(); speciesList.hasMoreElements();) {
            Species s = speciesList.nextElement();
            result.append("\n").append(s).append(" = ").append(speciesCollection.get(s));
            
            HashSet<Integer> affectReactions = s.getAffectReaction();            
            if(!affectReactions.isEmpty()){ 
                result.append("\n affects reaction: " );
                
                for(int dependencyReaction : affectReactions)
                {
                    result.append(dependencyReaction).append(", ");
                }
                
                result.delete(result.length() - 2, result.length());    
            }
        }
        return result.toString();
    }
}
