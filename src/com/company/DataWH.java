package com.company;

import java.util.*;
import java.io.*;

/**
 * Core class to manipulate and store data.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */

class DataWH {
    private final String CYCLE_WORD;
    private String cwValue;
    private final List<String> KEY_WORDS;
    private final ArrayList<Hashtable<String,String>> STORAGE = new ArrayList<>();
    private Hashtable<String,String> tmpHt = new Hashtable<>(15);
    private Check chk;

    DataWH(String cw, String [] kws) {
        this.CYCLE_WORD = cw;
        this.KEY_WORDS = Arrays.asList(kws);
    }
    void parse(String file_name){
        try{
            chk = new CheckDriversDebts();
            TxtWriter writer = new TxtWriter(CYCLE_WORD, KEY_WORDS);
            TxtReader reader = new TxtReader(file_name);
            boolean unfinished = true;

            while (unfinished){
                unfinished = makeHT(reader.get_from_txt());
            }
            reader.close_buffer();

            System.out.println(STORAGE.size());
            writer.write_to_txt(STORAGE);
        } catch (FileNotFoundException e) {
            System.out.println("Указанный файл не найден!");
            e.printStackTrace();
        }
    }
    private boolean makeHT (List<String> list){
        if (tmpHt.size() == KEY_WORDS.size()){
            tmpHt.put(CYCLE_WORD, cwValue);
            if (chk.check(tmpHt)) {
                STORAGE.add(tmpHt);
            }
            tmpHt = new Hashtable<>(15);
        }
        if (list == null){                                      /* End of input */
            return false;
        }
        if (list.contains(CYCLE_WORD)){
            cwValue = list.get(list.indexOf(CYCLE_WORD)+1);     /* Next string after CW is CW value */
        }
        for (int i = 0; i < list.size(); i += 2) {           /* Even strings (and 0) are KWs */
            if (KEY_WORDS.contains(list.get(i))) {              /* Odd strings are KW values */
                tmpHt.put(list.get(i), list.get(i + 1));           /* Need to extract only KW values */
            }
        }
        return true;
    }
}
