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

// 锁屏日期

package de.xiaoxia.xstatusbarlunardate;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.TextClock;
import android.widget.TextView;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Lockscreen implements IXposedHookLoadPackage{

    /* 初始变量 */
    private String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private String lDate = "LAST";
    private Lunar lunar = new Lunar(Main._lang);
    private TextClock mTextClock;
    private TextView mTextView;

    /*获取农历字符串子程序*/
    private String returnText(String nDate){
        //判断日期是否发生变更，没有变更则直接返回缓存
        if(!nDate.equals(lDate)){
            //初始化时间
            lunar.init(System.currentTimeMillis());
            //Lunar类中的返回农历文本组合
            lunarText = lunar.getFormattedDate(Main._lockscreen_custom_format, Main._lockscreen_format);
            if("".equals(lunarText))
                return nDate;
            //根据锁屏布局选项设置输出文本
            switch(Main._lockscreen_layout){
                //不换行
                case 1: lunarText = nDate + " " + lunarText;
                    break;
                //前换行
                case 2: lunarText = nDate.trim() + "\n" + lunarText;
                    break;
                //后换行
                case 3: lunarText = nDate + " " + lunarText + "\n";
                    break;
                //前后都换行
                case 4: lunarText = nDate.trim() + "\n" + lunarText + "\n";
                    break;
            }
            lDate = nDate;
        }
        return lunarText;
    }

    /*替换日期函数*/
    public void handleLoadPackage(final LoadPackageParam lpparam){
        //如果打开了锁屏农历
        if(Main._lockscreen){
            switch(Main._rom){
                //大多数android系统
                case 1:
                    //4.4之前的keyguard在android.policy.odex里面，即系统进程“android”中
                    if(lpparam.packageName.equals("android")){
                        if(Build.VERSION.SDK_INT <= 16) {
                            //4.1和之前的锁屏界面日期更新程序放在 /com/android/internal/policy/impl/KeyguardStatusViewManager.java(smali)里面
                            //在android进程中查找该类
                            Class<?> hookClass = XposedHelpers.findClass("com.android.internal.policy.impl.KeyguardStatusViewManager", null);
                            //Hook 这个类中的 refreshDate 函数
                            findAndHookMethod(hookClass, "refreshDate", new XC_MethodHook() {
                                @Override
                                //在该函数执行后执行
                                protected void afterHookedMethod(final MethodHookParam param){
                                    //从该类中获取该类已经定义好的变量，这里的mDateView即是KeyguardStatusViewManager.java中定义好的显示日期的TextView控件，以下的操作和Main.java中的操作类似
                                    mTextView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                    //如果修改锁屏布局，则先将其的singleLine属性去除
                                    if(Main._lockscreen_layout > 1){
                                        mTextView.setSingleLine(false);
                                    }
                                    mTextView.setText(returnText(mTextView.getText().toString()));
                                }
                            });
                        }else if(Build.VERSION.SDK_INT <= 18){
                            //4.2 4.3的锁屏界面日期更新程序放在 /com/android/internal/policy/impl/keyguard/KeyguardStatusView.java(smali)里面
                            Class<?> hookClass = XposedHelpers.findClass("com.android.internal.policy.impl.keyguard.KeyguardStatusView", null);
                            findAndHookMethod(hookClass, "refreshDate", new XC_MethodHook() {
                                @SuppressLint("NewApi")
                                @Override
                                protected void afterHookedMethod(final MethodHookParam param){
                                    mTextView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                    if(Main._lockscreen_layout > 1){
                                        mTextView.setSingleLine(false);
                                        //如果修改锁屏对其，则设置对齐（setTextAlignment仅在Android4.2+上有效）
                                        if(Main._lockscreen_alignment > 1){
                                            mTextView.setTextAlignment(Main._lockscreen_alignment);
                                        }
                                    }
                                    mTextView.setText(returnText(mTextView.getText().toString()));
                                }
                            });
                        }
                    //4.4之后keyguard独立为一个apk，所以不再查找“android”核心进程，去匹配包名“com.android.keyguard”
                    }else if(lpparam.packageName.equals("com.android.keyguard")){
                        findAndHookMethod("com.android.keyguard.KeyguardStatusView", lpparam.classLoader, "refresh", new XC_MethodHook() {
                            @SuppressLint("NewApi")
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param){
                                //4.4新增了TextClock控件
                                mTextClock = (TextClock) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                if(Main._lockscreen_layout > 1){
                                    mTextClock.setSingleLine(false);
                                    if(Main._lockscreen_alignment > 1){
                                        mTextView.setTextAlignment(Main._lockscreen_alignment);
                                    }
                                }
                                mTextClock.setText(returnText(mTextClock.getText().toString()));
                            }
                        });
                    }
                    break;
                case 2:
                    break;
            }
            /* Samsung touchwiz 4.4 hook 能找到，但是更无法显示
            if(lpparam.packageName.equals("com.android.keyguard")){
                findAndHookMethod("com.android.keyguard.sec.SecKeyguardClock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param){
                        textview = (TextView) XposedHelpers.getObjectField(param.thisObject, "mSingleDate");
                        nDate = (String) textview.getText().toString();
                        textview.setText(nDate + " - " + returnDate(nDate));
                        XposedBridge.log("2");
                        //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
                    }
                });
            }
            */
        }
    }
}