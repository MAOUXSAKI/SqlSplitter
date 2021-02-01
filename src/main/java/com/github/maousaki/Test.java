package com.github.maousaki;

import com.github.maousaki.oracle.OracleSqlSplit;

import java.io.IOException;
import java.util.List;

/**
 * @author Maou Saki
 * @date: 2021/2/1 14:05
 */
public class Test {

    public static void main(String[] args) throws IOException {
        TestClass testClass = new TestClass();
        for (String sql : testClass.test()) {
            System.out.println(sql);
        }
    }

    public static class TestClass {
        public List<String> test() throws IOException {
            return new OracleSqlSplit(this.getClass().getResourceAsStream("insert_test.sql")).getSqlList();
        }
    }

}
