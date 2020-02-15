package sis;


import java.util.Map;
import java.util.logging.Logger;

/**
 * 纯客户端
 * Created by root on 2018/8/22.
 */
public class HandlerClient implements Handler {


    public static HandlerClient instance = new HandlerClient();
    private Logger logger;

    public Return handler(Map<String, Object> map) {

        //先查询SqlManager
        Return ret = SqlManager.instance.handler(map);
        if (ret != null) {
            return ret;
        }

        //再查HandlerClient
        ret = HandlerManager.instance.handler(map);
        if (ret != null) {
            return ret;
        }

        //最后查远程服务
        ret = ServiceManager.instance.handler(map);


        if (ret != null) {
            return ret;
        } else {
            return Return.Error("service not found");
        }

    }

    public SqlManager SqlManager() {
        return SqlManager.instance;
    }

    public HandlerManager HandlerManager() {
        return HandlerManager.instance;
    }

    public ServiceManager ServiceManager() {
        return ServiceManager.instance;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
