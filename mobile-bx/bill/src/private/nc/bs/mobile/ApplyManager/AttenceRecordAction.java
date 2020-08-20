package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.vo.field.IVOField;
import nc.vo.hm.hm02.AttendanceRecordVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.er.util.YerUtil;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.bd.timezone.TimezoneUtil;
import nc.itf.fi.pub.SysInit;
import nc.itf.ta.IImportDataManageMaintain;
import nc.itf.ta.ITimeRuleQueryService;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFTime;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.trade.pub.IBillStatus;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 考勤打卡
 * 
 * @author zhangzhao
 * 
 */
public class AttenceRecordAction extends AbstractMobileAction {

	private BaseDAO baseDao;

	public BaseDAO getBaseDao() {
		if (null == baseDao)
			baseDao = new BaseDAO();
		return baseDao;
	}

	public AttenceRecordAction() {
		super();
	}

	@Override
	public Object processAction(String account, String userid, String billtype,
			String action, Object obj) throws BusinessException {
		if (action.equals("UPLOADIMG")) {
			uploadImg(userid, obj);
			return null;
		} else if (action.equals("FACECOMPARE")) {
			faceCompare(userid, obj);
			return null;
		} else if (action.equals("DEFBINDING")) {
			DefBinging(userid, obj);
			return null;
		} else {
			return super.processAction(account, userid, billtype, action, obj);
		}

	}

	private void DefBinging(String userid, Object obj) throws BusinessException {
		// TODO 自动生成的方法存根
		String devid = InvocationInfoProxy.getInstance().getProperty("devid");
		// 检查当天，一台设备只能给一个用户进行打卡。
		if (devid != null && devid.length() > 0) {
			String sql = "select sm_user.cuserid from sm_user where user_name6='"
					+ devid + "'";
			Object useid = getBaseDao()
					.executeQuery(sql, new ColumnProcessor());
			if (null == useid || "".equals(useid)) {
				String sql1 = " update sm_user set user_name6='" + devid
						+ "'  where cuserid='" + userid + "'";
				getBaseDao().executeUpdate(sql1);
			} else {
				if (userid.equals(useid)) {
					throw new BusinessException("当前用户已经绑定这部设备！");
				}
				String t = SysInit.getParaString("GLOBLE00000000000000",
						"UNBINING"); // 获取时间
				if (null != t && !"".equals(t)) {
					UFTime time = new UFTime(t.split("-")[0]);
					UFTime time1 = new UFTime(t.split("-")[1]);
					if (new UFTime().after(time) && new UFTime().before(time1)) {
						String sql2 = " update sm_user set user_name6='' where user_name6='"
								+ devid + "'";
						String sql1 = " update sm_user set user_name6='"
								+ devid + "'  where cuserid='" + userid + "'";
						getBaseDao().executeUpdate(sql2);
						getBaseDao().executeUpdate(sql1);
					} else {
						throw new BusinessException("该手机已经被别人绑定，现在不允许重复绑定！");
					}
				} else {
					throw new BusinessException("重复绑定时间参数不存在！");
				}
			}
		}
	}

