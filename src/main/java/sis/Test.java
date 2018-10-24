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
//        testBug();
        testSqlDataSource();
//        testHandlerManager();
//        testClientHandler();
//        testSqlManager();
//        createSql();
    }

    public static void testSqlDataSource() throws Exception {
//        List<Map<String, Object>> list = DataSource.getDataSource().getList("getUser", );
//        System.out.println(list.size());
        Map<String, Object> map = DataSource.getDataSource().getMap("getUser", 64000000);
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

    public static void testBug() throws Exception {

        String strData = "000025,2018-10-23,26.9,26.86,27.58,26.5,27.16,-0.011046,3404847,9157";
        int ok = DataSource.getDataSource().update("batchInsertStockDayFrom126", strData);


    }

    public static void createSql() {
        int nDb = 3;
        int nTable = 100;
        int nStep = 1000000;
        int nStart = 0;

        String strSql = "CREATE TABLE tbUser{nTableIndex}\n" +
                "(\n" +
                "    lId INT(11) PRIMARY KEY NOT NULL COMMENT '主键' AUTO_INCREMENT,\n" +
                "    strUsername VARCHAR(30),\n" +
                "    nAge INT(6) COMMENT '年龄',\n" +
                "    nSex INT(4) DEFAULT '3' COMMENT '1男2女3未知',\n" +
                "    strMsg VARCHAR(150)\n" +
                ") AUTO_INCREMENT={nStart} DEFAULT CHARSET=utf8;";

        for (int i = 0; i < nDb; i++) {
            for (int j = i * nTable; j < (i + 1) * nTable; j++) {
                nStart = j * nStep;
                String strSqlNew = strSql.replace("{nDbIndex}", i + "").replace("{nTableIndex}", j + "").replace("{nStart}", nStart + "");
                System.out.println(strSqlNew);
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        }


    }


}
