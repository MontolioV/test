package com.JUnit_tests;

import org.junit.Before;
import org.junit.Test;
import com.company.KeyWordWithFrequency;
import com.company.KWFrequencyComparator;

import static org.junit.Assert.*;

/**
 * JUnit test for KeyWordWithFrequency class and its comparator.
 * <p>Created by MontolioV on 22.02.17.
 */
public class KeyWordWithFrequencyTest {
    private KeyWordWithFrequency kw;
    private KeyWordWithFrequency kwFullEq;
    private KeyWordWithFrequency kwNameEq;
    private KeyWordWithFrequency kwFrEq;
    private KeyWordWithFrequency kwFullUniq;
    private KWFrequencyComparator comparator = new KWFrequencyComparator();

    @Before
    public void setUp() throws Exception {
        kw = new KeyWordWithFrequency("one");
        kwFullEq = new KeyWordWithFrequency("one");
        kwFrEq = new KeyWordWithFrequency("two");
        kwNameEq = new KeyWordWithFrequency("one");
        kwNameEq.riseFrequ();
        kwFullUniq = new KeyWordWithFrequency("a");
        for (int i = 0; i < 2; i++) {
            kwFullUniq.riseFrequ();
        }
    }

    @Test
    public void getFrequ() throws Exception {
        assertEquals(0, kw.getFrequ());
        assertEquals(2, kwFullUniq.getFrequ());
    }

    @Test
    public void riseFrequ() throws Exception {
        for (int i = 0; i < 5; i++) {
            kw.riseFrequ();
        }
        assertEquals(5,kw.getFrequ());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("one", kw.getName());
        assertEquals("two", kwFrEq.getName());
    }

    @Test
    public void equals() throws Exception {
        assertTrue(kw.equals(kwFullEq));
        assertTrue(kwNameEq.equals(kw));
        assertFalse(kw.equals(kwFrEq));
        assertFalse(kwFullUniq.equals(kw));
    }

    @Test
    public void compareTo() throws Exception {
        assertEquals(0, kw.compareTo(kwFullEq));
        assertEquals(0, kw.compareTo(kwNameEq));
        assertTrue(kw.compareTo(kwFrEq) < 0);
        assertTrue(kw.compareTo(kwFullUniq) > 0);
        assertNotEquals(0, kw.compareTo(kwFrEq));
        assertNotEquals(0, kw.compareTo(kwFullUniq));
    }

    //Test for comparator
    @Test
    public void compare() throws Exception {
        assertEquals(0, comparator.compare(kw, kwFrEq));
        assertEquals(0, comparator.compare(kwFullEq, kw));
        assertEquals(1, comparator.compare(kwNameEq, kw));
        assertEquals(-2, comparator.compare(kw, kwFullUniq));
    }
}