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

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.os.PowerManager;
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
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources{

    private static String MODULE_PATH = null;
    private static String PACKAGE_NAME = "com.android.systemui";
    private static String HOOK_CLASS = "com.android.systemui.statusbar.policy.DateView";

    /* 初始变量 */
    private static String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private static String breaklineText = "\n"; //是否换行的文本
    private static String lDate = "LAST"; //上次记录的日期
    private static String nDate;
    private static String lunarTextToast = ""; //最终输出文本仅节日
    private static Boolean layout_run = false; //判断是否设置过singleLine属性
    private static TextView mDateView;
    private static Context mContext;
    private static Boolean isFest = false;

    /* 读取设置 */
    //使用Xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    /* 将设置保存到变量中，以备后用 */
    //允许布局调整
    protected final static Boolean _remove_all = prefs.getBoolean("remove_all", false);
    //删除换行
    protected final static Boolean _remove = prefs.getBoolean("remove", false);
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
    protected static int _notify_times = Integer.valueOf(prefs.getString("notify_times", "1")).intValue();
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

    /* 向Systemui注入图标 */
    private static int iconNormal_id;
    private static int iconFest_id;
    @Override
    public void initZygote(StartupParam startupParam){
        MODULE_PATH = startupParam.modulePath;
    }

    public void handleInitPackageResources(InitPackageResourcesParam resparam){
        if (!resparam.packageName.equals(PACKAGE_NAME))
            return;

        //这里将自带的图标资源插入到systemui中，并获取到一个资源id
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        iconNormal_id = resparam.res.addResource(modRes, R.drawable.ic_toast);
        iconFest_id   = resparam.res.addResource(modRes, R.drawable.ic_toast_fest);
    }

    /* 替换日期函数 */
    public void handleLoadPackage(final LoadPackageParam lpparam){
        if (!lpparam.packageName.equals(PACKAGE_NAME))
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
                //For most android roms
                //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
                //这的函数可以参考 https://github.com/rovo89/XposedBridge/wiki/Development-tutorial，比较简单
                findAndHookMethod(HOOK_CLASS, lpparam.classLoader, "updateClock", new XC_MethodHook() {
                    @Override
                    //在原函数执行完之后再执行自定义程序
                    protected void afterHookedMethod(MethodHookParam param){
                        //获取原文字，com.android.systemui.statusbar.policy.DateView类是extends于TextView。
                        mDateView = (TextView) param.thisObject; //所以直接获取这个对象
                        if(mDateView != null){
                            //注册接收器
                            registerReceiver();
                            //交给setText处理
                            setText();
                        }
                    }
                });
                break;
            case 2:
                //For Miui
                //Miui 4.4 之前的系统更新时间的函数名称为“a”
                findAndHookMethod(HOOK_CLASS, lpparam.classLoader, "a", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param){
                        mDateView = (TextView) param.thisObject;
                        if(mDateView != null){
                            registerReceiver();
                            setText();
                        }
                    }
                });
            break;
        }
    }

    /* 获取农历字符串子程序 */
    private void setText(){
        /* 判断当前日期栏是否包含上次更新后的日期文本
         * 如果当前日期已经改变，则必须重新计算农历
         * 如果当前日期未改变，则只需要重新用已经缓存的文本写入TextView */
        //判断日期是否改变，不改变则不更新内容，改变则重新计算农历
        nDate = mDateView.getText().toString();
        if (!nDate.equals(lDate)) {
            //获取时间
            lunar.init(System.currentTimeMillis());
            //修正layout的singleLine属性
            if(!layout_run){
                //去掉singleLine属性
                if(prefs.getBoolean("layout_line", false)){
                    mDateView.setSingleLine(false); //去除singleLine属性
                }
                //去掉align_baseline，并将其设置为center_vertical
                if(prefs.getBoolean("layout_align", false)){
                    //一般机型的状态栏都是RelativeLayout，少数为LinearLayout，但似乎影响不大
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mDateView.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0); //去除baseline对齐属性
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL); //并将其设置为绝对居中
                    mDateView.setLayoutParams(layoutParams); //设置布局参数
                }
                //设置宽度为fill_parent
                if(prefs.getBoolean("layout_width", false)){
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mDateView.getLayoutParams();
                    layoutParams.width = -1; //取消宽度限制
                    mDateView.setLayoutParams(layoutParams);
                }
                layout_run = true; //已经执行过布局的处理步骤，下次不再执行
            }

            //更新记录的日期
            lDate = nDate;
            //从Lunar类中获得组合好的农历日期字符串（包括各节日）
            lunarText = lunar.getFormattedDate(_custom_format, _format);

            //重置提醒次数
            _notify_times = Integer.valueOf(prefs.getString("notify_times", "3")).intValue();
            if(_notify > 1){
                //当天是否是节日
                isFest = !"".equals(lunar.getFormattedDate("ff", 5));
                if(isFest || _notify == 2){
                    lunarTextToast = nDate.trim().replaceAll("\\n", " ") + "\n" + (_format == 5 ? lunar.getFormattedDate("", 3) : lunarText);
                }else{
                    lunarTextToast = "";
                }
            }
        }

        //向TextView设置显示的最终文字
        mDateView.setText(_remove_all ? lunarText : (_remove ? nDate.trim().replaceAll("\\n", " ") : nDate) + breaklineText + lunarText);
    }

    //显示 Toast
    private void makeToast(Context context){
        Toast toast = Toast.makeText(context, lunarTextToast, Toast.LENGTH_LONG);
        LinearLayout toastView = (LinearLayout) toast.getView();
        TextView toastTextView = (TextView) toastView.getChildAt(0);
        toastTextView.setGravity(Gravity.CENTER_HORIZONTAL); //调整Toast为文字居中
        toastTextView.setLineSpacing(0, 1.2f); //调整Toast文字行间距为原来的1.2倍
        if(_notify_center)
            toast.setGravity(Gravity.CENTER, 0, 0); //Toast在屏幕正中显示
        if(_notify_icon){
            //为Toast加入图标
            ImageView imageview = new ImageView(context.getApplicationContext());
            imageview.setImageResource(isFest ? iconFest_id : iconNormal_id);
            imageview.setPadding(0, 10, 0, 20);
            toastView.addView(imageview, 0);
        }
        toast.show();
    }

    //注册接收器
    private void registerReceiver(){
        if(mContext == null && _notify > 1){
            mContext = mDateView.getContext();
            if(mContext != null){
                IntentFilter intent = new IntentFilter();
                intent.addAction(Intent.ACTION_USER_PRESENT); //注册解锁屏幕事件
                intent.addAction(Intent.ACTION_DATE_CHANGED); //注册日期变更事件
                intent.addAction(Intent.ACTION_TIMEZONE_CHANGED); //注册时区变更事件
                mContext.registerReceiver(xReceiver, intent);
            }
        }
    }

    //广播接收
    private BroadcastReceiver xReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context = mContext;
            //如果用户解锁屏幕
            if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                if(_notify_times > 0 && !"".equals(Main.lunarTextToast)){
                    makeToast(context);
                    _notify_times--;
                }
            //如果用日期变更且用户处于亮屏状态
            }else if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
                XposedHelpers.callMethod(mDateView, "updateClock"); //强制执行updateClock函数
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
                if(pm.isScreenOn() && !km.inKeyguardRestrictedInputMode() && !"".equals(Main.lunarTextToast))
                    makeToast(context);
            }else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)){
                lunar = new Lunar(_lang);
                XposedHelpers.callMethod(mDateView, "updateClock");
                if(!"".equals(Main.lunarTextToast))
                    makeToast(context);
            }
        }
    };
}