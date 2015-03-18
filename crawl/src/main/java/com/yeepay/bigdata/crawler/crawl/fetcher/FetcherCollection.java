package com.yeepay.bigdata.crawler.crawl.fetcher;

import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FetcherCollection implements Fetcher {

    private  Map<String, Fetcher> map = new ConcurrentHashMap<String, Fetcher>();

    public FetcherCollection(){
        Fetcher fetcher = new HttpClientFetchWrapper(new HttpClientFetcher());
        map.put("default", fetcher);
        map.put("static",  fetcher);
//        map.put("dynamic", new RenderFetcherWrapper(new RenderFetcher()));
    }

    public void addFetcher(String key,Fetcher fetcher){
        if (StringUtils.isBlank(key)||key.equals("default")) {
            throw new IllegalArgumentException("key can't be empty or can't add key named 'default'");
        }
        map.put(key, fetcher);
    }

    @Override
    public <T> T fetch(CrawlURL crawlURL) throws Exception{
        if (StringUtils.isBlank(crawlURL.getType())) {
            return map.get("default").fetch(crawlURL);
        }
        Fetcher fetcher = map.get(crawlURL.getType());
        if (fetcher==null) {
            return map.get("default").fetch(crawlURL);
        }
        return fetcher.fetch(crawlURL);
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, Fetcher> entry : map.entrySet()) {
             Fetcher fetcher = entry.getValue();
             if (fetcher!=null) {
                 entry.getValue().destroy();
             }
        }
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException, InterruptedException {
        for (Map.Entry<String, Fetcher> entry : map.entrySet()) {
             entry.getValue().dump(out, indent);
        }

    }

}
