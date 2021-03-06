package com.JUnit_tests;

import com.company.ReportMerger;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.Assert.*;

/**
 * <p>Created by MontolioV on 22.02.17.
 */
public class ReportMergerTest {
    private ReportMerger merger;
    private final String OUT_FILE_NAME = "unit_test_txt/Unit_test_output.txt";
    private final String[] INPUT_FILE_NAMES = {
            "unit_test_txt/Unit_test_merge1.txt",
            "unit_test_txt/Unit_test_merge2.txt",
            "unit_test_txt/Unit_test_merge3.txt"
    };
    private final int[] RIGHT_MERGE_COL = {0, 4, 4};
    private final int[] WRONG_MERGE_COL = {0, 0, 0};
    private final String[] GOOD_RESULT = {
            "KeyWord 1\tKeyWord 2" +
                    "\tCycle 1\tCycle 2/1\tCycle 2/2\tCycle 3\tKeyWord 2" +
                    "\tCycle 1\tCycle 2/1\tCycle 2/2\tCycle 3\tKeyWord 3",
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1",
            "2\t2\t1\t1\t1\t2\t2\t1\t1\t1\t2\t2",
            "3\t3\t1\t2\t2\t3\t3\t1\t2\t2\t3\t3",
            "4\t4\t2\t3\t3\t4\t4\t2\t3\t3\t4\t4",
            "5\t5\t3\t4\t4\t5\t5\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!",
            "NOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\tNOT FOUND!\t3\t4\t4\t5\t5,1"
    };

    @Test
    public void doInBackground_Ok() throws Exception {
        merger = new ReportMerger(INPUT_FILE_NAMES, RIGHT_MERGE_COL, OUT_FILE_NAME);

        assertEquals(0, merger.getProgress());
        merger.doInBackground();
        merger.done();
        assertEquals(100, merger.getProgress());

        try (BufferedReader br = new BufferedReader(new FileReader(OUT_FILE_NAME))) {
            String s;
            for (int j = 0; (s = br.readLine()) != null; j++) {
                assertEquals(GOOD_RESULT[j], s);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void doInBackground_MergeColHasDuplicates() throws Exception {
        try {
            merger = new ReportMerger(INPUT_FILE_NAMES, WRONG_MERGE_COL, OUT_FILE_NAME);
            merger.doInBackground();
        } finally {
            merger.done();
        }
    }
}