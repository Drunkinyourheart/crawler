/**
 *
 */
package com.yeepay.bigdata.crawler.manager.seed;

import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeedQuery {

	/**
	 * seed format: url,isDynamic
	 * 
	 * @return
	 */
	public static List<SeedInfo> listSeedInfosFromFile() {
		List<SeedInfo> result = listSeedInfosFromFile("list.txt", SeedInfoType.HOTNEWSDETAIL);
//		result.addAll(listSeedInfosFromFile("list.txt", SeedInfoType.LIST));
//		result.addAll(listSeedInfosFromFile("source_list_1.txt", SeedInfoType.LIST));
//		result.addAll(listSeedInfosFromFile("jieguo3.txt", SeedInfoType.LIST));
//		result.addAll(listSeedInfosFromFile("goodseeds.txt", SeedInfoType.RSSLIST));
//		result.addAll(listSeedInfosFromFile("epaper.txt", SeedInfoType.LIST));
		return result;
	}

	private static List<SeedInfo> listSeedInfosFromFile(String fileName, SeedInfoType type) {
		List<String> seeds;
		try {
			seeds = IOUtils.readLines(ClassLoader
					.getSystemResourceAsStream(fileName));
//			System.out.println(seeds.get(10));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<SeedInfo> result = new ArrayList<SeedInfo>();
		for (String seed : seeds) {
			String[] fields = StringUtils.splitPreserveAllTokens(seed, ",");
//			System.out.println("-------------------------------------------------------------------");
//			boolean isDynamic = StringUtils.equalsIgnoreCase(fields[1], "true") ? true : false;
			boolean isDynamic = false;
//			System.out.println("type : " + type);
			SeedInfo info = new SeedInfo(fields[0], type, "600000", isDynamic);
//			System.out.println("info : " + info);
			result.add(info);
		}
		return result;
	}

	public static List<SeedInfo> listSeedInfos() throws ClassNotFoundException,
			SQLException {

		List<SeedInfo> seedInfo = new ArrayList<SeedInfo>();

		Connection conn = getConnection();
		try {

			Statement statement = conn.createStatement();
			String sql = "select * from list_xpath_table";
			ResultSet resultSet = statement.executeQuery(sql);

			if (resultSet != null) {
				while (resultSet.next()) {
					String uri = resultSet.getString("uri");
					if (StringUtils.isNotBlank(uri)
							&& !StringUtils.endsWithIgnoreCase(uri, "null")) {
						seedInfo.add(createSeedInfo(uri));
					}
				}
			}
		} finally {
			conn.close();
		}

		return seedInfo;
	}

	private static SeedInfo createSeedInfo(String uri) {
		return new SeedInfo(uri, SeedInfoType.LIST, "300000", false);
	}

	public static Connection getConnection() throws ClassNotFoundException,
			SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager
				.getConnection("jdbc:mysql://10.13.82.17:3306/xpath_db",
						"recom", "xsKR6QSufx");
		return conn;
	}

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException {
		// List<SeedInfo> seedInfos = listSeedInfos();
		List<SeedInfo> seedInfos = listSeedInfosFromFile();
		for (SeedInfo seedInfo : seedInfos) {
			System.out.println(seedInfo.getUrl());
		}

	}
}
