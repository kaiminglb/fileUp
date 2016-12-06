/** 

 * @Title: Config.java

 * @Package com.megalight.model

 * @Description: TODO(用一句话描述该文件做什么)

 * @author hulikaimen@gmail.com

 * @date 2016-8-15 上午9:14:37

 * @version V1.0 

 */
package com.megalight.model;

import java.util.Arrays;
import java.util.List;

import com.megalight.util.ConfigUtil;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-15 上午9:14:37 类说明 配置信息 从xml文件中读取配置信息
 *  单例模式
 */
public class ConfigInfo {
	

	// 工具类
	private static ConfigUtil config;
	
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private String ftpHost;
	private String ftpPort;
	private String ftpUser;
	private String ftpPass;


	// 搜索视频文件路径
	private List<String> paths;

	private static class SingletonHolder {
		private static final ConfigInfo INSTANCE = new ConfigInfo();
	}

	private ConfigInfo() {
		this.jdbcUrl = config.get("jdbcUrl");
		this.ftpHost = config.get("ftpHost");
		this.ftpPort = config.get("ftpPort");
		this.jdbcUser = config.get("jdbcUser");
		this.jdbcPassword = config.get("jdbcPassword");
		this.ftpUser = config.get("ftpUser");
		this.ftpPass = config.get("ftpPass");
		
		this.paths = Arrays.asList(config.get("path").split(";"));
	}
	
	//需要时加载
	public static final ConfigInfo getInstance() {
		return SingletonHolder.INSTANCE;
	}


	
	public String getFtpUser() {
		return ftpUser;
	}


	public String getFtpPass() {
		return ftpPass;
	}

	/**
	 * @return the jdbcUrl
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * @return the ftpHost
	 */
	public String getFtpHost() {
		return ftpHost;
	}

	/**
	 * @return the ftpPort
	 */
	public String getFtpPort() {
		return ftpPort;
	}

	/**
	 * @return the paths
	 */
	public List<String> getPaths() {
		return paths;
	}
	
	/**
	 * @return the jdbcUser
	 */
	public String getJdbcUser() {
		return jdbcUser;
	}


	/**
	 * @return the jdbcPassword
	 */
	public String getJdbcPassword() {
		return jdbcPassword;
	}
	

}
