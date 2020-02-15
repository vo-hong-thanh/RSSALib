/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.delay.delayed_rssa;

import utils.ComputingMachine;
import utils.DataWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import model.DELAY_TYPE;
import model.DelayInfo;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import model.rates.InhibitoryHillKinetics;
import simulator.IAlgorithm;
import simulator.delay.DelayedEventQueue;
import simulator.delay.DelayedReactionTime;
import simulator.nondelay.rssa.RSSANode;


/**
 *
 * @author Hong Thanh
 */
public class DelayedRSSA implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();

    //current simulation time
    private double currentTime = 0;
    
    //simulation time
    private double maxTime = 0;
    private long maxStep = 0;
    private boolean simulationByStep = false;
    
    //logging
    private double logPoint;
    private double logInterval;
    
    //step
    private int firing = 1;
    private int totalTrial = 1;
    private int updateStep = 0;
    private int delayStep = 0;
        
    //data structure for simulation
    private int threshold = 25;
    private int adjustSize = 4;    
    private double percentage = 0.1;
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    private RSSANode[] RDMNodeList;
    private double totalMaxPropensity = 0;
    private Hashtable<Integer, Integer> mapRactionIndexNodeIndex = new Hashtable<Integer, Integer>();
    
    //data structure for delay queue
    private DelayedEventQueue delayQueue = new DelayedEventQueue();
    
    //output
    private Hashtable<String, Vector<Double> > simOutput;
    
    //data tracking
    private boolean willWriteFile;
    private String outputFile;
    private DataWriter dataWriter = null;
    private DataWriter performanceWriter = null;

    public void loadModel(String modelFilename) throws Exception {
        //build model
        ComputingMachine.buildModelFromFile(modelFilename, states, reactions);

        //build bipartie dependency
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build propensity
        buildRDMNodeArray();
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Delayed RSSA with Fluctuation Interval [ (1 -/+ " + percentage + ")#X ]");

//        System.out.println("---------------------------------------------------");
//        System.out.println("----------------- Model info ----------------------");
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions);
//        
//        System.out.println("---------------------------------------------------");
        //initialize output
        initalizeSimulation(_maxTime, 0, _logInterval, _isWritingFile, _outputFilename);

        //do sim
        double randomValue = 0.0;
        double searchValue = 0.0;
        double acceptantProb = 0.0;

        int numTrial = 1;
        int nodeIndex = -1;

        HashSet<Integer> updateReactions = new HashSet<Integer>();

        //simulation runtime
        long simTime;
    
        //start simulation
        long startSimTime = System.currentTimeMillis();
        do {
            //clear updated reactions list
            updateReactions.clear();

            //genepercentage time
            double delta = ComputingMachine.computeTentativeTime(rand, totalMaxPropensity);

            //update time
            currentTime += delta;
            
            if(!simulationByStep && currentTime >= maxTime){
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
            
//            System.out.println("total max propensity: "+ totalMaxPropensity +" => delta: " + delta );
//            System.out.println("Current Time: " + currentTime);
//            
            boolean willUpdateDataStructure = false;

            //proceed delay reaction
            DelayedReactionTime d;
            while ( (d = delayQueue.peekTop()) != null) {
                double storedDelayTime = d.getDelayTime();

                if (currentTime > storedDelayTime) {
//                    System.out.println("updated delayed reaction");                    
                    int delayReactionIndex = d.getDelayReactionIndex();

                    //CD reaction
                    if(d.getDelayType().equals(DELAY_TYPE.CONSUMING)){
                        //update product population
                        ComputingMachine.executeDelayReactionProduct(delayReactionIndex, reactions, states);
     
                        //update propensity array as necessary
                        updateReactions = getUpdateReactionsByProduct(delayReactionIndex);

                    }
                    //NCD reaction
                    else if(d.getDelayType().equals(DELAY_TYPE.NONCONSUMING)){
                        ComputingMachine.executeReaction(delayReactionIndex, reactions, states);
                        updateReactions = getUpdateReactions(delayReactionIndex);
                    }
                
                    delayQueue.removeTop();

                    delayStep++;
                    
                    if (!updateReactions.isEmpty()) {
                        willUpdateDataStructure = true;
                        currentTime = storedDelayTime;
                        break;
                    }
                } else {
                    break;
                }
            }

            //update time
            if (!willUpdateDataStructure) {
                randomValue = rand.nextDouble();

                searchValue = randomValue * totalMaxPropensity;

//                System.out.println("Search value: " + searchValue);
                double partialMaxPropensity = 0.0;
                for (nodeIndex = 0; nodeIndex < RDMNodeList.length; nodeIndex++) {
                    partialMaxPropensity += RDMNodeList[nodeIndex].getMaxPropensity();

                    if (partialMaxPropensity >= searchValue) {
                        break;
                    }
                }

                acceptantProb = rand.nextDouble();
//                System.out.println(" - acceptance prob. " + acceptantProb);

                boolean accept = false;

//                System.out.println(" - min propensity: " + RDMNodeList[nodeIndex].getMinPropensity() + ", max propensity: "+ RDMNodeList[nodeIndex].getMaxPropensity() );
                if (acceptantProb <= RDMNodeList[nodeIndex].getMinPropensity() / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => Squeeze");
                    accept = true;
                }

                if (!accept) {
                    double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(RDMNodeList[nodeIndex].getReactionIndex()), states);
//                    System.out.println(" - curent propensity: " + currentPropensity );
                    if (acceptantProb <= currentPropensity / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                        System.out.println("  => Evaluate propensity");
                        accept = true;
                    }
                }

                if (accept) {
                    //fire reaction
                    int fireReactionIndex = RDMNodeList[nodeIndex].getReactionIndex();

//                    System.out.println("Reaction fired " + fireReactionIndex);
//
//                    System.out.println("Current Step: " + firing);
//                    System.out.println("Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));

                    DelayInfo delayInfo = reactions.getReaction(fireReactionIndex).getDelayInfo();
                    double delayTime = delayInfo.getDelayTime();                                        
                    if(delayTime > 0) {
//                        System.out.println("a delayed reaction is schedule @"+ (currentTime + delayTime));                        
                        String delayType = delayInfo.getDelayType();
                        //CD reaction
                        if (delayType.equals(DELAY_TYPE.CONSUMING) ) {
                            delayQueue.add(new DelayedReactionTime(fireReactionIndex, delayType, currentTime + delayTime));

                            //update reactant only
                            ComputingMachine.executeDelayReactionReactant(fireReactionIndex, reactions, states);

                            //update propensity array as necessary
                            updateReactions = getUpdateReactionsByReactant(fireReactionIndex);
                        } 
                        //NCD reaction
                        else if (delayType.equals(DELAY_TYPE.NONCONSUMING) ) {
        //                    System.out.println("non consuming delayed reaction");     
                            delayQueue.add(new DelayedReactionTime(fireReactionIndex, delayType, currentTime + delayTime));
                        }
                    }
                    //non-delayed reaction
                    else { 
                        //update population
                        ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

                        //update propensity array as necessary
                        updateReactions = getUpdateReactions(fireReactionIndex);
                    }

                    firing++;
                    totalTrial += numTrial;
                    numTrial = 1;
                } else {
                    numTrial++;
                }
            }

            if (!updateReactions.isEmpty()) {
                updateRDMNodeArray(updateReactions);
                updateStep++;
            }
//              //print trace
//            System.out.print("State list: ");
//            System.out.println(states);

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
        } while ((!simulationByStep && currentTime < maxTime) || (firing < maxStep));
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
            
            performanceWriter.writeLine("Time\tFiring\tTrial\tUpdate\tDelayStep\tRunTime");
            performanceWriter.writeLine(currentTime + "\t" + firing + "\t" + totalTrial + "\t" + updateStep + "\t" + delayStep + "\t" + simTime/1000.0 );
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }
        
        return simOutput;
    }

    private HashSet<Integer> getUpdateReactions(int fireReactionIndex) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;

        HashSet<Integer> updateReactions = new HashSet<Integer>();

        //update population
        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();

            if (states.getPopulation(s) < lowerStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                updateReactions.addAll(s.getAffectReaction());
            }
        }

        for (Term product : r.getProducts()) {
            s = product.getSpecies();

            if (states.getPopulation(s) > upperStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                updateReactions.addAll(s.getAffectReaction());
            }
        }

        return updateReactions;
    }

    private HashSet<Integer> getUpdateReactionsByReactant(int fireReactionIndex) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;

        HashSet<Integer> updateReactionsByReactant = new HashSet<Integer>();

        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();

            if (states.getPopulation(s) < lowerStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                updateReactionsByReactant.addAll(s.getAffectReaction());
            }
        }

        return updateReactionsByReactant;
    }

    private HashSet<Integer> getUpdateReactionsByProduct(int fireReactionIndex) {
        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;

        HashSet<Integer> updateReactionsByProduct = new HashSet<Integer>();

        for (Term product : r.getProducts()) {
            s = product.getSpecies();

            if (states.getPopulation(s) > upperStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                updateReactionsByProduct.addAll(s.getAffectReaction());
            }
        }

        return updateReactionsByProduct;
    }

    //store min-max propensity for RSSA
    private void buildRDMNodeArray() {
        computeIntervalStateList();

//        System.out.println("Build node array");
        Reaction[] list = reactions.getReactionList();
        RDMNodeList = new RSSANode[list.length];
        int i = 0;
        totalMaxPropensity = 0;
        for (Reaction r : list) {
            double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
            double max_propensity = ComputingMachine.computePropensity(r, upperStates);

            if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                double temp = min_propensity;
                min_propensity = max_propensity;
                max_propensity = temp;
            }
            
            RDMNodeList[i] = new RSSANode(r.getReactionIndex(), min_propensity, max_propensity);
            mapRactionIndexNodeIndex.put(r.getReactionIndex(), i);
//            System.out.println("Node index: " + i + " contains (reaction " + RDMNodeList[i].getReactionIndex() +", min_propensity = "+ RDMNodeList[i].getMinPropensity() +", max_propensity = " + RDMNodeList[i].getMaxPropensity() +")");

            totalMaxPropensity += max_propensity;
            i++;
        }
    }

    private void updateRDMNodeArray(HashSet<Integer> affectReactions) {
        for (int reactionIndex : affectReactions) {
            Reaction r = reactions.getReaction(reactionIndex);
            double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
            double max_propensity = ComputingMachine.computePropensity(r, upperStates);
            
            if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                double temp = min_propensity;
                min_propensity = max_propensity;
                max_propensity = temp;
            }
            
            int nodePos = mapRactionIndexNodeIndex.get(reactionIndex);

            totalMaxPropensity += (max_propensity - RDMNodeList[nodePos].getMaxPropensity());

            RDMNodeList[nodePos].setMinPropensity(min_propensity);
            RDMNodeList[nodePos].setMaxPropensity(max_propensity);
        }
    }

    private void computeIntervalStateList() {
        for (Species s : states.getSpeciesList()) {
            computeIntervalSpecies(s);
        }
    }

    private void computeIntervalSpecies(Species s) {
        int pop = states.getPopulation(s);

        if (pop <= 0) {
            upperStates.updateSpecies(s, 0);
            lowerStates.updateSpecies(s, 0);
        } else if (pop < threshold) {
            upperStates.updateSpecies(s, pop + adjustSize);
            lowerStates.updateSpecies(s, ((pop - adjustSize) > 0 ? (pop - adjustSize) : 0));
        } else {
            upperStates.updateSpecies(s, (int) (pop * (1 + percentage)));
            lowerStates.updateSpecies(s, (int) (pop * (1 - percentage)));
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
