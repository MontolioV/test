package com.company;

/**
 * This class allows to show hierarchy of Cycle words.
 * <p>Cycle 0 includes all CW with lvl 1 and deeper. And so on.
 * If "super" cycle ends, all "sub" cycles also must end.
 * <p>Created by MontolioV on 28.01.2017.
 */
public class CWwithLvl {
    private String cw;
    private int lvl;

    public CWwithLvl(String cw, int lvl) {
        this.cw = cw;
        this.lvl = lvl;
    }

    public String getCw() {
        return cw;
    }

    public int getLvl() {
        return lvl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CWwithLvl)) return false;

        CWwithLvl cWwithLvl = (CWwithLvl) o;

        if (lvl != cWwithLvl.lvl) return false;
        return cw.equals(cWwithLvl.cw);
    }

    @Override
    public int hashCode() {
        int result = cw.hashCode();
        result = 31 * result + lvl;
        return result;
    }
}
