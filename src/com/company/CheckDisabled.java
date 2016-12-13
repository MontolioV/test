package com.company;

import java.util.Hashtable;

/**
 * Use to output all data without filtration.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */
class CheckDisabled implements Check {

    @Override
    public boolean check(Hashtable<String, String> ht) {
        return true;
    }
}
