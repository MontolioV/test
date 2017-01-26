package com.company;

import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to output AX report (.txt) and parse strings to logic parts.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

class TxtReader {
    private BufferedReader buffer = null;
    private File targetFile;

    public TxtReader(String file_name) throws FileNotFoundException {
        this(new File(file_name));
    }

    public TxtReader(File file) throws FileNotFoundException {
        targetFile = file;
        this.buffer = new BufferedReader(new FileReader(file));
    }

    List<String> getListFromTxt() {
        String[] sArr = getArrayFromTxt();
        if (sArr != null) {
            return Arrays.asList(sArr);
        } else {
            return null;
        }
    }

    String[] getArrayFromTxt() {
        try {
            String s = this.buffer.readLine();
            if (s != null){
                if (s.endsWith("\t")) {                 /* To prevent last data cell lost, if it was empty */
                    s = s + "\tend";
                }
                return s.split("\t");
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

    /**
     * This method returns the amount of lines in file. BufferReader is reinitialised.
     */
    long getAmountOfLines() throws FileNotFoundException{
        long result = buffer.lines().count();
        close_buffer();
        buffer = new BufferedReader(new FileReader(targetFile));
        return result;
    }
}

