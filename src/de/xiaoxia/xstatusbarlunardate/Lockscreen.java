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

// 锁屏日期

package de.xiaoxia.xstatusbarlunardate;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextClock;
import android.widget.TextView;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Lockscreen implements IXposedHookLoadPackage{

    /* 初始变量 */
    private String lunarText = "LUNAR";
    private String lDate = "LAST";
    private Lunar lunar;
    private TextClock mTextClock = null;
    private TextView mTextView = null;
    private Context mContext;

    /*获取农历字符串子程序*/
    private String returnText(String nDate){
        //判断日期是否发生变更，没有变更则直接返回缓存
        if(!nDate.equals(lDate)){
            //初始化时间
            lunar.init();
            //Lunar类中的返回农历文本组合
            lunarText = lunar.getFormattedDate(Main._lockscreen_custom_format, Main._lockscreen_format);
            if("".equals(lunarText))
                return nDate;
            //根据锁屏布局选项设置输出文本
            StringBuilder sb = new StringBuilder();
            switch(Main._lockscreen_layout){
                //不换行
                case 1:
                    sb.append(nDate);
                    sb.append(" ");
                    sb.append(lunarText);
                    break;
                //前换行
                case 2:
                    sb.append(nDate.trim());
                    sb.append("\n");
                    sb.append(lunarText);
                    break;
                //后换行
                case 3:
                    sb.append(nDate);
                    sb.append(" ");
                    sb.append(lunarText);
                    sb.append("\n");
                    break;
                //前后都换行
                case 4:
                    sb.append(nDate.trim());
                    sb.append("\n");
                    sb.append(lunarText);
                    sb.append("\n");
                    break;
            }
            lunarText = sb.toString();
            lDate = nDate;
        }
        return lunarText;
    }

    /*替换日期函数*/
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam){
        //如果打开了锁屏农历
        if(Main._lockscreen){

            lunar = new Lunar(Main._lang);
            XposedBridge.log("run");
            switch(Main._rom){
                //大多数android系统
                case 1:
                    //4.4之前的keyguard在android.policy.odex里面，即系统进程“android”中
                    if(lpparam.packageName.equals("android")){
                        if(Build.VERSION.SDK_INT <= 16) {
                            //4.1和之前的锁屏界面日期更新程序放在 /com/android/internal/policy/impl/KeyguardStatusViewManager.java(smali)里面
                            //Hook 这个类中的 refreshDate 函数
                            findAndHookMethod("com.android.internal.policy.impl.KeyguardStatusViewManager", lpparam.classLoader, "refreshDate", new XC_MethodHook() {
                                @Override
                                //在该函数执行后执行
                                protected void afterHookedMethod(final MethodHookParam param){
                                    //从该类中获取该类已经定义好的变量，这里的mDateView即是KeyguardStatusViewManager.java中定义好的显示日期的TextView控件，以下的操作和Main.java中的操作类似
                                    mTextView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                    registerReceiver();
                                    //如果修改锁屏布局，则先将其的singleLine属性去除
                                    if(Main._lockscreen_layout > 1){
                                        try{
                                            mTextView.setSingleLine(false);
                                        }catch(Throwable t){
                                            XposedBridge.log("xStatusBarLunarDate: Lockscreen layout fix error(API 16)");
                                            XposedBridge.log(t);
                                        }
                                    }
                                    mTextView.setText(returnText(mTextView.getText().toString()));
                                }
                            });
                        }else if(Build.VERSION.SDK_INT <= 18){
                            //4.2 4.3的锁屏界面日期更新程序放在 /com/android/internal/policy/impl/keyguard/KeyguardStatusView.java(smali)里面
                            findAndHookMethod("com.android.internal.policy.impl.keyguard.KeyguardStatusView", lpparam.classLoader, "refreshDate", new XC_MethodHook() {
                                @SuppressLint("NewApi")
                                @Override
                                protected void afterHookedMethod(final MethodHookParam param){
                                    mTextView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                    registerReceiver();
                                    if(Main._lockscreen_layout > 1){
                                        mTextView.setSingleLine(false);
                                        //如果修改锁屏对其，则设置对齐（setTextAlignment仅在Android4.2+上有效）
                                        if(Main._lockscreen_alignment > 1){
                                            try{
                                                mTextView.setTextAlignment(Main._lockscreen_alignment);
                                            }catch(Throwable t){
                                                XposedBridge.log("xStatusBarLunarDate: Lockscreen layout fix error(API 18)");
                                                XposedBridge.log(t);
                                            }
                                        }
                                    }
                                    mTextView.setText(returnText(mTextView.getText().toString()));
                                }
                            });
                        }
                    //4.4之后keyguard独立为一个apk，所以不再查找“android”核心进程，去匹配包名“com.android.keyguard”
                    }else if(Build.VERSION.SDK_INT == 19 && lpparam.packageName.equals("com.android.keyguard")){
                        findAndHookMethod("com.android.keyguard.KeyguardStatusView", lpparam.classLoader, "refresh", new XC_MethodHook() {
                            //4.4+的TextClock变化是onTimeChanged的，所以这里监听refresh没有用，只是为了获取到mTextClock的实例对象。
                            @SuppressLint("NewApi")
                            @Override
                            protected void beforeHookedMethod(final MethodHookParam param){
                                //4.4新增了TextClock控件
                                mTextClock = (TextClock) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                if(mTextClock != null){
                                    try{
                                        mTextClock.removeTextChangedListener(textOnChanged);
                                    } catch (Throwable t) {}
                                    mTextClock.addTextChangedListener(textOnChanged);
                                }
                                registerReceiver();
                                if(Main._lockscreen_layout > 1){
                                    try{
                                        mTextClock.setSingleLine(false);
                                        if(Main._lockscreen_alignment > 1){
                                            mTextClock.setTextAlignment(Main._lockscreen_alignment);
                                        }
                                    }catch(Throwable t){
                                        XposedBridge.log("xStatusBarLunarDate: Lockscreen layout fix error(API 19)");
                                        XposedBridge.log(t);
                                    }
                                }
                            }
                        });
                    //5.0之后的锁屏画面并入了systemui
                    }else if(Build.VERSION.SDK_INT >= 20 && lpparam.packageName.equals("com.android.systemui")){
                        findAndHookMethod("com.android.keyguard.KeyguardStatusView", lpparam.classLoader, "refresh", new XC_MethodHook() {
                            @SuppressLint("NewApi")
                            @Override
                            protected void beforeHookedMethod(final MethodHookParam param){
                                mTextClock = (TextClock) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                if(mTextClock != null){
                                    try{
                                        mTextClock.removeTextChangedListener(textOnChanged);
                                    } catch (Throwable t) {}
                                    mTextClock.addTextChangedListener(textOnChanged);
                                }
                                registerReceiver();
                                if(Main._lockscreen_layout > 1){
                                    try{
                                        mTextClock.setSingleLine(false);
                                        if(Main._lockscreen_alignment > 1){
                                            mTextClock.setTextAlignment(Main._lockscreen_alignment);
                                        }
                                    }catch(Throwable t){
                                        XposedBridge.log("xStatusBarLunarDate: Lockscreen layout fix error(API 20+)");
                                        XposedBridge.log(t);
                                    }
                                }
                            }
                        });
                    }
                    break;
                case 2:break;
                case 3:break;
            }
        }
    }

    //针对4.4+检测字符变化
    private TextWatcher textOnChanged = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable nString) {
            String oriString = mTextClock.getText().toString();
            if(!oriString.equals(lunarText)){
                mTextClock.setText(returnText(oriString));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };

    //注册接收器
    private void registerReceiver(){
        if(Main._lockscreen && mContext == null){
            mContext = mTextClock != null ? mTextClock.getContext() : mTextView.getContext();
            if(mContext != null){
                IntentFilter intent = new IntentFilter();
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
            context = mTextClock != null ? mTextClock.getContext() : mTextView.getContext();
            if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
                lDate = "RESET";
            }else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)){
                lunar = new Lunar(Main._lang);
                lDate = "RESET";
            }
        }
    };
}