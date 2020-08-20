package hd.bs.bill.bxbd;

//import java.awt.Component;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.ObjectOutputStream;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.InetAddress;
//import java.net.URL;
//import java.net.URLConnection;
//import java.text.SimpleDateFormat;
import java.util.ArrayList; 
import java.util.HashMap; 
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy; 
import nc.bs.framework.common.NCLocator;
//import nc.bs.framework.common.NCLocator;
//import nc.bs.framework.common.RuntimeEnv;
//import nc.bs.framework.common.UserExit;
//import nc.bs.framework.comn.NetObjectInputStream;
//import nc.bs.logging.Logger;
//import nc.bs.pub.IClusterFinder;
//import nc.desktop.ui.WorkbenchEnvironment;
//import nc.desktop.ui.console.NCConsoleFrame;
//import nc.desktop.ui.console.logrecord.LogWriter;
//import nc.desktop.ui.console.logrecord.RecordClientLogListener;
//import nc.desktop.ui.console.logspr.LogAnalyze;
//import nc.image.toolkit.keyboardScreenCapture;
import nc.itf.er.reimtype.IReimTypeService;
import nc.itf.erm.mactrlschema.IErmMappCtrlFieldManage;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor; 
//import nc.sfbase.client.ClientToolKit;
//import nc.sfbase.client.NCAppletStub;
//import nc.sfbase.toolkit.ComponentToolKit;
//import nc.ui.dbcache.DBCacheEnv;
//import nc.ui.ml.NCLangRes;
//import nc.ui.pub.beans.UITextField;
import nc.vo.bd.meta.BatchOperateVO;
import nc.vo.er.djlx.DjLXVO; 
import nc.vo.erm.fieldcontrast.FieldcontrastVO;
import nc.vo.erm.mactrlschema.MtappCtrlbillVO;
import nc.vo.erm.mactrlschema.MtappCtrlfieldVO; 
//import nc.vo.jcom.io.FileUtil;
import nc.vo.pub.BusinessException;  
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
//import nc.vo.pub.lang.UFDateTime;
import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;

/**
 * 0.MUJ00770  费用申请控制规则设置-集团
 * 1.MUJ00771  费用申请控制规则设置-组织
 */
public class MtappCtrlRuleAction extends ApproveWorkAction{

