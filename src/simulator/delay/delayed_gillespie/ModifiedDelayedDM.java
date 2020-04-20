/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.delay.delayed_gillespie;


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
import simulator.nondelay.gillespie.DMNode;


/**
 * ModifiedDelayedDM: Modified Delayed DM
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class ModifiedDelayedDM implements IAlgorithm{
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
    
    //data structure for simulation
    private DMNode[] DMNodeList;
    private double totalPropensity = 0;
    private Hashtable<Integer, DMNode> mapRactionIndexNode = new Hashtable<Integer, DMNode>();
    
    //delay queue
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
        
        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);

        //build bipartie species-reaction dependency graph
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build propensity list
        buildDMNodeList();        
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Modified Delayed Direct Method");

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
        initalizeSimulation(_maxTime, 0, _logInterval, _isWritingFile, _outputFilename);

        //do sim
        long simTime = 0;
                
        HashSet<Integer> updateReactions = new HashSet<Integer>();

        //start simulation
        long startSimTime = System.currentTimeMillis();
        do {
            int fireReactionIndex = -1;

            double delta = ComputingMachine.computeTentativeTime(rand, totalPropensity);
//            System.out.println("=> delta: " + delta);            
            //update time
            currentTime = currentTime + delta;
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
            
            //proceed delay reaction
            DelayedReactionTime d;
            //proceed delay reaction
            while ( (d = delayQueue.peekTop()) != null) {
                double storedDelayTime = d.getDelayTime();

                if (currentTime > storedDelayTime) {
                    int delayReactionIndex = d.getDelayReactionIndex();

//                    System.out.println("updated delayed reaction");
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
                     
                        //update propensity array as necessary
                        updateReactions = getUpdateReactions(delayReactionIndex);
                    }
                    
                    double oldtotalPropensity = totalPropensity;
                    updateDMNodeList(updateReactions);

                    currentTime = storedDelayTime + (currentTime - storedDelayTime)*(oldtotalPropensity/totalPropensity);
                    
                    delayQueue.removeTop();
                    delayStep++;
                } else {
                    break;
                }
            }                
            //proceed next reaction
            double partialPropensity = 0;
            double randomValue = rand.nextDouble();

            for (DMNode node : DMNodeList) {
//                    System.out.println("reaction: " + reactions.getReaction(node.getReactionIndex()) + ": " + node.getPropensity());
                partialPropensity += node.getPropensity();

                if (partialPropensity >= randomValue * totalPropensity) {
                    fireReactionIndex = node.getReactionIndex();
                    break;
                }
            }
//                System.out.println("@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));

            DelayInfo delayInfo = reactions.getReaction(fireReactionIndex).getDelayInfo();            
            double delayTime = delayInfo.getDelayTime();
            
            //delayed reaction
            if(delayTime > 0){                    
//                System.out.println("a delayed reaction is schedule @"+ (currentTime + delayTime));
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
            
            updateDMNodeList(updateReactions);
            
            //time handling
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;

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
                        
            performanceWriter.writeLine("Time\tFiring\tDelayStep\tRunTime");
            performanceWriter.writeLine(currentTime +"\t" + firing + "\t" + delayStep + "\t" + simTime/1000.0 );
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }
        
        return simOutput;
    }

    //Fast retrieve reaction for DM
    private void buildDMNodeList() {
//        System.out.println("[Build array]");
        Reaction[] list = reactions.getReactionList();
        DMNodeList = new DMNode[list.length];
        int i = 0;
        for (Reaction r : list) {
            double propensity = ComputingMachine.computePropensity(r, states);
            DMNodeList[i] = new DMNode(r.getReactionIndex(), propensity, i);
//            System.out.println("Node index: " + i + " contains (reaction " + DMNodeList[i].getReactionIndex() +", propensity = "+ DMNodeList[i].getPropensity() +")");

            mapRactionIndexNode.put(r.getReactionIndex(), DMNodeList[i]);
            totalPropensity += propensity;

            i++;
        }
    }

    //update propensity list
    private void updateDMNodeList(HashSet<Integer> dependent) {
//        System.out.println("[update array]");
        for (int reactionIndex : dependent) {
            Reaction r = reactions.getReaction(reactionIndex);
            double newPropensity = ComputingMachine.computePropensity(r, states);

            DMNode node = mapRactionIndexNode.get(reactionIndex);

            totalPropensity += (newPropensity - node.getPropensity());

            node.setPropensity(newPropensity);
//            System.out.println("Node index: " + nodePos + " contains (reaction " + DMNodeList[nodePos].getReactionIndex() +", propensity = "+ DMNodeList[nodePos].getPropensity() +")");
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