	protected Object faceCompare(String userid, Object obj)
			throws BusinessException {
		// {"similar":0.997397244,"result":true,"errorinfo":""}
		// try {
		// String url = SysInit.getParaString("0001", "H701");
		// if (url.startsWith("N")) {
		// // 不启用人脸识别
		// return;
		// }
		// String url =
		// "http://api.qiansou.cn/api/url2urlv4?appid=0c03f5b9efbc418e9462e011a2a719d4";

		// String file1 = System.getProperties().getProperty("user.dir") +
		// "/webapps/nc_web/upload/user/" + userid + ".jpg";
		// String file2 = System.getProperties().getProperty("user.dir") +
		// "/webapps/nc_web/upload/" + userid + new UFDate() + ".jpg";
		// HttpPost post = new HttpPost();
		// /*
		// * String url1 =
		// *
		// "http://nc.ztgmcom.com:80/service/~muap/hd.muap.pub.internet.FileService?path=upload/user&filename="
		// * +userid+".jpg"; String url2 =
		// *
		// "http://nc.ztgmcom.com:80/service/~muap/hd.muap.pub.internet.FileService?path=upload&filename="
		// * +userid+new UFDate()+".jpg"; String json =
		// * "{\"url1\":\""+url1+"\",\"url2\":\""+url2+"\"}";
		// */
		//
		// String picdata1 = readImage(file1);
		// String picdata2 = readImage(file2);
		//
		// String json = "{\"faceimage1\":\"" + picdata1 +
		// "\",\"faceimage2\":\"" + picdata2 + "\"}";
		//
		// byte[] resdata = null;
		// try {
		// // resdata = post.postTrans(url, json.getBytes("utf-8"));
		// } catch (Exception e1) {
		// Logger.error(e1);
		// // 人脸识别出现服务异常，直接跳过，不影响正常的打开，然后记录日志
		// MessageVO msgVO = new MessageVO();
		// msgVO.setPk_sender("SYS");
		// msgVO.setPk_receiver("0001N110000000007WSB");
		// msgVO.setImsgstatus(0);
		// msgVO.setVmessage(e1.getMessage());
		// msgVO.setVmsgtype("SYS");
		// msgVO.setDr(0);
		// msgVO.setDmsgdatetime(new UFDateTime(System.currentTimeMillis()));
		// BaseDAO dao = new BaseDAO();
		// dao.insertVO(msgVO);
		// return;
		// }
		//
		// MFaceCPReSultVO resultVO = new ObjectMapper().readValue(resdata,
		// MFaceCPReSultVO.class);
		//
		// if (resultVO.getResult()) {
		// // 判断识别lv
		// if (resultVO.getSimilar().doubleValue() > 0.0) {
		// // 识别率高于0.9即可
		// } else {
		// // 识别率低
		// throw new BusinessException("不是同一人，识别率为：" + resultVO.getSimilar());
		// }
		// } else {
		// throw new BusinessException("人脸识别错误：" + resultVO.getErrorinfo() +
		// ",识别率：" + resultVO.getSimilar());
		// }
		//
		// } catch (Exception e) {
		// Logger.error(e);
		// throw new BusinessException(e.getMessage());
		// }
		return null;
	}

