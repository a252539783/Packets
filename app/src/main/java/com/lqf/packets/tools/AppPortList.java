package com.lqf.packets.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

import com.lqf.packets.MApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Administrator on 2017/9/26.
 */

public class AppPortList {

    private static AppPortList instance=null;

    private final File _TCP=new File("/proc/net/tcp");
    private final File _TCP6=new File("/proc/net/tcp6");

    private PackageManager mPm;
    private SparseArray<AppInfo> mPkgList=new SparseArray<>();
    private SparseArray<AppInfo> mPortList=new SparseArray<>();

    private AppPortList(Context c)
    {
        mPm=c.getPackageManager();
        freshPkg();
    }

    private void freshPkg()
    {
        Log.e("freshPkg","freshPkg");
        List<PackageInfo> infos=mPm.getInstalledPackages(PackageManager.GET_GIDS);
        for (int i=0;i<infos.size();i++)
        {
            mPkgList.put(infos.get(i).applicationInfo.uid,new AppInfo(infos.get(i)));
        }
    }

    public void freshPort()
    {
        boolean inited=false;
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(_TCP)));
            String line=reader.readLine();
            while ((line=reader.readLine())!=null) {
                Scanner s=new Scanner(line);
                s.next();
                String ip=s.next();
                for (int i=2;i<7;i++)
                {
                    s.next();
                }
                int uid=Integer.parseInt(s.next());
                int port=Integer.parseInt(ip.split(":")[1],16);
                AppInfo ai=mPkgList.get(uid);
                if (ai==null)
                    continue;

                mPortList.put(port,ai);
            }

            reader=new BufferedReader(new InputStreamReader(new FileInputStream(_TCP6)));
            line=reader.readLine();
            while ((line=reader.readLine())!=null) {
                Scanner s=new Scanner(line);
                s.next();
                String ip=s.next();
                for (int i=2;i<7;i++)
                {
                    s.next();
                }
                int uid=Integer.parseInt(s.next());
                int port=Integer.parseInt(ip.split(":")[1],16);
                AppInfo ai=mPkgList.get(uid);
                if (ai==null)
                    continue;

                mPortList.put(port,ai);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }
    }

    public AppInfo getAppInfo(int port)
    {
        freshPort();
        return mPortList.get(port).loadIcon();
    }

    public AppInfo getAppByUid(int uid) {
        AppInfo ai = mPkgList.get(uid);

        //有可能应用被卸载了找不到
        if (ai == null)
            return null;

        ai.loadIcon();
        return ai;
    }

    public static void init()
    {
        instance=new AppPortList(MApp.get());
    }

    public static AppPortList get()
    {
        return instance;
    }

    public static Drawable getIcon(int uid)
    {
        return instance.mPkgList.get(uid).icon;
    }

    public static String getAppName(int uid)
    {
        return instance.mPkgList.get(uid).appName;
    }

    public class AppInfo
    {
        public PackageInfo info;
        public String appName;
        public Drawable icon;

        private AppInfo(PackageInfo info)
        {
            this.info=info;
            appName=info.applicationInfo.loadLabel(mPm).toString();
        }

        public AppInfo loadIcon()
        {
            if (icon!=null)
                return this;

            icon=info.applicationInfo.loadIcon(mPm);
            return this;
        }
    }

}
