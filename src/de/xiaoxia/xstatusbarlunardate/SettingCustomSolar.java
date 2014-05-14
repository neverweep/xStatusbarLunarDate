package de.xiaoxia.xstatusbarlunardate;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingCustomSolar extends PreferenceActivity implements OnSharedPreferenceChangeListener{

    EditTextPreference lp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_custom_solar);

        //设置返回按钮
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //找到设置，并将其概括修改为当前设置option_name
        for(int i = 0; i < 15; i++){
            lp = (EditTextPreference)findPreference("custom_solar_item_" + i);
            if(!"".equals(lp.getText()) && lp.getText() != null)
                lp.setSummary(lp.getText());
            lp.setTitle(getString(R.string.custom_solar) + " " + (i + 1));
        }

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //设置发生变化时，设置summary为option_name
        for(int i = 0; i < 15; i++){
            if(key.equals("custom_solar_item_" + i)){
                lp = (EditTextPreference)findPreference("custom_solar_item_" + i);
                if(!"".equals(lp.getText()) && lp.getText() != null){
                    lp.setSummary(lp.getText());
                }else{
                    lp.setSummary(getString(R.string.custom_solar_summary));
                }
                break;
            }
        }
    }
}
