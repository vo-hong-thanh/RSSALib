/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * DataWriter: utility for writing file
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class DataWriter {
    private DataOutputStream writer;
    
    public DataWriter(File trackingFile) throws Exception
    {
        writer = new DataOutputStream(new FileOutputStream(trackingFile));
    }
    
    public DataWriter(String trackingFile) throws Exception
    {
        writer = new DataOutputStream(new FileOutputStream(trackingFile));
    }
    
    public void write(String line) throws Exception
    {
        //performance writting
        writer.writeBytes(line);
    }
    
    public void writeLine(String line) throws Exception
    {
        //performance writting
        writer.writeBytes(line);
        writer.write(13);
        writer.write(10);
    }
    
    public void writeLine() throws Exception
    {
        //performance writting
        writer.write(13);
        writer.write(10);
    }
    
    public void flush() throws Exception
    {
        writer.flush();
    }
    
    public void close() throws Exception
    {
        writer.close();
    }
}
