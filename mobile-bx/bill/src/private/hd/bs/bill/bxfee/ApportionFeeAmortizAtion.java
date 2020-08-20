package hd.bs.bill.bxfee;

import java.util.ArrayList;
import java.util.HashMap;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pubitf.erm.expamortize.IExpAmortize;
import nc.pubitf.erm.expamortize.IExpAmortizeinfoManage;
import nc.pubitf.erm.expamortize.IExpAmortizeinfoQuery;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.erm.expamortize.ExpamtinfoVO;
import nc.vo.pub.BusinessException;
import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
/**
 * 待摊费用摊销
 */
public class ApportionFeeAmortizAtion extends ApproveWorkAction {
	
	BaseDAO dao = new BaseDAO();
	IExpAmortizeinfoManage its = NCLocator.getInstance().lookup(IExpAmortizeinfoManage.class);
	IExpAmortize ia = NCLocator.getInstance().lookup(IExpAmortize.class);
	IExpAmortizeinfoQuery iq = NCLocator.getInstance().lookup(IExpAmortizeinfoQuery.class);

	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		
		
		
		/**
		 * 保存摊销期间  按钮
		 */
		if("DEFMODMONTH".equals(action)){
			
			BillVO bill = (BillVO) obj;
			HashMap<String, Object> head = bill.getHeadVO();
			HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
			HashMap<String, Object>[] exp = bodys.get("er_expamtinfo");
			
			if(null==head.get("defitem1")|| "".equals(head.get("defitem1"))){
				throw new BusinessException("会计期间不能空!");
			}
			String sql = "select * from bd_accperiodmonth where pk_accperiodmonth='"+head.get("defitem1").toString()+"'";
			ArrayList<AccperiodmonthVO> mvos = (ArrayList<AccperiodmonthVO>)dao.executeQuery(sql, new BeanListProcessor(AccperiodmonthVO.class));
			
			for(int i=0; null!=exp && i<exp.length; i++){
				ExpamtinfoVO vo = (ExpamtinfoVO) transMapTONCVO(ExpamtinfoVO.class,exp[i]);
				if(null==exp[i].get("res_period")|| "".equals(exp[i].get("res_period"))){
					throw new BusinessException("剩余摊销期不能空!");
				}
				if(Integer.parseInt(exp[i].get("res_period").toString())==0){
					continue;
				}
				if(null!=mvos && mvos.size()>0){
					//调用接口
					its.updatePeriod(Integer.parseInt(exp[i].get("res_period").toString()), vo, mvos.get(0).getYearmth());
				}
			}
			return queryBillVoByPK(userid,head.get("pk_org")+"-"+head.get("defitem1"));
		}
		
		if("DEFAMOR".equals(action) || "DEFCANCELAMOR".equals(action)){
			return getAmor(userid, action, obj);
		}
		
