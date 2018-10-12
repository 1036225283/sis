# sis
sql is service 简称sis
>简要说明
1.无论是hibernate还是mybatis，使用起来都不是很方便，在当今微服务横行的情况下，我们需要更方便更快捷的构建服务
2.还在为读写分离，一主多从负载均衡而发愁，还在为了分库分表而头疼，sis满足你的需求

>内部构成
###DBHelper
    负责读取tbDataSource和tbSql的数据
    
 ##UtilSql
    sql执行的辅助工具类
 ##DataSource
    负责加载sql和数据源，并在执行sql时，如果该sql涉及分库分表，会计算并找到正确的库跟表，进行数据操作
    同时提供了3种操作，getList,getMap,update
    getList 获取多条数据，返回结果：List<Map<String, Object>>
    getMap  获取单条数据，返回结果：Map<String, Object>
    update  执行更新和删除操作，返回结果，影响数据的条数
    
    

>初始化工作
需要在数据里面建两张表
```SQL
//记录数据源信息
CREATE TABLE tbDataSource
(
    lId int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    strKey varchar(36) NOT NULL COMMENT '数据源唯一标示',
    strName varchar(128) DEFAULT '' COMMENT '数据源名称',
    strUsername varchar(128) DEFAULT '' COMMENT '用户名',
    strPassword varchar(128) DEFAULT '' COMMENT '密码',
    strUrl varchar(128) DEFAULT '' COMMENT 'JDBC连接串,一主多从时,多个数据源用逗号分隔',
    strDriverClassName varchar(128) DEFAULT '' COMMENT '驱动名称',
    strConnectionProperties varchar(128) DEFAULT '' COMMENT '连接属性,格式[参数名=参数值]',
    nIsolation int(4) DEFAULT '2' COMMENT '事务隔离级别',
    nInitialSize int(8) DEFAULT '5' COMMENT '初始化的连接数',
    nMaxTotal int(8) DEFAULT '10' COMMENT '最大的连接数',
    nMaxIdle int(8) DEFAULT '10' COMMENT '最大空闲连接数',
    nMinIdle int(8) DEFAULT '10' COMMENT '最小空闲连接数',
    nMaxWaitMillis int(8) DEFAULT '10' COMMENT '从连接池获取一个连接时，最大的等待时间',
    dtModifyTime datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    dtCreateTime datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    strDataGroup varchar(64) DEFAULT '1' COMMENT '分库分表配置，五个库时值为5，1个库时值为1',
    nState int(4) DEFAULT '1' COMMENT '1:生效,0:失效'
);
CREATE UNIQUE INDEX tbDataSource_strKey_index ON tbDataSource (strKey);

//记录sql
CREATE TABLE tbSql
(
    lId INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    strKey VARCHAR(36) NOT NULL COMMENT '编码',
    strName VARCHAR(128) DEFAULT '' COMMENT '名称',
    strSql VARCHAR(1024) DEFAULT '' COMMENT 'sql',
    strParam VARCHAR(128) DEFAULT '' COMMENT '参数',
    dtModifyTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    dtCreateTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    strTable VARCHAR(100) DEFAULT '' COMMENT '表名:属于哪个表的sql',
    strResultType VARCHAR(32) DEFAULT '' COMMENT '返回值类型',
    strDataSource VARCHAR(100) DEFAULT '' COMMENT '数据源',
    strDataGroup VARCHAR(64) DEFAULT '' COMMENT '分库分表规则'
);
CREATE UNIQUE INDEX tbSql_strKey_uindex ON tbSql (strKey);
```

>举个例子说明一般的使用情形

tbSql.strKey:           getBorrowerByUserId
tbSql.strSql:           SELECT * FROM tbBorrower WHERE lUserId = {lUserId};
tbSql.strParam:         lUserId
tbSql.strResultType:    map
tbSql.strDataSource:    borrowerDataSource,对应tbDataSource.strKey
tbSql.strDataGroup:     5:100:50000:lUserId,意思是5库100表，每个表存放50000个用户的数据，根据lUserId来进行分库分表，如果单库单表则为""


>Java中如何调用
```JAVA
        DBHelper dbHelper = new DBHelper();

        String url = "jdbc:mysql://localhost:3306/test?zeroDateTimeBehavior=convertToNull&Unicode=true&amp;characterEncoding=utf8";
        String user = "root";
        String password = "root";
        dbHelper.setUser(user);
        dbHelper.setPassword(password);
        dbHelper.setDriverClassName("com.mysql.jdbc.Driver");
        dbHelper.setUrl(url);


        DataSource dataSource = new DataSource();
        dataSource.switchDbHelper(dbHelper);
        dataSource.init();
        
        //如果调用没有参数的sql
        List<Map<String, Object>> list = dataSource.getList("listUser");
        Map<String, Object> map = dataSource.getMap("getUser");
        int count = dataSource.update("updateUser");
        
        //如果有参数呢
        List<Map<String, Object>> list = dataSource.getList("listUser", nAge, strMobile);
        Map<String, Object> map = dataSource.getMap("getUser", "5898823");
        int count = dataSource.update("updateUser", lUserId, nAge);

```

>是不是特别简单