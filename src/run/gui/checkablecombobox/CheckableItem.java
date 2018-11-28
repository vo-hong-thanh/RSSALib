/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui.checkablecombobox;

/**
 *
 * @author vot2
 */
public class CheckableItem {
    public final String text;
    public boolean selected;
    public CheckableItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    
    public boolean isSelected()
    {
        return selected;
    }
    
    @Override 
    public String toString() {
        return text;
    }
}
