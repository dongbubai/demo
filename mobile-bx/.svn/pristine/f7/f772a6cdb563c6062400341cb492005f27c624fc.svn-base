/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.itf.muap.pub.IMobileAction;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.DefEventVO;
import hd.vo.muap.pub.QueryBillVO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.ta.ILeaveAppInfoDisplayer;
import nc.itf.ta.ILeaveApplyApproveManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.formulaset.FormulaParseFather;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.leave.AggLeaveVO;
import nc.vo.ta.leave.LeavebVO;
import nc.vo.ta.leave.LeavehVO;
import nc.vo.ta.leave.SplitBillResult;

/**
 * @author LILY 休假申请
 * 
 */
public class XjApplyAction extends AbstractMobileAction {
	private BaseDAO baseDAO;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	String time = sdf.format(new Date());

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object processAction(String account, String userid, String billtype,
			String action, Object obj) throws BusinessException {
		if (action.equals(IMobileAction.AFTEREDIT)) {
			String ruleCode = "6404";
			AfterEditVO aevo = (AfterEditVO) obj;
			BillVO bill = aevo.getVo();
			BillVO billvo = new BillVO();
			DefEventVO evevo = new DefEventVO();
			if (aevo.getKey().equals("fun_code")) {
				String pk_org = (String) bill.getHeadVO().get("pk_org");
				String pk_group = (String) bill.getHeadVO().get("pk_group");
				String billcode = NCLocator.getInstance()
						.lookup(IHrBillCode.class)
						.getBillCode(ruleCode, pk_group, pk_org);
				HashMap<String, Object> head = new HashMap<String, Object>();
				head.put("pk_org", pk_org);
				head.put("pk_group", pk_group);
				head.put("bill_code", billcode);
				head.put("billmaker", userid);
				head.put("apply_date", time);
				head.put("approve_state", -1); // -1=自由，0=审批未通过，1=审批通过，2=审批进行中，3=提交，
				head.put("pk_billtype", 6404);// 单据类型
				head.put("ibillstatus", -1);// 单据状态
				billvo.setHeadVO(head);
				evevo.setVo(billvo);
				evevo.setAction("SETDATA");
			}
			return evevo;
		} else {
			return super.processAction(account, userid, billtype, action, obj);
		}
	}

