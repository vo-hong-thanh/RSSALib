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
public class GeneratingMultiScaledNetwork {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        DataOutputStream networkWriter;
         
        System.out.println("Current working directory: " + (new File(".")).getCanonicalPath());
        
        System.out.println("Enter network name: ");
        String networkName = reader.readLine();
        
        System.out.println("Number Species A: ");
        int numSpeciesA = Integer.parseInt(reader.readLine());
                
        System.out.println("Number Species B: ");
        int numSpeciesB = Integer.parseInt(reader.readLine());
        
        String speciesAPrefix = "A";
        String speciesBPrefix = "B";
        
        networkWriter = new DataOutputStream(new FileOutputStream(networkName + ".txt"));
        
        networkWriter.writeBytes("### Species ###");
        writeLine(networkWriter);
        for(int i = 1; i <= numSpeciesA; i++)
        {
            networkWriter.writeBytes(speciesAPrefix + i + " = 10000");
            writeLine(networkWriter);
        }
        
        for(int i = 1; i <= numSpeciesB; i++)
        {
            networkWriter.writeBytes(speciesBPrefix + i + " = 100");
            writeLine(networkWriter);
        }
        writeFlush(networkWriter);
                
        networkWriter.writeBytes("### Reactions ###");
        writeLine(networkWriter);
        for(int i = 1; i < numSpeciesA; i++)
        {
            for(int j = i + 1; j <= numSpeciesA; j++)
            {
                networkWriter.writeBytes(speciesAPrefix+i + " -> " + speciesAPrefix+j + " , 100");
                writeLine(networkWriter);
                
                networkWriter.writeBytes(speciesAPrefix+j + " -> "+ speciesAPrefix+i + " , 100");
                writeLine(networkWriter);
            }
        }
        
        for(int i = 1; i <= numSpeciesB; i++)
        {
            int j = (int)(Math.random()*numSpeciesB) + 1;
            int a = (int)(Math.random()*numSpeciesA) + 1;
            
            networkWriter.writeBytes(speciesBPrefix+i + " + " + speciesAPrefix + a + " -> " + speciesBPrefix+j + " , 0.00001");
            writeLine(networkWriter);
             
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
