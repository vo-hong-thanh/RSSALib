/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.Hashtable;
import java.util.Vector;

/**
 * IAlgorithm: interface for simualtion algorithm
 * @author Vo Hong Thanh
 * @version 1.0
*/
public interface IAlgorithm {
//    public void loadModel(String[] speciesInfo, String[] reactionInfo, IRateLaw[] rates) throws Exception;    
        /**
 * load a biochemical reaction model
 * @param modelFilename: name of the model file
 *  
*/
    public void loadModel(String modelFilename) throws Exception;
    
            /**
 * execute the simulation
 * @param _maxTime: simulation time
 * @param _logInterval: logging time interval
 * @param _isWritingFile: check whether the ouput will be written to file
 * @param _outputFilename: name of the file to write to if it write
 * @return a vector contains the states at logging time
*/
    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception;
//    public Hashtable<String, Vector<Double> > runSim(long _maxStep, double _logInterval, boolean _isWritingFile, String outputFilename) throws Exception;
}
