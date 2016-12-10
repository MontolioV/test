package com.company;

public class Main {
    public static void main(String[] args) {
        DataWH dwh = new DataWH("Заказ", new String[]{"Накладная", "Дата", "Сумма накладной", "Счет на",
                                "Статус документа","Сторно"});
        dwh.parse("3.txt");
        System.out.println("end");
    }
}

