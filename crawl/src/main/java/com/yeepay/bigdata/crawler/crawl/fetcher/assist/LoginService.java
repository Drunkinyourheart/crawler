package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 类LoginService.java的实现描述：TODO 类实现描述 
 */
final class LoginService {
    private static final Logger LOGGER = Logger.getLogger(LoginService.class);
    private static final FirefoxDriverFactory FIREFOX_DRIVER_FACTORY = FirefoxDriverFactory.getInstance();
    
    
    synchronized static boolean checkCookieFresh(LoginConfig loginConfig,List<Cookie> list) throws Exception{
        if (loginConfig==null) {
            return false;
        }
        WebDriver driver = null;
        try{
            driver = FIREFOX_DRIVER_FACTORY.makeDriver();
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            driver.manage().deleteAllCookies();
            // 很奇怪必须在addcookie之前get一次url 
            driver.get(loginConfig.getCheckSite());
            for (Cookie cookie : list) {
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(),cookie.getValue()));
            }
            //这里又要调用一次get url之前的cookie才生效
            driver.get(loginConfig.getCheckSite());
            WebElement webElement =  driver.findElement(By.xpath(loginConfig.getCheckXpath()));
            if (webElement==null) {
                return false;
            }
            return true;
        }finally{
            if (driver!=null) {
                driver.quit();   
           }
        }
    }
    
    
   /**
 * @throws Exception 
    * 
    */
   synchronized static List<Cookie>  loginGetCookieSync(LoginConfig loginConfig) throws Exception{
          WebDriver driver = null;
          try{
              driver = FIREFOX_DRIVER_FACTORY.makeDriver();
              return  loginGetCookie(loginConfig, driver);
          }finally{
              if (driver!=null) {
                  driver.quit();   
             }
          }
   }
   
   static List<Cookie>  loginGetCookie(LoginConfig loginConfig,WebDriver driver) throws Exception{
       if (loginConfig==null) {
          return null; 
       }
       String baseUrl = loginConfig.getCheckSite();
       driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
//       driver.manage().deleteAllCookies();
       driver.get(baseUrl);
       if (loginConfig.getDenglu()!=null) {
           driver.findElement(By.xpath(loginConfig.getDenglu())).click();
       }
       driver.findElement(By.xpath(loginConfig.getUsername_key())).clear();
       driver.findElement(By.xpath(loginConfig.getUsername_key())).sendKeys(loginConfig.getUsername_value());
       driver.findElement(By.xpath(loginConfig.getPassword_key())).clear();
       driver.findElement(By.xpath(loginConfig.getPassword_key())).sendKeys(loginConfig.getPassword_value());
       driver.findElement(By.xpath(loginConfig.getSubmit())).click();
       driver.findElement(By.xpath(loginConfig.getCheckXpath()));
       Set<org.openqa.selenium.Cookie> cookies =  driver.manage().getCookies();
       if (cookies==null) {
          return null;
       }
       List<Cookie> list = new ArrayList<Cookie>();
       for (org.openqa.selenium.Cookie cookie : cookies) {
            if (!cookie.getDomain().endsWith(loginConfig.getSite())) {
                continue;
            }
            BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            basicClientCookie.setDomain(cookie.getDomain());
            list.add(basicClientCookie);
       }
       System.out.println(list);
       driver.manage().deleteAllCookies();
       return list;
   }

   
  
}
