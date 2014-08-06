/**
 * Copyright (C) 2014 xiaoxia.de
 *
 * @author xiaoxia.de
 * @date 2014
 * @license MIT
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 *
 */

// 主设置界面

package de.xiaoxia.xstatusbarlunardate;

import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class Setting extends PreferenceActivity implements OnSharedPreferenceChangeListener{

    ListPreference lp;
    ListPreference _lp;
    EditTextPreference etp;

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

        lp = (ListPreference)findPreference("format");
        lp.setSummary(lp.getEntry());
        etp = (EditTextPreference)findPreference("custom_format");
        etp.setEnabled(lp.getValue().toString().equals("5"));
        if(!"".equals(etp.getText()) && etp.getText() != null){
            etp.setSummary(etp.getText());
        }else{
            etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
        }

        lp = (ListPreference)findPreference("lockscreen_format");
        lp.setSummary(lp.getEntry());
        etp = (EditTextPreference)findPreference("lockscreen_custom_format");
        etp.setEnabled(lp.getValue().toString().equals("5"));
        if(!"".equals(etp.getText()) && etp.getText() != null){
            etp.setSummary(etp.getText());
        }else{
            etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
        }

        lp = (ListPreference)findPreference("rom");
        lp.setSummary(lp.getEntry());

        lp = (ListPreference)findPreference("lockscreen_layout");
        lp.setSummary(lp.getEntry());

        lp = (ListPreference)findPreference("notify");
        lp.setSummary(lp.getEntry());

        lp = (ListPreference)findPreference("notify_times");
        lp.setSummary(lp.getEntry());

        _lp = (ListPreference)findPreference("lockscreen_alignment");
        if(Build.VERSION.SDK_INT < 17){
            //Android SDK 版本小于4.2时，显示summary为不可用，并将其设为不可用
            _lp.setSummary(getString(R.string.lockscreen_alignment_disable));
            _lp.setEnabled(false);
        }else{
            //否则...
            //如果lockscreen_layout值不为“1”，即不为不调整布局，则对齐选项设为不可用
            _lp.setEnabled(!lp.getValue().toString().equals("1"));
            _lp.setSummary(_lp.getEntry());
        }

        //监听sharedPreferences变化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //注册监听事件
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
        if(key.equals("format")){
            lp = (ListPreference)findPreference("format");
            lp.setSummary(lp.getEntry());
            etp = (EditTextPreference)findPreference("custom_format");
            etp.setEnabled(lp.getValue().toString().equals("5"));
            return;
        }
        if(key.equals("lockscreen_format")){
            lp = (ListPreference)findPreference("lockscreen_format");
            lp.setSummary(lp.getEntry());
            etp = (EditTextPreference)findPreference("lockscreen_custom_format");
            etp.setEnabled(lp.getValue().toString().equals("5"));
            return;
        }
        if(key.equals("rom")){
            lp = (ListPreference)findPreference("rom");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("notify")){
            lp = (ListPreference)findPreference("notify");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("notify_times")){
            lp = (ListPreference)findPreference("notify_times");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("lockscreen_alignment")){
            lp = (ListPreference)findPreference("lockscreen_alignment");
            lp.setSummary(lp.getEntry());
            return;
        }
        if(key.equals("lockscreen_layout")){
            lp = (ListPreference)findPreference("lockscreen_layout");
            lp.setSummary(lp.getEntry());
            _lp = (ListPreference)findPreference("lockscreen_alignment");
            if(Build.VERSION.SDK_INT < 17){
                _lp.setSummary(getString(R.string.lockscreen_alignment_disable));
                _lp.setEnabled(false);
            }else{
                _lp.setEnabled(!lp.getValue().toString().equals("1"));
            }
            return;
        }
        if(key.equals("custom_format")){
            etp = (EditTextPreference)findPreference("custom_format");
            if(!"".equals(etp.getText()) && etp.getText() != null){
                etp.setSummary(etp.getText());
            }else{
                etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
            }
            return;
        }
        if(key.equals("lockscreen_custom_format")){
            etp = (EditTextPreference)findPreference("lockscreen_custom_format");
            if(!"".equals(etp.getText()) && etp.getText() != null){
                etp.setSummary(etp.getText());
            }else{
                etp.setSummary(getString(R.string.setting_custom_solar_item_summary));
            }
            return;
        }
    }

    //创建ActionBar右上角按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about, menu); //菜单选项调用 /menu/about.xml
        return true;
    }

    //按钮点击行为，因为没有二级按钮，不需要判断点击内容
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //点击Info按钮后，新建一个AlertDialog显示关于信息
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.about, null); //使用 /layout/about.xml 作为输出布局
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); //建立对话框
        builder.setIcon(R.drawable.ic_launcher); //图标资源调用 /drawable/ic_launcher.png
        builder.setTitle(R.string.about); //标题设为 @string/about
        builder.setView(textEntryView); //设置布局
        builder.setPositiveButton(R.string.ok, null); //设置按钮，仅设置一个确定按钮
        builder.show(); //显示对话框
        return true;
    }
}
