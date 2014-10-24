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

// 状态栏日期，主程序

package de.xiaoxia.xstatusbarlunardate;

import android.app.ActionBar.LayoutParams;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Gravity;
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
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources{

    private static String MODULE_PATH = null; //模块所在路径
    private static String PACKAGE_NAME = "com.android.systemui"; //系统UI的包名
    private static String HOOK_CLASS = "com.android.systemui.statusbar.policy.DateView"; //要Hook的Class名
    private static String UPDATE_FUNC = "updateClock"; //更新函数名
    private static String INTENT_SETTING_CHANGED = "de.xiaoxia.xstatusbarlunardate.SETTING_CHANGED"; //更改设置事件
    private static String INTENT_SETTING_TOAST = "de.xiaoxia.xstatusbarlunardate.SETTING_TOAST"; //显示吐司

    /* 初始变量 */
    private static String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private static String lDate = "LAST"; //上次记录的日期
    private static String finalText = "FINALTEXT"; //最终输出的文本
    private static String nDate; //当前状态栏的日期文字
    private static String lunarTextToast = ""; //最终输出文本仅节日
    private static Boolean layout_run = false; //判断是否设置过singleLine属性
    private static TextView mDateView = null; //状态栏日期的 TextView
    private static Context mContext = null; //显示Toast、注册接收器和获取系统服务所必需的context
    private static Boolean isFest = false; //今天是否为节日的标记
    private static KeyguardManager km = null; //锁屏管理器，用来获取锁屏状态
    private static PowerManager pm = null; //电源管理器，用来获取亮屏状态
    private static Vibrator vibrator = null; //震动
    private static int _notify_times_setting = 0; //记录设置好的提醒次数

    /* 读取设置 */
    //使用Xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    /* 将设置保存到变量中，以备后用 */
    protected static Boolean _remove_all = prefs.getBoolean("remove_all", false); //允许布局调整
    protected static Boolean _remove = prefs.getBoolean("remove", false); //删除换行
    protected static Boolean _term = prefs.getBoolean("term", true); //显示节气
    protected static Boolean _fest = prefs.getBoolean("fest", true); //显示农历节日
    protected static Boolean _custom = prefs.getBoolean("custom", false); //显示自定义农历节日
    protected static Boolean _solar = prefs.getBoolean("solar", true); //显示公历节日
    protected static Boolean _solar_custom = prefs.getBoolean("solar_cutom", true); //显示自定义公历节日
    protected static Boolean _breakline = prefs.getBoolean("breakline", true); //另起一行
    protected static Boolean _layout_enable = prefs.getBoolean("layout_enable", false); //允许布局调整
    protected static Boolean _layout_line = prefs.getBoolean("layout_line", false); //允许布局调整单行
    protected static Boolean _layout_width = prefs.getBoolean("layout_width", false); //允许布局调整宽度
    protected static Boolean _layout_height = prefs.getBoolean("layout_height", false); //允许布局调整高度
    protected static Boolean _layout_align = prefs.getBoolean("layout_align", false); //允许布局调整对齐
    protected static String _custom_format = prefs.getString("custom_format", ""); //自定义状态栏字符串
    protected static int _minor = Integer.valueOf(prefs.getString("minor", "1")).intValue(); //小年选项，将字符串型转换为整数型
    protected static int _lang = Integer.valueOf(prefs.getString("lang", "1")).intValue(); //语言选项，将字符串型转换为整数型
    protected static int _format = Integer.valueOf(prefs.getString("format", "1")).intValue(); //显示格式选项，将字符串型转换为整数型
    protected static int _rom = Integer.valueOf(prefs.getString("rom", "1")).intValue(); //系统类型选项，将字符串型转换为整数型

    protected static int _notify = Integer.valueOf(prefs.getString("notify", "1")).intValue(); //通知方法
    protected static int _notify_times = Integer.valueOf(prefs.getString("notify_times", "1")).intValue(); //通知次数
    protected static Boolean _notify_center = prefs.getBoolean("notify_center", false); //通知居中
    protected static Boolean _notify_icon = prefs.getBoolean("notify_icon", false); //显示图标
    protected static Boolean _notify_comp = prefs.getBoolean("notify_comp", false); //兼容性
    protected static Boolean _notify_vibration = prefs.getBoolean("notify_vibration", false); //震动

    protected static Boolean _lockscreen = prefs.getBoolean("lockscreen", false); //开启添加到锁屏
    protected static int _lockscreen_layout = Integer.valueOf(prefs.getString("lockscreen_layout", "1")).intValue(); //锁屏布局，将字符串型转换为整数型
    protected static int _lockscreen_alignment = Integer.valueOf(prefs.getString("lockscreen_alignment", "1")).intValue(); //锁屏对齐，将字符串型转换为整数型
    protected static int _lockscreen_format = Integer.valueOf(prefs.getString("lockscreen_format", "1")).intValue(); //显示格式选项，将字符串型转换为整数型
    protected static String _lockscreen_custom_format = prefs.getString("lockscreen_custom_format", ""); //自定义锁屏字符串

    //读取自定义农历纪念日，并放入到一个数组中
    protected static String[] clf = {
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
    protected static String[] csf = {
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
    private static Lunar lunar;

    /* 向Systemui注入图标和字符串资源*/
    private static int ic_toast_bg_fest;
    private static int ic_toast_bg;

    @Override
    public void initZygote(StartupParam startupParam){
        MODULE_PATH = startupParam.modulePath; //获取模块实际路径
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam){
        if (!resparam.packageName.equals(PACKAGE_NAME))
            return; //如果不是UI则跳过

        //这里将自带的图标资源插入到systemui中，并获取到一个资源id
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res); //创建一个插入资源的实例
        ic_toast_bg_fest = resparam.res.addResource(modRes, R.drawable.ic_toast_bg_fest);
        ic_toast_bg      = resparam.res.addResource(modRes, R.drawable.ic_toast_bg);
    }

    /* 替换日期函数 */
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam){
        if(!lpparam.packageName.equals(PACKAGE_NAME))
            return; //如果不是UI则跳过

        lunar = new Lunar(Main._lang); //初始化Lunar类

        _notify_times_setting = _notify_times; //记录提醒次数

        //如果打开了调整布局，则允许进入调整布局步骤
        if(!_layout_enable)
            layout_run = true;

        //根据rom类型选择正确的更新函数
        switch(_rom){
            case 1: break; //原生Android的日起更新函数名
            case 2: UPDATE_FUNC = "a";
                    break; //Miui 4.4 之前的系统更新时间的函数名称为“a”
            case 3: HOOK_CLASS = "com.android.systemui.statusbar.policy.QuickSettingsDateView";
                    break; //Asus Zenfone6
        }

        //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
        //这的函数可以参考 https://github.com/rovo89/XposedBridge/wiki/Development-tutorial，比较简单
        findAndHookMethod(HOOK_CLASS, lpparam.classLoader, UPDATE_FUNC, new XC_MethodHook() {
            @Override
            //在原函数执行完之后再执行自定义程序
            protected void afterHookedMethod(MethodHookParam param){
                //获取原文字，com.android.systemui.statusbar.policy.DateView类是extends于TextView
                if(mContext == null){
                    mDateView = (TextView) param.thisObject; //获取这个对象
                    mContext = mDateView.getContext(); //获取上下文
                    if(mContext != null)
                        registerReceiver(mContext); //注册接收器
                }
                setText();//交给setText处理
            }
        });
    }

    /* 获取农历字符串子程序 */
    private void setText(){
        /* 判断当前日期栏是否包含上次更新后的日期文本
         * 如果当前日期已经改变，则必须重新计算农历
         * 如果当前日期未改变，则只需要重新用已经缓存的文本写入TextView */
        //判断日期是否改变，不改变则不更新内容，改变则重新计算农历
        nDate = mDateView.getText().toString();
        if(!(nDate.contains(lunarText) || nDate.equals(finalText))){
            if (!nDate.equals(lDate)) {
                //获取时间
                lunar.init();

                //修正layout的singleLine属性
                if(!layout_run){
                    try{
                        //去掉singleLine属性
                        if(_layout_line)
                            mDateView.setSingleLine(false); //去除singleLine属性
                        //去掉align_baseline，并将其设置为center_vertical
                        if(_layout_align){
                            //一般机型的状态栏都是RelativeLayout，少数为LinearLayout，但似乎影响不大
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mDateView.getLayoutParams();
                            layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0); //去除baseline对齐属性
                            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL); //并将其设置为绝对居中
                            mDateView.setLayoutParams(layoutParams); //设置布局参数
                        }
                        //设置宽度为wrap_content
                        if(_layout_width){
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mDateView.getLayoutParams();
                            layoutParams.width = LayoutParams.WRAP_CONTENT; //取消宽度限制
                            mDateView.setLayoutParams(layoutParams);
                        }
                        //设置高度为wrap_content
                        if(_layout_height){
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mDateView.getLayoutParams();
                            layoutParams.height = LayoutParams.WRAP_CONTENT; //取消高度限制
                            mDateView.setLayoutParams(layoutParams);
                        }
                        layout_run = true; //已经执行过布局的处理步骤，下次不再执行
                    }catch(Throwable t){
                        XposedBridge.log("xStatusBarLunarDate: Statusbar layout fix error:");
                        XposedBridge.log(t);
                    }
                }

                //更新记录的日期
                lDate = nDate;
                //从Lunar类中获得组合好的农历日期字符串（包括各节日）
                lunarText = lunar.getFormattedDate(_custom_format, _format);

                //重置提醒次数
                _notify_times = _notify_times_setting;
                if(_notify > 1){
                    //当天是否是节日
                    isFest = !"".equals(lunar.getFormattedDate("ff", 5));
                    if(isFest || _notify == 2){
                        lunarTextToast = nDate.trim().replaceAll("\\n", " ") + "\n" + (_format == 5 ? lunar.getFormattedDate("", 3) : lunarText);
                    }else{
                        lunarTextToast = "";
                    }
                }
                //组合最终显示的文字
                finalText = _remove_all ? lunarText : (_remove ? nDate.trim().replaceAll("[\\n|\\r]", " ") : nDate) + (_breakline ? "\n" : " ") + lunarText;
            }
            //向TextView设置显示的最终文字
            mDateView.setText(finalText);
        }
    }

    //震动控制
    private void makeVibration(Context context){
        if(vibrator == null)
            vibrator = (Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
        if(isFest && _notify_vibration) //仅当有节日且开启时才震动
            vibrator.vibrate(250);
    }

    //显示 Toast
    private void makeToast(Context context){
        if("".equals(lunarTextToast))
            return;

        try{
            Toast toast = Toast.makeText(context, lunarTextToast, Toast.LENGTH_LONG);
            if(_notify_comp){
                //如果打开了美化
                LinearLayout toastView = (LinearLayout) toast.getView();

                //尝试寻找toastView布局中的textView节点
                TextView toastTextView;
                try{
                    toastTextView = (TextView) toastView.getChildAt(0);
                }catch(Throwable t){
                    try {
                        toastTextView = (TextView) toastView.getChildAt(1);
                    } catch (Throwable tt) {
                        toastTextView = null;
                    }
                }
                if(toastTextView != null){
                    toastTextView.setGravity(Gravity.CENTER_HORIZONTAL); //调整Toast为文字居中
                    toastTextView.setLineSpacing(0, 1.2f); //调整Toast文字行间距为原来的1.2倍
                }

                if(_notify_center)
                    //Toast在屏幕正中显示
                    toast.setGravity(Gravity.CENTER, 0, 0);

                if(_notify_icon){
                    //为Toast加入背景
                    toastView.setBackground((context.getResources().getDrawable(isFest ? ic_toast_bg_fest : ic_toast_bg)));
                    toastView.setGravity(Gravity.CENTER);
                    if(toastTextView != null){
                        toastTextView.setTextColor(0xFF000000);
                        toastTextView.setPadding(0, 15, 0, 0);
                        toastTextView.setShadowLayer(0, 0, 0, 0X00FFFFFF);
                    }
                }
            }
            toast.show();
            makeVibration(context);
        }catch(Throwable t){
            XposedBridge.log("xStatusBarLunarDate: Toast error:");
            XposedBridge.log(t);
        }
    }

    //重置日期记录信息
    private void resetLast(){
        lunarText = finalText = lDate = "RESET"; //重置记录的日期

        //4.4以上有个mLastText储存上次的日期字符串，在此尝试清空它，避免导致文字出现重复等现象
        if(Build.VERSION.SDK_INT >= 19){
            try{
                XposedHelpers.setObjectField(mDateView, "mLastText", ""); //重置DateView内部日期记录
            }catch(Throwable t){}
        }
    }

    //注册接收器
    private void registerReceiver(Context context){
        IntentFilter intent = new IntentFilter();
        intent.addAction(Intent.ACTION_USER_PRESENT); //注册解锁屏幕事件
        intent.addAction(Intent.ACTION_SCREEN_ON); //注册亮屏事件
        intent.addAction(Intent.ACTION_DATE_CHANGED); //注册日期变更事件
        intent.addAction(Intent.ACTION_TIMEZONE_CHANGED); //注册时区变更事件
        intent.addAction(INTENT_SETTING_CHANGED); //注册设置变更事件
        intent.addAction(INTENT_SETTING_TOAST); //注册显示Toast事件
        context.registerReceiver(xReceiver, intent);
    }

    //广播接收处理
    private BroadcastReceiver xReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context = mContext;

            if(km == null)
                km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if(pm == null)
                pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            //Intent事件判断
            if(intent.getAction().equals(INTENT_SETTING_CHANGED)){
                //如果用户更改了设置，从intent中读取更改过的设置
                _remove_all = intent.getExtras().getBoolean("remove_all", _remove_all);
                _remove = intent.getExtras().getBoolean("remove", _remove);
                _term = intent.getExtras().getBoolean("term", _term);
                _fest = intent.getExtras().getBoolean("fest", _fest);
                _custom = intent.getExtras().getBoolean("custom", _custom);
                _solar = intent.getExtras().getBoolean("solar", _solar);
                _solar_custom = intent.getExtras().getBoolean("solar_cutom", _solar_custom);
                _breakline = intent.getExtras().getBoolean("breakline", _breakline);
                _layout_enable = intent.getExtras().getBoolean("layout_enable", _layout_enable);
                _layout_line = intent.getExtras().getBoolean("layout_line", _layout_line);
                _layout_width = intent.getExtras().getBoolean("layout_width", _layout_width);
                _layout_align = intent.getExtras().getBoolean("layout_align", _layout_align);
                _layout_height = intent.getExtras().getBoolean("layout_height", _layout_height);
                _custom_format = intent.getExtras().getString("custom_format", _custom_format);
                _minor = intent.getExtras().getInt("minor", _minor);
                _lang = intent.getExtras().getInt("lang", _lang);
                _format = intent.getExtras().getInt("format", _format);

                _notify = intent.getExtras().getInt("notify", _notify);
                _notify_times_setting = _notify_times = intent.getExtras().getInt("notify_times", _notify_times_setting);
                _notify_center = intent.getExtras().getBoolean("notify_center", _notify_center);
                _notify_icon = intent.getExtras().getBoolean("notify_icon", _notify_icon);
                _notify_comp = intent.getExtras().getBoolean("notify_comp", _notify_comp);
                _notify_vibration = intent.getExtras().getBoolean("notify_vibration", _notify_vibration);

                for(int i = 0; i <= 14; i++){
                    clf[i] = intent.getExtras().getString("custom_lunar_item_" + i, clf[i]);
                    csf[i] = intent.getExtras().getString("custom_solar_item_" + i, csf[i]);
                }

                //重置一些变量
                if(!_layout_enable)
                    layout_run = true;

                resetLast();
                lunar = new Lunar(_lang);
                XposedHelpers.callMethod(mDateView, UPDATE_FUNC); //强制执行日期更新函数
            }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                //如果用户亮屏且屏幕处于未解锁状态
                if(!km.inKeyguardRestrictedInputMode() && _notify_times > 0){
                    makeToast(context);
                     _notify_times--;
                }
            }else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                //如果用户解锁屏幕
                if(_notify_times > 0){
                    makeToast(context);
                    _notify_times--;
                }
            }else if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
                //如果日期变更且用户处于亮屏状态
                resetLast();
                XposedHelpers.callMethod(mDateView, UPDATE_FUNC); //强制执行日期更新函数
                if(pm.isScreenOn() && !km.inKeyguardRestrictedInputMode())
                    makeToast(context);
            }else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)){
                //如果时区变更
                resetLast();
                lunar = new Lunar(_lang);
                XposedHelpers.callMethod(mDateView, UPDATE_FUNC); //强制执行日期更新函数
                makeToast(context);
            }else if(intent.getAction().equals(INTENT_SETTING_TOAST)){
                if(_notify != 2){
                    //改为总是弹出，强制更新，再改回去，强制更新
                    int _notify_temp = _notify; //设置一个临时变量记录初始状态
                    _notify = 2; //设置为总是弹出通知
                    resetLast();
                    XposedHelpers.callMethod(mDateView, UPDATE_FUNC); //强制执行日期更新函数
                    makeToast(context);
                    _notify = _notify_temp; // 吐司通知设置复位到初始状态
                    lunarTextToast = ""; // 清空Toast文字，避免其它情况再次显示
                    resetLast();
                    XposedHelpers.callMethod(mDateView, UPDATE_FUNC);
                }else{
                    makeToast(context);
                }
            }
        }
    };
}