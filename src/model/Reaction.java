package model;

import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.HashSet;
import model.rates.IRateLaw;
import model.rates.MassActionKinetics;
/**
 * Reaction
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class Reaction {
    private int reactionIndex;
    private ArrayList<Term> reactants;
    private ArrayList<Term> products;
    
    /**
     * reaction rate
     */
    private IRateLaw rate;
    
    /**
     * delay 
     */
    private DelayInfo delay;
        
    
    /**
     * dependent reactions
     */
    private HashSet<Integer> dependent = new HashSet<Integer>();

    /**
     * Create a new reaction 
     * @param index
     * @param reactants
     * @param products
     */
    public Reaction(int index, ArrayList<Term> reactants, ArrayList<Term> products) {
        this.reactionIndex = index;
        this.reactants = reactants;
        this.products = products;        
        //default mass-action kinetics
        this.rate = new MassActionKinetics(0);
        
        //no delay
        this.delay = new DelayInfo(DELAY_TYPE.NODELAY, 0);
    } 
    
    /**
     * Create a new reaction 
     * @param index
     * @param reactants
     * @param products
     * @param rate
     */    
    public Reaction(int index, ArrayList<Term> reactants, ArrayList<Term> products, IRateLaw rate) {
        this.reactionIndex = index;
        this.reactants = reactants;
        this.products = products;   
        this.rate = rate;
        
        //no delay
        this.delay = new DelayInfo(DELAY_TYPE.NODELAY, 0);
    }
    
    /**
     * Create a new reaction 
     * @param index
     * @param reactants
     * @param products
     * @param rate
     * @param delay
     */   
    public Reaction(int index, ArrayList<Term> reactants, ArrayList<Term> products, IRateLaw rate, DelayInfo delay) {
        this.reactionIndex = index;
        this.reactants = reactants;
        this.products = products;   
        this.rate = rate;
        this.delay = delay;
    }
    
    /**
     * @return reaction index
    */
    public int getReactionIndex() {
        return reactionIndex;
    }
    
    /**
     * @return reactants of the reaction
    */
    public ArrayList<Term> getReactants() {
        return reactants;
    }
    
    /**
     * @return products of the reaction
    */
    public ArrayList<Term> getProducts() {
        return products;
    }

    /**
     * check reactants of the reaction contian a species
     * @param s
     * @return true if the species is its reactant, otherwise false
    */
    public boolean reactantsContainSpecies(Species s) {
        for (Term r : reactants) {
            if (r.getSpecies().equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check products of the reaction contian a species
     * @param s
     * @return true if the species is its products, otherwise false
    */    
    public boolean productsContainSpecies(Species s) {
        for (Term pr : products) {
            if (pr.getSpecies().equals(s)) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * check if a reaction affect other reaction 
     * @param o: the other reaction 
     * @return true if the current reaction affects o, otherwise false
    */    
    public boolean affect(Reaction o) {
        boolean isAffect = false;

        for (Term r : reactants) {
            if (!isCatalyst(r) && o.reactantsContainSpecies(r.getSpecies())) {
                isAffect = true;
                break;
            }
        }

        for (Term p : products) {
            if (!isCatalyst(p) && o.reactantsContainSpecies(p.getSpecies())) {
                isAffect = true;
                break;
            }
        }

        return isAffect;
    }
    
    /**
     * check if a species is a catalyst
     * @param t: the species with its stoichiometric coefficient 
     * @return true if t is a catalyst of the current reaction, otherwise false
    */ 
    public boolean isCatalyst(Term t) {
        boolean result = false;

        int coffBackup = t.getCoff();
        
        t.setCoff(-coffBackup);
        if(coffBackup < 0 && products.contains(t)){
            result = true;            
        }
        else if(coffBackup > 0 && reactants.contains(t)){
            result = true;
        }
        
        t.setCoff(coffBackup);
        return result;
    }

    /**
     * add a reaction into to list of its dependence
     * @param index: the index of the dependent reaction     
    */ 
    public void addDependentReaction(int index) {
        dependent.add(index);
    }

    /**
     * @return the list of dependent reactions
    */ 
    public HashSet<Integer> getDependent() {
        return dependent;
    }

    /**
     * @return rate law of the reaction
    */ 
    public IRateLaw getRateLaw(){
        return rate;   
    }
    
    /**
     * @return delay information of the reaction
    */ 
    public DelayInfo getDelayInfo(){
        return delay;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(reactionIndex +": ");
        
        if(reactants.size() == 0){
            result.append("_ ");
        }
        else{
            for (Term t : reactants) {
                result.append(t.toString() + " + ");
            }
            result.delete(result.length() - 2, result.length());
        }
        result.append("->");

        if(products.size() == 0){
            result.append("_");
        }
        else{
            for (Term t : products) {
                result.append(t.toString() + " + ");
            }
            result.delete(result.length() - 2, result.length());
        }
        
        result.append(" , " + rate.toString() );
        
        if(delay.getDelayType().equals(DELAY_TYPE.NODELAY) )
        {
            result.append(" , " + "No delay" );
        }else
        {
            result.append(" , " + delay.getDelayType() + "(" + delay.getDelayTime() + ")" );
        }
        
        
        return result.toString();
    }    
}