	IErmMappCtrlFieldManage irts = NCLocator.getInstance().lookup(IErmMappCtrlFieldManage.class);
	BaseDAO dao = new BaseDAO();
	/**
	 * 费用申请控制规则设置-集团  固定功能注册编码
	 */
	String funnode = "MUJ00770";
	
//	private void transServerLog(String userid) {
//		ObjectOutputStream outPut = null;
//		NetObjectInputStream inputStream = null;
//		LogWriter lw = null;
//		File file = null;
//		try {
//			UFDateTime starttime = new UFDateTime("2020-07-27 17:27:00");
//			UFDateTime finishTime = new UFDateTime(NCLocator.getInstance().lookup(IClusterFinder.class).getServerTime());
//			Hashtable<String, Object> hm = new Hashtable<String, Object>();
//			hm.put("servicename", "directlogquery");
//			hm.put("methodname", "query");
//			hm.put("parametertypes", new Class[] { String.class });
//
//			String query = null;
//			if (true) {
//				query = "select " + getServerName() + "/m" + " from nclogs where ts>=\"" + starttime.toString() + "\"  and ts<=\"" + finishTime.toString() + "\" and userid=\"" + userid+ "\" ";
//			}else if ((type == null || type.length() <= 0)) {
//				query = "select " + NCLocator.getInstance().lookup(IClusterFinder.class).getMasterName() + " from nclogs where ts>=\"" + starttime.toString() + "\"  and ts<=\"" + finishTime.toString() + "\" and userid=\"" + userid+ "\" ";
//			} else {
//				query = "select " + getServerName() + "/" + type + " from nclogs where ts>=\"" + starttime.toString() + "\"  and ts<=\"" + finishTime.toString() + "\" and userid=\"" +userid + "\" ";
//			}
//
//			Logger.error("=============" + query);
//			hm.put("parameter", new Object[] { query });
//
//			final String dir = "C:/Users/86138/NCCACHE/localhost_kohbppgnlc_2020";// ClientToolKit.getNCCodeBase();
//			Logger.debug(dir);
//			String serverUrl = "http://localhost:2020/";//ClientToolKit.getServerURL();
//			NCAppletStub stub = NCAppletStub.getInstance();
//			String serverip = "localhost";//stub.getParameter("SERVER_IP");
//			String protocol = "http";//stub.getParameter("SCHEME");
//			URL url = null;
//			url = new URL(protocol + "://" + serverip + ":" + port
//					+ "/remotecall");
//			if (!isConnect(url))
//				url = new URL(serverUrl + "service/monitorservlet");
//			Logger.debug("request url=" + url);
//			URLConnection con = url.openConnection();
//			con.setDoOutput(true);
//			con.setDoInput(true);
//			outPut = new ObjectOutputStream(con.getOutputStream());
//			outPut.writeObject(hm);
//			inputStream = new NetObjectInputStream(con.getInputStream());
//			// 读记录数。
//			long lognum = inputStream.readLong();
//			Logger.debug(NCLangRes.getInstance().getStrByID("logrecord", "LogRecordPanel-000006", null, new String[]{lognum+""})/*结果集个数 = {0}*/);
//			int i = 0;
//			file = new File(dir + "/recordlog");
//			if (!file.exists()) {
//				file.mkdirs();
//			}
//			lw = new LogWriter("serverlog", dir + "/recordlog", 5000000);
//			while (true) {
//				i++;
//				String s = null;
//				if (inputStream != null) {
//					s = (String) inputStream.readObject();
//					if (!s.equals("0x99")) {
//
//						if (true) {
//							// 分析性能参数
//							parseStr(s);
//						}
//						lw.writeInformation(s + "\n");
//					}
//				}
//				if (s.equals("0x99")) {
//					break;
//				}
//				// if (i % 1000 == 0) {
//				Logger.debug(NCLangRes.getInstance().getStrByID("logrecord", "LogRecordPanel-000007")/*已完成*/ + (i * 100) / lognum + "%");
//				// }
//			}
//			//录取完日志后，生成SPR报告
//			if(true){
//			new Thread()
//			{
//				public void run(){		
//					    String busiaction=UserExit.getInstance().getBusiaction();
//				        String java_home=System.getProperty("java.home");
//						String java=java_home+File.separator+"bin"+File.separator+"java";
//						String classpath=dir+File.separator+"CODE"+File.separator+"modules"+File.separator+"riart"+File.separator+"client"+File.separator+"lib"+File.separator+"uiriart_riasfbaseLevel-1.jar";
//						//String classpath=dir+File.separator+"CODE"+File.separator+"modules"+File.separator+"uap"+File.separator+"client"+File.separator+"classes";
//						String osname=System.getProperty("os.name");
//						 String heapmax="0m",heapmin="0m",permsize="0m";  
//						try{
//						String memory=System.getProperty("javaplugin.vm.options");//-Xmx384m -XX:PermSize=128m -Xms128m -Dsun.awt.keepWorkingSetOnMinimize=true
//					    String[] mems=memory.split("-");
//					    for(String mem:mems){
//					    	if(mem.startsWith("Xmx")){
//					    		
//					    		heapmax=mem.substring(3, mem.length()-1);
//					    		int mindex=heapmax.indexOf("m");
//					    		if(mindex==-1){
//					    			mindex=heapmax.indexOf("g");
//					    		}
//					    		heapmax=heapmax.substring(0,mindex+1);
//					    	}else if(mem.startsWith("XX:PermSize=")){
//					    		permsize=mem.substring(12, mem.length()-1);
//					    		int mindex=permsize.indexOf("m");
//					    		permsize=permsize.substring(0, mindex+1);
//					    	}else if(mem.startsWith("Xms")){
//					    		heapmin=mem.substring(3, mem.length());
//					    		int mindex=heapmin.indexOf("m");
//					    		heapmin=heapmin.substring(0, mindex+1);
//					    	}
//					    	
//					    }
//						}catch(Exception e){
//							heapmax=Runtime.getRuntime().maxMemory()/1024/1024+"m";
//						}
//					 
//					   boolean isenablecached= DBCacheEnv.isCacheEnabled();
//					   String enablecached=String.valueOf(isenablecached);
//					   osname=osname.replace(" ", "");
//						String reportDir = new SimpleDateFormat("yyyyMMddHHmmss")
//						.format(new Date());
//					
//						String name=dir+File.separator+"SPR_"+(busiaction!=null?busiaction:"")+reportDir+".html";
//					    if(RuntimeEnv.getInstance().isDevelopMode()){
//					    	String nchome=System.getProperty("user.dir");
//					        classpath=nchome+File.separator+"modules"+File.separator+"riart"+File.separator+"client"+File.separator+"lib"+File.separator+"uiriart_riasfbaseLevel-1.jar";	
//					    	//classpath=nchome+File.separator+"modules"+File.separator+"uap"+File.separator+"client"+File.separator+"classes";	
//					    }
//					    String command="\""+java+"\""+" -Xmx1024m"+" -cp "+"\""+classpath+"\""+" "+LogAnalyze.class.getName()+" "+"\""+dir+"/recordlog"+"\""+ " " +heapmax+ " " +heapmin+ " " +permsize+ " "+osname+" "+pjmethod+" "+enablecached+" "+"\""+name+"\"";
//						Logger.debug(command);
//					    Process p=null;
//							    Logger.debug("generate SPR");
//							    Logger.debug("SPR文件保存在"+name);/*-=notranslate=-*/
//							    try{
//							    p=Runtime.getRuntime().exec(command);
//						         int exitval=p.waitFor();
//								  if(exitval==1){
//									  Logger.debug("create SPR failed");
//								  }else{
//									  Logger.debug("create SPR success");
//								  }
//							    }catch(Exception e){
//							    	Logger.error(e.getMessage());
//							    }
//								 
//				}
//			}.start();
//			}
//			Logger.debug(NCLangRes.getInstance().getStrByID("logrecord", "LogRecordPanel-000008", null, new String[]{dir})/*已完成,请到  {0}/recordlog 下将日志打包发送给技术人员*/);
//		} catch (Exception e) {
//			Logger.error(e.getMessage(), e);
//			return;
//		} finally {
//			try {
//				if (lw != null)
//					lw.close();
//			} catch (Exception e) {
//				Logger.error(e.getMessage(), e);
//			}
//			try {
//				if (outPut != null)
//					outPut.close();
//			} catch (Exception e) {
//				Logger.error(e.getMessage(), e);
//			}
//			try {
//				if (inputStream != null)
//					inputStream.close();
//			} catch (Exception e) {
//				Logger.error(e.getMessage(), e);
//			}
//		}
//
////		if (getPerfCbx().isSelected()) {
////			handlerPerfdata(file);
////		}
//	}
	
//	private String getServerName() throws BusinessException {
//		if (RuntimeEnv.getInstance().isDevelopMode()) {
//			return "";
//		}
//		if (servername == null) {
//			servername = NCLocator.getInstance().lookup(IClusterFinder.class)
//					.getMasterName();
//		}
//		return servername;
//	}
//	
//	private void parseStr(String str) {
//		String[] ss = str.split("\n");
//		for (String s : ss) {
//			parseLine(s);
//		}
//	}
//	
//	private String servername = null;
//	private static final String SPLIT_PRE = "\\$\\$";
//	private int remoteCallNum = 0;
//	private static final String BUSI_ACTION = "busiaction";
//	private String busiAction = "";
//	private static final String WRITE_TO_CLIENT_BYTE = "writetoclientbytes";
//	private static final String READ_FROM_CLIENT_BYTE = "readfromclientbytes";
//	// 默认性能指标
//	private String[] indicators = new String[] { READ_FROM_CLIENT_BYTE,
//			WRITE_TO_CLIENT_BYTE };
//	// pj需要过滤的远程调用方法
//	private String[] pjRemoteCallMethods = null;
//	private long[] indicator_values = new long[2];
//	// 过滤的远程调用
//	private String pjmethod = null;
//	// 默认端口
//	private String port = "9999";
//	private String type = null;
//		
//	private void parseLine(String line) {
//		String[] ss = line.split(SPLIT_PRE);
//		String msgStr = null;
//		boolean isSummary = false;
//		for (String s : ss) {
//			s = s.trim();
//			if (s.startsWith("file=") && s.indexOf("mwsummary-log") > -1) {
//				isSummary = true;
//			} else if (s.startsWith("msg=")) {
//				msgStr = s.substring("msg=".length());
//			}
//			if (isSummary && msgStr != null) {
//				break;
//			}
//		}
//		if (isSummary) {
//			remoteCallNum++;
//			if (null != msgStr) {
//				parseMsg(msgStr);
//			}
//		}
//	}
//	
//	private void parseMsg(String msgStr) {
//		// 2012-04-12 modify by huzw
//		for (String pjFilterMethod : pjRemoteCallMethods) {
//			if (msgStr.indexOf("remoteCallMethod=" + pjFilterMethod.trim()) > -1) {
//				remoteCallNum--;
//				return;
//			}
//		}
//		// if (msgStr.indexOf("remoteCallMethod=nc.bs.pub.IClusterFinder") > -1)
//		// {
//		// remoteCallNum--;
//		// return;
//		// }
//		String[] ss = msgStr.split(";");
//
//		for (String s : ss) {
//			s = s.trim();
//
//			if (s.startsWith(BUSI_ACTION)) {
//				if (busiAction.equals("") || busiAction.equals("unkown")) {
//					busiAction = s.substring(BUSI_ACTION.length() + 1);
//					if (busiAction.contains("/")) {
//						busiAction = busiAction.replaceAll("\\/", "-");
//					}
//				}
//				continue;
//			}
//			for (int i = 0; i < indicators.length; i++) {
//				String indicator = indicators[i];
//				if (s.startsWith(indicator)) {
//					int value = Integer
//							.parseInt(s.substring(indicator.length() + 1));
//					indicator_values[i] += value;
//					break;
//				}
//			}
//		}
//	}
//	private boolean isConnect(URL url) {
//		try {
//			HttpURLConnection con = null;
//		    con = (HttpURLConnection)url.openConnection();
//		      con.setDoOutput(true);
//		      con.setDoInput(true);
//		      int code=con.getResponseCode();
//		} catch (Exception e1) {
//			return false;
//		}
//		return true;
//	}

//	private void handlerPerfdata(File file) {
//		try {
//			File baseFile = new File(base);
//			if (!baseFile.exists()) {
//				baseFile.mkdirs();
//			}
//
//			StringBuffer sb = new StringBuffer();
//			sb.append("remoteallcount=").append(remoteCallNum).append(";  ");
//			for (int k = 0; k < indicators.length; k++) {
//				sb.append(indicators[k]).append("=")
//						.append(indicator_values[k]).append(";  ");
//			}
//
//			if (file.listFiles() != null && file.listFiles().length > 0) {
//
//				for (File f : file.listFiles()) {
//					if (f.getName().equals("clientlog.log")) {
//						sb.append("costtime=").append(getCostTime(f))
//								.append("; ");
//						break;
//					}
//				}
//
//				String dateStr = dateFormat.format(new Date(System
//						.currentTimeMillis()));
//				File jf = new File(baseFile, dateStr + "pjlog_" + busiAction
//						+ ".jar");
//
//				jar(file, jf, true);
//				String serverPath = serverBase + File.separator
//						+ InetAddress.getLocalHost().getHostName()
//						+ jf.getName();
//				sb.append("file=").append(serverPath);
//				upload(jf, serverPath);
//			}
//
//			// 更新历史记录
//			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
//					new FileOutputStream(
//							new File(baseFile, "pjjobhistory.log"), true)));
//			writer.println(sb.toString());
//			writer.flush();
//			writer.close();
//			try {
//				((UITextField) getCompByName("perfTx")).setText(sb.toString());
//			} catch (Exception e) {
//
//			}
//		} catch (Exception e) {
//			Logger.error(e.getMessage(), e);
//			;
//		}
//	}
//	
	/**
	 * 保存-新增保存和修改保存
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
//		transServerLog(userid);
		BaseDAO dao = new BaseDAO();
		String billtype = super.getBilltype();
	 
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] cfieldMap = bodys.get("mtapp_cfield");//控制维度
		HashMap<String, Object>[] cbillMap = bodys.get("mtapp_cbill");//控制对象

		/**
		 * 控制维度
		 */
		ArrayList<MtappCtrlfieldVO> updateFVOs = new ArrayList<MtappCtrlfieldVO>();
		ArrayList<MtappCtrlfieldVO> deleteFVOs = new ArrayList<MtappCtrlfieldVO>();
		ArrayList<MtappCtrlfieldVO> addnewFVOs = new ArrayList<MtappCtrlfieldVO>();
		
