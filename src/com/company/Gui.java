package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    private JMenuBar menuBar = new JMenuBar();;
    private JPanel background;
    private JTextArea preview = new JTextArea();
    private JPanel fieldsPanel;
    private ArrayList<JComboBox<String>> updCB = new ArrayList<JComboBox<String>>();
    private ArrayList<KeyWordWithFrequency> frKWs;
    private String fileNameAbsolute;
    private JProgressBar progressBar;

    public Gui() {
        JMenu mainMenu = new JMenu("Файл");
        JMenuItem menuSaveButton = new JMenuItem("Сохранить");
        JMenuItem menuLoadButton = new JMenuItem("Загрузить");
        menuSaveButton.addActionListener(new SaveListener());
        menuLoadButton.addActionListener(new LoadListener());
        mainMenu.add(menuSaveButton);
        mainMenu.add(menuLoadButton);
        menuBar.add(mainMenu);

        background = new JPanel(new GridBagLayout());

        JButton chooseFileBut = new JButton("Выбрать текстовый файл " + "(в кодировке " + System.getProperty("file.encoding") + ")");
        chooseFileBut.addActionListener(new OpenListener());

        fieldsPanel = new JPanel(new GridBagLayout());
        this.addField("Маркер цикла", 1);
        this.addField();

        preview.setEditable(false);

        JButton goParseButton = new JButton("Пуск!");
        goParseButton.addActionListener(new GoParseActionListener());

        //ChooseFile
        GridBagConstraints headerGBCons = new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 20);
        background.add(chooseFileBut, headerGBCons);

        //Fields
        GridBagConstraints fieldsGBCons = new GridBagConstraints(0, 2, 1, 1, 0, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 10, 20), 0, 0);
        background.add(fieldsPanel, fieldsGBCons);

        //Preview
        GridBagConstraints previewCons = new GridBagConstraints(1, 2, 1, 1, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 20), 0, 0);
        background.add(new JScrollPane(preview), previewCons);

        //Progress bar
        progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        GridBagConstraints progressBarCons = new GridBagConstraints(0, 3, 2, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 20, 20), 0, 10);
        background.add(progressBar, progressBarCons);

        //Start button
        GridBagConstraints goParseButCons = new GridBagConstraints(0, 4, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 20, 0), 200, 20);
        background.add(goParseButton, goParseButCons);

        fr.setJMenuBar(menuBar);
        fr.getContentPane().add(background);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setBounds(200, 150, 700, 500);
        fr.setVisible(true);
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

    private JFileChooser makeSerialisFChooser() {
        Path saveDir = Paths.get("","Saves").toAbsolutePath();
        if (!saveDir.toFile().exists()) {
            if (!saveDir.toFile().mkdir()) {
                System.out.println("Не удалось создать папку Saves");
            }
        }

        JFileChooser fileChooser = new JFileChooser(saveDir.toFile());
        fileChooser.setFileFilter(new FileNameExtensionFilter(".ser","ser"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        return fileChooser;
    }

    private class OpenListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Path currentDir = Paths.get("").toAbsolutePath();
            JFileChooser fileChooser = new JFileChooser(currentDir.toFile());
            fileChooser.setFileFilter(new FileNameExtensionFilter(".txt","txt"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            int response = fileChooser.showDialog(fr, null);

            if (response == JFileChooser.APPROVE_OPTION) {
                if (fileChooser.getSelectedFile().exists()) {
                    fillGuiFromFile(fileChooser.getSelectedFile());
                    fileNameAbsolute = fileChooser.getSelectedFile().toString();
                } else {
                    System.out.println("Нет такого файла!");
                }
            }
        }
    }

    private class SaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = makeSerialisFChooser();
            int response = fileChooser.showDialog(fr, "Save");

            if (response == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.toString().endsWith(".ser")) {
                    selectedFile = new File(selectedFile.toString() + ".ser");
                }
                serialiseEnteredValues(selectedFile);
            }

        }

        private void serialiseEnteredValues(File saveFile) {
            ObjectOutputStream objOutputStream = null;
            try {
                objOutputStream = new ObjectOutputStream(new FileOutputStream(saveFile));

                ArrayList selectedVal = new ArrayList();
                for (JComboBox cb : updCB) {
                    selectedVal.add(cb.getSelectedItem());
                }

                objOutputStream.writeObject(frKWs);
                objOutputStream.writeObject(fileNameAbsolute);
                objOutputStream.writeObject(selectedVal);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Сериализация провалилась. Не удалось сохранить настройки в файл.");
            } finally {
                try {
                    objOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Не удалось закрыть поток сериализации.");
                }
            }
        }
    }

    private class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = makeSerialisFChooser();
            int response = fileChooser.showDialog(fr, "Load");

            if (response == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.toString().endsWith(".ser")) {
                    selectedFile = new File(selectedFile.toString() + ".ser");
                }
                if (selectedFile.exists()) {
                    deserialise(selectedFile);
                } else {
                    System.out.println("Нет такого файла. Невозможно загрузить.");
                }

            }
        }

        private void deserialise(File file) {
            ObjectInputStream objInputStream = null;
            try {
                objInputStream = new ObjectInputStream(new FileInputStream(file));

                frKWs = (ArrayList<KeyWordWithFrequency>) objInputStream.readObject();
                fileNameAbsolute = (String) objInputStream.readObject();
                ArrayList selectedVal = (ArrayList) objInputStream.readObject();

                File restoredFile = new File(fileNameAbsolute);
                if (restoredFile.exists()) {
                    fillGuiFromFile(restoredFile);
                } else {
                    System.out.println("Не получилось найти старый файл при загрузке.");
                }

                //Restore selected items in comboboxes
                if (updCB.size() < selectedVal.size()) {
                    int fieldsToAdd = selectedVal.size() - updCB.size();
                    for (int i = 0; i < fieldsToAdd; i++) {
                        addField();
                    }
                }
                for (int i = 0; i < (selectedVal.size() - 1); i++) {
                    updCB.get(i).setSelectedItem(selectedVal.get(i));
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Сериализация провалилась. Не удалось загрузить настройки из файла.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                fr.revalidate();
                try {
                    objInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Не удалось закрыть поток сериализации.");
                }
            }
        }
    }

    private class NoEmptyFieldsActListener implements ActionListener {
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

    private class GoParseActionListener implements ActionListener {
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

            System.out.println("\n" + "Cycle word:" + cw);
            System.out.println("Keywords:" + kws.toString());

            if (cw == null || cw.equals("")) {
                System.out.println("Нельзя парсить без маркера цикла!");
            } else if (kws.isEmpty()) {
                System.out.println("Выберите по крайней мере одно ключевое слово.");
            } else {
//                long time = System.currentTimeMillis();
//                DataWH dwh = new DataWH(fileNameAbsolute,cw, kws.toArray(new String[0]));
//                dwh.parse();
//                System.out.println(((double) (System.currentTimeMillis() - time) / 1000) + " sec");

                DataWH dwh = new DataWH(fileNameAbsolute, cw, kws.toArray(new String[0]));
                dwh.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        switch (event.getPropertyName()) {
                            case "state":
                                switch ((SwingWorker.StateValue) event.getNewValue()) {
                                    case PENDING:
                                        break;
                                    case STARTED:
                                        progressBar.setVisible(true);
                                        break;
                                    case DONE:
                                        progressBar.setVisible(false);
                                        makePreview(new File("Отчет.txt"));
                                        break;
                                }
                                break;
                            case "progress":
                                progressBar.setIndeterminate(false);
                                progressBar.setValue((Integer) event.getNewValue());
                                break;
                        }
                    }
                });
                dwh.execute();
            }
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

class ParseWorker extends SwingWorker<Integer, String> {



    /**
     * Computes a result, or throws an exception if unable to do so.
     * <p>
     * <p>
     * Note that this method is executed only once.
     * <p>
     * <p>
     * Note: this method is executed in a background thread.
     *
     * @return the computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    protected Integer doInBackground() throws Exception {

        return null;
    }
}