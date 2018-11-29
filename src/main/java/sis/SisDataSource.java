package sis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Created by root on 2018/11/27.
 */
public class SisDataSource {

    private static String url;
    private static String user;
    private static String password;

    private static int initSize = 3;// 初始化连接池的连接数
    private static int MaxSize = 10;// 假设数据库可以创建10个连接
    public static int CurrentSize = 0;// 当前连接数

    static {
        url = "jdbc:mysql://localhost:3306/yiki?useUnicode=true&characterEncoding=gb2312";
        user = "root";
        password = "******";
    }

    LinkedList<Connection> conPool = new LinkedList<Connection>();// 创建链表来存储连接

    public SisDataSource() {

        for (int i = 0; i < initSize; i++) {// 连接池可以容纳5个连接
            try {
                this.conPool.addLast(this.createCon());
                SisDataSource.CurrentSize++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public Connection getCon() throws Exception {

        synchronized (conPool) {// 加锁，保证多个线程不会难道同一个链接
            if (this.conPool.size() > 0) {// 看看还有没有
                return this.conPool.removeFirst();// 就是从连接池里取出来（连接池是链表，移走表头
            } // else表已经没有连接了，就再创建连接
            if (SisDataSource.CurrentSize < MaxSize) {// 如果请求的连接超载了，可在数据库允许的连接上再创建连接
                SisDataSource.CurrentSize++;
                return this.createCon();
            }

            throw new SQLException("连接池已没有链接");
        }

    }

    public void free(Connection con) {// 释放连接就是把连接放回连接池
        this.conPool.addLast(con);
    }

    private Connection createCon() throws SQLException {// 创建连接
        Connection realconn = DriverManager.getConnection(url, user, password);
        SisConnectionHandler proxy = new SisConnectionHandler(this);
        return proxy.bind(realconn);


    }

}
