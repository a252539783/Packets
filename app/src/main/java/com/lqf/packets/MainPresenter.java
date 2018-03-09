package com.lqf.packets;

import android.content.Intent;
import android.net.VpnService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.lqf.packets.packet.ClientService;
import com.lqf.packets.packet.LocalPackets;
import com.lqf.packets.packet.ServerService;
import com.lqf.packets.tools.Filter;
import com.lqf.packets.view.ViewStub;


public class MainPresenter extends CommonPresenter implements View.OnClickListener,AdapterView.OnItemSelectedListener,TextWatcher,MApp.OnDispatchResourceListener{

    private ViewPager mViewPager;
    private ViewStub mStub;
    private TabLayout mTab;
    private FloatingActionButton mButton_start;
    private AppCompatSpinner mSpinner;
    private EditText mEditFilter;
    private Toolbar mToolbar;

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
        mToolbar=(Toolbar)v.findViewById(R.id.tool_bar_main);

        mActivity.setSupportActionBar(mToolbar);
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
        MApp.get().getResource(MApp.RESOURCE_PACKET_PAGER,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mButton_start.setVisibility(View.GONE);
        ViewStub.replace(mViewPager,mStub);
        MApp.get().releaseResource(MApp.RESOURCE_PACKET_PAGER,mViewPager);
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

    @Override
    public void onDispatch(int resourceCode, Object resource) {
        mViewPager=(ViewPager) mStub.load((ViewPager)resource);
        mTab.setupWithViewPager(mViewPager);
        mButton_start.setVisibility(View.VISIBLE);
    }
}
