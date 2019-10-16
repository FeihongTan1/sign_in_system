package team.a9043.sign_in_system.util;

import java.util.HashMap;

public class SisScheduleUtil {
    public static final HashMap<Integer, String> dayMap = new HashMap<Integer, String>() {{
        this.put(1, "一");
        this.put(2, "二");
        this.put(3, "三");
        this.put(4, "四");
        this.put(5, "五");
        this.put(6, "六");
        this.put(7, "日");
    }};
    public static final HashMap<Integer, String> fortMap = new HashMap<Integer, String>() {{
        this.put(0, "全");
        this.put(1, "单");
        this.put(2, "双");
    }};
    public static final String timeFormat = "%s [%s-%s] 星期%s 第 %s~%s 节 %s";
}
