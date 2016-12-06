/** 

* @Title: StringUtil.java

* @Package com.megalight.util

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-8 上午11:30:42

* @version V1.0 

*/ 
package com.megalight.util;
/**
 * @author PipiLu
 * @version 创建时间：2016-8-8 上午11:30:42
 * 类说明 字符串工具类
 */
public class StringUtil {
	public static  boolean  isEmpty(String str){
		str = str.trim();
		if("".equals(str) || null == str){
			return true;
		}else{
			return false;
		}
	}
}
