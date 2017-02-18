package com.JUnit_tests;

import org.junit.*;
import static org.junit.Assert.*;

import com.company.TxtReader;

/**
 * JUnit test for TxtReader class.
 * <p></p>
 * Created by MontolioV on 13.02.17.
 */
public class TxtReaderTest {
    private TxtReader txtReader;
    private final String[] line5 = {"KeyWord 1", "1", "KeyWord 2", "1", "", "end"};

    @Before
    public void setUp() throws Exception {
        txtReader = new TxtReader("unit_test_txt/Unit_test_input_UTF-8.txt");
    }

    @After
    public void tearDown() throws Exception {
        txtReader.close_buffer();
        txtReader = null;
    }

    @Test
    public void getListFromTxt() throws Exception {
        assertEquals(1, txtReader.getListFromTxt().size());
        for (String s : txtReader.getListFromTxt()) {
            assertNotNull(s);
        }
        assertEquals(5, txtReader.getListFromTxt().size());
        txtReader.getListFromTxt();
        assertArrayEquals(line5, txtReader.getListFromTxt().toArray());
    }

    @Test
    public void getArrayFromTxt() throws Exception {
        assertEquals(1, txtReader.getArrayFromTxt().length);
        for (String s : txtReader.getArrayFromTxt()) {
            assertNotNull(s);
        }
        assertEquals(5, txtReader.getArrayFromTxt().length);
        txtReader.getArrayFromTxt();
        assertArrayEquals(line5, txtReader.getArrayFromTxt());
    }

    @Test
    public void getAmountOfLines() throws Exception {
        assertEquals(27, txtReader.getAmountOfLines());
    }
}