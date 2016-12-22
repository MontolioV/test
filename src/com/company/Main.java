package com.company;

public class Main {
    public static void main(String[] args) {
        long stT = System.currentTimeMillis();
        DataWH dwh = new DataWH("Заказ", new String[]{"Накладная", "Дата", "Сумма накладной", "Счет на",
                                "Статус документа","Сторно"});
        //dwh.parse("09.txt");
        System.out.println("Finished in " +
                            String.valueOf((double) (System.currentTimeMillis() - stT) / 1000) +
                            " sec");

        Gui gui = new Gui();
        gui.go();
    }
}

