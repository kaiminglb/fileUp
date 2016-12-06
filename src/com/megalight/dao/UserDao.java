/** 

* @Title: UserDao.java

* @Package com.megalight.dao

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-8 上午10:03:08

* @version V1.0 

*/ 
package com.megalight.dao;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.megalight.model.User;
import com.megalight.util.JDBCUtil;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-8 上午10:03:08
 * 类说明
 */
public class UserDao {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(UserDao.class);
	
	
	public User login(String loginName,String password) throws Exception{
		User user = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			con = JDBCUtil.getConnection();
			String sql = "select * from T_User where loginName = ? and userpwd = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, loginName);
			ps.setString(2, password);
			rs = ps.executeQuery();
			
			while(rs.next()){
				user = new User();
				user.setUsername(rs.getString("UserName"));
				user.setPassword(rs.getString("UserPwd"));
				user.setUserCode(rs.getString("UserCode"));
			}
			if(user == null) throw new Exception("用户名或密码错误");
		}catch(SQLException e){
			if (logger.isInfoEnabled()) {
				logger.info("login() - NetError or Server Mistake");
			}
			throw new Exception("网路故障或服务器异常，请联系管理员");
		}finally{
			JDBCUtil.close(rs);
			JDBCUtil.close(ps);
			JDBCUtil.close(con);
		}
		return user;
	}
}
