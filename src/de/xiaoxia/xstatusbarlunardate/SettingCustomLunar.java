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
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);

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
