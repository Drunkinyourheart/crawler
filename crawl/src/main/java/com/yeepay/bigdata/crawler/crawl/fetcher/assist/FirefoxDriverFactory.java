package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;


/**
 * 类FirefoxDriverFactory.java的实现描述：TODO 类实现描述
 */
public class FirefoxDriverFactory extends WebDriverPool.WebDriverFactory {

    private static final String WEBDRIVER_PAHT_CONFIG = "webdriver.firefox.bin";

    private static final class Holder {
        static final FirefoxDriverFactory FIREFOX_DRIVER_FACTORY_INSTANCE = new FirefoxDriverFactory();
    }

    public static FirefoxDriverFactory getInstance() {
        return Holder.FIREFOX_DRIVER_FACTORY_INSTANCE;
    }

    private FirefoxDriverFactory() {
        if (StringUtils.isBlank(System.getProperty(WEBDRIVER_PAHT_CONFIG))) {
            System.setProperty(WEBDRIVER_PAHT_CONFIG, "/usr/bin/firefox");
            String os = System.getProperty("os.name");
            if (os != null && StringUtils.containsIgnoreCase(os, "windows")) {
                System.getProperties().setProperty("webdriver.firefox.bin", "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
            }
        }
    }

    @Override
    public WebDriver makeDriver() throws Exception {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setEnableNativeEvents(true);
        WebDriver webDriver = new FirefoxDriver(profile);
        return webDriver;
    }

    /**
     * 销毁driver
     */
    @Override
    public void destroyDriver(WebDriver webDriver) {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Override
    public boolean validateDriver(WebDriver webDriver) {
        return true;
    }

}
