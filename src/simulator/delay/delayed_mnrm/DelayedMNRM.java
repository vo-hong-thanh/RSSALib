/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.delay.delayed_mnrm;

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
import simulator.IAlgorithm;
import simulator.delay.DelayedEventQueue;
import simulator.delay.DelayedReactionTime;

/**
 *
 * @author Vo
 */
public class DelayedMNRM implements IAlgorithm{
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
    private int delayStep = 0;
    
    //data structire for simulation
    private MNRMNode[] nodes;
    
    //delay queue
    private DelayedEventQueue delayQueue = new DelayedEventQueue();
    
    //output
    private Hashtable<String, Vector<Double> > simOutput;
    
    //data tracking
    private boolean willWriteFile;
    private String outputFile;
    private DataWriter dataWriter = null;
    private DataWriter performanceWriter = null;
    
    public void config(long _maxStep, double _maxTime, double _logInterval, String modelFilename, boolean _isWriteable, String outputFilename) throws Exception {
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
        
        //build model
        ComputingMachine.buildModel(modelFilename, states, reactions);

        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);
        
        //build bipartie species-reaction dependency graph
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build Modified NRM mode
        buildModifiedNRMNodes();

        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("Delayed Modified Next Reaction Method (NRM)");
        
//        System.out.println("---------------------------------------------------");
//        System.out.println("----------------- Model info -----------------------");
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

        //write data
        dataWriter.write("time" + "\t");
        for (Species s : states.getSpeciesList()) {
            dataWriter.write(s.getName() + "\t");
        }
        dataWriter.writeLine();

        dataWriter.write(currentTime + "\t");
        simOutput.get("t").add(currentTime);       
        for (Species s : states.getSpeciesList()) {
            int pop = states.getPopulation(s);
            dataWriter.write(pop +"\t");
            
            simOutput.get(s.getName()).add((double)pop);
        }
        dataWriter.writeLine();

        performanceWriter.writeLine("Time\tFiring\tDelayStep\tRunTime");
        
        long simTime;

        HashSet<Integer> updateReactions = new HashSet<Integer>();
        
        //start simulation
        long startSimTime = System.currentTimeMillis();
        do {
            double delta = Double.MAX_VALUE;

            int fireReactionIndex = -1;

            //search for reaction firing time
            for (int i = 0; i < nodes.length; i++) {
                if (delta >= nodes[i].getDelta()) {
                    fireReactionIndex = nodes[i].getReactionIndex();
                    delta = nodes[i].getDelta();
                }
            }

            //proceed delayed reaction
            boolean isDelayedUpdate = false;
            String delayTypeUpdate = "";
            DelayedReactionTime d;
            if ((d = delayQueue.peekTop()) != null) {
                if ((d.getDelayTime() - currentTime) < delta) {
//                    System.out.println("updated delayed reaction");
                    
                    //update delta
                    delta = d.getDelayTime() - currentTime;
                    delayTypeUpdate = d.getDelayType();
                    
                    fireReactionIndex = d.getDelayReactionIndex();
                    
                    isDelayedUpdate = true;

                    //remove element from queue
                    delayQueue.removeTop();

                    delayStep++;
                }
            }

            //update time
            currentTime += delta;

            if (isDelayedUpdate) {                
                //CD reaction
                if(delayTypeUpdate.equals(DELAY_TYPE.CONSUMING)) {
                    //update product population
                    ComputingMachine.executeDelayReactionProduct(fireReactionIndex, reactions, states);
                    
                    updateReactions = getUpdateReactionsByProduct(fireReactionIndex);
                }
                //NCD reaction
                else if(delayTypeUpdate.equals(DELAY_TYPE.NONCONSUMING)) {
                    ComputingMachine.executeReaction(fireReactionIndex, reactions, states);
                    
                    updateReactions = getUpdateReactions(fireReactionIndex);
                }
            }
            else {
                DelayInfo delayInfo = reactions.getReaction(fireReactionIndex).getDelayInfo();
                double delayTime = delayInfo.getDelayTime();
                
                if(delayTime > 0) {
//                    System.out.println("a delayed reaction is schedule @"+ (currentTime + delayTime));
                    
                    String delayType = delayInfo.getDelayType();

                    //CD reaction
                    if (delayType.equals(DELAY_TYPE.CONSUMING) ) {
    //                    System.out.println("consuming delayed reaction");     
                        delayQueue.add(new DelayedReactionTime(fireReactionIndex, delayType, currentTime + delayTime));
                        //update reactants only
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
                //ND reaction
                else {
                    ComputingMachine.executeReaction(fireReactionIndex, reactions, states);
                    
                    //update propensity array as necessary
                    updateReactions = getUpdateReactions(fireReactionIndex);
                }
                
                firing++;
            }

            //update
            updateModifiedNRMNodes(delta, isDelayedUpdate, fireReactionIndex, updateReactions);

//            //print trace
//            System.out.print("State list: ");
//            System.out.println(states);

            //time handling
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;

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
        } while ((!simulationByStep && currentTime < maxTime) || (firing < maxStep));
        long endSimTime = System.currentTimeMillis();
        simTime = (endSimTime - startSimTime);
        performanceWriter.writeLine(currentTime +"\t" + firing + "\t" + delayStep + "\t" + simTime/1000.0 );

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
        
                performanceWriter.writeLine("Time\tFiring\tDelayStep\tRunTime");
                performanceWriter.writeLine(currentTime +"\t" + firing + "\t" + delayStep + "\t" + simTime/1000.0 );
            }
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }

        return simOutput;
    }

    //build Modified NRM node
    private void buildModifiedNRMNodes() {
        Reaction[] list = reactions.getReactionList();
        nodes = new MNRMNode[list.length];

        for (int i = 0; i < list.length; i++) {
            Reaction r = list[i];
            //add node
            double propensity = ComputingMachine.computePropensity(r, states);
            double pk = ComputingMachine.computeTentativeTime(rand);
            double tk = 0;
            nodes[i] = new MNRMNode(r.getReactionIndex(), i, propensity, pk, tk);
        }
    }

    private void updateModifiedNRMNodes(double delta, boolean isDelayedUpdate, int firedReactionIndex, HashSet<Integer> dependent) {
//        System.out.println("[Update priority queue]");
        int reactionIndex;
        for (int i = 0; i < nodes.length; i++) {
            reactionIndex = nodes[i].getReactionIndex();
            nodes[i].updateTk(delta);

            if (dependent.contains(reactionIndex)) {
                if (!isDelayedUpdate && reactionIndex == firedReactionIndex) {
                    double randomPk = ComputingMachine.computeTentativeTime(rand);
                    nodes[i].updatePk(randomPk);
                }

                double newPropensity = ComputingMachine.computePropensity(reactions.getReaction(reactionIndex), states);
                nodes[i].updateDelta(newPropensity);
            } 
            else {
                nodes[i].updateDelta();
            }
        }
    }
    
    private HashSet<Integer> getUpdateReactions(int fireReactionIndex) {
        return reactions.getReaction(fireReactionIndex).getDependent();
    }

    private HashSet<Integer> getUpdateReactionsByReactant(int fireReactionIndex) {
        HashSet<Integer> updateReactionsByReactant = new HashSet<Integer>();

        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;
        
        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();
            updateReactionsByReactant.addAll(s.getAffectReaction());
        }

        return updateReactionsByReactant;
    }

    private HashSet<Integer> getUpdateReactionsByProduct(int fireReactionIndex) {
        HashSet<Integer> updateReactionsByProduct = new HashSet<Integer>();

        Reaction r = reactions.getReaction(fireReactionIndex);
        Species s;

        for (Term product : r.getProducts()) {
            s = product.getSpecies();

            updateReactionsByProduct.addAll(s.getAffectReaction());
        }

        return updateReactionsByProduct;
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
