package model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hong Thanh
 */
public class Term {
    private  Species species;
    private  int     stoichiometric;
    
    public Term(Species species, int stoichiometric)
    {
        this.species = species;
        this.stoichiometric = stoichiometric;
    }
    
    public void setCoff(int coff)
    {
        stoichiometric = coff;
    }
    
    public int getCoff()
    {
        return stoichiometric;
    }
    
    public Species getSpecies() {
        return species;
    }
    
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
