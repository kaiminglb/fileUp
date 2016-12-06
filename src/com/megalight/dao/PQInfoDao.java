/** 

* @Title: PQDao.java

* @Package com.megalight.dao

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-9 上午10:23:53

* @version V1.0 

*/ 
package com.megalight.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.megalight.model.PQKTInfo;
import com.megalight.util.JDBCUtil;
import com.megalight.util.StringUtil;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-9 上午10:23:53
 * 类说明 排期信息及开庭查询
 */
public class PQInfoDao {
	
	/**
	 * 
	* @throws Exception 
	 * @Description: 根据年度号，案件编号,书记员代码 查询案件开庭信息  
	 */
	public List<PQKTInfo> listCases(Integer ndh,String ajbh,String sjydm) throws Exception{
		List<PQKTInfo> list = new ArrayList<PQKTInfo>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		
		StringBuffer sb = new StringBuffer();
		//开庭日期  日期型
		sb.append("SELECT aj.AJBH AS aj_bh,aj.AHQC AS ahqc,aj.LAAYNAME AS laayname,aj.DYYG AS dyyg," + //
				"aj.DYBG AS dybg,kt.XH AS xh,kt.KTRQ AS ktrq,kt.NDH AS ndh " + //
				"FROM dbo.T_PQ_KTXX AS kt ,dbo.T_PQ_AJXX AS aj WHERE kt.AJBH = aj.AJBH AND " + //
				" kt.NDH = aj.NDH AND kt.SJYDM = ?");
		if(null != ndh){
			sb.append(" AND aj.ndh = ?");
		}
		if(!StringUtil.isEmpty(ajbh)){
			sb.append(" AND aj.AJBH like ?");
		}
		//按开庭日期 降序排列
		sb.append(" order by ktrq Desc");
		
		String sql = sb.toString();
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			if(!StringUtil.isEmpty(ajbh)){
				ps.setString(1,sjydm);
				ps.setInt(2, ndh.intValue());
				ps.setString(3,ajbh+"%");//自动添加单引号 （包装后的参数）
			}else{
				ps.setString(1,sjydm);
				ps.setInt(2, ndh.intValue());
			}
			
			rs = ps.executeQuery();
			
			
			while(rs.next()){
				PQKTInfo info = new PQKTInfo();
				info.setNdh(rs.getInt("ndh"));
				info.setAhqc(rs.getString("ahqc"));
				info.setAjbh(rs.getString("aj_bh"));
				info.setLaayname(rs.getString("laayname"));
				info.setDyyg(rs.getString("dyyg"));
				info.setDybg(rs.getString("dybg"));
				info.setXh(rs.getInt("xh"));
				info.setKtrq(rs.getDate("ktrq"));
				list.add(info);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
		return list;
	}
	
	//根据年度号、案件编号、序号查询 案件开庭信息
	public PQKTInfo findPQKTInfo(int ndh,String ajbh,int xh) throws Exception{
		PQKTInfo info = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT k.NDH,k.BZZH,k.AJBH,k.XH,k.KTRQ,k.SJYDM,k.SJYXM,k.BLMC,k.LXURL ")//
			.append(" FROM dbo.T_PQ_KTXX AS k")//
			.append(" WHERE k.NDH = ? AND k.AJBH = ? AND k.XH = ?");
		String sql = sb.toString();
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			//ndh,xh是int
			ps.setInt(1, ndh);
			ps.setString(2,ajbh);
			ps.setInt(3,xh);
			rs = ps.executeQuery();
			
			while(rs.next()){
				info = new PQKTInfo();
				info.setNdh(rs.getInt("ndh"));
				info.setBzzh(rs.getString("bzzh"));
				info.setAjbh(rs.getString("ajbh"));
				info.setXh(rs.getInt("xh"));
				info.setKtrq(rs.getDate("ktrq"));
				info.setSjydm(rs.getString("sjydm"));
				info.setSjyxm(rs.getString("sjyxm"));
				info.setBlmc(rs.getString("blmc"));
				info.setLxurl(rs.getString("lxurl"));
			}
			if(info == null) throw new Exception("没找到该记录");
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
		return info;
	}

	/**     
	* @throws Exception 
	 * @Description: 更新笔录名称和二进制到数据库   
	*/  
	public void updateFile(PQKTInfo info, File uploadFile) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = "update kt set kt.BLMC = ?, kt.KTBLBYTE = ?"//
				+ " from dbo.T_PQ_KTXX AS kt WHERE kt.NDH = ? AND kt.AJBH = ? AND kt.XH = ? AND kt.BZZH = ?";
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			//blmc,笔录二进制
			ps.setString(1, uploadFile.getName());//笔录名称
			InputStream in = new FileInputStream(uploadFile);
//			//ps.setBinaryStream(2, in, in.available());
			ps.setBinaryStream(2,in);//笔录2进制
			//where条件
			ps.setInt(3, info.getNdh());//年度号 int
			ps.setString(4, info.getAjbh());//案件编号
			ps.setInt(5, info.getXh());//序号int
			ps.setString(6, info.getBzzh());
			
			ps.executeUpdate();
			
			in.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
	}
	
