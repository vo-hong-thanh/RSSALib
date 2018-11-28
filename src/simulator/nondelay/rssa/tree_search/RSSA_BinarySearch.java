/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa.tree_search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import utils.ComputingMachine;
import utils.DataWriter;
import java.util.Vector;
import simulator.IAlgorithm;
import simulator.nondelay.rssa.RSSANode;

/**
 *
 * @author Hong Thanh
 */
public class RSSA_BinarySearch implements IAlgorithm{
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
    
    //info for rejection
    private double fluctuationRate = 0.1;
    private int threshold = 20;
    private int adjustSize = 4;
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    private int treeLength;
    private RSSANode[] RDMNodeTree;
    private Hashtable<Integer, Integer> mapRactionIndexToNodeIndex = new Hashtable<Integer, Integer>();

    //output
    private Hashtable<String, Vector<Double> > simOutput;
    
    //data tracking
    private boolean willWriteFile;
    private String outputFile;
    private DataWriter dataWriter = null;
    private DataWriter performanceWriter = null;
    
    public void config(long _maxStep, double _maxTime, double _logInterval, String modelFilename, boolean _isWriteable, String outputFilename) throws Exception {
        if(_maxStep > 0)
        {
            maxStep = _maxStep;
            simulationByStep = true;
            maxTime = Double.MAX_VALUE;
        }else
        {
            maxStep = 0;
            simulationByStep = false;
            maxTime = _maxTime;
        }
        
        logInterval = _logInterval;
        logPoint = _logInterval; 

        //build model
        ComputingMachine.buildModel(modelFilename, states, reactions);
                
        //build bipartie dependency
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build propensity
        buildRDMNodeTree();

        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("RSSA with Binary Search and Fluctuation Interval [ (1 -/+ " + fluctuationRate + ")#X ]");
        
//        System.out.println("---------------------------------------------------");//   
//        System.out.println(" Model information ");     
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions.toStringFull());
//
//        System.out.println("---------------------------------------------------");
        
        long simTime = 0;
        long searchTime = 0;
        long updateTime = 0;

        double searchValue = 0.0;
        double acceptantProb = 0.0;

        int numTrial = 1;
        int nodeIndex = -1;
        
        long startSimTime = System.currentTimeMillis();
        do{
            numTrial = 1;
            nodeIndex = -1;

            long startSearchTime = System.currentTimeMillis();
            while (true) {
                //search for candidate reaction
                searchValue = rand.nextDouble() * RDMNodeTree[0].getMaxPropensity();
                nodeIndex = 0;
                while (2 * nodeIndex + 1 < treeLength && 2 * nodeIndex + 2 < treeLength) {
                    if (RDMNodeTree[2 * nodeIndex + 1].getMaxPropensity() > searchValue) {
                        nodeIndex = 2 * nodeIndex + 1;
                    } else {
                        searchValue = RDMNodeTree[nodeIndex].getMaxPropensity() - searchValue;
                        nodeIndex = 2 * nodeIndex + 2;
                    }
                }

                //rejection test on candidate
                acceptantProb = rand.nextDouble();
//                System.out.println(" - acceptance prob. " + acceptantProb);

//                System.out.println(" - min propensity: " + RDMNodeList[nodeIndex].getMinPropensity() + ", max propensity: "+ RDMNodeList[nodeIndex].getMaxPropensity() );
                if (RDMNodeTree[nodeIndex].getMinPropensity() != 0 && acceptantProb <= RDMNodeTree[nodeIndex].getMinPropensity() / RDMNodeTree[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => Squeeze");
                    break;
                }

                double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(RDMNodeTree[nodeIndex].getReactionIndex()), states);
//                System.out.println(" - curent propensity: " + currentPropensity );
                if (currentPropensity != 0 && acceptantProb <= currentPropensity / RDMNodeTree[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => Evaluate propensity");
                    break;
                }

                numTrial++;
            }
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            double delta = ComputingMachine.computeTentativeTime(numTrial, rand, RDMNodeTree[0].getMaxPropensity());
//            System.out.println("total max propensity: " + totalMaxPropensity + "=> delta: " + delta);
            
            //update time
            currentTime += delta;
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;

            int fireReactionIndex = RDMNodeTree[nodeIndex].getReactionIndex();

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

             //update propensity tree as necessary
            long startUpdateTime = System.currentTimeMillis();
            if (updateRDMNodeTree(fireReactionIndex)) {
                updateStep++;
            }
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);
            //update step
            totalTrial += numTrial;
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
        
                performanceWriter.writeLine("Time\tFiring\tTrial\tUpdate\tRunTime\tSearchTime\tUpdateTime");
                performanceWriter.writeLine(currentTime +"\t" + firing + "\t" +  totalTrial + "\t" +  updateStep + "\t" + simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
            }
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }        

        return simOutput;
    }

