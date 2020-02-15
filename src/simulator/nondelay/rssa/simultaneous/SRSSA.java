/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa.simultaneous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import model.MStateList;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.rates.InhibitoryHillKinetics;
import simulator.nondelay.rssa.RSSANode;

import utils.ComputingMachine;
import utils.DataWriter;

/**
 *
 * @author Vo
 */
public class SRSSA{
    //model info
    private StateList states = new StateList();
    private ReactionList reactions = new ReactionList();
    
    //simulation time
    private double simulationTime = 0;
    
    //log
    private double logInterval = 0;
    private double logPoint = 0;
    
    //number of trajectories
    private int numTrajectory;
    
    //MStaeList
    private MStateList mstates;
    
    //step
    private long updateStep = 0;
    private long[] trialStep;    
    private long[] firingStep;
    private double[] time;
        
    //random generator
    private Random[] rand;
    
    //info for rejection
    private double fluctuationRate = 0.1;
    private int threshold = 25;
    private int adjustSize = 4;
        
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    private RSSANode[] RDMNodeList;
    private Hashtable<Integer, RSSANode> mapRactionIndexToNode = new Hashtable<Integer, RSSANode>();
    private double totalMaxPropensity = 0;
    
    //data tracking
    private DataWriter dataWriter;
    private DataWriter performanceWriter;

    public void loadModel(String modelFile) throws Exception {
        //build model
        ComputingMachine.buildModelFromFile(modelFile, states, reactions);

        //build bipartie dependency
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build propensity
        buildRDMNodeArray();

    }

