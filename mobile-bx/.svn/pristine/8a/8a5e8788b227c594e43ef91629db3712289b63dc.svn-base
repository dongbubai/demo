/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import java.util.ArrayList;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.ta.IAwayOffManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.pub.billcode.vo.BillCodeContext;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.awayoff.AggAwayOffVO;
import nc.vo.ta.awayoff.AwayOffVO;
/**
 * @author LILY 薪资查询
 *
 */
public class XcApplyAction  extends AbstractMobileAction {
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
		AggAwayOffVO aggvo = saveaggvo(userid, obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}
	
	public static IAwayOffManageMaintain getAwayOffManageMaintainImpl(){
		return (IAwayOffManageMaintain) NCLocator.getInstance().lookup(IAwayOffManageMaintain.class.getName());
	}
	
	public AggAwayOffVO saveaggvo(String userid, Object obj)
			throws BusinessException {
		BillVO billVO = (BillVO) obj;
		String pk_org = (String) billVO.getHeadVO().get("pk_org");
		String pk_group = (String) billVO.getHeadVO().get("pk_group");
		String ruleCode = "6407"; // nc存在编码规则定义节点
		AggAwayOffVO aggvo = new AggAwayOffVO();
		// 主表
		try {
			HashMap<String, Object> mvo = billVO.getHeadVO();
			AwayOffVO vo = new AwayOffVO();
			int hstatus = Integer.parseInt(mvo.get("vostatus").toString());
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
				vo.setPk_awayoff(hpk);
			} else {
				vo.setPk_awayoff(mvo.get("pk_awayoff").toString());
				vo.setBill_code(mvo.get("bill_code").toString());
				vo.setModifier(userid);// 修改人
				vo.setModifiedtime(time);// 修改时间
				vo.setCreator(mvo.get("creator")==null?userid:mvo.get("creator").toString()); // 创建人
				vo.setBillmaker(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("creator")));// 申请人
				vo.setCreationtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("creationtime"))));// 创建时间
			}
			vo.setPk_group(pk_group);
			vo.setPk_org(pk_org);
			vo.setPk_billtype("6407");// 单据类型
			vo.setApprove_state(-1);//单据状态
			vo.setApply_date(new UFLiteralDate());// 申请日期
			vo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_psndoc")));// 人员基本信息
			vo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_psnjob")));// 人员工作记录
			vo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_psnorg")));// 组织关系主键
			vo.setFun_code(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("fun_code")));// 审批流节点编号
			vo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("transtype")));// 交易类型
			vo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("transtypeid")));// 流程类型
			vo.setRegbegintimecopy(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("regbegintimecopy"))));//开始时间
			vo.setRegbegindatecopy(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("regbegindatecopy"))));//开始日期
			vo.setRegenddatecopy(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("regenddatecopy"))));//结束日期
			vo.setRegendtimecopy(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("regendtimecopy"))));//销假申请
			vo.setRegawayhourcopy(PuPubVO.getUFDouble_NullAsZero(mvo.get("regawayhourcopy")));//登记单时长
			vo.setAwaybegintime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("awaybegintime"))));//实际始时间
			vo.setAwaybegindate(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("awaybegindate"))));//实际开始日期
			vo.setAwayendtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("awayendtime"))));//实际结束时间
			vo.setAwayenddate(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("awayenddate"))));//实际结束日期
			vo.setReallyawayhour(PuPubVO.getUFDouble_NullAsZero(mvo.get("reallyawayhour")));//实际出差时长
			vo.setDifferencehour(PuPubVO.getUFDouble_NullAsZero(mvo.get("differencehour")));//差异时长
			vo.setPk_awaytype(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_awaytype")));// 加班类别
			vo.setPk_awaytypecopy(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_awaytypecopy")));// 加班类别copy
			vo.setPk_awayreg(PuPubVO.getString_TrimZeroLenAsNull(mvo.get("pk_awayreg")));
			aggvo.setParentVO(vo);
			
			IAwayOffManageMaintain appAutoDisplayer =  NCLocator.getInstance().lookup(IAwayOffManageMaintain.class);
			aggvo = appAutoDisplayer.calculate(aggvo);
			AwayOffVO hvo = (AwayOffVO)aggvo.getParentVO();
			UFDouble reallyawayhour = hvo.getReallyawayhour();
			UFDouble differencehour = hvo.getDifferencehour();
			hvo.setReallyawayhour(reallyawayhour);
			hvo.setDifferencehour(differencehour);
			aggvo.setParentVO(hvo);
			
			if(hstatus==VOStatus.NEW){
				getAwayOffManageMaintainImpl().insertData(aggvo);
			}else{
				getAwayOffManageMaintainImpl().updateData(aggvo);
			}			
		} catch (BusinessException e) {
			throw new BusinessException("保存失败;" + e.getMessage());
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

		String sql = "select * from (select rownum rowno, t1.* from (select * from tbm_awayoff where nvl(dr,0)=0 "
				+ condition
				+ " order by ts desc)t1 where (billmaker = '"+userid+"' or approver = '"+userid+"')) where rowno between "
				+ startnum + " and " + endnum + " ";
		ArrayList<AwayOffVO> list = (ArrayList<AwayOffVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(AwayOffVO.class));

		if (list == null || list.size() == 0) {
			return null;
		}

		String[] formulas = new String[] { 
				"billmaker_name->getColValue(cp_user,user_name,cuserid,billmaker);" ,
				"psncode->getcolvalue(bd_psndoc,code,pk_psndoc,pk_psndoc)",
				"psnname->getcolvalue(bd_psndoc,name,pk_psndoc,pk_psndoc)",
				"psnjob->getcolvalue(om_job,jobname,pk_job,getcolvalue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob))",
				"psnpost->getcolvalue(om_post,postname,pk_post,getcolvalue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))",
				"psnorg->getcolvalue(hi_psnjob,pk_org,pk_psnjob,pk_psnjob)",
				"psndept->getcolvalue(hi_psnjob,pk_dept,pk_psnjob,pk_psnjob)"
				
				};
	//	"jobglbdef->getcolvalue(hi_psnjob,jobglbdef1,pk_psnjob,pk_psnjob)"
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new AwayOffVO[0]));
		PubTools.execFormulaWithVOs(maps, formulas);
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];			
			Object flag = headVO.get("approve_state");// -1=自由，0=审批未通过，1=审批通过，2=审批进行中，3=提交
			// -1=自由，0=审批未通过，1=审批通过，2=审批进行中，3=提交
			// 4=作废，5=冲销，6=终止，7=终结
			if (flag == null || "".equals(flag) || "-1".equals(flag.toString())) {
				headVO.put("ibillstatus", "-1");
			} else if ("0".equals(flag.toString())) {
				headVO.put("ibillstatus", "0");
			} else if ("1".equals(flag.toString())) {
				headVO.put("ibillstatus", "1");
			} else if ("2".equals(flag.toString())) {
				headVO.put("ibillstatus", "2");
			} else {
				headVO.put("ibillstatus", "3");
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

	public Object refreshData(Object aggvo) throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;			
		String pk_awayoff = (String) bill.getHeadVO().get("pk_awayoff");
		AggAwayOffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayOffVO.class, pk_awayoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
	    eParam.put("notechecked", "notechecked");
	    IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
	    pf.processAction("DELETE", "6407", null, aggvos, null, eParam);											
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_awayoff = (String) bill.getHeadVO().get("pk_awayoff");
		AggAwayOffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayOffVO.class, pk_awayoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processAction("SAVE", "6407", null, aggvos, null, eParam);	
		return queryBillVoByPK(userid,pk_awayoff);
	}

	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk) throws BusinessException {
		BillVO billVO = new BillVO();
		// 表头
		ConditionAggVO condAggVO_head = new ConditionAggVO();
		ConditionVO[] condVOs_head = new ConditionVO[1];
		condVOs_head[0] = new ConditionVO();
		condVOs_head[0].setField("pk_awayoff");
		condVOs_head[0].setOperate("=");
		condVOs_head[0].setValue(pk);
		condAggVO_head.setConditionVOs(condVOs_head);
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
//		ApproveInfoVO bill = (ApproveInfoVO) obj;
//		String pk_awayoff = (String) bill.getBillid();
//		AggAwayOffVO aggvos = MDPersistenceService
//				.lookupPersistenceQueryService().queryBillOfVOByPK(
//						AggAwayOffVO.class, pk_awayoff, false);
//		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
//				IPFBusiAction.class.getName());
//		pf.processBatch("APPROVE", "6407", new AggAwayOffVO[] { aggvos },
//				null, null, null);
//		return queryBillVoByPK(userid, pk_awayoff);
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_awayoff = (String) bill.getBillid() ;
		AggAwayOffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayOffVO.class, pk_awayoff, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6407", new AggAwayOffVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_awayoff);
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_awayoff = (String) bill.getHeadVO().get("pk_awayoff");
		AggAwayOffVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayOffVO.class, pk_awayoff, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processAction("RECALL", "6407", null, aggvos, null, eParam);	
		return queryBillVoByPK(userid,pk_awayoff);
	}
}
