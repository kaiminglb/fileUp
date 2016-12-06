/** 

* @Title: CourtDao.java

* @Package com.megalight.dao

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-28 下午10:41:47

* @version V1.0 

*/ 
package com.megalight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.megalight.model.Court;
import com.megalight.model.PQKTInfo;
import com.megalight.util.JDBCUtil;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-28 下午10:41:47
 * 类说明
 */
public class CourtDao {
	public Court findCourtByIP(String ip) throws Exception{
		Court court = null;
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT b.CODE as ftbm,b.NAME as ftmc ")//
			.append(" FROM dbo.BM_FT AS b ,dbo.T_RecClientInfo AS r ")//
			.append(" WHERE r.RecClientIP = ? AND r.RecClientName = b.NAME");
		String sql = sb.toString();
		
		try{
			con = JDBCUtil.getConnection();
			ps = con.prepareStatement(sql);
			ps.setString(1,ip);
			rs = ps.executeQuery();
			
			while(rs.next()){
				court = new Court();
				court.setFtbm(rs.getString("ftbm"));
				court.setFtmc(rs.getString("ftmc"));
				court.setFtIP(ip);
			}
			if(court == null) throw new Exception("没找到对应的法庭");
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
		return court;
	}
}
