/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.File;
import simulator.IAlgorithm;
import simulator.delay.delayed_mnrm.DelayedMNRM;
import simulator.delay.delayed_rssa.DelayedRSSA;

/**
 *
 * @author Hong Thanh
 */
public class CMDSim_Hes1 {
    public static void main(String[] args) throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        //build RSSA simulatior 
        IAlgorithm simulator;
                
        //load Lotka-Volterra model
        String modelFile = "Hes1";
        String extension = ".txt";
                
        //run simulation
        double simulationTime = 500;
        double logInterval = 50;
        
        int numRuns = 1000;
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new DelayedRSSA();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "DRSSA_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
        
        for(int run = 1; run <= numRuns; run++)
        {
            System.out.println("run @"+run);
            simulator = new DelayedMNRM();
            simulator.loadModel(modelFile+extension);
            
            String outputFileName = "DNRM_" + modelFile + "_" + run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
    }
}
