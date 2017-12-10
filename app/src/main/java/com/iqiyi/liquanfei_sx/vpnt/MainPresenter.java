package com.iqiyi.liquanfei_sx.vpnt;

import android.content.Intent;
import android.net.VpnService;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.iqiyi.liquanfei_sx.vpnt.packet.ClientService;
import com.iqiyi.liquanfei_sx.vpnt.packet.LocalPackets;
import com.iqiyi.liquanfei_sx.vpnt.packet.ServerService;
import com.iqiyi.liquanfei_sx.vpnt.tools.Filter;
import com.iqiyi.liquanfei_sx.vpnt.view.ViewStub;

import java.util.ArrayList;


public class MainPresenter extends CommonPresenter implements View.OnClickListener,AdapterView.OnItemSelectedListener,TextWatcher{

    private ViewPager mViewPager;
    private ViewStub mStub;
    private PagerAdapter mPagerAdapter;
    private TabLayout mTab;
    private FloatingActionButton mButton_start;
    private AppCompatSpinner mSpinner;
    private EditText mEditFilter;

    private boolean mPaused=false;

    private AppCompatActivity mActivity;

    public MainPresenter(AppCompatActivity activity)
    {
        mActivity=activity;
    }

    @Override
    protected void onViewBind(View v) {
        mStub=(ViewStub) v.findViewById(R.id.pager_main);
        mTab=(TabLayout)v.findViewById(R.id.tab_main);
        mButton_start=(FloatingActionButton)v.findViewById(R.id.fab_start);
        mSpinner=(AppCompatSpinner)v.findViewById(R.id.main_spinner);
        mEditFilter=(EditText) v.findViewById(R.id.edit_filter);
        mSpinner.setAdapter(new FilterSpinnerAdapter(new String[]{"按应用","按包名","按目的ip","按目的端口","按源端口","按目的ip+目的端口","按目的ip+源端口"}));
        mSpinner.setOnItemSelectedListener(this);
        mEditFilter.addTextChangedListener(this);
        mButton_start.setOnClickListener(this);
    }

    private void startService()
    {
        final Intent i= VpnService.prepare(mActivity);

        if (i==null)
        {
            startServiceUnCheck();
        }else
        {
            mActivity.startActivityForResult(i,1);
        }
    }

    void startServiceUnCheck()
    {
        if (!ClientService.isRun())
            mActivity.startService(new Intent(mActivity,ServerService.class));
        mActivity.startService(new Intent(mActivity,ClientService.class));
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.fab_start)
        {
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
            mViewPager=(ViewPager) mStub.load(MApp.get().packetContent());
            mTab.setupWithViewPager(mViewPager);
            mButton_start.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mButton_start.setVisibility(View.GONE);
        ViewStub.replace(mViewPager,mStub);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        LocalPackets.get().addFilterKey(Filter.BY_NAME,s.toString());
        MApp.get().notifyFilterChanged();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
