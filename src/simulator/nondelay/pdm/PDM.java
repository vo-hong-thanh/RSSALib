/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm;

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
import model.rates.MassActionKinetics;
import simulator.IAlgorithm;

/**
 *
 * @author vo
 */
public class PDM implements IAlgorithm{
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
    
    //number of reactionInNode firings
    private long firing = 0;
    
    //data structure for PDM
    private PartialGroup[] partialGroup;
    private double[] partialPropensity;
    private Hashtable<Species, Integer> mapSpeciesToPartialGroupIndex = new Hashtable<Species, Integer>();
    private Hashtable<Species, ArrayList<CombineGroupNodeIndex>> mapSpeciesToCombineGroupNodeIndex = new Hashtable<Species, ArrayList<CombineGroupNodeIndex>>();
    
    private double totalSum;
    
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

        //build data structure for PDM
        buildPartialPropensityStructure();        
        buildPropensity();       
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Partial-propensity Direct Method (PDM)");        
//        System.out.println("----------------------------");                  
//        System.out.println("------- Model information ----------");
//        System.out.print("Reaction");
//        System.out.println(reactions);
//        
//        System.out.print("State");
//        System.out.println(states.toStringFull());
//        
//        System.out.println("------- Data structure information --------");
//        System.out.println("Partial group");
//        for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
//            System.out.println("@Group " + groupIndex);
//            Species s = partialGroup[groupIndex].getSpecies();
//            
//            System.out.println(" +Grouping species " + s);
//            
//            ArrayList<PartialNode> partialNodeCollection = partialGroup[groupIndex].getAllPartialNode();
//            for(int nodeIndex = 0 ; nodeIndex <  partialNodeCollection.size(); nodeIndex++)
//            {
//                System.out.println("  @pos " + nodeIndex +": "+ partialNodeCollection.get(nodeIndex));
//            }
//            System.out.println("  Total Partial value : " + partialGroup[groupIndex].getGroupSumPartialValue());
//        }
//        
//        System.out.println("Partial propensity");
//        for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
//            System.out.println("@Group: " + groupIndex + " => propensity =  " + partialPropensity[groupIndex]);
//        }
//        
//        System.out.println("Map Species-Partial Group index");
//        for (Enumeration<Species> sc = mapSpeciesToPartialGroupIndex.keys(); sc.hasMoreElements();) {
//            Species s = sc.nextElement();
//            int groupIndex = mapSpeciesToPartialGroupIndex.get(s);
//            
//            System.out.println(" Grouping Species " + s + " is in group " + groupIndex);
//        }
//        
//        System.out.println("Map Species-(Partial Group index, Node index)");
//        for (Enumeration<Species> sc = mapSpeciesToCombineGroupNodeIndex.keys(); sc.hasMoreElements();) {
//            Species s = sc.nextElement();
//            ArrayList<GroupNodeIndex> combineGroupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(s);
//            
//            System.out.println(" Species " + s + " appears in ");
//            
//            for(CombineGroupNodeIndex combineIndex : combineGroupNodeIndexList)
//            {
//                System.out.println("  " + combineIndex);
//            }
//        }
//        System.out.println("----------------------------"); 
        //initialize output
        initalizeSimulation(_maxTime, 0, _logInterval, _isWritingFile, _outputFilename);

        //do sim
        long simTime = 0;
        long updateTime = 0;
        long searchTime = 0;
        
        //start simulation
        long startSimTime = System.currentTimeMillis();        
        do{
//            //generate firing time
//            System.out.println("Total propensity: " + totalSum);
//            
            double delta = ComputingMachine.computeTentativeTime(rand, totalSum);
//            System.out.println("firing time " + delta);

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
            
            //find node in the partial propensity list
            double searchValue = rand.nextDouble() * totalSum;

//            System.out.println("random =  " + randomValue);
//            System.out.println("=> search = random*totalSum = " + searchValue);

            double partialSum = 0;
            int fireReactionIndex = -1;

            long startSearchTime = System.currentTimeMillis();
//            System.out.println("Partial sum " + partialSum);
            for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
                partialSum += partialPropensity[groupIndex];
//                System.out.println(" after adding to group " + groupIndex + " => " + partialSum);

                if (partialSum >= searchValue) {
//                    System.out.println("=> found group " + groupIndex);
                    //find reactionInNode firing
                    int speciesPopulation = 1;                
                    if(groupIndex != 0) {
                        speciesPopulation = states.getPopulation(partialGroup[groupIndex].getSpecies());
                    }
                    
                    double scaledValue = (searchValue - partialSum + partialPropensity[groupIndex]) / speciesPopulation;
//                    System.out.println(" scale value " + scaledValue);
                    int nodeIndex = partialGroup[groupIndex].findNodeBySequentialSearch(scaledValue);
//                    System.out.println("=> found node " + nodeIndex);
                    
                    fireReactionIndex = partialGroup[groupIndex].getPartialNode(nodeIndex).getReactionIndex();
                    break;
                }
            }
            long endSearchTime = System.currentTimeMillis();
            searchTime += (endSearchTime - startSearchTime);

