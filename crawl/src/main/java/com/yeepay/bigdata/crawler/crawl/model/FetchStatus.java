package com.yeepay.bigdata.crawler.crawl.model;

/**
 * fetch 结果
 */
public enum FetchStatus {
    
    /** 连超时和读超时会重试，其它异常直接丢弃  */ 
    fetched("fetched",3),
    timeout("timeout",4),
    connectexception("connectexception",5),
    unformated("unformated",6),
    bigfile("bigfile",7),
    canceled("canceled",8),
    other("other",9),
    not200("not200",10);
 
    private String strValue;
    private Integer intValue;
    private FetchStatus(String strValue,Integer intValue){
        this.strValue = strValue;
        this.intValue = intValue;
    }

    /**
     * @return the strValue
     */
    public String getStrValue() {
        return strValue;
    }
    
    /**
     * @param strValue the strValue to set
     */
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
    
    /**
     * @return the intValue
     */
    public Integer getIntValue() {
        return intValue;
    }
    
    /**
     * @param intValue the intValue to set
     */
    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }
    

}
