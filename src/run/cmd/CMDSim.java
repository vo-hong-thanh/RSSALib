/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import simulator.IAlgorithm;
import simulator.nondelay.rssa.RSSA;

/**
 *
 * @author Hong Thanh
 */
public class CMDSim {
    public static void main(String[] args)  throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        IAlgorithm simulator;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("Enter number simulation time: ");
        double time = Long.parseLong(reader.readLine().trim());

        System.out.print("Loging time point: ");
        double logInterval = Double.parseDouble(reader.readLine().trim());

        System.out.print("Model file: ");
        String modelFile = reader.readLine().trim();

        String outputFileName;
        
        //rssa
        outputFileName = "RSSA_" + modelFile;
        simulator = new RSSA();
        simulator.loadModel(modelFile);
        simulator.runSim(time, logInterval, true, outputFileName);
    }
}
