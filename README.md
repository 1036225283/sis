# sis
sql is service

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

