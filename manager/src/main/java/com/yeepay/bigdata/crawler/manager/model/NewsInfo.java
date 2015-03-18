/**
 * 
 */
package com.yeepay.bigdata.crawler.manager.model;

/**
 * @author yeyanchao
 * 
 */
public class NewsInfo {

	private String url;
	private String title;
    private String   publicationName="";//刊物名称
	private String   typeArea="";//detailUrl的具体版面信息
	private String   channel="";//频道
	private String   sub_channel="";//子频道

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	@Override
	public String toString() {
		return "NewsInfo [url=" + url + ", title=" + title
				+ ", publicationName=" + publicationName + ", typeArea="
				+ typeArea + ", channel=" + channel + ", sub_channel="
				+ sub_channel + "]";
	}

	
}
