package com.company;
import java.util.*;
import java.io.*;
/**
 * Class to output AX report (.txt) and parse strings to logic parts.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

class TxtReader {
    private BufferedReader buffer = null;

    TxtReader (String file_name) throws FileNotFoundException{
        this.buffer = new BufferedReader(new FileReader(file_name));
    }
    List<String> get_from_txt() {
        try {
            String s = this.buffer.readLine();
            if (s != null){
                if (s.endsWith("\t")) {                 /* To prevent last data cell lost, if it was empty */
                    s = s + "\tend";
                }
                return Arrays.asList(s.split("\t"));
            }else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("String extracting from txt failed");
            e.printStackTrace();
            return null;
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

