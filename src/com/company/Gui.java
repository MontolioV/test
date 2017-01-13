package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * GUI object.
 * <p>
 * Created by MontolioV on 20.12.16.
 */
public class Gui {
    private JFrame fr = new JFrame("test");
    private JPanel background;
    private JPanel fieldsPanel;
    private Box buttonBox = new Box(BoxLayout.Y_AXIS);
    private ArrayList<JComboBox> updCB = new ArrayList<JComboBox>();
    private ArrayList<KeyWordWithFrequency> frKWs;

    public Gui() {
        BorderLayout bl = new BorderLayout();
//        background = new JPanel(bl);
//        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        background = new JPanel();

        GridLayout fieldsGrid = new GridLayout(2, 2);
        fieldsGrid.setHgap(5);
        fieldsGrid.setVgap(5);
        fieldsPanel = new JPanel(fieldsGrid);

        JButton b1 = new JButton("b1");
        b1.addActionListener(new FinderListener());
        buttonBox.add(b1);

        addField("Маркер цикла");
        addField();

        background.add(BorderLayout.CENTER, fieldsPanel);
        background.add(BorderLayout.EAST, buttonBox);

        fr.getContentPane().add(background);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setBounds(150,150,700,700);
        fr.setVisible(true);
    }

    private JLabel addField() {
        JLabel defaultLabel = new JLabel("Ключевая фраза");
        JComboBox<String> defaultComboBox = new JComboBoxPreset();
        GridLayout grid = (GridLayout) fieldsPanel.getLayout();

        defaultComboBox.setEditable(true);
        updCB.add(defaultComboBox);

        if (frKWs != null) {
            for (KeyWordWithFrequency kw : frKWs) {
                defaultComboBox.addItem(kw.getName());
            }
        }

        if (grid.getRows() < updCB.size()) {
            grid.setRows(grid.getRows() + 1);
        }

        defaultComboBox.addActionListener(new NoEmptyFieldsActListener());

        fieldsPanel.add(defaultLabel);
        fieldsPanel.add(defaultComboBox);
        return defaultLabel;
    }

    private void addField(String labelText) {
        JLabel defaultLabel = this.addField();
        defaultLabel.setText(labelText);
    }

    class FinderListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            frKWs = findKWs();

            for (JComboBox cb : updCB) {
                cb.removeAllItems();
                for (KeyWordWithFrequency kw : frKWs) {
                    cb.addItem(kw.getName());
                }
            }

            fr.revalidate();
        }

        private ArrayList<KeyWordWithFrequency> findKWs() {
            HashSet<String> strPull = new HashSet<String>();
            ArrayList<KeyWordWithFrequency> possibleKWs = new ArrayList<KeyWordWithFrequency>();
            ArrayList<String> tmpSal;
            TxtReader tr;
            try {
                //XXX
                tr = new TxtReader("09.txt");

                for (int i = 0; i < 20; i++) {
                    tmpSal = new ArrayList<String>();
                    List<String> from_txt = tr.get_from_txt();

                    if (from_txt == null) {
                        break;
                    }

                    tmpSal.addAll(from_txt);
                    for (String s : tmpSal) {
                        if ((!s.isEmpty()) && (!strPull.add(s))) {
                            KeyWordWithFrequency tmpfrKW = new KeyWordWithFrequency(s);
                            int index = possibleKWs.indexOf(tmpfrKW);
                            if (index < 0) {
                                possibleKWs.add(tmpfrKW);
                            } else {
                                possibleKWs.get(index).riseFrequ();
                            }
                        }
                    }
                }

                Collections.sort(possibleKWs, new KWFrequencyComparator());

                possibleKWs.add(new KeyWordWithFrequency(""));
                Collections.reverse(possibleKWs);

                tr.close_buffer();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            return possibleKWs;
        }
    }

    class NoEmptyFieldsActListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String s = (String) updCB.get(updCB.size() - 1).getSelectedItem();
            if ((s != null) && (!s.equals(""))) {
                addField();
            }

            fr.revalidate();
        }
    }


}

/**
 * Object for store string with a counter of frequency.
 * <p>Comparable by String <tt>name</tt>.
 * <p>
 * Created by MontolioV on 10.01.17.
 */
class KeyWordWithFrequency implements Comparable<KeyWordWithFrequency> {
    private String name;
    private int frequ = 0;

    KeyWordWithFrequency(String name) {
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
        if (!(o instanceof KeyWordWithFrequency)) return false;

        KeyWordWithFrequency that = (KeyWordWithFrequency) o;

        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(KeyWordWithFrequency o) {
        return this.getName().compareTo(o.getName());
    }
}

/**
 * Comparator for {@link KeyWordWithFrequency} to compare it by frequency of appiarence.
 * <p>
 * Created by MontolioV on 10.01.17.
 */
class KWFrequencyComparator implements Comparator<KeyWordWithFrequency> {

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
    public int compare(KeyWordWithFrequency o1, KeyWordWithFrequency o2) {
        return (o1.getFrequ() - o2.getFrequ());
    }
}

/**
 * JComboBox with fixed size.
 * <p>
 * Created by MontolioV on 10.01.17.
 */
class JComboBoxPreset<E> extends JComboBox<E> {
    public JComboBoxPreset(ComboBoxModel<E> aModel) {
        super(aModel);
        myPreset();
    }

    public JComboBoxPreset(E[] items) {
        super(items);
        myPreset();
    }

    public JComboBoxPreset(Vector<E> items) {
        super(items);
        myPreset();
    }

    public JComboBoxPreset() {
        super();
        myPreset();
    }

    private void myPreset() {
        myPresetSize();
    }

    private void myPresetSize() {
        super.setMinimumSize(new Dimension(300, 20));
        super.setMaximumSize(new Dimension(300, 20));
        super.setPreferredSize(new Dimension(300, 20));
    }

}