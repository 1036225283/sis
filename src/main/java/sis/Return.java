package sis;

import java.util.List;
import java.util.Map;

/**
 * 返回结果
 * Created by root on 2018/8/25.
 */
public class Return {
    private int code;
    private String msg;
    private Map<String, Object> data;


    public Return(int code) {
        this.code = code;
    }

    public Return(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Return(int code, String msg, Map<String, Object> data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Return Error() {
        return new Return(-1);
    }

    public static Return Error(String msg) {
        return new Return(-1, msg);
    }


    public Map<String, Object> getMap() {
        return (Map<String, Object>) data.get("strData");
    }

    public List<Map<String, Object>> getList() {
        return (List<Map<String, Object>>) data.get("strData");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
