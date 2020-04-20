package model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Term: species and its stoichiomtric coefficient in a reaction 
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class Term {
   /**
    * species
    */ 
    private  Species species;
    
    /**
     * stoichiometric coefficient
     */
    private  int     stoichiometric;
    
    /**
     * Create a new Term 
     * @param species 
     * @param stoichiometric
     */            
    public Term(Species species, int stoichiometric)
    {
        this.species = species;
        this.stoichiometric = stoichiometric;
    }
    
    /**
     * set stoichiometric coefficient
     * @param coff
     */            
    public void setCoff(int coff)
    {
        stoichiometric = coff;
    }
    
    /**     
     * @return stoichiometric coefficient
     */   
    public int getCoff()
    {
        return stoichiometric;
    }
    
    /**
     * @return species      
     */   
    public Species getSpecies() {
        return species;
    }
    
    /**
     * set species
     * @param s
     */   
    public void setSpecies(Species s) {
        species = s;
    }
    
    @Override
    public String toString()
    {
        return "(" + stoichiometric+")*"+species;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof Term)
        {
            Term cast = (Term)o;
            if(species.equals(cast.species) && stoichiometric == cast.stoichiometric)
                return true;
        }
        return false;
    }
}
