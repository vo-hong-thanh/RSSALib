/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa;

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
import model.rates.InhibitoryHillKinetics;
import simulator.IAlgorithm;

/**
 *
 * @author Hong Thanh
 */
public class RSSA implements IAlgorithm{
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
    private int threshold = 25;
    private int adjustSize = 4;    
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();
    
    private RSSANode[] RDMNodeList;
    private double totalMaxPropensity = 0;
    private Hashtable<Integer, RSSANode> mapRactionIndexToNode = new Hashtable<Integer, RSSANode>();

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
    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("RSSA with Fluctuation Interval [ (1 -/+ " + fluctuationRate + ")#X ]");
//        System.out.println("---------------------------------------------------");
//        System.out.println("-----------------Model information-----------------");
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
//            System.out.println("total max propensity: " + totalMaxPropensity);
            
            long startSearchTime = System.currentTimeMillis();
            while (true) {
                //propensity is too small => stop simulation
                if(totalMaxPropensity < 1e-7){
                    break;
                }
                
                //search for candidate reaction
                searchValue = rand.nextDouble() * totalMaxPropensity;
//                System.out.println(" - random search value: " + searchValue);
                double partialMaxPropensity = 0.0;
                for (nodeIndex = 0; nodeIndex < RDMNodeList.length; nodeIndex++) {
//                    System.out.println(" examine node with value " + RDMNodeList[nodeIndex].getMaxPropensity());
                    partialMaxPropensity += RDMNodeList[nodeIndex].getMaxPropensity();

                    if (partialMaxPropensity >= searchValue) {
//                        System.out.println(" => select candidate reaction index " + RDMNodeList[nodeIndex].getReactionIndex());
                        
                        break;
                    }
                }

                //rejection test for candidate
                acceptantProb = rand.nextDouble();
//                System.out.println(" - acceptance prob. " + acceptantProb);

//                System.out.println(" - min propensity: " + RDMNodeList[nodeIndex].getMinPropensity() + ", max propensity: "+ RDMNodeList[nodeIndex].getMaxPropensity() );
                if (RDMNodeList[nodeIndex].getMinPropensity() != 0 && acceptantProb <= RDMNodeList[nodeIndex].getMinPropensity() / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => squeezely accepted");
                    break;
                }

                double currentPropensity = ComputingMachine.computePropensity(reactions.getReaction(RDMNodeList[nodeIndex].getReactionIndex()), states);
//                System.out.println(" - compute propensity: " + currentPropensity );
                if (currentPropensity != 0 && acceptantProb <= currentPropensity / RDMNodeList[nodeIndex].getMaxPropensity()) {
//                    System.out.println("  => accept by propensity");
                    break;
                }
                
//                System.out.println("  => reject, try again");
                numTrial++;
            }
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            double delta = ComputingMachine.computeTentativeTime(numTrial, rand, totalMaxPropensity);
//            System.out.println(" delta: " + delta);
            
            //too small time increasemence
//            if(delta < 1e-7){
//                delta = logPoint - currentTime > 0 ? logPoint - currentTime : maxTime - currentTime;
//            }

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
//            System.out.println("Step: " + firing + " @Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
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

            mapRactionIndexToNode.put(r.getReactionIndex(), RDMNodeList[i]);

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
