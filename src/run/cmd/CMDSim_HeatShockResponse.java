/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.File;
import simulator.IAlgorithm;
import simulator.nondelay.gillespie.ModifiedDM;
import simulator.nondelay.prssa.PRSSA;
import simulator.nondelay.rssa.ModifiedRSSA;
import simulator.nondelay.rssa.RSSA;
import simulator.nondelay.rssa.tree_search.RSSA_Binary;

/**
 *
 * @author Hong Thanh
 */
public class CMDSim_HeatShockResponse {
    public static void main(String[] args) throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        //build RSSA simulatior 
        IAlgorithm simulator;
                
        //load Lotka-Volterra model
        String modelFile = "Heat-shock-response";
        String extension = ".txt";
                
        //run simulation
        double simulationTime = 100;
        double logInterval = 10;
        
        int numRuns = 10;
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new PRSSA();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "RSSA_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
//        for(int run = 1; run <= numRuns; run++)
//        {
//            System.out.println("run @"+run);
//            simulator = new DelayedMNRM();
//            simulator.loadModel(modelFile+extension);
//            
//            String outputFileName = "DNRM_" + modelFile + "_" + run + extension;
//            simulator.runSim(simulationTime, logInterval, true, outputFileName);
//        }
    }
}
