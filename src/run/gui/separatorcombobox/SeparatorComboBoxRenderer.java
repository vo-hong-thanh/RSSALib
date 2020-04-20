/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui.separatorcombobox;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * SeparatorComboBoxRenderer
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class SeparatorComboBoxRenderer extends BasicComboBoxRenderer implements ListCellRenderer
{
   public SeparatorComboBoxRenderer() {
      super();
   }
    
   @Override
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
          setBackground(list.getSelectionBackground());
          setForeground(list.getSelectionForeground());
      }
      else {
          setBackground(list.getBackground());
          setForeground(list.getForeground());
      }
  
      setFont(list.getFont());
      if (value instanceof Icon) {
         setIcon((Icon)value);
      }
      if (value instanceof JSeparator) {
         return (Component) value;
      }
      else {
         setText((value == null) ? "" : value.toString());
      }
  
      return this;
  } 
}