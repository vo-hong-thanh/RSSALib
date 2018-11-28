/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.gillespie.tree_search;


import utils.ComputingMachine;
import utils.DataWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import simulator.IAlgorithm;
import simulator.nondelay.gillespie.DMNode;


/**
 *
 * @author vo
 */
public class TreeSSA  implements IAlgorithm{
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
    private DMNode[] tree;
    private int treeLength;
    private Hashtable<Integer, Integer> mapRactionIndexToTreeIndex = new Hashtable<Integer, Integer>();
    
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
        buildTree();
        
        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("Tree-based SSA");        
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
        long updateTime = 0;
        long searchTime = 0;
        
        //start simulation
        long startSimTime = System.currentTimeMillis();        
        do{
//            System.out.println("Total propensity: " + totalPropensity);
            double delta = ComputingMachine.computeTentativeTime(rand, tree[0].getPropensity());
//            System.out.println("=> delta: " + delta);            
            
            //update time
            currentTime += delta;
            
            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;
            
            long startSearchTime = System.currentTimeMillis();
            
            double searchValue = rand.nextDouble()*tree[0].getPropensity();
            int nodeIndex = 0;
            while (2 * nodeIndex + 1 < treeLength && 2 * nodeIndex + 2 < treeLength) {
                if (tree[2 * nodeIndex + 1].getPropensity() > searchValue) {
                    nodeIndex = 2 * nodeIndex + 1;
                } else {
                    searchValue = tree[nodeIndex].getPropensity() - searchValue;
                    nodeIndex = 2 * nodeIndex + 2;
                }
            }
            int fireReactionIndex = tree[nodeIndex].getReactionIndex();
            
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);
            
            //update population
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);
 
            //update array of propensity            
            long startUpdateTime = System.currentTimeMillis();

            updateTree(reactions.getReaction(fireReactionIndex).getDependent());

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
    private void buildTree(){        
        //add a dummy reaction
        if (reactions.getLength() % 2 != 0) {
            reactions.addReaction(new Reaction(-1, new ArrayList<Term>(), new ArrayList<Term>()));
        }

//        System.out.println("Build node array");
        Reaction[] list = reactions.getReactionList();

        treeLength = 2 * list.length - 1;
        tree = new DMNode[treeLength];

        int startDownIndex = treeLength / 2;
        for (Reaction r : list) {
            double propensity = ComputingMachine.computePropensity(r, states);
            
            tree[startDownIndex] = new DMNode(r.getReactionIndex(), propensity, startDownIndex);

            mapRactionIndexToTreeIndex.put(r.getReactionIndex(), startDownIndex);

//            System.out.println("Tree index: " + startDownIndex + " contains (reaction " + tree[startDownIndex].getReactionIndex() +", propensity = "+ tree[startDownIndex].getPropensity() + ")");
            startDownIndex++;
        }

        for (int startUpIndex = (treeLength / 2) - 1; startUpIndex >= 0; startUpIndex--) {
            double propensity_left = tree[2 * startUpIndex + 1].getPropensity();
            double propensity_right = tree[2 * startUpIndex + 2].getPropensity();

            tree[startUpIndex] = new DMNode(-1, propensity_left + propensity_right, startUpIndex);
        }
    }
    
    //update propensity list
    private void updateTree(HashSet<Integer> dependent)
    {
//        System.out.println("[update array]");
        for(int reactionIndex : dependent) {
            Reaction r = reactions.getReaction(reactionIndex);
            double newPropensity = ComputingMachine.computePropensity(r, states);
            
            int nodePos = mapRactionIndexToTreeIndex.get(reactionIndex);
            
            double diff = newPropensity - tree[nodePos].getPropensity();
            tree[nodePos].setPropensity(newPropensity);
            
            //update parent
            while (nodePos != 0) {
                nodePos = (nodePos - 1) / 2;
                tree[nodePos].setPropensity(tree[nodePos].getPropensity() + diff);
            }
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
