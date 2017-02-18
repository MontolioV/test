package com.JUnit_tests;

import com.company.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>Created by MontolioV on 16.02.17.
 */
public class DataWHTest {
    private DataWH dwh;
    private List<String> cws;
    private List<CWwithLvl> cWwithLvls;
    private List<String> kws;
    private String input = "unit_test_txt/Unit_test_input_UTF-8.txt";
    private String output = "unit_test_txt/Unit_test_output.txt";

    @Before
    public void setUp() throws Exception {
        cws = Arrays.asList("Cycle 1", "Cycle 2/1", "Cycle 2/2", "Cycle 3");
        kws = Arrays.asList("KeyWord 1", "KeyWord 2", "KeyWord 3");
    }

    @After
    public void tearDown() throws Exception {
        dwh.done();
    }

    @Test
    public void doInBackground_Algorithm() throws Exception {
        cWwithLvls = Arrays.asList(new CWwithLvl("Cycle 1", 1), new CWwithLvl("Cycle 2/1", 2),
                new CWwithLvl("Cycle 2/2", 2), new CWwithLvl("Cycle 3", 3));
        dwh = new DataWH(input, output, cws, cWwithLvls, kws);
        String[] expectedVals = new String[]{
                "Cycle 1\tCycle 2/1\tCycle 2/2\tCycle 3\tKeyWord 1\tKeyWord 2\tKeyWord 3\t",
                "1\t1\t1\t1\t1\t1\t1",
                "1\t1\t1\t2\t2\t2\t2",
                "1\t2\t2\t3\t3\t3\t3",
                "2\t3\t3\t4\t4\t4\t4"};

        dwh.doInBackground();
        dwh.done();
        try (BufferedReader br = new BufferedReader(new FileReader(output))) {
            for (int i = 0; i < expectedVals.length; i++) {
                assertEquals(expectedVals[i], br.readLine());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void doInBackground_BadCWs() throws Exception {
        cWwithLvls = Arrays.asList(new CWwithLvl("Cycle 1", 100), new CWwithLvl("Cycle 2/1", 2),
                new CWwithLvl("Cycle 2/2", 2), new CWwithLvl("Cycle 3", 3));
        dwh = new DataWH(input, output, cws, cWwithLvls, kws);

        dwh.doInBackground();
    }
}