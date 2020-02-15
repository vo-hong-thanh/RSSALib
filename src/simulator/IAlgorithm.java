/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author vot2
 */
public interface IAlgorithm {
//    public void loadModel(String[] speciesInfo, String[] reactionInfo, IRateLaw[] rates) throws Exception;
    public void loadModel(String modelFilename) throws Exception;
    public Hashtable<String, Vector<Double> > runSim(double _maxTime, double _logInterval, boolean _isWritingFile, String _outputFilename) throws Exception;
//    public Hashtable<String, Vector<Double> > runSim(long _maxStep, double _logInterval, boolean _isWritingFile, String outputFilename) throws Exception;
}
