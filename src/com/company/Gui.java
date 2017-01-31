package com.company;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;

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
import java.util.concurrent.ExecutionException;

/**
 * GUI object.
 * <p>
 * Created by MontolioV on 20.12.16.
 */
public class Gui {
    private JFrame frame = new JFrame("test");
    private JMenuBar menuBar = new JMenuBar();;
    private JPanel background;
    private JTextArea preview = new JTextArea();
    private JPanel fieldsPanel;
    private ArrayList<JComboBox<String>> cwJCBs = new ArrayList<JComboBox<String>>();
    private ArrayList<JSpinner> cwSpinners = new ArrayList<>();
    private JList<String> kwsJList = new JList<String>();
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
        this.addField(0);

        JPanel kwListPanel = new JPanel();
        kwListPanel.setLayout(new BoxLayout(kwListPanel, BoxLayout.Y_AXIS));
        JLabel kwsLabel = new JLabel("Ключевые слова");
        kwsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane kwsScrollPane = new JScrollPane(kwsJList);
        kwsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        kwListPanel.add(kwsLabel);
        kwListPanel.add(kwsScrollPane);

        preview.setEditable(false);

        progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        JButton goParseButton = new JButton("Пуск!");
        goParseButton.addActionListener(new GoParseActionListener());

        //ChooseFile
        GridBagConstraints headerGBCons = new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 20);
        background.add(chooseFileBut, headerGBCons);

        //Fields
        GridBagConstraints fieldsGBCons = new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0);
        background.add(fieldsPanel, fieldsGBCons);

        //Fields #2
        GridBagConstraints fieldsAreaGBCons = new GridBagConstraints(0, 3, 1, 1, 0, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0);
        background.add(kwListPanel, fieldsAreaGBCons);

        //Preview
        GridBagConstraints previewCons = new GridBagConstraints(1, 2, 1, 2, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 10), 0, 0);
        background.add(new JScrollPane(preview), previewCons);

        //Progress bar
        GridBagConstraints progressBarCons = new GridBagConstraints(0, 4, 2, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 20, 20), 0, 10);
        background.add(progressBar, progressBarCons);

        //Start button
        GridBagConstraints goParseButCons = new GridBagConstraints(0, 5, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 200, 20);
        background.add(goParseButton, goParseButCons);

        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(200, 150, 700, 500);
        frame.setVisible(true);
    }

    private void fillGuiFromFile(File file) {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        makePreview(file);
        frKWs = findKWs(file);

        for (JComboBox<String> cb : cwJCBs) {
            cb.removeAllItems();
            for (KeyWordWithFrequency kw : frKWs) {
                cb.addItem(kw.getName());
            }
        }
        for (KeyWordWithFrequency kw : frKWs) {
            listModel.addElement(kw.getName());
        }
        kwsJList.setModel(listModel);

        frame.revalidate();
    }

    private void addField() {
        this.addField(cwJCBs.size());
    }

    private void addField(int gridY) {
        JSpinner defaultSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JComboBox<String> defaultComboBox = new JComboBoxPreset<>();

        GridBagConstraints comboBoxConstraints = new GridBagConstraints(0, gridY, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 3, 10), 0, 0);
        GridBagConstraints spinnerConstraints = new GridBagConstraints(1, gridY, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 3, 0), 0, 0);

        defaultComboBox.setEditable(true);
        cwJCBs.add(defaultComboBox);
        cwSpinners.add(defaultSpinner);

        if (frKWs != null) {
            for (KeyWordWithFrequency kw : frKWs) {
                defaultComboBox.addItem(kw.getName());
            }
        }

        defaultComboBox.addActionListener(new NoEmptyFieldsActListener());

        fieldsPanel.add(defaultSpinner, spinnerConstraints);
        fieldsPanel.add(defaultComboBox, comboBoxConstraints);
    }

    private void makePreview(File file) {
        BufferedReader bReader = null;
        preview.setText(null);
        String ending = "И так далее...";

        try {
            bReader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < 30; i++) {
                String s = bReader.readLine();
                if (s == null) {
                    ending = "Конец.";
                    break;
                }
                preview.append(s + "\n");
            }
            preview.append("\n" + ending);
        } catch (FileNotFoundException e) {
            showWarningMessage("Превью не заполнено.", e);
        } catch (IOException e) {
            showWarningMessage("При заполнении превью возникло исключение.", e);
        }finally {
            try {
                if (bReader != null) bReader.close();
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

            for (int i = 0; i < 300; i++) {
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
            showWarningMessage("", e1);
        } catch (IOException e) {
            showWarningMessage("Исключение при формировании списка ключевых слов.", e);
        }
        return possibleKWs;
    }

    private JFileChooser makeSerialisFChooser() throws SecurityException {
        Path saveDir = Paths.get("","Saves").toAbsolutePath();
        if (!saveDir.toFile().exists()) {
            if (!saveDir.toFile().mkdir()) {
                throw new SecurityException("Не удалось создать папку Saves. Недостаточно прав.");
            }
        }

        JFileChooser fileChooser = new JFileChooser(saveDir.toFile());
        fileChooser.setFileFilter(new FileNameExtensionFilter(".ser","ser"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        return fileChooser;
    }

    private void showWarningMessage(String  message) {
        showWarningMessage(message, null);
    }

    private void showWarningMessage(Exception e) {
        showWarningMessage("", e);
    }

    private void showWarningMessage(String message, Exception e) {
        e.printStackTrace();

        StringJoiner stringJoiner = new StringJoiner("\n");
        if (message != null && !message.equals("")) stringJoiner.add(message);
        while (e != null) {
            message = e.getMessage();
            if (message != null && !message.equals("")) stringJoiner.add(message);
            e = (Exception) e.getCause();
        }
        JOptionPane.showMessageDialog(frame, stringJoiner.toString(),
                "Возникла проблема", JOptionPane.WARNING_MESSAGE);
    }

    private class OpenListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Path currentDir = Paths.get("").toAbsolutePath();
            JFileChooser fileChooser = new JFileChooser(currentDir.toFile());
            fileChooser.setFileFilter(new FileNameExtensionFilter(".txt","txt"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            int response = fileChooser.showDialog(frame, null);

            if (response == JFileChooser.APPROVE_OPTION) {
                if (fileChooser.getSelectedFile().exists()) {
                    fillGuiFromFile(fileChooser.getSelectedFile());
                    fileNameAbsolute = fileChooser.getSelectedFile().toString();
                } else {
                    showWarningMessage(new IllegalArgumentException("Нет такого файла!"));
                }
            }
        }
    }

    private class SaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = makeSerialisFChooser();
            int response = fileChooser.showDialog(frame, "Save");

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
            ArrayList selectedVal = new ArrayList();
            ArrayList selectedLvl = new ArrayList();

            for (int i = 0; i < cwJCBs.size(); i++) {
                selectedVal.add(cwJCBs.get(i).getSelectedItem());
                selectedLvl.add(cwSpinners.get(i).getValue());
            }

            try {
                objOutputStream = new ObjectOutputStream(new FileOutputStream(saveFile));

                objOutputStream.writeObject(fileNameAbsolute);
                objOutputStream.writeObject(selectedVal);
                objOutputStream.writeObject(selectedLvl);
                objOutputStream.writeObject(kwsJList.getSelectedValuesList());

            } catch (IOException e) {
                showWarningMessage("Сериализация провалилась. Не удалось сохранить настройки в файл.", e);
            } finally {
                try {
                    if (objOutputStream != null) objOutputStream.close();
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
            int response = fileChooser.showDialog(frame, "Load");

            if (response == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.toString().endsWith(".ser")) {
                    selectedFile = new File(selectedFile.toString() + ".ser");
                }
                if (selectedFile.exists()) {
                    deserialise(selectedFile);
                } else {
                    showWarningMessage(new IllegalArgumentException("Нет такого файла. Невозможно загрузить."));
                }

            }
        }

        private void deserialise(File file) {
            ObjectInputStream objInputStream = null;
            StringJoiner missingWordsJoiner = new StringJoiner("\",\"", "\"", "\"");
            try {
                objInputStream = new ObjectInputStream(new FileInputStream(file));

                fileNameAbsolute = (String) objInputStream.readObject();
                ArrayList cwsSelectedVal = (ArrayList) objInputStream.readObject();
                ArrayList cwsSelectedLvl = (ArrayList) objInputStream.readObject();
                List<String> kwsListSelected = (List<String>) objInputStream.readObject();

                File restoredFile = new File(fileNameAbsolute);
                if (restoredFile.exists()) {
                    fillGuiFromFile(restoredFile);
                } else {
                    throw new FileNotFoundException("Не получилось найти старый файл при загрузке.");
                }

                //Restore selected items in combo boxes
                if (cwJCBs.size() < cwsSelectedVal.size()) {
                    int fieldsToAdd = cwsSelectedVal.size() - cwJCBs.size();
                    for (int i = 0; i < fieldsToAdd; i++) {
                        addField();
                    }
                }
                for (int i = 0; i < cwsSelectedVal.size(); i++) {
                    if (!frKWs.contains(new KeyWordWithFrequency((String) cwsSelectedVal.get(i)))) {
                        missingWordsJoiner.add((String) cwsSelectedVal.get(i));
                    }
                    cwJCBs.get(i).setSelectedItem(cwsSelectedVal.get(i));
                    cwSpinners.get(i).setValue(cwsSelectedLvl.get(i));
                }
                //Restore selected items in JList
                DefaultListModel<String> curentModel = (DefaultListModel<String>) kwsJList.getModel();
                ArrayList<Integer> newSelected = new ArrayList<Integer>();
                int ind;
                for (String oldVal : kwsListSelected) {
                    ind = curentModel.indexOf(oldVal);
                    if (ind == -1) {
                        missingWordsJoiner.add(oldVal);
                    } else {
                        newSelected.add(ind);
                    }
                }

                if (!missingWordsJoiner.toString().equals("\"\"")) {
                    showWarningMessage("Не получается найти в файле " + fileNameAbsolute + "\n" +
                            missingWordsJoiner.toString() + "\n" + "Возможна потеря данных.");
                }

                int[] indices = newSelected.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
                kwsJList.setSelectedIndices(indices);

            } catch (IOException e) {
                showWarningMessage("Сериализация провалилась. Не удалось загрузить настройки из файла.", e);
            } catch (ClassNotFoundException e) {
                showWarningMessage("Ошибка десерриализации. ClassNotFoundException", e);
            } finally {
                frame.revalidate();
                try {
                    if (objInputStream != null) objInputStream.close();
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
//            if (cwJCBs.size() < 10) {
                String s = (String) cwJCBs.get(cwJCBs.size() - 1).getSelectedItem();
                if ((s != null) && (!s.equals(""))) {
                    addField();
                }

                frame.revalidate();
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
            try {
                prepareAndGo();
            } catch (IllegalArgumentException e1) {
                showWarningMessage(e1);
            }
        }

        private void prepareAndGo() {
            ArrayList<String> cws = new ArrayList<String>();
            ArrayList<CWwithLvl> cwsLvl = new ArrayList<>();

            for (int i = 0; i < cwJCBs.size(); i++) {
                String s = (String) cwJCBs.get(i).getSelectedItem();
                if (s != null && !s.equals("")) {
                    cws.add(s);
                    cwsLvl.add(new CWwithLvl(s, (int) cwSpinners.get(i).getValue()));
                }
            }

            for (String kw : kwsJList.getSelectedValuesList()) {
                if (cws.contains(kw)) {
                    throw new IllegalArgumentException("Ключевые слова не должны совпадать с маркерами цикла." +
                            "\n" + "\"" + kw + "\"");
                }
            }

            System.out.println("\n" + "Cycle words:" + cws.toString());
            System.out.println("Keywords:" + kwsJList.getSelectedValuesList().toString());

            if (fileNameAbsolute == null) {
                throw new IllegalArgumentException("Файл не выбран.");
            } else if (cws.isEmpty()) {
                throw new IllegalArgumentException("Нельзя парсить без маркера цикла.");
            } else if (kwsJList.getSelectedValuesList().isEmpty()) {
                throw new IllegalArgumentException("Нельзя парсить без ключевых слов.");
            } else {
                DataWH dwh = new DataWH(fileNameAbsolute, cws, cwsLvl, kwsJList.getSelectedValuesList());
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
                                        try {
                                            dwh.get();
                                            makePreview(new File("Отчет.txt"));
                                        } catch (InterruptedException e1) {
                                            showWarningMessage("InterruptedException", e1);
                                        } catch (ExecutionException exex) {
                                            showWarningMessage((Exception) exex.getCause());
//                                            exex.printStackTrace();
//                                            JOptionPane.showMessageDialog(frame, exex.getCause().getMessage()
//                                                    , "Возникла проблема", JOptionPane.WARNING_MESSAGE);
                                        }
                                        progressBar.setVisible(false);
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