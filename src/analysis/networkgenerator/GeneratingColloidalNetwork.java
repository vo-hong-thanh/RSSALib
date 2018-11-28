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
public class GeneratingColloidalNetwork {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        DataOutputStream networkWriter;
                
        String speciesPrefix = "S";
        
        System.out.println("Current working directory: " + (new File(".")).getCanonicalPath());
        
        System.out.println("Enter network name: ");
        String networkName = reader.readLine();
        
        System.out.println("Number of Species: ");
        int numSpecies = Integer.parseInt(reader.readLine());
        
        networkWriter = new DataOutputStream(new FileOutputStream(networkName + ".txt"));
        
        //population file
        networkWriter.writeBytes("### Species ###");
        writeNewline(networkWriter);
        for(int i = 1; i <= numSpecies; i++)
        {
            int population = (int)(Math.random()*10000);
            networkWriter.writeBytes(speciesPrefix + i + " = " + population);
            writeNewline(networkWriter);
        }
        writeFlush(networkWriter);
        
        //reaction file
        networkWriter.writeBytes("### Reaction ###");
        writeNewline(networkWriter);
        //S_n + S_m -> S_n+m
        for(int n = 1; n <= numSpecies / 2; n++)
        {
            for(int m = n; m <= numSpecies - n; m++)
            {
                networkWriter.writeBytes( (n != m ? (speciesPrefix + n + " + " + speciesPrefix + m) : (2 + speciesPrefix + n) ) + " -> " + speciesPrefix + (n+m) +  " , 1" );
                writeNewline(networkWriter);
            }
        }
        //S_p -> S_q + S_p-q
        for(int p = 1; p <= numSpecies; p++)
        {
            for(int q = 1; q <= p / 2; q++)
            {
                networkWriter.writeBytes(speciesPrefix + p + " -> " + (q != p - q ? (speciesPrefix + q + " + " + speciesPrefix + (p-q)) :  (2 + speciesPrefix + q)) +  " , 1" );
                writeNewline(networkWriter);
            }
        }
        
        writeFlush(networkWriter);
    }
    
    private static void writeNewline(DataOutputStream writer) throws IOException
    {
        writer.write(13);
        writer.write(10);
    }
    private static void writeFlush(DataOutputStream writer) throws IOException
    {
        writer.flush();
    }
}
