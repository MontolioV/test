package com.company;

import java.util.*;
import java.io.*;

/**
 * Created by main on 10.12.2016.
 */

class DataWH {
    private final String CYCLE_WORD;
    private String cw_value;
    private final List<String> KEY_WORDS;
    private final ArrayList<Hashtable<String,String>> STORAGE = new ArrayList<>();
    private Hashtable<String,String> tmp_ht = new Hashtable<>(15);
    private Check chk;

    DataWH(String cw, String [] kws) {
        this.CYCLE_WORD = cw;
        this.KEY_WORDS = Arrays.asList(kws);
    }
    void parse(String file_name){
        chk = new CheckDriversDebts();
        TxtWriter writer = new TxtWriter(CYCLE_WORD, KEY_WORDS);
        try{
            TxtReader reader = new TxtReader(file_name);
            boolean unfinished = true;
            while (unfinished){
                unfinished = makeHT(reader.get_from_txt());
            }
            reader.close_buffer();
            System.out.println(STORAGE.size());
        } catch (FileNotFoundException e) {
            System.out.println("Указанный файл не найден!");
            e.printStackTrace();
        }
        writer.write_to_txt(STORAGE);
    }
    private boolean makeHT (List<String> list){
        if (tmp_ht.size() == KEY_WORDS.size()){
            tmp_ht.put(CYCLE_WORD,cw_value);
            if (chk.check(tmp_ht)) {
                STORAGE.add(tmp_ht);
            }
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
