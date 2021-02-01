package com.github.maousaki.oracle.end_check.impl;

import com.github.maousaki.oracle.end_check.EndCheck;

/**
 * @author Maou Saki
 */
public class CreatePackageEndCheck implements EndCheck {

    private boolean endStart = false;
    private int count = 1;

    @Override
    public boolean checkSqlEnd(StringBuffer word) {
        String wordStr = word.toString().toLowerCase();
        if (wordStr.equals("end")) {
            endStart = true;
        }else if (endStart && wordStr.equals(";")){
            endStart = false;
            count--;
        }

        if (count == 0){
            return true;
        }else {
            return false;
        }
    }

}
