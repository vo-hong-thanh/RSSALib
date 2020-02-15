/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.rssa.lookup;

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
import model.rates.InhibitoryHillKinetics;
import simulator.IAlgorithm;
import simulator.nondelay.rssa.RSSANode;

/**
 *
 * @author Hong Thanh
 */
public class RSSA_LookupSearch implements IAlgorithm{
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
    
    private RSSANode[] RDMNodeList;
    private double totalMaxPropensity = 0;
    private Hashtable<Integer, Integer> mapRactionIndexNodeIndex = new Hashtable<Integer, Integer>();
    
    //table lookup
    private static double[] cutt_off_table;       //rate for lookup
    private static int[] alias_table;          //alias   
    
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
        buildRDMNodeList();

        //table lookup
        buildTableLookup();        
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean __isWritingFile, String _outputFilename) throws Exception {
        System.out.println("RSSA with Alias Lookup and Fluctuation Interval [ (1 -/+ " + fluctuationRate + ")#X ]");
                
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

        double lookupProb = 0.0;
        double acceptantProb = 0.0;

        int nodeIndex = -1;
        int numTrial = 1;
            
        long startSimTime = System.currentTimeMillis();
        do{
            nodeIndex = -1;
            numTrial = 1;

            long startSearchTime = System.currentTimeMillis();
            while (true) {
                //lookup candidate reaction
//                System.out.println("***");
                lookupProb = rand.nextDouble();
                nodeIndex = lookupIndex(lookupProb);
//                System.out.println(" - lookup prob. " + lookupProb  + " => node index " + nodeIndex);

                //rejection test on candidate
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
                buildTableLookup();
                updateStep++;
            }            
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);
            
            //update number of firing
            totalTrial += numTrial;
            firing++;
            
//            System.out.println("Reaction fired " + fireReactionIndex);

//            System.out.println("Step: " + firing) + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));
      
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
    private void buildRDMNodeList() {
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

            mapRactionIndexNodeIndex.put(r.getReactionIndex(), i);

//            System.out.println("Node index: " + i + " contains (reaction " + RDMNodeList[i].getReactionIndex() +", min_propensity = "+ RDMNodeList[i].getMinPropensity() +", max_propensity = " + RDMNodeList[i].getMaxPropensity() +")");

            totalMaxPropensity += max_propensity;
            i++;
        }
    }

    private boolean updateRDMNodeArray(int firedReactionIndex) {
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
            return true;
        }
    }

    //fast grnerating index
    private void buildTableLookup() {
        int tableSize = RDMNodeList.length;
        cutt_off_table = new double[tableSize];
        alias_table = new int[tableSize];

        ArrayList<Integer> Greater = new ArrayList<Integer>();
        ArrayList<Integer> Smaller = new ArrayList<Integer>();

        for (int i = 0; i < tableSize; i++) {
            cutt_off_table[i] = RDMNodeList[i].getMaxPropensity() * tableSize;

            if (cutt_off_table[i] >= totalMaxPropensity) {
                Greater.add(i);
            } else {
                Smaller.add(i);
            }
        }

        while (Greater.size() != 0 && Smaller.size() != 0) {
            int k = Greater.get(0);
            int l = Smaller.get(0);

            //set alias
            alias_table[l] = k;
            Smaller.remove(0);

            //reduce the greater
            cutt_off_table[k] = cutt_off_table[k] - (totalMaxPropensity - cutt_off_table[l]);

            if (cutt_off_table[k] < totalMaxPropensity) {
                Greater.remove(0);
                Smaller.add(k);
            }
        }

        //only one element left in Greater
        if (Greater.size() != 0) {
            cutt_off_table[Greater.get(0)] = totalMaxPropensity;
        }

//        //due to numerical unstable
//        while(Smaller.size() != 0)
//        {
//            cutt_off_table[Smaller.get(0)] = totalMaxPropensity;
//            
//            Smaller.remove(0);
//        }

//        System.out.println("cutt_off_table Table" );
//        for(int i = 0; i < cutt_off_table.length; i++)
//        {
//            System.out.println(i + ": " + cutt_off_table[i]);
//        }
//
//        System.out.println("alias Table" );
//        for(int i = 0; i < alias_table.length; i++)
//        {
//            System.out.println("alias_table["+i+"] = " + alias_table[i]);
//        }
    }

    private int lookupIndex(double u) {
//        System.out.println("Lookup with value: " + u);

        int pos = (int) Math.floor(RDMNodeList.length * u);

        double v = RDMNodeList.length * u - pos;

//        System.out.println("pos: " + pos);
//        System.out.println("v = " + v + " => v*totalMaxPropensity = " + v*totalMaxPropensity);
//        System.out.println("cutt_off_table[" + pos + "] = " + cutt_off_table[pos]);
//        System.out.println("alias_table[" + pos + "] = " + alias_table[pos]);

        if (v * totalMaxPropensity < cutt_off_table[pos]) {
//            System.out.println("accept cutt_off_table");
            return pos;
        } else {
//            System.out.println("accept alias_table");
            return alias_table[pos];
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
