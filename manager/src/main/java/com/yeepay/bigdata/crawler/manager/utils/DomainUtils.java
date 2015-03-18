package com.yeepay.bigdata.crawler.manager.utils;

import com.google.common.net.InternetDomainName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.MalformedURLException;
import java.net.URL;

public class DomainUtils {

    private static String getUrlDomainName(String url) {
        String domainName = new String(url);

        int index = domainName.indexOf("://");

        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3);
        }

        index = domainName.indexOf('/');

        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index);
        }

        index = domainName.indexOf('?');
        if (index != -1) {
            domainName = domainName.substring(0, index);
        }

        domainName = StringUtils.trim(domainName);

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        // domainName = domainName.replaceFirst("^www.*?\\.", "");

        return domainName;
    }

    public static String getTopPrivateDomain(String url) {
        String domain = getUrlDomainName(url);

        if (StringUtils.contains(domain, ":")) {
            domain = StringUtils.substring(domain, 0, StringUtils.indexOf(domain, ":"));
        }

        if (InetAddressValidator.getInstance().isValid(domain)) {
            return domain;
        }
        return InternetDomainName.from(domain).topPrivateDomain().toString();
    }

    public static void main(String[] args) throws MalformedURLException {
        System.out.println(getUrlDomainName("http://news.aasfa.com"));
        System.out.println(InternetDomainName.from("news.aasfa.com.cn").topPrivateDomain().toString());
        System.out.println(getTopPrivateDomain("http://news.dsafs.aasfa.com.cn/asfasf"));
        System.out.println(getTopPrivateDomain("http://10.12.13.13:2903/"));

        InetAddressValidator validator = InetAddressValidator.getInstance();
        System.out.println(validator.isValid("0.0.0.0"));

        URL url = new URL("http://www.jntimes.cn/tags.php?/%c9%e7%bb%e1/");
        URL ne = new URL(url, "/fazhoukan/2013/1121/125175.shtml");
        System.out.println(ne.toString());
        float f = 120.12345f;
        System.out.println(f);

        // String url = "http://qiang.tmall.com/go/chn/qiang/cate-fushi.php";
        // String pattern = "[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)";
        // String pattern1 =
        // "(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)";
        // Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        // Matcher matcher = p.matcher(url);
        // matcher.find();
        // System.out.println(matcher.groupCount());

    }
}
