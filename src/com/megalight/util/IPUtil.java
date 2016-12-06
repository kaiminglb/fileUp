/** 

 * @Title: IPUtil.java

 * @Package com.megalight.util

 * @Description: TODO(用一句话描述该文件做什么)

 * @author hulikaimen@gmail.com

 * @date 2016-8-28 下午5:53:03

 * @version V1.0 

 */
package com.megalight.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-28 下午5:53:03 类说明 获得ip工具类
 */
public class IPUtil {
	private static String address = null;

	private static String getWinIP() {
		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return address;
	}

	private static String getLinuxIP() {
		Enumeration allNetInterfaces;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			System.out.println(netInterface.getName());
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					address = ip.getHostAddress();
				}
			}
		}
		return address;
	}

	public static String getIP() {
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			return getWinIP();
		} else {
			return getLinuxIP();
		}
	}
	
}
