/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import model.rates.KINETIC_TYPE;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import model.DELAY_TYPE;
import model.DelayInfo;
import model.MStateList;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import model.rates.ExhibitoryHillKinetics;
import model.rates.IRateLaw;
import model.rates.InhibitoryHillKinetics;
import model.rates.MMKinetics;
import model.rates.MassActionKinetics;

/**
 * ComputingMachine: utility for computing
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class ComputingMachine {    
    public static final double BASE = 2;
    
    //compute exponent
    public static int computeGroupExponent(double propensityValue) {
        int exponent = (int) Math.ceil(Math.log(propensityValue) / Math.log(BASE));
        return exponent;
    }    
    
    //compute tentative time
    public static double computeTentativeTime(Random rand) {
        return Math.log(1/rand.nextDouble());
    }
    
    public static double computeTentativeTime(Random rand, double propensity) {
        double time = Double.MAX_VALUE;

        if (propensity >= 0.0) {
            time = Math.log(1/rand.nextDouble())/propensity;
        }
        return time;
    }
    
    public static double computeTentativeTime(int k, Random rand) {
        double random = 1;
        for (int i = 0; i < k; i++) {
            random *= rand.nextDouble();
        }

        return Math.log(1 / random);
    }
    
    public static double computeTentativeTime(int k, Random rand, double propensity) {
        double random = 1;
        for (int i = 0; i < k; i++) {
            random *= rand.nextDouble();
        }

        double time = Double.MAX_VALUE;
        if (propensity >= 0.0) {
            time = Math.log(1 / random) / propensity;
        }

        return time;
    }
    
    public static double computePropensity(Reaction r, StateList stateList) {
        return ComputingMachine.computeRate(r, stateList)*computeCombination(r, stateList);
    }
    
    public static double computePropensity(Reaction r, MStateList mstates, int position) {
        return computeRate(r, mstates, position)*computeCombination(r, mstates, position);
    }
    
    public static long computeCombination(Reaction r, StateList stateList){
        ArrayList<Term> reactants = r.getReactants();
        
        //zeroth reaction
        if(reactants.isEmpty()){
            return 1;
        }
        //other reaction types
        else{
            long numerator = 1;
            long denominator = 1;
            for (Term reactant : reactants) {
                int pop = stateList.getPopulation(reactant.getSpecies());
                int coff = -reactant.getCoff();
                int base = pop - coff + 1;
                
                if (pop <= 0 || base <= 0) {
                    return 0;
                } else {
                    for (int run = base; run <= pop; run++) {
                        numerator *= run;
                    }
                    for (int run = 2; run <= coff; run++) {
                        denominator *= run;
                    }
                }
            }
            return numerator / denominator;
        }        
    }
    
    public static long computeCombination(Reaction r, MStateList mstates, int position){
        ArrayList<Term> reactants = r.getReactants();
        
        //zeroth reaction
        if(reactants.isEmpty()){
            return 1;
        }
        //other reaction types
        else{
            long numerator = 1;
            long denominator = 1;
            for (Term reactant : reactants) {
                int pop = mstates.getPopulation(reactant.getSpecies(), position);
                int coff = -reactant.getCoff();
                int base = pop - coff + 1;
                
                if (pop <= 0 || base <= 0) {
                    return 0;
                } else {
                    for (int run = base; run <= pop; run++) {
                        numerator *= run;
                    }
                    for (int run = 2; run <= coff; run++) {
                        denominator *= run;
                    }
                }
            }
            return numerator / denominator;
        }        
    }

    public static double computeRate(Reaction r, StateList stateList) {
        return r.getRateLaw().evaluateRate(stateList);        
    }
    
    private static double computeRate(Reaction r, MStateList mstates, int position) {
        return r.getRateLaw().evaluateRate(mstates, position);
    }
    
    //execute reaction
    public static void executeReaction(int fireReactionIndex, ReactionList reactions, StateList states) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        //update population
        for (Term reactant : r.getReactants()) {
            states.updateSpecies(reactant.getSpecies(), states.getPopulation(reactant.getSpecies()) + reactant.getCoff());
        }
        for (Term product : r.getProducts()) {
            states.updateSpecies(product.getSpecies(), states.getPopulation(product.getSpecies()) + product.getCoff());
        }
    }
    
    public static void executeReaction(int fireReactionIndex, ReactionList reactions, MStateList mstates, int position) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        //update population
        for (Term reactant : r.getReactants()) {
                mstates.updatePopulation(reactant.getSpecies(), position, mstates.getPopulation(reactant.getSpecies(), position) + reactant.getCoff());
        }
        for (Term product : r.getProducts()) {
            mstates.updatePopulation(product.getSpecies(), position, mstates.getPopulation(product.getSpecies(), position) + product.getCoff());
        }
    }
   
    public static HashSet<Species> executeReaction(int fireReactionIndex, ReactionList reactions, MStateList mstates, int position, StateList lowerState, StateList upperState) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;
        int updatedPop;
                
        HashSet<Species> updateSpecies = new HashSet<Species>();

        //update population
        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();
            
            updatedPop = mstates.getPopulation(s, position) + reactant.getCoff() ;
                        
            mstates.updatePopulation(s, position, updatedPop);
            
            if ( (updatedPop != 0 && updatedPop < lowerState.getPopulation(s)) || updatedPop <= 0) {
                updateSpecies.add(s);
            }
        }

        for (Term product : r.getProducts()) {
            s = product.getSpecies();

            updatedPop = mstates.getPopulation(s, position) + product.getCoff();
                        
            mstates.updatePopulation(s, position, updatedPop);
            
            if (!s.isProductOnly() && updatedPop > upperState.getPopulation(s)) {
                updateSpecies.add(s);
            }
        }

        return updateSpecies;
    }
    
    //update reactant
    public static void executeDelayReactionReactant(int fireReactionIndex, ReactionList reactions, StateList states) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        
        //update reactant population
        for (Term reactant : r.getReactants()) {
//            if (states.getPopulation(reactant.getSpecies()) + reactant.getCoff() <= 0) {
//                states.updateSpecies(reactant.getSpecies(), 0);
//            } else {
                states.updateSpecies(reactant.getSpecies(), states.getPopulation(reactant.getSpecies()) + reactant.getCoff());
//            }
        }
    }
    
    //update reactant
    public static void executeDelayReactionProduct(int fireReactionIndex, ReactionList reactions, StateList states) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        
        //update product population
        for (Term product : r.getProducts()) {
            states.updateSpecies(product.getSpecies(), states.getPopulation(product.getSpecies()) + product.getCoff());
        }      
    }
    
    //build model
    public static void buildModelFromFile(String modelFilename, StateList states, ReactionList reactions) throws Exception{
        Hashtable<String, Double> parameters = new Hashtable<>();
        
        //read initial state vector
        int reactionIndex = 1;
        
        BufferedReader pReader = new BufferedReader(new FileReader(modelFilename));
        String dataLine = null;
        
        while ( (dataLine = pReader.readLine()) != null){
//            System.out.println("read : " + dataLine);
            
            //skip space and comment
            if (dataLine.equals("") || dataLine.startsWith("#")) {
                continue;
            }
            
            dataLine = dataLine.trim();
            //parse data
            if(dataLine.contains("=")){
//                System.out.println("+build parameter");
                //parse parameter
                StringTokenizer spo = new StringTokenizer(dataLine, "=");
                String parameterName = spo.nextToken().trim();
                double value = Double.parseDouble(spo.nextToken().trim());
                parameters.put(parameterName, value);   
//                System.out.println(" => set parameter = " + parameterName + ", value = " + parameters.get(parameterName));    
            }
            else if(dataLine.contains("->")){
//                System.out.println("+build reaction with info: " + dataLine);
                
                //parse reaction
                int indexofTo = dataLine.indexOf("->");
                               
                //build reactants
                String reactantData = dataLine.substring(0, indexofTo);
//                System.out.println(" -build reactants");
                ArrayList<Term> reactants = buildPartofReaction(parameters, states, reactantData.trim(), -1);                
                for(Term t : reactants){
                    t.getSpecies().setIsProductOnly(false);
                }
                
                int commasIndex;                
                String restData = dataLine.substring(indexofTo + 2);
                
                commasIndex = restData.indexOf(',');
                //build products
//                System.out.println(" -build products");
                ArrayList<Term> products = buildPartofReaction(parameters, states, restData.substring(0, commasIndex).trim(), 1);
                
                restData = restData.substring(commasIndex + 1).trim();
                
                String rateData = "";
                String delayData = "";
                if(restData.startsWith(KINETIC_TYPE.MASS_ACTION) || restData.startsWith(KINETIC_TYPE.MICHAELIS_MENTEN) 
                || restData.startsWith(KINETIC_TYPE.HILL) || restData.startsWith(KINETIC_TYPE.INHIBITORYHILL)) {
                    int closeParenthesisIndex = restData.indexOf(')');
                    rateData = restData.substring(0, closeParenthesisIndex + 1);
                    
                    restData = restData.substring(closeParenthesisIndex + 1);
                    
                    commasIndex = restData.indexOf(',');
                    delayData = restData.substring(commasIndex + 1);
                }
                else{
                    commasIndex = restData.indexOf(',');
                    
                    if(commasIndex == -1){
                        rateData = restData;                        
                    }
                    else{
                        rateData = restData.substring(0, commasIndex);
                        delayData = restData.substring(commasIndex + 1);
                    }                    
                }
                
                //build rate
//                System.out.println(" - build rate " + rateData);
                IRateLaw rate = buildRateLaw(parameters, reactants, rateData.trim());
//                    System.out.println(" -> rate: " + rate.toString());    
                
                DelayInfo delay = null;
                if(delayData.equals("")){
                    delay = new DelayInfo();
                }
                else{
                    delay = buildDelay(parameters, delayData.trim());
                }
                
                Reaction r = new Reaction(reactionIndex, reactants, products, rate, delay);
//                System.out.println(" -> Reaction: " + r.toString());
                
                reactions.addReaction(r);
                reactionIndex++;
            }        
        }
    }
    
    //build parts of reaction   
    private static ArrayList<Term> buildPartofReaction(Hashtable<String, Double> parameters, StateList states, String part, int mul) {
//        System.out.println("proceed " + part);        
        ArrayList<Term> partTerm = new ArrayList<Term>();
        if (part.trim().equals("_")) {
            return partTerm;
        }

        StringTokenizer tokens = new StringTokenizer(part, "+");
        while (tokens.hasMoreTokens()) {
            String piece = tokens.nextToken().trim();
            int i;
            for (i = 0; i < piece.length(); i++) {
                if (Character.isLetter(piece.charAt(i))) {
                    break;
                }
            }
            
            String name = piece.substring(i).trim();
            int coff = mul;
            if (!piece.substring(0, i).equals("")) {
                coff *= Integer.parseInt(piece.substring(0, i));
            }
            
            int pop = 0;
            if(parameters.containsKey(name)){
                pop = parameters.get(name).intValue();
            }
                        
//            System.out.print("  species name = " + name + ", pop = " + pop );            
            Species s = states.getSpecies(name);
            if(s == null){
                s = new Species(name);
                states.addSpecies(s, pop);
//                System.out.println("  => new species (add to state species " + s + ", pop = " + pop + ")");  
            }
//            else{
//                System.out.println("  => already in srate");  
//            }
            partTerm.add(new Term(s, coff));
        }
        return partTerm;
    }
    
    //build rate
    public static IRateLaw buildRateLaw(Hashtable<String, Double> parameters, ArrayList<Term> reactants, String rateInfo){
        if(rateInfo.indexOf('(') == -1){
//            System.out.print("  build mass-action kinetics by default");  
            return buildMassActionKinetics(parameters, rateInfo);            
        }
        else{
            int openParenthesisIndex = rateInfo.indexOf('(');
            int closeParenthesisIndex = rateInfo.indexOf(')');
        
            String kineticInfo = rateInfo.substring(0, openParenthesisIndex).trim();
            String rateData = rateInfo.substring(openParenthesisIndex+1, closeParenthesisIndex).trim();
            
            if(kineticInfo.equals(KINETIC_TYPE.MASS_ACTION)){
//                System.out.print("  build mass-action kinetics ");  
                return buildMassActionKinetics(parameters, rateData);                 
            }
            else if(kineticInfo.equals(KINETIC_TYPE.MICHAELIS_MENTEN)){
//                System.out.print("  build Michaelis-Menten kinetics ");  
                return buildMichaelisMentenKinetics(parameters, reactants, rateData);                
            }
            else if(kineticInfo.equals(KINETIC_TYPE.HILL)){
//                System.out.print("  build Hill kinetics "); 
                return buildHillKinetics(parameters, reactants, rateData);  
            }
            else if(kineticInfo.equals(KINETIC_TYPE.INHIBITORYHILL)){
//                System.out.print("  build Hill kinetics "); 
                return buildInhibitoryHillKinetics(parameters, reactants, rateData);  
            }
            else{
                throw new UnsupportedOperationException("Not yet supported kinetics");
            }           
        }
    }
    
    //build specific rate
    private static IRateLaw buildMassActionKinetics(Hashtable<String, Double> parameters, String rateData) {
//        System.out.print("  build mass-action kinetics with value " + rateData);  
        double rateConstant = getParameterValue(parameters, rateData);        
        MassActionKinetics massRate = new MassActionKinetics(rateConstant);
        
//        System.out.println( massRate.toString());  
        
        return massRate;
    }

    private static IRateLaw buildMichaelisMentenKinetics(Hashtable<String, Double> parameters, ArrayList<Term> reactants, String rateData) {
        StringTokenizer tokens = new StringTokenizer(rateData, ",");
        
        String substrateName = tokens.nextToken().trim();
        
        double Vm = getParameterValue(parameters, tokens.nextToken().trim());
        double Km = getParameterValue(parameters, tokens.nextToken().trim());        
                
        for(int i = 0; i < reactants.size(); i++){
            Species s = reactants.get(i).getSpecies();
            if(s.getName().equals(substrateName)){
                MMKinetics mmRate = new MMKinetics(s, Vm, Km);
                
//                System.out.println( mmRate.toString());
                
                return mmRate;
            }
        }       
       
        return null;
    }

    private static IRateLaw buildHillKinetics(Hashtable<String, Double> parameters, ArrayList<Term> reactants, String rateData) {
        StringTokenizer tokens = new StringTokenizer(rateData, ",");
        
        String proteinName = tokens.nextToken().trim();                
        double baseRate = getParameterValue(parameters, tokens.nextToken().trim());
        
        double n = getParameterValue(parameters, tokens.nextToken().trim());        
        double threshold = getParameterValue(parameters, tokens.nextToken().trim());               
              
        for(int i = 0; i < reactants.size(); i++){
            Species s = reactants.get(i).getSpecies();
            if(s.getName().equals(proteinName)){
                ExhibitoryHillKinetics hillRate = new ExhibitoryHillKinetics(s, baseRate, n, threshold);
                
//                System.out.println( hillRate.toString());
                
                return hillRate;
            }
        }
        return null;    
    }
    
    private static IRateLaw buildInhibitoryHillKinetics(Hashtable<String, Double> parameters, ArrayList<Term> reactants, String rateData) {
        StringTokenizer tokens = new StringTokenizer(rateData, ",");
        
        String proteinName = tokens.nextToken().trim();                
        double baseRate = getParameterValue(parameters, tokens.nextToken().trim());
        double n = getParameterValue(parameters, tokens.nextToken().trim());        
        double threshold = getParameterValue(parameters, tokens.nextToken().trim());               
              
        for(int i = 0; i < reactants.size(); i++){
            Species s = reactants.get(i).getSpecies();
            if(s.getName().equals(proteinName)){
                InhibitoryHillKinetics hillRate = new InhibitoryHillKinetics(s, baseRate, n, threshold);                
//                System.out.println( hillRate.toString());
                
                return hillRate;
            }
        }
        return null;    
    }
    
    //build delay
    private static DelayInfo buildDelay(Hashtable<String, Double> parameters, String delayData) {
        DelayInfo delay = null;
        
        int openParenthesisIndex = delayData.indexOf('(');
        int closeParenthesisIndex = delayData.indexOf(')');
            
        String type = delayData.substring(0, openParenthesisIndex).trim();
        double value = getParameterValue(parameters, delayData.substring(openParenthesisIndex+1, closeParenthesisIndex).trim());                

        if(type.equals(DELAY_TYPE.CONSUMING)) {//consuming reaction            
            delay = new DelayInfo(DELAY_TYPE.CONSUMING, value);            
        }
        else if(type.equals(DELAY_TYPE.NONCONSUMING)){ //non-consuming reaction
            delay = new DelayInfo(DELAY_TYPE.NONCONSUMING, value);             
        }
        else{
            throw new RuntimeException("Unknown delay type!");
        }
        
        return delay;        
    }
    
    //build dependency Graph and Priority Queue
    public static void buildReactionDependency(ReactionList reactions) {
        Reaction[] list = reactions.getReactionList();
        
        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add dependency to other reaction, allowed self reference
            for (Reaction o : list) {
                if (r.equals(o) || r.affect(o)) {
                    r.addDependentReaction(o.getReactionIndex());
                }
            }
        }
    }
    
    //build bipertie graph
    public static void buildSpecieReactionDependency(ReactionList reactions, StateList states) {
        Reaction[] list = reactions.getReactionList();

        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add dependency to other reaction, allowed self reference
            for(Term t : r.getReactants()){
                Species s = t.getSpecies();
                states.getSpecies(s.getName()).addAffectReaction(r.getReactionIndex());
            }            
        }
    }    
    
    //get value of parameter       
    private static double getParameterValue(Hashtable<String, Double> parameters, String paraName){
        double paraValue;
        if(isNumeric(paraName)){
            //numeric data
            paraValue = Double.parseDouble(paraName);
        }
        else{
            //non-numeric data - look up it from parameters
            if(parameters.get(paraName) == null)
            {
                throw new RuntimeException(paraName + " has not been defined");
            }
            paraValue = parameters.get(paraName);
        }
        return paraValue;
    }
    
    //check numeric
    private static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?(e(-|\\+)?\\d+)?");
    }    
}
