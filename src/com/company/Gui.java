package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
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
    private JTextArea preview = new JTextArea();
    private JPanel fieldsPanel;
    private ArrayList<JComboBox<String>> updCB = new ArrayList<JComboBox<String>>();
    private ArrayList<KeyWordWithFrequency> frKWs;
    private String fileNameAbsolute;

    public Gui() {
        GridBagLayout gridbag = new GridBagLayout();
        background = new JPanel(gridbag);

        JButton chooseFileBut = new JButton("Выбрать текстовый файл " + "(в кодировке " + System.getProperty("file.encoding") + ")");
        chooseFileBut.addActionListener(new FileChooseListener());

        fieldsPanel = new JPanel(new GridBagLayout());
        this.addField("Маркер цикла", 1);
        this.addField();

        preview.setEditable(false);

        JButton goParseButton = new JButton("Пуск!");
        goParseButton.addActionListener(new GoParseActionListener());

        GridBagConstraints headerGBCons = new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 20);
        background.add(chooseFileBut, headerGBCons);

        GridBagConstraints fieldsGBCons = new GridBagConstraints(0, 2, 1, 1, 0, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 10, 20), 0, 0);
        background.add(fieldsPanel, fieldsGBCons);

        GridBagConstraints previewCons = new GridBagConstraints(1, 2, 1, 1, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 20), 0, 0);
        background.add(new JScrollPane(preview), previewCons);

        GridBagConstraints goParseButCons = new GridBagConstraints(0, 3, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 20, 0), 200, 20);
        background.add(goParseButton, goParseButCons);

        fr.getContentPane().add(background);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setBounds(200, 150, 700, 500);
        fr.setVisible(true);
    }

    private void addField() {
        this.addField("Ключевая фраза", updCB.size() + 2);
    }

    private void addField(String labelText, int gridY) {
        JLabel defaultLabel = new JLabel(labelText);
        JComboBox<String> defaultComboBox = new JComboBoxPreset<>();

        GridBagConstraints lableConstraints = new GridBagConstraints(1, gridY, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 10, 3, 20), 0, 0);
        GridBagConstraints comboBoxConstraints = new GridBagConstraints(2, gridY, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 3, 0), 0, 0);

        defaultComboBox.setEditable(true);
        updCB.add(defaultComboBox);

        if (frKWs != null) {
            for (KeyWordWithFrequency kw : frKWs) {
                defaultComboBox.addItem(kw.getName());
            }
        }

        defaultComboBox.addActionListener(new NoEmptyFieldsActListener());

        fieldsPanel.add(defaultLabel,lableConstraints);
        fieldsPanel.add(defaultComboBox,comboBoxConstraints);
    }

    private void makePreview(File file) {
        BufferedReader bReader = null;
        preview.setText(null);
        String ending = "И так далее...";

        try {
            bReader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < 15; i++) {
                String s = bReader.readLine();
                if (s == null) {
                    ending = "Конец.";
                    break;
                }
                preview.append(s + "\n");
            }
            preview.append("\n" + ending);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Файл не найден. Превью не заполнено.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("При заполнении превью возникло исключение.");
        }finally {
            try {
                bReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class FileChooseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Path currentDir = Paths.get("").toAbsolutePath();
            JFileChooser fileChooser = new JFileChooser(currentDir.toFile());
            int response = fileChooser.showDialog(null, null);

            if (response == JFileChooser.APPROVE_OPTION) {
                fillGuiFromFile(fileChooser.getSelectedFile());
                fileNameAbsolute = fileChooser.getSelectedFile().toString();
            }
        }

        private void fillGuiFromFile(File file) {
            makePreview(file);
            frKWs = findKWs(file);

            for (JComboBox<String> cb : updCB) {
                cb.removeAllItems();
                for (KeyWordWithFrequency kw : frKWs) {
                    cb.addItem(kw.getName());
                }
            }

            fr.revalidate();
        }

        private ArrayList<KeyWordWithFrequency> findKWs(File file) {
            HashSet<String> strPull = new HashSet<String>();
            ArrayList<KeyWordWithFrequency> possibleKWs = new ArrayList<KeyWordWithFrequency>();
            ArrayList<String> tmpSal;
            TxtReader tr;
            try {
                tr = new TxtReader(file);

                for (int i = 0; i < 50; i++) {
                    tmpSal = new ArrayList<String>();
                    List<String> from_txt = tr.getListFromTxt();

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
//            if (updCB.size() < 10) {
                String s = (String) updCB.get(updCB.size() - 1).getSelectedItem();
                if ((s != null) && (!s.equals(""))) {
                    addField();
                }

                fr.revalidate();
//            }
        }
    }

    class GoParseActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String cw = null;
            ArrayList<String> kws = new ArrayList<String>();

            for (JComboBox<String> comboB : updCB) {
                String s = (String) comboB.getSelectedItem();
                if (cw == null) {
                    cw = s;
                } else if (!s.equals("")){
                    kws.add(s);
                }
            }

            System.out.println(cw);
            System.out.println(kws.toString());

            long time = System.currentTimeMillis();
            DataWH dwh = new DataWH(cw, kws.toArray(new String[0]));
            dwh.parse(fileNameAbsolute);
            System.out.println(((double) (System.currentTimeMillis() - time) / 1000) + " sec");

            makePreview(new File("Отчет.txt"));
        }
    }
}

/**
 * Object for store string with a counter of frequency.
 * <p>Comparable by String <tt>name</tt>.
 * <p>
 * Created by MontolioV on 10.01.17.
 */
class KeyWordWithFrequency implements Comparable<KeyWordWithFrequency>, Serializable {
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
        super.setMinimumSize(new Dimension(200, 20));
        super.setMaximumSize(new Dimension(200, 20));
        super.setPreferredSize(new Dimension(200, 20));
    }

}