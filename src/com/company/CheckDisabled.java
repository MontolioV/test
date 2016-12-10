package com.company;

import java.util.Hashtable;

/**
 * Created by main on 10.12.2016.
 */
class CheckDisabled implements Check {

    @Override
    public boolean check(Hashtable<String, String> ht) {
        return true;
    }
}
