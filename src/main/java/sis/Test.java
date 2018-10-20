package sis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试代码
 * Created by root on 2018/10/12.
 */
public class Test {


    public static void main(String[] args) throws Exception {


        Map<String, Object> reqStock = new HashMap<String, Object>();
        reqStock.put("strAction", "getBaiduCode");
        reqStock.put("strCode", "002222");
        Return ret = HandlerClient.instance.handler(reqStock);

        System.out.println(ret.getMap());


    }

    public void testSqlDataSource() throws Exception {
        List<Map<String, Object>> list = DataSource.getDataSource().getList("limitStockDayByCodeByDate", "002222", "2018-10-10", 11);
        System.out.println(list.size());
        Map<String, Object> map = DataSource.getDataSource().getMap("getBaiduCode", "002222");
        System.out.println(map);
    }


}