		return super.processAction(account, userid, billtype, action, obj);
	}
	
	
	public Object getAmor(String userid,String action,Object obj) throws BusinessException{
		
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] exp = bodys.get("er_expamtinfo");
		
		if(null==head.get("pk_org")|| "".equals(head.get("pk_org"))){
			throw new BusinessException("财务组织不能空!");
		}
		if(null==head.get("defitem1")|| "".equals(head.get("defitem1"))){
			throw new BusinessException("会计期间不能空!");
		}
		String sql = "select * from bd_accperiodmonth where pk_accperiodmonth='"+head.get("defitem1").toString()+"'";
		ArrayList<AccperiodmonthVO> mvos = (ArrayList<AccperiodmonthVO>)dao.executeQuery(sql, new BeanListProcessor(AccperiodmonthVO.class));
		
		ArrayList<ExpamtinfoVO> vos = new ArrayList<ExpamtinfoVO>();
		
		for(int i=0; null!=exp && i<exp.length; i++){
			ExpamtinfoVO vo = (ExpamtinfoVO) transMapTONCVO(ExpamtinfoVO.class,exp[i]);
			vos.add(vo);
		}
		if(null!=mvos && mvos.size()>0 && null!=vos && vos.size()>0){
			/**
			 * 摊销 按钮
			 */
			if("DEFAMOR".equals(action)){
				ia.amortize(head.get("pk_org").toString(), mvos.get(0).getYearmth(), vos.toArray(new ExpamtinfoVO[0]));
			}
			/**
			 * 取消摊销 按钮
			 */
			if("DEFCANCELAMOR".equals(action)){
				ia.unAmortize(head.get("pk_org").toString(), mvos.get(0).getYearmth(), vos.toArray(new ExpamtinfoVO[0]));
			}
		}
		return queryBillVoByPK(userid,head.get("pk_org")+"-"+head.get("defitem1"));
	}
	
	/**
	 * 用pk查询单据
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
		QueryBillVO body_querybillVO = (QueryBillVO) queryPage_body(userid, condAggVO_body,1,1);
		// 查询 表体  
		if(null!=body_querybillVO && null!=body_querybillVO.getQueryVOs()){
			billVO = body_querybillVO.getQueryVOs()[0];
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		
		String pk_org = null;
		String pk_period = null;
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();
			for (int i = 0; i < conVOs.length; i++) {
				if (conVOs[i].getField().contains("head.pk_org")) {
					pk_org = conVOs[i].getValue();
				}
				if (conVOs[i].getField().contains("head.defitem1")) {
					pk_period = conVOs[i].getValue();
				}
				if (conVOs[i].getField().contains("PPK")) {
					String[] strs = conVOs[i].getValue().split("-");
					if(null!=strs && strs.length==2){
						pk_org = strs[0];
						pk_period = strs[1];
					}
				}
			}
		} 
		if (null!=pk_org && null!=pk_period) {
			HashMap<String, Object>[] maps = new HashMap[1];
			QueryBillVO qbillVO = new QueryBillVO();
			BillVO[] billVOs = new BillVO[1];
			for (int i = 0; i < maps.length; i++) {
				BillVO billVO = new BillVO();
				maps[i] = new HashMap<String, Object>();
				maps[i].put("ibillstatus", -1);// 自由
				maps[i].put("pk_org", pk_org); 
				maps[i].put("defitem1", pk_period); 
				maps[i].put("pk_group", InvocationInfoProxy.getInstance().getGroupId()); 
				maps[i].put("PK", pk_org+"-"+pk_period); 
				HashMap<String, Object> headVO = maps[i];
				billVO.setHeadVO(headVO);
				billVOs[i] = billVO;
			}
			qbillVO.setQueryVOs(billVOs); 
			return qbillVO;
		}
		return null;
	}
	
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String pk_org = null;
		String pk_period = null;
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();
			for (int i = 0; i < conVOs.length; i++) {
				if (conVOs[i].getField().contains("PPK")) {
					String[] strs = conVOs[i].getValue().split("-");
					if(null!=strs && strs.length==2){
						pk_org = strs[0];
						pk_period = strs[1];
					}
				}
			} 
		}
		String period = "";
		String sql = "select yearmth from bd_accperiodmonth where pk_accperiodmonth='"+pk_period+"'";
		ArrayList<AccperiodmonthVO> list1 = (ArrayList<AccperiodmonthVO>) dao.executeQuery(sql, new BeanListProcessor(AccperiodmonthVO.class));
		if(null!=list1 && list1.size()>0){
			period = list1.get(0).getYearmth();
		}
		if (null!=pk_org && null!=pk_period) {
			sql="SELECT distinct er_expamtinfo.*,billtypename as defitem2,total_period-res_period as accu_period" +
				"      ,total_amount-res_amount as accu_amount,case when res_period=0 then 0 else total_amount/res_period end as curr_amount \n" +
				"  FROM er_expamtinfo\n " +
				"  join bd_billtype on er_expamtinfo.bx_pk_billtype=bd_billtype.pk_billtypecode \n " +
				" WHERE er_expamtinfo.pk_org = '"+pk_org+"'\n" + 
				"   and '"+period+"' between start_period and end_period\n" + 
				"   and nvl(er_expamtinfo.dr, 0) = 0";
			ArrayList<ExpamtinfoVO> list2 = (ArrayList<ExpamtinfoVO>) dao.executeQuery(sql, new BeanListProcessor(ExpamtinfoVO.class));
			if (list2 != null && list2.size() >0) {
				
				String[] pks = new String[list2.size()];
				for(int i=0; i<list2.size(); i++){
					pks[i] = list2.get(i).getPk_expamtinfo();
				}
				ExpamtinfoVO[] evos = iq.queryExpamtinfoByPks(pks, period);
				for(int i=0; i<list2.size(); i++){
					for(int e=0; e<evos.length; e++){
						if(list2.get(i).getPk_expamtinfo().equals(evos[e].getPk_expamtinfo())){
							list2.get(i).setAmt_status(evos[e].getAmt_status());
						}
					}
				}
				
				QueryBillVO qbillVO = new QueryBillVO();
				BillVO[] billVOs = new BillVO[1]; 
				BillVO billVO = new BillVO();
				HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new ExpamtinfoVO[0])); 
				String[] formulas = new String[] { "vostatus->1;" };
				PubTools.execFormulaWithVOs(maps, formulas);
				billVO.setTableVO("er_expamtinfo", maps); 
				billVOs[0] = billVO;
				qbillVO.setQueryVOs(billVOs);
				return qbillVO; 
			}
			return null;
		}
		return null;
	}
}
