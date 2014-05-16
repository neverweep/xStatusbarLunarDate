package de.xiaoxia.xstatusbarlunardate;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingCustomLunar extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	//初始化对象 lp
    EditTextPreference lp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_custom_lunar);

        //设置返回按钮
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //找到设置，并将其概括修改为当前设置option_name
        for(int i = 0; i < 15; i++){
            lp = (EditTextPreference)findPreference("custom_lunar_item_" + i);
            if(!"".equals(lp.getText()) && lp.getText() != null)
                lp.setSummary(lp.getText());
            lp.setTitle(getString(R.string.custom_lunar) + " " + (i + 1));
        }

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    //监听到sharedPreferences变化后的处理
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设置发生变化时，设置summary为option_name
        for(int i = 0; i < 15; i++){
            if(key.equals("custom_lunar_item_" + i)){
                lp = (EditTextPreference)findPreference("custom_lunar_item_" + i);
                if(!"".equals(lp.getText()) && lp.getText() != null){
                	//如果该选项储存值不为空字符串，且不为空，则将其summary设置为储存的内容
                    lp.setSummary(lp.getText());
                }else{
                	//否则显示“未设置”
                    lp.setSummary(getString(R.string.custom_lunar_summary));
                }
                break;
            }
        }
    }
}
