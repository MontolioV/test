package com.company;

import java.util.*;
import java.io.*;

/**
 * Class to output AX report (.txt) and parse strings to logic parts.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

public class TxtReader {
    private BufferedReader buffer = null;
    private File targetFile;

    public TxtReader(String file_name) throws FileNotFoundException {
        this(new File(file_name));
    }

    public TxtReader(File file) throws FileNotFoundException {
        targetFile = file;
        this.buffer = new BufferedReader(new FileReader(file));
    }

    public List<String> getListFromTxt() throws IOException {
        String[] sArr = getArrayFromTxt();
        if (sArr != null) {
            return Arrays.asList(sArr);
        } else {
            return null;
        }
    }

    public String[] getArrayFromTxt() throws IOException {
        try {
            String s = this.buffer.readLine();
            if (s != null){
                // To prevent last data cell lost, if cell was empty.
                if (s.endsWith("\t")) {
                    s = s + "\tend";
                    String[] sArr = s.split("\t");
                    return Arrays.copyOf(sArr, sArr.length - 1);
                }
                return s.split("\t");
            }else {
                return null;
            }
        } catch (IOException e) {
            close_buffer();
            throw new IOException("Не удалось прочитать строку из файла.", e);
        }
    }

    public void close_buffer(){
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
    public long getAmountOfLines() throws FileNotFoundException{
        long result = buffer.lines().count();
        close_buffer();
        buffer = new BufferedReader(new FileReader(targetFile));
        return result;
    }
}

