/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.nondelay.pdm.cr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import model.Reaction;
import model.ReactionList;
import model.Species;
import model.StateList;
import model.Term;
import utils.ComputingMachine;
import utils.DataWriter;
import java.util.Vector;
import model.kinetics.MassActionKinetics;
import simulator.IAlgorithm;
import simulator.nondelay.pdm.CombineGroupNodeIndex;
import simulator.nondelay.pdm.PartialGroup;
import simulator.nondelay.pdm.PartialNode;

/**
 *
 * @author vo
 */
public class PSSA_CR implements IAlgorithm{
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
    
    private int maxGroupExponent;
    private int minGroupExponent;
    private LinkedList<CRPartialGroupBlock> crgroup_list;    
        
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

        //build bipartie dependency
        ComputingMachine.buildSpecieReactionDependency(reactions, states);

        //build data structure for PDM
        buildPartialPropensityStructure();        
        buildPropensity();
        
        //writer
        this.willWriteFile = _isWriteable;     
        outputFile = outputFilename;
        
        //output
        initalizeOutput();
    }

    public Hashtable<String, Vector<Double> > runSim() throws Exception {
        System.out.println("Partial-propensity direct method with composition-rejection");
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
//        for (int partialGroupIndex = 0; partialGroupIndex < partialGroup.length; partialGroupIndex++) {
//            System.out.println("@Group " + partialGroupIndex);
//            Species s = partialGroup[partialGroupIndex].getSpecies();
//            
//            System.out.println(" +Grouping species " + s);
//            
//            ArrayList<PartialNode> partialNodeCollection = partialGroup[partialGroupIndex].getAllPartialNode();
//            for(int partialNodeIndex = 0 ; partialNodeIndex <  partialNodeCollection.size(); partialNodeIndex++)
//            {
//                System.out.println("  @pos " + partialNodeIndex +": "+ partialNodeCollection.get(partialNodeIndex));
//            }
//            System.out.println("  Total Partial value : " + partialGroup[partialGroupIndex].getGroupSumPartialValue());
//        }
//        
//        System.out.println("Partial propensity");
//        for (int partialGroupIndex = 0; partialGroupIndex < partialGroup.length; partialGroupIndex++) {
//            System.out.println("@Group: " + partialGroupIndex + " => propensity =  " + partialPropensity[partialGroupIndex]);
//        }
//        
//        System.out.println("Map Species-Partial Group index");
//        for (Enumeration<Species> sc = mapSpeciesToPartialGroupIndex.keys(); sc.hasMoreElements();) {
//            Species s = sc.nextElement();
//            int partialGroupIndex = mapSpeciesToPartialGroupIndex.get(s);
//            
//            System.out.println(" Grouping Species " + s + " is in group " + partialGroupIndex);
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

            if(!simulationByStep && currentTime >= maxTime)
                currentTime = maxTime;
            
            //find node in the partial propensity list
            int fireReactionIndex = -1;

            long startSearchTime = System.currentTimeMillis();     
		
            //select a group via linear search
            int crIndex = -1;
            int partialGroupIndex = -1;         
            double searchGroupValue = rand.nextDouble()*totalSum;
            double partialGroupSum = 0;
            for(crIndex = 0; crIndex < crgroup_list.size(); crIndex++){
                partialGroupSum += crgroup_list.get(crIndex).getBlockSum();
                if(partialGroupSum >= searchGroupValue) {
                    break;
                }
            }
    //        System.out.println(" + found CRPartialGroupBlock " + crIndex);  

            //select reaction in group via rejection
            CRPartialGroupBlock g = crgroup_list.get(crIndex);
            ArrayList<Integer> partialGroupIndexInCRPartialGroup = g.getPartialGroupIndexInCRPartialGroupBlock();

            double maxGroupPropensity = g.getMaxBlockValue();
            
            int randomIndexInCRPartialGroup = -1;
            double randomCheckingValue;

            if (partialGroupIndexInCRPartialGroup.size() == 1) { //if there is only one reaction in the group, choose it
                partialGroupIndex = partialGroupIndexInCRPartialGroup.get(0);
            } else {
                while (true) {
                    //generate an integer index between 0 and size of group-1 as tentative reaction index 
                    randomIndexInCRPartialGroup = (int) (rand.nextDouble() * partialGroupIndexInCRPartialGroup.size());

                    //generate a continuous number within the group's propensity range
                    randomCheckingValue = rand.nextDouble() * maxGroupPropensity;
                    //if randomCheckingValue is less than the propensity of tentativeReaction propensity then accept the reaction
                    
                    partialGroupIndex = partialGroupIndexInCRPartialGroup.get(randomIndexInCRPartialGroup);
                    if (randomCheckingValue <= partialPropensity[partialGroupIndex]) {
    //                        System.out.println(" + found reaction " + reactionIndex);
                       break;
                    }
                }
            }
            
            double searchNodeValue = rand.nextDouble() * partialGroup[partialGroupIndex].getGroupSumPartialValue();
            int partialNodeIndex = partialGroup[partialGroupIndex].findNodeBySequentialSearch(searchNodeValue);
//            int partialNodeIndex = partialGroup[partialGroupIndex].findNodeByCRSearch(rand);
//            System.out.println("=> found node " + partialNodeIndex);

            fireReactionIndex = partialGroup[partialGroupIndex].getPartialNode(partialNodeIndex).getReactionIndex();

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
//            for (int partialGroupIndex = 0; partialGroupIndex < partialGroup.length; partialGroupIndex++) {
//                System.out.println("@Group " + partialGroupIndex);
//                Species s = partialGroup[partialGroupIndex].getSpecies();
//
//                System.out.println(" +Grouping species " + s);
//
//                ArrayList<PartialNode> partialNodeCollection = partialGroup[partialGroupIndex].getAllPartialNode();
//                for(int partialNodeIndex = 0 ; partialNodeIndex <  partialNodeCollection.size(); partialNodeIndex++)
//                {
//                    System.out.println("  @pos " + partialNodeIndex +": "+ partialNodeCollection.get(partialNodeIndex));
//                }
//                System.out.println("  Total Partial value : " + partialGroup[partialGroupIndex].getGroupSumPartialValue());
//            }
//
//            System.out.println("Partial propensity");
//            for (int partialGroupIndex = 0; partialGroupIndex < partialGroup.length; partialGroupIndex++) {
//                System.out.println("@Group: " + partialGroupIndex + " => propensity =  " + partialPropensity[partialGroupIndex]);
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
        
        //build CR data structure for group
        buildCRPartialGroupList();
        
//        //build CR data structure for node
//        for (int groupIndex = 0; groupIndex < partialGroup.length; groupIndex++) {
//            partialGroup[groupIndex].buildCRPartialNodeList();
//        }
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

//                double oldValue = node.getPartialValue();
                node.updatePartialValue(diff);
                
    //                System.out.println(" update node with info " + combineGroupNodeIndex);
    //                System.out.println("  => new partial value " + node.getPartialValue());

                partialGroup[groupPosition].updateGroupSumPartialValue(diff);
    //                System.out.println(" update group " + groupPosition);
    //                System.out.println("  => new partial group value " + partialGroup[groupPosition].getGroupSumPartialValue());

//                System.out.println("Update: Group index = " + groupPosition + ", Node index " + nodePosition);
//                partialGroup[groupPosition].updateCRPartialNodeList(nodePosition, oldValue);

                double temp = partialPropensity[groupPosition];
                partialPropensity[groupPosition] = partialGroup[groupPosition].getGroupSumPartialValue()*states.getPopulation(groupSpecies);
                
                updateCRPartialGroupList(groupPosition, temp, partialPropensity[groupPosition]);
                
                delta_a += partialPropensity[groupPosition] - temp;
            }
        }
        
        //update the grouping species
        int partialGroupIndex = mapSpeciesToPartialGroupIndex.get(s);
        
        double newTemp = partialGroup[partialGroupIndex].getGroupSumPartialValue()*states.getPopulation(s);
        delta_a += newTemp - partialPropensity[partialGroupIndex];
        
        updateCRPartialGroupList(partialGroupIndex, partialPropensity[partialGroupIndex], newTemp);
                
        partialPropensity[partialGroupIndex] = newTemp;
