/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui.checkablecombobox;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

/**
 * CheckedComboBox: Combo box with checkable iterm
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class CheckedComboBox<E extends CheckableItem> extends JComboBox<E> {
    private boolean keepOpen;
    private transient ActionListener listener;

    public CheckedComboBox() {
        super();
    }
    public CheckedComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
    }
//     protected CheckedComboBox(E[] m) {
//         super(m);
//     }
    
    @Override 
    public Dimension getPreferredSize() {
        return new Dimension(200, 20);
    }
    
    @Override 
    public void updateUI() {
        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };
        setRenderer(new CheckBoxCellRenderer<>());
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    ComboPopup pop = (ComboPopup) a;
                    updateItem(pop.getList().getSelectedIndex());
                }
            }
        });
    }
    protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
//             ComboBoxModel m = getModel();
//             if (m instanceof CheckableComboBoxModel) {
//                 ((CheckableComboBoxModel) m).fireContentsChanged(index);
//             }
            // removeItemAt(index);
            // insertItemAt(item, index);
            setSelectedIndex(-1);
            setSelectedItem(item);
        }
    }
    @Override 
    public void setPopupVisible(boolean v) {
        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }
    }
}

class CheckBoxCellRenderer<E extends CheckableItem> implements ListCellRenderer<E> {
    private final JLabel label = new JLabel(" ");
    private final JCheckBox check = new JCheckBox(" ");
    @Override 
    public Component getListCellRendererComponent(JList list, CheckableItem value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0) {
            label.setText(getCheckedItemString(list.getModel()));
            return label;
        } else {
            check.setText(Objects.toString(value, ""));
            check.setSelected(value.selected);
            if (isSelected) {
                check.setBackground(list.getSelectionBackground());
                check.setForeground(list.getSelectionForeground());
            } else {
                check.setBackground(list.getBackground());
                check.setForeground(list.getForeground());
            }
            return check;
        }
    }
    private static String getCheckedItemString(ListModel model) {
        List<String> sl = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
                sl.add(o.toString());
            }
        }
        return sl.stream().sorted().collect(Collectors.joining(", "));
    }
}