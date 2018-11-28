/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author vot2
 */

import javax.swing.plaf.basic.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
     
public class TestComboboxSeparator extends JFrame
{
   public TestComboboxSeparator() {
      getContentPane().setLayout(new FlowLayout());
       
      final JComboBox combobox =
         new JComboBox(new Object[] {
               "Item 1",
               "Item 2",
               "Item 3",
               new JSeparator(JSeparator.HORIZONTAL),
               "Item 4",
               "Item 5"
            }
         );
  
      getContentPane().add(combobox);
      combobox.setRenderer(new SeparatorComboBoxRenderer());
      combobox.addActionListener(new SeparatorComboBoxListener(combobox));
   
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent we) {
            System.exit(1);
         }
      });     
    
      setSize(new Dimension(200, 200));
   }
  
   public static void main(String[] args) throws Exception {
      TestComboboxSeparator main = new TestComboboxSeparator();
      main.setVisible(true);
   }
}
  
class SeparatorComboBoxRenderer extends BasicComboBoxRenderer implements ListCellRenderer
{
   public SeparatorComboBoxRenderer() {
      super();
   }
    
   public Component getListCellRendererComponent( JList list,
           Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
  
class SeparatorComboBoxListener implements ActionListener {
   JComboBox combobox;
   Object oldItem;
     
   SeparatorComboBoxListener(JComboBox combobox) {
      this.combobox = combobox;
      combobox.setSelectedIndex(0);
      oldItem = combobox.getSelectedItem();
   }
      
   public void actionPerformed(ActionEvent e) {
      Object selectedItem = combobox.getSelectedItem();
      if (selectedItem instanceof JSeparator) {
         combobox.setSelectedItem(oldItem);
      } else {
         oldItem = selectedItem;
      }
   }
}