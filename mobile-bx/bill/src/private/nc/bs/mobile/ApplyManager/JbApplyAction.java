/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.ta.IOvertimeAppInfoDisplayer;
import nc.itf.ta.IOvertimeApplyApproveManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
import nc.pub.billcode.vo.BillCodeContext;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.overtime.AggOvertimeVO;
import nc.vo.ta.overtime.OvertimebVO;
import nc.vo.ta.overtime.OvertimehVO;

/**
 * @author LILY 加班申请
 * 
 */
public class JbApplyAction extends AbstractMobileAction {
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
		AggOvertimeVO aggvo = saveaggvo(userid, obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}
	
	public static IOvertimeApplyApproveManageMaintain getOvertimeApplyApproveManageMaintainImpl(){
		return (IOvertimeApplyApproveManageMaintain) NCLocator.getInstance().lookup(IOvertimeApplyApproveManageMaintain.class.getName());
	}

	public AggOvertimeVO saveaggvo(String userid, Object obj)
			throws BusinessException {

		BillVO billVO = (BillVO) obj;
		String pk_org = (String) billVO.getHeadVO().get("pk_org");
		String pk_group = (String) billVO.getHeadVO().get("pk_group");
		String ruleCode = "6405"; // nc存在编码规则定义节点
		AggOvertimeVO aggvo = new AggOvertimeVO();
		// 主表
		try {
			HashMap<String, Object> mhvo = billVO.getHeadVO();
			OvertimehVO hvo = new OvertimehVO();
			int hstatus = Integer.parseInt(mhvo.get("vostatus").toString());
			IdGenerator hid = new SequenceGenerator();
			String hpk = hid.generate();
			UFDateTime time = new UFDateTime();
			if (hstatus == VOStatus.NEW) {
				// 获取客户编码
				String vcode = getPreBillCode(ruleCode, pk_org, pk_group);
				hvo.setBill_code(vcode);
				hvo.setCreator(userid); // 创建人
				hvo.setCreationtime(time);// 创建时间
				hvo.setBillmaker(userid);// 申请人
				hvo.setPk_overtimeh(hpk);
			} else {
				hvo.setPk_overtimeh(mhvo.get("pk_overtimeh").toString());
				hvo.setBill_code(mhvo.get("bill_code").toString());
				hvo.setModifier(userid);// 修改人
				hvo.setModifiedtime(time);// 修改时间
				hvo.setCreator(mhvo.get("creator").toString()); // 创建人
				hvo.setBillmaker(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("creator")));// 申请人
				hvo.setCreationtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("creationtime"))));// 创建时间
