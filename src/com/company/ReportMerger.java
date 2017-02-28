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
    private ArrayList<List<String[]>> sortedListsOfValArrs= new ArrayList<>();
    private ArrayList<boolean[]> processedValsIndicatorList = new ArrayList<>();
    private int keyPosition;
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
        calcLines();
        StringJoiner sj = new StringJoiner("\t");

        prepareLists();

        for (String[] sArr : headers) {
            for (String s : sArr) {
                sj.add(s);
            }
            processedLinesIncr();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAMES))) {
            bw.write(sj.toString());
            writeResult(bw);
        }

        return 0;
    }

    protected void prepareLists() throws IOException {
        for (int i = 0; i < INPUT_FILE_NAMES.length; i++) {
            sortedListsOfValArrs.add(makeList(INPUT_FILE_NAMES[i], MERGE_WORD_I[i], true, true));
        }
    }

    protected void writeResult(BufferedWriter bw) throws IOException {
        while (processedLines < totalLines) {
            bw.newLine();
            bw.write(merge());
            updProgress();
        }
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

    private void calcLines() throws IOException {
        long result = 0;
        for (String fName : INPUT_FILE_NAMES) {
            try (BufferedReader br = new BufferedReader(new FileReader(fName))) {
                result += br.lines().count();
            }
        }
        totalLines = result * 2;
    }

    List<String[]> makeList(String fileName, int mergeIndex, boolean onlyUniq, boolean sorted)
                                          throws IllegalArgumentException, IOException {
        String[] tmpArr;
        List<String[]> result = new ArrayList<>();
        HashSet<String> uniqDetector = new HashSet<>();
        TxtReader txtReader = new TxtReader(fileName);

        try {
            headers.add(txtReader.getArrayFromTxt());
            processedLines++;

            tmpArr = txtReader.getArrayFromTxt();
            while (tmpArr != null) {
                result.add(tmpArr);
                if (onlyUniq) {
                    if (!uniqDetector.add(tmpArr[mergeIndex])) {
                        throw new IllegalArgumentException("Колонка, выбранная связующей, содержит" +
                                " повторяющиеся значения.\nПравилькое слияние не получится. " +
                                "Однозначно сопоставить можно только уникальные идентификаторы.\n" +
                                "Значение: " + tmpArr[mergeIndex] + "\n" +
                                "Файл: " + fileName);
                    }
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

        if (sorted) {
            sortList(result, mergeIndex);
        }

        processedValsIndicatorList.add(new boolean[result.size()]);
        return result;
    }

    private List<String[]> sortList(List<String[]> list, int mergeIndex) {
        Comparator<String[]> comparator = (ar1, ar2) -> ar1[mergeIndex].compareTo(ar2[mergeIndex]);
        Collections.sort(list, comparator);
        return list;
    }

    private String merge() {
        StringJoiner result = new StringJoiner("\t");
        String[] key = null;
        int keyIndex = -1;

        for (int i = 0; i < sortedListsOfValArrs.size(); i++) {
            List<String[]> list = sortedListsOfValArrs.get(i);
            if (!list.isEmpty()) {
                if (!allValsInListProcessed(list)) {
                    boolean b = processedValsIndicatorList.get(i)[keyPosition];
                    while (keyPosition < list.size() && processedValsIndicatorList.get(i)[keyPosition]) {
                        keyPosition++;
                    }
                    if (!allValsInListProcessed(list)) break;
                }
            }
        }

        for (int i = 0; i < sortedListsOfValArrs.size(); i++) {
            if (sortedListsOfValArrs.get(i).isEmpty()) {
                addNotFound(result, headers.get(i).length);
            } else {
                if (key == null) {
                    key = sortedListsOfValArrs.get(i).get(keyPosition);
                    keyIndex = MERGE_WORD_I[i];

                    addFound(result, sortedListsOfValArrs.get(i).get(keyPosition));
                    processedValsIndicatorList.get(i)[keyPosition] = true;
                    keyPosition++;
                } else {
                    int matchIndex = findInList(sortedListsOfValArrs.get(i), key, keyIndex, i);
                    if (matchIndex < 0 || processedValsIndicatorList.get(i)[matchIndex]) {
                        addNotFound(result, headers.get(i).length);
                    } else {
                        addFound(result, sortedListsOfValArrs.get(i).get(matchIndex));
                        processedValsIndicatorList.get(i)[matchIndex] = true;
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

    void addNotFound(StringJoiner sj, int size) {
        for (int i = 0; i < size; i++) {
            sj.add("NOT FOUND!");
        }
    }

    void addFound(StringJoiner sj, String[] sArr) {
        for (String s : sArr) {
            sj.add(s);
        }
        processedLines++;
    }

    int findInList(List<String[]> list,String[] key, int keyMergeI, int listIndex) {
        Comparator<String[]> comparator = (sArrCollec, sArrKey) ->
                sArrCollec[MERGE_WORD_I[listIndex]].compareTo(sArrKey[keyMergeI]);

        return Collections.binarySearch(list, key, comparator);
    }

    /**
     * If all values have been processed, the list will be emptied and keyPosition will be set to 0.
     * @param list to check.
     * @return true, if all values have been processed. Otherwise - false.
     */
    private boolean allValsInListProcessed(List<String[]> list) {
        if (keyPosition >= list.size()) {
            list.clear();
            keyPosition = 0;
            return true;
        }
        return false;
    }

    void processedLinesIncr() {
        processedLines++;
    }

    void updProgress() {
        setProgress(progress.apply(processedLines));
    }
}
