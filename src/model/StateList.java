/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author Hong Thanh
 */
public class StateList {
    private Hashtable<Species, Integer> speciesCollection;

    public StateList() {
        speciesCollection = new Hashtable<Species, Integer>();
    }

    public void addSpecies(Species s, int newPopulation){
        speciesCollection.put(s, newPopulation);
    }
    
    public Species[] getSpeciesList()
    {
        Species[] list = new Species[speciesCollection.size()];
        int index = 0;
        for (Enumeration<Species> sp = speciesCollection.keys(); sp.hasMoreElements();) {
            list[index++] = sp.nextElement();
        }
        return list;
    }
    
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
    
    public void updateSpecies(Species s, int newPopulation){
        speciesCollection.put(s, newPopulation);
    }
    
    public int getPopulation(Species s){
        return speciesCollection.get(s);
    }

    public static StateList makeClone(StateList source){
        StateList newStateList = new StateList();
               
        for (Enumeration<Species> sc = source.speciesCollection.keys(); sc.hasMoreElements();) {
            Species s = sc.nextElement();
            newStateList.updateSpecies(s, source.speciesCollection.get(s));
        }
        return newStateList;
    }
       
    public String getSpeciesNameString(){
        StringBuilder result = new StringBuilder();
        for (Enumeration<Species> s = speciesCollection.keys(); s.hasMoreElements();) {
            Species retrieveSpecies = s.nextElement();
            result.append(retrieveSpecies + "\t");
        }
        return result.toString();
    }
    
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
