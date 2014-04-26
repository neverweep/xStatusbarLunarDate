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

        //找到minor设置，并将其概括修改为strings.xml中的value加当前设置option_name
        lp = (ListPreference)findPreference("minor");
        lp.setSummary(this.getString(R.string.minor_current) + lp.getEntry());

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	//设置发生变化时，如果键值等于minor就将其概括修改为strings.xml中的value加当前设置option_name
        if(key.equals("minor")){
            lp.setSummary(this.getString(R.string.minor_current) + lp.getEntry());
        }
        return;
    }
}
