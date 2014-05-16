package de.xiaoxia.xstatusbarlunardate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.RelativeLayout;
import android.widget.TextView;

//导入xposed基本类
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
//import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Main implements IXposedHookLoadPackage{

    /* 初始变量 */
    private static String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private static String breaklineText = "\n"; //是否换行的文本
    private static String lDate = "LAST"; //上次记录的日期
    private static String nDate;
    private static String finalText; //最终输出文本
    private static Boolean layout_run = false; //判断是否设置过singleLine属性
    private final static Pattern reg = Pattern.compile("\\n");
    private static TextView textview;

    /* 读取设置 */
    //使用xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    /* 将设置保存到变量中 */
    //删除换行
    protected final static Boolean _remove = prefs.getBoolean("remove", true);
    //显示节气
    protected final static Boolean _term = prefs.getBoolean("term", true);
    //显示农历节日
    protected final static Boolean _fest = prefs.getBoolean("fest", true);
    //显示自定义农历节日
    protected final static Boolean _custom = prefs.getBoolean("custom", false);
    //显示公历节日
    protected final static Boolean _solar = prefs.getBoolean("solar", true);
    //显示自定义公历节日
    protected final static Boolean _solar_custom = prefs.getBoolean("solar_cutom", true);
    //另起一行
    protected final static Boolean _breakline = prefs.getBoolean("breakline", true);
    //允许布局调整
    protected final static Boolean _layout_enable = prefs.getBoolean("layout_enable", false);
    //开启添加到锁屏
    protected final static Boolean _lockscreen = prefs.getBoolean("lockscreen", false);
    //小年选项，将字符串型转换为整数型
    protected final static int _minor = Integer.valueOf(prefs.getString("minor", "1")).intValue();
    //语言选项，将字符串型转换为整数型
    protected final static int _lang = Integer.valueOf(prefs.getString("lang", "1")).intValue();
    //年份显示选项，将字符串型转换为整数型
    protected final static int _year = Integer.valueOf(prefs.getString("year", "1")).intValue();
    //系统类型选项，将字符串型转换为整数型
    protected final static int _rom = Integer.valueOf(prefs.getString("rom", "1")).intValue();
    //锁屏布局，将字符串型转换为整数型
    protected final static int _lockscreen_layout = Integer.valueOf(prefs.getString("lockscreen_layout", "1")).intValue();
    //锁屏对齐，将字符串型转换为整数型
    protected final static int _lockscreen_alignment = Integer.valueOf(prefs.getString("lockscreen_alignment", "1")).intValue();
    //读取自定义农历纪念日，并放入到一个数组中
    protected final static String[] clf = {
        prefs.getString("custom_lunar_item_0", "").trim(),
        prefs.getString("custom_lunar_item_1", "").trim(),
        prefs.getString("custom_lunar_item_2", "").trim(),
        prefs.getString("custom_lunar_item_3", "").trim(),
        prefs.getString("custom_lunar_item_4", "").trim(),
        prefs.getString("custom_lunar_item_5", "").trim(),
        prefs.getString("custom_lunar_item_6", "").trim(),
        prefs.getString("custom_lunar_item_7", "").trim(),
        prefs.getString("custom_lunar_item_8", "").trim(),
        prefs.getString("custom_lunar_item_9", "").trim(),
        prefs.getString("custom_lunar_item_10", "").trim(),
        prefs.getString("custom_lunar_item_11", "").trim(),
        prefs.getString("custom_lunar_item_12", "").trim(),
        prefs.getString("custom_lunar_item_13", "").trim(),
        prefs.getString("custom_lunar_item_14", "").trim(),
        prefs.getString("custom_lunar_item_15", "").trim()
    };
    //读取自定义公历纪念日，并放入到一个数组中
    protected final static String[] csf = {
        prefs.getString("custom_solar_item_0", "").trim(),
        prefs.getString("custom_solar_item_1", "").trim(),
        prefs.getString("custom_solar_item_2", "").trim(),
        prefs.getString("custom_solar_item_3", "").trim(),
        prefs.getString("custom_solar_item_4", "").trim(),
        prefs.getString("custom_solar_item_5", "").trim(),
        prefs.getString("custom_solar_item_6", "").trim(),
        prefs.getString("custom_solar_item_7", "").trim(),
        prefs.getString("custom_solar_item_8", "").trim(),
        prefs.getString("custom_solar_item_9", "").trim(),
        prefs.getString("custom_solar_item_10", "").trim(),
        prefs.getString("custom_solar_item_11", "").trim(),
        prefs.getString("custom_solar_item_12", "").trim(),
        prefs.getString("custom_solar_item_13", "").trim(),
        prefs.getString("custom_solar_item_14", "").trim()
    };
    //初始化Lunar类
    private static Lunar lunar = new Lunar(_lang);

    /* 获取农历字符串子程序 */
    private String returnDate(String nDate){
        /* 判断当前日期栏是否包含上次更新后的日期文本
         * 1 如果包含,则说明原生updateClock()没有被执行，不用再去操作
         * 2 如果不包含，则说明需要重新写入TextView
         *  2.1 如果当前日期已经改变，则必须重新计算农历
         *  2.2 如果当前日期未改变，则只需要重新用已经缓存的文本写入TextView */
        //判断日期是否改变，不改变则不更新内容，改变则重新计算农历
        if (!nDate.contains(lunarText) && !nDate.equals(lDate)) {
            //获取时间
            lunar.init(System.currentTimeMillis());

            //修正layout的singleLine属性
            if(!layout_run){
                //去掉singleLine属性
                if(prefs.getBoolean("layout_line", false)){
                    textview.setSingleLine(false); //去除singleLine属性
                }
                //去掉align_baseline，并将其设置为center_vertical
                if(prefs.getBoolean("layout_align", false)){
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0); //去除baseline对齐属性
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL); //并将其设置为绝对居中
                    textview.setLayoutParams(layoutParams); 
                }
                //设置宽度为fill_parent
                if(prefs.getBoolean("layout_width", false)){
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                    layoutParams.width = -1;
                    textview.setLayoutParams(layoutParams);
                }
                layout_run = true; //已经执行过布局的处理步骤，下次不再执行
            }

            //更新记录的日期
            lDate = nDate;
            
            lunarText = lunar.getComboText();
            //输出到最终字符串
            finalText = nDate + breaklineText + lunarText;
            //如果需要去换行
            if(_remove){
                Matcher mat = reg.matcher(finalText);
                finalText = mat.replaceFirst(" "); //仅需要换掉第一个换行符，替换成一个空格保持美观和可读性
            }
        }
        return finalText;
    }

    /* 替换日期函数 */
    public void handleLoadPackage(final LoadPackageParam lpparam){
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        //将决定是否换行的文本输出到字符串中
        if(!_breakline){
            breaklineText = " ";
        }

        //如果打开了调整布局，则允许进入调整布局步骤
        if(!_layout_enable){
            layout_run = true;
        }


        //根据用户选择的rom类型进入相应的hook步骤
        switch(_rom){
            case 1: 
                try{
                    //For most android roms
                    //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
                    findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
                        @Override
                        //在原函数执行完之后再执行自定义程序
                        protected void afterHookedMethod(MethodHookParam param){
                            //获取原文字
                            textview = (TextView) param.thisObject;
                            //取textview中的字符串
                            nDate = textview.getText().toString();
                            //交给returnDate处理后，再setText显示出来
                            textview.setText(returnDate(nDate));
                        }
                    });
                }catch(Exception e){
                    //Do nothing
                }
                break;
            case 2:
                try{
                    //For Miui
                	//Miui 4.4 之前的系统更新时间的函数名称为“a”
                    findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "a", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param){
                            textview = (TextView) param.thisObject;    
                            nDate = textview.getText().toString();
                            textview.setText(returnDate(nDate));
                        }
                    });
                }catch(Exception e){
                    //Do nothing
                }
            break;
        }
    }
}