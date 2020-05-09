package sis;

import java.util.List;
import java.util.Map;

/**
 * 返回结果
 * Created by root on 2018/8/25.
 */
public class Return {
    private int code = 0;
    private String msg;
    private String action;
    private String clientId;
    private Map<String, Object> data;


    public Return(String strAction, String strClientId) {
        action = strAction;
        clientId = strClientId;
    }

    public Return(String strAction, String strClientId, int nCode) {
        action = strAction;
        clientId = strClientId;
        code = nCode;
    }

    public Return(String strAction, String strClientId, int nCode, String strMsg) {
        action = strAction;
        clientId = strClientId;
        code = nCode;
        msg = strMsg;
    }

    public Return(String strAction, String strClientId, int nCode, String strMsg, Map<String, Object> mapData) {
        action = strAction;
        clientId = strClientId;
        code = nCode;
        msg = strMsg;
        data = mapData;
    }


    public static Return OK(String strAction, String strClientId) {
        return new Return(strAction, strClientId);
    }

    public static Return OK(String strAction, String strClientId, String strMsg) {
        return new Return(strAction, strClientId, 0, strMsg);
    }


    public static Return OK(String strAction, String strClientId, String strMsg, Map<String, Object> data) {
        return new Return(strAction, strClientId, 0, strMsg, data);
    }


    public static Return Error(String strAction, String strClientId) {
        return new Return(strAction, strClientId, -1);
    }

    public static Return Error(String strAction, String strClientId, String strMsg) {
        return new Return(strAction, strClientId, 0, strMsg);
    }

    public static Return Error(String strAction, String strClientId, String strMsg, Map<String, Object> data) {
        return new Return(strAction, strClientId, 0, strMsg, data);
    }


    public int count() {
        return Integer.parseInt(data.get("strData").toString());
    }

    public Map<String, Object> map() {
        return (Map<String, Object>) data.get("strData");
    }

    public List<Map<String, Object>> list() {
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
