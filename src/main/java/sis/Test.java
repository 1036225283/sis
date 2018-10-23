package sis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试代码
 * Created by root on 2018/10/12.
 */
public class Test {

    static {
        HandlerManager.instance.regisger("last", new Handler() {
            public Return handler(Map<String, Object> map) {
                return Return.Error("this is last");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        testSqlDataSource();
//        testHandlerManager();
//        testClientHandler();
//        testSqlManager();
    }

    public static void testSqlDataSource() throws Exception {
//        List<Map<String, Object>> list = DataSource.getDataSource().getList("getUser", );
//        System.out.println(list.size());
        Map<String, Object> map = DataSource.getDataSource().getMap("getUser", 32232323);
        System.out.println(map);
    }


    public static void testHandlerManager() {
        HandlerManager.instance.regisger("test", new Handler() {
            public Return handler(Map<String, Object> map) {
                return Return.Error("this is test");
            }
        });

        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "last");
        reqStock.put("strCode", "002222");
        Return ret = HandlerManager.instance.handler(reqStock);
        System.out.println(ret.getMsg());
    }


    public static void testClientHandler() {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "listTaskUser");
        Return ret = HandlerClient.instance.handler(reqStock);
        System.out.println(ret.getList());
    }


    public static void testSqlManager() {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "limitStockExtend");
        reqStock.put("strFlag", "nDayInfoFlag");
        reqStock.put("strValue", 0);
        reqStock.put("limit", 30);
        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret.getCode() != 0) {
            System.out.println(ret.getMsg());
        }

        //拼装编码，然后从网易查询数据
        List<Map<String, Object>> list = ret.getList();
        System.out.println(list);
    }


}
