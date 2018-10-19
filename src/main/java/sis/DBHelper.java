package sis;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {

    public String url;
    public String strDriverClassName;
    public String user;
    public String password;

    private Connection conn = null;

    public DBHelper() {

    }

    public void close() {
        System.out.println("Close connection...");
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName(strDriverClassName);// 指定连接类型
                conn = DriverManager.getConnection(url, user, password);// 获取连接
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return strDriverClassName;
    }

    public void setDriverClassName(String strDriverClassName) {
        this.strDriverClassName = strDriverClassName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
