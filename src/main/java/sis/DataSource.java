package sis;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据操作
 */
public class DataSource {

    private DBHelper dbHelper = null;


    static DataSource dataSource;


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
        List<Map<String, Object>> list = UtilSql.getListSelf("SELECT * FROM tbDataSource;", dbHelper.getConnection());
        for (Map<String, Object> map : list) {

            List<BasicDataSource> listBasicDataSource = new ArrayList<BasicDataSource>();

            String strKey = map.get("strKey").toString();
            String strDataGroup = map.get("strDataGroup").toString();
            int nDataGroup = Integer.valueOf(strDataGroup);

            String strConnectionProperties = map.get("strConnectionProperties").toString();
            String nIsolation = map.get("nIsolation").toString();

            //一主多从时多个数据源的加载
            String strUrls = map.get("strUrl").toString();
            String[] arrUrl = strUrls.split(",");
            for (String strUrl : arrUrl) {

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
                properties.setProperty("timeBetweenEvictionRunsMillis", "10000");
                properties.setProperty("numTestsPerEvictionRun", "3");
                properties.setProperty("removeAbandonedOnBorrow", "true");
                properties.setProperty("removeAbandonedOnMaintenance", "true");
                properties.setProperty("removeAbandonedTimeout", "300");
                properties.setProperty("validationQuery", "SELECT 1");
                properties.setProperty("testOnReturn", "false");
                properties.setProperty("testOnBorrow", "true");
                //分库分表时，多个数据源的处理 ,nDataGroup==1表示只有一个库
                if (nDataGroup == 1) {
                    BasicDataSource basicDataSource = BasicDataSourceFactory.createDataSource(properties);
                    listBasicDataSource.add(basicDataSource);
                } else {

                    for (int i = 0; i < nDataGroup; i++) {
                        //对strUrl进行处理
                        String strPrefix = strUrl.substring(0, strUrl.indexOf("."));
                        String strSuffix = strUrl.substring(strUrl.indexOf("."), strUrl.length());
                        String strRealUrl = strPrefix + i + strSuffix;
                        properties.setProperty("url", strRealUrl);
                        BasicDataSource basicDataSource = BasicDataSourceFactory.createDataSource(properties);
                        listBasicDataSource.add(basicDataSource);
                    }
                }


            }

            mapDataSource.put(strKey, listBasicDataSource);
            mapDataSourceIndex.put(strKey, new AtomicInteger(0));
        }
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
    private String getSqlFromArray(Map<String, Object> map, String... strParams) {
        String sql = map.get("strSql").toString();
        String params = map.get("strParam").toString();
        String[] arrParam = params.split(",");
        for (int i = 0; i < arrParam.length; i++) {
            if (strParams == null) {
                return sql;
            }
            sql = sql.replace("{" + arrParam[i] + "}", strParams[i]);
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
    public List<Map<String, Object>> getList(String strServiceName, String... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromArray(map, arrParam);

        if ("".equals(strDataGroup)) {
            return UtilSql.getList(sql, getConnection(strDataSource));
        }

        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromArray(strDataGroup, strTable, sql, arrParam);
        strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
        return UtilSql.getList(sql, getConnection(strDataSource));

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
    public Map<String, Object> getMap(String strServiceName, String... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromArray(map, arrParam);

        if ("".equals(strDataGroup)) {
            return UtilSql.getUnique(sql, getConnection(strDataSource));
        }

        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromArray(strDataGroup, strTable, sql, arrParam);
        strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
        return UtilSql.getUnique(sql, getConnection(strDataSource));
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
    public int update(String strServiceName, String... arrParam) throws Exception {
        Map<String, Object> map = mapSql.get(strServiceName);
        String strDataSource = map.get("strDataSource").toString();
        String strDataGroup = map.get("strDataGroup").toString();
        String sql = getSqlFromArray(map, arrParam);
        System.out.println(sql);

        if ("".equals(strDataGroup)) {
            return UtilSql.executeUpdate(sql, getConnection(strDataSource));
        }

        String strTable = map.get("strTable").toString();
        sql = getDataGroupSqlFromArray(strDataGroup, strTable, sql, arrParam);
        strDataSource = getDataGroupDataSourceKeyFromArray(strDataSource, strDataGroup, arrParam);
        return UtilSql.executeUpdate(sql, getConnection(strDataSource));

    }


    //获取所有的服务
    public Map<String, Map<String, Object>> getServices() {
        return mapSql;
    }

    //一主多从时多个数据源连接的获取，采用轮询策略
    public Connection getConnection(String strServiceName) throws Exception {

        List<BasicDataSource> list = mapDataSource.get(strServiceName);

        AtomicInteger index = mapDataSourceIndex.get(strServiceName);

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


    private String getDataGroupSqlFromArray(String strDataGroup, String strTable, String sql, String... arrParam) {
        //strDataGroup = 5:100:50000:lUserId 意思是5库100表每
        String[] arrDataGroup = strDataGroup.split(":");
        int nCapacity = Integer.valueOf(arrDataGroup[2]);

        String strKey = arrParam[0];
        int nKey = Integer.valueOf(strKey);

        int nTableIndex = nKey / nCapacity;
        sql = sql.replace(strTable, strTable + nTableIndex);
        return sql;

    }

    private String getDataGroupDataSourceKeyFromArray(String strDataSource, String strDataGroup, String... arrParam) {

        String[] arrDataGroup = strDataGroup.split(":");
        int nTableCount = Integer.valueOf(arrDataGroup[1]);
        int nCapacity = Integer.valueOf(arrDataGroup[2]);
        String strKey = arrParam[0];
        int nKey = Integer.valueOf(strKey);
        int nTableIndex = nKey / nCapacity;
        int nDbIndex = nTableIndex / nTableCount;
        return strDataSource + nDbIndex;
    }


    public static void setDataSource(DataSource dataSourceInt) {
        dataSource = dataSourceInt;
    }
}
