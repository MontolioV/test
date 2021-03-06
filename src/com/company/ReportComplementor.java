package com.company;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * <p>Created by MontolioV on 27.02.17.
 */
public class ReportComplementor extends ReportMerger {
    private final String[] INPUT_FILE_NAMES;
    private final int[] MERGE_WORD_I;
    private List<String[]> mainList;
    private ArrayList<List<String[]>> sortedListsOfValArrs = new ArrayList<>();

    public ReportComplementor(String[] repNames, int[] mergeWordIndexes, String output) {
        super(repNames, mergeWordIndexes, output);
        INPUT_FILE_NAMES = repNames;
        MERGE_WORD_I = mergeWordIndexes;
    }

    @Override
    protected long calcTotalWork() throws IOException {
        long result;
        long superPart = super.calcTotalWork();
        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_NAMES[0]))) {
            result = br.lines().count();
        }
        result *= 2;
        superPart -= result;
        superPart /= 2;
        result += superPart;
        result += INPUT_FILE_NAMES.length - 1;
        return result;
    }

    @Override
    protected void prepareLists() throws IOException, InterruptedException {
        mainList = makeList(0, false, false);
        for (int i = 1; i < INPUT_FILE_NAMES.length; i++) {
            sortedListsOfValArrs.add(makeList(i, true, true));
        }
    }

    @Override
    protected void writeResult(BufferedWriter bw) throws IOException, InterruptedException {
        for (String[] sArr : mainList) {
            bw.newLine();
            bw.write(complement(sArr));
            updProgress();
            checkIfInterrupted();
        }
    }

    private String complement(String[] mainLine) {
        StringJoiner result = new StringJoiner("\t");
        int index;

        processedLinesIncr();
        addFound(result, mainLine);
        for (int i = 0; i < sortedListsOfValArrs.size(); i++) {
            int listIdxForMergeW = i + 1;
            index = findInList(sortedListsOfValArrs.get(i), mainLine, MERGE_WORD_I[0], listIdxForMergeW);
            if (index < 0) {
                addNotFound(result, sortedListsOfValArrs.get(0).get(0).length);
            } else {
                addFound(result, sortedListsOfValArrs.get(i).get(index));
            }
        }
        return result.toString();
    }
}
