package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.QueryBillVO;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.wa.payslip.ctrl.MyPaySlip;
import nc.bs.logging.Logger;
import nc.itf.hr.wa.IPayslipService;
import nc.itf.hrss.pub.admin.IConfigurationService;
import nc.itf.hrss.pub.admin.IHROrgProvider;
import nc.itf.uap.busibean.SysinitAccessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sm.UserVO;
import nc.vo.wa.payslip.MyPayslipVO;
import nc.vo.wa.payslip.PaySlipItemValueVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 我的薪资查询
 * 
 * @author 贺阳
 */
public class WaCategoryAction extends AbstractMobileAction {
	private BaseDAO baseDAO;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}

	private final Integer WA_TYPE = 3;
	public static final String ZERO_SEND = "HRWA016";
	private String PARAM_BATCH = "离职结薪";
	private String TITLE_SUM = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
			.getStrByID("c_wa-res", "0c_wa-res0030")/*
													 * @ res
													 * "&nbsp;&nbsp;&nbsp;&nbsp;合计&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(&nbsp;{0}&nbsp;)"
													 */;
	private String TITLE_LIP = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
			.getStrByID("c_wa-res", "0c_wa-res0001")/*
													 * @ res
													 * "&nbsp;&nbsp;&nbsp;&nbsp;{0}年 {1}月&nbsp;&nbsp;&nbsp;&nbsp;{2}&nbsp;&nbsp;&nbsp;&nbsp;{3}&nbsp;&nbsp;&nbsp;&nbsp;(&nbsp;{4}&nbsp;)"
													 */;
	private String TITLE_LIP_BATCH = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
			.getStrByID("c_wa-res", "0c_wa-res0002")/*
													 * @ res
													 * "&nbsp;&nbsp;&nbsp;&nbsp;{0}年 {1}月&nbsp;&nbsp;&nbsp;&nbsp;{2}&nbsp;&nbsp;&nbsp;&nbsp;{3}&nbsp;&nbsp;&nbsp;&nbsp;第{4}次发薪&nbsp;&nbsp;&nbsp;&nbsp;(&nbsp;{5}&nbsp;)"
													 */;

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		// 返回的结果集VO
		QueryBillVO querybillVO = new QueryBillVO();
		StringBuffer strwhere = dealCondition(obj, false);
		String[] cons = strwhere.toString().split("and");
		String startDate = "";
		String endDate = "";
		if (startnum > 1) {
			return null;
		} else {
			if (cons.length == 1) {
				startDate = new UFDate().toString().substring(0, 7);
				endDate = startDate;
				// throw new RuntimeException("请输入期间查询!");
			} else {
				if (!strwhere.toString().contains("f_period")) {

					throw new RuntimeException("请输入期间起始年月!");
				}
				if (!strwhere.toString().contains("to_period")) {

					throw new RuntimeException("请输入期间结束年月!");
				}
				startDate = cons[1].substring(cons[1].indexOf("'") + 1,
						cons[1].lastIndexOf("'"));
				endDate = cons[2].substring(cons[2].indexOf("'") + 1,
						cons[2].lastIndexOf("'"));
			}

		}

		// 将年月类型转换成年月日时分秒类型(yyyy-mm-dd 00:00:00)
		String beginYear = startDate.substring(0, 4);
		String endYear = endDate.substring(0, 4);
		String beginMonth = startDate.substring(5, 7);
		String endMonth = endDate.substring(5, 7);
		if (beginYear != null && beginMonth != null) {
			startDate = startDate.substring(0, 7) + "-01 00:00:00";

		}
		if (endYear != null && endMonth != null) {
			endDate = endDate.substring(0, 7)
					+ "-"
					+ UFLiteralDate.getDaysMonth(Integer.parseInt(endYear),
							Integer.parseInt(endMonth)) + " 00:00:00";

		}
		if (!StringUtil.isEmptyWithTrim(startDate)
				&& !StringUtil.isEmptyWithTrim(endDate)) {
			if (Integer.parseInt(beginYear) > Integer.parseInt(endYear)) {

				throw new RuntimeException("薪资期间开始年月不能晚于结束年月！");

			}

			if (Integer.parseInt(beginYear) == Integer.parseInt(endYear)
					&& Integer.parseInt(beginMonth) > Integer
							.parseInt(endMonth)) {
				throw new RuntimeException("薪资期间开始年月不能晚于结束年月！");

			}
		}

		try {

			// 获取零值项目控制参数
			UFBoolean zeroPara = SysinitAccessor.getInstance().getParaBoolean(
					getHROrg(userid, "E20200501", false), ZERO_SEND);
			if (zeroPara == null) {
				zeroPara = UFBoolean.TRUE;
			}

			// 获得员工的薪资条列表
			List<MyPayslipVO> list = getMyPaysLipList(userid, startDate,
					endDate);
			if (list == null || list.size() == 0) {

				throw new RuntimeException("没有找到和搜索条件匹配的工资条数据！");

			}
			// 合计列排序集合
			ArrayList<String> arrOrder = new ArrayList<String>();
			// 合计列Map： key： 薪资项目 , value 项目值
			Map<String, Map<String, UFDouble>> sumMaps = new HashMap<String, Map<String, UFDouble>>();
			StringBuffer mySlipStr = new StringBuffer("");
			for (int i = 0; i < list.size(); i++) {
				// 是否是该薪资条的第一个显示项目
				boolean isFirst = true;
				// 员工的薪资条
				MyPayslipVO vo = list.get(i);
				// 薪资项目数组
				PaySlipItemValueVO[] itemVOs = vo.getPaySlipVOs();
				StringBuffer itemsJson = new StringBuffer("");
				// 在零值项目控制参数控制下不显示薪资项目个数
				int count = 0;
				for (int j = 0; j < itemVOs.length; j++) {
					boolean isRight = true;
					// 薪资项目VO
					PaySlipItemValueVO itemVO = itemVOs[j];
					// 薪资项目的名称
					String itemName = itemVO.getName();
					// 薪资项目的值
					String itemValue = itemVO.getValue() == null ? "" : itemVO
							.getValue().toString();
					// 在是我的薪资条项目的前提下，再根据零值项目控制参数来判断薪资项目是否需要实现
					if (itemVO.getIsEmpPro().booleanValue()) {
						if (itemVO.getDataType() == null
								|| itemVO.getDataType() == 2) {
							if (Double
									.parseDouble(itemVO.getValue().toString()) == 0.0) {
								if (!zeroPara.booleanValue()) {
									++count;
									itemValue = "";
									isRight = false;
								}
							}
						}
					} else {
						isRight = false;
					}

					if (isRight) {
						// 拼接 薪资项目的Json串
						itemsJson.append(getItemJsonString(isFirst, j,
								itemName, itemValue, itemVO.getDataType()));
						isFirst = false;
						// 是合计项目并且薪资项目的数据类型是UFDouble，2表示数据类型是UFDouble
						if (itemVO.getIsCountPro().booleanValue()
								&& 2 == itemVO.getDataType()) {
							// 计算合计列的值
							calculateSumItem(sumMaps, itemVO, itemName, itemVO
									.getValue().toString(),
									vo.isMultiParentClass(), vo.getMoneyType(),
									arrOrder);
						}

					}
				}
				mySlipStr.append(getLipJsonString(i, getWaLipContext(vo),
						itemsJson.toString(), itemVOs.length - count));
			}
			// 制作合计列
			StringBuffer sumJsonStr = new StringBuffer("");
			if (list.size() > 1) {

				Iterator<String> it = sumMaps.keySet().iterator();
				int x = 0;
				while (it.hasNext()) {// 循环币种
					boolean isFirst = true;
					String MoneyType = (String) it.next();
					Map<String, UFDouble> itemMap = sumMaps.get(MoneyType);
					StringBuffer sumItemJson = new StringBuffer("");
					for (int i = 0; i < arrOrder.size(); i++) {
						// 循环币种下的合计项目
						String sumItemName = arrOrder.get(i);
						if (itemMap.get(sumItemName) != null) {
							String sumItemValue = itemMap.get(sumItemName)
									.toString();
							if (Double.parseDouble(sumItemValue) == 0.0) {
								if (!zeroPara.booleanValue()) {
									sumItemValue = "";
								}
							}
							sumItemJson.append(getItemJsonString(isFirst, i,
									sumItemName, sumItemValue, 2));
							isFirst = false;
						}
					}

					// 显示内容
					String context = MessageFormat.format(TITLE_SUM, MoneyType);
					sumJsonStr.append(getLipJsonString(x, context,
							sumItemJson.toString(), arrOrder.size()));
					x++;
				}
			}
			StringBuffer jsonBuf = new StringBuffer("");
			jsonBuf.append(mySlipStr.toString());
			if (!StringUtil.isEmptyWithTrim(sumJsonStr.toString())) {
				jsonBuf.append(",");
				jsonBuf.append(sumJsonStr.toString());
			}
			JSONArray jsonarray = new JSONArray("[" + jsonBuf.toString() + "]");
			BillVO[] billVOs = new BillVO[jsonarray.length()];
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject jo = jsonarray.getJSONObject(i);
				String tital = (String) jo.get("tital");// 工资条名称
				BillVO billVO = new BillVO();
				HashMap<String, Object> headVO = new HashMap<String, Object>();
				headVO.put("data", tital);
				headVO.put(IVOField.PK, jo.get("detail").toString());
				headVO.put("funccode", "MUJ00115");
				headVO.put("menucode", "MUJ00115_detail");
				headVO.put("menuname", "薪资明细");
				headVO.put("pk_billtype", "MUJ00115");
				headVO.put("isbuttonpower", "N");
				headVO.put("level", "F");
				headVO.put("uistyle", "R-T");
				billVO.setHeadVO(headVO);
				billVOs[i] = billVO;
			}
			querybillVO.setQueryVOs(billVOs);
		} catch (Exception ex) {
			throw new RuntimeException("没有找到和搜索条件匹配的工资条数据！");
		}

		return querybillVO;
	}

	/**
	 * 根据功能节点号和对象类型获取当前对象的管理HR组织
	 * 
	 * @param funCode
	 *            功能节点号
	 * @param byDept
	 *            是否部门对象
	 * @return
	 * @author haoy 2011-12-19
	 */
	public String getHROrg(String userid, String funCode, boolean byDept) {
		String org = null;
		String sql = "select * from hi_psnjob where exists (select pk_psndoc from sm_user where   cuserid='"
				+ userid
				+ "'and hi_psnjob.pk_psndoc=sm_user.pk_psndoc ) and dr=0 and lastflag='Y'";
		try {
			List<PsnJobVO> list = (List<PsnJobVO>) getQuery().executeQuery(sql,
					new BeanListProcessor(PsnJobVO.class));

			IHROrgProvider provider = ServiceLocator.lookup(
					IConfigurationService.class).getHROrgProvider(funCode);

			org = provider.getPsnHROrg(list.get(0));

		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.deal();
		}
		return org;
	}

	/**
	 * 获得我的薪资的薪资条的显示名称
	 * 
	 * @author 贺阳
	 * @param vo
	 * @return
	 */
	private String getWaLipContext(MyPayslipVO vo) {
		String context = null;

		// 薪资发放次数字段, 薪资方案不能多次发放时,值为0;
		int batch = vo.getBatch();
		if (0 < batch && batch <= 100) {
			// 显示内容 --{0}期间;{1}组织名;{2}方案名称;{3}发放次数;{4}币种;
			context = MessageFormat.format(TITLE_LIP_BATCH, vo.getCyear(),
					vo.getCperiod(), vo.getOrgName(), vo.getWaClassName(),
					vo.getBatch(), vo.getMoneyType());
		} else {
			if (batch > 100) {
				// 显示内容 --{0}期间;{1}组织名;{2}方案名称;{3}离职结薪;{4}币种;
				context = MessageFormat.format(TITLE_LIP_BATCH, vo.getCyear(),
						vo.getCperiod(), vo.getOrgName(), vo.getWaClassName(),
						PARAM_BATCH, vo.getMoneyType());
			} else {
				// 显示内容 --{0}期间;{1}组织名;{2}方案名称;{4}币种;
				context = MessageFormat.format(TITLE_LIP, vo.getCyear(),
						vo.getCperiod(), vo.getOrgName(), vo.getWaClassName(),
						vo.getMoneyType());
			}

		}
		return context;
	}

	/**
	 * 根据员工主键、起始日期和截至日期, 获得我的薪资条列表数据
	 * 
	 * @author 贺阳
	 * @param start
	 * @param end
	 * @return
	 */
	private List<MyPayslipVO> getMyPaysLipList(String userid, String start,
			String end) {
		// 薪资条查询接口
		IPayslipService service = NCLocator.getInstance().lookup(
				IPayslipService.class);
		List<MyPayslipVO> list = null;
		// 员工主键
		String pk_psndoc = null;
		try {
			String sql = "select * from sm_user where   cuserid='" + userid
					+ "'";
			List<UserVO> user_list = (List<UserVO>) getQuery().executeQuery(
					sql, new BeanListProcessor(UserVO.class));
			pk_psndoc = user_list.get(0).getPk_psndoc();
			// 根据自然开始日期和截至日期及人员主键，查询该员工的薪资条列表
			list = service.querySelfAggPayslipVOs(start, end, pk_psndoc,
					WA_TYPE);
		} catch (BusinessException ex) {
			throw new LfwRuntimeException("HRSS.PUB.2", "IPayslipService", ex);
		}
		return list;
	}

	/**
	 * 获得一个薪资条的Json串<br/>
	 * 格式1：{tital:"XXXXXX", detail:{item0:{name:"2位",value:"10000.00"}} }<br/>
	 * 
	 * @author 贺阳
	 * @param index
	 * @param name
	 * @param value
	 * @return
	 */
	private String getLipJsonString(int index, String context, String detail,
			int length) {
		StringBuffer lipJson = new StringBuffer("");
		if (index > 0) {
			lipJson.append(",");
		}
		lipJson.append("{");
		lipJson.append("tital:\"" + context + "\"");
		// 63增加一个length(每个薪资条中薪资项目的个数)
		lipJson.append(",length:\"" + length + "\"");
		// tianxx 将属于全局的公共薪资项目的精度设定为默认是2
		detail = MyPaySlip.splitString(detail);
		lipJson.append(", detail:{" + detail + "}");
		lipJson.append("}");
		return lipJson.toString();
	}

	/**
	 * 获得一个薪资项目的Json串
	 * 
	 * @author 贺阳
	 * @param index
	 * @param name
	 * @param value
	 * @return
	 */
	private String getItemJsonString(boolean isFirst, int index, String name,
			String value, int dataType) {
		StringBuffer itemStr = new StringBuffer("");
		if (!isFirst) {
			itemStr.append(",");
		}
		itemStr.append("item" + index + ": {");
		itemStr.append("name:\"" + name + "\"");
		itemStr.append(",");
		itemStr.append("type:\"" + dataType + "\"");
		itemStr.append(",");
		itemStr.append("value:\"" + value + "\"");
		itemStr.append("}");
		return itemStr.toString();
	}

	/**
	 * 计算合计列的值
	 * 
	 * @author 贺阳
	 * @param sumMap
	 * @param MoneyType
	 * @param itemVO
	 * @param itemName
	 * @param itemValue
	 */
	private void calculateSumItem(Map<String, Map<String, UFDouble>> sumMaps,
			PaySlipItemValueVO itemVO, String itemName, String itemValue,
			boolean isMultiParentClass, String moneyType,
			ArrayList<String> arrOrder) {
		Map<String, UFDouble> sumMap = sumMaps.get(moneyType);
		if (sumMap == null) {
			sumMap = new HashMap<String, UFDouble>();
			sumMaps.put(moneyType, sumMap);
		}
		if (itemVO.getIsCountPro() != null
				&& itemVO.getIsCountPro().booleanValue()) {
			if (!arrOrder.contains(itemVO.getName())) {
				arrOrder.add(itemVO.getName());
			}
			if (sumMap.containsKey(itemName)) {
				UFDouble sum = sumMap.get(itemVO.getName()).add(
						new UFDouble(itemValue));
				sumMap.remove(itemName);
				sumMap.put(itemName, sum);
			} else {
				sumMap.put(itemName, new UFDouble(itemValue, 2));
			}
		}
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		if (startnum > 1) {
			return null;
		}
		QueryBillVO querybillVO = new QueryBillVO();
		StringBuffer strwhere = dealCondition(obj, false);
		String cons = strwhere.toString().substring(
				strwhere.toString().indexOf("'") + 1,
				strwhere.toString().lastIndexOf("'"));
		StringBuffer html = new StringBuffer();
		try {
			JSONObject jsonobj = new JSONObject(cons.replace("item", ""));
			BillVO[] billVOs = new BillVO[1];
			BillVO billVO = new BillVO();
			HashMap<String, Object> bodyVO = new HashMap<String, Object>();
			// 获取明细列明
			String namesindex[] = jsonobj.getNames(jsonobj);
			int index[] = new int[namesindex.length];

			// 明细条目下标字符串数组转换成int数组
			for (int i = 0; i < namesindex.length; i++) {
				index[i] = Integer.parseInt(namesindex[i]);
			}
			;
			// 转换后的下标int数组进行升序排列
			Arrays.sort(index);
			for (int i = 0; i < index.length; i++) {
				JSONObject itemjson = (JSONObject) jsonobj.get(""+index[i]);
				String itemname = (String) itemjson.get("name");
				String itemvalue = (String) itemjson.get("value");

				html.append("<div style='text-align:left;heigth:auto;'><span style='color:#000000;'> "
						+ itemname + ":" + itemvalue + " </span></p>");

			}
			;
			bodyVO.put("bodyhtml", html.toString());
			billVO.getBodyVOsMap().put("detail", new HashMap[] { bodyVO });
			billVOs[0] = billVO;
			querybillVO.setQueryVOs(billVOs);

		} catch (JSONException e) {
			Logger.error(e.getMessage());
			throw new BusinessException(e.getMessage());
		}
		return querybillVO;
	}

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage(String userid, Object obj)
			throws BusinessException {

		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj)
			throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		return null;
	}

}
