package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.QueryBillVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.logging.Logger;
import nc.hr.utils.ResHelper;
import nc.itf.bd.psn.psndoc.IPsndocQueryService;
import nc.itf.hr.bm.HRBMFacadeForHrss;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.bd.psn.PsnjobVO;
import nc.vo.bm.bmclass.BmClassItemVO;
import nc.vo.bm.data.BmDataVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.om.orginfo.AggHROrgVO;
import nc.vo.om.orginfo.HROrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sm.UserVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * �ҵ�н�ʲ�ѯ
 * 
 * @author ����
 */
public class MySheBaoAction extends AbstractMobileAction {
	private BaseDAO baseDAO;
	private IPsndocQueryService manageService;
	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}

	public static final String ZERO_SEND = "HRWA016";

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		// ���صĽ����VO
		QueryBillVO querybillVO = new QueryBillVO();
		StringBuffer strwhere = dealCondition(obj, false);
		String[] cons = strwhere.toString().split("and");
		String startDate = "";
		String endDate = "";
		String[] xianzhong = null;
		if (startnum > 1) {
			return null;
		} else {
			if (cons.length == 1) {
				startDate = new UFDate().toString().substring(0, 7);
				endDate = startDate;
			} else {
				if (!strwhere.toString().contains("from_period")) {

					throw new RuntimeException("��������ʼ�ڼ�!");
				}
				if (!strwhere.toString().contains("to_period")) {

					throw new RuntimeException("��������ֹ�ڼ�!");
				}
				startDate = cons[1].substring(cons[1].indexOf("'") + 1,
						cons[1].lastIndexOf("'"));
				endDate = cons[2].substring(cons[2].indexOf("'") + 1,
						cons[2].lastIndexOf("'"));
				if (strwhere.toString().contains("xianzhong")) {
					xianzhong = cons[3].substring(cons[3].indexOf("'") + 1,
							cons[3].lastIndexOf("'")).split(",");

				}
			}

		}

		// ����������ת����������ʱ��������(yyyy-mm-dd 00:00:00)
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

				throw new RuntimeException("��ʼ�ڼ䲻��������ֹ�ڼ䣡");

			}

			if (Integer.parseInt(beginYear) == Integer.parseInt(endYear)
					&& Integer.parseInt(beginMonth) > Integer
							.parseInt(endMonth)) {
				throw new RuntimeException("��ʼ�ڼ䲻��������ֹ�ڼ䣡");

			}
		}

		try {

			HRBMFacadeForHrss facade = new HRBMFacadeForHrss();
			BmClassItemVO[] items = null;
			BmDataVO[] data = null;
			String pk_psndoc = null;
			try {
				// Ա������
				String sql = "select * from sm_user where   cuserid='" + userid
						+ "'";
				List<UserVO> user_list = (List<UserVO>) getQuery()
						.executeQuery(sql, new BeanListProcessor(UserVO.class));
				pk_psndoc = user_list.get(0).getPk_psndoc();
				items = facade.getHrssBmitem(pk_psndoc, xianzhong,
						startDate.toString(), endDate.toString());
				data = facade.getHrssBmData(pk_psndoc, xianzhong,
						startDate.toString(), endDate.toString());
			} catch (BusinessException e) {
				new HrssException(e).deal();
			}
			if (data == null || data.length == 0) {
				throw new RuntimeException("û���ҵ�����������ƥ����籣���ݣ�");

			}
			Map<String, String[][]> datas = getData(startDate, endDate, items,
					data);
			Set<String> periods = datas.keySet();
			if (periods.size() == 0)
				return null;
			String[] periodArr = (String[]) periods.toArray(new String[0]);
			StringBuffer lipJson = new StringBuffer("");
			Arrays.sort(periodArr, null);
			for (int i = 0; i < periodArr.length; i++) {
				String title = periodArr[i];
				if (title.contains("��")) {
					String[][] item = (String[][]) datas.get(periodArr[i]);
					lipJson.append("{");
					lipJson.append("tital:\"" + periodArr[i] + "\"");
					// 63����һ��length(ÿ��н������н����Ŀ�ĸ���)
					lipJson.append(",length:\"" + items.length + "\"");
					lipJson.append(", detail:{");

					StringBuffer itemStr = new StringBuffer("");
				
					int s=0;
						for (int k = 1; k < item.length; k++) {
						  for (int j = 0; j <item[0].length; j++) {
							  
							itemStr.append("item" + s + ": {");
							itemStr.append("name:\"" + item[0][j] + "\"");
							itemStr.append(",");
							itemStr.append("value:\"" + item[k][j]+ "\"");
							itemStr.append("},");
                            s++;
						   }
					}
					lipJson.append(itemStr.substring(0, itemStr.length() - 1));
					lipJson.append("}},");
				}

			}
			JSONArray jsonarray = new JSONArray("["
					+ lipJson.substring(0, lipJson.length() - 1).toString()
					+ "]");
			BillVO[] billVOs = new BillVO[jsonarray.length()];
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject jo = jsonarray.getJSONObject(i);
				String tital = (String) jo.get("tital");// ����������
				BillVO billVO = new BillVO();
				HashMap<String, Object> headVO = new HashMap<String, Object>();
				headVO.put("data", tital);
				headVO.put(IVOField.PK, jo.get("detail").toString());
				headVO.put("funccode", "MUJ00116");
				headVO.put("menucode", "MUJ00116_detail");
				headVO.put("menuname", "�籣��ϸ");
				headVO.put("pk_billtype", "MUJ00116");
				headVO.put("isbuttonpower", "N");
				headVO.put("level", "F");
				headVO.put("uistyle", "R-T");
				billVO.setHeadVO(headVO);
				billVOs[i] = billVO;
			}
			querybillVO.setQueryVOs(billVOs);
		} catch (Exception ex) {
			throw new RuntimeException("û���ҵ�����������ƥ����籣���ݣ�");
		}
		return querybillVO;
	}

	private Map<String, String[][]> getData(String beginStr, String endStr,
			BmClassItemVO[] itemVOs, BmDataVO[] dataVOs) {
		int beginYear = Integer.valueOf(beginStr.substring(0, 4)).intValue();
		int beginMonth = Integer.valueOf(beginStr.substring(5, 7)).intValue();
		int endYear = Integer.valueOf(endStr.substring(0, 4)).intValue();
		int endMonth = Integer.valueOf(endStr.substring(5, 7)).intValue();
		int periodLength = 0;
		Map<String, String[][]> map = new HashMap();

		List<String> listItemKey = new ArrayList();

		if (beginYear == endYear) {
			periodLength = endMonth - beginMonth + 1;
		} else {
			periodLength = (endYear - beginYear) * 12 + endMonth - beginMonth
					+ 1;
		}

		for (int i = 0; i < periodLength; i++) {
			String yearPeriod = "";
			if (Integer.valueOf(beginMonth).intValue() + i > 12) {
				if ((Integer.valueOf(beginMonth).intValue() + i) % 12 != 0) {
					int newYear = beginYear
							+ (Integer.valueOf(beginMonth).intValue() + i) / 12;
					int newMonth = (Integer.valueOf(beginMonth).intValue() + i) % 12;
					if (newMonth >= 10) {
						yearPeriod = "" + newYear + newMonth;
					} else {
						yearPeriod = "" + newYear + "0" + newMonth;
					}
				} else {
					int newYear = beginYear
							+ (Integer.valueOf(beginMonth).intValue() + i) / 12
							- 1;
					int newMonth = 12;

					yearPeriod = "" + newYear + newMonth;
				}

			} else {
				int newMonth = beginMonth + i;
				if (newMonth >= 10) {
					yearPeriod = "" + beginYear + newMonth;
				} else {
					yearPeriod = "" + beginYear + "0" + newMonth;
				}
			}

			List<String> listPK = new ArrayList();
			List<String> listItem = new ArrayList();
			for (int j = 0; j < itemVOs.length; j++) {
				if (yearPeriod.equals(itemVOs[j].getCyear()
						+ itemVOs[j].getCperiod())) {

					if (!listPK.contains(itemVOs[j].getPk_bm_class())) {
						listPK.add(itemVOs[j].getPk_bm_class());
					}
				}
			}

			for (int j = 0; j < itemVOs.length; j++) {
				if (yearPeriod.equals(itemVOs[j].getCyear()
						+ itemVOs[j].getCperiod())) {

					if (!listItem.contains(itemVOs[j].getItemkey())) {
						listItem.add(itemVOs[j].getItemkey());
					}
				}
			}

			if (listPK.size() >= 1) {

				String[][] data = new String[listPK.size() + 2][listItem.size() + 1];
				for (int j = 0; j < itemVOs.length; j++) {
					if (yearPeriod.equals(itemVOs[j].getCyear()
							+ itemVOs[j].getCperiod())) {

						for (int h = 0; h < dataVOs.length; h++) {
							if ((dataVOs[h].getPk_bm_class().equals(itemVOs[j]
									.getPk_bm_class()))
									&& (dataVOs[h].getCyear().equals(itemVOs[j]
											.getCyear()))
									&& (dataVOs[h].getCperiod()
											.equals(itemVOs[j].getCperiod()))) {

								int xIndex = listPK.indexOf(itemVOs[j]
										.getPk_bm_class()) + 1;
								int yIndex = listItem.indexOf(itemVOs[j]
										.getItemkey()) + 1;
								String value = null;

								if (itemVOs[j].getItemkey().startsWith("f")) {
									UFDouble dValue = (UFDouble) dataVOs[h]
											.getAttributeValue(itemVOs[j]
													.getItemkey());
									if (dValue != null) {
										dValue = new UFDouble(dValue).setScale(
												itemVOs[j].getIflddecimal()
														.intValue(), 4);
										value = dValue.toString();

										if ((itemVOs[j].getCategory_id() != null)
												&& (!itemVOs[j]
														.getCategory_id()
														.endsWith("1"))) {
											if (data[(listPK.size() + 1)][yIndex] != null) {
												String oldValue = data[(listPK
														.size() + 1)][yIndex];
												data[(listPK.size() + 1)][yIndex] = new UFDouble(
														oldValue).add(dValue)
														.toString();
											} else {
												data[(listPK.size() + 1)][yIndex] = dValue
														.toString();
											}

										}

									}
								} else if (itemVOs[j].getItemkey().startsWith(
										"c")) {
									value = (String) dataVOs[h]
											.getAttributeValue(itemVOs[j]
													.getItemkey());
								} else if (itemVOs[j].getItemkey().startsWith(
										"d")) {
									UFLiteralDate dValue = (UFLiteralDate) dataVOs[h]
											.getAttributeValue(itemVOs[j]
													.getItemkey());
									if (dValue != null) {
										value = dValue.toString();
									}
								}

								data[xIndex][yIndex] = (value == null ? null
										: value.toString());
								data[xIndex][0] = dataVOs[h].getClassname();
								data[0][yIndex] = itemVOs[j].getItemname();

								if (itemVOs[j].getItemkey().equals("f_1"))
									data[(listPK.size() + 1)][yIndex] = null;
							}
						}
					}
				}
				data[0][0] = ResHelper.getString("c_wa-res", "0c_wa-res0098");
				data[(listPK.size() + 1)][0] = ResHelper.getString("c_wa-res",
						"0c_wa-res0099");

				map.put(ResHelper.getString("c_wa-res", "0c_wa-res0097") + "��"
						+ yearPeriod, data);
			}
		}

		Map itemKeyMap = new HashMap();
		for (int i = 0; i < itemVOs.length; i++) {
			if ((itemVOs[i].getItemkey().startsWith("f"))
					&& ((itemVOs[i].getCategory_id() == null) || (!itemVOs[i]
							.getCategory_id().endsWith("1")))) {

				if (listItemKey.size() == 0) {
					listItemKey.add(itemVOs[i].getItemkey());
					itemKeyMap.put(itemVOs[i].getItemkey(),
							itemVOs[i].getItemname());
					itemKeyMap.put(itemVOs[i].getItemkey() + "pos",
							itemVOs[i].getIflddecimal());

				} else if (!listItemKey.contains(itemVOs[i].getItemkey())) {
					listItemKey.add(itemVOs[i].getItemkey());
					itemKeyMap.put(itemVOs[i].getItemkey(),
							itemVOs[i].getItemname());
					itemKeyMap.put(itemVOs[i].getItemkey() + "pos",
							itemVOs[i].getIflddecimal());
				} else if (((Integer) itemKeyMap.get(itemVOs[i].getItemkey()
						+ "pos")).intValue() < itemVOs[i].getIflddecimal()
						.intValue()) {
					itemKeyMap.put(itemVOs[i].getItemkey() + "pos",
							itemVOs[i].getIflddecimal());
				}
			}
		}
		String[][] total = new String[2][listItemKey.size()];
		for (int i = 0; i < listItemKey.size(); i++) {
			String count = "0";
			int precision = 0;

			for (BmClassItemVO vo : itemVOs) {
				if (vo.getItemkey().equals(listItemKey.get(i))) {

					for (int k = 0; k < dataVOs.length; k++) {
						if ((dataVOs[k].getPk_bm_class().equals(vo
								.getPk_bm_class()))
								&& (dataVOs[k].getCyear().equals(vo.getCyear()))
								&& (dataVOs[k].getCperiod().equals(vo
										.getCperiod()))) {

							UFDouble num = (UFDouble) dataVOs[k]
									.getAttributeValue((String) listItemKey
											.get(i));
							if (num != null) {
								precision = ((Integer) itemKeyMap
										.get((String) listItemKey.get(i)
												+ "pos")).intValue();
								count = new UFDouble(count).add(num)
										.setScale(precision, 4).toString();
							}
						}
					}
				}
			}
			total[0][i] = ((String) itemKeyMap.get(listItemKey.get(i)));
			total[1][i] = count;
		}
		if (listItemKey.size() > 0) {
			map.put(ResHelper.getString("c_wa-res", "0c_wa-res0100"), total);
		}

		return map;
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
			
			//ƴ����Ա�����Ϣ
			
			String sql = "select * from bd_psndoc where  pk_psndoc=(select pk_psndoc from sm_user where   cuserid='" + userid
					+ "')";
			List<PsndocVO> psn_list = (List<PsndocVO>) getQuery()
					.executeQuery(sql, new BeanListProcessor(PsndocVO.class));
			String pk_psndoc = psn_list.get(0).getPk_psndoc();
			String psncode=psn_list.get(0).getCode();
			String psnname=psn_list.get(0).getName();
			PsnjobVO psnjob=getManageService().queryPsnJobVOByPsnDocPK(pk_psndoc);
			
			Collection<AggHROrgVO> generalVOC = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(AggHROrgVO.class, " isnull(dr,0)=0 and pk_org='" + psnjob.getPk_org() + "' ", false);
			AggHROrgVO agghrorgvo=generalVOC.toArray(new AggHROrgVO[] {})[0];
			HROrgVO  hrorgheadvo=(HROrgVO) agghrorgvo.getParentVO();
			html.append("<div style='text-align:left;heigth:auto;'><span style='color:#000000;'> ��Ա����:" + psncode + " </span></p>");
			html.append("<div style='text-align:left;heigth:auto;'><span style='color:#000000;'> ����:" + psnname + " </span></p>");

			html.append("<div style='text-align:left;heigth:auto;'><span style='color:#000000;'> ��ְ��֯:" + hrorgheadvo.getName() + " </span></p>");
			JSONObject jsonobj = new JSONObject(cons.replace("item", ""));
			BillVO[] billVOs = new BillVO[1];
			BillVO billVO = new BillVO();
			HashMap<String, Object> bodyVO = new HashMap<String, Object>();
			// ��ȡ��ϸ����
			String namesindex[] = jsonobj.getNames(jsonobj);
			int index[] = new int[namesindex.length];

			// ��ϸ��Ŀ�±��ַ�������ת����int����
			for (int i = 0; i < namesindex.length; i++) {
				index[i] = Integer.parseInt(namesindex[i]);
			}
			;
			// ת������±�int���������������
			Arrays.sort(index);
			for (int i = 0; i < index.length; i++) {
				JSONObject itemjson = (JSONObject) jsonobj.get(""+index[i]);
				String itemname = (String) itemjson.get("name");
				String itemvalue ="null".equals(itemjson.get("value"))?"0.00":itemjson.get("value").toString();

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
	public static void main(String[] args) {
		new UFDouble("49.90.00", Integer.valueOf("90.00").intValue());
	}
	/**
	 * ��Աservice
	 * 
	 * @author ����
	 * @return
	 */
	protected IPsndocQueryService getManageService() {
		if (manageService == null) {
			manageService = (IPsndocQueryService) ((IPsndocQueryService) NCLocator.getInstance().lookup(
					IPsndocQueryService.class));
		}

		return manageService;
	}
}