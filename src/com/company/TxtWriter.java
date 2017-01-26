package com.company;

import java.io.*;
import java.util.*;

/**
 * Class to output results (reports) to txt.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */
class TxtWriter {
    private final String CYCLE_WORD;
    private final List<String> KEY_WORDS;
    private BufferedWriter buffer;

    TxtWriter(String CYCLE_WORD, List<String> KEY_WORDS) {
        this.CYCLE_WORD = CYCLE_WORD;
        this.KEY_WORDS = KEY_WORDS;
        try {
            this.buffer = new BufferedWriter(new FileWriter(new File("Отчет.txt")));
            buffer.write(CYCLE_WORD + "\t");
            for (String s : KEY_WORDS) {
                buffer.write(s + "\t");
            }
            buffer.newLine();
        } catch (IOException e) {
            System.out.println("Не удалось открыть файл для записи отчета и записать шапку.");
            e.printStackTrace();
        }
    }

    /**
     * The method to output results data to txt.
     * <p>Txt report may be easily transformed in electronic table (excel, libre/open calc) by tabs.
     * @param arl data structure to convert to report.
     */
    void write_to_txt (ArrayList<Hashtable<String,String>> arl) {
        String tmpS;
        for (Hashtable<String, String> ht : arl) {
            tmpS = ht.get(CYCLE_WORD) + "\t";
            for (String s : KEY_WORDS) {
                tmpS += ht.get(s) + "\t";
            }
            send_to_buff(tmpS);
        }
        close_buffer();
    }

    void writeLineToTxt(Hashtable<String, String> ht) {
        try {
            buffer.write(ht.get(CYCLE_WORD) + "\t");
            for (String s : KEY_WORDS) {
                buffer.write(ht.get(s) + "\t");
            }
            buffer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось записать строку в файл.");
        }
    }

    private void send_to_buff(String s) {
        try {
            buffer.write(s);
            buffer.newLine();
        } catch (IOException e) {
            System.out.println("buffer.write(s) fails");
            e.printStackTrace();
        }
    }

    void close_buffer(){
        try {
            if (this.buffer != null) {
                this.buffer.close();
            }
        } catch (IOException e) {
            System.out.println("Buffer close failed");
            e.printStackTrace();
        }
    }
}
