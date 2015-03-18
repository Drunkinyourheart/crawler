package com.yeepay.bigdata.crawler.crawl.dns;

import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;
import org.apache.http.conn.DnsResolver;
import org.apache.log4j.Logger;
import scala.actors.threadpool.Arrays;
import sun.misc.Service;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;
import sun.security.action.GetPropertyAction;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.util.Iterator;

/**
 * 类NonBlockingDNSResolver.java的实现描述：无同步方法实现
 */
public class NonBlockingDNSResolver implements DnsResolver, Dumpable {

    private static Logger logger = Logger.getLogger(NonBlockingDNSResolver.class);

    private static final DNSCache cache = new DNSCache();

    /**
     * InetAddressImpl is package privilige : so using reflection to access it
     */
    private static Object impl;

    private static Method lookupAllHostAddr;

    private static Method getHostByAddr;

    /* Used to store the name service provider */
    private static NameService nameService = null;

    static InetAddress[] unknown_array = new InetAddress[1];                            // put THIS in cache

    static {
        try {
            initClass();
        } catch (Throwable e) {
            logger.error("DNS resolver class init is failed!!!", e);
        }
    }

    private static void initClass() throws SecurityException, IllegalArgumentException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {

        loadInetAddressImplFacotry();

        // get name service if provided and requested
        String provider = null;

        String propPrefix = "sun.net.spi.nameservice.provider.";
        int n = 1;
        while (nameService == null) {
            provider = (String) AccessController.doPrivileged(new GetPropertyAction(propPrefix + n, "default"));
            n++;
            if (provider.equals("default")) {
                // initialize the default name service
                nameService = new NameService() {

                    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
                        try {
                            return (InetAddress[]) lookupAllHostAddr.invoke(impl, host);
                        } catch (Throwable e) {
                            throw new UnknownHostException(e.getMessage());
                        }
                    }

                    public String getHostByAddr(byte[] addr) throws UnknownHostException {
                        try {
                            return (String) getHostByAddr.invoke(impl, addr);
                        } catch (Throwable e) {
                            throw new UnknownHostException(e.toString());
                        }
                    }
                };
                break;
            }

            final String providerName = provider;

            try {
                AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Object>() {

                    public Object run() {
                        Iterator<?> itr = Service.providers(NameServiceDescriptor.class);
                        while (itr.hasNext()) {
                            NameServiceDescriptor nsd = (NameServiceDescriptor) itr.next();
                            if (providerName.equalsIgnoreCase(nsd.getType() + "," + nsd.getProviderName())) {
                                try {
                                    nameService = nsd.createNameService();
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Cannot create name service:" + providerName + ": " + e);
                                }
                            }
                        } /* while */
                        return null;
                    }
                });
            } catch (java.security.PrivilegedActionException e) {
            }

        }
    }

    private static void loadInetAddressImplFacotry() throws ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Class.forName("java.net.InetAddress");// load native lib libnet.so
        Class<?> clazz = Class.forName("java.net.InetAddressImplFactory");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object factory = constructor.newInstance();

        // create InetAddressImpl
        Method create = clazz.getDeclaredMethod("create");
        create.setAccessible(true);
        impl = create.invoke(factory);

        Class<?> implClass = impl.getClass();
        lookupAllHostAddr = implClass.getDeclaredMethod("lookupAllHostAddr", String.class);
        lookupAllHostAddr.setAccessible(true);

        getHostByAddr = implClass.getDeclaredMethod("getHostByAddr", byte[].class);
        getHostByAddr.setAccessible(true);
    }

    private NonBlockingDNSResolver() {
    }

    private static final NonBlockingDNSResolver instance = new NonBlockingDNSResolver();

    public static DnsResolver getInstance() {
        return instance;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = (InetAddress[]) cache.get(host);
        if (addresses == null) {
            addresses = getAddressFromNameService(host);
        }

        if (addresses == unknown_array) {// load from cache
            throw new UnknownHostException(host);
        }

        return addresses;
    }

    public InetAddress[] getAddressFromNameService(String host) throws UnknownHostException {
        boolean success = false;
        InetAddress[] addresses = null;
        try {
            addresses = nameService.lookupAllHostAddr(host);
            success = true;
        } catch (UnknownHostException e) {// exception
            addresses = unknown_array;// use to mark exception
            success = false;
            throw e;
        } finally {
            cache.put(host, addresses, success);// update dns cache
        }

        return addresses;
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
//        out.append("dns resolver : ").append(System.getProperty("line.separator"));
        out.append(String.format("%-50s", ("dns resolver : ")) + "[" + "null" + "]").append("    " + System.getProperty("line.separator"));
        cache.dump(out, indent);
    }

    public static void main(String[] args) {
        DnsResolver resolver = new NonBlockingDNSResolver();
        try {
            System.out.println(Arrays.toString(resolver.resolve("www.qq.com")));
            System.out.println(Arrays.toString(resolver.resolve("www.163.com")));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
