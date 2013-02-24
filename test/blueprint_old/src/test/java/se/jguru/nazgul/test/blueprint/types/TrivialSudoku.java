/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.blueprint.types;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TrivialSudoku implements Sudoku {

    @Override
    public boolean isHard() {
        return false;
    }
}
