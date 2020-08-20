/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.mobile.entryapply.EntryapplyQueryVO;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;

import java.util.ArrayList;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.hi.entrymng.IEntrymngManageService;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.hi.entrymng.AggEntryapplyVO;
import nc.vo.hi.entrymng.EntryapplyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;

/**
 * @author LILY 入职申请
 * 
 */
public class RzApplyAction extends AbstractMobileAction {
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
		AggEntryapplyVO aggvo = savevo(userid, obj);
		String pk = ((EntryapplyVO) aggvo.getParentVO()).getPk_entryapply();
		return queryBillVoByPK(userid, pk);
	}

	public static IEntrymngManageService getEntrymngManageServiceImpl() {
		return (IEntrymngManageService) NCLocator.getInstance().lookup(
				IEntrymngManageService.class.getName());
	}

	public AggEntryapplyVO savevo(String userid, Object obj)
			throws BusinessException {
		BillVO billvo = (BillVO) obj;
		EntryapplyVO vo = new EntryapplyVO();
		AggEntryapplyVO aggvo = new AggEntryapplyVO();
		String pk_org = (String) billvo.getHeadVO().get("pk_org");
		String pk_group = (String) billvo.getHeadVO().get("pk_group");
		int hstatus = Integer.parseInt(billvo.getHeadVO().get("vostatus")
				.toString());
		vo.setStatus(hstatus);
		if (hstatus == VOStatus.NEW) {
			// 申请单编码
			String bill_code = NCLocator.getInstance()
					.lookup(IHrBillCode.class)
					.getBillCode("6101", pk_group, pk_org);
			vo.setBill_code(bill_code);
			// 获取人员信息
			String pk_psnjob = billvo.getHeadVO().get("pk_psnjob").toString();
			String sql = "SELECT bd_psndoc.pk_psndoc FROM bd_psndoc INNER JOIN hi_psnorg ON hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc INNER JOIN hi_psnjob ON hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg WHERE hi_psnjob.pk_psnjob = '"
					+ pk_psnjob + "'";
			Object pk_psndoc = getQuery().executeQuery(sql,
					new ColumnProcessor());
			if (pk_psndoc != null) {
				vo.setPk_psndoc(pk_psndoc.toString());
			}

		} else {
			String bill_code = billvo.getHeadVO().get("bill_code").toString();
			vo.setBill_code(bill_code);
			vo.setTs(new UFDateTime(billvo.getHeadVO().get("ts").toString()));
			// 获取人员信息
			String sql = "SELECT pk_psndoc FROM hi_entryapply where bill_code = '"
					+ bill_code + "' ";
			Object pk_psndoc = getQuery().executeQuery(sql,
					new ColumnProcessor());
			vo.setPk_psndoc(pk_psndoc.toString());
			vo.setPk_entryapply(billvo.getHeadVO().get("pk_entryapply")
					.toString());
			if (billvo.getHeadVO().get("modifier") != null) {
				vo.setModifier(billvo.getHeadVO().get("modifier").toString());
				vo.setModifiedtime(new UFDateTime(billvo.getHeadVO()
						.get("modifiedtime").toString()));
			}
		}
		vo.setPk_psnjob(billvo.getHeadVO().get("pk_psnjob").toString());
		vo.setIssyncwork(new UFBoolean(billvo.getHeadVO().get("issyncwork")
				.toString()));// 同步履历
		vo.setBillmaker(billvo.getHeadVO().get("billmaker").toString());
		vo.setCreator(billvo.getHeadVO().get("billmaker").toString());
		vo.setCreationtime(new UFDateTime());
		vo.setApply_date(new UFLiteralDate());
		vo.setPk_org(pk_org);
		vo.setApprove_state(-1);// 审批状态(自由 -1)
		vo.setPk_group(pk_group);
		// vo.setAttributeValue("jobglbdef1",
		// PuPubVO.getString_TrimZeroLenAsNull(billvo.getHeadVO().get("jobglbdef1")));
		if (billvo.getHeadVO().get("business_type") != null) {
			vo.setBusiness_type(billvo.getHeadVO().get("business_type")
					.toString());
		}
		vo.setPk_billtype(billvo.getHeadVO().get("pk_billtype").toString());
		vo.setDr(0);
		billvo.getHeadVO().get("approve_state");
		if (billvo.getHeadVO().get("transtypeid") != null) {
			vo.setTranstype(billvo.getHeadVO().get("transtype").toString());
			vo.setTranstypeid(billvo.getHeadVO().get("transtypeid").toString());
		}
		if (billvo.getHeadVO().get("memo") != null) {
			vo.setMemo(billvo.getHeadVO().get("memo").toString());
		}
		aggvo.setParentVO(vo);
		if (hstatus == VOStatus.NEW) {
			getEntrymngManageServiceImpl().insertBill(aggvo);
		} else {
			getEntrymngManageServiceImpl().updateBill(aggvo, true);
		}
		return aggvo;
	}

	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk, boolean isbody)
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
		if (str.toString().trim().contains("entryapply.")) {
			condit = str.toString().replace("entryapply.", "");

		}

		if (condit.trim().length() > 0) {
			if (condit.contains("pk_corp")) {
				condit = condit.replace("pk_corp", "pk_org");
			}
			if (condit.contains("PPK")) {
				condit = condit.replace("PPK", "pk_entryapply");
			}
			if (condit.contains("PK")) {
				condit = condit.replace("PK", "pk_entryapply");
			}
			if (condit.trim().contains("apply_date")) {
				condit = condit.replace(" 00:00:00", "").replace(" 23:59:59",
						"");
			}
		}
		String sql = "select * from (select rownum rowno, t1.* from (select * from hi_entryapply where nvl(dr,0)=0 "
				+ condit
				+ " order by ts desc)t1 where (billmaker = '"
				+ userid
				+ "' or approver = '"
				+ userid
				+ "')) where rowno between "
				+ startnum + " and " + endnum + " ";
		ArrayList<EntryapplyQueryVO> list = (ArrayList<EntryapplyQueryVO>) getQuery()
				.executeQuery(sql,new BeanListProcessor(EntryapplyQueryVO.class));
		String[] formulas = new String[] {
				"sxrq->getcolvalue(hi_psnjob,begindate,pk_psnjob,pk_psnjob)",
				"category->getcolvalue(bd_psncl,name,pk_psncl,getcolvalue(hi_psnjob,pk_psncl,pk_psnjob,pk_psnjob))",
				"dept_apply->getcolvalue(org_dept,name,pk_dept,getcolvalue(hi_psnjob,pk_dept,pk_psnjob,pk_psnjob))",
				"apply_jobs->getcolvalue(om_post,postname,pk_post,getcolvalue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))",
				"jobtitle->getcolvalue(bd_defdoc,name,pk_defdoc,getcolvalue(bd_psndoc,titletechpost,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob)))",
				"professional->getcolvalue(bd_defdoc,name,pk_defdoc,getcolvalue(bd_psndoc,prof,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob)))",
				"major->getcolvalue(hi_psndoc_edu,major,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",				
				"school->getcolvalue(hi_psndoc_edu,school,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",
				"sex->getcolvalue(bd_psndoc,sex,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",
				"education->getcolvalue(hi_psndoc_edu,education,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",
				"mobile->getcolvalue(bd_psndoc,mobile,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))",
				"jobtype->getcolvalue(om_jobtype,jobtypename,pk_jobtype,getcolvalue(hi_psnjob,series,pk_psnjob,pk_psnjob))",
				"jobname->getColValue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob)" };
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new EntryapplyVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		PubTools.execFormulaWithVOs(maps, formulas);
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
		String pk_entryapply = (String) bill.getHeadVO().get("pk_entryapply");
		AggEntryapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggEntryapplyVO.class, pk_entryapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		eParam.put("notechecked", "notechecked");
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("DELETE", "6101", null, aggvos, null, eParam);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_entryapply = (String) bill.getHeadVO().get("pk_entryapply");
		AggEntryapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggEntryapplyVO.class, pk_entryapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("SAVE", "6101", null, aggvos, null, eParam);
		return queryBillVoByPK(userid, pk_entryapply);
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
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(),
				ainfoVO.getState(), ainfoVO.getApprovenote(),
				ainfoVO.getDspVOs());
		// ApproveInfoVO bill = (ApproveInfoVO) obj;
		// String pk_entryapply = (String) bill.getBillid();
		// AggEntryapplyVO aggvos = MDPersistenceService
		// .lookupPersistenceQueryService().queryBillOfVOByPK(
		// AggEntryapplyVO.class, pk_entryapply, false);
		// IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
		// IPFBusiAction.class.getName());
		// pf.processBatch("APPROVE", "6101", new AggEntryapplyVO[] { aggvos },
		// null, null, null);
		// return queryBillVoByPK(userid, pk_entryapply);
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_entryapply = (String) bill.getBillid();
		AggEntryapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggEntryapplyVO.class, pk_entryapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6101", new AggEntryapplyVO[] { aggvos },
				null, null, null);
		return queryBillVoByPK(userid, pk_entryapply);
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_entryapply = (String) bill.getHeadVO().get("pk_entryapply");
		AggEntryapplyVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggEntryapplyVO.class, pk_entryapply, false);
		HashMap<String, String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processAction("RECALL", "6101", null, aggvos, null, eParam);
		return queryBillVoByPK(userid, pk_entryapply);
	}
}
