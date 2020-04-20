/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.gillespie.cr;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import utils.ComputingMachine;
import utils.DataWriter;
import java.util.Vector;
import simulator.IAlgorithm;

/**
 * SSA_CR: DM with composition-rejection search
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class SSA_CR  implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();
    
    //current time
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
    
    //data structure for CR
    private int maxGroupExponent;
    private int minGroupExponent;
    private LinkedList<CRBlock> groups;
    
    private Hashtable<Integer, Double> mapReactionToPropensity = new Hashtable();
    private double totalPropensity = 0;

    //output
    private Hashtable<String, Vector<Double> > simOutput;
    
    //data tracking
    private boolean willWriteFile;
    private String outputFile;
    private DataWriter dataWriter = null;
    private DataWriter performanceWriter = null;
    
    public void loadModel(String modelFilename) throws Exception {        
        //build population
        ComputingMachine.buildModelFromFile(modelFilename, states, reactions);

        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);
                
        //build propensity list
        buildCRGroupList();        
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Composition-Rejection SSA");
        
//        System.out.println("---------------------------------------------------");
//        System.out.println("------------ Model information --------------------");
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions.toStringFull());
//
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
//                System.out.println("   + reaction " + reactionIndex + " propensity " + mapReactionToPropensity.get(reactionIndex));
//            }
//            
//            System.out.println(" => group sum: " + g.getBlockSum());
//        }
//        System.out.println("---------------------------------------------------");
        //initialize output
        initalizeSimulation(_maxTime, 0, _logInterval, _isWritingFile, _outputFilename);

        //do sim
        long simTime = 0;
        long updateTime = 0;
        long searchTime = 0;

        //start simulation
        long startSimTime = System.currentTimeMillis();
        do {
//            System.out.println("Total propensity: " + totalPropensity);
            double delta = ComputingMachine.computeTentativeTime(rand, totalPropensity);
//            System.out.println("=> delta: " + delta);            

            //update time
            currentTime += delta;

            if (!simulationByStep && currentTime >= maxTime) {
                currentTime = maxTime;
                
                if (currentTime >= logPoint) {
                    //output
                    simOutput.get("t").add(logPoint);                
                    for (Species s : states.getSpeciesList()) {
                        int pop = states.getPopulation(s);
                        simOutput.get(s.getName()).add((double)pop);
                    }
                }
                break;
            }

            //search reaction firing
            int groupIndex = -1;
            int fireReactionIndex = -1;
            
            long startSearchTime = System.currentTimeMillis();            
            
            //select a group via linear search
            double searchValue = rand.nextDouble()*totalPropensity;
            double partialGroupSum = 0;
            for(groupIndex = 0; groupIndex < groups.size(); groupIndex++){
                partialGroupSum += groups.get(groupIndex).getBlockSum();
                if(partialGroupSum >= searchValue) {
                    break;
                }
            }
    //        System.out.println(" + found group " + groupIndex);  

            //select reaction in group via rejection
            CRBlock g = groups.get(groupIndex);
            ArrayList<Integer> reactionInGroup = g.getReactionInBlock();

            double maxGroupPropensity = g.getMaxBlockValue();
            
            int indexInGroup = -1;
            double randomCheckingValue;

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
                    if (randomCheckingValue <= mapReactionToPropensity.get(fireReactionIndex)) {
    //                        System.out.println(" + found reaction " + reactionIndex);
                       break;
                    }
                }
            }

            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

            firing++;
//            System.out.println("Current Step: " + firing + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
//
//            //state
//            System.out.print("State list: ");
//            System.out.println(states);
        
            //update array of propensity            
            long startUpdateTime = System.currentTimeMillis();

            for (int reactionIndex : reactions.getReaction(fireReactionIndex).getDependent()) {
                double oldPropensity = mapReactionToPropensity.get(reactionIndex);
                double newPropensity = ComputingMachine.computePropensity(reactions.getReaction(reactionIndex), states);
                updateCRGroupList(reactionIndex, oldPropensity, newPropensity);
                mapReactionToPropensity.put(reactionIndex, newPropensity);
            }

            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);

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
            
            performanceWriter.writeLine("Time\tFiring\tRunTime\tSearchTime\tUpdateTime");
            performanceWriter.writeLine(currentTime + "\t" + firing + "\t" + simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        } 

        return simOutput;
    }

    private void buildCRGroupList() {
//        System.out.println(" --- Build group ---");        
        groups = new LinkedList<CRBlock>();

        Reaction[] list = reactions.getReactionList();
        boolean oneNonZeroPropensity = false;

        for (Reaction r : list) {
            int reactionIndex = r.getReactionIndex();
            double propensity = ComputingMachine.computePropensity(r, states);

//            System.out.println(" process reaction: " + reactionIndex + " with propensity: " + propensity); 
            
            mapReactionToPropensity.put(reactionIndex, propensity);

            if (!oneNonZeroPropensity) {
                if (propensity > 0.0) {
//                    System.out.println(" (This is first non-zero propensity) "); 
                    oneNonZeroPropensity = true;

                    int exponent = ComputingMachine.computeGroupExponent(propensity);
            
                    minGroupExponent = exponent;
                    maxGroupExponent = exponent;

                    CRBlock newGroup = new CRBlock(exponent);
                    newGroup.insert(reactionIndex, propensity);

//                    System.out.println(" put it to group " + getGroupIndex(propensity)); 
            
                    groups.addFirst(newGroup);
                    
                }
            } else {
                int newGroupIndex = getGroupIndex(propensity);
                
//                System.out.println(" tentative group index " + newGroupIndex); 
            
                if (newGroupIndex == -1) {
                    //either need to add new group or do nothing (propensity = 0)
                    if (propensity != 0.0) {
                        addGroup(ComputingMachine.computeGroupExponent(propensity));
                        newGroupIndex = getGroupIndex(propensity);//group index changed
                        groups.get(newGroupIndex).insert(reactionIndex, propensity);
                        
//                        System.out.println(" since it does not exist => create a new group " + newGroupIndex); 
                    }
                }else { // insert new reaction into group
//                    System.out.println(" since it exists => put to the group " + newGroupIndex); 
            
                    groups.get(newGroupIndex).insert(reactionIndex, propensity);
                }                
//                if (propensity > 0.0) {
//                    updateCRGroupList(reactionIndex, 0.0, propensity);
//                }
            }
            totalPropensity += propensity;
        }

        if (!oneNonZeroPropensity) {
            throw new RuntimeException("All propensities are zero.");
        }
    }

    private void updateCRGroupList(int reactionIndex, double oldPropensity, double newPropensity) {
//        System.out.println(" --- update reaction : " + reactionIndex);
        
        totalPropensity += (newPropensity - oldPropensity);

        int oldGroupIndex = getGroupIndex(oldPropensity);
        int newGroupIndex = getGroupIndex(newPropensity);

//        System.out.println(" change propensity from: " + oldPropensity + " (group index: " + oldGroupIndex +")" + " to: " + newPropensity + " (group index: " + newGroupIndex +")");
        
        if (newGroupIndex == oldGroupIndex) {
            if (newGroupIndex == -1) {
                
                //either need to add new group or do nothing (propensity = 0)
                if (newPropensity != 0.0) {
                    addGroup(ComputingMachine.computeGroupExponent(newPropensity));
                    newGroupIndex = getGroupIndex(newPropensity);//group index changed
                    
//                    System.out.println(" => create new group " + newGroupIndex);
                    
                    groups.get(newGroupIndex).insert(reactionIndex, newPropensity);
                }
            } else {
//                System.out.println(" => update group " + newGroupIndex);
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
