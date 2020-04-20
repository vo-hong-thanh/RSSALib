/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * ModelFileFilter: File filter
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class ModelFileFilter extends FileFilter{
    private String extension;
    private String Description;
    
    public ModelFileFilter(String extension, String description)
    {
        this.extension = extension;
        this.Description = description;
    }
    
    
    @Override
    public boolean accept(File file) {
        if(file.isDirectory()){
            return true;
        }
        else{
            return file.getName().endsWith(extension);
        }
    }

    @Override
    public String getDescription() {
        return Description + String.format(" (%s)", extension);
    }    
}
