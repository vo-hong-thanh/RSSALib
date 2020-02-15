/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.File;
import simulator.IAlgorithm;
import simulator.nondelay.rssa.RSSA;

/**
 *
 * @author Hong Thanh
 */
public class CMDSim_Lotka_Volterra {
    public static void main(String[] args) throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        //build RSSA simulatior 
        IAlgorithm simulator;        
        
        //load Lotka-Volterra model
        String modelFile = "Lotka-Volterra";
        String extension = ".txt";
                
        //run simulation
        double simulationTime = 30;
        double logInterval = 1;
        
        int numRuns = 10000;
        for(int run = 1; run <= numRuns; run++){
            System.out.println("run @"+run);
            simulator = new RSSA();
            simulator.loadModel(modelFile + extension);
            String outputFileName = "RSSA_" + modelFile +"_"+ run + extension;
            simulator.runSim(simulationTime, logInterval, true, outputFileName);
        }
    }
}
