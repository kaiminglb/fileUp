/** 

 * @Title: FileUtil.java

 * @Package com.megalight.util

 * @Description: TODO(用一句话描述该文件做什么)

 * @author hulikaimen@gmail.com

 * @date 2016-8-13 下午6:33:52

 * @version V1.0 

 */
package com.megalight.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-13 下午6:33:52 类说明 文件工具类
 */
public class FileUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(FileUtil.class);

	private List<File> fileList = new LinkedList<File>();

	private String kssj, jssj;// 案件开始、结束时间

	/**
	 * @return the kssj
	 */
	public String getKssj() {
		return kssj;
	}

	/**
	 * @param kssj
	 *            the kssj to set
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
	 * @param jssj
	 *            the jssj to set
	 */
	public void setJssj(String jssj) {
		this.jssj = jssj;
	}

	// 获取文件的后缀名
	public String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		// 无扩展名，直接返回
		return filename;
	}

	// 根据文件名比较，判断该文件是否 满足时间区间的要求 （业务逻辑） 过滤,并选出最小、大时间为开始、结束时间
	// startTime endTime 20160630-1440
	public List<File> fileFilter(List<File> files, String startTime,
			String endTime) {
		List<File> result = new ArrayList<File>();
		// 找最大结束时间
		String max = startTime.substring(startTime.indexOf("-") + 1);
		// 找最小开始时间
		String min = endTime.substring(endTime.indexOf("-") + 1);
		;

		for (File f : files) {
			String name = f.getName();
			// 客户端 文件名 20160630-144030-05-20160630-151033-A.asf
			String[] str = name.split("-");
			if (str.length == 6) {// 文件名符合标准的 才比较
				String start = str[1].substring(0, 4);
				String end = str[4].substring(0, 4);
				String prefix = str[0] + "-" + start;
				String suffix = str[3] + "-" + end;
				if ((prefix.compareTo(startTime) >= 0)
						&& (suffix.compareTo(endTime) <= 0)) {
					result.add(f);
					if (min.compareTo(start) > 0)
						min = start;
					if (max.compareTo(end) < 0)
						max = end;
				}
			}

		}
		if (result.size() > 0) {
			setKssj(min);
			setJssj(max);
		}
		return result;
	}

	// 获得文件夹下,指定后缀名 的文件列表
	public List<File> getFileList(File root, String extensionName) {
		if (root == null || !root.exists() || root.isFile()) {
			return null;
		}
		File[] files = root.listFiles();
		// 文件夹为空
		if (files == null || files.length == 0) {
			return null;
		}

		for (File f : files) {
			String exName = getExtensionName(f.getName());
			if (f.isDirectory()) {
				getFileList(f, extensionName);
			} else if (exName.equalsIgnoreCase(extensionName)) {// 指定的扩展名
				fileList.add(f);
			}
		}

		if (fileList.size() == 0) {
			return null;
		} else {
			return fileList;
		}

	}

	// 获得多个文件夹下指定后缀的文件列表
	public List<File> getFileList(List<File> files, String extensionName) {
		if (files == null || files.size() == 0) {
			return null;
		}

		for (File f : files) {
			getFileList(f, extensionName);
		}
		return fileList;
	}

	// 获得多个文件夹下指定后缀的文件列表
	public List<File> getFilesFromStrList(List<String> fileStrs,
			String extensionName) {
		if (fileStrs == null || fileStrs.size() == 0) {
			return null;
		}

		for (String fname : fileStrs) {
			File file = new File(fname);
			if (file.exists() && file.isDirectory()) {
				getFileList(file, extensionName);
			}
		}
		return fileList;
	}

	/**
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param files
	 *            本地上传文件集合
	 * @param titleName
	 *            tssp文件 标题名 年度号-bzzh-ajbh(6位)-xh
	 * @return 创建本地文件同目录下tssp.asx文件，并写内容
	 */
	public File createTsspFile(List<File> files,String titleName) {
		File tssp = null;
		// 若files没值，则返回空File
		if(files == null || files.size() == 0){
			return tssp;
		}
		String filename = "tssp.asx";
		tssp = new File(files.get(0).getParentFile(),filename);
		if(!tssp.exists()){//文件不存在，创建
			try {
				tssp.createNewFile();
			} catch (IOException e) {
				if (logger.isInfoEnabled()) {
					logger.info(tssp.getPath() + "创建失败");
				}
			}
		}
		//写文件
		if(tssp.exists()){
			try {
				StringBuffer sb = new StringBuffer();
				sb.append("<ASX version =\"3.0\">")//
					.append("<Title>" + titleName + "</Title>");//
				//
				for(File f : files){
					sb.append("<Entry>").append("<Ref href =\"http://149.70.0.5/CourtVideo/")//
					.append(titleName + "/").append(f.getName())//
					.append("\"></Ref></Entry>");
				}
				sb.append("</ASX>");
				
				BufferedWriter out = new BufferedWriter(new FileWriter(tssp));
				out.write(sb.toString());
				out.flush();
				out.close();
			} catch (IOException e) {
				if (logger.isInfoEnabled()) {
					logger.info(tssp.getPath() + "ASX文件创建失败");
				}
				e.printStackTrace();
			} 
		}
		
		return tssp;
	}
}
