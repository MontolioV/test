package com.company;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

/**
 * GUI object.
 * <p>
 * Created by MontolioV on 20.12.16.
 */
public class Gui {
    private JFrame frame = new JFrame("Обработка отчетов");
    private JMenuBar menuBar = new JMenuBar();
    String lastReportFile = "Отчет.txt";

    private JPanel parserPanel;
    private JPanel fieldsPanel;
    private JTabbedPane previewsTabs = new JTabbedPane();
    private File oldPreviewFile;
    private ArrayList<JComboBox<String>> cwJCBs = new ArrayList<JComboBox<String>>();
    private ArrayList<JSpinner> cwSpinners = new ArrayList<>();
    private JList<String> kwsJList = new JList<String>();
    private ArrayList<KeyWordWithFrequency> frKWs;
    private String inputFileNameAbsolute;
    private JProgressBar progressBar;
    private JTextField outputFileNameTF = new JTextField("Отчет.txt");
    private JButton goParseButton = new JButton("Пуск!");

    private JPanel joinerPanel;
    private JPanel joinFilesPanel = new JPanel(new GridBagLayout());
    private JTabbedPane previewsTabsJoiner = new JTabbedPane();
    private ArrayList<JTextField> joinFileNames = new ArrayList<>();
    private ArrayList<JComboBoxPreset<String>> joinFileWordsCBs = new ArrayList<>();
    private JProgressBar progressBarJoiner;
    private JTextField outputFileNameTFJoiner = new JTextField("Отчет.txt");
    private JButton goJoinButton = new JButton("Пуск!");
    private JoinerMode joinerMode = JoinerMode.COMPLEMENTOR;

