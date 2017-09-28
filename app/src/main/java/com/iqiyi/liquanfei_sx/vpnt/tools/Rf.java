package com.iqiyi.liquanfei_sx.vpnt.tools;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by admin on 2017/3/2.
 */

public class Rf {

    public static Object readField(Object o,String name)
    {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(o);
        }catch (Exception e)
        {

            Field f = null;
            try {
                f = o.getClass().getField(name);
                f.setAccessible(true);
                return f.get(o);
            } catch (Exception e1) {
                Log.e("xx",e1.toString());
            }
            //if(f==null)
            Log.e("xx",e.toString());

        }
        return null;
    }

    public static Object readField(Class r,Object o,String name)
    {
        try {
            Field f = r.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(o);
        }catch (Exception e)
        {
            Log.e("xx",e.toString());
        }
        return null;
    }

    public static void setField(Object o,String name,Object x)
    {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(o,x);
        }catch (Exception e)
        {
            Field f = null;
            try {
                f = o.getClass().getField(name);
                f.setAccessible(true);
                f.set(o,x);
            } catch (Exception e1) {
                Log.e("xx","in "+o.getClass().getName()+":"+e1.toString());
            }
            //if(f==null)
                Log.e("xx","in "+o.getClass().getName()+":"+e.toString());
        }
    }

    public static void setField(Class r,Object o,String name,Object x)
    {
        try {
            Field f = r.getDeclaredField(name);
            f.setAccessible(true);
            f.set(o,x);
        }catch (Exception e)
        {
            Log.e("xx",e.toString());
        }
    }

    public static Object invoke(Class r,Object o,String name,Class []vp,Object ...params)
    {Method m=null;
        try {
            m = r.getDeclaredMethod(name,vp);
            m.setAccessible(true);
            return m.invoke(o,params);
        }catch (InvocationTargetException e)
        {
            Throwable ee=e.getCause();
            while (ee!=null) {
                Log.e("xx", "in " + m.getName() +":"+ ee.toString());
                ee=ee.getCause();
            }
        } catch (NoSuchMethodException e) {
            Log.e("xx",r.toString()+":"+e.toString());
        } catch (IllegalAccessException e) {
            Log.e("xx",e.toString());
        }
        return null;
    }

    public static Object invoke(Object o,String name,Class []vp,Object ...params)
    {Method m=null;
        Class r=o.getClass();
        try {
            m = r.getDeclaredMethod(name,vp);
            m.setAccessible(true);
            return m.invoke(o,params);
        }catch (InvocationTargetException e)
        {
            Log.e("xx","in"+m.getName()+e.getCause().toString());
        } catch (NoSuchMethodException e) {
            try {
                m =r.getMethod(name,vp);
                m.setAccessible(true);
                return m.invoke(o,params);
            } catch (Exception e1) {
                Log.e("xx",e1.toString());
            }
            //if(m==null)
                Log.e("xx",r.toString()+":"+e.toString());
        } catch (IllegalAccessException e) {
            Log.e("xx",e.toString());
        }
        return null;
    }

    public static Object invoke(Object o,String name)
    {
        Class r=o.getClass();
        Method m=null;
        try {
            m = r.getDeclaredMethod(name,new Class[]{});
            m.setAccessible(true);
            return m.invoke(o);
        }catch (InvocationTargetException e)
        {
            Log.e("xx","in"+m.getName()+e.getCause().toString());
        } catch (NoSuchMethodException e) {
            try {
                m =r.getMethod(name,new Class[]{});
                m.setAccessible(true);
                return m.invoke(o);
            } catch (Exception e1) {
                Log.e("xx",e1.toString());
            }
            //if(m==null)
                Log.e("xx",r.toString()+":"+e.toString());
            Log.e("xx",r.toString()+":"+e.toString());
        } catch (IllegalAccessException e) {
            Log.e("xx",e.toString());
        }
        return null;
    }

    public static Object invoke(Class r,Object o,String name)
    {Method m=null;
        try {
            m = r.getDeclaredMethod(name,new Class[]{});
            m.setAccessible(true);
            return m.invoke(o);
        }catch (InvocationTargetException e)
        {
            Log.e("xx","in"+m.getName()+e.getCause().toString());
        } catch (NoSuchMethodException e) {
            Log.e("xx",r.toString()+":"+e.toString());
        } catch (IllegalAccessException e) {
            Log.e("xx",e.toString());
        }
        return null;
    }

    public static void p(Method m,InvocationTargetException e)
    {
        Throwable ee=e.getCause();
        while (ee!=null) {
            Log.e("xx", "in " + m.getName() +":"+ ee.toString());
            ee=ee.getCause();
        }
    }

    public static void p(String m,InvocationTargetException e)
    {
        Throwable ee=e.getCause();
        while (ee!=null) {
            Log.e("xx", "in " + m +":"+ ee.toString());
            ee=ee.getCause();
        }
    }
}
