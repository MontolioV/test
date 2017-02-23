package com.company;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.function.Function;

/**
 * This class allows to make single txt file from a number of reports, generated with {@link DataWH}.
 * To merge them.
 * <p>Created by MontolioV on 22.02.17.
 */
public class ReportMerger extends SwingWorker<Integer, String> {
    private final String[] INPUT_FILE_NAMES;
    private final String OUTPUT_FILE_NAMES;
    private final int[] MERGE_WORD_I;
    private ArrayList<String[]> headers = new ArrayList<>();
    private ArrayList<List<String[]>> sortedListsOfValArrs;
    private long totalLines;
    private long processedLines;
    private Function<Long, Integer> progress;

    public ReportMerger(String[] repNames, int[] mergeWordIndexes, String output) {
        INPUT_FILE_NAMES = repNames;
        MERGE_WORD_I = mergeWordIndexes;
        OUTPUT_FILE_NAMES = output;
        progress = (lines) -> (int) (((double) lines / totalLines) * 100);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     * <p>
     * <p>
     * Note that this method is executed only once.
     * <p>
     * <p>
     * Note: this method is executed in a background thread.
     *
     * @return the computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer doInBackground() throws Exception {
        totalLines = calcLines();
        sortedListsOfValArrs = new ArrayList<>();
        StringJoiner sj = new StringJoiner("\t");

        for (int i = 0; i < INPUT_FILE_NAMES.length; i++) {
            sortedListsOfValArrs.add(makeSortedList(INPUT_FILE_NAMES[i], MERGE_WORD_I[i]));
        }

        for (String[] sArr : headers) {
            for (String s : sArr) {
                sj.add(s);
            }
            processedLines++;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAMES))) {
            bw.write(sj.toString());
            while (processedLines < totalLines) {
                bw.newLine();
                bw.write(merge());
            }
        }


        return 0;
    }

    /**
     * Executed on the <i>Event Dispatch Thread</i> after the {@code doInBackground}
     * method is finished. The default
     * implementation does nothing. Subclasses may override this method to
     * perform completion actions on the <i>Event Dispatch Thread</i>. Note
     * that you can query status inside the implementation of this method to
     * determine the result of this task or whether this task has been cancelled.
     *
     * @see #doInBackground
     * @see #isCancelled()
     * @see #get
     */
    @Override
    public void done() {
        headers.forEach(ar -> System.out.println(Arrays.toString(ar)));
        System.out.println("totalLines: " + totalLines + "\nprocessedLines: " + processedLines);
    }

    private long calcLines() throws IOException {
        long result = 0;
        for (String fName : INPUT_FILE_NAMES) {
            try (BufferedReader br = new BufferedReader(new FileReader(fName))) {
                result += br.lines().count();
            }
        }
        return result * 2;
    }

    private List<String[]> makeSortedList(String fileName, int mergeIndex)
                                          throws IllegalArgumentException, IOException {
        Comparator<String[]> comparator = (ar1, ar2) -> ar1[mergeIndex].compareTo(ar2[mergeIndex]);

        String[] tmpArr;
        List<String[]> result = new LinkedList<>();
        HashSet<String> uniqDetector = new HashSet<>();
        TxtReader txtReader = new TxtReader(fileName);

        try {
            headers.add(txtReader.getArrayFromTxt());
            processedLines++;

            tmpArr = txtReader.getArrayFromTxt();
            while (tmpArr != null) {
                result.add(tmpArr);
                if (!uniqDetector.add(tmpArr[mergeIndex])) {
                    throw new IllegalArgumentException("Колонка, выбранная связующей, содержит" +
                            " повторяющиеся значения.\nПравилькое слияние не получится. " +
                            "Однозначно сопоставить можно только уникальные идентификаторы.\n" +
                            "Файл:\n" + fileName);
                }
                tmpArr = txtReader.getArrayFromTxt();
                setProgress(progress.apply(++processedLines));
            }
        }finally {
            txtReader.close_buffer();
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Файл не содержит значений.\n" + fileName);
        }
        Collections.sort(result, comparator);
        return result;
    }

    private String merge() {
        StringJoiner result = new StringJoiner("\t");
        String[] key = null;
        int keyIndex = -1;

        for (int i = 0; i < sortedListsOfValArrs.size(); i++) {
            if (sortedListsOfValArrs.get(i).isEmpty()) {
                addNotFound(result, headers.get(i).length);
            } else {
                if (key == null) {
                    key = sortedListsOfValArrs.get(i).get(0);
                    keyIndex = MERGE_WORD_I[i];

                    addFound(result, sortedListsOfValArrs.get(i).get(0));
                    sortedListsOfValArrs.get(i).remove(0);
                } else {
                    int matchIndex = findInList(key, keyIndex, i);
                    if (matchIndex < 0) {
                        addNotFound(result, headers.get(i).length);
                    } else {
                        addFound(result, sortedListsOfValArrs.get(i).get(matchIndex));
                        sortedListsOfValArrs.get(i).remove(matchIndex);
                    }
                }
            }
        }

        if (key == null) {
            throw new NoSuchElementException("К этому моменту слияние все строк должно быть завершено.\n" +
                                             "THIS STRING SHOULD NOT APPEAR");
        }
        return result.toString();
    }

    private void addNotFound(StringJoiner sj, int size) {
        for (int i = 0; i < size; i++) {
            sj.add("not found");
        }
    }

    private void addFound(StringJoiner sj, String[] sArr) {
        for (String s : sArr) {
            sj.add(s);
        }
        processedLines++;
    }

    private int findInList(String[] key, int keyMergeI, int listIndex) {
        Comparator<String[]> comparator = (sArrCollec, sArrKey) ->
                sArrCollec[MERGE_WORD_I[listIndex]].compareTo(sArrKey[MERGE_WORD_I[keyMergeI]]);

        return Collections.binarySearch(sortedListsOfValArrs.get(listIndex), key, comparator);
    }
}
