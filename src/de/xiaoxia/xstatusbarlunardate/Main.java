/**
 * Copyright (C) 2014 xiaoxia.de
 *
 * @author xiaoxia.de
 * @date 2014
 * @license MIT
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 *
 */

// 状态栏日期，主程序

package de.xiaoxia.xstatusbarlunardate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources{

    /* 初始变量 */
    private static String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private static String breaklineText = "\n"; //是否换行的文本
    private static String lDate = "LAST"; //上次记录的日期
    private static String nDate;
    private static String finalText; //最终输出文本
    private static String lunarTextToast = ""; //最终输出文本仅节日
    private static Boolean layout_run = false; //判断是否设置过singleLine属性
    private final static Pattern reg = Pattern.compile("\\n"); //去除换行的正则表达式
    private static TextView textview;
    public static Context mContext;

    /* 读取设置 */
    //使用Xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    /* 将设置保存到变量中，以备后用 */
    //允许布局调整
    protected final static Boolean _remove_all = prefs.getBoolean("remove_all", false);
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
    //自定义状态栏字符串
    protected final static String _custom_format = prefs.getString("custom_format", "");
    //小年选项，将字符串型转换为整数型
    protected final static int _minor = Integer.valueOf(prefs.getString("minor", "1")).intValue();
    //语言选项，将字符串型转换为整数型
    protected final static int _lang = Integer.valueOf(prefs.getString("lang", "1")).intValue();
    //显示格式选项，将字符串型转换为整数型
    protected final static int _format = Integer.valueOf(prefs.getString("format", "1")).intValue();
    //系统类型选项，将字符串型转换为整数型
    protected final static int _rom = Integer.valueOf(prefs.getString("rom", "1")).intValue();

    //通知方法
    protected final static int _notify = Integer.valueOf(prefs.getString("notify", "1")).intValue();
    //通知次数
    protected static int _notify_times = Integer.valueOf(prefs.getString("notify_times", "3")).intValue();
    //通知居中
    protected final static Boolean _notify_center = prefs.getBoolean("notify_center", false);
    //显示图标
    protected final static Boolean _notify_icon = prefs.getBoolean("notify_icon", false);

    //开启添加到锁屏
    protected final static Boolean _lockscreen = prefs.getBoolean("lockscreen", false);
    //锁屏布局，将字符串型转换为整数型
    protected final static int _lockscreen_layout = Integer.valueOf(prefs.getString("lockscreen_layout", "1")).intValue();
    //锁屏对齐，将字符串型转换为整数型
    protected final static int _lockscreen_alignment = Integer.valueOf(prefs.getString("lockscreen_alignment", "1")).intValue();
    //显示格式选项，将字符串型转换为整数型
    protected final static int _lockscreen_format = Integer.valueOf(prefs.getString("lockscreen_format", "1")).intValue();
    //自定义锁屏字符串
    protected final static String _lockscreen_custom_format = prefs.getString("lockscreen_custom_format", "");

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
        prefs.getString("custom_lunar_item_15", "").trim(),
        prefs.getString("custom_lunar_item_16", "").trim(),
        prefs.getString("custom_lunar_item_17", "").trim(),
        prefs.getString("custom_lunar_item_18", "").trim(),
        prefs.getString("custom_lunar_item_19", "").trim()
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
        prefs.getString("custom_solar_item_14", "").trim(),
        prefs.getString("custom_solar_item_15", "").trim(),
        prefs.getString("custom_solar_item_16", "").trim(),
        prefs.getString("custom_solar_item_17", "").trim(),
        prefs.getString("custom_solar_item_18", "").trim(),
        prefs.getString("custom_solar_item_19", "").trim()
    };
    //初始化Lunar类
    private static Lunar lunar = new Lunar(_lang);


    private static String MODULE_PATH = null;
    private static int icon_id;
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.android.systemui"))
            return;

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        icon_id = resparam.res.addResource(modRes, R.drawable.ic_toast);
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

        //根据用户设置的rom类型进入相应的hook步骤
        switch(_rom){
            case 1:
                try{
                    //For most android roms
                    //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
                    //这的函数可以参考 https://github.com/rovo89/XposedBridge/wiki/Development-tutorial，比较简单
                    findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
                        @Override
                        //在原函数执行完之后再执行自定义程序
                        protected void afterHookedMethod(MethodHookParam param){
                            //获取原文字，com.android.systemui.statusbar.policy.DateView类是extends于TextView。
                            textview = (TextView) param.thisObject; //所以直接获取这个对象
                            registerReceiver(textview);
                            //交给setText处理
                            setText(textview);
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
                            registerReceiver(textview);
                            setText(textview);
                        }
                    });
                }catch(Exception e){
                    //Do nothing
                }
            break;
        }
    }

    /* 获取农历字符串子程序 */
    private void setText(TextView textview){
        /* 判断当前日期栏是否包含上次更新后的日期文本
         * 如果当前日期已经改变，则必须重新计算农历
         * 如果当前日期未改变，则只需要重新用已经缓存的文本写入TextView */
        //判断日期是否改变，不改变则不更新内容，改变则重新计算农历
        nDate = textview.getText().toString();
        if(!nDate.contains(lunarText)){
            if (!nDate.equals(lDate)) {
                //重置提醒次数
                _notify_times = Integer.valueOf(prefs.getString("notify_times", "3")).intValue();
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
                        //一般机型的状态栏都是RelativeLayout，少数为LinearLayout，但似乎影响不大
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                        layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0); //去除baseline对齐属性
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL); //并将其设置为绝对居中
                        textview.setLayoutParams(layoutParams); //设置布局参数
                    }
                    //设置宽度为fill_parent
                    if(prefs.getBoolean("layout_width", false)){
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                        layoutParams.width = -1; //取消宽度限制
                        textview.setLayoutParams(layoutParams);
                    }
                    layout_run = true; //已经执行过布局的处理步骤，下次不再执行
                }

                //更新记录的日期
                lDate = nDate;
                //从Lunar类中获得组合好的农历日期字符串（包括各节日）
                lunarText = lunar.getFormattedDate(_custom_format, _format);
                if(_notify > 1)
                    if(!"".equals(lunar.getFormattedDate("ff", 5).trim())){
                        lunarTextToast = _format == 5 ? lunar.getFormattedDate("", 3) : lunarText;
                    }else{
                        lunarTextToast = "";
                    }
                //如果需要去换行
                if(_remove){
                    Matcher mat = reg.matcher(nDate);
                    nDate = mat.replaceFirst(" "); //仅需要换掉第一个换行符，替换成一个空格保持美观和可读性
                }
                if(_remove_all)
                    nDate = breaklineText = "";
                //输出到最终字符串
                finalText = nDate + breaklineText + lunarText;
            }
            textview.setText(finalText);
        }
    }

    private void registerReceiver(TextView textview){
        if(mContext == null && _notify > 1){
            mContext = textview.getContext();
            IntentFilter intent = new IntentFilter();
            intent.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(xReceiver, intent);
        }
    }

    private BroadcastReceiver xReceiver = new BroadcastReceiver() {
        @Override  
        public void onReceive(Context context, Intent intent) {
            context = mContext;
            if(intent.getAction().equals(Intent.ACTION_USER_PRESENT) && !"".equals(Main.lunarTextToast)){
                if(_notify_times-- > 0){
                    Toast toast = Toast.makeText(context, lunarTextToast, Toast.LENGTH_LONG);
                    if(_notify_center)
                        toast.setGravity(Gravity.CENTER, 0, 0);
                    if(_notify_icon){
                        LinearLayout toastView = (LinearLayout) toast.getView();
                        ImageView imageview = new ImageView(context.getApplicationContext());
                        imageview.setImageResource(icon_id);
                        imageview.setPadding(0, 0, 0, 20);
                        toastView.addView(imageview, 0);
                    }
                    toast.show();
                }
            }
        }
    };
}