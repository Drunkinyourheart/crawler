package com.yeepay.bigdata.crawler.manager.monitor;

import java.io.IOException;

public interface Dumpable {

	public void dump();

	public void dump(Appendable out, String indent) throws IOException;

}