    public Gui() {
        makeMenus();
        makeParserPanel();
        makeJoinerPanel();

        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(parserPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(200, 150, 700, 500);
        frame.setVisible(true);
    }

    private void makeMenus() {
        JMenu mainMenu = new JMenu("Файл");
        JMenuItem menuParserPanelButton = new JMenuItem("Парсер");
        JMenuItem menuJoinerPanelButton = new JMenuItem("Мерджер");
        JMenuItem menuCopyRepButton = new JMenuItem("Отчет в буфер");
        JMenuItem menuSaveButton = new JMenuItem("Сохранить");
        JMenuItem menuLoadButton = new JMenuItem("Загрузить");

        menuParserPanelButton.addActionListener(new DisplayParserPaneListener());
        menuJoinerPanelButton.addActionListener(new DisplayJoinerPanelListener());
        menuCopyRepButton.addActionListener(new CopyReportToClipboardListener());
        menuSaveButton.addActionListener(new SaveListener());
        menuLoadButton.addActionListener(new LoadListener());

        menuParserPanelButton.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_1));
        menuJoinerPanelButton.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_2));
        menuCopyRepButton.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_3));
        menuSaveButton.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_4));
        menuLoadButton.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_5));

        mainMenu.add(menuParserPanelButton);
        mainMenu.add(menuJoinerPanelButton);
        mainMenu.addSeparator();
        mainMenu.add(menuCopyRepButton);
        mainMenu.addSeparator();
        mainMenu.add(menuSaveButton);
        mainMenu.add(menuLoadButton);
        menuBar.add(mainMenu);
    }

    private void makeParserPanel() {
        parserPanel = new JPanel(new GridBagLayout());

        JButton chooseFileBut = new JButton("Выбрать текстовый файл " +
                "(в кодировке " + System.getProperty("file.encoding") + ")");
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

        previewsTabs.addTab("Текущий файл", new JScrollPane(new JTextArea()));
        previewsTabs.addTab("Предыдущий файл", new JScrollPane(new JTextArea()));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        //ChooseFile
        GridBagConstraints headerGBCons = new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 20);
        parserPanel.add(chooseFileBut, headerGBCons);

        //Fields cws title
        GridBagConstraints fieldsTitleGBCons = new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0);
        parserPanel.add(new JLabel("Маркеры циклов и их вложенность"), fieldsTitleGBCons);

        //Fields cws
        GridBagConstraints fieldsGBCons = new GridBagConstraints(0, 3, 1, 1, 0, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0);
        parserPanel.add(fieldsPanel, fieldsGBCons);

        //Fields kws
        GridBagConstraints fieldsAreaGBCons = new GridBagConstraints(0, 4, 1, 1, 0, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0);
        parserPanel.add(kwListPanel, fieldsAreaGBCons);

        //Preview
        GridBagConstraints previewCons = new GridBagConstraints(1, 2, 1, 3, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 10), 0, 0);
        parserPanel.add(previewsTabs, previewCons);

        //Progress bar
        GridBagConstraints progressBarCons = new GridBagConstraints(0, 5, 2, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 20, 20), 0, 10);
        parserPanel.add(progressBar, progressBarCons);

        //Footer
        GridBagConstraints goParseButCons = new GridBagConstraints(0, 6, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0);
        parserPanel.add(makeFooter(goParseButton, new GoParseActionListener(), outputFileNameTF), goParseButCons);
    }

    private void makeJoinerPanel() {
        joinerPanel = new JPanel(new GridBagLayout());

        JButton chooseReportButton = new JButton("Добавить отчет для объединения");
        chooseReportButton.addActionListener(new OpenForJoinerListener());

        remakeJoinFilesPanel();

        previewsTabsJoiner.addTab("Текущий файл", new JScrollPane(new JTextArea()));
        previewsTabsJoiner.addTab("Предыдущий файл", new JScrollPane(new JTextArea()));

        JRadioButton selectMerger = new JRadioButton("Слить все отчеты, как равнозначные. Без потерь.");
        selectMerger.setActionCommand("merger");
        selectMerger.addActionListener(new ChooseJoinerMode());
        JRadioButton selectComplementor = new JRadioButton("Присоединить к первому отчету остальные.", true);
        selectComplementor.setActionCommand("complementor");
        selectComplementor.addActionListener(new ChooseJoinerMode());

        ButtonGroup selectJoinerGroop = new ButtonGroup();
        selectJoinerGroop.add(selectComplementor);
        selectJoinerGroop.add(selectMerger);
        JPanel selectJoinerPanel = new JPanel(new GridLayout(2, 0));
        selectJoinerPanel.add(selectComplementor);
        selectJoinerPanel.add(selectMerger);

        progressBarJoiner = new JProgressBar(0, 100);
        progressBarJoiner.setStringPainted(true);
        progressBarJoiner.setVisible(false);

        //Header, file chooser
        GridBagConstraints consHeader = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 20);
        joinerPanel.add(chooseReportButton, consHeader);

        //Reports panel
        GridBagConstraints consFilePanel = new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 10), 0, 0);
        joinerPanel.add(joinFilesPanel, consFilePanel);

        //Preview
        GridBagConstraints consPreview = new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0);
        joinerPanel.add(previewsTabsJoiner, consPreview);

        //Joiner mode selection
        GridBagConstraints selectCons = new GridBagConstraints(0, 3, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 20, 20), 0, 10);
        joinerPanel.add(selectJoinerPanel, selectCons);

        //Progress bar
        GridBagConstraints progressBarCons = new GridBagConstraints(0, 4, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 20, 20), 0, 10);
        joinerPanel.add(progressBarJoiner, progressBarCons);

        //Footer, start button
        GridBagConstraints footerCons = new GridBagConstraints(0, 5, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0);
        joinerPanel.add(makeFooter(goJoinButton, new GoJoinActionListener(), outputFileNameTFJoiner), footerCons);
    }

    private JPanel makeFooter(JButton startButton, ActionListener actionListener, JTextField outputTF) {
        JPanel result = new JPanel(new GridBagLayout());

        startButton.addActionListener(actionListener);

        //Label
        GridBagConstraints labelCons = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        result.add(new JLabel("Как назовем файл отчета?"), labelCons);

        //Output file name field
        GridBagConstraints textFieldCons = new GridBagConstraints(0, 1, 1, 1, 0.6, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 10);
        result.add(outputTF, textFieldCons);

        //Start button
        GridBagConstraints startButtonCons = new GridBagConstraints(1, 0, 1, 2, 0.4, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        result.add(startButton, startButtonCons);

        return result;
    }

    private void fillGuiFromFile(File file) {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        updatePreview(file, previewsTabs);
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

    private void updatePreview(File file, JTabbedPane prTabs) {
        JScrollPane oldPreview = (JScrollPane) prTabs.getComponentAt(0);
        JScrollPane newPreview = makePreview(file);

        if (file.equals(oldPreviewFile)) {
            prTabs.setComponentAt(0, newPreview);
        } else {
            prTabs.setComponentAt(0, newPreview);
            prTabs.setComponentAt(1, oldPreview);
            oldPreviewFile = file;
        }
    }

    private JScrollPane makePreview(File file) {
        JTextArea previewTA = new JTextArea();
        BufferedReader bReader = null;
        String ending = "/* И так далее... */";

        previewTA.setEditable(false);
        previewTA.append("Файл " + file.getName() + "\n");
        previewTA.append("/* Начало */" + "\n");
        try {
            bReader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < 30; i++) {
                String s = bReader.readLine();
                if (s == null) {
                    ending = "/* Конец. */";
                    break;
                }
                previewTA.append(s + "\n");
            }
            previewTA.append(ending);
        } catch (FileNotFoundException e) {
            showWarningMessage("Превью не заполнено. Не получается найти файл.", e);
        } catch (IOException e) {
            showWarningMessage("При заполнении превью возникло исключение.", e);
        } finally {
            try {
                if (bReader != null) bReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JScrollPane(previewTA);
    }

    private void addField() {
        this.addField(cwJCBs.size());
    }

    private void addField(int gridY) {
        JSpinner defaultSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JComboBox<String> defaultComboBox = new JComboBoxPreset<>();

        GridBagConstraints comboBoxConstraints = new GridBagConstraints(0, gridY, 1, 1, 0, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.VERTICAL, new Insets(0, 0, 3, 10), 0, 0);
        GridBagConstraints spinnerConstraints = new GridBagConstraints(1, gridY, 1, 1, 0, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.VERTICAL, new Insets(0, 0, 3, 0), 0, 0);

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

    private void remakeJoinFilesPanel() {
        joinFilesPanel.removeAll();

        if (joinFileNames.size() > 0) {
            JButton reset = new JButton("Сброс");
            reset.addActionListener(new ClearJoinFilesPanel());

            GridBagConstraints consTFLabel = new GridBagConstraints(0, 0, 1, 1, 1, 0,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            joinFilesPanel.add(new JLabel("Файл с отчетом"), consTFLabel);
            GridBagConstraints consCBLabel = new GridBagConstraints(1, 0, 1, 1, 0, 0,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
            joinFilesPanel.add(new JLabel("Общая колонка"), consCBLabel);
            GridBagConstraints resetButtonCons = new GridBagConstraints(3, 1, 1, joinFileNames.size(), 0, 1,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.VERTICAL, new Insets(3, 10, 0, 0), 0, 0);
            joinFilesPanel.add(reset, resetButtonCons);
        }

        for (int i = 0; i < joinFileNames.size(); i++) {
            GridBagConstraints consTF = new GridBagConstraints(0, i + 1, 1, 1, 1, 1,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(3, 0, 0, 5), 0, 0);
            joinFilesPanel.add(joinFileNames.get(i), consTF);
            GridBagConstraints consCB = new GridBagConstraints(1, i + 1, 1, 1, 0, 1,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.VERTICAL, new Insets(3, 0, 0, 0), 0, 0);
            joinFilesPanel.add(joinFileWordsCBs.get(i), consCB);
        }

        frame.revalidate();
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
        Path saveDir = Paths.get("", "Saves").toAbsolutePath();
        if (!saveDir.toFile().exists()) {
            if (!saveDir.toFile().mkdir()) {
                throw new SecurityException("Не удалось создать папку Saves. Недостаточно прав.");
            }
        }

        JFileChooser fileChooser = new JFileChooser(saveDir.toFile());
        fileChooser.setFileFilter(new FileNameExtensionFilter(".ser", "ser"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        return fileChooser;
    }

    private void showWarningMessage(String message) {
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

    private class DisplayParserPaneListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.setContentPane(parserPanel);
            frame.revalidate();
        }
    }

    private class DisplayJoinerPanelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.setContentPane(joinerPanel);
            frame.revalidate();
        }
    }

    private class OpenListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doSmthWithFile(chooseFile());
        }

        private void doSmthWithFile(File selectedFile) {
            if (selectedFile != null) {
                fillGuiFromFile(selectedFile);
                inputFileNameAbsolute = selectedFile.toString();
            }
        }

        @Nullable
        private File chooseFile() {
            Path currentDir = Paths.get("").toAbsolutePath();
            JFileChooser fileChooser = new JFileChooser(currentDir.toFile());
            fileChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            int response = fileChooser.showDialog(frame, null);

            if (response == JFileChooser.APPROVE_OPTION) {
                if (fileChooser.getSelectedFile().exists()) {
                    return fileChooser.getSelectedFile();
                } else {
                    showWarningMessage(new IllegalArgumentException("Нет такого файла!"));
                }
            }
            return null;
        }
    }

    private class OpenForJoinerListener extends OpenListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doSmthWithFile(super.chooseFile());
        }

        private void doSmthWithFile(File selectedFile) {
            if (selectedFile != null) {
                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                    JComboBoxPreset<String> tmpCB = new JComboBoxPreset<>(br.readLine().split("\t"));
                    joinFileNames.add(new JTextField(selectedFile.toString()));
                    joinFileWordsCBs.add(tmpCB);
                    updatePreview(selectedFile, previewsTabsJoiner);

                    remakeJoinFilesPanel();
                } catch (IOException e) {
                    showWarningMessage(e);
                }
            }
        }
    }

    private class CopyReportToClipboardListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try (BufferedReader br = new BufferedReader(new FileReader(lastReportFile))) {
                StringJoiner joiner = new StringJoiner("\n");
                Predicate<String> addLine = (line) -> {
                    if (line != null) {
                        joiner.add(line);
                        return true;
                    }
                    return false;
                };

                while (addLine.test(br.readLine())) ;

                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable textToCB = new StringSelection(joiner.toString());
                cb.setContents(textToCB, null);
            } catch (IOException e1) {
                showWarningMessage(e1);
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

                objOutputStream.writeObject(inputFileNameAbsolute);
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

                inputFileNameAbsolute = (String) objInputStream.readObject();
                ArrayList cwsSelectedVal = (ArrayList) objInputStream.readObject();
                ArrayList cwsSelectedLvl = (ArrayList) objInputStream.readObject();
                List<String> kwsListSelected = (List<String>) objInputStream.readObject();

                File restoredFile = new File(inputFileNameAbsolute);
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
                    showWarningMessage("Не получается найти в файле " + inputFileNameAbsolute + "\n" +
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
            boolean emptyFieldExists = false;
            String s;
            for (JComboBox<String> cb : cwJCBs) {
                s = (String) cb.getSelectedItem();
                if ((s == null) || (s.equals(""))) {
                    emptyFieldExists = true;
                    break;
                }
            }
            if (!emptyFieldExists) {
                addField();
                frame.revalidate();
            }
        }
    }

    private class ClearJoinFilesPanel implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            joinFileNames = new ArrayList<>();
            joinFileWordsCBs = new ArrayList<>();
            remakeJoinFilesPanel();
        }
    }

    private class ChooseJoinerMode implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "merger":
                    joinerMode = JoinerMode.MERGER;
                    break;
                case "complementor":
                    joinerMode = JoinerMode.COMPLEMENTOR;
                    break;
            }
        }
    }

    private class GoParseActionListener implements ActionListener {
        private boolean isRunning;
        private DataWH dwh;

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (isRunning) {
                    cancel();
                } else {
                    prepareAndGo();
                }
            } catch (IllegalArgumentException e1) {
                showWarningMessage(e1);
            }
        }

        private void prepareAndGo() throws IllegalArgumentException {
            ArrayList<String> cws = new ArrayList<String>();
            ArrayList<CWwithLvl> cwsLvl = new ArrayList<>();
            String outputFileName = outputFileNameTF.getText();

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

            if (outputFileName == null || outputFileName.equals("")) {
                outputFileNameTF.setText("Отчет.txt");
                throw new IllegalArgumentException("Файл для отчета должен как-то называться.");
            } else if (!outputFileName.endsWith(".txt")) {
                outputFileNameTF.setText(outputFileNameTF.getText() + ".txt");
                outputFileName += ".txt";
            }

            System.out.println("\n" + "Cycle words:" + cws.toString());
            System.out.println("Keywords:" + kwsJList.getSelectedValuesList().toString());

            if (inputFileNameAbsolute == null) {
                throw new IllegalArgumentException("Файл не выбран.");
            } else if (cws.isEmpty()) {
                throw new IllegalArgumentException("Нельзя парсить без маркера цикла.");
            } else if (kwsJList.getSelectedValuesList().isEmpty()) {
                throw new IllegalArgumentException("Нельзя парсить без ключевых слов.");
            } else {
                dwh = new DataWH(inputFileNameAbsolute, outputFileName,
                        cws, cwsLvl, kwsJList.getSelectedValuesList());
                String finalOutputFileName = outputFileName;
                dwh.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        switch (event.getPropertyName()) {
                            case "state":
                                switch ((SwingWorker.StateValue) event.getNewValue()) {
                                    case PENDING:
                                        break;
                                    case STARTED:
                                        isRunning = true;
                                        progressBar.setValue(0);
                                        progressBar.setVisible(true);
                                        progressBar.setIndeterminate(true);
                                        goParseButton.setText("Отмена");
                                        break;
                                    case DONE:
                                        try {
                                            dwh.get();
                                            updatePreview(new File(finalOutputFileName), previewsTabs);
                                        } catch (InterruptedException e1) {
                                            showWarningMessage("InterruptedException", e1);
                                        } catch (ExecutionException e2) {
                                            showWarningMessage((Exception) e2.getCause());
                                        } catch (CancellationException e3) {
                                            showWarningMessage("Отменено", e3);
                                        }
                                        progressBar.setVisible(false);
                                        isRunning = false;
                                        goParseButton.setText("Пуск!");
                                        lastReportFile = outputFileNameTF.getText();
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

        private void cancel() {
            dwh.cancel(true);
        }
    }

    private class GoJoinActionListener implements ActionListener {
        private boolean isRunning;
        private ReportMerger joiner;

        @Override

        public void actionPerformed(ActionEvent e) {
            try {
                if (isRunning) {
                    cancel();
                } else {
                    prepareAndGo();
                }
            } catch (IllegalArgumentException e1) {
                showWarningMessage(e1);
            }
        }

        private void prepareAndGo() throws IllegalArgumentException {
            if (outputFileNameTFJoiner.getText().equals("")) {
                outputFileNameTFJoiner.setText("Отчет.txt");
                throw new IllegalArgumentException("Файл для отчета должен как-то называться.");
            } else if (!outputFileNameTFJoiner.getText().endsWith(".txt")) {
                outputFileNameTFJoiner.setText(outputFileNameTFJoiner.getText() + ".txt");
            }

            String output = outputFileNameTFJoiner.getText();
            ArrayList<String> reports = new ArrayList<>();
            ArrayList<Integer> mergeWordIndexes = new ArrayList<>();

            joinFileNames.forEach((tf) -> reports.add(tf.getText()));
            joinFileWordsCBs.forEach((cb) -> mergeWordIndexes.add(cb.getSelectedIndex()));

            if (reports.size() < 2 || mergeWordIndexes.size() < 2) {
                throw new IllegalArgumentException("Выберите не менее двух отчетов для слияния.");
            }

            switch (joinerMode) {

                case MERGER:
                    joiner = new ReportMerger(reports.toArray(new String[0]), mergeWordIndexes.stream().mapToInt(Integer::intValue).toArray(), output);
                    break;
                case COMPLEMENTOR:
                    joiner = new ReportComplementor(reports.toArray(new String[0]), mergeWordIndexes.stream().mapToInt(Integer::intValue).toArray(), output);
                    break;
            }

            joiner.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    switch (event.getPropertyName()) {
                        case "state":
                            switch ((SwingWorker.StateValue) event.getNewValue()) {
                                case PENDING:
                                    break;
                                case STARTED:
                                    isRunning = true;
                                    progressBarJoiner.setValue(0);
                                    progressBarJoiner.setVisible(true);
                                    progressBarJoiner.setIndeterminate(true);
                                    goJoinButton.setText("Отмена");
                                    break;
                                case DONE:
                                    try {
                                        joiner.get();
                                        updatePreview(new File(output), previewsTabsJoiner);
                                    } catch (InterruptedException e1) {
                                        showWarningMessage("InterruptedException", e1);
                                    } catch (ExecutionException e2) {
                                        showWarningMessage((Exception) e2.getCause());
                                    } catch (CancellationException e3) {
                                        showWarningMessage("Отменено", e3);
                                    }
                                    progressBarJoiner.setVisible(false);
                                    isRunning = false;
                                    goJoinButton.setText("Пуск!");
                                    lastReportFile = outputFileNameTFJoiner.getText();
                                    break;
                            }
                            break;
                        case "progress":
                            progressBarJoiner.setIndeterminate(false);
                            progressBarJoiner.setValue((Integer) event.getNewValue());
                            break;
                    }
                }
            });

            joiner.execute();
        }

        private void cancel() {
            joiner.cancel(true);
        }
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

enum JoinerMode {
    MERGER, COMPLEMENTOR,;
}
