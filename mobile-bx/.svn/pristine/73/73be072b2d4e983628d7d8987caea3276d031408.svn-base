/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import java.util.ArrayList;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.trn.regmng.IRegmngManageService;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.pub.billcode.vo.BillCodeContext;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.trn.regmng.AggRegapplyVO;
import nc.vo.trn.regmng.RegapplyVO;

/**
 * @author LILY 转正申请
 * 
 */
public class ZzApplyAction extends AbstractMobileAction {
	private BaseDAO baseDAO;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object processAction(String account, String userid, String billtype,
			String action, Object obj) throws BusinessException {
		return super.processAction(account, userid, billtype, action, obj);
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		AggRegapplyVO aggvo = savevo(userid, obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}

	public static IRegmngManageService getRegmngManageServiceImpl() {
		return (IRegmngManageService) NCLocator.getInstance().lookup(
				IRegmngManageService.class.getName());
	}

	public AggRegapplyVO savevo(String userid, Object obj)
			throws BusinessException {
		BillVO billVO = (BillVO) obj;
		RegapplyVO vo = new RegapplyVO();
		HashMap<String, Object> mvo = billVO.getHeadVO();
		String ruleCode = "6111"; // nc存在编码规则定义节点
		AggRegapplyVO aggvo = new AggRegapplyVO();
		String pk_org = (String) mvo.get("pk_org");
		String pk_group = (String) mvo.get("pk_group");
		int hstatus = Integer.parseInt(mvo.get("vostatus").toString());
		vo.setStatus(hstatus);
		IdGenerator hid = new SequenceGenerator();
		String hpk = hid.generate();
		UFDateTime time = new UFDateTime();
		if (hstatus == VOStatus.NEW) {
			// 获取客户编码
			String vcode = getPreBillCode(ruleCode, pk_org, pk_group);
			vo.setBill_code(vcode);
			vo.setCreator(userid); // 创建人
			vo.setCreationtime(time);// 创建时间
			vo.setBillmaker(userid);// 申请人
			vo.setPk_hi_regapply(hpk);
		} else {
			vo.setPk_hi_regapply(mvo.get("pk_hi_regapply").toString());
			vo.setBill_code(mvo.get("bill_code").toString());
			vo.setModifier(userid);// 修改人
			vo.setModifiedtime(time);// 修改时间
			vo.setCreator(mvo.get("creator") == null ? userid : mvo.get(
					"creator").toString()); // 创建人
			vo.setBillmaker(PuPubVO.getString_TrimZeroLenAsNull(mvo
					.get("billmaker")));// 申请人
			vo.setCreationtime(new UFDateTime(PuPubVO
					.getString_TrimZeroLenAsNull(mvo.get("creationtime"))));// 创建时间
		}
		vo.setPk_group(pk_group);
		vo.setPk_org(pk_org);
		vo.setPk_billtype("6111");// 单据类型
		vo.setApprove_state(-1);// 单据状态
		vo.setApply_date(new UFLiteralDate());// 申请日期
		vo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("pk_psndoc")));// 人员基本信息
		vo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("pk_psnjob")));// 转正人
		vo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("pk_psnorg")));// 组织关系主键
		vo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("transtype")));// 交易类型
		vo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("transtypeid")));// 流程类型

		vo.setProbation_type(Integer.parseInt(mvo.get("probation_type")
				.toString()));
		vo.setBegin_date(new UFLiteralDate(PuPubVO
				.getString_TrimZeroLenAsNull(mvo.get("begin_date"))));// 试用开始日期
		vo.setEnd_date(new UFLiteralDate());// 试用结束日期
		vo.setTrialresult(Integer.parseInt(mvo.get("trialresult").toString())); // 试用结果
		vo.setTrialdelaydate(new UFLiteralDate().getDateAfter(15)); // 延期转正日期
		vo.setPk_tutor(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_tutor")));// 督导人
		vo.setMemo(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("memo")));// 转正述职
		vo.setRegulardate(new UFLiteralDate(PuPubVO
				.getString_TrimZeroLenAsNull(mvo.get("regulardate"))));// 转正述职
		// 转正前人员信息
		vo.setOldpk_dept(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_dept")));
		vo.setOldpk_psncl(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_psncl")));
		vo.setOldpk_org(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_org")));
		vo.setOldpk_post(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_post")));
		vo.setOldpk_postseries(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_postseries")));
		vo.setOldpk_job(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_job")));
		vo.setOldseries(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldseries")));
		vo.setOldpk_jobrank(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_jobrank")));
		vo.setOldpk_jobgrade(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_jobgrade")));
		vo.setOldpk_job_type(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldpk_job_type")));
		vo.setOldjobmode(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldjobmode")));
		vo.setOlddeposemode(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("olddeposemode")));
		vo.setOldoccupation(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldoccupation")));
		vo.setOldworktype(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("oldworktype")));
		vo.setOldpoststat(PuPubVO.getUFBoolean_NullAs(mvo.get("oldpoststat"),
				UFBoolean.FALSE));
		vo.setOldmemo(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("oldmemo")));
		// 转正后人员信息
		vo.setNewpk_dept(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_dept")));
		vo.setNewpk_psncl(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_psncl")));
		vo.setNewpk_org(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_org")));
		vo.setNewpk_post(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_post")));
		vo.setNewpk_postseries(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_postseries")));
		vo.setNewpk_job(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_job")));
		vo.setNewseries(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newseries")));
		vo.setNewpk_jobrank(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_jobrank")));
		vo.setNewpk_jobgrade(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_jobgrade")));
		vo.setNewpk_job_type(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newpk_job_type")));
		vo.setNewjobmode(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newjobmode")));
		vo.setNewdeposemode(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newdeposemode")));
		vo.setNewoccupation(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newoccupation")));
		vo.setNewworktype(PuPubVO.getString_TrimZeroLenAsNull(mvo
				.get("newworktype")));
		vo.setNewpoststat(PuPubVO.getUFBoolean_NullAs(mvo.get("newpoststat"),
				UFBoolean.FALSE));
		vo.setNewmemo(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("newmemo")));
//		vo.setAttributeValue("oldjobglbdef1", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("oldjobglbdef1")));
//		vo.setAttributeValue("newjobglbdef1", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("newjobglbdef1")));
//		vo.setAttributeValue("oldjobglbdef17", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("oldjobglbdef17")));
//		vo.setAttributeValue("newjobglbdef17", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("newjobglbdef17")));
//		vo.setAttributeValue("oldjobglbdef18", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("oldjobglbdef18")));
//		vo.setAttributeValue("newjobglbdef18", PuPubVO.getString_TrimZeroLenAsNull(mvo.get("newjobglbdef18")));
		
		vo.setIfsynwork(PuPubVO.getUFBoolean_NullAs(mvo.get("ifsynwork"),
				UFBoolean.FALSE));// 是否同步工作履历
		vo.setIshrssbill(PuPubVO.getUFBoolean_NullAs(mvo.get("ishrssbill"),
				UFBoolean.FALSE));// 是否自助
		aggvo.setParentVO(vo);
		if (hstatus == VOStatus.NEW) {
			getRegmngManageServiceImpl().insertBill(aggvo);
		} else {
			getRegmngManageServiceImpl().updateBill(aggvo, true);
		}
		return aggvo;
	}

	protected String getPreBillCode(String billRuleCode, String pkOrg,
			String pkGroup) throws BusinessException {
		String billCode = "";
		BillCodeContext billCodeContext = ((IBillcodeManage) NCLocator
				.getInstance().lookup(IBillcodeManage.class))
				.getBillCodeContext(billRuleCode, pkGroup, pkOrg);
		if (billCodeContext != null) {
			if (billCodeContext.isPrecode()) {
				billCode = ((IBillcodeManage) NCLocator.getInstance().lookup(
						IBillcodeManage.class)).getPreBillCode_RequiresNew(
						billRuleCode, pkGroup, pkOrg);
			}
		}
		return billCode;
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
	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condit = "";
		String ands[] = str.toString().split("and");
		for (String st : ands) {
			if (st.trim().length() > 0) {
				if (st.contains("pk_corp")) {
					condit = condit + " and " + st.replace("pk_corp", "pk_org");
				} else if (st.contains("PPK")) {
					condit = condit + " and "
							+ st.replace("PPK", "pk_hi_regapply");
				} else if (st.contains("PK")) {
					condit = condit + " and "
							+ st.replace("PK", "pk_hi_regapply");
				} else {
					condit = condit + " and  " + st;
				}
			}
		}
		String sql = "select * from (select rownum rowno, t1.* from (select * from hi_regapply where nvl(dr,0)=0 "
				+ condit
				+ " order by ts desc)t1 where (billmaker = '"+userid+"' or approver = '"+userid+"')) where rowno between "
				+ startnum
				+ " and " + endnum + " ";
		ArrayList<RegapplyVO> list = (ArrayList<RegapplyVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(RegapplyVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		String[] formulas = new String[] { "mobile->getcolvalue(bd_psndoc,mobile,pk_psndoc,pk_psndoc)" };
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new RegapplyVO[0]));
		PubTools.execFormulaWithVOs(maps, formulas);
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			// -1=自由，3=提交，2=审批进行中，1=审批通过，0=审批未通过，102=已执行，
			if (list.get(i).getApprove_state() == null
					|| -1 == Integer.parseInt(maps[i].get("approve_state")
							.toString())) {
				headVO.put("ibillstatus", "-1");
			} else if (3 == Integer.parseInt(maps[i].get("approve_state")
					.toString())) {
				headVO.put("ibillstatus", "3");
			} else if (1 == Integer.parseInt(maps[i].get("approve_state")
					.toString())) {
				headVO.put("ibillstatus", "1");
			}
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		return null;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_regapply = (String) bill.getHeadVO().get("pk_hi_regapply");
		AggRegapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggRegapplyVO.class, pk_hi_regapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		eParam.put("notechecked", "notechecked");
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("DELETE", "6111", null, aggvos, null, eParam);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_regapply = (String) bill.getHeadVO().get("pk_hi_regapply");
		AggRegapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggRegapplyVO.class, pk_hi_regapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("SAVE", "6111", null, aggvos, null, eParam);
		return queryBillVoByPK(userid, pk_hi_regapply);
	}

	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk)
			throws BusinessException {
		BillVO billVO = new BillVO();
		// 表头
		ConditionAggVO condAggVO_head = new ConditionAggVO();
		ConditionVO[] condVOs_head = new ConditionVO[1];
		condVOs_head[0] = new ConditionVO();
		condVOs_head[0].setField(IVOField.PPK);
		condVOs_head[0].setOperate("=");
		condVOs_head[0].setValue(pk);
		condAggVO_head.setConditionVOs(condVOs_head);
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid,
				condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
//		ApproveInfoVO bill = (ApproveInfoVO) obj;
//		String pk_hi_regapply = (String) bill.getBillid();
//		AggRegapplyVO aggvos = MDPersistenceService
//				.lookupPersistenceQueryService().queryBillOfVOByPK(
//						AggRegapplyVO.class, pk_hi_regapply, false);
//		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
//				IPFBusiAction.class.getName());
//		pf.processBatch("APPROVE", "6111", new AggRegapplyVO[] { aggvos },
//				null, null, null);
//		return queryBillVoByPK(userid, pk_hi_regapply);
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_hi_regapply = (String) bill.getBillid() ;
		AggRegapplyVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggRegapplyVO.class, pk_hi_regapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6111", new AggRegapplyVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_hi_regapply);
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_regapply = (String) bill.getHeadVO().get("pk_hi_regapply");
		AggRegapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggRegapplyVO.class, pk_hi_regapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("RECALL", "6111", null, aggvos, null, eParam);
		return queryBillVoByPK(userid, pk_hi_regapply);
	}
}