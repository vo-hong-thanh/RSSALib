/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.prssa;

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
import model.rates.MassActionKinetics;
import simulator.IAlgorithm;
import simulator.nondelay.pdm.CombineGroupNodeIndex;


/**
 * PRSSA: Partial propensity RSSA
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class PRSSA implements IAlgorithm{
    //random generator
    private Random rand = new Random();
    
    //model info
    private StateList states =  new StateList();
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
    
    private long updateStep = 0;
    private long totalTrial = 0;
    
    //info for rejection
    private int threshold = 25;
    private int adjustSize = 4;
    
    private double percentage = 0.1;
    
    private StateList upperStates = new StateList();
    private StateList lowerStates = new StateList();    
    
    //data structure for PRSSA
    private PartialRSSAGroup[] partialRSSAGroup;
    private double[] partialMaxPropensity;
    private Hashtable<Species, Integer> mapSpeciesToPartialGroupIndex = new Hashtable<Species, Integer>();
    private Hashtable<Species, ArrayList<CombineGroupNodeIndex>> mapSpeciesToCombineGroupNodeIndex = new Hashtable<Species, ArrayList<CombineGroupNodeIndex>>();
    
    private double totalMaxPropensity;
    
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

        //build data structure for PRSSA
        buildPartialPropensityStructure();        
        buildPropensity();       
    }

    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception {
        System.out.println("Partial Propensity Rejection-based SSA (PRSSA)");
//        System.out.println("----------------------------"); 
//        System.out.println("------- model information ----------");
//        System.out.print("Reaction");
//        System.out.println(reactions);
//        
//        System.out.print("State");
//        System.out.println(states.toStringFull());
//        
//        System.out.print("Upper State");
//        System.out.println(upperStates.toStringFull());
//        
//        System.out.print("Lower State");
//        System.out.println(lowerStates.toStringFull());
//        
//        System.out.println("------- data structure information --------");
//        System.out.println("Partial group");
//        for (int groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
//            System.out.println("@Group " + groupIndex);
//            Species groupSpecies = partialRSSAGroup[groupIndex].getSpecies();
//            
//            System.out.println(" +Grouping species " + groupSpecies);
//            
//            ArrayList<PartialRSSANode> partialNodeCollection = partialRSSAGroup[groupIndex].getAllPartialRSSANodes();
//            for(int nodeIndex = 0 ; nodeIndex <  partialNodeCollection.size(); nodeIndex++)
//            {
//                System.out.println("  @pos " + nodeIndex +": "+ partialNodeCollection.get(nodeIndex));
//            }
//            System.out.println("  Total Partial value : " + partialRSSAGroup[groupIndex].getMaxPartialValueSum());
//        }
//        
//        System.out.println("Partial propensity");
//        for (int groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
//            System.out.println("@Group: " + groupIndex + " => propensity =  " + partialMaxPropensity[groupIndex]);
//        }
//        
//        System.out.println("Map Species-Partial Group index");
//        for (Enumeration<Species> sc = mapSpeciesToPartialGroupIndex.keys(); sc.hasMoreElements();) {
//            Species groupSpecies = sc.nextElement();
//            int groupIndex = mapSpeciesToPartialGroupIndex.get(groupSpecies);
//            
//            System.out.println(" Grouping Species " + groupSpecies + " is in group " + groupIndex);
//        }
//        
//        System.out.println("Map Species-(Partial Group index, Node index)");
//        for (Enumeration<Species> sc = mapSpeciesToCombineGroupNodeIndex.keys(); sc.hasMoreElements();) {
//            Species groupSpecies = sc.nextElement();
//            ArrayList<CombineGroupNodeIndex> combineGroupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(groupSpecies);
//            
//            System.out.println(" Species " + groupSpecies + " appears in ");
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
        
        double searchValue = 0.0;
        double acceptantProb = 0.0;
        
        int numTrial;    
        int groupIndex;
        int nodeIndex;
                
        int fireReactionIndex = -1;
        
        //start simulation
        long startSimTime = System.currentTimeMillis();        
        do{
            numTrial = 1;
            groupIndex = -1;
            nodeIndex = -1;
                    
            long startSearchTime = System.currentTimeMillis();
            while (true) {   
                //propensity is too small => stop simulation
                if(totalMaxPropensity < 1e-7){
                    break;
                }
                
                //find node in the partial propensity list
                searchValue = rand.nextDouble() * totalMaxPropensity;

    //            System.out.println("random =  " + randomValue);
    //            System.out.println("=> search = random*totalMaxPropensity = " + searchValue);

                double partialSum = 0;
    //            System.out.println("Partial sum " + partialSum);
                for (groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
                    partialSum += partialMaxPropensity[groupIndex];
    //                System.out.println(" after adding to group " + groupIndex + " => " + partialSum);

                    if (partialSum >= searchValue) {
    //                    System.out.println("=> found group " + groupIndex);
                        //find reactionInNode firing
                        int speciesPopulation = 1;                
                        if(groupIndex != 0) {
                            speciesPopulation = upperStates.getPopulation(partialRSSAGroup[groupIndex].getSpecies());
                        }

                        double scaledValue = (searchValue - partialSum + partialMaxPropensity[groupIndex]) / speciesPopulation;
    //                    System.out.println(" scale value " + scaledValue);
                        nodeIndex = partialRSSAGroup[groupIndex].findNodeBySequentialSearch(scaledValue);
    //                    System.out.println("=> found node " + nodeIndex);
                        break;
                    }
                }
                
                //rejection test for candidate
                acceptantProb = rand.nextDouble();
                
                PartialRSSANode node = partialRSSAGroup[groupIndex].getPartialRSSANode(nodeIndex);
                Species groupSpecies = partialRSSAGroup[groupIndex].getSpecies();
                
                double populationRatio;
                populationRatio = (groupIndex == 0 ? 1 : (double)(lowerStates.getPopulation(groupSpecies)) / upperStates.getPopulation(groupSpecies)); 
                if(acceptantProb <= populationRatio*(node.getMinPartialValue() / node.getMaxPartialValue()) ){
                    break;  
                }
                
                populationRatio = (groupIndex == 0 ? 1 : (double)(states.getPopulation(groupSpecies)) / upperStates.getPopulation(groupSpecies)); 
                Reaction r = reactions.getReaction(node.getReactionIndex());
                ArrayList<Term> reactants = r.getReactants();
               
                double partialPropensity = ComputingMachine.computeRate(r, states);
                if (reactants.size() == 1) {
                    int minuscoff = -reactants.get(0).getCoff();
                    if (minuscoff == 2) {
                        partialPropensity *= states.getPopulation(groupSpecies) - 1;
                    }
                } else if (reactants.size() == 2) {
                    Species nodeSpecies = node.getSpecies();
                    partialPropensity *= states.getPopulation(nodeSpecies);
                }
                
                if(acceptantProb <= populationRatio*(partialPropensity / node.getMaxPartialValue()) )
                {
                    break;
                }
                
                numTrial++;
            }            
            fireReactionIndex = partialRSSAGroup[groupIndex].getPartialRSSANode(nodeIndex).getReactionIndex();

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
                

            //update speciesPopulation
            ComputingMachine.executeReaction(fireReactionIndex, reactions, states);

//            System.out.println("Update information after firing");
            
//            System.out.print("state");
//            System.out.println(states);

            //update array of propensity            
            long startUpdateTime = System.currentTimeMillis();
            //update data structure
            if(updatePartialPropensityStructure(fireReactionIndex))
            {
                updateStep++;
            }
            long endUpdateTime = System.currentTimeMillis();
            updateTime += (endUpdateTime - startUpdateTime);

//          firing            
//            System.out.println("@Step: " + firing + ", @Time: " + currentTime + " => Fire reaction " + reactions.getReaction(fireReactionIndex) );
            totalTrial += numTrial;            
            firing++;            
            
//            System.out.println("Partial group");
//            for (int groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
//                System.out.println("@Group " + groupIndex);
//                Species groupSpecies = partialRSSAGroup[groupIndex].getSpecies();
//
//                System.out.println(" +Grouping species " + groupSpecies);
//
//                ArrayList<PartialNode> partialNodeCollection = partialRSSAGroup[groupIndex].getAllPartialRSSANodes();
//                for(int nodeIndex = 0 ; nodeIndex <  partialNodeCollection.size(); nodeIndex++)
//                {
//                    System.out.println("  @pos " + nodeIndex +": "+ partialNodeCollection.get(nodeIndex));
//                }
//                System.out.println("  Total Partial value : " + partialRSSAGroup[groupIndex].getMaxPartialValueSum());
//            }
//
//            System.out.println("Partial propensity");
//            for (int groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
//                System.out.println("@Group: " + groupIndex + " => propensity =  " + partialMaxPropensity[groupIndex]);
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

            performanceWriter.writeLine("Time\tFiring\tTrial\tUpdate\tRunTime\tSearchTime\tUpdateTime");
            performanceWriter.writeLine(currentTime + "\t" + firing + "\t" + totalTrial +"\t" + updateStep + "\t" + simTime/1000.0 + "\t" + searchTime/1000.0 + "\t" + updateTime/1000.0);
                
            dataWriter.flush();
            dataWriter.close();

            performanceWriter.flush();
            performanceWriter.close(); 
        }        
        
        
        return simOutput;
    }

    private void buildPartialPropensityStructure() throws Exception {
        Species[] speciesList = states.getSpeciesList();
        int numberOfNode = speciesList.length + 1;

        //build fluctuation interval
        for (Species s : speciesList) {
            if(!s.isProductOnly())
            {
                computeIntervalSpecies(s);
            }
        }
        
        //build partial propensity structure
        ArrayList<Integer> processedReaction = new ArrayList<Integer>();
        
        partialRSSAGroup = new PartialRSSAGroup[numberOfNode];
        partialMaxPropensity = new double[numberOfNode];
        
        PartialRSSANode node = null;

        //zeroth order reactionInNode
        partialRSSAGroup[0] = new PartialRSSAGroup();

        //other types of reactionInNode
        int groupIndex = 1;
        for (Species s : speciesList) {
            partialRSSAGroup[groupIndex] = new PartialRSSAGroup(s);
            mapSpeciesToPartialGroupIndex.put(s, groupIndex);
                
            HashSet<Integer> reactionIndexList = s.getAffectReaction();
            for (int reactionIndex : reactionIndexList) {
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

                    if (minuscoff == 1) {
                        node = new PartialRSSANode(reactionIndex, ComputingMachine.computeRate(r, upperStates), ComputingMachine.computeRate(r, lowerStates));
                        partialRSSAGroup[groupIndex].addPartialRSSANode(node);
                    }
                    else if (minuscoff == 2) {
                        node = new PartialRSSANode(s, reactionIndex, 0.5 * ComputingMachine.computeRate(r, upperStates) * (upperStates.getPopulation(s) - 1), 0.5 * ComputingMachine.computeRate(r, lowerStates) * (lowerStates.getPopulation(s) - 1));
                        int nodeIndex = partialRSSAGroup[groupIndex].addPartialRSSANode(node);

                        CombineGroupNodeIndex combineIndex = new CombineGroupNodeIndex(groupIndex, nodeIndex);
                        
                        ArrayList<CombineGroupNodeIndex> groupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(s);
                        if(groupNodeIndexList == null){
                            groupNodeIndexList = new ArrayList<CombineGroupNodeIndex>();
                        }
                        groupNodeIndexList.add(combineIndex);
                        
                        mapSpeciesToCombineGroupNodeIndex.put(s, groupNodeIndexList);                        
                    }
                    else if (minuscoff > 2) {
                        throw new Exception("Partial-propensity approach does not support high order reaction (order > 2)");
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

                    node = new PartialRSSANode(second, reactionIndex, ComputingMachine.computeRate(r, upperStates) * upperStates.getPopulation(second), ComputingMachine.computeRate(r, lowerStates) * lowerStates.getPopulation(second));
                    int nodeIndex = partialRSSAGroup[groupIndex].addPartialRSSANode(node);

                    CombineGroupNodeIndex combineIndex = new CombineGroupNodeIndex(groupIndex, nodeIndex);
                    
                    ArrayList<CombineGroupNodeIndex> groupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(second);
                    if(groupNodeIndexList == null){
                        groupNodeIndexList = new ArrayList<CombineGroupNodeIndex>();
                    }
                    groupNodeIndexList.add(combineIndex);
                    
                    mapSpeciesToCombineGroupNodeIndex.put(second, groupNodeIndexList);
                } 
                else if (reactants.size() > 2) {
                    throw new Exception("PDM does not support high order reaction (order > 2)");
                }
                processedReaction.add(reactionIndex);
            }
            
            partialMaxPropensity[groupIndex] = partialRSSAGroup[groupIndex].getMaxPartialValueSum()*upperStates.getPopulation(s);
            groupIndex++;
        }

        //proceed zeroth order reactionInNode
        Reaction[] reactionList = reactions.getReactionList();
        for (Reaction r : reactionList) {
            int reactionIndex = r.getReactionIndex();

            if (!processedReaction.contains(reactionIndex)) {
                node = new PartialRSSANode(reactionIndex, ComputingMachine.computeRate(r, upperStates), ComputingMachine.computeRate(r, lowerStates));
                partialRSSAGroup[0].addPartialRSSANode(node);
            }
        }
        partialMaxPropensity[0] = partialRSSAGroup[0].getMaxPartialValueSum();
    }

    private void buildPropensity() {
        //compute total propensity
        totalMaxPropensity = 0;
        for (int groupIndex = 0; groupIndex < partialRSSAGroup.length; groupIndex++) {
            totalMaxPropensity += partialMaxPropensity[groupIndex];
        }
    }
    private boolean updatePartialPropensityStructure(int fireReactionIndex) {
        Reaction fireReaction = reactions.getReaction(fireReactionIndex);
        Species s;

        HashSet<Species> needUpdateSpecies = new HashSet<Species>();

        //update population
        for (Term reactant : fireReaction.getReactants()) {
            s = reactant.getSpecies();
            int pop = states.getPopulation(s);
            
            if ( pop <= 0 || (pop != 0 && pop < lowerStates.getPopulation(s)) ) {
                computeIntervalSpecies(s);
                needUpdateSpecies.add(s);
            }
        }

        for (Term product : fireReaction.getProducts()) {
            s = product.getSpecies();

            if (!s.isProductOnly() && states.getPopulation(s) > upperStates.getPopulation(s)) {
                computeIntervalSpecies(s);
                needUpdateSpecies.add(s);
            }
        }
        
        if(needUpdateSpecies.isEmpty()){
            return false;
        }
        else{
            for(Species species : needUpdateSpecies ) {
                updateSpecies(species);
            }
            return true;
        }
    }
    
    private void updateSpecies(Species s) {
        double delta_a = 0;
       
        //update node species
        ArrayList<CombineGroupNodeIndex> combineGroupNodeIndexList = mapSpeciesToCombineGroupNodeIndex.get(s);
        if(combineGroupNodeIndexList != null)
        {
            for (CombineGroupNodeIndex combineGroupNodeIndex : combineGroupNodeIndexList) 
            {
                int groupPosition = combineGroupNodeIndex.getGroupIndex();
                int nodePosition = combineGroupNodeIndex.getNodeIndex();

                Species groupSpecies = partialRSSAGroup[groupPosition].getSpecies();

                PartialRSSANode node = partialRSSAGroup[groupPosition].getPartialRSSANode(nodePosition);
                Reaction reactionInNode = reactions.getReaction(node.getReactionIndex());
                
                double currentMaxPartialValue = node.getMaxPartialValue();
                double newMaxPartialValue;                
                if (s.equals(groupSpecies)) {
                    newMaxPartialValue = 0.5*ComputingMachine.computeRate(reactionInNode, upperStates)*(upperStates.getPopulation(s) - 1);
                    node.setMaxPartialValue(newMaxPartialValue);
                    node.setMinPartialValue(0.5*ComputingMachine.computeRate(reactionInNode, lowerStates)*(lowerStates.getPopulation(s) - 1));
                }
                else {
                    newMaxPartialValue = ComputingMachine.computeRate(reactionInNode, upperStates)*upperStates.getPopulation(s);
                    node.setMaxPartialValue(newMaxPartialValue);
                    node.setMinPartialValue(ComputingMachine.computeRate(reactionInNode, lowerStates)*lowerStates.getPopulation(s));
                }
                
                double diff = newMaxPartialValue - currentMaxPartialValue;
                partialRSSAGroup[groupPosition].updateMaxPartialValueSum(diff);
    //                System.out.println(" update group " + groupPosition);
    //                System.out.println("  => new partial group value " + partialRSSAGroup[groupPosition].getMaxPartialValueSum());

                double temp = partialMaxPropensity[groupPosition];
                partialMaxPropensity[groupPosition] = partialRSSAGroup[groupPosition].getMaxPartialValueSum()*upperStates.getPopulation(groupSpecies);
                delta_a += partialMaxPropensity[groupPosition] - temp;
            }
        }
        
        //update the grouping species
        int groupIndex = mapSpeciesToPartialGroupIndex.get(s);
        
        double newTemp = partialRSSAGroup[groupIndex].getMaxPartialValueSum()*upperStates.getPopulation(s);
        delta_a += newTemp - partialMaxPropensity[groupIndex];
        partialMaxPropensity[groupIndex] = newTemp;
//        System.out.println(" Group " + groupIndex + " has new partial propensity " + partialMaxPropensity[groupIndex]);
        
        totalMaxPropensity += delta_a;
//        System.out.println(" => new total sum " + totalMaxPropensity);
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
            upperStates.updateSpecies(s, (int) (pop * (1 + percentage)) );
            lowerStates.updateSpecies(s, (int) (pop * (1 - percentage)) );
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
