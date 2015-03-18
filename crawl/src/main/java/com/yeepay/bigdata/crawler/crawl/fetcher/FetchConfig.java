package com.yeepay.bigdata.crawler.crawl.fetcher;


/**
 * fetch配置
 */
public class FetchConfig {
    
    private static final int DEFAULT_SOCKETTIMEOUT      = 20000;
    private static final int DEFAULT_CONNECTIONTIMEOUT  = 20000;
    private static final String DEFAULT_USERAGENT        = "Google Chrome";   
    private static final int DEFAULT_MAXDOWNLOADSIZE     = 5242880;   

    public static final FetchConfig DEFAULT = new Builder().build();
    /**读超时 */
    private int socketTimeOut     = DEFAULT_SOCKETTIMEOUT;
    /** 连超时 */
    private int connectionTimeOut = DEFAULT_CONNECTIONTIMEOUT;

    /** 抓取userAgent */
    private String userAgent = DEFAULT_USERAGENT;
    /** 限制最大下载字节数  */
    private int maxdownloadSize = DEFAULT_MAXDOWNLOADSIZE;

    /**
     *   Constructure
     */
    FetchConfig(final int socketTimeOut,
                final int connectionTimeOut,
                final String userAgent,
                final int maxdownloadSize){
        this.socketTimeOut = socketTimeOut;
        this.connectionTimeOut = connectionTimeOut;
        this.userAgent = userAgent;
        this.maxdownloadSize = maxdownloadSize;
    }
    
    /**
     * @return the socketTimeOut
     */
    public int getSocketTimeOut() {
        return socketTimeOut;
    }
    
    /**
     * @return the connectionTimeOut
     */
    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }
    
    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * @return the maxdownloadSize
     */
    public int getMaxdownloadSize() {
        return maxdownloadSize;
    }

	 public static FetchConfig.Builder custom() {
	        return new Builder();
	 }
	 
    /**
	 * 
	 * fetchConfig 构建器
	 */
	public static class Builder {

	    private int socketTimeOut     ;
	    private int connectionTimeOut ;

	    private String userAgent ;
	    private int maxdownloadSize ;
	    
        Builder() {
            super();
            this.socketTimeOut = DEFAULT_SOCKETTIMEOUT;
            this.connectionTimeOut = DEFAULT_CONNECTIONTIMEOUT;
            this.userAgent = DEFAULT_USERAGENT;
            this.maxdownloadSize = DEFAULT_MAXDOWNLOADSIZE;
        }
        public FetchConfig build() {
            return new FetchConfig(socketTimeOut, connectionTimeOut, userAgent,maxdownloadSize);
        }
	}
}
