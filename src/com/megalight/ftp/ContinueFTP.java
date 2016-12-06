package com.megalight.ftp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * 
 * @ClassName: ContinueFTP
 * @Description: 用Apache的FTPClient，实现断点续传功能
 * @author PiPiLu
 * @date 2016-7-13 下午3:04:42
 * 
 */
public class ContinueFTP {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(ContinueFTP.class);

	private FTPClient ftpClient = new FTPClient();
	private boolean flag = true; // false传输中断信号, true正常传输

	// private SwingWorker<Void, Long> task;// 用于更新进度条
	private JProgressBar jpb;
	long process; // 进度

	/**
	 * @param jpb
	 *            the jpb to set
	 */
	public void setJpb(JProgressBar jpb) {
		this.jpb = jpb;
	}

	/**
	 * true正常传输，false传输中断信号
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * @param flag
	 * 
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	/**
	 * @return the ftpClient
	 */
	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public ContinueFTP() {
		ftpClient.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
	}

	public ContinueFTP(JProgressBar jpb) {
		this.jpb = jpb;
		ftpClient.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
	}

	/**
	 * 
	 * @Description: 能否连接到FTP服务器
	 */
	public boolean connect(String hostname, int port, String username,
			String password) throws IOException {
		ftpClient.connect(hostname, port);
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				return true;
			}
		}
		disconnect();
		return false;
	}

	/**
	 * 单个文件断点上传
	 * 
	 * @param remote
	 *            远程文件路径，非文件夹
	 * @param local
	 *            本地文件路径
	 * @return 上传的状态
	 * @throws IOException
	 */
	public UploadStatus upload(String remote, String local) throws IOException {
		ftpClient.setBufferSize(1024 * 1024 * 25);
		// ftpClient.setBufferSize(1024 * 50);
		// 每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
		ftpClient.enterLocalPassiveMode();

		// 设置以二进制流的方式传输
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("GBK");

		UploadStatus result;
		// 对远程目录的处理
		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			// 创建服务器远程目录结构，创建失败直接返回失败标记 ；创建成功，ftpClient 工作目录切换到创建目录下
			if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
				return UploadStatus.Create_Directory_Fail;
			}
		}

		// 检查远程 工作目录下 （d:/books/） remoteFileName (book1) 路径下 d:/books/book1
		// 所有FTPFile
		FTPFile[] files = ftpClient.listFiles(new String(remoteFileName
				.getBytes("GBK"), "iso-8859-1"));

		// FTPFile存在的情况, 可能是文件夹 最好判断类型 file.getType == FTPFile.FILE_TYPE
		if (files.length == 1 && files[0].getType() == FTPFile.FILE_TYPE) {
			long remoteSize = files[0].getSize();
			File f = new File(local);
			long localSize = f.length();
			if (remoteSize == localSize) {
				return UploadStatus.File_Exits;
			} else if (remoteSize > localSize) {
				return UploadStatus.Remote_Bigger_Local;
			}

			// 尝试移动文件内读取指针,实现断点续传
			result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

			// 假如断点续传没有成功，则删除服务器上文件，重新上传
			if (result == UploadStatus.Upload_From_Break_Failed) {
				if (!ftpClient.deleteFile(remoteFileName)) {
					return UploadStatus.Delete_Remote_Faild;
				}
				result = uploadFile(remoteFileName, f, ftpClient, 0);
			}
		} else {
			result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
		}

		return result;
	}

	/** */
	/**
	 * 上传文件到服务器,新上传和断点续传
	 * 
	 * @param remoteFile
	 *            远程文件名，在上传之前已经将服务器工作目录做了改变
	 * @param localFile
	 *            本地文件 File句柄，绝对路径
	 * @param processStep
	 *            需要显示的处理进度步进值
	 * @param ftpClient
	 *            FTPClient 引用
	 * @param remoteSize
	 *            续传的偏移量
	 * @return
	 * @throws IOException
	 */
	public UploadStatus uploadFile(String remoteFile, File localFile,
			FTPClient ftpClient, long remoteSize) throws IOException {
		UploadStatus status = UploadStatus.Upload_New_File_Success;
		// 显示进度的上传
		long step = localFile.length() / 100;
		this.process = 0L;
		long localreadbytes = 0L;
		RandomAccessFile raf = new RandomAccessFile(localFile, "r");
		OutputStream out = ftpClient.appendFileStream(new String(remoteFile
				.getBytes("GBK"), "iso-8859-1"));

		// 断点续传
		if (remoteSize > 0) {
			ftpClient.setRestartOffset(remoteSize);
			process = remoteSize / step;

			updateUI();

			raf.seek(remoteSize);// 设置文件指针偏移量
			localreadbytes = remoteSize;
		}
		byte[] bytes = new byte[1024 * 1024 * 25];
		// byte[] bytes = new byte[1024 * 50];
		int c;

		while (flag && ((c = raf.read(bytes)) != -1)) {
			out.write(bytes, 0, c);// 断点续传引起异常 原因 服务器端没开启续传功能
			localreadbytes += c;
			if (localreadbytes / step != process) {
				process = localreadbytes / step;
				// 防止process>100
				process = (process > 100) ? 100 : process;
				System.out.println("上传进度:" + process + "%");

				// TODO 汇报上传状态
				updateUI();
			}
		}
		out.flush();
		raf.close();
		out.close();
		// 暂停、中断
		if (flag == false) {
			if (ftpClient.isConnected()) {
				ftpClient.abort();
				ftpClient.completePendingCommand();
				status = UploadStatus.Upload_Abort;

				if (logger.isInfoEnabled()) {
					logger.info("uploadFile-abort{remote:" + remoteFile
							+ ",local:" + localFile.getAbsolutePath() + "}");
				}
			}
		} else {
			// True if successfully completed, false if not.
			// 不调用此方法，随后的命令（若操作多个文件）可能会出现意想不到的异常
			// 操作完一个文件，调用completePendingCommand()才能操作另外的文件
			boolean result = ftpClient.completePendingCommand();
			if (remoteSize > 0) {
				status = result ? UploadStatus.Upload_From_Break_Success
						: UploadStatus.Upload_From_Break_Failed;
			} else {
				status = result ? UploadStatus.Upload_New_File_Success
						: UploadStatus.Upload_New_File_Failed;
			}
		}

		return status;
	}

	/**
	 * 若工作目录不存在，递归创建远程服务器工作目录，ftpClient并切换到此工作目录下
	 * 
	 * @param remote
	 *            远程服务器文件绝对路径
	 * @param ftpClient
	 *            FTPClient 对象
	 * @return 目录创建是否成功
	 * @throws IOException
	 */
	public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient)
			throws IOException {
		UploadStatus status = UploadStatus.Create_Directory_Success;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(new String(directory
						.getBytes("GBK"), "iso-8859-1"))) {
			// 假如远程目录不存在，则递归创建远程服务器目录
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			// 创建所有子目录，最后 一个/后 可能是文件或文件夹
			while (true) {
				String subDirectory = new String(remote.substring(start, end)
						.getBytes("GBK"), "iso-8859-1");
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {// 递归创建目录
						ftpClient.changeWorkingDirectory(subDirectory); // 切换工作目录
					} else {
						System.out.println("创建目录失败");
						return UploadStatus.Create_Directory_Fail;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有子目录是否创建完毕
				if (end <= start) {
					break;
				}
			}
		}
		return status;
	}

	/**
	 * 
	 * @Description: 删除ftp指定目录下，不与本地文件列表中的同名的， FTPFile
	 * @param remotePath
	 *            远程目录 服务端路径 E:/RecFileListServer/2016M1700200444611/
	 * @param localFiles
	 *            本地文件列表
	 * @param ftpClient
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void deleteRemoteFTPFile(String remotePath, List<File> localFiles,
			FTPClient ftpClient) throws UnsupportedEncodingException,
			IOException {
		//若本地文件列表为空，不做任何操作
//		if(localFiles == null || localFiles.size() == 0){
//			return;
//		}
		
		ftpClient.enterLocalPassiveMode();
		// 若ftp目录存在，该目录下多余的文件删除
		if (ftpClient.changeWorkingDirectory(//
				new String(remotePath.getBytes("GBK"), "iso-8859-1"))//
		) {
			FTPFile[] ftpFiles = ftpClient.listFiles();

			// 本地文件名集合
			Collection local = new ArrayList<String>();
			for (File f : localFiles) {
				local.add(f.getName());
			}

			for (FTPFile f : ftpFiles) {
				if (!local.contains(f.getName())) {
					ftpClient.deleteFile(f.getName());

					if (logger.isInfoEnabled()) {
						logger.info("deleteRemoteFTPFile- del[" + remotePath
								+ "]" + f.getName());
					}
				}
			}
		}
		// 若ftp目录不存在，不做任何处理

	}

	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	// 更新进度条
	private void updateUI() {
		if (jpb == null)
			return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jpb.setValue((int) process);
			}
		});
	}

	/**
	 * @Description: 上传tssp.asx文件。若目录下存在删除了再上传
	 * @param remotePath
	 *            ftp远程文件路径
	 * @param localPath
	 *            本地文件路径
	 * @throws Exception
	 */
	public void uploadTsspFile(String remotePath, String localPath,
			FTPClient ftpClient) throws Exception {
		ftpClient.enterLocalPassiveMode();

		FTPFile[] remoteFiles = ftpClient.listFiles(new String(remotePath
				.getBytes("GBK"), "iso-8859-1"));
		// ftp文件若存在则删除
		if (remoteFiles.length == 1
				&& remoteFiles[0].getType() == FTPFile.FILE_TYPE) {
			if (!ftpClient.deleteFile(remotePath)) {
				throw new Exception("FTP下tssp.asx文件删除失败");
			}
		}
		// 上传
		upload(remotePath, localPath);
	}

}
