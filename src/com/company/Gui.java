package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Created by user on 20.12.16.
 */
public class Gui {
    JFrame fr = new JFrame("test");
    JButton b1 = new JButton("b1");
    JButton b2 = new JButton("b222222222222222");
    JComboBox cwCombo = new JComboBox();
    JList kwList = new JList();
    GridLayout mainGrid = new GridLayout(1,2);
    JPanel mainPanel = new JPanel(mainGrid);
    Box buttonBox = new Box(BoxLayout.Y_AXIS);
    Box fieldsBox = new Box(BoxLayout.Y_AXIS);


    void go() {
        BorderLayout bl = new BorderLayout();
        JPanel background = new JPanel(bl);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainGrid.setHgap(5);
        mainGrid.setVgap(5);

        b1.addActionListener(new FinderListener());
        buttonBox.add(b1);

        cwCombo.setEditable(true);
        cwCombo.setMaximumSize(new Dimension(300,20));
        fieldsBox.add(cwCombo);

        kwList.setFixedCellWidth(300);
        fieldsBox.add(kwList);

        background.add(BorderLayout.CENTER, fieldsBox);
        background.add(BorderLayout.EAST, buttonBox);

        fr.getContentPane().add(background);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setBounds(50,50,600,600);
        fr.setVisible(true);
    }

    class FinderListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultListModel<String> defListModel = new DefaultListModel<String>();
            cwCombo.removeAllItems();
            kwList.removeAll();
            for (String s : findKWs()) {
                cwCombo.addItem(s);
                defListModel.addElement(s);
            }
            kwList.setModel(defListModel);
        }

        private TreeSet<String> findKWs() {
            HashSet<String> strPull = new HashSet<String>();
            TreeSet<String> ununiqStrs = new TreeSet<String>();
            ArrayList<String> tmpSal;
            TxtReader tr;
            try {
                tr = new TxtReader("09.txt");

                for (int i = 0; i < 10; i++) {
                    tmpSal = new ArrayList<String>();
                    tmpSal.addAll(tr.get_from_txt());
                    for (String s : tmpSal) {
                        if (!strPull.add(s)) {
                            ununiqStrs.add(s);
                        }
                    }
                }

                tr.close_buffer();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            return ununiqStrs;
        }
    }
}