	public static ILeaveApplyApproveManageMaintain getLeaveApplyApproveManageMaintainImpl() {
		return (ILeaveApplyApproveManageMaintain) NCLocator.getInstance()
				.lookup(ILeaveApplyApproveManageMaintain.class.getName());
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		AggLeaveVO aggvo = saveaggvo(userid, obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}

	public AggLeaveVO saveaggvo(String userid, Object obj)
			throws BusinessException {
		BillVO billvo = (BillVO) obj;
		String pk_org = (String) billvo.getHeadVO().get("pk_org");
		String pk_group = (String) billvo.getHeadVO().get("pk_group");
		LeavehVO hvo = new LeavehVO();
		AggLeaveVO aggvo = new AggLeaveVO();
		try {
			HashMap<String, Object> mhvo = billvo.getHeadVO();
			HashMap<String, Object>[] bvosmap = billvo.getBodyVOsMap().get(
					"leaveb_sub");
			int hstatus = Integer.parseInt(mhvo.get("vostatus").toString());
			if (bvosmap == null || bvosmap.length <= 0) {
				throw new BusinessException("子表没有数据！");
			}
			UFDateTime time = new UFDateTime();
			hvo.setStatus(hstatus);
			if (hstatus == VOStatus.NEW) {
				hvo.setCreationtime(time);
				hvo.setCreator(userid);
				hvo.setBillmaker(userid);// 申请人
			} else {
				hvo.setPk_leaveh(mhvo.get("pk_leaveh").toString());
				hvo.setModifiedtime(time);// 修改时间
				hvo.setModifier(userid);// 修改人
				hvo.setCreationtime(new UFDateTime(PuPubVO
						.getString_TrimZeroLenAsNull(mhvo.get("creationtime"))));
				hvo.setCreator(mhvo.get("creator") == null ? userid : mhvo.get(
						"creator").toString());
				hvo.setBillmaker(mhvo.get("billmaker") == null ? userid : mhvo
						.get("billmaker").toString());// 申请人
			}
			hvo.setApply_date(new UFLiteralDate()); // 申请日期
			hvo.setApprove_state(-1);
			hvo.setBill_code(mhvo.get("bill_code") == null ? null : mhvo.get(
					"bill_code").toString());// 单据编码
			hvo.setFreezedayorhour(new UFDouble());// 冻结时长
			hvo.setIshrssbill(UFBoolean.TRUE);// 是否员工自助增加
			hvo.setIslactation(UFBoolean.FALSE);// 是否哺乳假
			hvo.setLeaveindex(1);// 假期结算记录顺序号
			hvo.setLeavemonth(String.valueOf(time.getMonth()).length() == 1 ? "0"
					+ String.valueOf(time.getMonth())
					: String.valueOf(time.getMonth()));// 假期期间
			hvo.setLeaveyear(String.valueOf(time.getYear()));// 假期年度
			hvo.setPk_billtype("6404");// 单据类型
			hvo.setPk_dept_v(mhvo.get("pk_dept_v") == null ? null : mhvo.get(
					"pk_dept_v").toString());// 部门
			hvo.setPk_group(pk_group);// 所属集团
			hvo.setPk_org(pk_org);// 所属组织
			hvo.setPk_org_v(mhvo.get("pk_org_v") == null ? null : mhvo.get(
					"pk_org_v").toString());// 组织
			String pk_psnjob = mhvo.get("pk_psnjob") == null ? null : mhvo.get(
					"pk_psnjob").toString();
			String pk_leavetype = mhvo.get("pk_leavetype") == null ? null
					: mhvo.get("pk_leavetype").toString();
			String[] formulas = new String[3];
			formulas[0] = "getcolvalue( hi_psnjob, pk_psndoc, pk_psnjob,\""
					+ pk_psnjob + "\" ) ";
			formulas[1] = "getcolvalue( hi_psnjob, pk_psnorg, pk_psnjob,\""
					+ pk_psnjob + "\" ) ";
			formulas[2] = "getcolvalue( tbm_timeitemcopy, pk_timeitemcopy, pk_timeitem,\""
					+ pk_leavetype + "\" ) ";
			FormulaParseFather m_formulaParse = new nc.bs.pub.formulaparse.FormulaParse();
			m_formulaParse.setExpressArray(formulas);
			Object[][] values = m_formulaParse.getValueOArray();
			String pk_psndoc = getString_TrimZeroLenAsNull(values[0][0]);
			String pk_psnorg = getString_TrimZeroLenAsNull(values[1][0]);
			String pk_timeitemcopy = getString_TrimZeroLenAsNull(values[2][0]);
			hvo.setPk_psnjob(pk_psnjob);// 人员工作记录
			hvo.setPk_psndoc(pk_psndoc);// 人员基本信息
			hvo.setPk_psnorg(pk_psnorg);// 人员组织关系
			hvo.setPk_leavetype(pk_leavetype);// 休假类别
			hvo.setPk_leavetypecopy(pk_timeitemcopy);// 休假类别实例
			hvo.setRealdayorhour(new UFDouble(0));// 享有时长
			hvo.setRestdayorhour(new UFDouble(0));// 结余时长
			hvo.setResteddayorhour(new UFDouble(0));// 已休时长
			hvo.setRelatetel(PuPubVO.getString_TrimZeroLenAsNull(mhvo
					.get("relatetel")));// 假期联系电话
			hvo.setSplitid("");// 拆单id
			hvo.setTranstype(mhvo.get("transtype") == null ? null : mhvo.get(
					"transtype").toString());// 交易类型
			hvo.setTranstypeid(mhvo.get("transtypeid") == null ? null : mhvo
					.get("transtypeid").toString());// 流程类型
			hvo.setUsefuldayorhour(new UFDouble(0)); // 可用时长
//			hvo.setAttributeValue("jobglbdef1",
//					PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("jobglbdef1")));

			// 根据员工性别过滤休假类别
			// 获取员工性别
			String sex = getPsnSex(pk_psndoc);
			if("1".equals(sex)){
				//获取产假和哺乳假的休假类别PK 并判断
				boolean flag = getBoolean(pk_leavetype);
				if(flag == false){
					throw new BusinessException("休假类型不适用于你的性别！");
				}
			}

			// 给表体set数据
			LeavebVO[] bvos = new LeavebVO[bvosmap.length];
			UFDouble days = new UFDouble();
//			UFDouble day = new UFDouble();
//			StringBuffer modifypk = new StringBuffer();
			for (int i = 0; i < bvosmap.length; i++) {
				LeavebVO bvo = new LeavebVO();
				HashMap<String, Object> mapbvo = bvosmap[i];
				int bstatus = Integer.parseInt(mapbvo.get("vostatus")
						.toString());
				bvo.setStatus(bstatus);
				bvo.setLeavebegindate(new UFLiteralDate(mapbvo
						.get("leavebegintime").toString().substring(0, 10)));// 休假开始日期
				bvo.setLeavebegintime(mapbvo.get("leavebegintime") == null ? new UFDateTime()
						: new UFDateTime(mapbvo.get("leavebegintime")
								.toString()));// 休假结束时间
				bvo.setLeaveenddate(new UFLiteralDate(mapbvo
						.get("leaveendtime").toString().substring(0, 10)));// 休假结束日期
				bvo.setLeaveendtime(mapbvo.get("leaveendtime") == null ? new UFDateTime()
						: new UFDateTime(mapbvo.get("leaveendtime").toString()));// 休假结束时间
				bvo.setLeavehour(new UFDouble(1));// 休假时长
				bvo.setPk_group(pk_group);// 集团
				bvo.setPk_org(pk_org);// 组织
				bvo.setPk_agentpsn(PuPubVO.getString_TrimZeroLenAsNull(mhvo
						.get("pk_agentpsn")));// 工作交接人
				bvo.setWorkprocess(PuPubVO.getString_TrimZeroLenAsNull(mhvo
						.get("workprocess")));// 工作交接情况
				bvo.setLeaveremark(PuPubVO.getString_TrimZeroLenAsNull(mhvo
						.get("leaveremark")));// 休假说明
				if (bstatus == VOStatus.NEW) {
					if (mapbvo.get("pk_leaveb") == null) {
					} else {
						bvo.setPk_leaveh(mhvo.get("pk_leaveh").toString());
						bvo.setPk_leaveb(mapbvo.get("pk_leaveb").toString());
					}
				} else {
					bvo.setPk_leaveh(mhvo.get("pk_leaveh").toString());
					bvo.setPk_leaveb(mapbvo.get("pk_leaveb").toString());
				}
				// 新增
//				if (hstatus == VOStatus.NEW) {
//					if (null != bvosmap[i].get("vostatus")
//							&& "2".equals(bvosmap[i].get("vostatus").toString())) {
//						days = days.add(day);
//					}
//				}
//				// 修改
//				if (hstatus == VOStatus.UPDATED) {
//					if (null != bvosmap[i].get("vostatus")
//							&& "1".equals(bvosmap[i].get("vostatus").toString())) {
//						days = days.add(day);
//						modifypk.append(bvosmap[i].get("pk_leaveb").toString())
//								.append(",");
//					}
//				}
//				// 删除
//				if (hstatus == VOStatus.DELETED) {
//					if (null != bvosmap[i].get("vostatus")
//							&& "1".equals(bvosmap[i].get("vostatus").toString())) {
//						days = days.sub(day);
//					}
//				}
				bvos[i] = bvo;
			}
			aggvo.setChildrenVO(bvos);
			aggvo.setParentVO(hvo);
			ILeaveAppInfoDisplayer appAutoDisplayer = NCLocator.getInstance().lookup(ILeaveAppInfoDisplayer.class);
			aggvo = appAutoDisplayer.calculate(aggvo, TimeZone.getDefault());
			LeavehVO HVO = (LeavehVO) aggvo.getParentVO();
//			days = HVO.getSumhour();
//			// 如果是更新， 根据主表pk查询所有子表，然后根据增删改重新计算主表合计金额
//			if (!"".equals(hvo.getPk_leaveh())) {
//				String pk_gather = hvo.getPk_leaveh();
//				String sql = "select * from tbm_leaveb where nvl(dr,0)=0 and pk_leaveh='"
//						+ pk_gather + "'";
//				ArrayList<LeavebVO> dbvos = (ArrayList<LeavebVO>) new BaseDAO()
//						.executeQuery(sql,
//								new BeanListProcessor(LeavebVO.class));
//				for (int i = 0; i < dbvos.size(); i++) {
//					if (modifypk.toString().contains(
//							dbvos.get(i).getPk_leaveb().toString())) {
//					} else {
//						days = days.add(dbvos.get(i).getLeavehour());
//					}
//				}
//			}
			HVO.setSumhour(days);// 休假总时长
			aggvo.setParentVO(HVO);
			SplitBillResult<AggLeaveVO> splitResult = new SplitBillResult<AggLeaveVO>();
			splitResult.setOriginalBill(aggvo);
			splitResult.setSplitResult(new AggLeaveVO[] { aggvo });
			if (hstatus == VOStatus.NEW) {
				getLeaveApplyApproveManageMaintainImpl()
						.insertData(splitResult);
			} else {
				getLeaveApplyApproveManageMaintainImpl()
						.updateData(splitResult);
			}

			if(hstatus==VOStatus.UPDATED){
				String pk=	mhvo.get("pk_leaveh").toString();
		        String sql=" update tbm_leaveh set sumhour=(select sum(leavehour) from tbm_leaveb where pk_leaveh ='"+pk+"' and nvl(dr,0)=0 ) where  pk_leaveh = '"+pk+"' ";
		        getQuery().executeUpdate(sql);
			}
		} catch (BusinessException e) {
			throw new BusinessException("保存失败;" + e.getMessage());
		}
		return aggvo;
	}


	public static String getString_TrimZeroLenAsNull(Object value) {
		if (value == null || value.toString().trim().length() == 0) {
			return null;
		}
		return value.toString();
	}

	@Override
	public Object queryNoPage(String userid, Object obj)
			throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj)
			throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_leaveh");
		String sql = "select * from (select rownum rowno,tbm_leaveb.* from tbm_leaveb where nvl(dr,0)=0 "
				+ condition1 + " )  order by ts ";
		ArrayList<LeavebVO> list = (ArrayList<LeavebVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(LeavebVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new LeavebVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("leaveb_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_leaveh").replace("hrtaleaveh", "tbm_leaveh");
		StringBuffer sb = new StringBuffer();
		sb.append(
				"select * from (select rownum rowno, t1.* from (select * from tbm_leaveh where nvl(dr,0)=0 "
						+ condition1 + "").append(
				" order by ts desc)t1 where (billmaker = '" + userid
						+ "' or approver = '" + userid
						+ "')) where rowno between ");
		sb.append(" " + startnum + " and " + endnum + "");
		ArrayList<LeavehVO> list = (ArrayList<LeavehVO>) getQuery()
				.executeQuery(sb.toString(),
						new BeanListProcessor(LeavehVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new LeavehVO[0]));
		String[] formulas = new String[] {
				"code->getcolvalue(bd_psndoc,code,pk_psndoc,getColValue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",
				"name->getcolvalue(bd_psndoc,name,pk_psndoc,getColValue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob) )",
				"jobname->getcolvalue(om_job,jobname,pk_job,getColValue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob))",
				"postname->getcolvalue(om_post,postname,pk_post,getColValue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))",
				"kqunit->getcolvalue(tbm_timeitemcopy,timeitemunit,pk_timeitemcopy,pk_leavetypecopy)" };
		//"jobglbdef1->getcolvalue(hi_psnjob,jobglbdef1,pk_psnjob,pk_psnjob)"
		PubTools.execFormulaWithVOs(maps, formulas);
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();// -1=自由，0=审批未通过，1=审批通过，2=审批进行中，3=提交，
			HashMap<String, Object> headVO = maps[i];// 1由自 2审批 3冻结 4关闭 5作废 from
														// 3.1 0无状态（未使用）1自由 2审批
														// 3冻结 4关闭（未使用） 5作废 6结束
														// 7正在审批 8审批未通过
//			// 合计休假时长
//			String timeSql = "select sum(leavehour)leavehour from tbm_leaveb where pk_leaveh = '"
//					+ headVO.get("pk_leaveh") + "' and nvl(dr,0)=0";
//			ArrayList<LeavebVO> listSql = (ArrayList<LeavebVO>) getQuery()
//					.executeQuery(timeSql,
//							new BeanListProcessor(LeavebVO.class));
//			headVO.put("sumhour", listSql.get(0).getLeavehour());

			if (list.get(i).getApprove_state() == null
					|| "-1".equals(list.get(i).getApprove_state().toString())) {
				headVO.put("ibillstatus", "-1");
			} else if (list.get(i).getApprove_state().toString().equals("0")
					|| list.get(i).getApprove_state().toString().equals("1")
					|| list.get(i).getApprove_state().toString().equals("2")) {
				headVO.put("ibillstatus", "3");
			} else if (list.get(i).getApprove_state().toString().equals("3")) {
				headVO.put("ibillstatus", "2");
			} else {
				headVO.put("ibillstatus", "1");
			}
			headVO.get("ibillstatus");
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_leaveh");
		String sql = "select * from (select rownum rowno,tbm_leaveb.* from tbm_leaveb where nvl(dr,0)=0 "
				+ condition1
				+ "  order by ts desc) where rowno between "
				+ startnum + " and " + endnum;
		ArrayList<LeavebVO> list = (ArrayList<LeavebVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(LeavebVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new LeavebVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("leaveb_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		AggLeaveVO vo = (AggLeaveVO) aggvo;
		LeavehVO hvo = (LeavehVO) vo.getParentVO();
		HashMap<String, Object>[] maps = transNCVOTOMap(new LeavehVO[] { hvo });
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String tbm_leaveh = (String) bill.getHeadVO().get("pk_leaveh");
		getQuery().deleteByPK(LeavehVO.class, tbm_leaveh);
		getQuery().deleteByPK(LeavebVO.class, tbm_leaveh);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_leaveh = (String) bill.getHeadVO().get("pk_leaveh");
		AggLeaveVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggLeaveVO.class, pk_leaveh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("SAVE", "6404", new AggLeaveVO[] { aggvos }, null,
				null, null);
		return queryBillVoByPK(userid, pk_leaveh);
	}

	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk)
			throws BusinessException {
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
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid,
				condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		// 查询 表体
		QueryBillVO body_querybillVO = (QueryBillVO) queryNoPage_body(userid,
				condAggVO_body);
		if (null != body_querybillVO) {
			BillVO body_BillVO = body_querybillVO.getQueryVOs()[0];
			billVO.setTableVO("leaveb_sub",
					body_BillVO.getTableVO("leaveb_sub"));
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(),
				ainfoVO.getState(), ainfoVO.getApprovenote(),
				ainfoVO.getDspVOs());
		// ApproveInfoVO bill = (ApproveInfoVO) obj;
		// String pk_leaveh = (String) bill.getBillid();
		// AggLeaveVO aggvos = MDPersistenceService
		// .lookupPersistenceQueryService().queryBillOfVOByPK(
		// AggLeaveVO.class, pk_leaveh, false);
		// IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
		// IPFBusiAction.class.getName());
		// pf.processBatch("APPROVE", "6404", new AggLeaveVO[] { aggvos },
		// null, null, null);
		// return queryBillVoByPK(userid, pk_leaveh);
		// return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(),
		// ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_leaveh = (String) bill.getBillid();
		AggLeaveVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggLeaveVO.class, pk_leaveh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6404", new AggLeaveVO[] { aggvos }, null,
				null, null);
		return queryBillVoByPK(userid, pk_leaveh);
	}

	// 收回
	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_leaveh = (String) bill.getHeadVO().get("pk_leaveh");
		AggLeaveVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggLeaveVO.class, pk_leaveh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("RECALL", "6404", new AggLeaveVO[] { aggvos }, null,
				null, null);
		return queryBillVoByPK(userid, pk_leaveh);
	}

	//根据员工号获取员工性别
	private String getPsnSex(String pk_psndoc) throws BusinessException {
		String sex = null;
		String sql = "select sex from bd_psndoc where pk_psndoc = '"+pk_psndoc+"'";
		Object ob = getQuery().executeQuery(sql, new ColumnProcessor());
		if(ob != null && !"".equals(ob)){
			sex = ob.toString();			
		}
		return sex;
	}
	
	//判断休假类型是否为“产假”、“哺乳假”
	private boolean getBoolean(String pk_leavetype) throws BusinessException {
		String sql = "select timeitemname from tbm_timeitem where pk_timeitem = '"+pk_leavetype+"' ";
		Object ob = getQuery().executeQuery(sql, new ColumnProcessor());
		if(ob != null && !"".equals(ob)){
			if("产假".equals(ob) || "哺乳假".equals(ob)){				
				return false;
			}
		}		
		return true;		
	}
}
