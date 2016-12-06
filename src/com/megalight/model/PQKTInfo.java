/** 

 * @Title: PQKTInfo.java

 * @Package com.megalight.model

 * @Description: TODO(用一句话描述该文件做什么)

 * @author hulikaimen@gmail.com

 * @date 2016-8-8 下午10:30:16

 * @version V1.0 

 */
package com.megalight.model;

import java.util.Date;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-8 下午10:30:16 类说明 排期开庭信息 一个案件可能多次开庭
 */
public class PQKTInfo {
	private int Id;
	// 年度号
	private int ndh;
	// 案号全称
	private String ahqc;
	
	// 标准字号
	private String bzzh;
	// 案件编号
	private String ajbh;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	
	// 序号
	private int xh;

	// 第一原告
	private String dyyg;
	// 第一被告
	private String dybg;
	// 立案案由
	private String laayname;
	// 法庭编号、名称
	private String ftbm;
	private String ftmc;

	/**
	 * @return the ftbm
	 */
	public String getFtbm() {
		return ftbm;
	}
	/**
	 * @param ftbm the ftbm to set
	 */
	public void setFtbm(String ftbm) {
		this.ftbm = ftbm;
	}
	/**
	 * @return the ftmc
	 */
	public String getFtmc() {
		return ftmc;
	}
	/**
	 * @param ftmc the ftmc to set
	 */
	public void setFtmc(String ftmc) {
		this.ftmc = ftmc;
	}
	// 开庭日期  date格式  内容 2007-08-29 00:00:00.000
	private Date ktrq;
	// 笔录名称
	private String blmc;
	// Ktbl 存放二进制word文件  image格式

	// 开始时间
	private String kssj;
	// 结束时间
	private String jssj;
	// 录像url
	private String lxurl;
	// 书记员编码及姓名
	private String sjydm;
	private String sjyxm;
	
	@Override
	public String toString() {
		return "PQKTInfo [ahqc=" + ahqc + ", xh=" + xh + "]";
	}
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
	 * 案号全称
	 */
	public String getAhqc() {
		return ahqc;
	}
	/**
	 * 案号全称
	 */
	public void setAhqc(String ahqc) {
		this.ahqc = ahqc;
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
	 * @return the xh
	 */
	public int getXh() {
		return xh;
	}
	/**
	 * @param xh the xh to set
	 */
	public void setXh(int xh) {
		this.xh = xh;
	}
	
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
	 * @return the ktrq
	 */
	public Date getKtrq() {
		return ktrq;
	}
	/**
	 * @param ktrq the ktrq to set
	 */
	public void setKtrq(Date ktrq) {
		this.ktrq = ktrq;
	}
	/**
	 * @return the blmc
	 */
	public String getBlmc() {
		return blmc;
	}
	/**
	 * @param blmc the blmc to set
	 */
	public void setBlmc(String blmc) {
		this.blmc = blmc;
	}
	/**
	 * @return the kssj
	 */
	public String getKssj() {
		return kssj;
	}
	/**
	 * @param kssj the kssj to set
	 */
	public void setKssj(String kssj) {
		this.kssj = kssj;
	}
	/**
	 * @return the jssj
	 */
	public String getJssj() {
		return jssj;
	}
	/**
	 * @param jssj the jssj to set
	 */
	public void setJssj(String jssj) {
		this.jssj = jssj;
	}
	/**
	 * @return the lxurl
	 */
	public String getLxurl() {
		return lxurl;
	}
	/**
	 * @param lxurl the lxurl to set
	 */
	public void setLxurl(String lxurl) {
		this.lxurl = lxurl;
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
	/**
	 * @return the sjyxm
	 */
	public String getSjyxm() {
		return sjyxm;
	}
	/**
	 * @param sjyxm the sjyxm to set
	 */
	public void setSjyxm(String sjyxm) {
		this.sjyxm = sjyxm;
	}

	
	

}
