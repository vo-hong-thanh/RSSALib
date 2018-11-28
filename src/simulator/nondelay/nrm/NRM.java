/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.nrm;

import java.util.HashSet;
import java.util.Hashtable;
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
 *
 * @author HongThanh
 */
public class NRM implements IAlgorithm{
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

    //number of reaction firings
    private long firing = 0;
    
    //data structire for simulation
    private BinaryHeap heap;

//    //memory enquiry
//    private Runtime currentRuntime = Runtime.getRuntime();
    
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
        
//        // Run the garbage collector and get memory
//        currentRuntime.gc();
//        long beforeDependecyGraph = (currentRuntime.totalMemory() - currentRuntime.freeMemory()) / 1024L;
        
        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);
        
//        // Run the garbage collector and get memory
//        currentRuntime.gc();
//        long afterDependecyGraph = (currentRuntime.totalMemory() - currentRuntime.freeMemory()) / 1024L;
        
        //build priority queue
        buildHeap();
        
        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();

//        System.out.println("Allocated Memory (KB) before building Dependcy Graph: " + beforeDependecyGraph + " and after: " + afterDependecyGraph);
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("Next Reaction Method (NRM)");
//        System.out.println("---------------------------------------------------");
//        System.out.println(" Model information ");  
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions);
//        
//        System.out.println("Heap");
//        heap.printHeap();
//        
//        System.out.println("---------------------------------------------------");        
        
        long simTime = 0;
        long searchTime = 0;
        long updateTime = 0;
    
        //start simulation
        long startSimTime = System.currentTimeMillis();
        do{
            NRMNode n = heap.getMin();
            
            long startSearchTime = System.currentTimeMillis();
            
            int fireReactionIndex = n.getReactionIndex();
            
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);
            
            currentTime = n.getPutativeTime();
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;

            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

            //update heap           
            long startUpdateTime = System.currentTimeMillis();

            updateHeap(fireReactionIndex, reactions.getReaction(fireReactionIndex).getDependent());

            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);

            firing++;
//            System.out.println("Step: " + firing + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));

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
        
                performanceWriter.writeLine("Time\tFiring\tRunTime\tSearchTime\tUpdateTime");
                performanceWriter.writeLine(currentTime + "\t" + firing + "\t" +simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
            }
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }        

        return simOutput;
    }

     //build the priority Queue
    private void buildHeap() {
//        System.out.println("[Build priority queue]");
        
        Reaction[] list = reactions.getReactionList();
        NRMNode[] heapNode = new NRMNode[list.length];

        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add node
            double propensity = ComputingMachine.computePropensity(r, states);
            heapNode[i] = new NRMNode(r.getReactionIndex(), i, propensity, ComputingMachine.computeTentativeTime(rand, propensity));

//            System.out.println("Node index: " + i + " contains (reaction " + r.getReactionIndex() +", propensity = "+ propensity +")");
        }

        //build heap
        heap = new BinaryHeap(heapNode);
        
    }

    private void updateHeap(int firedReactionIndex, HashSet<Integer> dependent) {
//        System.out.println("[Update priority queue]");
        for (int reactionIndex : dependent) {
            NRMNode node = heap.getNodeByReactionIndex(reactionIndex);
            
//            System.out.println("Current node index: " + node.getHeapNodeIndex() + " contains (reaction " + reactionIndex +", propensity = "+ node.getPropensity() +", time = "+ node.getPutative() + ")");
            
            double newPropensity = ComputingMachine.computePropensity(reactions.getReaction(reactionIndex), states);

            double newTime = Double.MAX_VALUE;
//            System.out.println("=> new propensity = "+ newPropensity);
            
            if (reactionIndex == firedReactionIndex) {
//                System.out.println("Firing reaction");
                //compute new time for firing reaction
                double putativeTime = ComputingMachine.computeTentativeTime(rand, newPropensity);
                if (putativeTime != Double.MAX_VALUE) {
                    newTime = currentTime + putativeTime;
                }
            } else {
                double oldTime = node.getPutativeTime();
                double oldPropensity = node.getPropensity();
                
                //compute new time for affected reaction 
                if(oldPropensity == 0.0)
                {
//                    System.out.println("Affected reaction with old propensity zero");
                    double putativeTime = ComputingMachine.computeTentativeTime(rand, newPropensity);
                    if (putativeTime != Double.MAX_VALUE) {
                        newTime = currentTime + putativeTime;
                    }
                }
                else if (newPropensity == 0.0)
                {
//                    System.out.println("Affected reaction with new propensity zero");
                    newTime = Double.MAX_VALUE;
                }
                else //if (newPropensity != 0.0) 
                {
//                    System.out.println("Affected reaction is rescaled");
                    newTime = (oldPropensity / newPropensity) * (oldTime - currentTime) + currentTime;
                }
            }
//            System.out.println("=> new time = " + newTime);
            //update (absolute) putative time in queue
            node.updateNodeValue(newPropensity, newTime);
            heap.reconstruct(node.getNodeIndex());
        }
//        heap.printHeap();
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
