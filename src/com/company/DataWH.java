package com.company;

import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Core class to manipulate and store data.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

class DataWH extends SwingWorker<Integer, String> {
    private final String FILE_NAME;
    private final List<String> CYCLE_WORDS;
    private final List<String> KEY_WORDS;
    private String currentCWValue;
    private Hashtable<String, String> tmpHt = new Hashtable<>(15);
    private Check chk;
    private long totalLines;
    private long processedLines;
    private long reportLines = 1;
    private TxtWriter writer;
//    private final ArrayList<Hashtable<String, String>> STORAGE = new ArrayList<>();


    DataWH(String file_name, List<String> cws, List<String> kws) {
        this.FILE_NAME = file_name;
        this.CYCLE_WORDS = cws;
        this.KEY_WORDS = kws;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        try {
            chk = new CheckDisabled();
            writer = new TxtWriter(CYCLE_WORDS, KEY_WORDS);
            TxtReader reader = new TxtReader(FILE_NAME);
            boolean unfinished = true;

            totalLines = reader.getAmountOfLines();

            while (unfinished) {
                unfinished = makeHT(reader.getListFromTxt());
                if (unfinished) processedLines++;
                setProgress((int) (((double) processedLines / totalLines) * 100));
            }
            reader.close_buffer();
            writer.close_buffer();
//            writer.write_to_txt(STORAGE);

            System.out.println("Total lines in file: " + totalLines);
            System.out.println("Lines processed: " + processedLines);
            System.out.println("Generated lines to report: " + reportLines);
        } catch (FileNotFoundException e) {
            System.out.println("Указанный файл не найден!");
            e.printStackTrace();
        }
        return null;
    }


    private boolean makeHT(List<String> list) {
        if (tmpHt.size() == KEY_WORDS.size()) {
            tmpHt.put(CYCLE_WORDS, currentCWValue);
            if (chk.check(tmpHt)) {
//                STORAGE.add(tmpHt);
                writer.writeLineToTxt(tmpHt);
                reportLines++;
            }
            tmpHt = new Hashtable<>(15);
        }
        if (list == null) {                                      /* End of input */
            return false;
        }
        if (list.contains(CYCLE_WORDS)) {
            currentCWValue = list.get(list.indexOf(CYCLE_WORDS) + 1);     /* Next string after CW is CW value */
        }
        for (int i = 0; i < list.size(); i += 2) {           /* Even strings (and 0) are KWs */
            if (KEY_WORDS.contains(list.get(i))) {              /* Odd strings are KW values */
                tmpHt.put(list.get(i), list.get(i + 1));           /* Need to extract only KW values */
            }
        }
        return true;
    }
}
