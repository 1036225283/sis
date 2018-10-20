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

                    String[] objects = null;
                    if (!"".equals(strParamFromMysql)) {
                        String[] strParams = strParamFromMysql.split(",");
                        objects = new String[strParams.length];
                        if (strParams.length != 0) {
                            for (int i = 0; i < strParams.length; i++) {
                                String strParam = strParams[i];
                                if ("".equals(strParam)) {
                                    continue;
                                }
                                if (!map.containsKey(strParam)) {
                                    return Return.Error(strParam + " not exist");
                                }
                                objects[i] = map.get(strParam).toString();
                            }
                        }
                    }


                    Map<String, Object> mapResult = new HashMap<String, Object>();


                    if (strResultType.equals("list")) {
                        List<Map<String, Object>> list = DataSource.getDataSource().getList(strAction, objects);
                        mapResult.put("strAction", strAction);
                        mapResult.put("strData", list);
                        return new Return(0, "success", mapResult);
                    } else if (strResultType.equals("map")) {
                        Map<String, Object> mapSqlResult = DataSource.getDataSource().getMap(strAction, objects);
                        mapResult.put("strAction", strAction);
                        mapResult.put("strData", mapSqlResult);
                        return new Return(0, "success", mapResult);
                    } else if (strResultType.equals("int")) {
                        int ok = DataSource.getDataSource().update(strAction, objects);
                        mapResult.put("strAction", strAction);
                        mapResult.put("strData", ok);
                        return new Return(0, "success", mapResult);
                    }

                } catch (Exception e) {
                    return Return.Error(e.getMessage());
                }
            }
        }
        return null;
    }
}
