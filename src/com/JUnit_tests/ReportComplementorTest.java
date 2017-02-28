package com.JUnit_tests;

import com.company.ReportComplementor;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.Assert.*;

/**
 * <p>Created by MontolioV on 27.02.17.
 */
public class ReportComplementorTest {
    private ReportComplementor complementor;
    private final String OUT_FILE_NAME = "unit_test_txt/Unit_test_output.txt";
    private final String[] INPUT_FILE_NAMES_RIGHT = {
            "unit_test_txt/Unit_test_complementor1.txt",
            "unit_test_txt/Unit_test_merge2.txt",
            "unit_test_txt/Unit_test_merge3.txt"
    };
    private final String[] INPUT_FILE_NAMES_WRONG = {
            "unit_test_txt/Unit_test_merge1.txt",
            "unit_test_txt/Unit_test_complementor1.txt"
    };
    private final int[] MERGE_COL_RIGHT = {0, 4, 4};
    private final int[] MERGE_COL_WRONG = {0, 0};
    private final String[] GOOD_RESULT = {
            "KeyWord 1\tKeyWord 2" +
                    "\tCycle 1\tCycle 2/1\tCycle 2/2\tCycle 3\tKeyWord 2" +
                    "\tCycle 1\tCycle 2/1\tCycle 2/2\tCycle 3\tKeyWord 3",
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1",
            "2\t2\t1\t1\t1\t2\t2\t1\t1\t1\t2\t2",
            "3\t3\t1\t2\t2\t3\t3\t1\t2\t2\t3\t3",
            "4\t4\t2\t3\t3\t4\t4\t2\t3\t3\t4\t4",
            "5\t5\t3\t4\t4\t5\t5\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!",
            "5\t5\t3\t4\t4\t5\t5\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!"
    };

//    @Ignore
    @Test
    public void doInBackground_OK() throws Exception {
        complementor = new ReportComplementor(INPUT_FILE_NAMES_RIGHT, MERGE_COL_RIGHT, OUT_FILE_NAME);
        complementor.doInBackground();
        complementor.done();
        try (BufferedReader br = new BufferedReader(new FileReader(OUT_FILE_NAME))) {
            String s;
            for (int j = 0; (s = br.readLine()) != null; j++) {
                assertEquals(GOOD_RESULT[j], s);
            }
        }
    }

//    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void doInBackground_Ambiguity() throws Exception {
        try {
            complementor = new ReportComplementor(INPUT_FILE_NAMES_WRONG, MERGE_COL_WRONG, OUT_FILE_NAME);
            complementor.doInBackground();
        } finally {
            complementor.done();
        }
    }
}