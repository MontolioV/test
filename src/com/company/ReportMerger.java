package com.company;

import javax.swing.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * This class allows to make single txt file from a number of reports, generated with {@link DataWH}.
 * To merge them.
 * <p>Created by MontolioV on 22.02.17.
 */
public class ReportMerger extends SwingWorker<Integer, String> {
    private final String[] INPUT_FILE_NAMES;
    private final int[] MERGE_WORD_I;

    public ReportMerger(String[] repNames, int[] mergeWordIndexes) {
        INPUT_FILE_NAMES = repNames;
        MERGE_WORD_I = mergeWordIndexes;
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

        for (int i = 0; i < INPUT_FILE_NAMES.length; i++) {
            makeTreeSet(INPUT_FILE_NAMES[i], MERGE_WORD_I[i]);
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

    }

    private TreeSet<String[]> makeTreeSet(String fileName, int mergeIndex)
                                            throws IllegalArgumentException, IOException {
        Comparator<String[]> comparator = (ar1, ar2) -> ar1[mergeIndex].compareTo(ar2[mergeIndex]);
        TreeSet<String[]> result = new TreeSet<>(comparator);
        TxtReader txtReader = new TxtReader(fileName);

        try {
            if (!result.add(txtReader.getArrayFromTxt())) {
                throw new IllegalArgumentException("Колонка, выбранная связующей, содержит" +
                        " повторяющиеся значения.\nПравилькое слияние не получится. " +
                        "Однозначно сопоставить можно только уникальные идентификаторы.\n" +
                        "Файл:\n" + fileName);
            }
        }finally {
            txtReader.close_buffer();
        }
        return result;
    }
}
