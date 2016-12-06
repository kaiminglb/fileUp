/** 

* @Title: ConfigUtil.java

* @Package com.megalight.util

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-15 上午9:06:37

* @version V1.0 

*/ 
package com.megalight.util;

import java.util.ResourceBundle;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-15 上午9:06:37
 * 类说明 properties文件读取配置信息
 */
public class ConfigUtil {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("config");
	
	/**
	 * 
	* @Description: 获得配置属性的值  
	 */
	public static final String get(String key){
		return bundle.getString(key);
	}
}
