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

// 自定义公历界面

package de.xiaoxia.xstatusbarlunardate;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingCustomSolar extends PreferenceActivity implements OnSharedPreferenceChangeListener{

    EditTextPreference etp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_custom_solar);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        for(int i = 0; i < 20; i++){
            etp = (EditTextPreference)findPreference("custom_solar_item_" + i);
            if(!"".equals(etp.getText()) && etp.getText() != null)
                etp.setSummary(etp.getText());
            etp.setTitle(getString(R.string.custom_solar) + " " + (i + 1));
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for(int i = 0; i < 20; i++){
            if(key.equals("custom_solar_item_" + i)){
                etp = (EditTextPreference)findPreference("custom_solar_item_" + i);
                if(!"".equals(etp.getText()) && etp.getText() != null){
                    etp.setSummary(etp.getText());
                }else{
                    etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
                }
                break;
            }
        }
    }
}
