/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package analysis.networkgenerator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Hong Thanh
 */
public class GeneratingFullyNetwork {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        DataOutputStream networkWriter;
                 
        String speciesPrefix = "S";
        
        System.out.println("Current working directory: " + (new File(".")).getCanonicalPath());
        
        System.out.println("Enter network name: ");
        String networkName = reader.readLine();
        
        System.out.println("Number Species: ");
        int numSpecies = Integer.parseInt(reader.readLine());
        
        networkWriter = new DataOutputStream(new FileOutputStream(networkName + ".txt"));
        
        networkWriter.writeBytes("### Species ###");
        writeLine(networkWriter);
        for(int i = 1; i <= numSpecies; i++)
        {
            networkWriter.writeBytes(speciesPrefix + i + " = 100;");
            writeLine(networkWriter);
        }
        writeFlush(networkWriter);
        
        networkWriter.writeBytes("### Reactions ###");
        writeLine(networkWriter);
        int index = 1;
        for(int i = 1; i < numSpecies; i++)
        {
            for(int j = i + 1; j <= numSpecies; j++)
            {
                networkWriter.writeBytes("r" + index++ +", " + speciesPrefix+i + " -> " + speciesPrefix+j + " , 1;");
                writeLine(networkWriter);
                
                networkWriter.writeBytes("r" + index++ +", " + speciesPrefix+j + " -> "+ speciesPrefix+i + " , 1;");
                writeLine(networkWriter);
            }
        }
        writeFlush(networkWriter);
    }
    
    private static void writeLine(DataOutputStream writer) throws IOException
    {
        writer.write(13);
        writer.write(10);
    }
    private static void writeFlush(DataOutputStream writer) throws IOException
    {
        writer.flush();
    }

}