//				hvo.setAttributeValue("jobglbdef1", PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("jobglbdef1")));//职务级别
			}
			hvo.setPk_group(pk_group);
			hvo.setPk_org(pk_org);
			hvo.setApprove_state(-1);
			hvo.setApply_date(new UFLiteralDate());// 申请日期
			hvo.setPk_overtimetype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_overtimetype")));// 加班类别
			hvo.setPk_overtimetypecopy(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_overtimetypecopy")));// 加班类别copy
			hvo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psndoc")));// 人员基本信息
			hvo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnjob")));// 人员工作记录
			hvo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnorg")));// 组织关系主键
			hvo.setPk_org_v(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_org_v")));// 人员组织版本
			hvo.setPk_dept_v(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_dept_v")));// 人员部门版本
			hvo.setSumhour(PuPubVO.getUFDouble_NullAsZero(mhvo.get("sumhour")));// 合计工时
			hvo.setFun_code(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("fun_code")));// 审批流节点编号
			hvo.setIshrssbill(PuPubVO.getUFBoolean_NullAs(mhvo.get("ishrssbill"), UFBoolean.TRUE));// 员工是否自动增加
			hvo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtype")));// 交易类型
			hvo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtypeid")));// 流程类型
			hvo.setPk_billtype("6405");// 单据类型

			// 子表
			HashMap<String, Object>[] bvosmap = billVO.getBodyVOsMap().get(
					"overtime_sub");
			if (null == bvosmap || bvosmap.length == 0) {
				throw new BusinessException("表体没有数据！或者表体数据没有修改！");
			}
			OvertimebVO[] bvos = new OvertimebVO[bvosmap.length];
			UFLiteralDate begindate ;
			UFLiteralDate enddate ;
			int begintimeH ;//加班开始小时
			int begintimeM ;//加班开始分钟
			int endtimeH ;//加班结束小时
			int endtimeM ;//加班结束分钟
			for (int i = 0; i < bvosmap.length; i++) {
				OvertimebVO bvo = new OvertimebVO();
				HashMap<String, Object> mapbvo = bvosmap[i];
				begindate = new UFLiteralDate(mapbvo.get("overtimebegintime").toString().substring(0, 10));
				enddate = new UFLiteralDate(mapbvo.get("overtimeendtime").toString().substring(0, 10));
				bvo.setPk_group(pk_group);
				bvo.setPk_org(pk_org);
				bvo.setActhour(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("acthour")));// 实际加班时长
				bvo.setIsneedcheck(PuPubVO.getUFBoolean_NullAs(mapbvo.get("isneedcheck"), UFBoolean.FALSE));// 是否需要校验
				bvo.setOvertimealready(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("overtimealready")));// 加班时长
				bvo.setOvertimebegindate(begindate);
				bvo.setOvertimebegintime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("overtimebegintime"))));
				bvo.setOvertimeenddate(enddate);
				bvo.setOvertimeendtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("overtimeendtime"))));
				bvo.setOvertimehour(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("overtimehour")));
				bvo.setOvertimeremark(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("overtimeremark")));//加班说明
				IdGenerator bid = new SequenceGenerator();
				String bpk = bid.generate();
				
				//加班时长规则设置
				begintimeH = Integer.parseInt(mapbvo.get("overtimebegintime").toString().substring(11, 13));
				begintimeM = Integer.parseInt(mapbvo.get("overtimebegintime").toString().substring(14, 16));				
				endtimeH = Integer.parseInt(mapbvo.get("overtimeendtime").toString().substring(11, 13));
				endtimeM = Integer.parseInt(mapbvo.get("overtimeendtime").toString().substring(14, 16));
				if("1002Z710000000021ZLV".equals(mhvo.get("pk_overtimetype")) || "1002Z710000000021ZLX".equals(mhvo.get("pk_overtimetype"))){
					if(begindate.compareTo(enddate) == 0 ){
						if(begintimeH == 12){
							bvo.setDeduct(60 - begintimeM);
						}else if(endtimeH == 12){
							bvo.setDeduct(endtimeM);
						}else if(begintimeH < 12 && endtimeH > 12){
							bvo.setDeduct(60);
						}else{
							bvo.setDeduct(Integer.parseInt(mapbvo.get("deduct") == null ? "0"
									: mapbvo.get("deduct").toString()));// 扣除时长
						}
					}else{
						throw new BusinessException("开始日期与结束日期需要一致！");
					}
				}else{
					bvo.setDeduct(Integer.parseInt(mapbvo.get("deduct") == null ? "0"
							: mapbvo.get("deduct").toString()));// 扣除时长
				}
								
				int bstatus = Integer.parseInt(mapbvo.get("vostatus")
						.toString());
				bvo.setStatus(bstatus);
				if (hstatus == VOStatus.NEW) {
					bvo.setPk_overtimeh(hpk);
					bvo.setPk_overtimeb(bpk);
				} else {
					if (bstatus == VOStatus.NEW) {
						bvo.setPk_overtimeh(mhvo.get("pk_overtimeh").toString());
						bvo.setPk_overtimeb(bpk);
					} else {
						bvo.setPk_overtimeh(mhvo.get("pk_overtimeh").toString());
						bvo.setPk_overtimeb(mapbvo.get("pk_overtimeb")
								.toString());
					}
				}
				bvos[i] = bvo;
			}
			aggvo.setParentVO(hvo);			
			aggvo.setChildrenVO(bvos);
			
			IOvertimeAppInfoDisplayer appAutoDisplayer =  NCLocator.getInstance().lookup(IOvertimeAppInfoDisplayer.class);
			aggvo = appAutoDisplayer.calculate(aggvo, TimeZone.getDefault());
			OvertimehVO HVO = (OvertimehVO)aggvo.getParentVO();
			UFDouble overtimehour = HVO.getSumhour();
			HVO.setSumhour(overtimehour);
			aggvo.setParentVO(HVO);
			
			if(hstatus==VOStatus.NEW){
				getOvertimeApplyApproveManageMaintainImpl().insertData(aggvo);
			}else{
				getOvertimeApplyApproveManageMaintainImpl().updateData(aggvo);
			}
			if(hstatus==VOStatus.UPDATED){
				String pk=	mhvo.get("pk_overtimeh").toString();
		        String sql=" update tbm_overtimeh set sumhour=(select sum(nvl(overtimehour,0)) from tbm_overtimeb where pk_overtimeh = '"+pk+"' and nvl(dr,0)=0 ) where  pk_overtimeh = '"+pk+"' ";
		        getQuery().executeUpdate(sql);
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
	public Object queryNoPage(String userid, Object obj)
			throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj)
			throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String sql = "select * from (select rownum rowno,tbm_overtimeb.* from tbm_overtimeb where nvl(dr,0)=0 " + condition + " )  order by ts ";
		ArrayList<OvertimebVO> list = (ArrayList<OvertimebVO>) getQuery().executeQuery(sql, new BeanListProcessor(OvertimebVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new OvertimebVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("overtime_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum)
			throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");

		String sql = "select * from (select rownum rowno, t1.* from (select * from tbm_overtimeh where nvl(dr,0)=0 "
				+ condition
				+ " order by ts desc)t1 where (billmaker = '"+userid+"' or approver = '"+userid+"')) where rowno between "
				+ startnum + " and " + endnum + " ";
		ArrayList<OvertimehVO> list = (ArrayList<OvertimehVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(OvertimehVO.class));

		if (list == null || list.size() == 0) {
			return null;
		}

		String[] formulas = new String[] { 
				"billmaker_name->getColValue(cp_user,user_name,cuserid,billmaker);" ,
				"psncode->getcolvalue(bd_psndoc,code,pk_psndoc,pk_psndoc)",
				"postname->getcolvalue(om_post,postname,pk_post,getColValue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))",
				
				"jobname->getcolvalue(om_job,jobname,pk_job,getColValue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob))",
				 "psnname->getcolvalue(bd_psndoc,name,pk_psndoc,getColValue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))"
				};
//"jobglbdef1->getcolvalue(hi_psnjob,jobglbdef1,pk_psnjob,pk_psnjob)",
		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new OvertimehVO[0]));
		PubTools.execFormulaWithVOs(maps, formulas);
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			//合计加班时长
//			String timeSql = "select sum(overtimehour)overtimehour from tbm_overtimeb where pk_overtimeh = '"+headVO.get("pk_overtimeh")+"' and nvl(dr,0)=0";
//			ArrayList<OvertimebVO> listSql = (ArrayList<OvertimebVO>) getQuery()
//					.executeQuery(timeSql, new BeanListProcessor(OvertimebVO.class));
//			headVO.put("sumhour", listSql.get(0).getOvertimehour());			
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
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("PPK", "pk_overtimeh");
		String sql = "select * from (select rownum rowno, tbm_overtimeb.* from tbm_overtimeb  where nvl(dr,0)=0 "
				+ condition
				+ " order by ts desc) where rowno between "
				+ startnum + " and " + endnum + " ";
		ArrayList<OvertimebVO> list = (ArrayList<OvertimebVO>) getQuery()
				.executeQuery(sql, new BeanListProcessor(OvertimebVO.class));

		if (list == null || list.size() == 0) {
			return null;
		}

		HashMap<String, Object>[] maps = transNCVOTOMap(list
				.toArray(new OvertimebVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("overtime_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;			
		String pk_overtimeh = (String) bill.getHeadVO().get("pk_overtimeh");
		AggOvertimeVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggOvertimeVO.class, pk_overtimeh, false);
	    HashMap<String,String> eParam = new HashMap<String, String>();
	    eParam.put("notechecked", "notechecked");
	    IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
	    pf.processAction("DELETE", "6405", null, aggvos, null, eParam);											
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_overtimeh = (String) bill.getHeadVO().get("pk_overtimeh");
		AggOvertimeVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggOvertimeVO.class, pk_overtimeh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("SAVE", "6405", new AggOvertimeVO[] { aggvos }, null,
				null, null);
		return queryBillVoByPK(userid, pk_overtimeh);
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
		condVOs_head[0].setField("pk_overtimeh");
		condVOs_head[0].setOperate("=");
		condVOs_head[0].setValue(pk);
		condAggVO_head.setConditionVOs(condVOs_head);

		ConditionAggVO condAggVO_body = new ConditionAggVO();
		ConditionVO[] condVOs_body = new ConditionVO[1];
		condVOs_body[0] = new ConditionVO();
		condVOs_body[0].setField("pk_overtimeb");
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
			billVO.setTableVO("overtime_sub",
					body_BillVO.getTableVO("overtime_sub"));
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
//		ApproveInfoVO bill = (ApproveInfoVO) obj;
//		String pk_overtimeh = (String) bill.getBillid();
//		AggOvertimeVO aggvos = MDPersistenceService
//				.lookupPersistenceQueryService().queryBillOfVOByPK(
//						AggOvertimeVO.class, pk_overtimeh, false);
//		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
//				IPFBusiAction.class.getName());
//		pf.processBatch("APPROVE", "6405", new AggOvertimeVO[] { aggvos },
//				null, null, null);
//		return queryBillVoByPK(userid, pk_overtimeh);
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_overtimeh = (String) bill.getBillid() ;
		AggOvertimeVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggOvertimeVO.class, pk_overtimeh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6405", new AggOvertimeVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_overtimeh);
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_overtimeh = (String) bill.getHeadVO().get("pk_overtimeh");
		AggOvertimeVO aggvos = MDPersistenceService
				.lookupPersistenceQueryService().queryBillOfVOByPK(
						AggOvertimeVO.class, pk_overtimeh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
				IPFBusiAction.class.getName());
		pf.processBatch("RECALL", "6405",
				new AggOvertimeVO[] { aggvos }, null, null, null);
		return queryBillVoByPK(userid, pk_overtimeh);
	}
}
