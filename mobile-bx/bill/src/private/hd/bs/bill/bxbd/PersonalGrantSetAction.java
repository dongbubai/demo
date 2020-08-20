package hd.bs.bill.bxbd;

import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO; 
import java.util.ArrayList;
import java.util.HashMap; 
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.er.indauthorize.IIndAuthorizeService; 
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.bd.meta.BatchOperateVO;
import nc.vo.er.indauthorize.IndAuthorizeVO; 
import nc.vo.pub.BusinessException;  
import nc.vo.pub.lang.UFDateTime;
import nc.vo.sm.UserVO;
/**
 * 7. MUJ00777  个人授权设置
 */
public class PersonalGrantSetAction extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IIndAuthorizeService ifc = NCLocator.getInstance().lookup(IIndAuthorizeService.class);
  
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		 
		ArrayList<IndAuthorizeVO> updateFVOs = new ArrayList<IndAuthorizeVO>();
		ArrayList<IndAuthorizeVO> deleteFVOs = new ArrayList<IndAuthorizeVO>();
		ArrayList<IndAuthorizeVO> addnewFVOs = new ArrayList<IndAuthorizeVO>();
		
		if(null!=head){
			if(null==head.get("pk_org") || "".equals(head.get("pk_org"))){
				throw new BusinessException("组织不能空!");
			}
			if(null==head.get("pk_operator") || "".equals(head.get("pk_operator"))){
				throw new BusinessException("授权操作员不能空!");
			}
			if(null==head.get("startdate") || "".equals(head.get("startdate"))){
				throw new BusinessException("开始日期不能空!");
			}
			if(null==head.get("enddate") || "".equals(head.get("enddate"))){
				throw new BusinessException("结束日期不能空!");
			}
			if(null==head.get("pk_billtypeid") || "".equals(head.get("pk_billtypeid"))){
				throw new BusinessException("交易类型不能空!");
			}
			if(null==head.get("billtype") || "".equals(head.get("billtype"))){
				throw new BusinessException("交易类型编码不能空!");
			}
			String and = "";
			if(null!=head.get("pk_authorize") && !"".equals(head.get("pk_authorize"))){
				and = " and pk_authorize<>'"+head.get("pk_authorize")+"'";
			}
			/**
			 * 校验操作员是否有相同交易类型
			 */
			String sql ="select *\n" +
						"  from er_indauthorize\n" + 
						"  where type = 1\n" + 
						"  and nvl(dr,0)=0\n" + 
						"  and pk_billtypeid='"+head.get("pk_billtypeid")+"'\n" + 
						"  and pk_operator='"+head.get("pk_operator")+"'\n" + 
						"  "+and+"";
			ArrayList<IndAuthorizeVO> list2 = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
			if (null!=list2 && list2.size()>0) {
				throw new BusinessException("操作员不能授权相同的交易类型,请重新选择!");
			}
			IndAuthorizeVO fvo = (IndAuthorizeVO) transMapTONCVO(IndAuthorizeVO.class,head);
			if(fvo.getStartdate().after(fvo.getEnddate())){
				throw new BusinessException("开始日期不能大于结束日期!");
			}
			fvo.setType(1);
			fvo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
			UFDateTime dt = new UFDateTime(head.get("enddate").toString().substring(0,10)+" 23:59:59");
			fvo.setEnddate(dt.getDate());
			fvo.setTs(new UFDateTime());
			fvo.setPk_org(head.get("pk_org").toString());
			fvo.setBilltype(head.get("billtype").toString());
			sql = "select * from sm_user where cuserid='"+userid+"' and isnull(dr,0)=0 ";
			ArrayList<UserVO> list = (ArrayList<UserVO>) dao.executeQuery(sql, new BeanListProcessor(UserVO.class));
			if(null!=list && list.size()>0){
				fvo.setPk_user(list.get(0).getPk_psndoc());
			}

			//更新
			if(null!=head.get("vostatus") && head.get("vostatus").equals(1)){
				fvo.setStatus(1);
				fvo.setDr(0);
				updateFVOs.add(fvo);
			}
			
			//新增
			if(null!=head.get("vostatus") && head.get("vostatus").equals(2)){
				fvo.setStatus(2);
				fvo.setDr(0);
				addnewFVOs.add(fvo);
			}
			
			//删除
			if(null!=head.get("vostatus") && head.get("vostatus").equals(3)){
				fvo.setStatus(3);
				deleteFVOs.add(fvo);
			}
		}
		BatchOperateVO batchVO = new BatchOperateVO();
		
		if(updateFVOs.size()>0){
			//更新调产品接口报错，用dao
			dao.updateVOList(updateFVOs);
//			Object[] objs = new Object[updateFVOs.size()];
//			for(int i=0; i<objs.length; i++){
//				objs[i] = updateFVOs.get(i);
//			}
////			objs = updateFVOs.toArray(new IndAuthorizeVO[0]);
//			batchVO.setUpdObjs(objs);
//			int[] ai = new int[]{0};
//			batchVO.setUpdIndexs(ai);
		}
		if(deleteFVOs.size()>0){
			Object[] objs = new Object[deleteFVOs.size()];
			objs = deleteFVOs.toArray(new IndAuthorizeVO[0]);
			batchVO.setDelObjs(objs);
		}else{
			int[] ai = new int[]{};
			batchVO.setDelIndexs(ai);
			batchVO.setDelObjs(new Object[0]);
		}
		if(addnewFVOs.size()>0){
			Object[] objs = new Object[addnewFVOs.size()];
			objs = addnewFVOs.toArray(new IndAuthorizeVO[0]);
			batchVO.setAddObjs(objs);
		}else{
			batchVO.setAddObjs(new Object[0]);
			int[] ai = new int[]{};
			batchVO.setAddIndexs(ai);
		}
		
		String pk_authorize = "";
		//新增接口
		BatchOperateVO bvo = ifc.batchSaveIndAuthorize(batchVO);
		
		if(null!=bvo.getAddObjs()){
			Object[] add = bvo.getAddObjs();
			if(null!=add && add.length>0){
				IndAuthorizeVO vo = (IndAuthorizeVO) add[0];
				pk_authorize = vo.getPk_authorize();
			}
		}
		if(null!=bvo.getUpdObjs()){
			Object[] update = bvo.getUpdObjs();
			if(null!=update && update.length>0){
				IndAuthorizeVO vo = (IndAuthorizeVO) update[0];
				pk_authorize = vo.getPk_authorize();
			}
		}
		if(null!=pk_authorize){
			return queryBillVoByPK(userid, pk_authorize);
		}else{
			return null;
		}
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
		if(null!=head_querybillVO){
			BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
			billVO.setHeadVO(head_BillVO.getHeadVO());
			return billVO;
		}else{
			return null;
		}
	}
	
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_authorize");
		StringBuffer str = reCondition(obj, true,map);
		String and = "";
		if(null!=str && !str.toString().contains("pk_authorize")){
			and = " and er_indauthorize.pk_group = '"+groupid+"'\n" + 
				 "  and sm_user.cuserid = '"+userid+"'\n" ; 
			str = new StringBuffer();
		}
		String sql ="select distinct er_indauthorize.* \n" +
					"  from er_indauthorize er_indauthorize\n" + 
					"  join sm_user on er_indauthorize.pk_user=sm_user.pk_psndoc\n" + 
					" where nvl(er_indauthorize.dr,0)=0 \n" + 
					" "+and+" "+str+""+
					"   and type = 1";
		ArrayList<IndAuthorizeVO> list = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new IndAuthorizeVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			maps[i].put("ibillstatus", -1);// 
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}
 
	
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		if(null!=head.get("pk_authorize") && !"".equals(head.get("pk_authorize")) ){ 
			IndAuthorizeVO vo = new IndAuthorizeVO();
			vo.setPk_authorize(head.get("pk_authorize").toString());
			BatchOperateVO batchVO = new BatchOperateVO();
			Object[] objs = new Object[1];
			objs[0] = vo;
			batchVO.setDelObjs(objs);
			//删除接口
			ifc.batchSaveIndAuthorize(batchVO);
		}else{
			throw new BusinessException("主键不存在,删除失败!");
		} 
		return null;
	}
}

