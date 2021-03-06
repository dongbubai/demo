/**
 * 
 */
package nc.bs.mobile.ApplyManager;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.ta.IAwayAppInfoDisplayer;
import nc.itf.ta.IAwayApplyApproveManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.away.AggAwayVO;
import nc.vo.ta.away.AwaybVO;
import nc.vo.ta.away.AwayhVO;
/**
 * @author wanghy   出差申请
 *
 */
public class CcApplyAction  extends AbstractMobileAction {
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
			AggAwayVO aggvo = saveaggvo(userid, obj);
			String pk = aggvo.getParentVO().getPrimaryKey();
			if(aggvo.getParentVO().getStatus()==2){
				getAwayApplyApproveManageMaintainImpl().insertData(aggvo);
			}else{
				getAwayApplyApproveManageMaintainImpl().updateData(aggvo);
			}
			return queryBillVoByPK(userid, pk);
		}
		
		public static IAwayApplyApproveManageMaintain getAwayApplyApproveManageMaintainImpl(){
			return (IAwayApplyApproveManageMaintain) NCLocator.getInstance().lookup(IAwayApplyApproveManageMaintain.class.getName());
		}


		public AggAwayVO saveaggvo(String userid, Object obj) throws BusinessException {
			BillVO billvo = (BillVO) obj;
			String pk_org = (String) billvo.getHeadVO().get("pk_org");
			String pk_group = (String) billvo.getHeadVO().get("pk_group");
			String ruleCode = "6403"; // nc存在编码规则定义节点
			AwayhVO hvo = new AwayhVO();
			AggAwayVO aggvo = new AggAwayVO();
			IdGenerator hid = new SequenceGenerator();
	        String hpk= hid.generate();
			HashMap<String, Object> mhvo = billvo.getHeadVO();
			HashMap<String, Object>[] bvosmap = billvo.getBodyVOsMap().get("away_sub");
			int hstatus = Integer.parseInt(mhvo.get("vostatus").toString());
			if(bvosmap==null || bvosmap.length<=0){
				throw new BusinessException("子表数据没有发生变化！");
			}
			UFDateTime time = new UFDateTime();
			hvo.setPk_org(pk_org);
			hvo.setPk_group(pk_group);
			hvo.setBillmaker(mhvo.get("billmaker").toString());
			hvo.setApply_date(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("apply_date"))));
			hvo.setApprove_state(-1);
			hvo.setStatus(hstatus);
			hvo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnjob")));//员工号
			hvo.setPk_org_v(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_org_v")));//组织
			hvo.setPk_dept_v(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_dept_v")));//集团
			hvo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psndoc")));//人员基本信息
			hvo.setPk_billtype("6403");//单据类型
			hvo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnorg")));//人员组织关系主键
			hvo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtypeid")));//流程类型
			hvo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtype")));//流程类型编码
			hvo.setIshrssbill(PuPubVO.getUFBoolean_NullAs(mhvo.get("ishrssbill"), UFBoolean.TRUE));//是否员工自助增加
			hvo.setPk_awaytype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_awaytype")));//出差类别
			hvo.setPk_awaytypecopy(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_awaytypecopy")));//出差类别copy
			hvo.setFun_code("60170awayapply");//审批流节点号
			String vcode = NCLocator.getInstance().lookup(IHrBillCode.class).getBillCode(ruleCode, pk_group,pk_org);
			if (hstatus == VOStatus.NEW) {
				// 获取客户编码
				hvo.setBill_code(vcode);
				hvo.setCreationtime(time);
				hvo.setCreator(userid);
				hvo.setPk_awayh(hpk);
			} else {
				hvo.setPk_awayh(mhvo.get("pk_awayh").toString());
				hvo.setBill_code(mhvo.get("bill_code").toString());
				hvo.setModifier(userid.toString());
				hvo.setModifiedtime(time);
				hvo.setCreationtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("creationtime"))));
				hvo.setCreator(mhvo.get("creator")==null?userid:mhvo.get("creator").toString());
			}
			AwaybVO[] bvos = new AwaybVO[bvosmap.length];
			UFDouble awayhour = new UFDouble();//出差时长
			UFDouble aheadfee= new UFDouble();//预支费用
			UFDouble factfee= new UFDouble();//实际支出
			StringBuffer modifierpk = new StringBuffer();
			for (int i = 0; i < bvosmap.length; i++) {
				AwaybVO bvo = new AwaybVO();
				IdGenerator bid = new SequenceGenerator();
		        String bpk= bid.generate();
				HashMap<String, Object> mapbvo = bvosmap[i];
				
				bvo.setPk_group(pk_group);
				bvo.setPk_org(pk_org);
				bvo.setAwayhour(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("awayhour")));// 出差时长
				bvo.setAheadfee(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("aheadfee")));// 预支费用
				bvo.setFactfee(PuPubVO.getUFDouble_NullAsZero(mapbvo.get("factfee")));// 实际支出
				bvo.setAwaybegintime(new UFDateTime(mapbvo.get("awaybegintime").toString()));//出差开始时间
				bvo.setAwayendtime(new UFDateTime(mapbvo.get("awayendtime").toString()));//出差结束时间
				bvo.setAwayaddress(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("awayaddress")));//出差地点
				bvo.setPk_agentpsn(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("pk_agentpsn")));//工作交接人
				bvo.setWorkprocess(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("workprocess")));//工作交接情况
				bvo.setAwayremark(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("awayremark")));//出差说明
				bvo.setWorkfinish(PuPubVO.getString_TrimZeroLenAsNull(mapbvo.get("workfinish")));//出差工作完成情况
				bvo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psndoc")));
				bvo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnjob")));
				bvo.setPk_org(pk_org);
				bvo.setPk_group(pk_group);
				bvo.setPk_awaytype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_awaytype")));
				bvo.setPk_awaytypecopy(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_awaytypecopy")));
				bvo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtype")));
				bvo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("transtypeid")));
				bvo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_psnorg")));
				bvo.setPk_timeitem(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("pk_awaytype")));
				int bstatus = Integer.parseInt(mapbvo.get("vostatus").toString());
				bvo.setStatus(bstatus);
				if(bstatus == VOStatus.NEW){
			    	if(mapbvo.get("pk_awayb")!=null && mapbvo.get("pk_awayb").toString().trim().length()>0){
			    		bvo.setBill_code(mhvo.get("bill_code").toString());
			    		bvo.setPk_awayh(mhvo.get("pk_awayh").toString());
						bvo.setPk_awayb(mapbvo.get("pk_awayb").toString());
			    	}else{
			    		bvo.setBill_code(vcode);
			    		bvo.setPk_awayh(hpk);
			    		bvo.setPk_awayb(bpk);
			    	}
			    }else{
			    	bvo.setPk_awayh(mhvo.get("pk_awayh").toString());
					bvo.setPk_awayb(mapbvo.get("pk_awayb").toString());
			    }
				//修改，将表体数据为修改状态的累加，最后数据库查出没有修改修改的表体数据，相加
				if(hstatus==VOStatus.UPDATED){
					if(null!=bvosmap[i].get("vostatus") && "1".equals(bvosmap[i].get("vostatus").toString())){
						modifierpk.append(bvosmap[i].get("pk_awayb").toString()).append(",");
					}
				}
				bvos[i]= bvo;
			}
			aggvo.setParentVO(hvo);
			aggvo.setChildrenVO(bvos);
			IAwayAppInfoDisplayer appAutoDisplayer =  NCLocator.getInstance().lookup(IAwayAppInfoDisplayer.class);
			aggvo = appAutoDisplayer.calculate(aggvo, TimeZone.getDefault());
			AwayhVO hVO = (AwayhVO)aggvo.getParentVO();
			awayhour = hVO.getSumhour();
			aheadfee = hVO.getSumaheadfee();
			factfee = hVO.getSumfactfee();
			String pk_awayh = hVO.getPk_awayh();
			String sql = "select * from tbm_awayb where nvl(dr,0)=0 and pk_awayh='"+pk_awayh+"'";
			@SuppressWarnings("unchecked")
			ArrayList<AwaybVO> dbvos = (ArrayList<AwaybVO>) new BaseDAO().executeQuery(sql, new BeanListProcessor(AwaybVO.class));
			for(int j=0; j<dbvos.size();j++){
				if(!modifierpk.toString().contains(dbvos.get(j).getPk_awayb().toString())){
					awayhour = awayhour.add(PuPubVO.getUFDouble_NullAsZero(dbvos.get(j).getAwayhour()));
					aheadfee = aheadfee.add(PuPubVO.getUFDouble_NullAsZero(dbvos.get(j).getAheadfee()));
					factfee =  factfee.add(PuPubVO.getUFDouble_NullAsZero(dbvos.get(j).getFactfee()));
				}
			}
			hVO.setSumhour(awayhour);hVO.setSumaheadfee(aheadfee);hVO.setSumfactfee(factfee);
			aggvo.setParentVO(hVO);
			return aggvo;
		}

		@Override
		public Object queryNoPage(String userid, Object obj) throws BusinessException {
			return null;
		}

		@Override
		public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
			StringBuffer str = dealCondition(obj, true);
			String condition = str.toString().replace("pk_corp", "pk_org");
			String condition1 = condition.toString().replace("PPK", "pk_awayh");
			String sql = "select * from (select rownum rowno,tbm_awayb.* from tbm_awayb where nvl(dr,0)=0 " + condition1 + " )  order by ts ";
			@SuppressWarnings("unchecked")
			ArrayList<AwaybVO> list = (ArrayList<AwaybVO>) getQuery().executeQuery(sql, new BeanListProcessor(AwaybVO.class));
			if (list == null || list.size() == 0) {
				return null;
			}
			HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new AwaybVO[0]));
			QueryBillVO qbillVO = new QueryBillVO();
			BillVO[] billVOs = new BillVO[1];
			BillVO billVO = new BillVO();
			billVO.setTableVO("away_sub", maps);
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}

		@Override
		public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
			StringBuffer str = dealCondition(obj, true);
			String condition = str.toString().replace("hrtaawayh","tbm_awayh").replace("pk_corp", "pk_org");
			String condition1 = condition.toString().replace("PPK", "pk_awayh");		
			StringBuffer sb = new StringBuffer();
			sb.append("select * from (select rownum rowno, t1.* from (select * from tbm_awayh where nvl(dr,0)=0 "+condition1+"")
			.append(" order by ts desc)t1 where (billmaker = '"+userid+"' or approver = '"+userid+"')) where rowno between ");
			sb.append(" "+startnum+" and "+endnum+"");
			@SuppressWarnings("unchecked")
			ArrayList<AwayhVO> list = (ArrayList<AwayhVO>) getQuery().executeQuery(sb.toString(), new BeanListProcessor(AwayhVO.class));
			if (list == null || list.size() == 0) {
				return null;
			}
			HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new AwayhVO[0]));
			QueryBillVO qbillVO = new QueryBillVO();
			BillVO[] billVOs = new BillVO[list.size()];
			String[] formulas = new String[]{
					 "psncode->getcolvalue(bd_psndoc,code,pk_psndoc,pk_psndoc)",
					 "cardid->getcolvalue(bd_psndoc,id,pk_psndoc,pk_psndoc)",
					 "mobile->getcolvalue(bd_psndoc,mobile,pk_psndoc,pk_psndoc)",
					 "psnname->getcolvalue(bd_psndoc,name,pk_psndoc,pk_psndoc)",
					 "zw->getcolvalue(om_job,jobname,pk_job,getcolvalue(hi_psnjob,pk_job,pk_psnjob,pk_psnjob))",
					 "gw->getcolvalue(om_post,postname,pk_post,getcolvalue(hi_psnjob,pk_post,pk_psnjob,pk_psnjob))",
					 "zj->getcolvalue(om_joblevel,name, pk_joblevel ,getcolvalue(hi_psnjob,pk_jobgrade,pk_psnjob,pk_psnjob))",
					 "kqunit->getcolvalue(tbm_timeitemcopy,timeitemunit,pk_timeitemcopy,pk_awaytypecopy)"
					 };
			//"jobglbdef->getcolvalue(hi_psnjob,jobglbdef1,pk_psnjob,pk_psnjob)"
			PubTools.execFormulaWithVOs(maps, formulas);
			for(int i=0;i<maps.length;i++){
				BillVO billVO = new BillVO();
//				billVO.setTableVO("cmaterialid", maps);
				HashMap<String,Object> headVO = maps[i];
				//合计休假时长
				String timeSql = "select sum(awayhour)awayhour from tbm_awayb where pk_awayh = '"+headVO.get("pk_awayh")+"' and nvl(dr,0)=0";
				ArrayList<AwaybVO> listSql = (ArrayList<AwaybVO>) getQuery()
						.executeQuery(timeSql, new BeanListProcessor(AwaybVO.class));
				headVO.put("sumhour", listSql.get(0).getAwayhour());
				
				if (maps[i].get("approve_state") == null||-1==Integer.parseInt(maps[i].get("approve_state").toString())) {
					headVO.put("ibillstatus", "-1");
				} 
				else if (3==Integer.parseInt(maps[i].get("approve_state").toString())) {
					headVO.put("ibillstatus", "3");
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
			StringBuffer str = dealCondition(obj, true);
			String condition = str.toString().replace("pk_corp", "pk_org");
			String condition1 = condition.toString().replace("PPK", "pk_awayh");
			String sql = "select * from (select rownum rowno,tbm_awayb.* from tbm_awayb where nvl(dr,0)=0 " + condition1 + " order by ts desc ) where rowno between " + startnum + " and " + endnum + " ";
			@SuppressWarnings("unchecked")
			ArrayList<AwaybVO> list = (ArrayList<AwaybVO>) getQuery().executeQuery(sql, new BeanListProcessor(AwaybVO.class));
			if (list == null || list.size() == 0) {
				return null;
			}
			HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new AwaybVO[0]));
			QueryBillVO qbillVO = new QueryBillVO();
			BillVO[] billVOs = new BillVO[1];
			BillVO billVO = new BillVO();
			billVO.setTableVO("away_sub", maps);
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}

		public Object refreshData(Object aggvo) throws BusinessException {
			AggAwayVO vo = (AggAwayVO) aggvo;
			AwayhVO hvo = (AwayhVO) vo.getParentVO();
			HashMap<String, Object>[] maps = transNCVOTOMap(new AwayhVO[] { hvo });
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
			String pk_awayh = (String) bill.getHeadVO().get("pk_awayh");
			AggAwayVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayVO.class, pk_awayh, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processBatch("DELETE", "6403", new AggAwayVO[]{aggvos}, null, null, null);
			return null;
		}

		@Override
		public Object submit(String userid, Object obj) throws BusinessException {
			BillVO bill = (BillVO) obj;
			String pk_awayh = (String) bill.getHeadVO().get("pk_awayh");
			AggAwayVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayVO.class, pk_awayh, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processBatch("SAVE", "6403", new AggAwayVO[]{aggvos}, null, null, null);
			return queryBillVoByPK(userid,pk_awayh);
		}

		/**
		 * 根据pk 查询出BilVO
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
			QueryBillVO body_querybillVO = (QueryBillVO) queryNoPage_body(userid, condAggVO_body);
			if(null!=body_querybillVO){
				BillVO body_BillVO = body_querybillVO.getQueryVOs()[0];
				billVO.setTableVO("away_sub", body_BillVO.getTableVO("away_sub"));
			}
			billVO.setHeadVO(head_BillVO.getHeadVO());
			return billVO;
		}

		@Override
		public Object approve(String userid, Object obj) throws BusinessException {
			ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
			return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
//			ApproveInfoVO bill = (ApproveInfoVO) obj;
//			String pk_awayh = (String) bill.getBillid();
//			AggAwayVO aggvos = MDPersistenceService
//					.lookupPersistenceQueryService().queryBillOfVOByPK(
//							AggAwayVO.class, pk_awayh, false);
//			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(
//					IPFBusiAction.class.getName());
//			pf.processBatch("APPROVE", "6403", new AggAwayVO[] { aggvos },
//					null, null, null);
//			return queryBillVoByPK(userid, pk_awayh);
		}

		@Override
		public Object unapprove(String userid, Object obj) throws BusinessException {
			MApproveVO bill = (MApproveVO) obj;
			String pk_awayh = (String) bill.getBillid() ;
			AggAwayVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayVO.class, pk_awayh, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processBatch("UNAPPROVE", "6403", new AggAwayVO[]{aggvos}, null, null, null);
			return queryBillVoByPK(userid,pk_awayh);
		}

		@Override
		public Object unsavebill(String userid, Object obj)
				throws BusinessException {
			BillVO bill = (BillVO) obj;
			String pk_awayh = (String) bill.getHeadVO().get("pk_awayh");
			AggAwayVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAwayVO.class, pk_awayh, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processBatch("RECALL", "6403", new AggAwayVO[]{aggvos}, null, null, null);
			return queryBillVoByPK(userid,pk_awayh);
		}
}
