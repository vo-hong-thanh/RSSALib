/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hong Thanh
 */
public class MStateList {

    private ConcurrentHashMap<Species, ArrayList<Integer> > speciesCollection;

    public MStateList() {
        speciesCollection = new ConcurrentHashMap<Species, ArrayList<Integer>>();
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
    
    public void addSpecies(Species s, ArrayList<Integer> populationList)
    {
        speciesCollection.put(s, populationList);
    }
    
    public void updatePopulation(Species s, int position, int newPopulation)
    {
        speciesCollection.get(s).set(position, newPopulation);
    }
    
    public int getPopulation(Species s, int position)
    {
        return speciesCollection.get(s).get(position);
    }

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
