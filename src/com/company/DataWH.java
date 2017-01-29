package com.company;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

/**
 * Core class to manipulate and store data.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

class DataWH extends SwingWorker<Integer, String> {
    private final String FILE_NAME;
    private final List<String> CYCLE_WORDS;
    private final List<CWwithLvl> CYCLE_WORDS_LVL;
    private final List<String> KEY_WORDS;
    private HashMap<String, String> tmpKWvals = new HashMap<>(25);
    private HashMap<CWwithLvl, String> tmpCWvals = new HashMap<>();
    private ArrayList<HashMap<String, String>> poolOfKWs = new ArrayList<HashMap<String, String>>();
    private Check chk;
    private long totalLines;
    private long processedLines;
    private long reportLines = 1;
    private TxtWriter writer;

    DataWH(String file_name, List<String> cws, List<CWwithLvl> cwsLvl, List<String> kws) {
        this.FILE_NAME = file_name;
        this.CYCLE_WORDS = cws;
        this.CYCLE_WORDS_LVL = cwsLvl;
        this.KEY_WORDS = kws;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        chk = new CheckDisabled();
        writer = new TxtWriter(CYCLE_WORDS, KEY_WORDS);
        TxtReader reader = new TxtReader(FILE_NAME);
        boolean unfinished = true;

        try {
            totalLines = reader.getAmountOfLines();

            while (unfinished) {
                unfinished = makeHT(reader.getListFromTxt());
                if (unfinished) processedLines++;
                setProgress((int) (((double) processedLines / totalLines) * 100));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Указанный файл не найден!");
            e.printStackTrace();
        }finally {
            reader.close_buffer();
            writer.close_buffer();
            System.out.println("Total lines in file: " + totalLines);
            System.out.println("Lines processed: " + processedLines);
            System.out.println("Generated lines to report: " + reportLines);
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method processes line by line from input, filling the data structure.
     * When it gets all needed CWs and KWs, it sends the data structure to output.
     * <p></p>
     * @param inputList parsed input line.
     * @return  <tt>true</tt> - if input was received.<p><tt>false</tt> - if input stops.</p>
     */
    private boolean makeHT(List<String> inputList) {
        if (!poolOfKWs.isEmpty() && poolOfKWs.get(poolOfKWs.size() - 1).size() == KEY_WORDS.size() && tmpCWvals.size() == CYCLE_WORDS.size()) {
            for (HashMap<String, String> ht : poolOfKWs) {
                tmpCWvals.forEach((cWwithLvl, s) -> ht.put(cWwithLvl.getCw(), s));
                if (chk.check(ht)) {
                    writer.writeLineToTxt(ht);
                    reportLines++;
                }
            }
            poolOfKWs = new ArrayList<>();
        }

        //End of input
        if (inputList == null){
            return false;
        }

        for (CWwithLvl cwlvl : CYCLE_WORDS_LVL) {
            int index = inputList.indexOf(cwlvl.getCw());
            if (index >= 0) {
                tmpCWvals.put(cwlvl, inputList.get(index + 1));     /* Next string after CW is CW value */
                for (CWwithLvl randomCW : CYCLE_WORDS_LVL) {
                    if (randomCW.getLvl() > cwlvl.getLvl()) {
                        tmpCWvals.remove(randomCW);
                    }
                }
            }
        }

        for (int i = 0; i < inputList.size(); i += 2) {
            //Take KW values only if we're sure the user want to take them. Also we ensure that they are not CWs, KW and CW can't match.
            if (KEY_WORDS.contains(inputList.get(i))) {
                //Finding where to put KWs now
                if (poolOfKWs.isEmpty() || poolOfKWs.get(poolOfKWs.size() - 1).containsKey(inputList.get(i))) {
                    tmpKWvals = new HashMap<String, String>(25);
                    poolOfKWs.add(tmpKWvals);
                } else {
                    for (HashMap<String, String> kwHM : poolOfKWs) {
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
}
