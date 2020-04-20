/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui.separatorcombobox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JSeparator;

/**
 * SeparatorComboBoxListener
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class SeparatorComboBoxListener implements ActionListener {
   private JComboBox combobox;
   private Object oldItem;
     
   public SeparatorComboBoxListener(JComboBox combobox) {
      this.combobox = combobox;
      combobox.setSelectedIndex(0);
      oldItem = combobox.getSelectedItem();
   }
      
   @Override
   public void actionPerformed(ActionEvent e) {
      Object selectedItem = combobox.getSelectedItem();
      if (selectedItem instanceof JSeparator) {
         combobox.setSelectedItem(oldItem);
      } else {
         oldItem = selectedItem;
      }
   }
}