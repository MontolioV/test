package com.company;

import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Core class to manipulate and store data.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

public class DataWH extends SwingWorkerTemplate{
    private final String INPUT_FILE_NAME;
    private final String OUTPUT_FILE_NAME;
    private final List<String> CYCLE_WORDS;
    private final List<CWwithLvl> CYCLE_WORDS_LVL;
    private final List<String> KEY_WORDS;
    private HashMap<String, String> tmpKWvals = new HashMap<>(25);
    private HashMap<CWwithLvl, String> tmpCWvals = new HashMap<>();
    private ArrayList<HashMap<String, String>> pool = new ArrayList<HashMap<String, String>>();
    private Check chk;
    private long totalLines;
    private long processedLines;
    private long reportLines = 1;
    private TxtWriter writer;
    private TxtReader reader;

    public DataWH(String input_file_name, String output_file_name,
                  List<String> cws, List<CWwithLvl> cwsLvl, List<String> kws) {
        this.INPUT_FILE_NAME = input_file_name;
        this.CYCLE_WORDS = cws;
        this.CYCLE_WORDS_LVL = cwsLvl;
        this.KEY_WORDS = kws;
        this.OUTPUT_FILE_NAME = output_file_name;
    }

    @Override
    public Integer doInBackground() throws Exception {

        fileAnalise();

        chk = new CheckDisabled();
        writer = new TxtWriter(CYCLE_WORDS, KEY_WORDS, OUTPUT_FILE_NAME);
        reader = new TxtReader(INPUT_FILE_NAME);
        boolean unfinished = true;

            totalLines = reader.getAmountOfLines();

            while (unfinished) {
                try {
                    unfinished = makeHT(reader.getListFromTxt());
                    checkIfInterrupted();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Проверте структуру файла и ключевые слова.\n" +
                            "За ключевым словом должно следовать соответствующее значение, " +
                            "отделённое знаком табуляции.", e);
                }
                if (unfinished) processedLines++;
                setProgress((int) (((double) processedLines / totalLines) * 100));
            }
        return null;
    }

    @Override
    public void done() {
        if (reader != null) {
            reader.close_buffer();
        }
        if (writer != null) {
            writer.close_buffer();
        }
        System.out.println("Lines processed: " + processedLines + " of " + totalLines);
        System.out.println("Generated lines to report: " + reportLines);
    }

    /**
     * Analysis of file structure. If it is unappropriated an exception will be thrown.
     * <p></p>
     */
    private void fileAnalise() throws IOException, IllegalArgumentException, InterruptedException {
        String mainCycle = Collections.min(CYCLE_WORDS_LVL).getCw();
        LinkedList<String> unfoundCWs = new LinkedList<>(CYCLE_WORDS);
        LinkedList<String> unfoundKWs = new LinkedList<>(KEY_WORDS);
        TxtReader txtReader = new TxtReader(INPUT_FILE_NAME);
        List<String> tmpList;

        try {
            //Find start of cycle
            do {
                tmpList = txtReader.getListFromTxt();
            } while (tmpList != null && !tmpList.contains(mainCycle));
            //Work with single cycle
            do {
                if (tmpList != null) {
                    for (String s : tmpList) {
                        unfoundKWs.remove(s);
                        //Mark closing CWs
                        if (unfoundCWs.remove(s) && unfoundKWs.isEmpty()) {
                            for (CWwithLvl cwLvl : CYCLE_WORDS_LVL) {
                                if (cwLvl.getCw().equals(s)) {
                                    cwLvl.setAfterKWs(true);
                                }
                            }
                        }
                    }
                }

                tmpList = txtReader.getListFromTxt();
                checkIfInterrupted();
            } while (tmpList != null && !tmpList.contains(mainCycle));
        }finally {
            txtReader.close_buffer();
        }

        if (!unfoundCWs.isEmpty() || !unfoundKWs.isEmpty()) {
            throw new IllegalArgumentException("Проверьте правильность маркеров циклов.\n" +
                    "Заявленная вложенность не отслеживается.\n" +
                    "Постарайтесь сгенерировать файл возрастающим вложением.\n" +
                    "Цикличные слова не должны завершать циклы, они должны их начинать.");
        }
    }

    /**
     * Method processes line by line from input, filling the data structure.
     * When it gets all needed CWs and KWs, it sends the data structure to output.
     * <p></p>
     * @param inputList parsed input line.
     * @return  <tt>true</tt> - if input was received.<p><tt>false</tt> - if input stops.</p>
     */
    private boolean makeHT(List<String> inputList) throws IOException {
        HashMap<String, String> lastHMinPool = (pool.isEmpty()) ? null : pool.get(pool.size() - 1);

        if (lastHMinPool != null && lastHMinPool.size() == KEY_WORDS.size() + CYCLE_WORDS.size()) {
            sendPoolToWriter();
        }

        //End of input
        if (inputList == null){
            tmpCWvals.forEach(this::addMissingCwToPool);
            sendPoolToWriter();
            return false;
        }

        //CWs
        for (CWwithLvl cwlvl : CYCLE_WORDS_LVL) {
            int index = inputList.indexOf(cwlvl.getCw());
            if (index >= 0) {
                String saveCWVal;
                //Next string after CW is CW value
                saveCWVal = tmpCWvals.put(cwlvl, inputList.get(index + 1));
                //This is for the "CW in the end of cycle" scenario
                if (cwlvl.isAfterKWs()) {
                    saveCWVal = inputList.get(index + 1);
                }
                addMissingCwToPool(cwlvl, saveCWVal);

                //Clean old dependent CWs. Add them and their values to pool
                for (CWwithLvl randomCW : CYCLE_WORDS_LVL) {
                    if (randomCW.getLvl() > cwlvl.getLvl()) {
                        saveCWVal = tmpCWvals.remove(randomCW);
                        addMissingCwToPool(randomCW, saveCWVal);
                    }
                }
            }
        }

        //KWs
        for (int i = 0; i < inputList.size(); i += 2) {
            //Take KW values only if we're sure the user want to take them. Also we ensure that they are not CWs, KW and CW can't match.
            if (KEY_WORDS.contains(inputList.get(i))) {
                //Finding where to put KWs now
                if (pool.isEmpty() || pool.get(pool.size() - 1).containsKey(inputList.get(i))) {
                    tmpKWvals = new HashMap<String, String>(25);
                    pool.add(tmpKWvals);
                } else {
                    for (HashMap<String, String> kwHM : pool) {
                        if (!kwHM.containsKey(inputList.get(i))) {
                            tmpKWvals = kwHM;
                            break;
                        }
                    }
                }
                tmpKWvals.put(inputList.get(i), inputList.get(i + 1));      /* Even strings (and 0) are KWs. Odd strings are KW values */
            }
        }

        return true;
    }

    private void addMissingCwToPool(CWwithLvl cwLvl, String cwValPrev) {
        if (cwValPrev != null) {
            for (HashMap<String, String> hm : pool) {
                if (!hm.containsKey(cwLvl.getCw())) {
                    hm.put(cwLvl.getCw(), cwValPrev);
                }
            }
        }
    }

    private void sendPoolToWriter() throws IOException {
        for (HashMap<String, String> hm : pool) {
            if (chk.check(hm)) {
                writer.writeLineToTxt(hm);
                reportLines++;
            }
        }
        pool = new ArrayList<>();
    }
}
