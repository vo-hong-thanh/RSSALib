/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.File;
import simulator.IAlgorithm;
import simulator.nondelay.prssa.PRSSA;
import simulator.nondelay.rssa.RSSA;
import simulator.nondelay.rssa.cr.RSSA_CR;
import simulator.nondelay.rssa.tree_search.RSSA_BinarySearch;

/**
 *
 * @author Hong Thanh
 */
public class CMDSim_Phosphorylation_Syk {
    public static void main(String[] args) throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        //build RSSA simulatior 
        IAlgorithm simulator;
                
        //load FceRI model
        String modelFile = "Phosphorylation-Syk";
        String extension = ".txt";
                
        //run simulation
        double simulationTime = 20;
        double logInterval = 1;
        
        int numRuns = 1000;
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new RSSA();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "RSSA_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new PRSSA();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "PRSSA_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new RSSA_BinarySearch();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "RSSA_Binary_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new RSSA_CR();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "RSSA_CR_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
        
    }
}