	// 更新 录像地址url、Kssj,Jssj，开庭日期，法庭编号、名称
	public void updateUrl(PQKTInfo info,String url,String kssj,String jssj, Date ktrqDate) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = "update kt set kt.LXURL = ?,kt.KSSJ = ?,kt.JSSJ = ?, "//
				+ " kt.KTRQ = ?, kt.FTBM = ?, kt.FTMC = ? "
				+ " from dbo.T_PQ_KTXX AS kt WHERE kt.NDH = ? AND kt.AJBH = ? AND kt.XH = ? AND kt.BZZH = ?";
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			//录像url
			ps.setString(1, url);
			ps.setString(2, kssj);
			ps.setString(3, jssj);
			
			ps.setTimestamp(4,  new Timestamp(ktrqDate.getTime()));
			ps.setString(5,info.getFtbm());
			ps.setString(6, info.getFtmc());
			
			//where条件
			ps.setInt(7, info.getNdh());//年度号 int
			ps.setString(8, info.getAjbh());//案件编号
			ps.setInt(9, info.getXh());//序号int
			ps.setString(10, info.getBzzh());
			
			ps.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
	}

	/**     
	* @Description: 根据年度号，案件编号（可为空）,书记员代码 ，是否回传 查询案件开庭信息    
	* @param ndh
	* @param ajbh
	* @param sjydm
	* @param updated 是否回传  false 未回传
	* @return    设定文件   
	 * @throws Exception 
	*/  
	public List<PQKTInfo> listCases(Integer ndh, String ajbh, String sjydm,
			Boolean updated) throws Exception {
		List<PQKTInfo> list = new ArrayList<PQKTInfo>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		
		StringBuffer sb = new StringBuffer();
		//开庭日期  日期型
		sb.append("SELECT aj.AJBH AS aj_bh,kt.AHQC AS ahqc,kt.BZZH AS bzzh,aj.LAAYNAME AS laayname,aj.DYYG AS dyyg," + //
				" aj.DYBG AS dybg,kt.XH AS xh,kt.KTRQ AS ktrq,kt.NDH AS ndh, " + //
				" kt.sjyxm as SJYXM, kt.SJYDM as sjydm,kt.lxurl AS lxurl, kt.blmc AS blmc " + //
				" FROM dbo.T_PQ_KTXX AS kt ,dbo.T_PQ_AJXX AS aj WHERE kt.AJBH = aj.AJBH AND " + //
				" kt.NDH = aj.NDH AND kt.SJYDM = ?");
		if(null != ndh){
			sb.append(" AND aj.ndh = ?");
		}
		if(!StringUtil.isEmpty(ajbh)){
			sb.append(" AND aj.AJBH like ?");
		}
		// 是否回传
		
		if(updated == Boolean.FALSE){//未回传
			sb.append(" AND kt.LXURL is null ");
		}else{//已回传
			sb.append(" AND kt.LXURL is not null and kt.LXURL <> '' ");
		}
		
		
		//按开庭日期 降序排列
		sb.append(" order by ktrq Desc");
		
		String sql = sb.toString();
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			if(!StringUtil.isEmpty(ajbh)){
				ps.setString(1,sjydm);
				ps.setInt(2, ndh.intValue());
				ps.setString(3,ajbh+"%");//自动添加单引号 （包装后的参数）
			}else{
				ps.setString(1,sjydm);
				ps.setInt(2, ndh.intValue());
			}
			
			rs = ps.executeQuery();
			
			
			while(rs.next()){
				PQKTInfo info = new PQKTInfo();
				info.setNdh(rs.getInt("ndh"));
				info.setAjbh(rs.getString("aj_bh"));
				info.setAhqc(rs.getString("ahqc"));
				info.setBzzh(rs.getString("bzzh"));
				info.setSjydm(rs.getString("sjydm"));
				info.setSjyxm(rs.getString("sjyxm"));
				
				info.setLxurl(rs.getString("lxurl"));
				info.setBlmc(rs.getString("blmc"));
				
				info.setLaayname(rs.getString("laayname"));
				info.setDyyg(rs.getString("dyyg"));
				info.setDybg(rs.getString("dybg"));
				info.setXh(rs.getInt("xh"));
				info.setKtrq(rs.getDate("ktrq"));
				list.add(info);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
		return list;
	}
	
}
