package sis;

import java.util.Map;

/**
 * 接口一枚
 * Created by root on 2018/8/21.
 */
public interface Handler {

    public Return handler(Map<String, Object> map);
}
