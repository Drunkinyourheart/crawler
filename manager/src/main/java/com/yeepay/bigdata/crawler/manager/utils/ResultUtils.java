package com.yeepay.bigdata.crawler.manager.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class ResultUtils {

	private static PrintWriter writer;

	static {
		String logPath = System.getProperty("log.output", FileUtils
				.getUserDirectory().getPath());
		File file = FileUtils.getFile(logPath, "extractor.result");
		try {
			writer = new PrintWriter(new FileWriter(file, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void write(String result) {
		writer.println(result);
		writer.flush();
	}

	public static void close() {
		IOUtils.closeQuietly(writer);
	}

	public static void main(String[] args) {
		write("hello");
		close();
		System.out.println(System.getProperty("java.io.tmpdir"));
	}

}
