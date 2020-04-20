/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import sbml.SBMLConverter;

/**
 *
 * @author vot2
 */
public class CMDSBMLConverter {
    public static void main(String[] args) throws Exception{
        System.out.println("[Current working directory: " + (new File(".")).getCanonicalPath() + "]");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("SBML file to convert: ");
        String sbmlFile = "00030-sbml-l3v1.xml";//reader.readLine().trim();
        System.out.print("Converting " + sbmlFile + " ...");
        String outputFile = SBMLConverter.convert(sbmlFile);
        System.out.println();
        System.out.println("Finished converting SBML file: " + sbmlFile + " to " + outputFile);
        
    }
}