    public void runSim(int numRuns, double _maxTime, double _logInterval, String _outputFilename) throws Exception {
        System.out.println("Simulatneous RSSA");
//        
//        System.out.println("---------------------------------------------------");
//        
//        System.out.println("---   Initialization   ---");
//        
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Upper bound State list: ");
//        System.out.println(upperStates);
//        
//        System.out.print("Lower bound State list: ");
//        System.out.println(lowerStates);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions);

//        System.out.println("---------------------------------------------------");  

        simulationTime = _maxTime;
        logInterval = _logInterval;
        logPoint = _logInterval;
        
        numTrajectory = numRuns;

        //MStateList
        buildMStateList(states, numTrajectory);
        
        //set up writer
        dataWriter = new DataWriter("(Data)" + _outputFilename);
        performanceWriter = new DataWriter("(Perf)" + _outputFilename);
        
        //write data
        dataWriter.write("time" + "\t");
        for (Species s : states.getSpeciesList()) {
            dataWriter.write(s.getName() + "\t");
        }
        dataWriter.writeLine();

        dataWriter.write(0.0 + "\t");
        for (Species s : states.getSpeciesList()) {
            int pop = states.getPopulation(s);
            dataWriter.write(pop + "\t");
        }
        dataWriter.writeLine();

        //write performance
        performanceWriter.writeLine("Fire\tTrial\tUpdateStep\tRunTime\tSearchTime\tUpdateReactionTime\tUpdateSystemVariableTime\tSetOperationTime");
        
        //initialize information for simulation
        firingStep = new long[numTrajectory];
        trialStep = new long[numTrajectory];
        
        time = new double[numTrajectory];
        
        rand = new Random[numTrajectory];
        for (int i = 0; i < numTrajectory; i++) {
            firingStep[i] = 1;
            trialStep[i] = 1;
            
            time[i] = 0;
            
            rand[i] = new Random();
        }
        
        int countFinishedTrajectory = 0;
        
        //time taken for simulation
        long searchTime = 0;
        long updateReactionTime = 0;
        long updateSystemVariableTime = 0;
        long updateSetTime = 0; 
        long totalSimulationTime = 0;

        double randomValue;
        double searchValue;
        double acceptantProb;

        int numTrial;
                
        int nodeIndex = -1;
        int fireReactionIndex;
        double invertedTotalMaxPropensity;
        double delta;

        //start simulation
        long startSimTime = System.currentTimeMillis();
        do{
            HashSet<Species> updateSpecies = new HashSet<Species>();
            
            invertedTotalMaxPropensity = 1 / totalMaxPropensity;
            
            //simulation
            for (int position = 0; position < numTrajectory; position++) {                                
                while(time[position] < simulationTime){
                    long startSearchTime = System.currentTimeMillis();
                    numTrial = 1;
                    while (true) {
                        //propensity is too small => stop simulation
                        if(totalMaxPropensity < 1e-7){
                            break;
                        }
                        //random search value                        
                        randomValue = rand[position].nextDouble();

                        searchValue = randomValue * totalMaxPropensity;

                        double partialMaxPropensity = 0.0;
                        for (nodeIndex = 0; nodeIndex < RDMNodeList.length; nodeIndex++) {
                            partialMaxPropensity += RDMNodeList[nodeIndex].getMaxPropensity();

                            if (partialMaxPropensity >= searchValue) {
                                break;
                            }
                        }

                        acceptantProb = rand[position].nextDouble();
        //                System.out.println(" - acceptance prob. " + acceptantProb);

        //                System.out.println(" - min propensity: " + RDMNodeList[nodeIndex].getMinPropensity() + ", max propensity: "+ RDMNodeList[nodeIndex].getMaxPropensity() );
                        if (RDMNodeList[nodeIndex].getMinPropensity() != 0 && acceptantProb <= RDMNodeList[nodeIndex].getMinPropensity() / RDMNodeList[nodeIndex].getMaxPropensity()) {
        //                    System.out.println("  => Squeeze");
                            break;
                        }

                        double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(RDMNodeList[nodeIndex].getReactionIndex()), mstates, position);
                        
        //                System.out.println(" - curent propensity: " + currentPropensity );
                        if (currentPropensity != 0 && acceptantProb <= currentPropensity / RDMNodeList[nodeIndex].getMaxPropensity()) {
        //                    System.out.println("  => Evaluate propensity");
                            break;
                        }
                        numTrial++;
                    }
                    long endSearchTime = System.currentTimeMillis();
                    searchTime += endSearchTime - startSearchTime;
                    
                    //update system variable
                    long startUpdateSystemVariable = System.currentTimeMillis();
                    
                    delta = ComputingMachine.computeTentativeTime(numTrial, rand[position])*invertedTotalMaxPropensity;
        //            System.out.println("total max propensity: " + totalMaxPropensity + "=> delta: " + delta);

                    //update time
                    time[position] += delta;

                    trialStep[position] += numTrial;
                    firingStep[position]++;
                    
                    if(time[position] >= simulationTime)
                    {
                        time[position] = simulationTime;
                        countFinishedTrajectory++;
                        break;                        
                    }
                    
                    //reaction firing
                    fireReactionIndex = RDMNodeList[nodeIndex].getReactionIndex();
                    
                    //update population
                    HashSet<Species> updateSpeciesPerSimulation = ComputingMachine.executeReaction(fireReactionIndex, reactions, mstates, position, lowerStates, upperStates);

                    long endUpdateSystemVariable = System.currentTimeMillis();
                    updateSystemVariableTime += endUpdateSystemVariable - startUpdateSystemVariable;
                    
                    //set operation
                    if(!updateSpeciesPerSimulation.isEmpty()) {
                        long startUpdateSet = System.currentTimeMillis();
                    
                        updateSpecies.addAll(updateSpeciesPerSimulation);
                        
                        long endUpdateSet = System.currentTimeMillis();
                        updateSetTime += endUpdateSet - startUpdateSet;
                        
                        break;
                    }
                }               
            }
            
            long startUpdateTime = System.currentTimeMillis();
            if (!updateSpecies.isEmpty()) {
                updateStep++;
                updateRDMNodeArray(updateSpecies);
            }
            long endUpdateTime = System.currentTimeMillis();
            updateReactionTime += endUpdateTime - startUpdateTime;

//            System.out.println("---------------------------------------------------");
        } while (countFinishedTrajectory < numTrajectory);
        //finish simulation
        long endSimTime = System.currentTimeMillis();
        totalSimulationTime = endSimTime - startSimTime;
        
        //print state
        long totalFiringStep = 0;
        long totalTrialStep = 0;        
        for (int position = 0; position < numTrajectory; position++) {
            dataWriter.write(time[position] + "\t");
            for (Species s : states.getSpeciesList()) {
                int pop = mstates.getPopulation(s, position);
                dataWriter.write(pop + "\t");
            }
            dataWriter.writeLine();

            totalFiringStep += firingStep[position];
            totalTrialStep += trialStep[position];
        }
        dataWriter.flush();
        dataWriter.close();

        performanceWriter.writeLine(totalFiringStep  + "\t" + totalTrialStep + "\t" + updateStep + "\t" + totalSimulationTime / 1000.0 + "\t" + searchTime / 1000.0 + "\t" + updateReactionTime / 1000.0 + "\t" + updateSystemVariableTime / 1000.0 + "\t" + updateSetTime/1000.0);

