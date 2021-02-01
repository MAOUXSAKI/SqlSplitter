package com.github.maousaki;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Maou Saki
 */
public abstract class SqlSplit {

    protected InputStream inputStream;

    public SqlSplit(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public abstract List<String> getSqlList() throws IOException;

}
