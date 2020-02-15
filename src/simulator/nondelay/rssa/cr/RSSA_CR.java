/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa.cr;

import simulator.nondelay.gillespie.cr.CRBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import utils.ComputingMachine;
import utils.DataWriter;
import java.util.Vector;
import model.rates.InhibitoryHillKinetics;
import simulator.IAlgorithm;
import simulator.nondelay.rssa.RSSANode;

/**
 *
 * @author vo
 */
public class RSSA_CR implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();

    //simulation time
    private double currentTime = 0;
    
    //simulation time
    private double maxTime = 0;
    private long maxStep = 0;
    private boolean simulationByStep = false;
    
    //logging
    private double logPoint;
    private double logInterval;
    
    //number of reaction firings
    private long firing = 0;
    
    private long updateStep = 0;
    private long totalTrial = 0;
    
    //RSSA
    private double fluctuationRate = 0.1;
    private int threshold = 25;
    private int adjustSize = 4;
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    //Composition-Rejection
    private int maxGroupExponent;
    private int minGroupExponent;
    private LinkedList<CRBlock> groups;
    
    private Hashtable<Integer, RSSANode> mapReactionToNode = new Hashtable();
    private double totalMaxPropensity = 0;
    
    //output
    private Hashtable<String, Vector<Double> > simOutput;
    
    //data tracking
    private boolean willWriteFile;
    private String outputFile;
    private DataWriter dataWriter = null;
    private DataWriter performanceWriter = null;
    
    @Override
    public void loadModel(String modelFilename) throws Exception {
        //build model
        ComputingMachine.buildModelFromFile(modelFilename, states, reactions);
        
        //build bipartie dependency
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build group
        buildCRGroupList();        
    }

    @Override
    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean __isWritingFile, String _outputFilename) throws Exception {
        System.out.println("RSSA with CR search and Fluctuation Interval [ (1 -/+ " + fluctuationRate + ")#X ]");        
//        System.out.println("---------------------------------------------------");//   
//        System.out.println(" Model information ");     
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Upper bound state list: ");
//        System.out.println(upperStates);
//        
//        System.out.print("Lower bound state list: ");
//        System.out.println(lowerStates);
//
//        System.out.print("Reaction list: ");
//        System.out.println(reactions.toStringFull());
//
//        //group information
//        System.out.println("Group information");
//        for(int groupIndex = 0; groupIndex < groups.size(); groupIndex++)
//        {
//            CRBlock g = groups.get(groupIndex);
//            System.out.println("Group " + groupIndex);
//            
//            System.out.println(" - group exponent: " + g.getBlockExponent());
//            
//            System.out.println(" - contain reaction : "); 
//            for(int reactionIndex : g.getReactionInBlock())
//            {
//                System.out.println("   + reaction " + reactionIndex + " max propensity " + mapReactionToNode.get(reactionIndex).getMaxPropensity() + " min propensity " + " max propensity " + mapReactionToNode.get(reactionIndex).getMinPropensity());
//            }
//            
//            System.out.println(" => group sum: " + g.getBlockSum());
//        }
//
//        System.out.println("---------------------------------------------------");

        //initialize output
        initalizeSimulation(_maxTime, 0, _logInterval, __isWritingFile, _outputFilename);

        //do sim
        long simTime = 0;
        long searchTime = 0;
        long updateTime = 0;

        double searchValue;
        double acceptantProb;
        double randomCheckingValue; 
        
        int numTestOnGroup;
        int numTestOnReaction;  
          
        long startSimTime = System.currentTimeMillis();
        do{            
            numTestOnGroup = 0;
            numTestOnReaction = 1;
            
            int groupIndex = -1;
            int fireReactionIndex = -1;
            
            long startSearchTime = System.currentTimeMillis();
            while (true) {
                //propensity is too small => stop simulation
                if(totalMaxPropensity < 1e-7){
                    break;
                }                
                //select a group via linear search
                searchValue = rand.nextDouble()*totalMaxPropensity;
                double partialGroupSum = 0;        
                for(groupIndex = 0; groupIndex < groups.size(); groupIndex++){
                    partialGroupSum += groups.get(groupIndex).getBlockSum();
                    if(partialGroupSum >= searchValue){
                        break;
                    }
                }
        //        System.out.println(" + found group " + groupIndex);  

                //select candidate reaction in group via rejection
                CRBlock g = groups.get(groupIndex);
                ArrayList<Integer> reactionInGroup = g.getReactionInBlock();
                double maxGroupPropensity = g.getMaxBlockValue();
                int indexInGroup = -1;

                if (reactionInGroup.size() == 1) { //if there is only one reaction in the group, choose it
                    fireReactionIndex = reactionInGroup.get(0);
                } else {
                    while (true) {
                        //generate an integer index between 0 and size of group-1 as tentative reaction index 
                        indexInGroup = (int) (rand.nextDouble() * reactionInGroup.size());

                        //generate a continuous number within the group's propensity range
                        randomCheckingValue = rand.nextDouble() * maxGroupPropensity;
                        
                        //if randomCheckingValue is less than the propensity of tentativeReaction propensity then accept the reaction
                        fireReactionIndex = reactionInGroup.get(indexInGroup);
                        if (randomCheckingValue <= mapReactionToNode.get(fireReactionIndex).getMaxPropensity()) {
        //                    System.out.println(" + found reaction " + reactionIndex);
                            break;
                        }
                        numTestOnGroup++;
                    }
                }
                
                //rejection test on candidate
                acceptantProb = rand.nextDouble();
//                System.out.println(" - acceptance prob. " + acceptantProb);

                RSSANode node = mapReactionToNode.get(fireReactionIndex);
//                System.out.println(" - min propensity: " + node.getMinPropensity() + ", max propensity: "+node.getMaxPropensity() );

                if (node.getMinPropensity() != 0 && acceptantProb <= node.getMinPropensity() / node.getMaxPropensity()) {
//                    System.out.println("  => accepted by squeeze ");
                    break;
                }

                double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(fireReactionIndex), states);
//                System.out.println(" - curent propensity: " + currentPropensity );
                if (currentPropensity != 0 && acceptantProb <= currentPropensity / node.getMaxPropensity()) {
//                    System.out.println("  => accepted by evaluate propensity");
                    break;
                }
                numTestOnReaction++;
            }
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            double delta = ComputingMachine.computeTentativeTime(numTestOnReaction, rand, totalMaxPropensity);
//            System.out.println("total max propensity: " + totalMaxPropensity + "=> delta: " + delta);
            
            //update time
            currentTime += delta;
            if(!simulationByStep && currentTime >= maxTime){
                currentTime = maxTime;
                //output
                simOutput.get("t").add(logPoint);                
                for (Species s : states.getSpeciesList()) {
                    int pop = states.getPopulation(s);
                    simOutput.get(s.getName()).add((double)pop);
                }
                break;
            }

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

            //update propensity array as necessary
            long startUpdateTime = System.currentTimeMillis();
            if(isUpdateByReaction(fireReactionIndex)){
                updateStep++;
            }            
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);
            
            //update step
            totalTrial += (numTestOnGroup + numTestOnReaction);
            firing++;

