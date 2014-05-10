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
    private static String lDate = ""; //上次记录的日期
    private static String nDate;
    private static String finalText; //最终输出文本
    private static String year; //记录年份
    private static Boolean _layout_run = false; //判断是否设置过singleLine属性
    private final static Pattern reg = Pattern.compile("\\n");
    private static TextView textview;
    private static String term;
    private static String fest;
    private static String custom;
    private static String sfest;
    private static String sfest_custom;

    /* 读取设置 */
    //使用xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    //设置变量记录读取设置
    protected final static Boolean _remove = prefs.getBoolean("remove", true);
    protected final static Boolean _term = prefs.getBoolean("term", true);
    protected final static Boolean _fest = prefs.getBoolean("fest", true);
    protected final static Boolean _custom = prefs.getBoolean("custom", false);
    protected final static Boolean _solar = prefs.getBoolean("solar", true);
    protected final static Boolean _solar_custom = prefs.getBoolean("solar_cutom", true);
    protected final static Boolean _breakline = prefs.getBoolean("breakline", true);
    protected final static Boolean _layout_enable = prefs.getBoolean("layout_enable", false);
    protected final static Boolean _lockscreen = prefs.getBoolean("lockscreen", false);
    protected final static int _minor = Integer.valueOf(prefs.getString("minor", "1")).intValue();
    protected final static int _lang = Integer.valueOf(prefs.getString("lang", "1")).intValue();
    protected final static int _year = Integer.valueOf(prefs.getString("year", "1")).intValue();
    protected final static int _rom = Integer.valueOf(prefs.getString("rom", "1")).intValue();
    protected final static String[] _clf = {
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
    protected final static String[] _csf = {
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
    
    //获取农历字符串子程序
    private String returnDate(String nDate){
        /* 判断当前日期栏是否包含上次更新后的日期文本
         * 1 如果包含,则说明原生updateClock()没有被执行，不用再去操作
         * 2 如果不包含，则说明需要重新写入TextView
         *  2.1 如果当前日期已经改变，则必须重新计算农历
         *  2.2 如果当前日期未改变，则只需要重新用已经缓存的文本写入TextView */
        if(!nDate.contains(lunarText)){
            //判断日期是否改变，不改变则不更新内容，改变则重新计算农历
            if (!nDate.equals(lDate)) {
                //获取时间
                lunar.init(System.currentTimeMillis());

                //修正layout的singleLine属性
                if(!_layout_run){
                    //去掉singleLine属性
                    if(prefs.getBoolean("layout_line", false)){
                        textview.setSingleLine(false);
                    }
                    //去掉align_baseline，并将其设置为center_vertical
                    if(prefs.getBoolean("layout_align", false)){
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                        layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        textview.setLayoutParams(layoutParams); 
                    }
                    //设置宽度为fill_parent
                    if(prefs.getBoolean("layout_width", false)){
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                        layoutParams.width = -1;
                        textview.setLayoutParams(layoutParams);
                    }
                    _layout_run = true;
                }
                
                //判断是否是公里节日
                if (_solar && (!"".equals(lunar.getSFestivalName()))){
                	sfest = " " + lunar.getSFestivalName();
                }else{
                    sfest = "";
                }
                
                //判断是否是农历节日
                if (_fest && (!"".equals(lunar.getLFestivalName()))){
                	fest = " " + lunar.getLFestivalName();
                }else{
                    fest = "";
                }

                //判断是否是二十四节气
                if (_term && (!"".equals(lunar.getTermString()))){
                    term = " " + lunar.getTermString();
                }else{
                    term = "";
                }
                
                //判断是否是自定义农历节日
                if (_custom && (!"".equals(lunar.getCLFestivalName()))){
                	custom = "，" + lunar.getCLFestivalName();
                }else{
                    custom = "";
                }
                
                //判断是否是自定义公历节日
                if (_solar_custom && (!"".equals(lunar.getCSFestivalName()))){
                	sfest_custom = "，" + lunar.getCSFestivalName();
                }else{
                    sfest_custom = "";
                }

                //根据设置设置年份
                switch(_year){
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
                if(_lang != 3){
                	lunarText =  year + lunar.getLunarMonthString() + "月" + lunar.getLunarDayString() + term  + fest + custom + sfest + sfest_custom;
                }else{
                	lunarText = "[" + lunar.getLunarDay() + "/" + lunar.getLunarMonth() + "]";
                }
                
                //更新记录的日期
                lDate = nDate;
                //输出到最终字符串
                finalText = nDate + breaklineText + lunarText;
                //如果需要去换行
                if(_remove){
                    Matcher mat = reg.matcher(finalText);
                    finalText = mat.replaceFirst(" ");
                }
            }
        }
        return finalText;
    }
  
    //替换日期函数
    public void handleLoadPackage(final LoadPackageParam lpparam){
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        //将决定是否换行的文本输出到字符串中
        if(!_breakline){
            breaklineText = " ";
        }
        
        //如果打开了调整布局，则允许进入调整布局步骤
        if(!_layout_enable){
            _layout_run = true;
        }
        
        
        switch(_rom){
        	case 1: 
	    	    try{
	    	    	//For most android roms
	    	        //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
	    	        findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
	    	            @Override
	    	            protected void afterHookedMethod(MethodHookParam param){
	    	                //获取原文字
	    	                textview = (TextView) param.thisObject;    
	    	                nDate = textview.getText().toString();
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
		            findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "a", new XC_MethodHook() {
		                @Override
		                protected void afterHookedMethod(MethodHookParam param){
		                    //获取原文字
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