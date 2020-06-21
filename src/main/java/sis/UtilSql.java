package sis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilSql {


    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    /**
     * 获取map，填充key
     *
     * @param resultSet
     * @return
     */
    private static Map<String, Object> getMap(ResultSet resultSet) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            count = count + 1;
            for (int i = 1; i < count; i++) {
                // System.out.println(resultSetMetaData.getColumnName(i));
                // System.out.println(resultSetMetaData.getColumnClassName(i));
                // System.out.println(resultSetMetaData.getCatalogName(i));
                // System.out.println(resultSetMetaData.getColumnLabel(i));
                String label = resultSetMetaData.getColumnLabel(i);
                String name = resultSetMetaData.getColumnName(i);
                if (label != null) {
                    map.put(label, null);
                } else {
                    map.put(name, null);
                }

            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 传入map，填入value
     *
     * @param map
     * @param resultSet
     */
    private static void setMap(Map<String, Object> map, ResultSet resultSet) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                map.put(entry.getKey(), resultSet.getObject(entry.getKey()));
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * resultSet转换为map
     *
     * @param resultSet
     * @return
     */
    public static Map<String, Object> resultSetToMap(ResultSet resultSet) {
        Map<String, Object> map = getMap(resultSet);
        setMap(map, resultSet);
        return map;
    }

    /**
     * 获取表的所有字段
     *
     * @param table
     * @param connection
     * @return
     */
    public static Map<String, Object> getMapByTable(String table, Connection connection) {
        Map<String, Object> map = null;
        try {
            ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, null);
            map = getMap(resultSet);
            // System.out.println(map);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return map;
    }


    //自身数据的加载，connection不释放
    public synchronized static List<Map<String, Object>> getListSelf(String sql, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        System.out.println("self.getList : " + sql);
        ResultSet resultSet = statement.executeQuery(sql);// 执行语句，得到结果集
        return getList(resultSet);
    }

    /**
     * 获取sql查询结果
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    public synchronized static List<Map<String, Object>> getList(String sql, Connection connection) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            System.out.println("getList : " + sql);
            long start = System.nanoTime();
            ResultSet resultSet = statement.executeQuery(sql);// 执行语句，得到结果集
            long end = System.nanoTime();
            System.out.println("毫秒：" + (end - start) / 1000 / 1000 + "，微秒：" + (end - start) / 1000);
            return getList(resultSet);
        } finally {
            connection.close();
        }

    }

    public static Map<String, Object> getUnique(String sql, Connection connection) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            System.out.println("getMap : " + sql);
            ResultSet resultSet = statement.executeQuery(sql);// 执行语句，得到结果集
            return getUnique(resultSet);
        } finally {
            connection.close();
        }
    }

    public static int execute(String sql, Connection connection) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(sql);// 执行语句，得到结果集
            return result;
        } finally {
            connection.close();
        }

    }

    public static int executeUpdate(String sql, Connection connection) throws SQLException {

        try {
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);// 执行语句，得到结果集
        } finally {
            connection.close();
        }
    }

    public static boolean setAutoCommit(boolean value) throws SQLException {

        // if (statement == null) {
        // statement = connection.createStatement();
        // }
        // boolean result = statement.execute(sql);// 执行语句，得到结果集
        // return result;
        return false;
    }

    // conn.setAutoCommit(false);

    // 安全传参
    public static void execute(String sql, Connection connection, List<String> params) throws SQLException {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i, params.get(i));
            }
        } finally {
            connection.close();
        }

        // statement.ex
    }

    // 分装查询结果
    private static List<Map<String, Object>> getList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (resultSet.next()) {
            Map<String, Object> map = UtilSql.resultSetToMap(resultSet);
            list.add(map);
        }
        return list;
    }

    // 获取唯一数据集
    private static Map<String, Object> getUnique(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> list = getList(resultSet);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    //进行预编译的处理
    public static List<Map<String, Object>> getList(String sql, Connection connection, String[] params, Object[] values) throws SQLException {
        try {

            for (int i = 0; i < params.length; i++) {
                sql = sql.replace("{" + params[i] + "}", "?").replace("'?'", "?");
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
                }
            }

            System.out.println("getList : " + statement.toString());
            ResultSet resultSet = statement.executeQuery();// 执行语句，得到结果集
            return getList(resultSet);
        } finally {
            connection.close();
        }

    }

    //进行预编译的处理
    public static Map<String, Object> getUnique(String sql, Connection connection, String[] params, Object[] values) throws SQLException {
        try {

            for (int i = 0; i < params.length; i++) {
                sql = sql.replace("{" + params[i] + "}", "?").replace("'?'", "?");
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
                }
            }

            System.out.println("getMap : " + statement.toString());
            ResultSet resultSet = statement.executeQuery();// 执行语句，得到结果集
            return getUnique(resultSet);
        } finally {
            connection.close();
        }
    }


    //进行预编译的处理
    public static int executeUpdate(String sql, Connection connection, String[] params, Object[] values) throws SQLException {

        try {

            for (int i = 0; i < params.length; i++) {
                sql = sql.replace("{" + params[i] + "}", "?").replace("'?'", "?");
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
                }
            }

            System.out.println("getMap : " + statement.toString());
            return statement.executeUpdate();
        } finally {
            connection.close();
        }
    }


    //将map的值组装到sql里面去
    public static int update(String sql, Connection connection, Map<String, Object> map) throws Exception {
        try {
            int i = 0;
            String update = "";
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (i == 0) {
                    i++;
                } else {
                    update = update + ",";
                }
                update = update + entry.getKey() + " = " + entry.getValue();
            }

            sql = sql.replace("{update}", update);

            System.out.println("update : " + sql);

            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);// 执行语句，得到结果集
        } finally {
            connection.close();
        }


    }
}
