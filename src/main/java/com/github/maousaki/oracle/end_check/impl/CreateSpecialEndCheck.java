package com.github.maousaki.oracle.end_check.impl;

import com.github.maousaki.oracle.end_check.EndCheck;

/**
 * @author Maou Saki
 */
public class CreateSpecialEndCheck implements EndCheck {

    private boolean begin = false;
    private int count = 0;
    private boolean endStart =false;

    @Override
    public boolean checkSqlEnd(StringBuffer word) {
        String wordStr = word.toString().toLowerCase();
        if (wordStr.equals("begin")) {
            begin = true;
            count++;
        }else if ((wordStr.equals("if") || wordStr.equals("loop")) && !endStart){
            count++;
        }else if (wordStr.equals("end")){
            endStart = true;
        }else if (endStart && wordStr.equals(";")){
            endStart = false;
            count--;
        }
        if (begin && count == 0) {
            return true;
        }else {
            return false;
        }
    }
}
