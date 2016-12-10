package com.company;

import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        DataWH dwh = new DataWH("Заказ", new String[]{"Накладная", "Дата", "Сумма накладной", "Счет на",
                                "Статус документа", "Сторно"});
        dwh.parse("2.txt");
        System.out.println("end");
    }
}

class TxtReader {
    private BufferedReader buffer = null;

    TxtReader (String file_name) throws FileNotFoundException{
           this.buffer = new BufferedReader(new FileReader(file_name));
    }
    List<String> get_from_txt() {
        try {
            String s = this.buffer.readLine();
            if (s != null){
                if (s.endsWith("\t")) { //To prevent last data cell lost, if it was empty
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

class DataWH{
    private final String CYCLE_WORD;
    private String cw_value;
    private final List<String> KEY_WORDS;
    private final ArrayList<Hashtable<String,String>> STORAGE = new ArrayList<>();
    private Hashtable<String,String> tmp_ht = new Hashtable<>(15);

    DataWH(String cw, String [] kws) {
        this.CYCLE_WORD = cw;
        this.KEY_WORDS = Arrays.asList(kws);
    }
    void parse(String file_name){
        try{
            TxtReader reader = new TxtReader(file_name);
            boolean unfinished = true;
            while (unfinished){
                unfinished = makeHT(reader.get_from_txt());
            }
            reader.close_buffer();
            //
            System.out.println(STORAGE.size());
        } catch (FileNotFoundException e) {
            System.out.println("Указанный файл не найден!");
            e.printStackTrace();
        }
    }
    private boolean makeHT (List<String> list){
        if (tmp_ht.size() == KEY_WORDS.size()){
            tmp_ht.put(CYCLE_WORD,cw_value);
            STORAGE.add(tmp_ht);
            tmp_ht = new Hashtable<>(15);
        }
        if (list == null){
            return false;
        }
        if (list.contains(CYCLE_WORD)){
            cw_value = list.get(list.indexOf(CYCLE_WORD)+1);
        }
        for (int i = 0; i < list.size(); i = i + 2) {
            if (KEY_WORDS.contains(list.get(i))) {
                tmp_ht.put(list.get(i),list.get(i+1));
            }
        }
        return true;
    }
}
