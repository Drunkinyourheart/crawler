package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 类LoginConfig.java的实现描述：TODO 类实现描述
 *
 */
public class LoginConfig {
    private String key;
    private String site;
    private String checkSite;
    private String checkXpath;
    private String denglu;
    private String username_key;
    private String username_value;
    private String password_key;
    private String password_value;
    private String submit;

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }


    /**
     * @return the checkSite
     */
    public String getCheckSite() {
        return checkSite;
    }


    /**
     * @return the checkXpath
     */
    public String getCheckXpath() {
        return checkXpath;
    }

    /**
     * @return the denglu
     */
    public String getDenglu() {
        return denglu;
    }


    /**
     * @return the username_key
     */
    public String getUsername_key() {
        return username_key;
    }


    /**
     * @return the username_value
     */
    public String getUsername_value() {
        return username_value;
    }


    /**
     * @return the password_key
     */
    public String getPassword_key() {
        return password_key;
    }


    /**
     * @return the password_value
     */
    public String getPassword_value() {
        return password_value;
    }


    /**
     * @return the submit
     */
    public String getSubmit() {
        return submit;
    }


    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }


    public static LoginConfig getLoginModel(String json) {
        return JSON.parseObject(json, new TypeReference<LoginConfig>() {
        });
    }


    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }


    /**
     * @param site the site to set
     */
    public void setSite(String site) {
        this.site = site;
    }


    /**
     * @param checkSite the checkSite to set
     */
    public void setCheckSite(String checkSite) {
        this.checkSite = checkSite;
    }


    /**
     * @param checkXpath the checkXpath to set
     */
    public void setCheckXpath(String checkXpath) {
        this.checkXpath = checkXpath;
    }


    /**
     * @param denglu the denglu to set
     */
    public void setDenglu(String denglu) {
        this.denglu = denglu;
    }


    /**
     * @param username_key the username_key to set
     */
    public void setUsername_key(String username_key) {
        this.username_key = username_key;
    }


    /**
     * @param username_value the username_value to set
     */
    public void setUsername_value(String username_value) {
        this.username_value = username_value;
    }


    /**
     * @param password_key the password_key to set
     */
    public void setPassword_key(String password_key) {
        this.password_key = password_key;
    }


    /**
     * @param password_value the password_value to set
     */
    public void setPassword_value(String password_value) {
        this.password_value = password_value;
    }


    /**
     * @param submit the submit to set
     */
    public void setSubmit(String submit) {
        this.submit = submit;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LoginConfig [key=" + key + ", site=" + site + ", checkSite=" + checkSite + ", checkXpath=" + checkXpath
                + ", denglu=" + denglu + ", username_key=" + username_key + ", username_value=" + username_value
                + ", password_key=" + password_key + ", password_value=" + password_value + ", submit=" + submit + "]";
    }


    public static void main(String[] args) {
        String json = "{\"key\":\"zhihu\",\"site\":\".zhihu.com\",\"checkSite\":\"http://www.zhihu.com/\",\"checkXpath\":\"//*[@id=\\\"top-nav-profile-dropdown\\\"]\",\"denglu\":\"/html/body/div/div/div[2]/div/div[2]/div[2]/form/div/a\",\"username_key\":\"name=email\",\"username_value\":\"wyzssw@163.com\",\"password_key\":\"name=password\",\"password_value\":\"418417\",\"submit\":\"css=button.sign-button\"}";
        System.out.println(JSON.parseObject(json, new TypeReference<LoginConfig>() {
        }));
    }

}
