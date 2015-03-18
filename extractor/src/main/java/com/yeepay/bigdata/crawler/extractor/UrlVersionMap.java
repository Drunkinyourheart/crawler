package com.yeepay.bigdata.crawler.extractor;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UrlVersionMap {
	
	Connection conn = null; // 数据库连接
	Statement stmt = null; // Statement
	ResultSet rs = null; // 结果集

	private static Map<String, String> map                = new HashMap<String, String>();
	private static UrlVersionMap singleton = new UrlVersionMap();
	
	private UrlVersionMap() {
		// ..... 连接数据库
		// 保存数据
		// ok-end.
		try {
			// 注册驱动
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());

			// 获取数据库连接。三个参数分别为URL、用户名、密码
//			String url = "jdbc:mysql://127.0.0.1:3306/extractdb";  // jdbc:mysql://<hostname>[<:3306>]/<dbname>
			String url = "jdbc:mysql://10.13.82.17:3306/url_db";  // jdbc:mysql://<hostname>[<:3306>]/<dbname>
			String user = "recom";
			String password = "xsKR6QSufx";
			conn = DriverManager.getConnection(url, user, password);

			// 获取 Statement，相当于控制台，用于执行SQL
			stmt = conn.createStatement();

			// execute sql
			String sql = "select * from urlversion_tb";  // 需要修改的，表名????????????????????????????????????????????????????????????????
			rs = stmt.executeQuery(sql);
			
			while (rs.next()) {
				
				String uri = rs.getString("url");
				String version = rs.getString("version");
				
				map.put(uri, version);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static UrlVersionMap getInstance() {
		return singleton;
	}
	
	public static Map<String, String> getMap() {
		return map;
	}
	
}
