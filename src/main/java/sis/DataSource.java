package sis;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据操作
 */
public class DataSource {


    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);
    private DBHelper dbHelper = new DBHelper();


    static DataSource dataSource = new DataSource();

    public DataSource() {
        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static DataSource getDataSource() {
        return dataSource;
    }


    //存放sql语句
    private Map<String, Map<String, Object>> mapSql = new HashMap<String, Map<String, Object>>();
    //存放数据源
    private Map<String, List<BasicDataSource>> mapDataSource = new HashMap<String, List<BasicDataSource>>();
    //存在数据源当前调用下标
    private Map<String, AtomicInteger> mapDataSourceIndex = new ConcurrentHashMap<String, AtomicInteger>();

    //初始化，从数据库中读取sql记录
    public void init() throws Exception {

        //加载数据源
        loadDataSource();

        //加载sql语句
        List<Map<String, Object>> list = UtilSql.getListSelf("SELECT * FROM tbSql;", dbHelper.getConnection());
        for (Map<String, Object> map : list) {
            mapSql.put(map.get("strKey").toString(), map);
        }

        Runtime.getRuntime().addShutdownHook(shutdownThread);

    }

    //初始化，读取数据源。
    private void loadDataSource() throws Exception {

        logger.info("load dataSource is start ...");

        List<Map<String, Object>> list = UtilSql.getListSelf("SELECT * FROM tbDataSource;", dbHelper.getConnection());
        for (Map<String, Object> map : list) {


            String strKey = map.get("strKey").toString();
            String strDataGroup = map.get("strDataGroup").toString();
            int nDataGroup = Integer.valueOf(strDataGroup);

            //一主多从时多个数据源的加载
            String strUrls = map.get("strUrl").toString();
            String[] arrUrl = strUrls.split(",");

            //先判断是否分库分表
            if (nDataGroup == 1) {
                List<BasicDataSource> listBasicDataSource = new ArrayList<BasicDataSource>();
                for (String strUrl : arrUrl) {
                    logger.info(strKey + " : " + strUrl);
                    BasicDataSource basicDataSource = createDataSource(map, strKey, strUrl);
                    listBasicDataSource.add(basicDataSource);
                }
                mapDataSource.put(strKey, listBasicDataSource);
                mapDataSourceIndex.put(strKey, new AtomicInteger(0));
            } else {
                for (int i = 0; i < nDataGroup; i++) {
                    String strKeyNew = strKey + i;
                    List<BasicDataSource> listBasicDataSource = new ArrayList<BasicDataSource>();
                    for (String strUrl : arrUrl) {
                        String strPrefix = strUrl.substring(0, strUrl.indexOf("."));
                        String strSuffix = strUrl.substring(strUrl.indexOf("."), strUrl.length());
                        String strRealUrl = strPrefix + i + strSuffix;
                        logger.info(strKeyNew + " : " + strRealUrl);
                        BasicDataSource basicDataSource = createDataSource(map, strKeyNew, strRealUrl);
                        listBasicDataSource.add(basicDataSource);
                    }
                    mapDataSource.put(strKeyNew, listBasicDataSource);
                    mapDataSourceIndex.put(strKeyNew, new AtomicInteger(0));
                }
            }

        }

        logger.info("load dataSource is end ...");
    }

    Thread shutdownThread = new Thread() {
        public void run() {
            System.out.println("shutdownThread...");
            DataSource.this.destroy();
        }
    };

    //关闭定时器，释放资源
    public void destroy() {
//        dataSourceTimer.stop();
        dbHelper.close();
    }

    //获取sql，根据param map 来构建参数
    private String getSqlFromMap(Map<String, Object> map, Map<String, Object> mapParam) {
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrParam = params.split(",");
        for (String strParam : arrParam) {
            sql = sql.replace("{" + strParam + "}", mapParam.get(strParam).toString());
        }
        return sql;

    }

    //获取sql，根据param array 来构建参数
    private String getSqlFromArray(Map<String, Object> map, Object... strParams) {
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrParam = params.split(",");
        for (int i = 0; i < arrParam.length; i++) {
            if (strParams == null) {
                return sql;
            }
            sql = sql.replace("{" + arrParam[i] + "}", strParams[i].toString());
        }
        return sql;
    }

    //切换dbHelper
    public void switchDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //没有任何条件的查询
    public List<Map<String, Object>> getList(String strServiceName) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String sql = map.get("strSql").toString();
        return UtilSql.getList(sql, getConnection(strDataSource));
    }

    //带参数查询map
    public List<Map<String, Object>> getList(String strServiceName, Map<String, Object> mapParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromMap(map, mapParam);
        //如果没有分库分表
        if ("".equals(strDataGroup)) {
            return UtilSql.getList(sql, getConnection(strDataSource));
        }
        //strDataGroup = 5:100:50000:lUserId 意思是5库100表每
        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromMap(mapParam, strDataGroup, strTable, sql);
        strDataSource = getDataGroupDataSourceKeyFromMap(mapParam, strDataSource, strDataGroup);
        return UtilSql.getList(sql, getConnection(strDataSource));

    }

    //带参数查询array
    public List<Map<String, Object>> getList(String strServiceName, Object... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrKey = params.split(",");

        if (!"".equals(strDataGroup)) {
            strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
            sql = getDataGroupSqlFromArray(map, arrParam);
        }


        return UtilSql.getList(sql, getConnection(strDataSource), arrKey, arrParam);

    }


    //没有任何条件的查询
    public Map<String, Object> getMap(String strServiceName) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String sql = map.get("strSql").toString();
        return UtilSql.getUnique(sql, getConnection(strDataSource));

    }

    //带参数查询map
    public Map<String, Object> getMap(String strServiceName, Map<String, Object> mapParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromMap(map, mapParam);
        if ("".equals(strDataGroup)) {
            return UtilSql.getUnique(sql, getConnection(strDataSource));
        }

        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromMap(mapParam, strDataGroup, strTable, sql);
        strDataSource = getDataGroupDataSourceKeyFromMap(mapParam, strDataSource, strDataGroup);
        return UtilSql.getUnique(sql, getConnection(strDataSource));

    }

    //带参数查询array
    public Map<String, Object> getMap(String strServiceName, Object... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrKey = params.split(",");

        if (!"".equals(strDataGroup)) {
            strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
            sql = getDataGroupSqlFromArray(map, arrParam);
        }


        return UtilSql.getUnique(sql, getConnection(strDataSource), arrKey, arrParam);
    }


    //没有任何条件的更新
    public int update(String strServiceName) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String sql = map.get("strSql").toString();
        return UtilSql.executeUpdate(sql, getConnection(strDataSource));

    }

    //带参数更新map
    public int update(String strServiceName, Map<String, Object> mapParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromMap(map, mapParam);

        if ("".equals(strDataGroup)) {
            return UtilSql.executeUpdate(sql, getConnection(strDataSource));
        }

        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromMap(mapParam, strDataGroup, strTable, sql);
        strDataSource = getDataGroupDataSourceKeyFromMap(mapParam, strDataSource, strDataGroup);
        return UtilSql.executeUpdate(sql, getConnection(strDataSource));

    }

    //带参数更新array
    public int update(String strServiceName, Object... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrKey = params.split(",");


        if (!"".equals(strDataGroup)) {
            strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
            sql = getDataGroupSqlFromArray(map, arrParam);
        }


        return UtilSql.executeUpdate(sql, getConnection(strDataSource), arrKey, arrParam);

    }


    //获取所有的服务
    public Map<String, Map<String, Object>> getServices() {
        return mapSql;
    }

    //一主多从时多个数据源连接的获取，采用轮询策略
    public Connection getConnection(String strServiceName) throws Exception {

        List<BasicDataSource> list = mapDataSource.get(strServiceName);

        if (list == null) {
            throw new RuntimeException("DataSource do not found , strServiceName = " + strServiceName);
        }

        AtomicInteger index = mapDataSourceIndex.get(strServiceName);
        if (index == null) {
            throw new RuntimeException("mapDataSourceIndex is null , strServiceName = " + strServiceName);
        }

        if (index.addAndGet(1) < list.size()) {
            return list.get(index.get()).getConnection();
        } else {
            index.set(0);
            return list.get(0).getConnection();
        }

    }

    //获取分库分表的sql
    private String getDataGroupSqlFromMap(Map<String, Object> mapParam, String strDataGroup, String strTable, String sql) {
        //strDataGroup = 5:100:50000:lUserId 意思是5库100表每
        String[] arrDataGroup = strDataGroup.split(":");
        int nCapacity = Integer.valueOf(arrDataGroup[2]);
        String strTableKey = arrDataGroup[3];

        String strKey = mapParam.get(strTableKey).toString();
        int nKey = Integer.valueOf(strKey);

        int nTableIndex = nKey / nCapacity;
        sql = sql.replace(strTable, strTable + nTableIndex);
        return sql;

    }

    //获取分库分表时的数据源名称
    private String getDataGroupDataSourceKeyFromMap(Map<String, Object> mapParam, String strDataSource, String strDataGroup) {

        String[] arrDataGroup = strDataGroup.split(":");
        int nTableCount = Integer.valueOf(arrDataGroup[1]);
        int nCapacity = Integer.valueOf(arrDataGroup[2]);
        String strTableKey = arrDataGroup[3];
        String strKey = mapParam.get(strTableKey).toString();
        int nKey = Integer.valueOf(strKey);
        int nTableIndex = nKey / nCapacity;
        int nDbIndex = nTableIndex / nTableCount;
        return strDataSource + nDbIndex;
    }


    //根据指定的分片主键，计算表名，替换sql语句
    private String getDataGroupSqlFromArray(Map<String, Object> map, Object... arrParam) {
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String strTable = map.get("strTable").toString();
        String[] arrKey = params.split(",");
        //strDataGroup = 5:100:50000:lUserId 意思是5库100表每


        String[] arrDataGroup = strDataGroup.split(":");
        int nCapacity = Integer.valueOf(arrDataGroup[2]);

        //在这里需要根据参数列表
        int nKeyIndex = findKeyIndex(arrKey, arrDataGroup[3]);
        if (nKeyIndex == -1) {
            throw new RuntimeException("没有找到分库分表的主键：" + map.toString());
        }
        String strKey = arrParam[nKeyIndex].toString();
        int nKey = Integer.valueOf(strKey);

        int nTableIndex = nKey / nCapacity;
        sql = sql.replace(strTable, strTable + nTableIndex);
        return sql;

    }

    private String getDataGroupDataSourceKeyFromArray(String strDataSource, String strDataGroup, Object... arrParam) {

        String[] arrDataGroup = strDataGroup.split(":");
        int nTableCount = Integer.valueOf(arrDataGroup[1]);
        int nCapacity = Integer.valueOf(arrDataGroup[2]);
        Object strKey = arrParam[0];
        int nKey = Integer.valueOf(strKey.toString());
        int nTableIndex = nKey / nCapacity;
        int nDbIndex = nTableIndex / nTableCount;
        return strDataSource + nDbIndex;
    }


    //根据urls创建数据源
    public BasicDataSource createDataSource(Map<String, Object> map, String strKey, String strUrl) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("username", map.get("strUsername").toString());
        properties.setProperty("password", map.get("strPassword").toString());
        properties.setProperty("url", strUrl);
        properties.setProperty("driverClassName", map.get("strDriverClassName").toString());
        properties.setProperty("minIdle", map.get("nMinIdle").toString());
        properties.setProperty("maxIdle", map.get("nMaxIdle").toString());
        properties.setProperty("maxTotal", map.get("nMaxTotal").toString());
        properties.setProperty("initialSize", map.get("nInitialSize").toString());
        properties.setProperty("maxWaitMillis", map.get("nMaxWaitMillis").toString());
        properties.setProperty("testWhileIdle", "true");
        properties.setProperty("timeBetweenEvictionRunsMillis", "300");
        properties.setProperty("numTestsPerEvictionRun", "3");
        properties.setProperty("removeAbandonedOnBorrow", "true");
        properties.setProperty("removeAbandonedOnMaintenance", "true");
        properties.setProperty("removeAbandonedTimeout", "300");
        properties.setProperty("validationQuery", "SELECT 1");
        properties.setProperty("testOnReturn", "false");
        properties.setProperty("testOnBorrow", "true");
        properties.setProperty("idleConnectionTestPeriod", "30");
        //分库分表时，多个数据源的处理 ,nDataGroup==1表示只有一个库
        BasicDataSource basicDataSource = BasicDataSourceFactory.createDataSource(properties);
        return basicDataSource;
    }


    //从传递的参数中寻找分库分表主键的位置
    private int findKeyIndex(String[] arrKey, String strKey) {
        for (int i = 0; i < arrKey.length; i++) {
            if (strKey.equals(arrKey[i])) ;
            return i;
        }
        return -1;
    }

    public static void setDataSource(DataSource dataSourceInt) {
        dataSource = dataSourceInt;
    }

}
