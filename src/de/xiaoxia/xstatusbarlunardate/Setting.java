package de.xiaoxia.xstatusbarlunardate;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class Setting extends PreferenceActivity implements OnSharedPreferenceChangeListener{

    ListPreference lp;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        //找到设置，并将其概括修改为当前设置option_name
        lp = (ListPreference)findPreference("minor");
        lp.setSummary(lp.getEntry());

        lp = (ListPreference)findPreference("lang");
        lp.setSummary(lp.getEntry());
        
        lp = (ListPreference)findPreference("year");
        lp.setSummary(lp.getEntry());
        
        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	//设置发生变化时，设置summary为option_name
        if(key.equals("minor")){
        	lp = (ListPreference)findPreference("minor");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("lang")){
        	lp = (ListPreference)findPreference("lang");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("year")){
        	lp = (ListPreference)findPreference("year");
            lp.setSummary(lp.getEntry());
            return;
        }
    }
}
