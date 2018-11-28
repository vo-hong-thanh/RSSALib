/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.gillespie.sorting;

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
import simulator.IAlgorithm;
import simulator.nondelay.gillespie.DMNode;

/**
 *
 * @author HongThanh
 */
public class SDM implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states;
    private ReactionList reactions;

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
    
    //data structure for simulation
    private DMNode[] DMNodeList;
    private double totalPropensity = 0;
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
        
        //build dependency graph
        ComputingMachine.buildReactionDependency(reactions);
        
       //build propensity list
        buildDMNodeList();
        
        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("Sorting Direct Method (DM)");
//        System.out.println("---------------------------------------------------");
//        System.out.println("--------------- Model information -----------------");
//        System.out.print("State list: ");
//        System.out.println(states);
//        
//        System.out.print("Reaction list: ");
//        System.out.println(reactions.toStringFull());
//        
//        System.out.println("---------------------------------------------------");        
           
        long simTime = 0;
        long updateTime = 0;
        long searchTime = 0;
        
        //start simulation
        long startSimTime = System.currentTimeMillis();        
        do{
//            System.out.println("Total propensity: " + totalPropensity);
            double delta = ComputingMachine.computeTentativeTime(rand, totalPropensity);
//            System.out.println("=> delta: " + delta);            
            
            //update time
            currentTime += delta;
            
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;
            
            long startSearchTime = System.currentTimeMillis();
            
            int nodeIndex = -1;
            int fireReactionIndex = -1; 
            
            double searchValue = rand.nextDouble()*totalPropensity;
            double partialPropensity = 0;
            
            for(nodeIndex = 0; nodeIndex < DMNodeList.length; nodeIndex++)
            {
                DMNode node = DMNodeList[nodeIndex];
//                System.out.println("reaction: " + reactions.getReaction(node.getReactionIndex()) + ": " + node.getPropensity());
                partialPropensity += node.getPropensity();
            
                if(partialPropensity >= searchValue){
                    fireReactionIndex = node.getReactionIndex();
                    break;
                }
            }    
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);
            
            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);
 
            //update array of propensity            
            long startUpdateTime = System.currentTimeMillis();

            updateDMNodeList(reactions.getReaction(fireReactionIndex).getDependent());

            //swap nodes 
            if(nodeIndex >= 1){
                int prevNodeIndex = nodeIndex - 1;
                int reactionIndexInPrevNode = DMNodeList[prevNodeIndex].getReactionIndex();

                //exchange node
                DMNode temp = DMNodeList[prevNodeIndex];                
                DMNodeList[prevNodeIndex] = DMNodeList[nodeIndex];
                DMNodeList[nodeIndex] = temp;
                
                //update mapping
                mapRactionIndexToNodeIndex.put(fireReactionIndex, prevNodeIndex);
                mapRactionIndexToNodeIndex.put(reactionIndexInPrevNode, nodeIndex);
            }
            
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);
            
            firing++;
//            System.out.println("Step: " + firing + "@Time: " + currentTime + ": (Fired)" + reactions.getReaction(fireReactionIndex));

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
    
    //Fast retrieve reaction for DM
    private void buildDMNodeList(){
//        System.out.println("[Build array]");
        Reaction[] list = reactions.getReactionList();
        DMNodeList = new DMNode[list.length];
        int i = 0;
        for (Reaction r : list) {
            double propensity =  ComputingMachine.computePropensity(r, states);
            
            DMNodeList[i] = new DMNode(r.getReactionIndex(), propensity, i);
           
            mapRactionIndexToNodeIndex.put(r.getReactionIndex(), i);
//            System.out.println("Node index: " + i + " contains (reaction " + DMNodeList[i].getReactionIndex() +", propensity = "+ DMNodeList[i].getPropensity() +")");
            
            totalPropensity += propensity;            
            i++;
        }
    }
    
    //update propensity list
    private void updateDMNodeList(HashSet<Integer> dependent) {
//        System.out.println("[update array]");
        for(int reactionIndex : dependent) {
            Reaction r = reactions.getReaction(reactionIndex);
            double newPropensity = ComputingMachine.computePropensity(r, states);
            
            int nodeIndex = mapRactionIndexToNodeIndex.get(reactionIndex);
            DMNode node = DMNodeList[nodeIndex];
            
            totalPropensity += (newPropensity - node.getPropensity());

            node.setPropensity(newPropensity);
//            System.out.println("Node index: " + nodePos + " contains (reaction " + DMNodeList[nodePos].getReactionIndex() +", propensity = "+ DMNodeList[nodePos].getPropensity() +")");
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
