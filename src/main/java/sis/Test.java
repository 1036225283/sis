package sis;

import java.util.List;
import java.util.Map;

/**
 * 测试代码
 * Created by root on 2018/10/12.
 */
public class Test {


    public static void main(String[] args) throws Exception {

        DBHelper dbHelper = new DBHelper();

        dbHelper.setUser("root");
        dbHelper.setPassword("root");
        dbHelper.setDriverClassName("com.mysql.jdbc.Driver");
        dbHelper.setUrl("jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=convertToNull&Unicode=true&amp;characterEncoding=utf8");


        DataSource dataSource = new DataSource();
        dataSource.switchDbHelper(dbHelper);
        dataSource.init();

        List<Map<String, Object>> list = dataSource.getList("getStock");

        System.out.println(list);

    }


}