//        System.out.println(" Group " + partialGroupIndex + " has new partial propensity " + partialPropensity[partialGroupIndex]);
        
        totalSum += delta_a;
//        System.out.println(" => new total sum " + totalSum);
    }
    
    private void buildCRPartialGroupList() {
//        System.out.println(" --- Build group ---");        
        crgroup_list = new LinkedList<CRPartialGroupBlock>();

        boolean oneNonZeroPropensity = false;
        
        for (int partialGroupIndex = 0 ; partialGroupIndex < partialGroup.length; partialGroupIndex++) {
            double propensity = partialPropensity[partialGroupIndex];

            if (!oneNonZeroPropensity) {
                if (propensity > 0.0) {
//                    System.out.println(" (This is first non-zero propensity) "); 
                    oneNonZeroPropensity = true;

                    int exponent = ComputingMachine.calculateGroupExponent(propensity);
            
                    minGroupExponent = exponent;
                    maxGroupExponent = exponent;

                    CRPartialGroupBlock newGroup = new CRPartialGroupBlock(exponent);
                    newGroup.insert(partialGroupIndex, propensity);

//                    System.out.println(" put it to group " + getCRPartialGroupIndex(propensity)); 
            
                    crgroup_list.addFirst(newGroup);
                    
                }
            } else {
                int newGroupIndex = getCRPartialGroupIndex(propensity);
//                System.out.println(" tentative group index " + newGroupIndex); 
            
                if (newGroupIndex == -1) {
                    //either need to add new group or do nothing (propensity = 0)
                    if (propensity != 0.0) {
                        addCRPartialGroup(ComputingMachine.calculateGroupExponent(propensity));
                        newGroupIndex = getCRPartialGroupIndex(propensity);//group index changed
                        crgroup_list.get(newGroupIndex).insert(partialGroupIndex, propensity);
                        
//                        System.out.println(" since it does not exist => create a new group " + newGroupIndex); 
                    }
                }else { // insert new reaction into group
//                    System.out.println(" since it exists => put to the group " + newGroupIndex); 
            
                    crgroup_list.get(newGroupIndex).insert(partialGroupIndex, propensity);
                }                
            }
        }

        if (!oneNonZeroPropensity) {
            throw new RuntimeException("All propensities are zero.");
        }
    }

    private void updateCRPartialGroupList(int partialGroupIndex, double oldPropensity, double newPropensity) {
        int oldGroupIndex = getCRPartialGroupIndex(oldPropensity);
        int newGroupIndex = getCRPartialGroupIndex(newPropensity);

//        System.out.println(" change propensity from: " + oldPropensity + " (group index: " + oldGroupIndex +")" + " to: " + newPropensity + " (group index: " + newGroupIndex +")");
        
        if (newGroupIndex == oldGroupIndex) {
            if (newGroupIndex == -1) {                
                //either need to add new group or do nothing (propensity = 0)
                if (newPropensity != 0.0) {
                    addCRPartialGroup(ComputingMachine.calculateGroupExponent(newPropensity));
                    newGroupIndex = getCRPartialGroupIndex(newPropensity);//group index changed
                    
//                    System.out.println(" => create new group " + newGroupIndex);
                    
                    crgroup_list.get(newGroupIndex).insert(partialGroupIndex, newPropensity);
                }
            } else {
//                System.out.println(" => update group " + newGroupIndex);
                //did not change group, simple update
                crgroup_list.get(newGroupIndex).update(oldPropensity, newPropensity);
            }
        } else {//changed group
            //remove from old group
            if (oldGroupIndex != -1) {
//                System.out.println(" remove from group index " + oldGroupIndex);
                crgroup_list.get(oldGroupIndex).remove(partialGroupIndex, oldPropensity);
                //groups[oldGroupIndex].remove(reactionIndex,withinGroupIndexes[reactionIndex],withinGroupIndexes);
            }

            //add to new group
//            System.out.println(" to new group index " + newGroupIndex);
            if (newGroupIndex == -1) {
                if (newPropensity > 0) {
                    //need to add a group
                    addCRPartialGroup(ComputingMachine.calculateGroupExponent(newPropensity));
                    newGroupIndex = getCRPartialGroupIndex(newPropensity);//group index changed
                    
//                    System.out.println("  => create new group " + newGroupIndex);
                    
                    crgroup_list.get(newGroupIndex).insert(partialGroupIndex, newPropensity);
                }
            } else {
//                System.out.println("  => insert reaction to group " + newGroupIndex);
                crgroup_list.get(newGroupIndex).insert(partialGroupIndex, newPropensity);
            }
        }
    }    

    private void addCRPartialGroup(int newGroupExponent) {
        while (newGroupExponent < minGroupExponent) {
            CRPartialGroupBlock newGroup = new CRPartialGroupBlock(--minGroupExponent);
            crgroup_list.addLast(newGroup);
        }
        while (newGroupExponent > maxGroupExponent) {
            CRPartialGroupBlock newGroup = new CRPartialGroupBlock(++maxGroupExponent);
            crgroup_list.addFirst(newGroup);
        }
    }
    
    private int getCRPartialGroupIndex(double propensityValue) {
        if (propensityValue == 0.0) {
            return -1;
        } else {
            int exponent = ComputingMachine.calculateGroupExponent(propensityValue);
            if (exponent >= minGroupExponent && exponent <= maxGroupExponent) {
                return maxGroupExponent - exponent;
            } else {
                return -1;
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
