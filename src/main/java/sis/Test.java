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
//        testSqlDataSource();
//        testHandlerManager();
//        testClientHandler();
//        testSqlManager();
//        createUserSql();
//        createUserLoginSql();
//        testInsertUserLoginHistory();//插入用户登录记录
//        testUpdateUserLoginHistory();//更新用户登录记录
//        test();

//        testCountUser();
//        testListUser();
//        testUser();
        Map<String, Object> map = DataSource.getDataSource().getMap("countStockTrainData", "000001", "2020-05-20", 6, 1);
        System.out.println(map);
    }

    public static void testCountUser() {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "countUser");

        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret.getCode() != 0) {
            System.out.println(ret.getMsg());
        }

        System.out.println(ret.count());
    }

    public static void testListUser() {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "listUser");

        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret.getCode() != 0) {
            System.out.println(ret.getMsg());
        }

        System.out.println(ret.list());
    }

    public static void testUser() {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "getUserByUserId");
        reqStock.put("lUserId", 1);

        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret.getCode() != 0) {
            System.out.println(ret.getMsg());
        }

        System.out.println(ret.map());
    }

    public static void testReturn() {
        Return ret = new Return(12);
    }


    /**
     * 1.创建表结构
     * 2.创建数据源
     *
     * @throws Exception
     */

//    测试 数据的增，删，改，查


    //测试分库分表后数据的增删改查
    public static void testInsertUserLoginHistory() throws Exception {

        Map<String, Object> reqStock = new HashMap<String, Object>();
        int j = 1;
        for (int i = 100000; i < 100010; i++) {
            reqStock.put("strAction", "insertUserLoginHistory");
            reqStock.put("lUserId", i);
            reqStock.put("ip", "192.168.1." + j);
            j++;

            Return ret = HandlerClient.instance.handler(reqStock);
            if (ret.getCode() != 0) {
                System.out.println(ret.getMsg());
            }

            System.out.println(ret.count());
        }


    }

    public static void testUpdateUserLoginHistory() throws Exception {
        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "updateUserLoginHistory");
        reqStock.put("lUserId", 100009);
        reqStock.put("lId", 1000000010);
        reqStock.put("ip", "192.168.1.32");

        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret.getCode() != 0) {
            System.out.println(ret.getMsg());
        }

        System.out.println(ret.count());
    }

    public static void testDeleteUserLoginHistory() throws Exception {
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
        List<Map<String, Object>> list = ret.list();
        System.out.println(list);
    }

    public static void testSelectUserLoginHistory() throws Exception {
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
        List<Map<String, Object>> list = ret.list();
        System.out.println(list);
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
        reqStock.put("strAction", "loadTasks");

        System.out.println("before modify ... ");
        Map<String, Object> sqlMap = HandlerClient.instance.SqlManager().sqlMap("loadTasks");
        System.out.println(sqlMap);
        System.out.println(HandlerClient.instance.SqlManager().sql("loadTasks"));

        System.out.println("after modify ... ");
        HandlerClient.instance.SqlManager().modify("loadTasks", "SELECT * FROM tbTask", null);

        Return ret = HandlerClient.instance.handler(reqStock);
        if (ret != null && ret.getCode() != 0) {
            System.out.println(ret.list());
        }
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
        List<Map<String, Object>> list = ret.list();
        System.out.println(list);
    }

    public static void testBug() throws Exception {

        String strData = "000025,2018-10-23,26.9,26.86,27.58,26.5,27.16,-0.011046,3404847,9157";
        int ok = DataSource.getDataSource().update("batchInsertStockDayFrom126", strData);


    }

    //用户表
    public static void createUserSql() {
        int nDb = 3;
        int nTable = 100;
        int nStep = 1000000;//表自增步长
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
                nStart = j * nStep;//表自增起始点
                String strSqlNew = strSql.replace("{nDbIndex}", i + "").replace("{nTableIndex}", j + "").replace("{nStart}", nStart + "");
                System.out.println(strSqlNew);
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        }


    }


    //

    /**
     * 用户登录记录
     * 根据用户id来分，假定每个用户登录记录100条，那么一个表容纳的用户数 = 1000000/100 = 10000
     */
    public static void createUserLoginSql() {
        int nDb = 3;
        int nTable = 100;
        long nStep = 100000000;//表自增步长
        long nStart = 0;

        String strSql = "CREATE TABLE tbUserLoginHistory{nTableIndex}\n" +
                "(\n" +
                "    lId BIGINT(20) UNSIGNED PRIMARY KEY NOT NULL COMMENT '主键' AUTO_INCREMENT,\n" +
                "    lUserId BIGINT(20) UNSIGNED NOT NULL COMMENT '用户id',\n" +
                "    ip VARCHAR(30) COMMENT 'ip地址',\n" +
                "    dtCreateTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                "    dtModifyTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'\n" +
                ")COMMENT='用户登录历史表' default charset=utf8 COLLATE='utf8_bin' ENGINE=InnoDB AUTO_INCREMENT={nStart};\n" +
                "\n";

        for (int i = 0; i < nDb; i++) {
            for (int j = i * nTable; j < (i + 1) * nTable; j++) {
                nStart = j * nStep;//表自增起始点
                String strSqlNew = strSql.replace("{nDbIndex}", i + "").replace("{nTableIndex}", j + "").replace("{nStart}", nStart + "");
                System.out.println(strSqlNew);
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        }


    }

    public static void test() {
        Map<String, Object> mapReq = new HashMap();

        //先删数据
        mapReq.put("strAction", "test");
        mapReq.put("strName", "大乔");
        mapReq.put("strTitle", "呵呵");

        Return ret = HandlerClient.instance.handler(mapReq);
        if (ret.getCode() != 0) {
            System.out.println("出错了: " + ret.getMsg());
        } else {
            System.out.println("更新" + 000000 + "题材成功");
        }
    }


}
