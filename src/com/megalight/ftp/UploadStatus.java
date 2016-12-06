/*
 * 文 件 名  :  UploadStatus.java
 * 版    权    :  Ltd. Copyright (c) 2013 深圳市商巢互联网软件有限公司,All rights reserved
 * 描    述    :  <描述>
 * 创建人    :  韩红强
 * 创建时间:  上午8:47:18
 */
package com.megalight.ftp;

/**
 * @author Administrator
 *
 */
public enum  UploadStatus 
{	
	//传输结果
	Create_Directory_Fail,   //远程服务器相应目录创建失败
	Create_Directory_Success, //远程服务器闯将目录成功
	Upload_New_File_Success, //上传新文件成功
	Upload_New_File_Failed,   //上传新文件失败
	File_Exits,      //文件已经存在
	Remote_Bigger_Local,   //远程文件大于本地文件
	Upload_From_Break_Success, //断点续传成功
	Upload_From_Break_Failed, //断点续传失败
	Delete_Remote_Faild,   //删除远程文件失败
	//新增传输状态
	Upload_Abort,		//文件传送停止
	Upload_Suc,			//所有上传成功
	Uploading;			//上传中
	

}