	protected void uploadImg(String userid, Object obj)
			throws BusinessException {
		// 考勤上传图片
		String destdir = System.getProperties().getProperty("user.dir")
				+ "/webapps/nc_web/upload/";
		String pic = obj.toString();
		try {
			byte[] data = new BASE64Decoder().decodeBuffer(pic);
			String filename = userid + new UFDate().getDay() + ".jpg";
			String filefullpath = destdir + "/" + filename;

			File dir = new File(destdir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File file = new File(filefullpath);
			if (file.exists()) {
				// 文件存在
				file.delete();
			}
			file.createNewFile();

			FileOutputStream fileos = new FileOutputStream(file);
			fileos.write(data);
			fileos.flush();
			fileos.close();
		} catch (IOException e) {
			throw new BusinessException(e);
		}
	}

	/**
	 * 读取图片
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	protected String readImage(String filename) throws Exception {
		FileInputStream is = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		File file = new File(filename);
		// if (!file.exists()) {
		// throw new Exception("未上传头像！");
		// }
		is = new FileInputStream(file);
		byte[] buffer = new byte[1024 * 1024];
		int len;
		while ((len = is.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}

		byte[] data = bos.toByteArray();

		String picdata = new BASE64Encoder().encode(data);

		is.close();
		bos.close();

		return picdata;
	}

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		if (obj == null) {
			throw new BusinessException("数据不能为空!");
		}
		String devid = InvocationInfoProxy.getInstance().getProperty("devid");
		// 检查当天，一台设备只能给一个用户进行打卡。
		if (devid != null && devid.length() > 0) {
			String sql = "select sm_user.user_name6 from sm_user where cuserid='"
					+ userid + "'";
			Object devi = getBaseDao().executeQuery(sql, new ColumnProcessor());
			if (null == devi || "".equals(devi)) {
				throw new BusinessException("请先绑定手机！");
			}
			// String date = new
			// UFDate(System.currentTimeMillis()).toString().substring(0, 7);
			// String condition = "Creator<> '" + userid + "' and def2='" +
			// devid + "' and nvl(dr,0)=0 and dbilldate like '" + date + "%'";
			//
			// ArrayList<SuperVO> listARVOs = (ArrayList<SuperVO>)
			// getBaseDao().retrieveByClause(AttendanceRecordVO.class,
			// condition);

			if (!devid.equals(devi)) {
				throw new BusinessException("该手机不是当前登陆人绑定手机，无法打卡！");
			}
		}
		String sql = "select su.pk_psndoc, hp.pk_dept\n"
				+ "  from sm_user su\n" + "  join bd_psndoc bp\n"
				+ "    on bp.pk_psndoc = su.pk_psndoc\n"
				+ "   and nvl(bp.dr, 0) = 0\n" + "  join bd_psnjob hp\n"
				+ "    on hp.pk_psndoc = su.pk_psndoc\n"
				+ "    and nvl(hp.dr,0)=0\n" + " where nvl(su.dr, 0) = 0"
				+ " and su.cuserid='" + userid + "'";
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) getBaseDao()
				.executeQuery(sql, new MapListProcessor());
		String pk_psndoc = null;
		String pk_dept = null;
		if (list != null && list.size() > 0) {
			Object pk_psndocobj = list.get(0).get("pk_psndoc");
			Object pk_depts = list.get(0).get("pk_dept");
			if (pk_psndocobj == null) {
				throw new BusinessException("该用户没有关联人员档案,请去PC端关联！");

			}
			pk_psndoc = pk_psndocobj == null ? null : pk_psndocobj.toString();
			pk_dept = pk_depts == null ? null : pk_depts.toString();
		}
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>) obj;
		Set<String> keys = map.keySet();
		String pk_org = null;
		String gps = null;
		String address = null;
		for (String key : keys) {
			if ("pk_org".equals(key)) {
				pk_org = (String) map.get(key);
			} else if ("gps".equals(key)) {
				gps = (String) map.get(key);
			} else {
				address = (String) map.get(key);
			}
		}
		// 校验GPS误差
		// 截取经纬度，dus[0]是经度，dus[1]是纬度
		String[] dus = gps.split(",");
		// 返回false表示超出打卡GPS范围，抛异常
		int t = SysInit.getParaInt("GLOBLE00000000000000", "HM01"); // 获取打开范围
																	// 默认是500米
		// int t = 10000000;
		String nqsql = "select bd_psndoc.def20 from sm_user  join bd_psndoc on bd_psndoc.pk_psndoc=sm_user.pk_psndoc where sm_user.dr=0 and sm_user.cuserid='"
				+ userid + "'";
		Object nq = (Object) getBaseDao().executeQuery(nqsql,
				new ColumnProcessor());
		if (null != nq && "Y".equals(nq.toString())) {

		} else {
			if (!checkGPS(Double.parseDouble(dus[0]),
					Double.parseDouble(dus[1]), userid, t, pk_org)) {
				throw new BusinessException("打卡失败，超出设定的打卡范围！");
			}
		}
		// String timesql =
		// "select shortname from bd_defdoc where code='worktime' ";
		// String sb_time = (String) getBaseDao().executeQuery(timesql, new
		// ColumnProcessor());
		// if (sb_time == null || sb_time.trim().length() == 0) {
		// throw new BusinessException("未设置上下班时间！");
		// }
		String timesql = "SELECT def20 FROM org_dept WHERE pk_dept IN (select pk_dept from bd_psnjob bpb join bd_psndoc bpc"
				+ " on bpb.pk_psndoc = bpc.pk_psndoc  join sm_user su   on su.pk_psndoc = bpb.pk_psndoc WHERE su.cuserid = '"
				+ userid + "')";
		String sb_time = (String) getBaseDao().executeQuery(timesql,
				new ColumnProcessor());
		if (sb_time == null || sb_time.length() == 0) {
			timesql = "SELECT c.def20 FROM org_orgs c where c.pk_org = '"
					+ pk_org + "'";
			sb_time = (String) getBaseDao().executeQuery(timesql,
					new ColumnProcessor());
		}

		if (sb_time == null || sb_time.trim().length() == 0
				|| "".equals(sb_time)) {
			throw new BusinessException("未设置上下班时间！");
		}
		// 上下班时间sb_time格式： 08:30:00-17:00:00 以"-"分割
		String[] times = sb_time.split("-");

		AttendanceRecordVO vo = new AttendanceRecordVO();
		vo.setCreator(userid);// 用户pk
		vo.setBillmaker(userid);
		// Object pk_psndoc =
		// getBaseDao().executeQuery("select pk_psndoc  from sm_userandclerk where userid='"+userid+"'",
		// new ColumnProcessor());
		UFDateTime dateTime = new UFDateTime(System.currentTimeMillis());// 打开时间提前3分钟，服务器时间差-5*60*1000
		UFTime time = dateTime.getUFTime();

		UFTime time12 = new UFTime("12:00:00");

		int offset = 0;
		// Calendar.getInstance().getTimeZone().getRawOffset();
		// 1-正常 2-迟到 3-早退 4-旷工 半天 5-旷工全天
		if (time.after(new UFTime(times[0])) && time.before(time12)) {
			// 异常
			// 30分钟内算迟到
			long cdtime_end = new UFTime(times[0]).getMillis() + 30 * 60 * 1000
					- offset;
			long cdtime_end2 = new UFTime(times[0]).getMillis() + 3 * 60 * 60
					* 1000 - offset;
			if (time.after(new UFTime(times[0]))
					&& time.before(new UFTime(cdtime_end))) {
				// 迟到
				vo.setKq_type("2");// 考勤类型-迟到
			} else if (time.after(new UFTime(cdtime_end))) {
				// 旷工半天
				// 30分钟到3小时算旷工半天
				vo.setKq_type("5");
			}
			// else if (time.after(new UFTime(cdtime_end2)) &&
			// time.before(time12)) {
			// // 旷工全天
			// // 3小时以上 旷工全天
			// vo.setKq_type("5");
			// }

		} else if (time.after(time12) && time.before(new UFTime(times[1]))) {
			// 异常
			long cdtime_end = new UFTime(times[1]).getMillis() - 15 * 60 * 1000
					- offset;
			long cdtime_end2 = new UFTime(times[0]).getMillis() - 3 * 60 * 60
					* 1000 - offset;
			if (time.after(new UFTime(cdtime_end))
					&& time.before(new UFTime(times[1]))) {
				// 早退-30分钟内
				vo.setKq_type("3");// 考勤类型-早退
			} else if (time.before(new UFTime(cdtime_end))) {
				// 旷工半天
				vo.setKq_type("5");
			}
			// else if (time.after(time12) && time.before(new
			// UFTime(cdtime_end2))) {
			// // 旷工全天
			// vo.setKq_type("5");
			// }
		} else {
			vo.setKq_type("1");// 考勤类型-正常
		}
		OrgVO orgvo = (OrgVO) baseDao.retrieveByPK(OrgVO.class, pk_org);
		String pk_group = orgvo.getPk_group();
		String pk_org_v = orgvo.getPk_vid();
		// 设置设备ID
		vo.setPk_dept(pk_dept);
		vo.setDef2(InvocationInfoProxy.getInstance().getProperty("devid"));
		vo.setDef1(pk_psndoc);
		vo.setBillmaker(userid);// 人员pk
		vo.setDk_address(address);// 打卡地点
		vo.setDr(0);
		vo.setDk_time(dateTime);// 打卡时间
		vo.setPk_org_v(pk_org_v);
		vo.setPk_group(pk_group);
		vo.setMaketime(new UFDateTime());
		vo.setGps(gps);// GPS坐标
		vo.setPk_org(pk_org);// 公司pk
		vo.setBilltype("HM02");// 单据类型
		vo.setDbilldate(new UFDate());// 单据日期
		vo.setTs(new UFDateTime());
		vo.setApprovestatus(-1);
		vo.setCreator(userid);
		vo.setBillmaker(userid);
		vo.setStatus(VOStatus.NEW);
		getBaseDao().insertVO(vo);
		// return queryBillVoByPK(userid, returnPK);
		// 慧都智连考勤数据导入到考勤数据采集档案
		try {
			importData(pk_psndoc, dateTime);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

		return null;
	}

	/**
	 * 慧都智连考勤打卡数据同步到考勤机数据采集节点
	 * 
	 * @author 贺阳
	 * @param errormsg
	 * @param pk_psndoc
	 * @throws BusinessException
	 */
	public void importData(String pk_psndoc, UFDateTime dkTime)
			throws Exception {

		String pk_org = "";

		// 当前打卡日期
		UFDate currdate = new UFDate();
		String sql = "select tbm_psndoc.pk_org,bd_psndoc.id,bd_psndoc.pk_psndoc from tbm_psndoc left join  bd_psndoc on "
				+ "bd_psndoc.pk_psndoc=tbm_psndoc.pk_psndoc where bd_psndoc.pk_psndoc='"
				+ pk_psndoc + "'";
		ArrayList<HashMap<String, Object>> lists = (ArrayList<HashMap<String, Object>>) getBaseDao()
				.executeQuery(sql, new MapListProcessor());
		if (lists != null && lists.size() > 0) {
			for (int i = 0; i < lists.size(); i++) {
				pk_org = (String) lists.get(i).get("pk_org");
				String psndocid = (String) lists.get(i).get("id");
				// 自动执行考勤卡号为身份证号
				String update_sql = "update tbm_psndoc set timecardid='"
						+ psndocid + "' where  pk_psndoc='" + pk_psndoc + "'";
				getBaseDao().executeUpdate(update_sql);
			}
		}

		// 获取考勤规则
		TimeRuleVO timeRuleVO = NCLocator.getInstance()
				.lookup(ITimeRuleQueryService.class).queryByOrg(pk_org);

		// 是否启用了考勤地异常判断
		boolean useAddressException = timeRuleVO.getWorkplaceflag() == null ? false
				: timeRuleVO.getWorkplaceflag().booleanValue();
		ArrayList<String> list = new ArrayList<String>();
		String userCheckTime = dkTime.toString();
		// 通过人员档案获取考勤卡号
		String timecardid = YerUtil.getColValue("tbm_psndoc", "timecardid",
				"pk_psndoc", pk_psndoc);
		if (timecardid != null && !"".equals(timecardid)) {

			// 启用考勤地异常需要传5个参数格式的
			if (useAddressException) {
				list.add(";" + " " + timecardid + " "
						+ userCheckTime.toString().substring(0, 19) + " " + ";");

			} else {
				list.add(timecardid + " "
						+ userCheckTime.toString().substring(0, 19));
			}
			// 调用后台导入数据服务
			String fileContent[] = list.toArray(new String[list.size()]);
			String returnStr = NCLocator
					.getInstance()
					.lookup(IImportDataManageMaintain.class)
					.importData(
							pk_org,
							null,
							fileContent,
							TimezoneUtil.getTimeZone("0001Z010000000079U2P"),
							currdate.toUFLiteralDate(TimezoneUtil
									.getTimeZone("0001Z010000000079U2P")),
							currdate.toUFLiteralDate(TimezoneUtil
									.getTimeZone("0001Z010000000079U2P")));
			Logger.error(returnStr);

		}
	}

	/**
	 * 校验GPS误差
	 * 
	 * @param lng
	 *            经度
	 * @param lat
	 *            纬度
	 * @param userid
	 *            用户主键
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	private boolean checkGPS(double lng, double lat, String userid, int t,
			String pk_org) throws BusinessException {
		// 获得系统设置的默认GPS，先查部门的，若部门没有再去查公司的
		// userid = "0001AA100000000ALRU4";
		// String sql = "select memo from bd_defdoc where code='worktime' ";
		// String defGPS = (String) getBaseDao().executeQuery(sql, new
		// ColumnProcessor());
		// if (defGPS == null || defGPS.length() == 0) {
		// sql = "SELECT c.def19 FROM bd_corp c where c.pk_corp = '" + pk_org +
		// "'";
		// defGPS = (String) getBaseDao().executeQuery(sql, new
		// ColumnProcessor());
		// }
		// defGPS="116.315433,40.057864";
		String sql = "SELECT def19 FROM org_dept WHERE pk_dept IN (select pk_dept from bd_psnjob bpb join bd_psndoc bpc"
				+ " on bpb.pk_psndoc = bpc.pk_psndoc  join sm_user su   on su.pk_psndoc = bpb.pk_psndoc WHERE su.cuserid = '"
				+ userid + "')";
		String defGPS = (String) getBaseDao().executeQuery(sql,
				new ColumnProcessor());
		if (defGPS == null || defGPS.length() == 0) {
			sql = "SELECT c.def19 FROM org_orgs c where c.pk_org = '" + pk_org
					+ "'";
			defGPS = (String) getBaseDao().executeQuery(sql,
					new ColumnProcessor());
		}
		if (defGPS == null || defGPS.trim().length() == 0 || "".equals(defGPS)) {
			throw new BusinessException("未设置打卡的GPS坐标！");
		}

		String[] dus = defGPS.split(",");
		double defLng = Double.parseDouble(dus[0]);
		double defLat = Double.parseDouble(dus[1]);
		// double defLng = 108.896657;
		// double defLat = 34.221573;
		double a, b, R;
		R = 6378137; // 地球半径（米）
		defLat = defLat * Math.PI / 180.0;
		lat = lat * Math.PI / 180.0;
		a = defLat - lat;
		b = (defLng - lng) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2
				* R
				* Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(sa2) * Math.cos(lat)
						* sb2 * sb2));
		return d < t;
	}

	@Override
	public Object queryNoPage(String userid, Object obj)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		// ConditionAggVO condAggVO = (ConditionAggVO)obj;
		// ConditionVO[] condVOs = condAggVO.getConditionVOs();
		// String whereSQL = " and (1=1) ";
		// if (condVOs!=null && condVOs.length>0) {
		// for( int i=0;i<condVOs.length;i++ )
		// {
		// // PK
		// if( IVOField.PK.equals( condVOs[i].getField() )
		// && PuPubVO.getString_TrimZeroLenAsNull( condVOs[i].getValue() ) !=
		// null
		// )
		// {
		// whereSQL += " and M.pk_attendance_record = '" +condVOs[i].getValue()+
		// "' ";
		// }
		// }
		// }
		/*
		 * StringBuffer querySQL = new StringBuffer("")
		 * .append("select * from ( ")
		 * .append("select a.*,rownum as rowno from ( ") .append("select ")
		 * .append(" M .pk_attendance_record,") // PK .append(" M .vbillno,")//
		 * 单据号 .append(" M .pk_billtype, ")//单据类型
		 * .append(" M .dbilldate, ")//单据日期 .append(" M .dmakedate, ")//制单日期
		 * .append(" M .voperatorid, ")//操作员 .append(" M .pk_corp, ")//公司
		 * .append(" M .consumer, ")//用户 .append(" M .staff, ")//人员
		 * .append(" M .department, ")//部门 .append(" M .dk_time, ")//打卡时间
		 * .append(" M .bg_address, ")//办公位置 .append(" M .gps, ")//GPS坐标
		 * .append(" M .dk_address, ")//打卡地点 .append(" M .kq_type, ")//考勤类型
		 * .append(" M .vsourcebilltype, ")//源头单据类型
		 * .append(" M .vsourcebillid, ")//源头单据id .append(" M .remark ")//备注
		 * .append(" from m_attendance_record M ") //
		 * .append(" inner join sm_user sm on M.consumer = sm.cuserid ")
		 * .append(" where nvl(M.dr,0)=0 ") .append(whereSQL) //
		 * .append(" and sm.cuserid = '"+userid+"' ") .append(" )a ")
		 * .append(" )temp ")
		 * .append(" where rowno between "+startnum+" and "+endnum+" ");
		 */
		String stardate = new UFDate().getYear() + "-01-01 00:00:00";
		String scsql = "select\n"
				+ "       bd_psndoc.pk_psndoc,\n"
				+ "       bd_psndoc.name,\n"
				+ "       bd_psndoc.pk_org,\n"
				+ "              bd_workcalendardate.calendardate||' 00:00:00' calendardate\n"
				+ "  from bd_workcalendardate, bd_psndoc\n"
				+ " where bd_workcalendardate.calendardate <= to_char(sysdate, 'yyyy-MM-dd')\n"
				+ "   and bd_workcalendardate.calendardate >= '"
				+ stardate
				+ "'\n"
				+ "and bd_workcalendardate.datetype=0 and bd_psndoc.pk_psndoc = (select pk_base_doc from sm_user where cuserid='"
				+ userid + "')\n"
				+ " order by bd_workcalendardate.calendardate desc";

		StringBuffer sb = dealCondition(obj, true);
		String condition = sb.toString().replaceAll("HM02.",
				"hm_attendancerecord.");
		condition = sb.toString().replaceAll("pk_corp",
				"hm_attendancerecord.pk_org");
		String querysql =

		"select *\n"
				+ "  from (select rownum rowno,\n"
				+ "               b.def10,\n"
				+ "               b.def1,\n"
				+ "               b.gps, b.def2,\n"
				+ "               b.dk_time dk_time,\n"
				+ "               b.dk_address,\n"
				+ "               substr(b.dbilldate, 0, 10) dbilldate,\n"
				+ "               substr(b.dbilldate, 11, 20) def5,\n"
				+ "               case\n"
				+ "                 when b.kq_type = '0' then\n"
				+ "                  '未打卡'\n"
				+ "                 when b.kq_type = '1' then\n"
				+ "                  '正常'\n"
				+ "                 when b.kq_type = '2' then\n"
				+ "                  '迟到'\n"
				+ "                 when b.kq_type = '3' then\n"
				+ "                  '早退'\n"
				+ "                 when b.kq_type = '4' then\n"
				+ "                  '旷工半天'\n"
				+ "                 when b.kq_type = '5' then\n"
				+ "                  '旷工'\n"
				+ "                 when b.kq_type = '6' then\n"
				+ "                  '补签'\n"
				+ "                 when b.kq_type = '8' then\n"
				+ "                  '请假'\n"
				+ "               end as kq_type,\n"
				+ "               b.remark,\n"
				+ "               b.pk_org\n"
				+ "          from (select distinct sc.name def1,\n"
				+ "                                temp.pk_dept def10,\n"
				+ "                                nvl(max_dktime, calendardate) dk_time,\n"
				+ "                                sc.pk_psndoc def3,\n"
				+ "                                dk_address,def2,\n"
				+ "                                nvl(dbilldate, calendardate) dbilldate,\n"
				+ "                                gps,\n"
				+ "                                nvl(temp.kq_type, '5') kq_type,\n"
				+ "                                remark,\n"
				+ "                                nvl(temp.pk_org, sc.pk_org) pk_org\n"
				+ "                  from ("
				+ scsql
				+ ") sc\n"
				+ "                  left join (select cc.*,\n"
				+ "                                   greatest((select max(kq_type)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) kq_type,\n"
				+ "                                   greatest((select max(dk_address)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) dk_address,\n"
				+ "                                   greatest((select max(gps)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) gps,\n"
				+ "                                   greatest((select max(remark)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) remark,\n"
				+ "                                   greatest((select max(def10)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) def10\n"
				+ "                              from (select max(pk_dept) pk_dept,\n"
				+ "                                           MIN(min_dktime) max_dktime, (min(min_dktime) || '--' || max(max_dktime)) def2,\n"
				+ "                                           max(pk_org) pk_org,\n"
				+ "                                           def1,\n"
				+ "                                           dbilldate\n"
				+ "                                      from (select (pk_dept),\n"
				+ "\n"
				+ "                                                  dk_time as min_dktime, dk_time as max_dktime, "
				+ "                                                   (pk_org),\n"
				+ "                                                   def1,\n"
				+ "                                                   substr(dbilldate, 0, 10) dbilldate\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where nvl(dr, 0) = 0\n"
				+ "                                               and creator = '"
				+ userid
				+ "'\n"
				+ condition
				+ "                                             order by substr(dbilldate, 0, 10) desc)\n"
				+ "                                     group by def1, dbilldate\n"
				+ "                                     order by dbilldate desc) cc\n"
				+ "\n"
				+ "                            union all\n"
				+ "\n"
				+ "                            select cc.*,\n"
				+ "                                   greatest((select max(kq_type)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) kq_type,\n"
				+ "\n"
				+ "                                   greatest((select max(dk_address)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) dk_address,\n"
				+ "                                       greatest((select max(gps)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) gps,\n"
				+ "                                   greatest((select max(remark)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) remark,\n"
				+ "                                   greatest((select max(def10)\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where def1 = cc.def1\n"
				+ "                                               and dk_time = cc.max_dktime\n"
				+ "                                               and nvl(dr, 0) = 0)) def10\n"
				+ "                              from (select max(pk_dept) pk_dept,\n"
				+ "                                           MAX(min_dktime) max_dktime, (min(min_dktime) || '--' || max(max_dktime)) def2,\n"
				+ "                                           max(pk_org) pk_org,\n"
				+ "                                           def1,\n"
				+ "                                           dbilldate\n"
				+ "                                      from (select (pk_dept),\n"
				+ "\n"
				+ "                                                  dk_time as min_dktime, dk_time as max_dktime, "
				+

				"                                                   (pk_org),\n"
				+ "                                                   def1,\n"
				+ "                                                   substr(dbilldate, 0, 10) dbilldate\n"
				+ "                                              from hm_attendancerecord\n"
				+ "                                             where nvl(dr, 0) = 0\n"
				+ "                                               and creator = '"
				+ userid
				+ "'\n"
				+ condition
				+ "                                             order by substr(dbilldate, 0, 10) desc)\n"
				+ "                                     group by def1, dbilldate\n"
				+ "                                     order by dbilldate desc) cc) temp\n"
				+ "                    on sc.pk_psndoc = temp.def1\n"
				+ "                   and substr(sc.calendardate, 0, 10) =\n"
				+ "                       substr(temp.dbilldate, 0, 10)\n"
				+ "                 order by dbilldate desc) b)\n"
				+ "               where rowno between "
				+ startnum
				+ " and "
				+ endnum + "" + " order by dbilldate desc";

		@SuppressWarnings("unchecked")
		ArrayList<AttendanceRecordVO> list = (ArrayList<AttendanceRecordVO>) getBaseDao()
				.executeQuery(querysql.toString(),
						new BeanListProcessor(AttendanceRecordVO.class));
		if (list.isEmpty()) {
			return null;
		}

		// 针对非当天的考勤的特殊处理，只有上午打卡或者下午打卡的，考勤类型设置为全天旷工
		// jinfei 2017-10-09
		UFDate current_date = new UFDate(System.currentTimeMillis());
		for (int i = 0; i < list.size(); i++) {
			AttendanceRecordVO arecordVO = list.get(i);
			int rows = Integer.parseInt(list.get(i).getAttributeValue("rowno")
					.toString());
			if (i != 0
					&& !list.get(i)
							.getAttributeValue("dbilldate")
							.toString()
							.equals(list.get(i - 1)
									.getAttributeValue("dbilldate").toString())) {
				UFDate dw = new UFDate(list.get(i).getAttributeValue("dk_time")
						.toString());
				String bztime = list.get(i).getAttributeValue("dbilldate")
						.toString().substring(0, 10)
						+ " 17:00:00";
				UFDate bz = new UFDate(bztime);
				if (dw.before(bz)) {
					arecordVO.setKq_type("旷工");
				}
			}
			String def2 = arecordVO.getDef2();
			if (def2 != null && def2.contains("--")) {
				String[] split = def2.split("--");
				if (split.length == 2
						&& !split[0].startsWith(current_date.toString())) {
					// 考勤记录非当天
					UFDateTime startdatetime = new UFDateTime(split[0]);
					UFDateTime enddatetime = new UFDateTime(split[1]);
					if (startdatetime.getHour() >= 12
							|| enddatetime.getHour() < 12) {
						// 只有上午打卡，或者下午打卡
						arecordVO.setKq_type("旷工");
					}
				} else if (split.length == 2
						&& split[0].startsWith(current_date.toString())) {
					// 考勤记录当天，只有下午打卡，设置为旷工全天
					UFDateTime startdatetime = new UFDateTime(split[0]);
					UFDateTime enddatetime = new UFDateTime(split[1]);
					if (startdatetime.getHour() >= 12) {
						// 只有上午打卡，或者下午打卡
						arecordVO.setKq_type("旷工");
					}
				} else if (arecordVO.getDef1().equals(current_date.toString())) {
					// 当天还未打卡
					UFDateTime current_datetime = new UFDateTime(
							System.currentTimeMillis());
					if (current_datetime.getHour() < 12) {
						// 12点之前，设置为未打卡
						arecordVO.setKq_type("正常");
					}
				}
			}
		}

		HashMap<String, Object>[] headVOs = transNCVOTOMap(list
				.toArray(new AttendanceRecordVO[list.size()]));
		BillVO[] billVOs = new BillVO[headVOs.length];
		for (int i = 0; i < headVOs.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = headVOs[i];
			// String[] formulas = new String[] {
			// "staff_name->getcolvalue2(bd_psndoc,psnname,pk_psnbasdoc,staff,pk_corp,pk_corp)",
			// "deptcode->getcolvalue(bd_deptdoc,deptcode,pk_deptdoc,department)",
			// "department_name->getcolvalue(bd_deptdoc,deptname,pk_deptdoc,department)",
			// "dk_time->def1", "ibillstatus->1",
			// "kq_type_name->iif(kq_type==\"0\",\"未打卡\",iif(kq_type==\"1\",\"正常\",iif(kq_type==\"2\",\"迟到\",iif(kq_type==\"3\",\"早退\",iif(kq_type==\"4\",\"旷工半天\",iif(kq_type==\"5\",\"旷工全天\",\"补签\"))))))"
			// };
			// PubTools.execFormulaWithVOs(new HashMap[] { headVO }, formulas);
			headVO.put(IVOField.BILLSTATUS, IBillStatus.COMMIT);
			headVO.put("vbillstatus_name", IBillStatus.COMMIT);
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
			/*
			 * headVO.put("pk_attendance_record", value[0]);
			 * headVO.put("pk_billtype", value[2]); headVO.put("dbilldate",
			 * value[3]); headVO.put("dmakedate", value[4]);
			 * headVO.put("voperatorid", value[5]); headVO.put("pk_corp",
			 * value[6]); headVO.put("consumer", value[7]); headVO.put("staff",
			 * value[8]); headVO.put("department", value[9]);
			 * headVO.put("dk_time", value[10]); headVO.put("bg_address",
			 * value[11]); headVO.put("gps", value[12]);
			 * headVO.put("dk_address", value[13]); headVO.put("kq_type",
			 * value[14]); headVO.put("vsourcebilltype", value[15]);
			 * headVO.put("vsourcebillid", value[16]); headVO.put("remark",
			 * value[17]);
			 */
			// headVO.put("consumer", value[0]);
			// headVO.put("dk_time", ""+value[1]+"--"+value[2]);
			// headVO.put("staff", value[3]);
			// billVO.setHeadVO(headVO);
			// billVOs[i] = billVO;
		}

		// 返回的结果集VO
		QueryBillVO querybillVO = new QueryBillVO();
		querybillVO.setQueryVOs(billVOs);

		return querybillVO;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 根据pk查询出BillVO
	 * 
	 * @param userid
	 * @param PK
	 * @return
	 * @throws BusinessException
	 */
	public Object queryBillVoByPK(String userid, String PK)
			throws BusinessException {
		BillVO billVO = new BillVO();
		ConditionAggVO condAggVO = new ConditionAggVO();
		ConditionVO[] condVOs = new ConditionVO[1];
		condVOs[0] = new ConditionVO();
		condVOs[0].setField(IVOField.PK);
		condVOs[0].setValue(PK);
		condAggVO.setConditionVOs(condVOs);

		// 查询
		QueryBillVO querybillVO = (QueryBillVO) this.queryPage(userid,
				condAggVO, 1, 1);
		BillVO vo = querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(vo.getHeadVO());
		return billVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

}
