/** 

* @Title: JDBCUtil.java

* @Package com.megalight.util

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-5 下午2:45:20

* @version V1.0 

*/ 
package com.megalight.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.megalight.model.ConfigInfo;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-5 下午2:45:20
 * 类说明 连接sqlserver2000的工具类，用sqljdbc4.jar
 */

public class JDBCUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JDBCUtil.class);
	
	//属性从xml配置文件读取
	//从Property文件读取配置
	private static ConfigInfo config = ConfigInfo.getInstance();
	
	private static String username = config.getJdbcUser();
	private static String password = config.getJdbcPassword();
	//private static String datebase ="KG_DevArea";
	
	public static Connection getConnection() throws Exception{
		Connection con = null;
		String connectionUrl = config.getJdbcUrl();
		try {
			//加载JDBC驱动 微软官方驱动
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			
			// jtds驱动
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class.forName("net.sourceforge.jtds.jdbc.Driver", true, cl);
			con = DriverManager.getConnection(connectionUrl,username,password);
		} catch (SQLException e) {
			if (logger.isInfoEnabled()) {
				logger.info("getConnection()" + e.getMessage());
			}
			throw new Exception("网路故障或服务器异常，请联系管理员!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public static void close(Connection con){
		try {
			if(con!=null)con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(PreparedStatement ps){
		try {
			if(ps!=null)ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(ResultSet rs){
		try {
			if(rs!=null)rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
