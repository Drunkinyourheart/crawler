package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.UnsupportedCommandException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 类CookiesRepo.java的实现描述：TODO 类实现描述
 */
public final class CookiesRepo {

    private static final Logger LOGGER = Logger.getLogger(CookiesRepo.class);

    private static final ConcurrentMap<String, List<Cookie>> map = new ConcurrentHashMap<String, List<Cookie>>();

    private static HttpClient httpClient;

    private CookiesRepo() {

    }

    static {
        try {
            httpClient = HttpClientBuilder.create().build();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();
        scheduledThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    checkCookieFresh();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }, 0, 15, TimeUnit.MINUTES);
    }

    static void addCookies(String key, List<Cookie> list) {
        map.put(key, new ArrayList<Cookie>(list));
    }

    static void addCookie(String key, Cookie cookie) {
        throw new UnsupportedCommandException("unsupported method");
    }

    public static List<Cookie> getCookies(String key) {
        return map.get(key);
    }

    private static void checkCookieFresh() {
        for (String key : LoginConfigManager.getSiteKeySet()) {
            if (!checkSiteCookies(key, map.get(key))) {
                map.remove(key);
                List<Cookie> list = getSiteCookies(key);
                if (list != null) {
                    map.put(key, list);
                    continue;
                }
                LOGGER.error("site key=" + key + " cookies is expired and can't refreash");
            }
        }
    }


    public static void main(String[] args) throws IOException {

        List<String> list = FileUtils.readLines(new File("D://ok.txt"));
        List<Cookie> cookieList = new ArrayList<Cookie>();
        for (String string : list) {
            String[] array = string.split(",");
            if (array.length != 2) {
                continue;
            }
            BasicClientCookie cookie = new BasicClientCookie(array[0], array[1]);
            cookie.setDomain("www.zhihu.com");
            cookieList.add(cookie);
        }
        CookiesRepo.addCookies("zhihu", cookieList);
        CookiesRepo.checkCookieFresh();
        String html = getHtml("http://www.zhihu.com", map.get("zhihu"));
        System.out.println(checkXpath(html, "//*[@id='top-nav-profile-dropdown']"));
    }

    private static boolean checkSiteCookies(String key, List<Cookie> list) {
        if (list == null) {
            return false;
        }
        try {
            return LoginService.checkCookieFresh(LoginConfigManager.getLoginConfig(key), list);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private static List<Cookie> getSiteCookies(String key) {
        try {
            return LoginService.loginGetCookieSync(LoginConfigManager.getLoginConfig(key));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 仅用于测试
     *
     * @param html
     * @return
     */
    private static boolean checkXpath(String html, String xpath) {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode node = cleaner.clean(html);
        try {
            Object[] ns = node.evaluateXPath(xpath);
            if (ns != null && ns.length != 0) {
                return true;
            }
        } catch (XPatherException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 仅用于测试
     */
    private static String getHtml(String url, List<Cookie> list) {
        HttpContext httpContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();
        for (Cookie cookie : list) {
            cookieStore.addCookie(cookie);
        }

        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        HttpResponse httpResponse = null;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.addHeader("Cache-Control", "keep-alive");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:25.0) Gecko/20100101 Firefox/25.0");
        try {
            httpResponse = httpClient.execute(httpGet, httpContext);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }
        if (httpResponse.getEntity() == null) {
            return "";
        }
        try {
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }
    }
}
