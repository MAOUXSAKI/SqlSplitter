# SqlSplitter介绍

将一整个Sql脚本分割为由一条条sql语句组成的sql列表，用于给jdbc执行。
datagrip 能直接成功的脚本那么该Splitter分割出来的sql就能按照顺序执行。
如果有分割识别出错的项，希望能提到issue，尽可能将该解析方法做到全面。

## 注意事项
* 现在只有oracle实现,
    > postgres和mysql都可以通过添加额外并自定义分隔符,然后使用ibatis的ScriptRunner执行，而oracle jdbc由于本身不支持select 或者insert一类的语句末尾带分号，所以先实现了oracle
* 不支持oracle 的`Q'/test/'`语法
* 最后一行必须有结尾符，比如select语句最后必须有分号不能省略
## 使用
```
InputSteam inputSteam = "获取Stream方法"
List<String> sqlList = new OracleSqlSplit(inputStream).getSqlList();
```