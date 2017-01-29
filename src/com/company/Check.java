package com.company;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * General contract for AX_report_parser checker classes.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */
interface Check {

    boolean check (HashMap<String, String> dataStructure);
}
