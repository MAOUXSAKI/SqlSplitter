package com.github.maousaki.oracle;

import com.github.maousaki.SqlSplit;
import com.github.maousaki.oracle.end_check.EndCheck;
import com.github.maousaki.oracle.end_check.impl.CreatePackageBodyEndCheck;
import com.github.maousaki.oracle.end_check.impl.CreatePackageEndCheck;
import com.github.maousaki.oracle.end_check.impl.CreateSpecialEndCheck;
import com.github.maousaki.oracle.end_check.impl.NormalEndCheck;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Maou Saki
 * @date: 2021/2/1 13:37
 */
public class OracleSqlSplit extends SqlSplit {
    
    private List<String> sqlList;

    public static final List<String> SPECIAL_CREATE_SUB_PARAM = Arrays.asList("trigger","procedure","function");

    public OracleSqlSplit(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public List<String> getSqlList() throws IOException {
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        sqlList = new ArrayList<>();
        StringBuffer command = new StringBuffer();
        String line;
        String action = "";
        String subParam = "";
        boolean end = false;
        StringBuffer word = new StringBuffer();
        EndCheck endChecker = null;
        boolean packageCheck = false;
        boolean textStart = false;
        while ((line = lineReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            boolean wordStart = false;
            boolean wordEnd = false;
            char[] charLine = line.toCharArray();
            boolean shouldAppendChar = false;
            // 一个一个字符进行读取
            for (int i = 0; i < line.length(); i++) {
                char c = charLine[i];
                // 判断当前是否处于文本中
                if (textStart) {
                    //以单引号为结尾判断文本是否结束
                    if ('\'' == c) {
                        textStart = false;
                    }
                    //判断是否需要单独将字符放入命令中
                    shouldAppendChar = true;
                } else {
                    if ('-' == c) {
                        //若当前字符和下一个字符都为-则说明后面的内容都为注释则跳过改行剩下的内容
                        if (i + 1 < line.length() && charLine[i + 1] == '-') {
                            break;
                        }
                    } else if ('\'' == c) {
                        //以单引号未开始到下一个单引号为止都作为文本处理
                        textStart = true;
                        //并且也认为单词结束
                        wordEnd = true;
                        shouldAppendChar = true;
                    }
                    if (!textStart){
                        if (!wordStart) {
                            //读到非空格则认为单词开始，(这里不是严格意义上的单词，主要是取空格分隔的一个字符串)
                            if (c != ' ') {
                                wordStart = true;
                                wordEnd = false;
                            }
                        }
                        if (wordStart){
                            //将;与/作为单独的单词来进行处理，同时排除掉两个sql间的多余的分号与/，如果该字符为最后一个字符或者下一个字符为空行、分号则立即结束单词判断
                            if (i == line.length() - 1 || charLine[i + 1] == ' ' || charLine[i+1] == ';' || c == ';' || c == '/' ) {
                                wordEnd = true;
                                if (c != ' ' && c != '\'' && !(StringUtils.isBlank(action) && (c == ';' || c =='/'))){
                                    word.append(c);
                                }
                            }
                        }
                    }
                }
                //以单词为单位进行sql是否结束的判断
                if (wordEnd) {
                    //获取到了一个单词开始进行判断
                    String wordStr = word.toString().toLowerCase();
                    if (StringUtils.isNotBlank(wordStr)) {
                        //如果当前action为空，则说明一个sql语句即将开始
                        if (StringUtils.isBlank(action)) {
                            //跳过分号和/行
                            if (!wordStr.equals(";") && !wordStr.equals("/")){
                                action = wordStr;
                            }
                        } else if (StringUtils.isBlank(subParam)) {
                            //获取动词紧跟的参数
                            subParam = wordStr;
                            if (subParam.equals("or")){
                                //如果该参数为or则说明后面还有一个动词，则直接将动词置为空重新获取
                                action = "";
                                subParam = "";
                            }else if (subParam.equals("package")){
                                //如果该参数为package 则还需要判断一个词
                                packageCheck = true;
                            } else if ((action.equals("create") || action.equals("replace")) && SPECIAL_CREATE_SUB_PARAM.contains(subParam)){
                                //如果过动词为create或者Replace,参数为需要特殊处理的参数则用特除创建结束检查类
                                endChecker = new CreateSpecialEndCheck();
                            }else {
                                //其它则用普通检查类
                                endChecker = new NormalEndCheck();
                            }
                        } else if (packageCheck){
                            if (wordStr.equals("body")){
                                //如果第三个词为body则需要用body结束检查类
                                endChecker = new CreatePackageBodyEndCheck();
                            }else {
                                endChecker = new CreatePackageEndCheck();
                            }
                            packageCheck = false;
                        }
                        if (endChecker != null) {
                            //如果结束检查被复制，则检查一个sql语句是否结束
                            end = endChecker.checkSqlEnd(word);
                        }
                        //将该单词放入命令
                        command.append(word.toString());
                        //一个单词读取完了，置空读取下一个单词
                        word.setLength(0);

                        //如果命令不为空并且上一个单词结尾不是.则填入一个空格
                        if (command.length() != 0 && command.charAt(command.length() - 1) != '.') {
                            command.append(" ");
                        }

                    }
                    //重置变量
                    wordEnd = false;
                    wordStart = false;
                } else {
                    if (wordStart) {
                        //将字符放入单词
                        word.append(c);
                    }
                }

                if (shouldAppendChar){
                    command.append(c);
                    shouldAppendChar = false;
                }

                if (end) {
                    //一个sql结束重置所有变量
                    String sql = command.toString();
                    if (StringUtils.isNotBlank(sql)) {
                        action = "";
                        subParam = "";
                        sqlList.add(command.toString());
                        command.setLength(0);
                    }
                    String wordStr = word.toString();
                    if (StringUtils.isNotBlank(wordStr)) {
                        word.setLength(0);
                    }
                    end = false;
                    endChecker = null;
                }
            }
        }
        return sqlList;
    }
}
