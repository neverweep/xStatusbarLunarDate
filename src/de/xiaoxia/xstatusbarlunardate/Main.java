package de.xiaoxia.xstatusbarlunardate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.TextView;

//导入xposed基本类
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.xiaoxia.xstatusbarlunardate.Lunar;

/* Main */
public class Main implements IXposedHookLoadPackage {

    /* 初始变量 */
    private String lunarText = "LUNAR"; //记录最后更新时的文字字符串
    private String breaklineText = "\n"; //是否换行的文本
    private String lDate = ""; //上次记录的日期
    private String finalText;
    private Pattern reg = Pattern.compile("\\n");

    /* 读取设置 */
    //使用xposed提供的XSharedPreferences方法来读取android内置的SharedPreferences设置
    private XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    //设置变量记录读取设置
    private final Boolean _remove = prefs.getBoolean("remove", true);
    private final Boolean _term = prefs.getBoolean("term", true);
    private final Boolean _fest = prefs.getBoolean("fest", true);
    private final String _minor = prefs.getString("minor", "1");

    //初始话Lunar类
    private Lunar lunar = new Lunar();
    

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        //将决定是否换行的文本输出到字符串中
        if(prefs.getBoolean("breakline", true) == false){
            breaklineText = "  ";
        }
        
        //设置语言
        if(prefs.getString("lang", "1").equals("1")){
            lunar.changeLocale(false);
        }else{
        	lunar.changeLocale(true);
        }

        //勾在com.android.systemui.statusbar.policy.DateView里面的updateClock()之后
        findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //获取原文字
                TextView textview = (TextView) param.thisObject;
                String nDate = textview.getText().toString();

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

                        //判断是否是农历节日
                        String fest = " " + lunar.getLFestivalName();
                        if ((_fest == true) && (!"".equals(fest))){
                            if(fest.equals(" 小年")){
                                if((lunar.getLunarDay() == 23 && "1".equals(_minor)) || (lunar.getLunarDay() == 24 && "2".equals(_minor))  || (lunar.getLunarDay() == 25 && "3".equals(_minor))){
                                }else{
                                    fest = " ";
                                }
                            }
                        }else{
                            fest = " ";
                        }

                        //判断是否是二十四节气
                        String term = " " + lunar.getTermString();
                        if ((_term == true) && (!"".equals(term))){
                            term = " " + lunar.getTermString();
                        }else{
                            term = " ";
                        }

                        //组合农历文本
                        lunarText = lunar.getAnimalString() + "年" + lunar.getLunarMonthString() + "月" + lunar.getLunarDayString() + fest + term;
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
                    //更新TextView
                    textview.setText(finalText);
                }
            }
        });
    }
}