//    private HashSet<Integer> getUpdateReactions(int fireReactionIndex) {
//        Reaction firedReaction = reactions.getReaction(fireReactionIndex);
//        Species s;
//
//        HashSet<Integer> needUpdateReactions = new HashSet<Integer>();
//
//        //update population
//        for (Term reactant : firedReaction.getReactants()) {
//            s = reactant.getSpecies();
//
//            int pop = states.getPopulation(s);
//            
//            if ( (pop != 0 && pop < lowerStates.getPopulation(s)) || pop <= 0) {
//                computeIntervalSpecies(s);
//                needUpdateReactions.addAll(s.getAffectReaction());
//            }
//        }
//
//        for (Term product : firedReaction.getProducts()) {
//            s = product.getSpecies();
//
//            if (!s.isProductOnly() && states.getPopulation(s) > upperStates.getPopulation(s)) {
//                computeIntervalSpecies(s);
//                needUpdateReactions.addAll(s.getAffectReaction());
//            }
//        }
//
//        return needUpdateReactions;
//    }

    //store min-max propensity for RSSA
    private void buildRDMNodeTree() {
//        System.out.println("build upperPercentage and lowerPercentage state");
        for (Species s : states.getSpeciesList()) {
            if(!s.isProductOnly())
            {
                computeIntervalSpecies(s);
            }
        }

        //add a dummy reaction
        if (reactions.getLength() % 2 != 0) {
            reactions.addReaction(new Reaction(-1, new ArrayList<Term>(), new ArrayList<Term>()));
        }

//        System.out.println("Build node array");
        Reaction[] list = reactions.getReactionList();

        treeLength = 2 * list.length - 1;
        RDMNodeTree = new RSSANode[treeLength];

        int startDownIndex = treeLength / 2;
        for (Reaction r : list) {
            double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
            double max_propensity = ComputingMachine.computePropensity(r, upperStates);

            RDMNodeTree[startDownIndex] = new RSSANode(r.getReactionIndex(), min_propensity, max_propensity);

            mapRactionIndexToNodeIndex.put(r.getReactionIndex(), startDownIndex);

//            System.out.println("Node index: " + startDownIndex + " contains (reaction " + RDMNodeTree[startDownIndex].getReactionIndex() +", min_propensity = "+ RDMNodeTree[startDownIndex].getMinPropensity() +", max_propensity = " + RDMNodeTree[startDownIndex].getMaxPropensity() +")");
            startDownIndex++;
        }

        for (int startUpIndex = (treeLength / 2) - 1; startUpIndex >= 0; startUpIndex--) {
            double max_propensity_left = RDMNodeTree[2 * startUpIndex + 1].getMaxPropensity();
            double max_propensity_right = RDMNodeTree[2 * startUpIndex + 2].getMaxPropensity();

            RDMNodeTree[startUpIndex] = new RSSANode(-1, 0, max_propensity_left + max_propensity_right);
        }
    }

    private boolean updateRDMNodeTree(int firedReactionIndex) {
        Reaction firedReaction = reactions.getReaction(firedReactionIndex);
        Species s;

        HashSet<Integer> needUpdateReactions = new HashSet<Integer>();

        //update population
        for (Term reactant : firedReaction.getReactants()) {
            s = reactant.getSpecies();

            int pop = states.getPopulation(s);
            
            if ( (pop != 0 && pop < lowerStates.getPopulation(s)) || pop <= 0) {
                computeIntervalSpecies(s);
                needUpdateReactions.addAll(s.getAffectReaction());
            }
        }

        for (Term product : firedReaction.getProducts()) {
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
                double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
                double max_propensity = ComputingMachine.computePropensity(r, upperStates);

                int nodePos = mapRactionIndexToNodeIndex.get(reactionIndex);

                double diff = max_propensity - RDMNodeTree[nodePos].getMaxPropensity();
                RDMNodeTree[nodePos].setMinPropensity(min_propensity);
                RDMNodeTree[nodePos].setMaxPropensity(max_propensity);

                //update parent
                while (nodePos != 0) {
                    nodePos = (nodePos - 1) / 2;
                    RDMNodeTree[nodePos].setMaxPropensity(RDMNodeTree[nodePos].getMaxPropensity() + diff);
                }
            }
            return true;
        }
    }

    private void computeIntervalSpecies(Species s) {
        int pop = states.getPopulation(s);
        if (pop <= 0) {
            upperStates.updateSpecies(s, 0);
            lowerStates.updateSpecies(s, 0);
        }else if(pop < threshold) {
            upperStates.updateSpecies(s, pop + adjustSize );
            lowerStates.updateSpecies(s, ((pop - adjustSize) > 0 ? (pop - adjustSize) : 0) );
        } else {
            upperStates.updateSpecies(s, (int) (pop*(1 + fluctuationRate)) );
            lowerStates.updateSpecies(s, (int) (pop*(1 - fluctuationRate)) );
        }
    }
    
    private void initalizeOutput() {
        simOutput = new Hashtable<String, Vector<Double> >(); 
        
        //output
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
