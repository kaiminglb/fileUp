/** 

* @Title: User.java

* @Package com.megalight.model

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-8 上午10:01:17

* @version V1.0 

*/ 
package com.megalight.model;
/**
 * @author PipiLu
 * @version 创建时间：2016-8-8 上午10:01:17
 * 类说明
 */
public class User {
	private String username;
	private String password;
	//书记员代码
	private String userCode;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	
	
}
