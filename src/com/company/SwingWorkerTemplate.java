package com.company;

import javax.swing.*;

/**
 * Common stuff for all my swingworker classes.
 * <p>Created by MontolioV on 13.04.17.
 */
public abstract class SwingWorkerTemplate extends SwingWorker<Integer, String> {

    void checkIfInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
