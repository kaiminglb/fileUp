/** 

* @Title: TransportFile.java

* @Package com.megalight.model

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-19 上午10:18:38

* @version V1.0 

*/ 
package com.megalight.model;

import java.io.File;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-19 上午10:18:38
 * 类说明
 */
public class TransportFile {
	private File tsFile;//传输文件
	private Long costTime;//完成传输用时 单位秒
	
	
	
	public TransportFile(File tsFile, Long costTime) {
		this.tsFile = tsFile;
		this.costTime = costTime;
	}
	/**
	 * @return the tsFile
	 */
	public File getTsFile() {
		return tsFile;
	}
	/**
	 * @param tsFile the tsFile to set
	 */
	public void setTsFile(File tsFile) {
		this.tsFile = tsFile;
	}
	/**
	 * @return the costTime
	 */
	public Long getCostTime() {
		return costTime;
	}
	/**
	 * @param costTime the costTime to set
	 */
	public void setCostTime(Long costTime) {
		this.costTime = costTime;
	}
	
}
