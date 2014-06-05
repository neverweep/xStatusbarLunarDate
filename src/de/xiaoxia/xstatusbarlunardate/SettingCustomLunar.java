/**
 * Copyright (C) 2014 xiaoxia.de
 * 
 * @author by xiaoxia.de
 * @date 2014
 * @license MIT
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

// 自定义农历界面

package de.xiaoxia.xstatusbarlunardate;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingCustomLunar extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	//初始化对象 etp
    EditTextPreference etp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_custom_lunar);

        //设置返回按钮
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //找到设置，并将其概括修改为当前设置option_name
        for(int i = 0; i < 20; i++){
            etp = (EditTextPreference)findPreference("custom_lunar_item_" + i);
            if(!"".equals(etp.getText()) && etp.getText() != null)
                etp.setSummary(etp.getText());
            etp.setTitle(getString(R.string.custom_lunar) + " " + (i + 1));
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
        for(int i = 0; i < 20; i++){
            if(key.equals("custom_lunar_item_" + i)){
                etp = (EditTextPreference)findPreference("custom_lunar_item_" + i);
                if(!"".equals(etp.getText()) && etp.getText() != null){
                	//如果该选项储存值不为空字符串，且不为空，则将其summary设置为储存的内容
                    etp.setSummary(etp.getText());
                }else{
                	//否则显示“未设置”
                    etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
                }
                break;
            }
        }
    }
}
