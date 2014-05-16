package de.xiaoxia.xstatusbarlunardate;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.TextClock;
import android.widget.TextView;

//导入xposed基本类
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Lockscreen implements IXposedHookLoadPackage{

    /* 初始变量 */
    private String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private String lDate = "LAST";
    private String nDate;
    private Lunar lunar = new Lunar(Main._lang);
    private TextClock textclock;
    private TextView textview;


    /*获取农历字符串子程序*/
    private String returnDate(String nDate){
        //判断日期是否发生变更，没有变更则直接返回缓存
        if(!nDate.equals(lDate)){
            lunar.init(System.currentTimeMillis());
            lunarText = lunar.getComboText();
            //根据锁屏布局选项设置输出文本
            switch(Main._lockscreen_layout){
                case 1: lunarText = nDate + " - " + lunarText;
                    break;
                case 2: lunarText = nDate.trim() + "\n" + lunarText;
                    break;
                case 3: lunarText = nDate + " - " + lunarText + "\n";
                    break;
                case 4: lunarText = nDate.trim() + "\n" + lunarText + "\n";
                    break;
            }
            lDate = nDate;
            //XposedBridge.log("Calculating lunar date: @" + System.currentTimeMillis());
        }
        return lunarText;
    }

    /*替换日期函数*/
    public void handleLoadPackage(final LoadPackageParam lpparam){  
        if(Main._lockscreen){
            switch(Main._rom){
                //大多数android系统
                case 1:
                    //XposedBridge.log(lpparam.packageName);
                    //4.4之前的keyguard在android.policy.odex里面
                    if(lpparam.packageName.equals("android")){
                        try{
                            if(Build.VERSION.SDK_INT <= 16) {
                                //XposedBridge.log("SDK 15-16");
                                Class<?> hookClass = XposedHelpers.findClass("com.android.internal.policy.impl.KeyguardStatusViewManager", null);
                                XposedHelpers.findAndHookMethod(hookClass, "refreshDate", new XC_MethodHook() {

                                    @Override
                                    protected void afterHookedMethod(final MethodHookParam param){
                                        textview = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                        nDate = (String) textview.getText().toString();
                                        if(Main._lockscreen_layout > 1){
                                            textview.setSingleLine(false);
                                        }
                                        textview.setText(returnDate(nDate));
                                        //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
                                    }
                                });
                            }else if(Build.VERSION.SDK_INT <= 18){
                                //XposedBridge.log("SDK 17-18");
                                Class<?> hookClass = XposedHelpers.findClass("com.android.internal.policy.impl.keyguard.KeyguardStatusView", null);
                                XposedHelpers.findAndHookMethod(hookClass, "refreshDate", new XC_MethodHook() {
                                    
                                    @SuppressLint("NewApi")
                                    @Override
                                    protected void afterHookedMethod(final MethodHookParam param){
                                        textview = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                        if(Main._lockscreen_layout > 1){
                                            textview.setSingleLine(false);
                                            if(Main._lockscreen_alignment > 1){
                                                textview.setTextAlignment(Main._lockscreen_alignment);
                                            }
                                        }
                                        nDate = (String) textview.getText().toString();
                                        textview.setText(returnDate(nDate));
                                        //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
                                    }
                                });
                            }
                        }catch(Exception e){
                            //Do nothing
                        }
                    //4.4之后keyguard独立为一个apk
                    }else if(lpparam.packageName.equals("com.android.keyguard")){
                        try{
                            //XposedBridge.log("SDK 19");
                            findAndHookMethod("com.android.keyguard.KeyguardStatusView", lpparam.classLoader, "refresh", new XC_MethodHook() {

                                @SuppressLint("NewApi")
                                @Override
                                protected void afterHookedMethod(final MethodHookParam param){
                                    //4.4新增了TextClock类
                                    textclock = (TextClock) XposedHelpers.getObjectField(param.thisObject, "mDateView");
                                    if(Main._lockscreen_layout > 1){
                                        textclock.setSingleLine(false);
                                        if(Main._lockscreen_alignment > 1){
                                            textview.setTextAlignment(Main._lockscreen_alignment);
                                        }
                                    }
                                    nDate = (String) textclock.getText().toString();
                                    textclock.setText(returnDate(nDate));
                                    //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
                                }
                            });
                        }catch(Exception e){
                            //Do nothing
                        }
                    }
                    break;
                case 2:
                    break;
            }
            /* Samsung touchwiz 4.4 hook 能找到，但是更无法显示
            if(lpparam.packageName.equals("com.android.keyguard")){
                try{
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
                }catch(Exception e){
                    //Do nothing
                }
            }
            */
        }
    }
}