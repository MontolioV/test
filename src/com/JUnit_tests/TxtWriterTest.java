package com.JUnit_tests;

import com.company.TxtWriter;
import org.junit.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * JUnit test for TxtWriter class.
 * <p></p>
 * Created by MontolioV on 14.02.17.
 */
public class TxtWriterTest {
    private final String OUT_FILE_NAME = "unit_test_txt/Unit_test_output.txt";
    private List<String> cw = Arrays.asList("cw1", "cw2");
    private List<String> kw = Arrays.asList("kw1", "kw2", "kw3");
    private TxtWriter txtWriter;
    private HashMap<String, String> testHM;

    @Before
    public void setUp() throws Exception {
        txtWriter = new TxtWriter(cw, kw, OUT_FILE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        txtWriter.close_buffer();
    }

    @Test
    public void writeLineToTxt() throws Exception {
        testHM = new HashMap<>();
        for (String s : cw) {
            testHM.put(s, "cwVal");
        }
        for (String s : kw) {
            testHM.put(s, "kwVal");
        }

        txtWriter.writeLineToTxt(testHM);
        txtWriter.close_buffer();

        try (BufferedReader br = new BufferedReader(new FileReader(OUT_FILE_NAME))) {
            assertEquals("cw1\tcw2\tkw1\tkw2\tkw3", br.readLine());
            assertEquals("cwVal\tcwVal\tkwVal\tkwVal\tkwVal", br.readLine());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}