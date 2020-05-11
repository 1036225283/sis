package sis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sql管理器：负责调用sql服务
 * Created by root on 2018/10/20.
 */
public class SqlManager implements Handler {

    public static SqlManager instance = new SqlManager();

    private static Map<String, Map<String, Object>> services = DataSource.getDataSource().getServices();

    public Return handler(Map<String, Object> map) {
        if (map.containsKey("strAction")) {
            String strAction = map.get("strAction").toString();

            //先在sql映射中查询，如果存在
            if (services.containsKey(strAction)) {
                try {
                    Map<String, Object> service = services.get(strAction);
                    String strResultType = service.get("strResultType").toString();
                    //判断参数是否存在
                    String strParamFromMysql = service.get("strParam").toString();

                    Object[] objects = null;
                    if (!"".equals(strParamFromMysql)) {
                        String[] strParams = strParamFromMysql.split(",");
                        objects = new Object[strParams.length];
                        if (strParams.length != 0) {
                            for (int i = 0; i < strParams.length; i++) {
                                String strParam = strParams[i];
                                if ("".equals(strParam)) {
                                    continue;
                                }
                                if (!map.containsKey(strParam)) {
                                    return Return.Error(strParam + " not exist");
                                }
                                objects[i] = map.get(strParam);
                            }
                        }
                    }


                    if (strResultType.equals("list")) {
                        List<Map<String, Object>> list = DataSource.getDataSource().getList(strAction, objects);
                        return Return.OK().action(strAction).data(list);
                    } else if (strResultType.equals("map")) {
                        Map<String, Object> mapSqlResult = DataSource.getDataSource().getMap(strAction, objects);
                        return Return.OK().action(strAction).data(mapSqlResult);
                    } else if (strResultType.equals("int")) {
                        int ok = DataSource.getDataSource().update(strAction, objects);
                        return Return.OK().action(strAction).data(ok);
                    } else {
                        throw new RuntimeException("sql 的返回结果没有定义：list,map,int");
                    }


                } catch (Exception e) {
                    return Return.Error(e.getMessage());
                }
            }
        }
        return null;
    }

    public boolean modify(String strAction, String sql, String param) {
        Map<String, Object> sqlMap = sqlMap(strAction);
        if (sqlMap == null) {
            return false;
        }

        sqlMap.put("strSql", sql);

        if (param == null) {
            sqlMap.put("strParam", "");
        } else {
            sqlMap.put("strParam", param);
        }

        return true;
    }

    public Map<String, Object> sqlMap(String strAction) {
        return services.get(strAction);
    }


    public String sql(String strAction) {
        Map<String, Object> sqlMap = sqlMap(strAction);
        if (sqlMap == null) {
            return null;
        }

        if (sqlMap.containsKey("strSql")) {
            return sqlMap.get("strSql").toString();
        }

        return null;
    }
}
