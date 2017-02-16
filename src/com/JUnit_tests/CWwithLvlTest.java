package com.JUnit_tests;

import com.company.CWwithLvl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit test for CWwithLvl class
 * <p></p>
 * Created by MontolioV on 16.02.17.
 */
public class CWwithLvlTest {
    private CWwithLvl ob1 = new CWwithLvl("test", 0);
    private CWwithLvl ob11 = new CWwithLvl("test", 0);
    private CWwithLvl ob12 = new CWwithLvl("test", 1);
    private CWwithLvl ob13 = new CWwithLvl("test1", 0);
    private CWwithLvl ob3 = new CWwithLvl("test3", 3);


    @Test
    public void getCw() throws Exception {
        assertNotNull(ob3.getCw());
        assertEquals("test", ob1.getCw());
        assertEquals(ob1.getCw(), ob11.getCw());
        assertNotEquals("", ob1.getCw());
        assertNotEquals(ob1.getCw(), ob13.getCw());
    }

    @Test
    public void getLvl() throws Exception {
        assertNotNull(ob1.getLvl());
        assertEquals(0, ob1.getLvl());
        assertEquals(ob1.getLvl(), ob11.getLvl());
        assertNotEquals(0, ob12.getLvl());
        assertNotEquals(ob1.getLvl(), ob12.getLvl());
    }

    @Test
    public void equalsT() throws Exception {
        assertTrue(ob1.equals(ob11));
        assertFalse(ob1.equals(ob12));
        assertFalse(ob1.equals(ob13));
        assertFalse(ob1.equals(ob3));
    }

    @Test
    public void hashCodeT() throws Exception {
        assertNotNull(ob1.hashCode());
        assertEquals(ob1.hashCode(), ob11.hashCode());
        assertNotEquals(ob1.hashCode(), ob12.hashCode());
        assertNotEquals(ob1.hashCode(), ob13.hashCode());
        assertNotEquals(ob1.hashCode(), ob3.hashCode());
    }

}