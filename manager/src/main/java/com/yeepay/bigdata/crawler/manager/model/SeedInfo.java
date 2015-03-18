package com.yeepay.bigdata.crawler.manager.model;

import com.yeepay.bigdata.crawler.manager.utils.MultiPageLock;

public class SeedInfo {

    private String       title;
    private String       url;
    private SeedInfoType seedType;
    private SeedInfoType sourceSeedType;
    private String       rate;
    private boolean      isDynamic;
    private String       fromURL;
    // page id
    private int          pageIndex = 1;
    // for multipage news track page md5 id
    private String       firstPageID;
    private String       firstPageURL;

    private MultiPageLock.LockEntry lock;

    private long startCrawlerTs; //设置抓取时间，来方便统计每个种子从抓取到入库耗时

    //新闻精细化属性信息
    private String   publicationName="";//刊物名称
	private String   typeArea="";//版面列表，以逗号分隔
	private String   channel="";//频道
	private String   sub_channel="";//子频道
	
    public long getStartCrawlerTs() {
        return startCrawlerTs;
    }

    public void setStartCrawlerTs(long startCrawlerTs) {
        this.startCrawlerTs = startCrawlerTs;
    }

    public SeedInfo(){
    }

    public SeedInfo(String url, SeedInfoType seedType, String rate, boolean isDynamic){
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
    }

    public SeedInfo(String title, String url, SeedInfoType seedType, String rate, boolean isDynamic){
        this.title = title;
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
    }

    public SeedInfo(String title, String url, SeedInfoType seedType, String rate, boolean isDynamic, String fromURL){
        super();
        this.title = title;
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
        this.fromURL = fromURL;
    }

    public SeedInfo(String title, String url, SeedInfoType seedType, String rate, boolean isDynamic, String fromURL,
                    int pageIndex, String firstPageID){
        super();
        this.title = title;
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
        this.fromURL = fromURL;
        this.pageIndex = pageIndex;
        this.firstPageID = firstPageID;
    }

    public SeedInfo(String title, String url, SeedInfoType seedType, String rate, boolean isDynamic, String fromURL,
                    int pageIndex, String firstPageID, MultiPageLock.LockEntry lock){
        super();
        this.title = title;
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
        this.fromURL = fromURL;
        this.pageIndex = pageIndex;
        this.firstPageID = firstPageID;
        this.lock = lock;
    }

    /**
     * old 无刊物频道信息
     * @param title
     * @param url
     * @param seedType
     * @param rate
     * @param isDynamic
     * @param fromURL
     * @param pageIndex
     * @param firstPageID
     * @param firstPageURL
     * @param lock
     */
    public SeedInfo(String title, String url, SeedInfoType seedType, String rate, boolean isDynamic, String fromURL,
                    int pageIndex, String firstPageID, String firstPageURL, MultiPageLock.LockEntry lock){
        super();
        this.title = title;
        this.url = url;
        this.seedType = seedType;
        this.rate = rate;
        this.isDynamic = isDynamic;
        this.fromURL = fromURL;
        this.pageIndex = pageIndex;
        this.firstPageID = firstPageID;
        this.firstPageURL = firstPageURL;
        this.lock = lock;
    }
    /**
     * 增添刊物子频道信息
     * @param title
     * @param url
     * @param seedType
     * @param rate
     * @param isDynamic
     * @param fromURL
     * @param pageIndex
     * @param firstPageID
     * @param firstPageURL
     * @param lock
     * @param publicationName
     * @param typeArea
     * @param channel
     * @param sub_channel
     */
    public SeedInfo(String title, String url, SeedInfoType seedType,
			String rate, boolean isDynamic, String fromURL, int pageIndex,
			String firstPageID, String firstPageURL, MultiPageLock.LockEntry lock,
			String publicationName, String typeArea, String channel,
			String sub_channel) {
		super();
		this.title = title;
		this.url = url;
		this.seedType = seedType;
		this.rate = rate;
		this.isDynamic = isDynamic;
		this.fromURL = fromURL;
		this.pageIndex = pageIndex;
		this.firstPageID = firstPageID;
		this.firstPageURL = firstPageURL;
		this.lock = lock;
		this.publicationName = publicationName;
		this.typeArea = typeArea;
		this.channel = channel;
		this.sub_channel = sub_channel;
	}
    
    public String getTitle() {
        return this.title;
    }


	public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isList() {
        return (this.seedType != null)
               && ((this.seedType.equals(SeedInfoType.RSSLIST)) || (this.seedType.equals(SeedInfoType.LIST)));
    }

    public SeedInfoType getSeedType() {
        return this.seedType;
    }

    public void setSeedType(SeedInfoType seedType) {
        this.seedType = seedType;
    }

    public String getRate() {
        return this.rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public boolean isDynamic() {
        return this.isDynamic;
    }

    public void setDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public String getFromURL() {
        return fromURL;
    }

    public void setFromURL(String fromURL) {
        this.fromURL = fromURL;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (this.url == null ? 0 : this.url.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SeedInfo other = (SeedInfo) obj;
        if (this.url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!this.url.equals(other.url)) {
            return false;
        }
        return true;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getFirstPageID() {
        return firstPageID;
    }

    public void setFirstPageID(String firstPageID) {
        this.firstPageID = firstPageID;
    }

    public String getFirstPageURL() {
        return firstPageURL;
    }

    public void setFirstPageURL(String firstPageURL) {
        this.firstPageURL = firstPageURL;
    }

    public MultiPageLock.LockEntry getLock() {
        return lock;
    }

    public void setLock(MultiPageLock.LockEntry lock) {
        this.lock = lock;
    }

    
    public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public String getTypeArea() {
		return typeArea;
	}

	public void setTypeArea(String typeArea) {
		this.typeArea = typeArea;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSub_channel() {
		return sub_channel;
	}

	public void setSub_channel(String sub_channel) {
		this.sub_channel = sub_channel;
	}

	public SeedInfoType getSourceSeedType() {
		return sourceSeedType;
	}

	public void setSourceSeedType(SeedInfoType sourceSeedType) {
		this.sourceSeedType = sourceSeedType;
	}

	@Override
	public String toString() {
		return "SeedInfo [title=" + title + ", url=" + url + ", seedType="
				+ seedType + ", sourceSeedType=" + sourceSeedType + ", rate="
				+ rate + ", isDynamic=" + isDynamic + ", fromURL=" + fromURL
				+ ", pageIndex=" + pageIndex + ", firstPageID=" + firstPageID
				+ ", firstPageURL=" + firstPageURL + ", lock=" + lock
				+ ", startCrawlerTs=" + startCrawlerTs + ", publicationName="
				+ publicationName + ", typeArea=" + typeArea + ", channel="
				+ channel + ", sub_channel=" + sub_channel + "]";
	}


}
