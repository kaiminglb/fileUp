/*
 * KTInfo.java
 *
 * Created on __DATE__, __TIME__
 */

package com.megalight.view;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import com.megalight.dao.PQInfoDao;
import com.megalight.ftp.ContinueFTP;
import com.megalight.model.ConfigInfo;
import com.megalight.model.PQKTInfo;
import com.megalight.model.TransportFile;
import com.megalight.util.FileUtil;
import com.megalight.util.StringUtil;

/**
 * 
 * @author __USER__
 */
public class KTInfoFrm extends javax.swing.JFrame{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(KTInfoFrm.class);

	private PQKTInfo info;
	private PQXXFrm pqxxFrm;
	private ContinueFTP ftp;

	// true传输状态, false停止后台上传，终止状态。正常完成false->true
	private Boolean status;

	// true 传输状态,false后台停止传送
	public Boolean isSatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
		this.ftp.setFlag(status);
	}

	// 后台任务
	private Task task;
	// 上传笔录文件
	private File uploadFile;
	// 上传文件大小限制
	private static long FILESIZE = 5 * 1024 * 1014;

	private PQInfoDao pqInfoDao = new PQInfoDao();

	/** Creates new form KTInfo */
	public KTInfoFrm(PQKTInfo info, PQXXFrm pqxxFrm) {
		this.info = info;
		this.pqxxFrm = pqxxFrm;

		initComponents();
		// 绑定事件
		bindAction();
		// 初始值
		initValue();
		// 位置居中
		this.setLocationRelativeTo(null);

		this.ftp = new ContinueFTP(jpb);
	}

	/**     
	 * @Description: 给按钮绑定事件  
	 */
	private void bindAction() {
		//上传
		jb_upload.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jb_uploadActionPerformed(evt);
			}
		});
		//停止
		jb_stop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jb_stopActionPerformed(evt);
			}
		});
		//返回按钮
		jb_back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jb_backActionPerformed(evt);
			}
		});

	}

	/**     
	 * @Description: 返回  
	 * @param evt    设定文件   
	 */
	private void jb_backActionPerformed(ActionEvent evt) {
		this.dispose();
		this.pqxxFrm.setVisible(true);
	}

	/**
	 * 内部类，实现后台ftp。 SwingWorker类有 progress属性
	 */
	class Task extends SwingWorker<Void, TransportFile> {
		/**
		 * Logger for this class
		 */
		private final Logger logger = Logger.getLogger(Task.class);

		// http://149.70.0.5/BBSOFT/Default.aspx?fpath=2016M1700200428262
		// lxurl
		private String url = null;
		// 上传成功录像基地址
		private String urlBase = "http://149.70.0.5/BBSOFT/Default.aspx?fpath=";
		private String kssj, jssj;

		// ftp传输结果
		Boolean ftpResult = Boolean.TRUE;

		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {
			// startTime endTime 20160630-1440 开庭日期-时间
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			// String ktrq = sdf.format(info.getKtrq());
			String ktrq = jf_ktrq.getText().replace("-", "");
			String sTime = ktrq + "-" + jf_kssj.getText().replace(":", "");
			String eTime = ktrq + "-" + jf_jssj.getText().replace(":", "");
			ConfigInfo config = ConfigInfo.getInstance();

			// 筛选文件
			List<String> paths = config.getPaths();
			FileUtil fileUtil = new FileUtil();
			List<File> tempFiles = fileUtil.getFilesFromStrList(paths, "asf");
			List<File> files = fileUtil.fileFilter(tempFiles, sTime, eTime);
			kssj = fileUtil.getKssj();
			jssj = fileUtil.getJssj();
			final String msg;
			// 若没找到文件，不做后续操作
			if (files == null || files.size() == 0) {
				msg = String.format("未找到文件");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						jta_taskOutput.append(msg);
					}
				});
				//设定传输状态 为 终止。不然url为空，就改变lxurl了
				setStatus(false);

				if (logger.isInfoEnabled()) {
					logger.info("没找到视频文件，终止");
				}

				return null;
			}
			// 找到文件
			msg = String.format("共%d个文件,上传中..\n", files.size());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jta_taskOutput.append(msg);
				}
			});

			try {
				ftp.connect(config.getFtpHost(),
						Integer.parseInt(config.getFtpPort()),//
						config.getFtpUser(), config.getFtpPass());

				FTPClient ftpClient = ftp.getFtpClient();
				ftpClient.enterLocalPassiveMode();

				// 上传文件
				// 1、删除对应远程目录下（如 /2016M1700200443481）多余的FTPFile
				// 年度号-bzzh-ajbh(6位)-xh
				String remotePath = "/" + info.getNdh() + info.getBzzh() + //
						info.getAjbh() + info.getXh();
				ftp.deleteRemoteFTPFile(remotePath, files, ftpClient);

				for (File f : files) {
					if (isSatus() == Boolean.TRUE) {// 正常传输
						// 子任务执行时间
						Long costTime = 0L;
						// Initialize progress property. Task的progress属性
						long start = System.currentTimeMillis();
						// 2、每个local文件上传
						ftp.upload(remotePath + "/" + f.getName(),
								f.getAbsolutePath());

						long end = System.currentTimeMillis();
						costTime = (end - start) / 1000;

						TransportFile tsFile = new TransportFile(f, costTime);
						// 发布用时
						publish(tsFile);

						if (logger.isInfoEnabled()) {
							logger.info(String.format("%s updated.cost %d\n",
									f.getName(), costTime));
						}
					}
				}
				if (isSatus()) {// 若视频文件上传成功，再上传tssp.asx文件
					// url 赋值
					url = urlBase + remotePath.substring(1);
					// 建立tssp.asx文件并上传
					File tssp = fileUtil.createTsspFile(files,
							remotePath.substring(1));
					// 上传tssp.asx文件。
					String remoteFileName = remotePath + "/" + tssp.getName();
					try {
						ftp.uploadTsspFile(remoteFileName,
								tssp.getAbsolutePath(), ftpClient);
						if (logger.isInfoEnabled()) {
							logger.info(remoteFileName + "上传\n");
						}
					} catch (Exception e) {
						if (logger.isInfoEnabled()) {
							logger.info(remoteFileName + "上传失败\n");
						}
						e.printStackTrace();
					}

				}
				ftp.disconnect();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// io异常，Ftp传输失败
				ftpResult = Boolean.FALSE;
				JOptionPane.showMessageDialog(null, "Ftp失败。请检查网络后重试");
				if (logger.isInfoEnabled()) {
					logger.info("doInBackground()-Ftp传输失败\n");
				}
				return null;
			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			jb_stop.setEnabled(false);
			jb_upload.setEnabled(true);
			jb_bl.setEnabled(true);
			jb_back.setEnabled(true);
			jf_ktrq.setEditable(true);
			jf_kssj.setEditable(true);
			jf_jssj.setEditable(true);

			setCursor(null); // turn off the wait cursor
			if (ftpResult == Boolean.FALSE) {
				jta_taskOutput.append("Ftp传输失败!\n");
				if (logger.isInfoEnabled()) {
					logger.info("Ftp传输失败!\n");
				}
			} else {
				if (status == Boolean.FALSE) {
					jta_taskOutput.append("传输终止!\n");
					if (logger.isInfoEnabled()) {
						logger.info("传输终止!\n");
					}
				} else {
					// 完成
					if(!StringUtil.isEmpty(url)){
						jtf_url.setText(url);
					}
					
					// 录像地址url、Kssj,Jssj写数据库，开庭日期，法庭编号、名称
					//从ktrq控件取值
					Date ktrqDate = (Date) jf_ktrq.getValue();
					try {
						pqInfoDao.updateUrl(info, url, kssj, jssj, ktrqDate);
					} catch (Exception e) {
						e.printStackTrace();
					}
					jta_taskOutput.append("传输完成!\n");
					setStatus(Boolean.FALSE);
				}
			}
		}

		/*
		 * Executed in event dispatching thread 执行完的 不在里面了
		 */
		protected void process(List<TransportFile> list) {
			for (TransportFile tsFile : list) {
				jta_taskOutput.append(String.format("%s 用时 %d秒.\n",//
						tsFile.getTsFile().getName(), tsFile.getCostTime()));
			}
		}
	}


	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jLabel8 = new javax.swing.JLabel();
		jtf_sjydm = new javax.swing.JTextField();
		jLabel9 = new javax.swing.JLabel();
		jtf_sjymc = new javax.swing.JTextField();
		jLabel10 = new javax.swing.JLabel();
		jtf_url = new javax.swing.JTextField();
		jb_upload = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jta_taskOutput = new javax.swing.JTextArea();
		jPanel1 = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		jf_kssj = new javax.swing.JFormattedTextField();
		jf_jssj = new javax.swing.JFormattedTextField();
		jLabel5 = new javax.swing.JLabel();
		jtf_blmc = new javax.swing.JTextField();
		jb_bl = new javax.swing.JButton();
		jLabel7 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jf_ktrq = new javax.swing.JFormattedTextField();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jtf_ndh = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jtf_bzzh = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		jtf_ajbh = new javax.swing.JTextField();
		jLabel11 = new javax.swing.JLabel();
		jtf_ktxh = new javax.swing.JTextField();
		jLabel12 = new javax.swing.JLabel();
		jLabel13 = new javax.swing.JLabel();
		jLabel14 = new javax.swing.JLabel();
		jtf_yg = new javax.swing.JTextField();
		jtf_bg = new javax.swing.JTextField();
		jtf_ah = new javax.swing.JTextField();
		jb_stop = new javax.swing.JButton();
		jpb = new javax.swing.JProgressBar();
		jb_back = new javax.swing.JButton();
		jLabel15 = new javax.swing.JLabel();
		jtf_ftmc = new javax.swing.JTextField();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("\u5f00\u5ead\u4fe1\u606f");
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		jLabel8.setText("\u4e66\u8bb0\u5458\u4ee3\u7801");

		jtf_sjydm.setEditable(false);

		jLabel9.setText("\u4e66\u8bb0\u5458\u540d\u79f0");

		jtf_sjymc.setEditable(false);

		jLabel10.setText("\u5f55\u50cf\u5730\u5740");

		jtf_url.setEditable(false);

		jb_upload.setFont(new java.awt.Font("微软雅黑", 0, 18));
		jb_upload.setText("\u5ead\u5ba1\u89c6\u9891\u4e0a\u4f20");

		jta_taskOutput.setColumns(20);
		jta_taskOutput.setEditable(false);
		jta_taskOutput.setRows(5);
		jScrollPane1.setViewportView(jta_taskOutput);

		jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabel6.setForeground(new java.awt.Color(255, 0, 51));
		jLabel6.setText("\u5f00\u59cb\u65f6\u95f4");

		jLabel5.setText("\u7b14\u5f55\u540d\u79f0");

		jtf_blmc.setEditable(false);

		jb_bl.setText("\u7b14\u5f55Doc\u6587\u4ef6");
		jb_bl.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jb_blActionPerformed(evt);
			}
		});

		jLabel7.setForeground(new java.awt.Color(255, 0, 51));
		jLabel7.setText("\u7ed3\u675f\u65f6\u95f4");

		jLabel4.setText(" \u5f00\u5ead\u65e5\u671f");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																jLabel5,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel4,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																74,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				jf_ktrq,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				85,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				87,
																				Short.MAX_VALUE)
																		.addComponent(
																				jLabel6,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				76,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jf_kssj,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				76,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(45,
																				45,
																				45)
																		.addComponent(
																				jLabel7)
																		.addGap(44,
																				44,
																				44)
																		.addComponent(
																				jf_jssj,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				77,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																jtf_blmc,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																543,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jb_bl).addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jb_bl)
														.addComponent(
																jLabel5,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_blmc,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jf_ktrq,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jf_jssj,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel7,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jf_kssj,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel6,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabel1.setText("   \u5e74\u5ea6\u53f7");

		jtf_ndh.setEditable(false);

		jLabel2.setText("  \u6807\u51c6\u5b57\u53f7");

		jtf_bzzh.setEditable(false);

		jLabel3.setText(" \u6848\u4ef6\u7f16\u53f7");

		jtf_ajbh.setEditable(false);

		jLabel11.setText("  \u6392\u671f\u5e8f\u53f7");

		jtf_ktxh.setEditable(false);

		jLabel12.setText(" \u6848\u53f7");

		jLabel13.setText("\u539f\u544a");

		jLabel14.setText("\u88ab\u544a");

		jtf_yg.setEditable(false);

		jtf_bg.setEditable(false);

		jtf_ah.setEditable(false);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel1,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				58,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				jtf_ndh,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				135,
																				Short.MAX_VALUE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel3,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				67,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jtf_ajbh,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				133,
																				Short.MAX_VALUE)))
										.addGap(27, 27, 27)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																jLabel2,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel11,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																72,
																Short.MAX_VALUE))
										.addGap(11, 11, 11)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(jtf_ktxh)
														.addComponent(
																jtf_bzzh,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																98,
																Short.MAX_VALUE))
										.addGap(130, 130, 130))
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addGap(26, 26, 26)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel14,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				39,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				jtf_bg,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				470,
																				Short.MAX_VALUE)
																		.addGap(2,
																				2,
																				2))
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel13,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				39,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				jtf_yg,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				472,
																				Short.MAX_VALUE))
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel12,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				39,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				jtf_ah,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				472,
																				Short.MAX_VALUE)))));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel1,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_bzzh,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_ndh,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel3,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_ktxh,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel11,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_ajbh,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel12,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_ah,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel13,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_yg,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel14,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																29,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jtf_bg,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))));

		jb_stop.setFont(new java.awt.Font("微软雅黑", 0, 18));
		jb_stop.setText("\u505c\u6b62");

		jb_back.setFont(new java.awt.Font("微软雅黑", 0, 18));
		jb_back.setText("\u8fd4\u56de");

		jLabel15.setText("\u6cd5\u5ead\u540d\u79f0");

		jtf_ftmc.setEditable(false);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														jScrollPane1,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														777, Short.MAX_VALUE)
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING)
																				.addGroup(
																						javax.swing.GroupLayout.Alignment.LEADING,
																						layout.createSequentialGroup()
																								.addComponent(
																										jPanel2,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addGap(42,
																										42,
																										42)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.TRAILING)
																												.addComponent(
																														jb_back,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														141,
																														Short.MAX_VALUE)
																												.addComponent(
																														jb_stop,
																														javax.swing.GroupLayout.Alignment.LEADING,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														141,
																														Short.MAX_VALUE)
																												.addComponent(
																														jb_upload,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														Short.MAX_VALUE)))
																				.addComponent(
																						jpb,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						742,
																						Short.MAX_VALUE))
																.addGap(35, 35,
																		35))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		jPanel1,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		Short.MAX_VALUE)
																.addContainerGap())
												.addGroup(
														layout.createSequentialGroup()
																.addGap(13, 13,
																		13)
																.addComponent(
																		jLabel10,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		62,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		jtf_url,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		479,
																		Short.MAX_VALUE)
																.addGap(218,
																		218,
																		218))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		jLabel9,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		78,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		jtf_sjymc,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		62,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(53, 53,
																		53)
																.addComponent(
																		jLabel8,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		81,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		jtf_sjydm,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		62,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(39, 39,
																		39)
																.addComponent(
																		jLabel15,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		81,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		jtf_ftmc,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		157,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addContainerGap()))));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(39, 39,
																		39)
																.addComponent(
																		jb_upload,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		52,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(25, 25,
																		25)
																.addComponent(
																		jb_stop,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		38,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		jb_back,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		36,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(
														layout.createSequentialGroup()
																.addContainerGap()
																.addComponent(
																		jPanel2,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														jtf_url,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jLabel10,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														29,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														jLabel9,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														29,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jtf_sjymc,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jLabel8,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														29,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jtf_sjydm,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jtf_ftmc,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														jLabel15,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														29,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(23, 23, 23)
								.addComponent(jPanel1,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jpb,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										21,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane1,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										225, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>
	//GEN-END:initComponents

	/**
	 * @Description: 组件初始化值
	 */
	private void initValue() {
		jb_stop.setEnabled(false);

		// HH:mm
		DateFormat hm = new SimpleDateFormat("HH:mm");
		DateFormatter hmf = new DateFormatter(hm);
		jf_kssj.setFormatterFactory(new DefaultFormatterFactory(hmf));
		jf_jssj.setFormatterFactory(new DefaultFormatterFactory(hmf));

		jf_ktrq.setFormatterFactory(new DefaultFormatterFactory(//
				new DateFormatter(new SimpleDateFormat("yyyy-MM-dd"))));

		// ndh,bzzh,ajbh,ktxh,,ah
		jtf_ndh.setText(String.valueOf(info.getNdh()));
		jtf_bzzh.setText(info.getBzzh());
		jtf_ajbh.setText(info.getAjbh());
		jtf_ktxh.setText(Integer.toString(info.getXh()));

		jtf_ah.setText(info.getAhqc());

		// sjymc,sjydm,lxurl,ftmc,yg,bg
		jtf_sjydm.setText(info.getSjydm());
		jtf_sjymc.setText(info.getSjyxm());
		jtf_url.setText(info.getLxurl());
		jtf_ftmc.setText(info.getFtmc());
		jtf_yg.setText(info.getDyyg());
		jtf_bg.setText(info.getDybg());

		// blmc,kssj,jssj,ktrq
		jf_ktrq.setValue(info.getKtrq());
		jtf_blmc.setText(info.getBlmc());
		jf_kssj.setValue(new Date());
		jf_jssj.setValue(new Date());

	}

	/**
	 * @Description: 上传按钮事件
	 */
	private void jb_uploadActionPerformed(ActionEvent evt) {
		jta_taskOutput.setText("");
		jpb.setValue(0);
		// startTime endTime 20160630-1440 开庭日期-时间
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// 得到开庭日期的字符串格式，开庭日期可修改
		// String ktrq = sdf.format(info.getKtrq());
		// String ktrq = jf_ktrq.getText().replace("-", "");

		String start = jf_kssj.getText().replace(":", "");
		String end = jf_jssj.getText().replace(":", "");
		if (start.compareTo(end) >= 0) {
			JOptionPane.showMessageDialog(this, "开始时间大于结束时间，请重新输入");
			// 开始时间控件获得焦点
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jf_kssj.requestFocusInWindow();
				}
			});
			return;
		}

		// 提示
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta_taskOutput.append("筛选文件中..\n");
			}
		});
		jb_stop.setEnabled(true);
		jb_upload.setEnabled(false);
		jb_bl.setEnabled(false);
		jb_back.setEnabled(false);
		jf_ktrq.setEditable(false);
		jf_kssj.setEditable(false);
		jf_jssj.setEditable(false);
		this.setStatus(Boolean.TRUE);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// 开始后台任务
		task = new Task();
		task.execute();
	}

	/**
	 * @Description: 停止按钮事件
	 */
	private void jb_stopActionPerformed(ActionEvent evt) {
		jb_stop.setEnabled(false);
		this.setStatus(Boolean.FALSE);
		JOptionPane.showMessageDialog(this, "文件停止传输");
		// 停止工作线程
		// task.cancel(true);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		jb_back.setEnabled(true);
		jf_ktrq.setEditable(true);
		jf_kssj.setEditable(true);
		jf_jssj.setEditable(true);

		jb_upload.setEnabled(true);
		jb_bl.setEnabled(true);
		setCursor(null);

	}

	// 选择笔录文件,更新笔录名称
	private void jb_blActionPerformed(java.awt.event.ActionEvent evt) {

		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// 过滤
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"doc或docx", "doc", "docx");
		jfc.setFileFilter(filter);
		// jfc.showDialog(new JLabel(), "选择word");
		int returnVal = jfc.showOpenDialog(this);
		// 点确定
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			uploadFile = jfc.getSelectedFile();
			if (uploadFile.length() > FILESIZE) {
				JOptionPane.showMessageDialog(this, "文件大于5M,请重选");
				uploadFile = null;
				return;
			}

			// 更新笔录名称、笔录二进制文件 进数据库
			try {
				pqInfoDao.updateFile(info, uploadFile);
				// 更新结果。写日志，笔录名称
				if (logger.isInfoEnabled()) {
					logger.info(info.getSjyxm() + "-" + uploadFile.getName()
							+ "笔录上传");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "笔录文件上传失败");
				if (logger.isInfoEnabled()) {
					logger.info(info.getSjyxm() + "-" + uploadFile.getName()
							+ "笔录上传失败");
				}
				e.printStackTrace();
				return;
			}

			jta_taskOutput.append(uploadFile.getName() + "笔录已上传\n");
			jtf_blmc.setText(uploadFile.getName());
		}
	}

	private void formWindowClosing(java.awt.event.WindowEvent evt) {
		if (status == Boolean.TRUE) {
			JOptionPane.showMessageDialog(this, "文件传输中，请稍后关闭窗口");
			this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		} else {
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.pqxxFrm.setVisible(true);
		}
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton jb_back;
	private javax.swing.JButton jb_bl;
	private javax.swing.JButton jb_stop;
	private javax.swing.JButton jb_upload;
	private javax.swing.JFormattedTextField jf_jssj;
	private javax.swing.JFormattedTextField jf_kssj;
	private javax.swing.JFormattedTextField jf_ktrq;
	private javax.swing.JProgressBar jpb;
	private javax.swing.JTextArea jta_taskOutput;
	private javax.swing.JTextField jtf_ah;
	private javax.swing.JTextField jtf_ajbh;
	private javax.swing.JTextField jtf_bg;
	private javax.swing.JTextField jtf_blmc;
	private javax.swing.JTextField jtf_bzzh;
	private javax.swing.JTextField jtf_ftmc;
	private javax.swing.JTextField jtf_ktxh;
	private javax.swing.JTextField jtf_ndh;
	private javax.swing.JTextField jtf_sjydm;
	private javax.swing.JTextField jtf_sjymc;
	private javax.swing.JTextField jtf_url;
	private javax.swing.JTextField jtf_yg;
	// End of variables declaration//GEN-END:variables

}