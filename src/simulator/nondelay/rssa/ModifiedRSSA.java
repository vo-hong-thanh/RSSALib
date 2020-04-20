/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa;

import utils.ComputingMachine;
import utils.DataWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import model.rates.InhibitoryHillKinetics;
import simulator.IAlgorithm;

/**
 * ModifiedRSSA: Modified RSSA
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class ModifiedRSSA implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states;
    private ReactionList reactions;

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
    private int threshold = 25;
    private int adjustSize = 4;
    
    private double fluctuationRate = 0.1;
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    private RSSANode[] RDMNodeList;
    private double totalMaxPropensity = 0;
    
    private int nodeIndex = -1;
    private double subTotalMaxPropensity = 0;
    
    private Hashtable<Integer, Integer> mapRactionIndexToNodeIndex = new Hashtable<Integer, Integer>();

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

        //build propensity
        buildRDMNodeArray();
    }

    @Override
    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean __isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Modified RSSA with Fluctuation Interval [ (1 -/+ " + fluctuationRate + ")#X ]");
        
//        System.out.println("---------------------------------------------------");//   
//        System.out.println(" Model information ");     
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions.toStringFull());
//
//        System.out.println("---------------------------------------------------");
        
        //initialize output
        initalizeSimulation(_maxTime, 0, _logInterval, __isWritingFile, _outputFilename);
        
        //do sim
        long simTime = 0;
        long searchTime = 0;
        long updateTime = 0;

        double searchValue = 0.0;
        double acceptantProb = 0.0;

        int numTrial = 1;            
        
        long startSimTime = System.currentTimeMillis();
        do{
            numTrial = 1;            
            
            long startSearchTime = System.currentTimeMillis();
            while (true) {
                //propensity is too small => stop simulation
                if(totalMaxPropensity < 1e-7){
                    break;
                }
                
                //search for candidate reaction
                searchValue = rand.nextDouble() * totalMaxPropensity;
                if(subTotalMaxPropensity < searchValue){
                    //sum-up
    //                System.out.println("subTotalMaxPropensity < searchValue => sum-up method");
                    for(nodeIndex++; nodeIndex < RDMNodeList.length; nodeIndex++){
                        subTotalMaxPropensity += RDMNodeList[nodeIndex].getMaxPropensity();                    
                        if(subTotalMaxPropensity >= searchValue){
                            break;
                        }
                    }
                }else{
                    //chop-down
    //                System.out.println("subTotalMaxPropensity >= searchValue => chop-down method");
                    for(; nodeIndex >= 0; nodeIndex--){
                        if(subTotalMaxPropensity - RDMNodeList[nodeIndex].getMaxPropensity() < searchValue){
                            break;
                        }
                        subTotalMaxPropensity -= RDMNodeList[nodeIndex].getMaxPropensity();
                    }
                }
            
                //rejection test for candidate
                acceptantProb = rand.nextDouble();
//                System.out.println(" - acceptance prob. " + acceptantProb);

//                System.out.println(" - min propensity: " + RDMNodeList[nodeIndex].getMinPropensity() + ", max propensity: "+ RDMNodeList[nodeIndex].getMaxPropensity() );
                if (RDMNodeList[nodeIndex].getMinPropensity() != 0 && acceptantProb <= RDMNodeList[nodeIndex].getMinPropensity() / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => Squeeze");
                    break;
                }

                double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(RDMNodeList[nodeIndex].getReactionIndex()), states);
//                System.out.println(" - curent propensity: " + currentPropensity );
                if (currentPropensity != 0 && acceptantProb <= currentPropensity / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => Evaluate propensity");
                    break;
                }
                numTrial++;
            }
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            double delta = ComputingMachine.computeTentativeTime(numTrial, rand, totalMaxPropensity);
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
            }
                

            int fireReactionIndex = RDMNodeList[nodeIndex].getReactionIndex();

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

            //update propensity array as necessary
            long startUpdateTime = System.currentTimeMillis();
            if(updateRDMNodeArray(fireReactionIndex)){
                updateStep++;
            }            
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);
            
            //update step
            totalTrial += numTrial;
            firing++;

//            System.out.println("Step: " + firing + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
//            System.out.println("Reaction fired " + fireReactionIndex);

//            System.out.println("---------------------------------------------------");
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

    //store min-max propensity for RSSA
    private void buildRDMNodeArray() {
//        System.out.println("build upperPercentage and lowerPercentage state");
        for (Species s : states.getSpeciesList()) {
            if(!s.isProductOnly())
            {
                computeIntervalSpecies(s);
            }
        }

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

            mapRactionIndexToNodeIndex.put(r.getReactionIndex(), i);

//            System.out.println("Node index: " + i + " contains (reaction " + RDMNodeList[i].getReactionIndex() +", min_propensity = "+ RDMNodeList[i].getMinPropensity() +", max_propensity = " + RDMNodeList[i].getMaxPropensity() +")");

            totalMaxPropensity += max_propensity;
            i++;
        }
    }

    private boolean updateRDMNodeArray(int firedReactionIndex) {
        Reaction fireReaction = reactions.getReaction(firedReactionIndex);
        Species s;

        HashSet<Integer> needUpdateReactions = new HashSet<Integer>();

        //update population
        for (Term reactant : fireReaction.getReactants()) {
            s = reactant.getSpecies();
            int pop = states.getPopulation(s);
            
            if ( pop <= 0 || (pop != 0 && pop < lowerStates.getPopulation(s)) ) {
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
                Reaction updateReaction = reactions.getReaction(reactionIndex);
                double min_propensity = ComputingMachine.computePropensity(updateReaction, lowerStates);
                double max_propensity = ComputingMachine.computePropensity(updateReaction, upperStates);

                if(updateReaction.getRateLaw() instanceof InhibitoryHillKinetics){
                    double temp = min_propensity;
                    min_propensity = max_propensity;
                    max_propensity = temp;
                }
                
                int affectedNodeIndex = mapRactionIndexToNodeIndex.get(reactionIndex);

                double diff = max_propensity - RDMNodeList[affectedNodeIndex].getMaxPropensity();
                totalMaxPropensity += diff;

                if(affectedNodeIndex <= nodeIndex){
                    subTotalMaxPropensity += diff;
                }
                
                RDMNodeList[affectedNodeIndex].setMinPropensity(min_propensity);
                RDMNodeList[affectedNodeIndex].setMaxPropensity(max_propensity);
            }
            return true;
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
