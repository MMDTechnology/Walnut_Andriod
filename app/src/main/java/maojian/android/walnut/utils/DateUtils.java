package maojian.android.walnut.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hezuzhi
 * @Description: ()
 * @date 2017/3/2  10:20.
 * @version: 1.0
 */
public class DateUtils {
    /**
     * 处理时间 秒、分钟、小时、天
     *
     * @param date 消息时间
     */
    public static String dateProcess1(long date) {

        String formatStr = "yyyyMMdd-HH:mm:ss";

        // 消息时间
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date sendDate = null;
        try {
//			sendDate = format.parse(date);
            sendDate = new Date(date);//Long.parseLong(
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 当前时间
        Date curDate = new Date(System.currentTimeMillis());

        // 分钟间隔
        int minDiff = (int) ((curDate.getTime() - sendDate.getTime()) / 1000);
        String dateStr = "";

        // 60秒内
        if (minDiff < 60) {
            return "1 minute age";//minDiff + "秒前";
        }

        // 60分钟内
        if (minDiff < 3600) {
            return minDiff / 60 + " minutes age";
        }

        // 1到2小时内
        if (minDiff < 3600 * 2) {
            return "1 hour age";
        }

        // N天内
        int num = (int) ((curDate.getTime() - sendDate.getTime()) / 1000 / 3600 / 24);
        switch (num) {
            case 0:
//                formatStr = "HH:mm";
//                format.applyPattern(formatStr);
//                dateStr = curDate.getDate() != sendDate.getDate() ? "昨天 "
//                        + format.format(sendDate) : format.format(sendDate);
                return minDiff / 3600 + " hours age";
//                break;
            case 1:
//                if (curDate.getDate() - sendDate.getDate() < 2) {
//                    formatStr = "HH:mm";
//                    format.applyPattern(formatStr);
//                    dateStr = "昨天 " + format.format(sendDate);
//                } else {
//                    formatStr = "MM-dd";
//                    format.applyPattern(formatStr);
//                    dateStr = format.format(sendDate);
//                }
//                break;
                return "Yesterday";

            case 1 - 30:
                return num + " day(s) age";

            default:
                formatStr = "yyyy/MM/dd";
                format.applyPattern(formatStr);
                dateStr = format.format(sendDate);
                break;
        }
        return dateStr;
    }

    //处理时间差，返回天
    public static int getdatelong(String date) {

        String formatStr = "yyyyMMdd-HH:mm:ss";

        // 消息时间
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date sendDate = null;
        try {
//			sendDate = format.parse(date);
            sendDate = new Date(Long.parseLong(date));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 当前时间
        Date curDate = new Date(System.currentTimeMillis());

        // N天内
        return (int) ((curDate.getTime() - sendDate.getTime()) / 1000 / 3600 / 24);
    }
}
