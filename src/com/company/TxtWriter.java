package com.company;

import java.io.*;
import java.util.*;

/**
 * Created by main on 10.12.2016.
 */
class TxtWriter {
    private final String CYCLE_WORD;
    private final List<String> KEY_WORDS;
    private BufferedWriter buffer;

    public TxtWriter(String CYCLE_WORD, List<String> KEY_WORDS) {
        this.CYCLE_WORD = CYCLE_WORD;
        this.KEY_WORDS = KEY_WORDS;
        try {
            this.buffer = new BufferedWriter(new FileWriter(new File("Отчет.txt")));
        } catch (IOException e) {
            System.out.println("Не удалось открыть файл для записи отчета.");
            e.printStackTrace();
        }
    }

     void write_to_txt (ArrayList<Hashtable<String,String>> arl) {
         String tmpS;
         tmpS = CYCLE_WORD + "\t";
         for (String s: KEY_WORDS) {
             tmpS += s + "\t";
         }
         send_to_buff(tmpS);
         for (Hashtable<String,String> ht: arl) {
             tmpS = ht.get(CYCLE_WORD)+ "\t";
             for (String s: KEY_WORDS) {
                 tmpS += ht.get(s) + "\t";
             }
             send_to_buff(tmpS);
         }
         try {
             buffer.close();
         } catch (IOException e) {
             System.out.println("buffer.close() fails");
             e.printStackTrace();
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
}
