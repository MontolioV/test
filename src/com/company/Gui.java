package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

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
    JScrollPane scrollPane;


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

        //kwList.setSelectionMode();

        scrollPane = new JScrollPane(kwList);
        scrollPane.setMaximumSize(new Dimension(300,120));
        fieldsBox.add(scrollPane);

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

                for (int i = 0; i < 20; i++) {
                    tmpSal = new ArrayList<String>();
                    List<String> from_txt = tr.get_from_txt();
                    if (from_txt == null) {
                        break;
                    }
                    tmpSal.addAll(from_txt);
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

    class KWFrequencyComparator implements Comparator<KWwithFrequency> {

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p>
         * In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.<p>
         * <p>
         * The implementor must ensure that <tt>sgn(compare(x, y)) ==
         * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>compare(x, y)</tt> must throw an exception if and only
         * if <tt>compare(y, x)</tt> throws an exception.)<p>
         * <p>
         * The implementor must also ensure that the relation is transitive:
         * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
         * <tt>compare(x, z)&gt;0</tt>.<p>
         * <p>
         * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
         * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
         * <tt>z</tt>.<p>
         * <p>
         * It is generally the case, but <i>not</i> strictly required that
         * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
         * any comparator that violates this condition should clearly indicate
         * this fact.  The recommended language is "Note: this comparator
         * imposes orderings that are inconsistent with equals."
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * first argument is less than, equal to, or greater than the
         * second.
         * @throws NullPointerException if an argument is null and this
         *                              comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from
         *                              being compared by this comparator.
         */
        @Override
        public int compare(KWwithFrequency o1, KWwithFrequency o2) {
            return (o1.getFrequ() - o2.getFrequ());
        }
    }
}

class KWwithFrequency {
    private String name;
    private int frequ = 0;

    KWwithFrequency(String name) {
        this.name = name;
    }

    public int getFrequ() {
        return frequ;
    }

    public void riseFrequ() {
        this.frequ += 1;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KWwithFrequency)) return false;

        KWwithFrequency that = (KWwithFrequency) o;

        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}