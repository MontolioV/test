package com.company;

import java.io.*;
import java.util.*;

/**
 * Class to output results (reports) to txt.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */
class TxtWriter {
    private final List<String> CYCLE_WORDS;
    private final List<String> KEY_WORDS;
    private BufferedWriter buffer;

    TxtWriter(List<String> CYCLE_WORDS, List<String> KEY_WORDS) {
        this.CYCLE_WORDS = CYCLE_WORDS;
        this.KEY_WORDS = KEY_WORDS;
        try {
            this.buffer = new BufferedWriter(new FileWriter(new File("Отчет.txt")));
            for (String s : CYCLE_WORDS) {
                buffer.write(s + "\t");
            }
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
    void write_to_txt (ArrayList<HashMap<String, String>> arl) {
        StringJoiner sJoiner = new StringJoiner("\t");
        for (HashMap<String, String> ht : arl) {
            for (String s : CYCLE_WORDS) {
                sJoiner.add(ht.get(s));
            }
            for (String s : KEY_WORDS) {
                sJoiner.add(ht.get(s));
            }
            send_to_buff(sJoiner.toString());
        }
        close_buffer();
    }

    void writeLineToTxt(HashMap<String, String> ht) {
        StringJoiner sJoiner = new StringJoiner("\t");
        for (String s : CYCLE_WORDS) {
            sJoiner.add(ht.get(s));
        }
        for (String s : KEY_WORDS) {
            sJoiner.add(ht.get(s));
        }
        try {
            buffer.write(sJoiner.toString());
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
