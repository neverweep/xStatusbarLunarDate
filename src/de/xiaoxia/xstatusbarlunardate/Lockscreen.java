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
    private String lunarText; //记录最后更新时的文字字符串
    private String year; //记录年份
    private String lDate = "LastDate";
    private String nDate;
    private Lunar lunar = new Lunar(Main._lang);
    private TextClock textclock;
    private TextView textview;
    private String term;
    private String fest;
    private String custom;
    private String sfest;
    private String sfest_custom;

 
    //获取农历字符串子程序
    private String returnDate(String nDate){
    	//判断日期是否发生变更，没有变更则直接返回缓存
    	if(!nDate.equals(lDate)){
	    	lunar.init(System.currentTimeMillis());

            if (Main._solar && (!"".equals(lunar.getSFestivalName()))){
            	sfest = " " + lunar.getSFestivalName();
            }else{
                sfest = "";
            }
            
            //判断是否是农历节日
            if (Main._fest && (!"".equals(lunar.getLFestivalName()))){
            	fest = " " + lunar.getLFestivalName();
            }else{
                fest = "";
            }
	
            //判断是否是二十四节气
            if (Main._term && (!"".equals(lunar.getTermString()))){
                term = " " + lunar.getTermString();
            }else{
                term = "";
            }

            //判断是否是自定义农历节日
            if (Main._custom && (!"".equals(lunar.getCLFestivalName()))){
            	custom = "，" + lunar.getCLFestivalName();
            }else{
                custom = "";
            }
            
            if (Main._solar_custom && (!"".equals(lunar.getCSFestivalName()))){
            	sfest_custom = "，" + lunar.getCSFestivalName();
            }else{
                sfest_custom = "";
            }
            
	        //根据设置设置年份
	        switch(Main._year){
	            case 1:  year = lunar.getAnimalString() + "年";
	                break;
	            case 2:  year = lunar.getLunarYearString() + "年";
	                break;
	            case 3:  year = "";
	                break;
	            case 4:  year = lunar.getLunarYearString() + lunar.getAnimalString() + "年";
	                break;	        
	        }
	
	        //组合农历文本
	        if(Main._lang != 3){
	            lunarText =  year + lunar.getLunarMonthString() + "月" + lunar.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
	        }else{
	            lunarText = "[" + lunar.getLunarDay() + "/" + lunar.getLunarMonth() + "]";
	        }
	        lunarText = lunarText.trim();
	        lDate = nDate;
	        //XposedBridge.log("Calculating lunar date: @" + System.currentTimeMillis());
    	}
		return lunarText;
    }

    //替换日期函数
    public void handleLoadPackage(final LoadPackageParam lpparam){
    	if(Main._lockscreen){
    		//XposedBridge.log(lpparam.packageName);
	        if(lpparam.packageName.equals("android")){
	        	try{
			        if(Build.VERSION.SDK_INT <= 16) {
			        	//XposedBridge.log("SDK 15-16");
				        Class<?> kgStatusViewManagerClass = XposedHelpers.findClass("com.android.internal.policy.impl.KeyguardStatusViewManager", null);
				        XposedHelpers.findAndHookMethod(kgStatusViewManagerClass, "refreshDate", new XC_MethodHook() {
				        	//XposedBridge.log("Found 15-16");
				
				        	@Override
				            protected void afterHookedMethod(final MethodHookParam param){
				        		textview = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
				        		nDate = (String) textview.getText().toString();
			 	                textview.setText(nDate + " - " + returnDate(nDate));
			 	                //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
				            }
				        });
			        }else if(Build.VERSION.SDK_INT <= 18){
			        	//XposedBridge.log("SDK 17-18");
			        	Class<?> kgStatusViewManagerClass = XposedHelpers.findClass("com.android.internal.policy.impl.keyguard.KeyguardStatusView", null);
			 	        XposedHelpers.findAndHookMethod(kgStatusViewManagerClass, "refreshDate", new XC_MethodHook() {
			 	        	//XposedBridge.log("Found 17-18");
			 	
			 	        	@Override
			 	            protected void afterHookedMethod(final MethodHookParam param){
				        		textview = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDateView");
				        		nDate = (String) textview.getText().toString();
			 	                textview.setText(nDate + " - " + returnDate(nDate));
			 	                //XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
			 	            }
			 	        });
			        }
	        	}catch(Exception e){
	        		//Do nothing
	        	}
	        }else if(lpparam.packageName.equals("com.android.keyguard")){
	        	try{
	        		//XposedBridge.log("SDK 19");
		        	findAndHookMethod("com.android.keyguard.KeyguardStatusView", lpparam.classLoader, "refresh", new XC_MethodHook() {
		        		//XposedBridge.log("Found 19");
		
		        		@SuppressLint("NewApi")
		                @Override
		                protected void afterHookedMethod(MethodHookParam param){
			        		textclock = (TextClock) XposedHelpers.getObjectField(param.thisObject, "mDateView");
			        		nDate = (String) textclock.getText().toString();
			        		textclock.setText(nDate + " - " + returnDate(nDate));
			        		//XposedBridge.log("Hooking lunar date: @" + System.currentTimeMillis());
		                }
		        	});
	        	}catch(Exception e){
	        		//Do nothing
	        	}
	        }
    	}
    }
}