//            System.out.println("Step: " + firing + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
//            System.out.println("Reaction fired " + fireReactionIndex);
//            //print trace
//            System.out.print("State list: ");
//            System.out.println(states);
            
            //log info
            if (currentTime >= logPoint) {
                //output
                simOutput.get("t").add(logPoint);                
                for (Species s : states.getSpeciesList()) {
                    int pop = states.getPopulation(s);
                    simOutput.get(s.getName()).add((double)pop);
                }
                                
                logPoint += logInterval;                
            }
//            System.out.println("---------------------------------------------------");
        }while( (!simulationByStep && currentTime < maxTime) || (firing < maxStep) );
        long endSimTime = System.currentTimeMillis();
        simTime = (endSimTime - startSimTime);        
        
        if(willWriteFile){            
            dataWriter = new DataWriter("(Data)" + outputFile);
            performanceWriter = new DataWriter("(Perf)" + outputFile);
            
            //write data
            Species[] speciesList = states.getSpeciesList();
            
            dataWriter.write("time" + "\t");
            for (Species s : speciesList) {
                dataWriter.write(s.getName() + "\t");
            }
            dataWriter.writeLine();

            Vector<Double> timeData = simOutput.get("t");            
            for(int i = 0; i < timeData.size(); i++){
                dataWriter.write(timeData.get(i) + "\t");  
                
                for (Species s : speciesList) {
                    int pop = simOutput.get(s.getName()).get(i).intValue();
                    dataWriter.write(pop +"\t");                    
                }
                dataWriter.writeLine();                
            }
                        
            //performance
            performanceWriter.writeLine("Time\tFiring\tTrial\tUpdate\tRunTime\tSearchTime\tUpdateTime");
            performanceWriter.writeLine(currentTime +"\t" + firing + "\t" +  totalTrial + "\t" +  updateStep + "\t" + simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }

        return simOutput;  
    }
    
    private void buildCRGroupList() {
//        System.out.println(" --- Build group ---");
        for (Species s : states.getSpeciesList()) {
            if(!s.isProductOnly())
            {
                computeIntervalSpecies(s);
            }
        }
                
        groups = new LinkedList<CRBlock>();

        Reaction[] list = reactions.getReactionList();
        boolean oneNonZeroPropensity = false;

        for (Reaction r : list) {
            int reactionIndex = r.getReactionIndex();
            
            double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
            double max_propensity = ComputingMachine.computePropensity(r, upperStates);

            if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                double temp = min_propensity;
                min_propensity = max_propensity;
                max_propensity = temp;
            }
            
            RSSANode node = new RSSANode(reactionIndex, min_propensity, max_propensity);
//            System.out.println(" process reaction: " + reactionIndex + " with max propensity: " + max_propensity); 

            mapReactionToNode.put(reactionIndex, node);

            if (!oneNonZeroPropensity) {
                if (max_propensity > 0.0) {
//                    System.out.println(" (This is first non-zero propensity) "); 
                    oneNonZeroPropensity = true;

                    int exponent = ComputingMachine.computeGroupExponent(max_propensity);
            
                    minGroupExponent = exponent;
                    maxGroupExponent = exponent;

                    CRBlock newGroup = new CRBlock(exponent);
                    newGroup.insert(reactionIndex, max_propensity);

//                    System.out.println(" put it to group " + getGroupIndex(max_propensity)); 
                    groups.addFirst(newGroup);                    
                }
            } else {
                int newGroupIndex = getGroupIndex(max_propensity);
//                System.out.println(" tentative group index " + newGroupIndex); 
          
                if (newGroupIndex == -1) {
                    //either need to add new group or do nothing (propensity = 0)
                    if (max_propensity != 0.0) {
                        addGroup(ComputingMachine.computeGroupExponent(max_propensity));
                        newGroupIndex = getGroupIndex(max_propensity);//group index changed

                        groups.get(newGroupIndex).insert(reactionIndex, max_propensity);
//                        System.out.println(" since it does not exist => create a new group " + newGroupIndex); 
                    }
                }else { // insert new reaction into group
//                    System.out.println(" since it exists => put to the group " + newGroupIndex); 
                    groups.get(newGroupIndex).insert(reactionIndex, max_propensity);
                }                
            }
            totalMaxPropensity += max_propensity;
        }

        if (!oneNonZeroPropensity) {
            throw new RuntimeException("All propensities are zero.");
        }
    }

    private boolean isUpdateByReaction(int fireReactionIndex){
        Reaction fireReaction = reactions.getReaction(fireReactionIndex);
        Species s;

        HashSet<Integer> needUpdateReactions = new HashSet<Integer>();

        //update population
        for (Term reactant : fireReaction.getReactants()) {
            s = reactant.getSpecies();
            int pop = states.getPopulation(s);
            
            if ( (pop != 0 && pop < lowerStates.getPopulation(s)) || pop <= 0) {
                computeIntervalSpecies(s);
                needUpdateReactions.addAll(s.getAffectReaction());
            }
        }

        for (Term product : fireReaction.getProducts()) {
            s = product.getSpecies();

            if (!s.isProductOnly() && states.getPopulation(s) > upperStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                needUpdateReactions.addAll(s.getAffectReaction());
            }
        }
        
        if(needUpdateReactions.isEmpty()){
            return false;
        }
        else{
            for (int reactionIndex : needUpdateReactions) {
                Reaction r = reactions.getReaction(reactionIndex);
                double new_min_propensity = ComputingMachine.computePropensity(r, lowerStates);
                double new_max_propensity = ComputingMachine.computePropensity(r, upperStates);

                if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                    double temp = new_min_propensity;
                    new_min_propensity = new_max_propensity;
                    new_max_propensity = temp;
                }
                
                RSSANode node = mapReactionToNode.get(reactionIndex);
                double old_max_propensity = node.getMaxPropensity();
                
                updateCRGroupList(reactionIndex, old_max_propensity, new_max_propensity);
                
                node.setMinPropensity(new_min_propensity);
                node.setMaxPropensity(new_max_propensity);
            }
            return true;
        }
    }
    
    private void updateCRGroupList(int reactionIndex, double oldPropensity, double newPropensity) {
//        System.out.println(" --- update reaction : " + reactionIndex);
        
        totalMaxPropensity += (newPropensity - oldPropensity);

        int oldGroupIndex = getGroupIndex(oldPropensity);
        int newGroupIndex = getGroupIndex(newPropensity);

//        System.out.println(" change max propensity from: " + oldPropensity + " (group index: " + oldGroupIndex +")" + " to: " + newPropensity + " (group index: " + newGroupIndex +")");
        
        if (newGroupIndex == oldGroupIndex) {
            if (newGroupIndex == -1) {
                
                //either need to add new group or do nothing (propensity = 0)
                if (newPropensity != 0.0) {
                    addGroup(ComputingMachine.computeGroupExponent(newPropensity));
                    newGroupIndex = getGroupIndex(newPropensity);//group index changed
                    
//                    System.out.println(" => create new group with index " + newGroupIndex);
                    
                    groups.get(newGroupIndex).insert(reactionIndex, newPropensity);
                }
            } else {
//                System.out.println(" => update group with index " + newGroupIndex);
                //did not change group, simple update
                groups.get(newGroupIndex).update(oldPropensity, newPropensity);
            }
        } else {//changed group
            //remove from old group
            if (oldGroupIndex != -1) {
//                System.out.println(" remove from group index " + oldGroupIndex);
                groups.get(oldGroupIndex).remove(reactionIndex, oldPropensity);
                //groups[oldGroupIndex].remove(reactionIndex,withinGroupIndexes[reactionIndex],withinGroupIndexes);
            }

            //add to new group
//            System.out.println(" to new group index " + newGroupIndex);
            if (newGroupIndex == -1) {
                if (newPropensity > 0) {
                    //need to add a group
                    addGroup(ComputingMachine.computeGroupExponent(newPropensity));
                    newGroupIndex = getGroupIndex(newPropensity);//group index changed
                    
//                    System.out.println("  => create new group " + newGroupIndex);
                    
                    groups.get(newGroupIndex).insert(reactionIndex, newPropensity);
                }
            } else {
//                System.out.println("  => insert reaction to group " + newGroupIndex);
                groups.get(newGroupIndex).insert(reactionIndex, newPropensity);
            }
        }
    }    

    private void addGroup(int newGroupExponent) {
        while (newGroupExponent < minGroupExponent) {
            CRBlock newGroup = new CRBlock(--minGroupExponent);
            groups.addLast(newGroup);
        }
        while (newGroupExponent > maxGroupExponent) {
            CRBlock newGroup = new CRBlock(++maxGroupExponent);
            groups.addFirst(newGroup);
        }
    }
    
    private int getGroupIndex(double propensityValue) {
        if (propensityValue == 0.0) {
            return -1;
        } else {
            int exponent = ComputingMachine.computeGroupExponent(propensityValue);
            if (exponent >= minGroupExponent && exponent <= maxGroupExponent) {
                return maxGroupExponent - exponent;
            } else {
                return -1;
            }
        }
    }

    private void computeIntervalSpecies(Species s) {        
        int pop = states.getPopulation(s);
        if (pop <= 0) {
            upperStates.updateSpecies(s, 0);
            lowerStates.updateSpecies(s, 0);
        } else if(pop < threshold) {
            upperStates.updateSpecies(s, pop + adjustSize );
            lowerStates.updateSpecies(s, ((pop - adjustSize) > 0 ? (pop - adjustSize) : 0) );
        }else {
            upperStates.updateSpecies(s, (int) (pop * (1 + fluctuationRate)) );
            lowerStates.updateSpecies(s, (int) (pop * (1 - fluctuationRate)) );
        }        
    }
    
    private void initalizeSimulation(double _maxTime, long _maxStep, double _logInterval, boolean __isWritingFile, String _outputFilename) {
        if(_maxStep > 0){
            maxStep = _maxStep;
            simulationByStep = true;
            maxTime = Double.MAX_VALUE;
        }
        else{
            maxStep = 0;
            simulationByStep = false;
            maxTime = _maxTime;
        }
        
        logInterval = _logInterval;
        logPoint = _logInterval; 
        
        //writer
        this.willWriteFile = __isWritingFile;     
        outputFile = _outputFilename;
               
        //output
        simOutput = new Hashtable<String, Vector<Double> >(); 
        
        simOutput.put("t", new Vector<>());        
        Species[] species = states.getSpeciesList();
        for(Species s : species){
            simOutput.put(s.getName(), new Vector<>());
        }
        
        simOutput.get("t").add(currentTime);       
        for (Species s : states.getSpeciesList()) {
            int pop = states.getPopulation(s);
            simOutput.get(s.getName()).add((double)pop);
        }        
    }
}
