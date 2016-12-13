package com.company;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * Checks set to keep the invoices that must be on hand and yet are not.
 * <p>Will work only if necessary for analysis columns (KW) are in list.
 * <p>
 * Created by MontolioV on 10.12.2016.
 */
class CheckDriversDebts implements Check {
    private final String[] CRITERIA = new String[] {"Статус документа","Сторно","Сумма накладной","Счет на"};
    @Override
    public boolean check(Hashtable<String, String> ht) {
        try {
            if (ht.get("Статус документа").equals("Да")) {
                return false;
            } else if (ht.get("Сторно").equals("Да")) {
                return false;
            } else if (ht.get("Сумма накладной").contains("-")) {
                return false;
            } else {
                switch (ht.get("Счет на")) {
                    case "ПранаФарм":
                    case "МедСервисХарько":
                    case "МедСервисФирма":
                        return false;
                    default:
                        return true;
                }
            }
        } catch (NullPointerException npe) {
            System.out.println("Для корректного отчета нужны следующие колонки:" + Arrays.toString(CRITERIA));
            throw npe;
        }
    }
}
