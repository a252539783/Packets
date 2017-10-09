package com.iqiyi.liquanfei_sx.vpnt.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

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
    private SparseArray<PackageInfo> mPkgList=new SparseArray<>();
    private SparseArray<PackageInfo> mPortList=new SparseArray<>();
    private SparseArray<Drawable> mIcons=new SparseArray<>();

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
            mPkgList.put(infos.get(i).applicationInfo.uid,infos.get(i));
            mIcons.put(infos.get(i).applicationInfo.uid,infos.get(i).applicationInfo.loadIcon(mPm));
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
                PackageInfo pkgName=mPkgList.get(uid);
                if (pkgName==null)
                {
                    if (!inited)
                    {
                        freshPkg();
                        pkgName=mPkgList.get(uid);
                        inited=true;
                    }
                }

                if (pkgName==null)
                    continue;

                mPortList.put(port,pkgName);
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
                PackageInfo pkgName=mPkgList.get(uid);
                if (pkgName==null)
                {
                    if (!inited)
                    {
                        freshPkg();
                        pkgName=mPkgList.get(uid);
                        inited=true;
                    }
                }

                if (pkgName==null)
                    continue;

                mPortList.put(port,pkgName);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }
    }

    public PackageInfo getPkgInfo(int port)
    {
        freshPort();
        return mPortList.get(port);
    }

    public static void init(Context c)
    {
        instance=new AppPortList(c);
    }

    public static AppPortList get(Context c)
    {
        if (instance==null)
            init(c);
        return instance;
    }

    public static Drawable getIcon(int uid)
    {
        return instance.mIcons.get(uid);
    }

}