        performanceWriter.flush();
        performanceWriter.close();
    }

    //build MStateList
    private void buildMStateList(StateList states, int numTrajectory) {
        //multi-state list
        mstates = new MStateList();

        for (Species s : states.getSpeciesList()) {
            ArrayList<Integer> populationList = new ArrayList<Integer>(numTrajectory);
            int pop = states.getPopulation(s);

            for (int i = 0; i < numTrajectory; i++) {
                populationList.add(pop);
            }

            mstates.addSpecies(s, populationList);
        }        
    }

    //store min-max propensity for RSSA
    private void buildRDMNodeArray() {
//        System.out.println("build upperPercentage and lowerPercentage state");
        for (Species s : states.getSpeciesList()) {
            if (!s.isProductOnly()) {
                computeIntervalSpecies(s, states.getPopulation(s));
            }
        }

//        System.out.println("Build node array");
        Reaction[] list = reactions.getReactionList();
        RDMNodeList = new RSSANode[list.length];
        int i = 0;
        totalMaxPropensity = 0;
        for (Reaction r : list) {
            double minPropensity = ComputingMachine.computePropensity(r, lowerStates);
            double maxPropensity = ComputingMachine.computePropensity(r, upperStates);
            
            if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                double temp = minPropensity;
                minPropensity = maxPropensity;
                maxPropensity = temp;
            }
            
            RDMNodeList[i] = new RSSANode(r.getReactionIndex(), minPropensity, maxPropensity);

            mapRactionIndexToNode.put(r.getReactionIndex(), RDMNodeList[i]);
//            System.out.println("Node index: " + i + " contains (reaction " + RDMNodeList[i].getReactionIndex() +", minPropensity = "+ RDMNodeList[i].getMinPropensity() +", maxPropensity = " + RDMNodeList[i].getMaxPropensity() +")");

            totalMaxPropensity += maxPropensity;
            i++;
        }
    }

    private void updateRDMNodeArray(HashSet<Species> updateSpecies) {
        HashSet<Integer> updateReactions = new HashSet<Integer>();
        for (Species s : updateSpecies) {
            int minPopulation = Integer.MAX_VALUE;
            int maxPopulation = Integer.MIN_VALUE;

            for (int position = 0; position < numTrajectory; position++) {
                int currentPopulation = mstates.getPopulation(s, position);
                if (maxPopulation < currentPopulation) {
                    maxPopulation = currentPopulation;
                }
                if (minPopulation > currentPopulation) {
                    minPopulation = currentPopulation;
                }
            }
            computeIntervalSpecies(s, minPopulation, maxPopulation);
            updateReactions.addAll(s.getAffectReaction());
        }

        for (int reactionIndex : updateReactions) {
            Reaction r = reactions.getReaction(reactionIndex);
            double min_propensity = ComputingMachine.computePropensity(r, lowerStates);
            double max_propensity = ComputingMachine.computePropensity(r, upperStates);
            
            if(r.getRateLaw() instanceof InhibitoryHillKinetics){
                double temp = min_propensity;
                min_propensity = max_propensity;
                max_propensity = temp;
            }
            
            RSSANode node = mapRactionIndexToNode.get(reactionIndex);

            totalMaxPropensity += (max_propensity - node.getMaxPropensity());

            node.setMinPropensity(min_propensity);
            node.setMaxPropensity(max_propensity);
        }
    }

    private void computeIntervalSpecies(Species s, int population) {
        if (population <= 0) {
            upperStates.updateSpecies(s, 0);
            lowerStates.updateSpecies(s, 0);
        } else if (population < threshold) {
            upperStates.updateSpecies(s, population + adjustSize);
            lowerStates.updateSpecies(s, ((population - adjustSize) > 0 ? (population - adjustSize) : 0));
        } else {
            upperStates.updateSpecies(s, (int) (population * (1 + fluctuationRate)));
            lowerStates.updateSpecies(s, (int) (population * (1 - fluctuationRate)));
        }
    }

    private void computeIntervalSpecies(Species s, int minPopulation, int maxPopulation) {
        if (minPopulation <= 0) {
            lowerStates.updateSpecies(s, 0);
        } else if (minPopulation < threshold) {
            lowerStates.updateSpecies(s, ((minPopulation - adjustSize) > 0 ? (minPopulation - adjustSize) : 0));
        } else {
            lowerStates.updateSpecies(s, (int) (minPopulation * (1 - fluctuationRate)));
        }

        if (maxPopulation <= 0) {
            upperStates.updateSpecies(s, 0);
        } else if (maxPopulation < threshold) {
            upperStates.updateSpecies(s, maxPopulation + adjustSize);
        } else {
            upperStates.updateSpecies(s, (int) (maxPopulation * (1 + fluctuationRate)));
        }
    }
}
