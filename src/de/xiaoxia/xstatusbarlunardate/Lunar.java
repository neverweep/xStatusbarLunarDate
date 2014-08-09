/*
 * Copyright (C) 2014 XiaoXia(http://xiaoxia.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// 自定义农历界面

package de.xiaoxia.xstatusbarlunardate;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("SimpleDateFormat")
public class Lunar {

    private Calendar solar;
    private int lunarYear;
    private int lunarMonth;
    private int lunarDay;
    private boolean isLeap;
    private boolean isLeapYear;
    private int solarYear;
    private int solarMonth;
    private int solarDay;
    private int cyclicalYear = 0;
    private int cyclicalMonth = 0;
    private int cyclicalDay = 0;
    private DecimalFormat dfMMDD = new DecimalFormat("00");

    private String lFestivalName;
    private String termString;
    private String clFestivalName;
    private String sFestivalName;
    private String csFestivalName;

    private final static int[] lunarInfo = {
        //2000~2037的农历信息
        0xc960,0xd954,0xd4a0,0xda50,0x7552,0x56a0,0xabb7,0x25d0,0x92d0,0xcab5,
        0xa950,0xb4a0,0xbaa4,0xad50,0x55d9,0x4ba0,0xa5b0,0x5176,0x52bf,0xa930,
        0x7954,0x6aa0,0xad50,0x5b52,0x4b60,0xa6e6,0xa4e0,0xd260,0xea65,0xd530,
        0x5aa0,0x76a3,0x96d0,0x4afb,0x4ad0,0xa4d0,0xd0b6,0xd25f
    };
    private final static int[] solarTermInfo = {
             0,  21208,  42467,  63836,  85337, 107014, 128867, 150921,
        173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033,
        353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758
    };
    private final static String[] Tianan = {
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };
    private final static String[] lunarString1 = {
        "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };
    private final static String[] weekString = {
        "日", "一", "二", "三", "四", "五", "六"
    };
    private static String[] lunarString2;
    private static String[] Deqi;
    private static String[] Animals;
    private static String[] solarTerm;
    private static String[] lFtv;
    private static String[] sFtv;
    private static String[] wFtv;

    private final static Pattern sFreg = Pattern.compile("^(\\d{2})(\\d{2})(.+)$");
    private final static Pattern wFreg = Pattern.compile("^(\\d{2})(\\d)(\\d)(.+)$");

    private static GregorianCalendar utcCal = null;

    private static int toInt(String str) {
        try { return Integer.parseInt(str); }
        catch(Exception e) { return -1; }
    }
    private synchronized void findFestival() {
        //是纯数字显示就直接返回，不计算节日
        if(Main._lang == 3)
            return;


        int sM = this.getSolarMonth();
        int sD = this.getSolarDay();
        int sY = this.getSolarYear();
        int lM = this.getLunarMonth();
        int lD = this.getLunarDay();
        Matcher m;

        //公历节日计算
        if(Main._solar){
            for (int i=0; i<Lunar.sFtv.length; i++) {
                m = Lunar.sFreg.matcher(Lunar.sFtv[i]);
                if (m.find()) {
                    if (sM == Lunar.toInt(m.group(1)) && sD == Lunar.toInt(m.group(2))) {
                        this.sFestivalName = m.group(3);
                        break;
                    }
                }
            }
            //查找月周节日
            int fw, fd;
            for (int i=0; i<Lunar.wFtv.length; i++) {
                m = Lunar.wFreg.matcher(Lunar.wFtv[i]);
                if (m.find()) {
                    if (this.getSolarMonth() == Lunar.toInt(m.group(1))) {
                        fw = Lunar.toInt(m.group(2));
                        fd = Lunar.toInt(m.group(3));
                        if (this.solar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == fw &&
                                this.solar.get(Calendar.DAY_OF_WEEK) == fd) {
                            this.sFestivalName += " " + m.group(4);
                        }
                    }
                }
            }

            //计算复活节
            if(Main._lang == 4 || Main._lang == 5){
                int a = sY % 19;
                int b = sY / 100;
                int c = sY % 100;
                int d = b / 4;
                int e = b % 4;
                int f = (b + 8) / 25;
                int g = (b - f + 1) / 3;
                int h = (19 * a + b - d - g + 15) % 30;
                int i = c / 4;
                int k = c % 4;
                int l = (32 + 2 * e + 2 * i - h - k) % 7;
                int z = (a + 11 * h + 22 * l) / 451;
                if(sM == (h + l - 7 * z + 114) / 31 && sD == ((h + l - 7 * z + 114) % 31) + 1){
                    this.sFestivalName += " " + "復活節" ;
                }
            }

            this.sFestivalName = this.sFestivalName.replaceFirst("^\\s", "");
        }

        //农历
        if(Main._fest){
            for (int i=0; i<Lunar.lFtv.length; i++) {
                m = Lunar.sFreg.matcher(Lunar.lFtv[i]);
                if (m.find()) {
                    if (lM == Lunar.toInt(m.group(1)) && lD == Lunar.toInt(m.group(2))) {
                        this.lFestivalName = m.group(3);
                        break;
                    }
                    if(lM == 12 && lD == 29 && Lunar.getLunarMonthDays(this.lunarYear, lM) == 29){
                        this.lFestivalName = "除夕";
                        break;
                    }
                }
            }
        }

        //自定义农历
        if(Main._custom){
            for (int i=0; i<Main.clf.length; i++) {
                m = Lunar.sFreg.matcher(Main.clf[i]);
                if (m.find()) {
                    if (lM == Lunar.toInt(m.group(1)) && lD == Lunar.toInt(m.group(2))) {
                        this.clFestivalName = m.group(3);
                        break;
                    }
                }
            }
        }

        //自定义公历
        if(Main._solar_custom){
            for (int i=0; i<Main.csf.length; i++) {
                m = Lunar.sFreg.matcher(Main.csf[i]);
                if (m.find()) {
                    if (sM == Lunar.toInt(m.group(1)) && sD == Lunar.toInt(m.group(2))) {
                        this.csFestivalName = m.group(3);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 返回农历年闰月月份
     * @param lunarYear
     *            指定农历年份(数字)
     * @return 该农历年闰月的月份(数字,没闰返回0)
     */
    private static int getLunarLeapMonth(int lunarYear) {
        // 数据表中,每个农历年用16bit来表示,
        // 前12bit分别表示12个月份的大小月,最后4bit表示闰月
        // 若4bit全为1或全为0,表示没闰, 否则4bit的值为闰月月份
        int leapMonth = Lunar.lunarInfo[lunarYear - 2000] & 0xf;
        leapMonth = (leapMonth == 0xf ? 0 : leapMonth);
        return leapMonth;
    }

    /**
     * 返回农历年闰月的天数
     *
     * @param lunarYear 指定农历年份(数字)
     * @return 该农历年闰月的天数(数字)
     */
    private static int getLunarLeapDays(int lunarYear) {
        // 下一年最后4bit为1111,返回30(大月)
        // 下一年最后4bit不为1111,返回29(小月)
        // 若该年没有闰月,返回0
        return Lunar.getLunarLeapMonth(lunarYear) > 0 ? ((Lunar.lunarInfo[lunarYear - 1999] & 0xf) == 0xf ? 30 : 29) : 0;
    }

    /**
     * 返回农历年的总天数
     * @param lunarYear 指定农历年份(数字)
     * @return 该农历年的总天数(数字)
     */
    private static int getLunarYearDays(int lunarYear) {
        // 按小月计算,农历年最少有12 * 29 = 348天
        int daysInLunarYear = 348;
        // 数据表中,每个农历年用16bit来表示,
        // 前12bit分别表示12个月份的大小月,最后4bit表示闰月
        // 每个大月累加一天
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            daysInLunarYear += ((Lunar.lunarInfo[lunarYear - 2000] & i) != 0) ? 1 : 0;
        }
        // 加上闰月天数
        daysInLunarYear += Lunar.getLunarLeapDays(lunarYear);

        return daysInLunarYear;
    }

    /**
     * 返回农历年正常月份的总天数
     * @param lunarYear
     *            指定农历年份(数字)
     * @param lunarMonth
     *            指定农历月份(数字)
     * @return 该农历年闰月的月份(数字,没闰返回0)
     */
    private static int getLunarMonthDays(int lunarYear, int lunarMonth) {
        // 数据表中,每个农历年用16bit来表示,
        // 前12bit分别表示12个月份的大小月,最后4bit表示闰月
        int daysInLunarMonth = ((Lunar.lunarInfo[lunarYear - 2000] & (0x10000 >> lunarMonth)) != 0) ? 30 : 29;
        return daysInLunarMonth;
    }

    /**
     * 取 Date 对象中用全球标准时间 (UTC) 表示的日期
     *
     * @param date 指定日期
     * @return UTC 全球标准时间 (UTC) 表示的日期
     */
    public static synchronized int getUTCDay(Date date) {
        Lunar.makeUTCCalendar();
        synchronized (utcCal) {
            utcCal.clear();
            utcCal.setTimeInMillis(date.getTime());
            return utcCal.get(Calendar.DAY_OF_MONTH);
        }
    }

    private static synchronized void makeUTCCalendar() {
        if (Lunar.utcCal == null) {
            Lunar.utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        }
    }
    /**
     * 返回全球标准时间 (UTC) (或 GMT) 的 1970 年 1 月 1 日到所指定日期之间所间隔的毫秒数。
     * @param y 指定年份
     * @param m 指定月份
     * @param d 指定日期
     * @param h 指定小时
     * @param min 指定分钟
     * @param sec 指定秒数
     * @return 全球标准时间 (UTC) (或 GMT) 的 1970 年 1 月 1 日到所指定日期之间所间隔的毫秒数
     */
    public static synchronized long UTC(int y, int m, int d, int h, int min, int sec) {
        Lunar.makeUTCCalendar();
        synchronized (utcCal) {
            utcCal.clear();
            utcCal.set(y, m, d, h, min, sec);
            return utcCal.getTimeInMillis();
        }
    }

    /**
     * 返回公历年节气的日期
     * @param solarYear 指定公历年份(数字)
     * @param index 指定节气序号(数字,0从小寒算起)
     * @return 日期(数字,所在月份的第几天)
     */
    private static int getSolarTermDay(int solarYear, int index) {
        long l = (long)31556925974.7 * (solarYear - 2000) + solarTermInfo[index] * 60000L;
        l = l + Lunar.UTC(2000,0,6,0,0,0); //这里可能导致时区不同出现的计算结果错误
        return Lunar.getUTCDay(new Date(l));
    }

    public Lunar(int lang) {
        //在这里设置语言
        switch(lang){
            //大陆简中
            case 1:
                Lunar.lFtv = new String[]{
                    "",
                    "0101春节", "0115元宵", "0202龙头",
                    "0505端午", "0707七夕", "0715中元",
                    "0815中秋", "0909重阳", "1208腊八",
                    "1230除夕"
                };
                Lunar.sFtv = new String[]{
                    "0101元旦", "0214情人节", "0308妇女节", "0312植树节",
                    "0401愚人节", "0501劳动节", "0504青年节", "0601儿童节",
                    "0701建党节", "0801建军节", "0910教师节", "1001国庆节",
                    "1031万圣节", "1225圣诞节"
                };
                break;
            //台湾繁中
            case 2:
                Lunar.lFtv = new String[]{
                    "",
                    "0101春節", "0115元宵", "0202龍頭",
                    "0505端午", "0707七夕", "0715中元",
                    "0815中秋", "0909重陽", "1208臘八",
                    "1230除夕"
                };
                Lunar.sFtv = new String[]{
                    "0101元旦", "0214情人節", "0228和平紀念日", "0308婦女節",
                    "0404兒童節",    "0401愚人節", "0501勞動節", "0903軍人節",
                    "0928教師節",    "1010國慶日", "1031萬聖節", "1225耶誕節"
                };
                break;
            //香港繁中
            case 4:
                Lunar.lFtv = new String[]{
                    "",
                    "0101春節", "0115元宵", "0202龍頭",
                    "0505端午", "0707七夕", "0715中元",
                    "0815中秋", "0909重陽", "1208臘八",
                    "1230除夕", "0408佛诞"
                };
                Lunar.sFtv = new String[]{
                    "0101元旦", "0214情人節", "0308婦女節", "0401愚人節",
                    "0501勞動節", "0701特區紀念日", "0910教師節", "1001國慶日",
                    "1031萬聖節", "1225聖誕節"
                };
                break;
            //澳门繁中
            case 5:
                Lunar.lFtv = new String[]{
                    "",
                    "0101春節", "0115元宵", "0202龍頭",
                    "0505端午", "0707七夕", "0715中元",
                    "0815中秋", "0909重陽", "1208臘八",
                    "1230除夕", "0408佛诞"
                };
                Lunar.sFtv = new String[]{
                    "0101元旦", "0214情人節", "0308婦女節", "0401愚人節",
                    "0501勞動節", "0910教師節", "1001國慶日", "1031萬聖節",
                    "1102追思節", "1208聖母無原罪日", "1220特區紀念日", "1225聖誕節"
                };
                break;
            //大陆繁中
            case 6:
                Lunar.lFtv = new String[]{
                    "",
                    "0101春節", "0115元宵", "0202龍頭",
                    "0505端午", "0707七夕", "0715中元",
                    "0815中秋", "0909重陽", "1208臘八",
                    "1230除夕"
                };
                Lunar.sFtv = new String[]{
                    "0101元旦", "0214情人節", "0308婦女節", "0312植樹節",
                    "0401愚人節", "0501勞動節", "0504青年節", "0601兒童節",
                    "0701建黨節", "0801建軍節", "0910教師節", "1001國慶日",
                    "1031萬聖節", "1225耶誕節"
                };
                break;
            //英文，纯数字，初始化空字符串数组
            case 3:
                Lunar.Deqi = new String[]{"","","","","","","","","","","",""};
                Lunar.Animals = new String[]{"","","","","","","","","","","",""};
                Lunar.solarTerm = new String[]{
                    "", "", "", "", "", "",
                    "", "", "", "", "", "",
                    "", "", "", "", "", "",
                    "", "", "", "", "", ""
                };
                Lunar.lunarString2 = new String[]{"","","","","","","",""};
                Lunar.lFtv = new String[]{};
                Lunar.sFtv = new String[]{};
                Lunar.wFtv = new String[]{};
                break;
        }
        //如果不是英语
        if(lang != 3){
            //选择小年日期
            switch(Main._minor){
                case 1: Lunar.lFtv[0] = "1223 小年";break;
                case 2: Lunar.lFtv[0] = "1224 小年";break;
                case 3: Lunar.lFtv[0] = "1225 小年";break;
            }
            //初始化简体中文的通用字符串
            if(lang == 1){
                Lunar.Deqi = new String[]{
                    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
                };
                Lunar.Animals = new String[]{
                    "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
                };
                Lunar.solarTerm = new String[]{
                    "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
                    "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
                    "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
                    "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
                };
                Lunar.lunarString2 = new String[]{
                    "初", "十", "廿", "正", "冬", "腊", "闰"
                };
                Lunar.wFtv = new String[]{
                    "0521母亲节", "0631父亲节", "1145感恩节"
                };
            //初始化繁体中文的通用字符串
            }else{
                Lunar.Deqi = new String[]{
                    "子", "醜", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
                };
                Lunar.Animals = new String[]{
                    "鼠", "牛", "虎", "兔", "龍", "蛇", "馬", "羊", "猴", "雞", "狗", "豬"
                };
                Lunar.solarTerm = new String[]{
                    "小寒", "大寒", "立春", "雨水", "驚蟄", "春分",
                    "清明", "穀雨", "立夏", "小滿", "芒種", "夏至",
                    "小暑", "大暑", "立秋", "處暑", "白露", "秋分",
                    "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
                };
                Lunar.lunarString2 = new String[]{
                    "初", "十", "廿", "正", "冬", "臘", "閏"
                };
                Lunar.wFtv = new String[]{
                    "0521母親節", "0631父親節", "1145感恩節"
                };
            }
        }
    }

    public void init(long TimeInMillis) {
        lFestivalName = "";
        sFestivalName = "";
        termString = "";
        clFestivalName = "";
        csFestivalName = "";

        this.solar = Calendar.getInstance();
        this.solar.setTimeZone(TimeZone.getDefault());
        this.solar.setTimeInMillis(TimeInMillis);
        Calendar baseDate = new GregorianCalendar(2000, 1, 5);
        long offset = (TimeInMillis - baseDate.getTimeInMillis()) / 86400000;
        // 按农历年递减每年的农历天数，确定农历年份
        this.lunarYear = 2000;
        int daysInLunarYear = Lunar.getLunarYearDays(this.lunarYear);
        while (this.lunarYear < 2037 && offset >= daysInLunarYear) {
            offset -= daysInLunarYear;
            daysInLunarYear = Lunar.getLunarYearDays(++this.lunarYear);
        }
        // 农历年数字

        // 按农历月递减每月的农历天数，确定农历月份
        int lunarMonth = 1;
        // 所在农历年闰哪个月,若没有返回0
        int leapMonth = Lunar.getLunarLeapMonth(this.lunarYear);
        // 是否闰年
        this.isLeapYear = leapMonth > 0;
        // 闰月是否递减
        boolean leapDec = false;
        boolean isLeap = false;
        int daysInLunarMonth = 0;
        while (lunarMonth<13 && offset>0) {
            if (isLeap && leapDec) { // 如果是闰年,并且是闰月
                // 所在农历年闰月的天数
                daysInLunarMonth = Lunar.getLunarLeapDays(this.lunarYear);
                leapDec = false;
            } else {
                // 所在农历年指定月的天数
                daysInLunarMonth = Lunar.getLunarMonthDays(this.lunarYear, lunarMonth);
            }
            if (offset < daysInLunarMonth) {
                break;
            }
            offset -= daysInLunarMonth;

            if (leapMonth == lunarMonth && isLeap == false) {
                // 下个月是闰月
                leapDec = true;
                isLeap = true;
            } else {
                // 月份递增
                lunarMonth++;
            }
        }
        // 农历月数字
        this.lunarMonth = lunarMonth;
        // 是否闰月
        this.isLeap = (lunarMonth == leapMonth && isLeap);
        // 农历日数字
        this.lunarDay = (int) offset + 1;
        // 取得干支历
        this.getCyclicalData();

        findFestival();
    }

    /**
     * 取得公历节日名称
     * @return 公历节日名称,如果不是节日返回空串
     */
    public String getSFestivalName() {
        return this.sFestivalName;
    }

    /**
     * 取干支历 不是历年，历月干支，而是中国的从立春节气开始的节月，是中国的太阳十二宫，阳历的。
     * @param cncaData 日历对象(Tcnca)
     */
    private void getCyclicalData() {
        this.solarYear = this.solar.get(Calendar.YEAR);
        this.solarMonth = this.solar.get(Calendar.MONTH);
        this.solarDay = this.solar.get(Calendar.DAY_OF_MONTH);
        // 干支历
        int cyclicalYear = 0;
        int cyclicalMonth = 0;
        int cyclicalDay = 0;

        // 干支年2000年立春后为庚辰年(60进制16)
        int term2 = Lunar.getSolarTermDay(solarYear, 2); // 立春日期
        // 依节气调整二月分的年柱, 以立春为界
        if (solarMonth < 1 || (solarMonth == 1 && solarDay < term2)) {
            cyclicalYear = (solarYear - 2000 + 16 - 1) % 60;
        } else {
            cyclicalYear = (solarYear - 2000 + 16) % 60;
        }

        // 干支月 2000年1月小寒以前为 丙子月(60进制12)
        int firstNode = Lunar.getSolarTermDay(solarYear, solarMonth * 2); // 传回当月「节」为几日开始
        // 依节气月柱, 以「节」为界
        if (solarDay < firstNode) {
            cyclicalMonth = ((solarYear - 2000) * 12 + solarMonth + 12) % 60;
        } else {
            cyclicalMonth = ((solarYear - 2000) * 12 + solarMonth + 13) % 60;
        }

        // 当月一日与 1900/1/1 相差天数
        // 1900/1/1与 1970/1/1 相差25567日, 1900/1/1 日柱为甲戌日(60进制10)
        cyclicalDay = (int) (Lunar.UTC(solarYear, solarMonth, solarDay, 0, 0, 0) / 86400000 + 25567 + 10) % 60;

        this.cyclicalYear = cyclicalYear;
        this.cyclicalMonth = cyclicalMonth;
        this.cyclicalDay = cyclicalDay;

    }

    /**
     * 取农历年生肖
     * @return 农历年生肖(例:龙)
     */
    public String getAnimalString() {
        return Lunar.Animals[(this.lunarYear - 4) % 12];
    }

    /**
     * 返回公历日期的节气字符串
     * @return 二十四节气字符串,若不是节气日,返回空串(例:冬至)
     */
    public String getTermString() {
        // 二十四节气
        if("".equals(this.termString)){
            this.termString = "";
            if (Lunar.getSolarTermDay(solarYear, solarMonth * 2) == solarDay) {
                this.termString = Lunar.solarTerm[solarMonth * 2];
            } else if (Lunar.getSolarTermDay(solarYear, solarMonth * 2 + 1) == solarDay) {
                this.termString = Lunar.solarTerm[solarMonth * 2 + 1];
            }
        }
        return this.termString;
    }

    /**
     * 年份天干
     * @return 年份天干
     */
    public int getTiananY() {
        return Lunar.getTianan(this.cyclicalYear);
    }

    /**
     * 月份天干
     * @return 月份天干
     */
    public int getTiananM() {
        return Lunar.getTianan(this.cyclicalMonth);
    }

    /**
     * 日期天干
     * @return 日期天干
     */
    public int getTiananD() {
        return Lunar.getTianan(this.cyclicalDay);
    }

    /**
     * 年份地支
     * @return 年分地支
     */
    public int getDeqiY() {
        return Lunar.getDeqi(this.cyclicalYear);
    }

    /**
     * 月份地支
     * @return 月份地支
     */
    public int getDeqiM() {
        return Lunar.getDeqi(this.cyclicalMonth);
    }

    /**
     * 日期地支
     * @return 日期地支
     */
    public int getDeqiD() {
        return Lunar.getDeqi(this.cyclicalDay);
    }

    /**
     * 取得干支年字符串
     * @return 干支年字符串
     */
    public String getCyclicaYear() {
        return this.getCyclicalString(this.cyclicalYear);
    }

    /**
     * 取得干支月字符串
     * @return 干支月字符串
     */
    public String getCyclicaMonth() {
        return this.getCyclicalString(this.cyclicalMonth);
    }

    /**
     * 取得干支日字符串
     * @return 干支日字符串
     */
    public String getCyclicaDay() {
        return this.getCyclicalString(this.cyclicalDay);
    }

    /**
     * 返回农历日期字符串
     * @return 农历日期字符串
     */
    public String getLunarDayString() {
        return this.getLunarDayString(this.lunarDay);
    }

    /**
     * 返回农历日期字符串
     * @return 农历日期字符串
     */
    public String getLunarMonthString() {
        return (this.isLeap() ? Lunar.lunarString2[6] : "") + this.getLunarMonthString(this.lunarMonth);
    }

    /**
     * 返回农历日期字符串
     * @return 农历日期字符串
     */
    public String getLunarYearString() {
        return this.getLunarYearString(this.lunarYear);
    }

    /**
     * 农历年是否是闰月
     * @return 农历年是否是闰月
     */
    public boolean isLeap() {
        return isLeap;
    }

    /**
     * 农历年是否是闰年
     * @return 农历年是否是闰年
     */
    public boolean isLeapYear() {
        return isLeapYear;
    }

    /**
     * 农历日期
     * @return 农历日期
     */
    public int getLunarDay() {
        return lunarDay;
    }

    /**
     * 农历月份
     * @return 农历月份
     */
    public int getLunarMonth() {
        return lunarMonth;
    }

    /**
     * 农历年份
     * @return 农历年份
     */
    public int getLunarYear() {
        return lunarYear;
    }

    /**
     * 取得农历节日名称
     * @return 农历节日名称,如果不是节日返回空串
     */
    public String getLFestivalName() {
        return this.lFestivalName;
    }

    /**
     * 取得自定义农历节日名称
     * @return 农历节日名称,如果不是节日返回空串
     */
    public String getCLFestivalName() {
        return this.clFestivalName;
    }

    /**
     * 取得自定义公历节日名称
     * @return 公历节日名称,如果不是节日返回空串
     */
    public String getCSFestivalName() {
        return this.csFestivalName;
    }

    /**
     * 公历日期
     * @return 公历日期
     */
    public int getSolarDay() {
        return solarDay;
    }

    /**
     * 公历月份
     * @return 公历月份 (不是从0算起)
     */
    public int getSolarMonth() {
        return solarMonth+1;
    }

    /**
     * 公历年份
     * @return 公历年份
     */
    public int getSolarYear() {
        return solarYear;
    }

    /**
     * 干支字符串
     * @param cyclicalNumber 指定干支位置(数字,0为甲子)
     * @return 干支字符串
     */
    private String getCyclicalString(int cyclicalNumber) {
        return Lunar.Tianan[Lunar.getTianan(cyclicalNumber)] + Lunar.Deqi[Lunar.getDeqi(cyclicalNumber)];
    }

    /**
     * 获得地支
     * @param cyclicalNumber
     * @return 地支 (数字)
     */
    private static int getDeqi(int cyclicalNumber) {
         return cyclicalNumber % 12;
    }

    /**
     * 获得天干
     * @param cyclicalNumber
     * @return 天干 (数字)
     */
    private static int getTianan(int cyclicalNumber) {
         return cyclicalNumber % 10;
    }

    /**
     * 返回指定数字的农历年份表示字符串
     * @param lunarYear 农历年份(数字,0为甲子)
     * @return 农历年份字符串
     */
    private String getLunarYearString(int lunarYear) {
        return this.getCyclicalString(lunarYear - 2000 + 16);
    }

    /**
     * 返回指定数字的农历月份表示字符串
     * @param lunarMonth 农历月份(数字)
     * @return 农历月份字符串 (例:正)
     */
    private String getLunarMonthString(int lunarMonth) {
        String lunarMonthString = "";
        if (lunarMonth == 1) {
            lunarMonthString = Lunar.lunarString2[3];
        }else if(lunarMonth == 10){
            lunarMonthString += Lunar.lunarString2[1];
        }else if(lunarMonth > 10){
            lunarMonthString += Lunar.lunarString2[lunarMonth % 10 + 3];
        }else if(lunarMonth % 10 > 0){
            lunarMonthString += Lunar.lunarString1[lunarMonth % 10];
        }
        return lunarMonthString;
    }

    /**
     * 返回指定数字的农历日表示字符串
     * @param lunarDay 农历日(数字)
     * @return 农历日字符串 (例: 廿一)
     */
    private String getLunarDayString(int lunarDay) {
        if (lunarDay<1 || lunarDay>30) return "";
        int i1 = lunarDay / 10;
        int i2 = lunarDay % 10;
        String c1 = Lunar.lunarString2[i1];
        String c2 = Lunar.lunarString1[i2];
        if (lunarDay < 11){
            c1 = Lunar.lunarString2[0];
            if(i2 == 0) c2 = Lunar.lunarString2[1];
        }else if (i2 == 0){
            c2 = Lunar.lunarString2[1];
            c1 = Lunar.lunarString1[i1];
        }
        return c1 + c2;
    }

    /**
     * 返回格式化日期文本
     * @param t 自定义格式字符串
     * @param f 格式选项
     * @return 格式化日期文本
     */

    public String getFormattedDate(String t, int f){
        String sfest, fest, term, custom, sfest_custom, lunarText = "";

        //仅在不为英文时查找节日
        if(Main._lang != 3){
            //判断是否是公历节日
            if (Main._solar && (!"".equals(this.getSFestivalName()))){
                sfest = " " + this.getSFestivalName();
            }else{
                sfest = "";
            }

            //判断是否是农历节日
            if (Main._fest && (!"".equals(this.getLFestivalName()))){
                fest = " " + this.getLFestivalName();
            }else{
                fest = "";
            }

            //判断是否是二十四节气
            if (Main._term && (!"".equals(this.getTermString()))){
                term = " " + this.getTermString();
            }else{
                term = "";
            }

            //判断是否是自定义农历节日
            if (Main._custom && (!"".equals(this.getCLFestivalName()))){
                custom = " " + this.getCLFestivalName();
            }else{
                custom = "";
            }

            //判断是否是自定义公历节日
            if (Main._solar_custom && (!"".equals(this.getCSFestivalName()))){
                sfest_custom = " " + this.getCSFestivalName();
            }else{
                sfest_custom = "";
            }

            //根据设置设置显示格式
            switch(f){
                //属相
                case 1:
                    lunarText = this.getAnimalString() + "年" + this.getLunarMonthString() + "月" + this.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
                    break;
                //天干地支
                case 2:
                    lunarText = this.getLunarYearString() + "年" + this.getLunarMonthString() + "月" + this.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
                    break;
                //不显示年份
                case 3:
                    lunarText = this.getLunarMonthString() + "月" + this.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
                    break;
                //天干地支+属相
                case 4:
                    lunarText = this.getLunarYearString() + this.getAnimalString() + "年" + this.getLunarMonthString() + "月" + this.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
                    break;
                //自定义
                case 5:
                    t = t.replace("YYYY", String.valueOf(this.getSolarYear()));
                    t = t.replace("MMMM", dfMMDD.format(this.getSolarMonth()));
                    t = t.replace("DDDD", dfMMDD.format(this.getSolarDay()));
                    t = t.replace("yyyy", String.valueOf(this.getSolarYear()).replaceFirst("\\d\\d", ""));
                    t = t.replace("mmmm", String.valueOf(this.getSolarMonth()));
                    t = t.replace("dddd", String.valueOf(this.getSolarDay()));
                    t = t.replace("YY", this.getLunarYearString());
                    t = t.replace("yy", this.getAnimalString());
                    t = t.replace("mm", this.getLunarMonthString());
                    t = t.replace("dd", this.getLunarDayString());
                    t = t.replace("MM", this.getCyclicaMonth());
                    t = t.replace("DD", this.getCyclicaDay());
                    t = t.replace("NN", "\n");
                    t = t.replace("ww", weekString[this.solar.get(Calendar.DAY_OF_WEEK) - 1]);
                    lunarText = t.replace("ff", term + fest + custom + sfest + sfest_custom);
                    break;
            }
        }else{
            lunarText = "[" + this.getLunarDay() + "/" + this.getLunarMonth() + "]";
        }

        return lunarText.trim();

    }
}