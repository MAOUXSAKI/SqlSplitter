package com.github.maousaki.oracle.end_check.impl;

import com.github.maousaki.oracle.end_check.EndCheck;

/**
 * @author Maou Saki
 */
public class NormalEndCheck implements EndCheck {

    @Override
    public boolean checkSqlEnd(StringBuffer word) {
        int last = word.length();
        char lastChar = word.charAt(last - 1);
        if (lastChar == ';' || lastChar == '/'){
            word.deleteCharAt(last - 1);
            return true;
        }
        return false;
    }
}