		HashMap<String, String> fname = new HashMap<String, String>();
		
		for(int i=0; null!=cfieldMap && i<cfieldMap.length; i++){
			MtappCtrlfieldVO fvo = (MtappCtrlfieldVO) transMapTONCVO(MtappCtrlfieldVO.class,cfieldMap[i]);
			//更新
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(1)){
				if(fname.containsKey(fvo.getFieldname())){
					throw new BusinessException(fvo.getFieldname()+" 已经重复,不能保存!");
				}else{
					fname.put(fvo.getFieldname(), fvo.getFieldname());
				}
				fvo.setStatus(1);
				updateFVOs.add(fvo);
			}
			
			//新增
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(2)){
				if(fname.containsKey(fvo.getFieldname())){
					throw new BusinessException(fvo.getFieldname()+" 已经重复,不能保存!");
				}else{
					fname.put(fvo.getFieldname(), fvo.getFieldname());
				}
				fvo.setStatus(2);
				fvo.setDr(0);
				String sql ="select *\n" +
							"from (select replace(fullpath,'mtapp_bill.','') as fullpath ,\n" + 
							"             case when instr(fullpath,'mtapp_detail.')>0 then '费用申请单明细.'||displayname else displayname end as displayname\n" + 
							"      from md_attr_power\n" + 
							"      join (select displayname,case when tableid='er_mtapp_bill' then 'mtapp_bill.'||name\n" + 
							"                   when tableid='er_mtapp_detail' then 'mtapp_bill.mtapp_detail.'||name end as name\n" + 
							"              from md_column\n" + 
							"              where nvl(dr,0)=0\n" + 
							"              and tableid in ('er_mtapp_bill','er_mtapp_detail')) col on col.name=md_attr_power.fullpath\n" + 
							"     where beanid = 'e3167d31-9694-4ea1-873f-2ffafd8fbed8'\n" + 
							"       and powertype = 'erm'\n" + 
							"       and fullpath <> 'mtapp_bill.mtapp_detail'\n" + 
							"       and nvl(dr,0)=0\n" + 
							"      )\n" + 
							"where displayname='"+fvo.getFieldname()+"'";
				ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());
				if(null!=list && list.size()>0){
					fvo.setFieldcode(list.get(0)[0].toString());
				}
				if(null==fvo.getAdjust_enable()||"".equals(fvo.getAdjust_enable())){
					fvo.setAdjust_enable(new UFBoolean(false));
				}
				if(null!=head.get("djlxbm")){
					fvo.setPk_tradetype(head.get("djlxbm").toString());
				}else{
					throw new BusinessException("表头单据大类不能空!");
				}
				if(null!=billtype && funnode.equals(billtype)){
					fvo.setPk_org(InvocationInfoProxy.getInstance().getGroupId());
				} 
				addnewFVOs.add(fvo);
			}
			
			//删除
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(3)){
				fvo.setStatus(3);
				deleteFVOs.add(fvo);
			}
		}
		BatchOperateVO batchVO = new BatchOperateVO();
		if(updateFVOs.size()>0){
//			dao.updateVOList(updateFVOs);
			Object[] objs = new Object[updateFVOs.size()];
			objs = updateFVOs.toArray(new MtappCtrlfieldVO[0]);
			batchVO.setUpdObjs(objs);
		}
		if(deleteFVOs.size()>0){
//			dao.deleteVOList(deleteFVOs);
			Object[] objs = new Object[deleteFVOs.size()];
			objs = deleteFVOs.toArray(new MtappCtrlfieldVO[0]);
			batchVO.setDelObjs(objs);
		}
		if(addnewFVOs.size()>0){
//			dao.insertVOList(addnewFVOs);
			Object[] objs = new Object[addnewFVOs.size()];
			objs = addnewFVOs.toArray(new MtappCtrlfieldVO[0]);
			batchVO.setAddObjs(objs);
		}
		irts.batchSave(batchVO);
		
		
		/**
		 * 控制对象
		 */
		ArrayList<MtappCtrlbillVO> updateBVOs = new ArrayList<MtappCtrlbillVO>();
		ArrayList<MtappCtrlbillVO> deleteBVOs = new ArrayList<MtappCtrlbillVO>();
		ArrayList<MtappCtrlbillVO> addnewBVOs = new ArrayList<MtappCtrlbillVO>();
		
		HashMap<String, String> bbilltype = new HashMap<String, String>();
		
		for(int i=0; null!=cbillMap && i<cbillMap.length; i++){
			MtappCtrlbillVO bvo = (MtappCtrlbillVO) transMapTONCVO(MtappCtrlbillVO.class,cbillMap[i]);
			//更新
			if(null!=cbillMap[i].get("vostatus") && cbillMap[i].get("vostatus").equals(1)){
				if(bbilltype.containsKey(bvo.getPk_src_tradetype())){
					throw new BusinessException("控制对象交易类型已经重复,不能保存!");
				}else{
					bbilltype.put(bvo.getPk_src_tradetype(),bvo.getPk_src_tradetype());
				}
				bvo.setStatus(1);
				updateBVOs.add(bvo);
			}
			
			//新增
			if(null!=cbillMap[i].get("vostatus") && cbillMap[i].get("vostatus").equals(2)){
				if(bbilltype.containsKey(bvo.getPk_src_tradetype())){
					throw new BusinessException("控制对象交易类型已经重复,不能保存!");
				}else{
					bbilltype.put(bvo.getPk_src_tradetype(),bvo.getPk_src_tradetype());
				}
				bvo.setStatus(2);
				bvo.setDr(0);
//				bvo.setPk_src_tradetype(newPk_src_tradetype);
				if(null!=head.get("djlxbm")){
					bvo.setPk_tradetype(head.get("djlxbm").toString());
				}else{
					throw new BusinessException("表头单据大类不能空!");
				}
				if(null!=bvo.getPk_src_tradetype()){
					String sql = "select * from bd_billtype where pk_billtypeid='"+bvo.getPk_src_tradetype()+"' and nvl(dr,0)=0";
					ArrayList<BilltypeVO> list = (ArrayList<BilltypeVO>) dao.executeQuery(sql, new BeanListProcessor(BilltypeVO.class));
					if(null!=list && list.size()>0){
						bvo.setSrc_billtype(list.get(0).getNcbrcode());
						bvo.setSrc_tradetype(list.get(0).getPk_billtypecode());
					}
				}
				bvo.setSrc_system("erm");
				if(null!=billtype && funnode.equals(billtype)){
					bvo.setPk_org(InvocationInfoProxy.getInstance().getGroupId());
				} 
				addnewBVOs.add(bvo);
			}
			
			//删除
			if(null!=cbillMap[i].get("vostatus") && cbillMap[i].get("vostatus").equals(3)){
				bvo.setStatus(3);
				deleteBVOs.add(bvo);
			}
		}
		BatchOperateVO batchVO1 = new BatchOperateVO();
		if(updateBVOs.size()>0){
//			dao.updateVOList(updateBVOs);
			Object[] objs = new Object[updateBVOs.size()];
			objs = updateBVOs.toArray(new MtappCtrlbillVO[0]);
			batchVO1.setUpdObjs(objs);
		}
		if(deleteBVOs.size()>0){
//			dao.deleteVOList(deleteBVOs);
			Object[] objs = new Object[deleteBVOs.size()];
			objs = deleteBVOs.toArray(new MtappCtrlbillVO[0]);
			batchVO1.setDelObjs(objs);
		}
		if(addnewBVOs.size()>0){
//			dao.insertVOList(addnewBVOs);
			Object[] objs = new Object[addnewBVOs.size()];
			objs = addnewBVOs.toArray(new MtappCtrlbillVO[0]);
			batchVO1.setAddObjs(objs);
		}
		irts.batchSave(batchVO1);
		if(null!=head.get("djlxoid")){
			return queryBillVoByPK(userid, head.get("djlxoid").toString());
		}else{
			return null;
		}
	}
	
	/**
	 * 用pk查询单据
	 */
	public BillVO queryBillVoByPK(String userid, String pk) throws BusinessException {
		BillVO billVO = new BillVO();
		ConditionAggVO condAggVO_head = new ConditionAggVO();
		ConditionVO[] condVOs_head = new ConditionVO[1];
		condVOs_head[0] = new ConditionVO();
		condVOs_head[0].setField(IVOField.PPK);
		condVOs_head[0].setOperate("=");
		condVOs_head[0].setValue(pk);
		condAggVO_head.setConditionVOs(condVOs_head);

		ConditionAggVO condAggVO_body = new ConditionAggVO();
		ConditionVO[] condVOs_body = new ConditionVO[1];
		condVOs_body[0] = new ConditionVO();
		condVOs_body[0].setField(IVOField.PPK);
		condVOs_body[0].setOperate("=");
		condVOs_body[0].setValue(pk);
		condAggVO_body.setConditionVOs(condVOs_body);

		// 查询 表头
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		// 查询 表体
		QueryBillVO body_querybillVO = (QueryBillVO) queryPage_body(userid, condAggVO_body,1,1);
		// 查询 表体  
		if(null!=body_querybillVO.getQueryVOs()){
			billVO = body_querybillVO.getQueryVOs()[0];
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
//	private keyboardScreenCapture gen = null;
//	public void open(){
//		// 配置服务器的属性
//		setServerProp();
//		// 性能参数置零
//		resetPerfData();
//		if (RuntimeEnv.getInstance().isDevelopMode()) {
//			System.setProperty("redirect", "true");
//			NCConsoleFrame.getInstance().redirectOutputStreamAsNeed();
//		}
//		NCConsoleFrame.getInstance().addConsoleListener(getListener());
//		isRecording = !isRecording;
//		String dir = "C:/Users/86138/NCCACHE/localhost_kohbppgnlc_2020";
//		File file0 = new File(dir + "/recordlog");
//		FileUtil.cleanDirectory(file0);
//		File file = new File(dir + "/recordlog/image");
//		if (!file.exists()) {
//			file.mkdirs();
//		}
//		gen = new keyboardScreenCapture(file, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, KeyEvent.SHIFT_MASK, false));
//		try {
//			gen.startListenKeyboard();
//			UFDateTime starttime = new UFDateTime(NCLocator.getInstance().lookup(IClusterFinder.class).getServerTime());
//		} catch (Exception e1) {
//			Logger.error(e1.getMessage(), e1);
//		}
////		getBtnRecord().setIcon(ClientToolKit.loadImageIcon("/images/toolbar/icon/valide.GIF"));
////		setStateWhenRecord(true);
//	}
//	
////	private void setStateWhenRecord(boolean start) {
////		if (start) {
////			getCbbLogLevel().setSelectedItem("DEBUG");
////		} else {
////			getCbbLogLevel().setSelectedItem("default");
////		}
////		setCbbClient(start);
////		setCbremote(start);
////	}
	
//	private String pjPort = "8061";
//	private String pjServer = "localhost";
//	private RecordClientLogListener listener = null;
//	private boolean isRecording = false;
//	
//	private RecordClientLogListener getListener() {
//		if (listener == null) {
//			listener = new RecordClientLogListener();
//		}
//		return listener;
//	}
//	
//	private void resetPerfData() {
//		busiAction = "";
//		remoteCallNum = 0;
//		try {
//			indicator_values = new long[indicators.length];
////			((UITextField) getCompByName("perfTx")).setText("");
//		} catch (Exception e) {
//
//		}
//	}
//	
//	 
//	
//	private void setServerProp() {
//		InputStream input = null;
//		try {
//			input = this.getClass().getClassLoader().getResourceAsStream("monitor.properties");
//			Properties sprop = new Properties();
//			if (input != null) {
//				sprop.load(input);
//			}
//			if (sprop.get("MONITORSERVERPORT") != null && sprop.get("MONITORSERVERPORT").toString().trim().length() > 0) {
//				port = sprop.get("MONITORSERVERPORT").toString().trim();
//			} else {
//				port = "9999";
//			}
//			if (sprop.get("PJREMOTECALLMETHOD") != null && sprop.get("PJREMOTECALLMETHOD").toString().trim().length() > 0) {
//				pjmethod = sprop.getProperty("PJREMOTECALLMETHOD").toString().trim();
//			}
//
//			if (sprop.getProperty("PERFORMANCE_INDICATOR") != null && sprop.getProperty("PERFORMANCE_INDICATOR").trim().length() > 0) {
//				indicators = sprop.getProperty("PERFORMANCE_INDICATOR").split(";");
//				for (int i = 0; i < indicators.length; i++) {
//					indicators[i] = indicators[i].trim();
//				}
//			} else {
//				indicators = new String[] { READ_FROM_CLIENT_BYTE, WRITE_TO_CLIENT_BYTE };
//			}
//
//			if (sprop.get("PJPORT") != null && sprop.get("PJPORT").toString().trim().length() > 0) {
//				pjPort = sprop.get("PJPORT").toString().trim();
//			} else {
//				pjPort = "8061";
//			}
//
//			if (sprop.get("PJSERVER") != null && sprop.get("PJSERVER").toString().trim().length() > 0) {
//				pjServer = sprop.get("PJSERVER").toString().trim();
//			} else {
//				pjServer = "localhost";
//			}
//
//			if (sprop.get("LOGQUERYFILENAMELIKE") != null && sprop.get("LOGQUERYFILENAMELIKE").toString().trim().length() > 0)
//				type = sprop.get("LOGQUERYFILENAMELIKE").toString().trim();
//
//			// 2012-04-12 add by huzw
//			if (sprop.get("PJREMOTECALLMETHOD") != null && sprop.get("PJREMOTECALLMETHOD").toString().trim().length() > 0)
//				pjRemoteCallMethods = sprop.get("PJREMOTECALLMETHOD").toString().trim().split(";");
//
//		} catch (Exception e) {
//		} finally {
//			try {
//				if (input != null)
//					input.close();
//			} catch (IOException e) {
//				Logger.error(e.getMessage(), e);
//			}
//		}
//	}
	
	String pk_org = "";
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
//		open();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","djlxoid");
		map.put("PK","djlxoid");
		map.put("ermbilltype.pk_org","pk_org");
		StringBuffer djlxoid = reCondition(obj, true,map);
		String and = "";
		if(null!=djlxoid && djlxoid.toString().contains("pk_org")){
			pk_org = djlxoid.toString();
			djlxoid = new StringBuffer();
		}
		String org = "";
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();
			for (int i = 0; null != conVOs && conVOs.length > 0 && i < conVOs.length; i++) {
				if (conVOs[i].getField().startsWith("pk_org")) {
					org = conVOs[i].getValue();
				}
				if (conVOs[i].getField().startsWith("djlxoid")) {
					String[] s = conVOs[i].getValue().split("-");
					if(null!=s && s.length==2){
						pk_org = " and pk_org='"+s[1]+"'";
						djlxoid = new StringBuffer(" and djlxoid='"+s[0]+"'");
					}
				}
			}
		}
			//MUJ00770固定【费用申请控制规则设置-集团】节点
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		and = " and pk_group='"+groupid+"' ";
		String sql = "select distinct * from er_djlx where djdl = 'ma' and matype = 1 "+and+djlxoid+"";
		ArrayList<DjLXVO> list = (ArrayList<DjLXVO>) dao.executeQuery(sql, new BeanListProcessor(DjLXVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new DjLXVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			maps[i].put("ibillstatus", -1);// 自由
			maps[i].put("pk_org",org);
			maps[i].put("PK", headVO.get("djlxoid")+"-"+org);
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String billtype = super.getBilltype();
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","djlxoid");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();
			for (int i = 0; null != conVOs && conVOs.length > 0 && i < conVOs.length; i++) {
				if (conVOs[i].getField().startsWith("djlxoid")) {
					String[] s = conVOs[i].getValue().split("-");
					if(null!=s && s.length==2){
						pk_org = " and pk_org='"+s[1]+"'";
						str = new StringBuffer(" and djlxoid='"+s[0]+"'");
					}
				}
			}
		}
		if(null!=billtype && funnode.equals(billtype)){
			//MUJ00770固定【费用申请控制规则设置-集团】节点
			String groupid = InvocationInfoProxy.getInstance().getGroupId();
			pk_org = " and pk_org='"+groupid+"'";
		}
		
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		 
		String action = super.getAction();// 每次执行此方法，返回页签名称
		if(null!=action && !action.contains("QUERY")){
			// 控制维度 
			String sql = "select * from er_mtapp_cfield where nvl(dr,0)=0 "+pk_org+" and pk_tradetype in (select djlxbm from er_djlx where isnull(dr,0)=0 "+str+")";
			ArrayList<MtappCtrlfieldVO> list = (ArrayList<MtappCtrlfieldVO>) dao.executeQuery(sql, new BeanListProcessor(MtappCtrlfieldVO.class));
			if (list != null && list.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new MtappCtrlfieldVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas);
				billVO.setTableVO("mtapp_cfield", maps); 
			} 
			// 控制对象
			sql = "select * from er_mtapp_cbill  where nvl(dr,0)=0 "+pk_org+" and pk_tradetype in (select djlxbm from er_djlx where isnull(dr,0)=0 "+str+");";
			ArrayList<MtappCtrlbillVO> list2 = (ArrayList<MtappCtrlbillVO>) dao.executeQuery(sql, new BeanListProcessor(MtappCtrlbillVO.class));
			if (list2 != null && list2.size() >0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new MtappCtrlbillVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas);
				billVO.setTableVO("mtapp_cbill", maps); 
			} 
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}else if(null!=action && action.contains("QUERY")){
			// 控制维度 
			if (action.contains("mtapp_cfield")) {
				String sql = "select * from er_mtapp_cfield where nvl(dr,0)=0 "+pk_org+" and pk_tradetype in (select djlxbm from er_djlx where isnull(dr,0)=0 "+str+")";
				ArrayList<MtappCtrlfieldVO> list = (ArrayList<MtappCtrlfieldVO>) dao.executeQuery(sql, new BeanListProcessor(MtappCtrlfieldVO.class));
				if (list != null && list.size() > 0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new MtappCtrlfieldVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas);
					billVO.setTableVO("mtapp_cfield", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}
			// 控制对象
			if (action.contains("mtapp_cbill")) {
				String sql = "select * from er_mtapp_cbill  where nvl(dr,0)=0 "+pk_org+" and pk_tradetype in (select djlxbm from er_djlx where isnull(dr,0)=0 "+str+");";
				ArrayList<MtappCtrlbillVO> list2 = (ArrayList<MtappCtrlbillVO>) dao.executeQuery(sql, new BeanListProcessor(MtappCtrlbillVO.class));
				if (list2 != null && list2.size() >0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new MtappCtrlbillVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas);
					billVO.setTableVO("mtapp_cbill", maps); 
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}
		}
		return null;
	}
}
