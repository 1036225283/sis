package sis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Created by root on 2018/11/27.
 */
public class SisConnectionHandler implements InvocationHandler {


    private Connection realConnection;// sql
    private SisDataSource dataSource;
    private Connection warpedConnection;

    private int maxUseCount = 10;// 最大连接次数
    private int currentUserCount = 0;// 现在连接次数

    SisConnectionHandler(SisDataSource myDataSource) {
        this.dataSource = myDataSource;

    }

    Connection bind(Connection realconn) {

        this.realConnection = realconn;
        this.warpedConnection = (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class}, this);

        return warpedConnection;

    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("close".equals(method.getName())) {
            this.currentUserCount++;
            if (currentUserCount < maxUseCount) {
                this.dataSource.conPool.addLast(this.warpedConnection);
            } else {
                this.realConnection.close();
                SisDataSource.CurrentSize--;
                System.out.println("myDataSource.CurrentSize:" + SisDataSource.CurrentSize);
            }

        }

        return method.invoke(this.realConnection, args);
    }


}
