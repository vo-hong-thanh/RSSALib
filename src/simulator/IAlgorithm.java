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
    public void config(long _maxStep, double _maxTime, double _logInterval, String modelFilename, boolean writeOutputFile, String outputFilename) throws Exception;
    public Hashtable<String, Vector<Double> > runSim() throws Exception;
}
