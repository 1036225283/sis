package sis;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * java业务处理器：处理java程序服务
 * Created by root on 2018/8/22.
 */
public class HandlerManager {


    public static HandlerManager instance = new HandlerManager();
    private Logger logger;

    //strAction -> handler
    private Map<String, Handler> map = new HashMap<String, Handler>();

    public void regisger(String key, Handler handler) {
        if (map.containsKey(key)) {
            throw new RuntimeException("已经注册过了");
        }
        map.put(key, handler);
    }


    public Return handler(Map<String, Object> map) {

        if (map.containsKey("strAction")) {
            String strAction = map.get("strAction").toString();
            Handler handler = this.map.get(strAction);
            if (handler != null) {
                return handler.handler(map);
            } else {
                return Return.Error("服务找不到");
            }
        }
        return Return.Error("系统错误");
    }


    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
