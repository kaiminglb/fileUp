/** 

* @Title: PQInfo.java

* @Package com.megalight.model

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-8 下午5:51:05

* @version V1.0 

*/ 
package com.megalight.model;
/**
 * @author PipiLu
 * @version 创建时间：2016-8-8 下午5:51:05
 * 类说明 排期案件信息类
 */
public class PQCaseInfo {
	private int Id;
	//年度号
	private int ndh;
	//标准字号
	private String bzzh;
	//案件编号
	private String ajbh;
	//第一原告
	private String dyyg;
	//第一被告
	private String dybg;
	//立案案由
	private String laayname;
	//书记员编号
	private String sjydm;
	/**
	 * @return the id
	 */
	public int getId() {
		return Id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		Id = id;
	}
	/**
	 * @return the ndh
	 */
	public int getNdh() {
		return ndh;
	}
	/**
	 * @param ndh the ndh to set
	 */
	public void setNdh(int ndh) {
		this.ndh = ndh;
	}
	/**
	 * @return the bzzh
	 */
	public String getBzzh() {
		return bzzh;
	}
	/**
	 * @param bzzh the bzzh to set
	 */
	public void setBzzh(String bzzh) {
		this.bzzh = bzzh;
	}
	/**
	 * @return the ajbh
	 */
	public String getAjbh() {
		return ajbh;
	}
	/**
	 * @param ajbh the ajbh to set
	 */
	public void setAjbh(String ajbh) {
		this.ajbh = ajbh;
	}
	/**
	 * @return the dyyg
	 */
	public String getDyyg() {
		return dyyg;
	}
	/**
	 * @param dyyg the dyyg to set
	 */
	public void setDyyg(String dyyg) {
		this.dyyg = dyyg;
	}
	/**
	 * @return the dybg
	 */
	public String getDybg() {
		return dybg;
	}
	/**
	 * @param dybg the dybg to set
	 */
	public void setDybg(String dybg) {
		this.dybg = dybg;
	}
	/**
	 * @return the laayname
	 */
	public String getLaayname() {
		return laayname;
	}
	/**
	 * @param laayname the laayname to set
	 */
	public void setLaayname(String laayname) {
		this.laayname = laayname;
	}
	/**
	 * @return the sjydm
	 */
	public String getSjydm() {
		return sjydm;
	}
	/**
	 * @param sjydm the sjydm to set
	 */
	public void setSjydm(String sjydm) {
		this.sjydm = sjydm;
	}
	
}
