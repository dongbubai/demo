package nc.bs.mobile.ApplyManager;

import java.util.ArrayList;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.ta.ILeaveOffManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.formulaset.FormulaParseFather;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ta.leaveoff.AggLeaveoffVO;
import nc.vo.ta.leaveoff.LeaveoffVO;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;

/**
 * @author xc  销假申请
 *
 */
public class XiaoJApplyAction extends AbstractMobileAction {
	private BaseDAO baseDAO;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}
	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		return null;
	}
	
	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		return super.processAction(account, userid, billtype, action, obj);
	}
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		AggLeaveoffVO aggvo = saveaggvo(userid, obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}
	
	public AggLeaveoffVO saveaggvo(String userid, Object obj)
			throws BusinessException {
		BillVO billVO = (BillVO) obj;
		AggLeaveoffVO aggvo = new AggLeaveoffVO();
		try {
			HashMap<String, Object> mvo = billVO.getHeadVO();
			int hstatus = Integer.parseInt(mvo.get("vostatus").toString());
			LeaveoffVO vo =(LeaveoffVO) transMapTONCVO(LeaveoffVO.class, mvo);
			if(hstatus==VOStatus.NEW){
				String vcode = NCLocator.getInstance().lookup(IHrBillCode.class).getBillCode("6406", vo.getPk_group(),vo.getPk_org());
				vo.setBill_code(vcode);
				vo.setStatus(hstatus);
				vo.setApprove_state(-1);
				String[] formulas = new String[1];
				formulas[0] = "getcolvalue( hi_psnjob, pk_psnorg, pk_psnjob,\""+ vo.getPk_psnjob() + "\" ) ";
				FormulaParseFather m_formulaParse = new nc.bs.pub.formulaparse.FormulaParse();
				m_formulaParse.setExpressArray(formulas);
				Object[][] values = m_formulaParse.getValueOArray();
				String pk_psnorg= values[0][0].toString();
				vo.setPk_psnorg(pk_psnorg);
			}else{
				vo.setStatus(hstatus);
				vo.setPk_leavetypecopy(mvo.get("pk_leavetypecopy").toString());				
			}
			aggvo.setParentVO(vo);
			
			ILeaveOffManageMaintain impl =  NCLocator.getInstance().lookup(ILeaveOffManageMaintain.class);
			aggvo = impl.calculate(aggvo);
			LeaveoffVO hvo = (LeaveoffVO)aggvo.getParentVO();
			UFDouble reallyleavehour = hvo.getReallyleavehour();
			UFDouble differencehour = hvo.getDifferencehour();
			hvo.setReallyleavehour(reallyleavehour);
			hvo.setDifferencehour(differencehour);
			aggvo.setParentVO(hvo);		
			if(hstatus==VOStatus.NEW){
				impl.insertData(aggvo);
			}else{
				impl.updateData(aggvo);
			}			
		} catch (BusinessException e) {
			throw new BusinessException("保存失败;" + e.getMessage());
		}
		return aggvo;
	}

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_leaveoff");
		StringBuffer sb = new StringBuffer();
    	sb.append("SELECT * FROM (SELECT ROWNUM rw,T .*	FROM(SELECT tbm_leaveoff.* FROM tbm_leaveoff ")
		.append("WHERE NVL (tbm_leaveoff.dr, 0) = 0 "+condition1+"");
		sb.append(" ORDER BY tbm_leaveoff.ts DESC) T)WHERE (billmaker = '"+userid+"' or approver = '"+userid+"') and rw BETWEEN "+startnum+" and "+endnum+"");
		@SuppressWarnings("unchecked")
		ArrayList<LeaveoffVO> list = (ArrayList<LeaveoffVO>) getQuery().executeQuery(sb.toString(), new BeanListProcessor(LeaveoffVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new LeaveoffVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		String[] formulas = new String[]{
				 "psnorg->getcolvalue(hi_psnjob,pk_org,pk_psnjob,pk_psnjob)",
				 "psndept->getcolvalue(hi_psnjob,pk_dept,pk_psnjob,pk_psnjob)",
				 "leaveyear->getcolvalue(tbm_leavereg,leaveyear,pk_leavereg,pk_leavereg)",
				 "leavemonth->getcolvalue(tbm_leavereg,leavemonth,pk_leavereg,pk_leavereg)",
				 "lactationholidaytype->getcolvalue(tbm_leavereg,lactationholidaytype,pk_leavereg,pk_leavereg)",
				 "lactationhour->getcolvalue(tbm_leavereg,lactationhour,pk_leavereg,pk_leavereg)",
				
				 "jobname->getcolvalue(om_job,jobname,pk_job,getColValue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob))",
				 "psnname->getcolvalue(om_post,postname,pk_post,getColValue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))"
				 };
		// "jobglbdef->getcolvalue(hi_psnjob,jobglbdef1,pk_psnjob,pk_psnjob)",
		PubTools.execFormulaWithVOs(maps, formulas);
		for(int i=0;i<maps.length;i++){
			BillVO billVO = new BillVO();
			HashMap<String,Object> headVO = maps[i];
			if (maps[i].get("approve_state") == null||-1==Integer.parseInt(maps[i].get("approve_state").toString())) {
				headVO.put("ibillstatus", "-1");
			} 
			else if (3==Integer.parseInt(maps[i].get("approve_state").toString())) {
				headVO.put("ibillstatus", "3");
			}else if(2==Integer.parseInt(maps[i].get("approve_state").toString())){
				headVO.put("ibillstatus", "2");
			}
			else {
				headVO.put("ibillstatus", "1");
			}
			
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		return null;
	}
	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk) throws BusinessException {
		BillVO billVO = new BillVO();
		ConditionAggVO condAggVO_head = new ConditionAggVO();
		ConditionVO[] condVOs_head = new ConditionVO[1];
		condVOs_head[0] = new ConditionVO();
		condVOs_head[0].setField("pk_leaveoff");
		condVOs_head[0].setOperate("=");
		condVOs_head[0].setValue(pk);
		condAggVO_head.setConditionVOs(condVOs_head);
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;			
		String pk_leaveoff = (String) bill.getHeadVO().get("pk_leaveoff");
		AggLeaveoffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggLeaveoffVO.class, pk_leaveoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
	    eParam.put("notechecked", "notechecked");
	    IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
	    pf.processAction("DELETE", "6406", null, aggvos, null, eParam);											
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_leaveoff = (String) bill.getHeadVO().get("pk_leaveoff");
		AggLeaveoffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggLeaveoffVO.class, pk_leaveoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processAction("SAVE", "6406", null, aggvos, null, eParam);	
		return queryBillVoByPK(userid,pk_leaveoff);
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_leaveoff = (String) bill.getBillid() ;
		AggLeaveoffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggLeaveoffVO.class, pk_leaveoff, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6406", new AggLeaveoffVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_leaveoff);
	}

	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_leaveoff = (String) bill.getHeadVO().get("pk_leaveoff");
		AggLeaveoffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggLeaveoffVO.class, pk_leaveoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processAction("RECALL", "6406", null, aggvos, null, eParam);	
		return queryBillVoByPK(userid,pk_leaveoff);
	}

}