            firing++;
//            System.out.println("@Step: " + firing + ", @Time: " + currentTime + " => Fire reaction " + reactions.getReaction(fireReactionIndex) );

            //update speciesPopulation
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

//            System.out.println("Update information after firing");
            
//            System.out.print("state");
//            System.out.println(states);

            //update array of propensity            
            long startUpdateTime = System.currentTimeMillis();
            //update data structure
            updatePartialPropensityStructure(fireReactionIndex, reactions, states);

            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);

//            System.out.println("Partial group");
//            for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
//                System.out.println("@Group " + groupIndex);
//                Species s = partialGroup[groupIndex].getSpecies();
//
//                System.out.println(" +Grouping species " + s);
//
//                ArrayList<PartialNode> partialNodeCollection = partialGroup[groupIndex].getAllPartialNode();
//                for(int nodeIndex = 0 ; nodeIndex <  partialNodeCollection.size(); nodeIndex++)
//                {
//                    System.out.println("  @pos " + nodeIndex +": "+ partialNodeCollection.get(nodeIndex));
//                }
//                System.out.println("  Total Partial value : " + partialGroup[groupIndex].getGroupSumPartialValue());
//            }
//
//            System.out.println("Partial propensity");
//            for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
//                System.out.println("@Group: " + groupIndex + " => propensity =  " + partialPropensity[groupIndex]);
//            }
            
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
            
            performanceWriter.writeLine("Time\tFiring\tRunTime\tSearchTime\tUpdateTime");
            performanceWriter.writeLine(currentTime + "\t" + firing + "\t" +simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
            
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }

        return simOutput;
    }

    private void buildPartialPropensityStructure() throws Exception {
        ArrayList<Integer> processedReaction = new ArrayList<Integer>();

        Species[] speciesList = states.getSpeciesList();
        int numberOfNode = speciesList.length + 1;

        partialGroup = new PartialGroup[numberOfNode];
        partialPropensity = new double[numberOfNode];
        
        PartialNode node = null;

        //zeroth order reactionInNode
        partialGroup[0] = new PartialGroup();

        //other types of reactionInNode
        int groupIndex = 1;
        for (Species s : speciesList)
        {
            partialGroup[groupIndex] = new PartialGroup(s);
            mapSpeciesToPartialGroupIndex.put(s, groupIndex);
                
            HashSet<Integer> reactionIndexList = s.getAffectReaction();
            for (int reactionIndex : reactionIndexList) 
            {
                if (processedReaction.contains(reactionIndex)) {
                    continue;
                }

                Reaction r = reactions.getReaction(reactionIndex);
                if(!(r.getRateLaw() instanceof MassActionKinetics )) {
                    throw new Exception("Partial-propensity approach does not support complex reaction kinetics");
                }
                
                ArrayList<Term> reactants = r.getReactants();
                if (reactants.size() == 1) {
                    int minuscoff = -reactants.get(0).getCoff();

                    if (minuscoff == 2) {
                        node = new PartialNode(s, reactionIndex, 0.5 * ComputingMachine.computeRate(r, states) * (states.getPopulation(s) - 1));
                        int nodeIndex = partialGroup[groupIndex].addPartialNode(node);

                        CombineGroupNodeIndex combineIndex = new CombineGroupNodeIndex(groupIndex, nodeIndex);
                        
                        ArrayList<CombineGroupNodeIndex> groupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(s);
                        if(groupNodeIndexList == null){
                            groupNodeIndexList = new ArrayList<CombineGroupNodeIndex>();
                        }
                        groupNodeIndexList.add(combineIndex);
                        
                        mapSpeciesToCombineGroupNodeIndex.put(s, groupNodeIndexList);                        
                    } else {
                        node = new PartialNode(reactionIndex, ComputingMachine.computeRate(r, states));
                        partialGroup[groupIndex].addPartialNode(node);
                    }
                } else if (reactants.size() == 2) {
                    Species second;
                    Species firstSpecies = reactants.get(0).getSpecies();
                    Species secondSpecies = reactants.get(1).getSpecies();

                    if (s.equals(firstSpecies)) {
                        second = secondSpecies;
                    } else {
                        second = firstSpecies;
                    }

                    node = new PartialNode(second, reactionIndex, ComputingMachine.computeRate(r, states) * states.getPopulation(second));
                    int nodeIndex = partialGroup[groupIndex].addPartialNode(node);

                    CombineGroupNodeIndex combineIndex = new CombineGroupNodeIndex(groupIndex, nodeIndex);
                    
                    ArrayList<CombineGroupNodeIndex> groupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(second);
                    if(groupNodeIndexList == null){
                        groupNodeIndexList = new ArrayList<CombineGroupNodeIndex>();
                    }
                    groupNodeIndexList.add(combineIndex);
                    
                    mapSpeciesToCombineGroupNodeIndex.put(second, groupNodeIndexList);
                } 
                else if (reactants.size() > 2) {
                    throw new Exception("Partial-propensity approach does not support high order reaction (order > 2)");
                }
                processedReaction.add(reactionIndex);
            }
            
            partialPropensity[groupIndex] = partialGroup[groupIndex].getGroupSumPartialValue()*states.getPopulation(s);
            groupIndex++;
        }

        //proceed zeroth order reactionInNode
        Reaction[] reactionList = reactions.getReactionList();
        for (Reaction r : reactionList) {
            int reactionIndex = r.getReactionIndex();

            if (!processedReaction.contains(reactionIndex)) {
                node = new PartialNode(reactionIndex, ComputingMachine.computeRate(r, states));
                partialGroup[0].addPartialNode(node);
            }
        }
        partialPropensity[0] = partialGroup[0].getGroupSumPartialValue();
    }

    private void buildPropensity() {
        //compute total propensity
        totalSum = 0;
        for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
            totalSum += partialPropensity[groupIndex];
        }
    }
    private void updatePartialPropensityStructure(int fireReactionIndex, ReactionList reactions, StateList states) {
        Reaction r = reactions.getReaction(fireReactionIndex);

        Species s;
        int coff;
        
        for (Term reactant : r.getReactants()) {
            s = reactant.getSpecies();
            coff = reactant.getCoff();            
//            System.out.println("call updateSpecies " + s + " with coefficient " + coff);
            updateSpecies(s, coff);
        }
        for (Term product : r.getProducts()) {
            s = product.getSpecies();
            coff = product.getCoff();            
//            System.out.println("call updateSpecies " + s + " with coefficient " + coff);
            updateSpecies(s, coff);
        }
    }
    
    private void updateSpecies(Species s, int coff) {
        double delta_a = 0;
       
        //update node species
        ArrayList<CombineGroupNodeIndex> combineGroupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(s);
        if(combineGroupNodeIndexList != null)
        {
            for (CombineGroupNodeIndex combineGroupNodeIndex : combineGroupNodeIndexList) 
            {
                int groupPosition = combineGroupNodeIndex.getGroupIndex();
                int nodePosition = combineGroupNodeIndex.getNodeIndex();

                Species groupSpecies = partialGroup[groupPosition].getSpecies();

                PartialNode node = partialGroup[groupPosition].getPartialNode(nodePosition);
                Reaction reactionInNode = reactions.getReaction(node.getReactionIndex());

                double diff = coff*ComputingMachine.computeRate(reactionInNode, states);
                if (s.equals(groupSpecies)) {
                     diff *= 0.5;
                }

                node.updatePartialValue(diff);
    //                System.out.println(" update node with info " + combineGroupNodeIndex);
    //                System.out.println("  => new partial value " + node.getPartialValue());

                partialGroup[groupPosition].updateGroupSumPartialValue(diff);
    //                System.out.println(" update group " + groupPosition);
    //                System.out.println("  => new partial group value " + partialGroup[groupPosition].getGroupSumPartialValue());

                double temp = partialPropensity[groupPosition];
                partialPropensity[groupPosition] = partialGroup[groupPosition].getGroupSumPartialValue()*states.getPopulation(groupSpecies);
                delta_a += partialPropensity[groupPosition] - temp;
            }
        }
        
        //update the grouping species
        int groupIndex = mapSpeciesToPartialGroupIndex.get(s);
        
        double newTemp = partialGroup[groupIndex].getGroupSumPartialValue()*states.getPopulation(s);
        delta_a += newTemp - partialPropensity[groupIndex];
        partialPropensity[groupIndex] = newTemp;
//        System.out.println(" Group " + groupIndex + " has new partial propensity " + partialPropensity[groupIndex]);
        
        totalSum += delta_a;
//        System.out.println(" => new total sum " + totalSum);
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
