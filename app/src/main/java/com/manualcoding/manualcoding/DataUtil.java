package com.manualcoding.manualcoding;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoya on 2016/12/1.
 */
public class DataUtil {
    private static DataUtil instance;

    private static Map<String, Object> map;

    private DataUtil(){};
    public static DataUtil getInstance() {
        if(instance == null) {
            instance = new DataUtil();
            map = new HashMap<>();
        }
        return instance;
    }

    public Object getData(String key) {
        return map.get(key);
    }

    public void saveData(String key, Object data) {
        map.put(key, data);
    }

    /** 清理数据 */
    public void clean() {
        map.clear();
    }
}
