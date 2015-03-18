package com.yeepay.bigdata.crawler.crawl.dns;

import org.apache.commons.lang3.StringUtils;
import scala.actors.threadpool.Arrays;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSTest {

    public static void main(String[] args) throws UnknownHostException {

        testFactory();

        Class<InetAddress> clazz = InetAddress.class;
        Class primitive = int.class;

        String hostName = "www.163.com";
        // // resolve DNS
        InetAddress[] addresses = InetAddress.getAllByName(hostName);
        System.out.println(Arrays.toString(addresses));

        // InetAddress[] result = null;
        // try {
        // result = Address.getAllByName(hostName);
        // } catch (UnknownHostException e) {
        // e.printStackTrace();
        // }
        // System.out.println(Arrays.toString(result));
        //
        // try {
        // result = Address.getAllByName(hostName);
        // } catch (UnknownHostException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // System.out.println(result);
    }

    public static void reflect() {
        Class<InetAddress> inetAddress = InetAddress.class;
        Class<?>[] classes = inetAddress.getDeclaredClasses();
        for (Class<?> clazz : classes) {
            if (StringUtils.equals("java.net.InetAddressImplFactory", clazz.getName())) {
                System.out.println("find class : " + clazz.getName());
                try {
                    Constructor constructor = clazz.getDeclaredConstructor(inetAddress);
                    System.out.println(constructor.getName());
                    constructor.setAccessible(true);
                    Object factory = constructor.newInstance(InetAddress.getLocalHost());
                    System.out.println(factory.getClass());
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static void testFactory() {
        try {
            // InetAddress inetAddress = InetAddress.getLocalHost();
            Class.forName("java.net.InetAddress");// load native lib libnet.so
            // ClassLoader.getSystemClassLoader().loadClass("java.net.InetAddress");
            Class<?> clazz = Class.forName("java.net.InetAddressImplFactory");
            System.out.println(clazz.getName());
            Constructor<?> constructor = clazz.getDeclaredConstructor(null);
            System.out.println(constructor.getName());
            constructor.setAccessible(true);
            Object factory = constructor.newInstance(null);
            System.out.println(factory);

            // create InetAddressImpl
            Method create = clazz.getDeclaredMethod("create", null);
            create.setAccessible(true);
            Object impl = create.invoke(factory, null);
            System.out.println(impl.getClass());

            //
            Class<?> implClass = impl.getClass();
            Method lookupAllHostAddr = implClass.getDeclaredMethod("lookupAllHostAddr", String.class);
            lookupAllHostAddr.setAccessible(true);
            InetAddress[] addresses = (InetAddress[]) lookupAllHostAddr.invoke(impl, "www.qq.com");
            System.out.println(Arrays.toString(addresses));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
