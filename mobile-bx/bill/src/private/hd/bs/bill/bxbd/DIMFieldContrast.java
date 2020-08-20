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
import nc.itf.erm.prv.IFieldContrastService; 
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.bd.meta.BatchOperateVO; 
import nc.vo.erm.fieldcontrast.FieldcontrastVO; 
import nc.vo.pub.BusinessException; 
import nc.vo.pub.lang.UFDateTime;
/**
 * 17.MUJ00787  维度对照 
 */
public class DIMFieldContrast extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IFieldContrastService ifc = NCLocator.getInstance().lookup(IFieldContrastService.class);
  
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] cfieldMap = bodys.get("er_fieldcontrast");//控制维度
		 
		ArrayList<FieldcontrastVO> updateFVOs = new ArrayList<FieldcontrastVO>();
		ArrayList<FieldcontrastVO> deleteFVOs = new ArrayList<FieldcontrastVO>();
		ArrayList<FieldcontrastVO> addnewFVOs = new ArrayList<FieldcontrastVO>();
		
		for(int i=0; null!=cfieldMap && i<cfieldMap.length; i++){
			FieldcontrastVO fvo = (FieldcontrastVO) transMapTONCVO(FieldcontrastVO.class,cfieldMap[i]);
			if(null!=head.get("src_billtype") && !"".equals(head.get("src_billtype"))){
				String src_billtype = head.get("src_billtype").toString();
				fvo.setSrc_billtype(src_billtype);
				if(src_billtype.startsWith("261")){
					fvo.setSrc_busitype("mtapp_detail");
				}else{
					fvo.setSrc_busitype("er_cshare_detail");
				}
			}
			
			
			
			//更新
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(1)){
				fvo.setDes_billtype("~");
				fvo.setStatus(1);
				updateFVOs.add(fvo);
			}
			
			//新增
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(2)){
				fvo.setSrc_billtypepk(null!=head.get("src_billtypepk")?head.get("src_billtypepk").toString():"");
				fvo.setDes_billtype("~");
				fvo.setStatus(2);
				fvo.setDr(0);
				fvo.setApp_scene(5);
				fvo.setCreator(userid);
				fvo.setCreationtime(new UFDateTime());
				fvo.setPk_org(InvocationInfoProxy.getInstance().getGroupId());
				fvo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
				addnewFVOs.add(fvo);
			}
			
			//删除
			if(null!=cfieldMap[i].get("vostatus") && cfieldMap[i].get("vostatus").equals(3)){
				fvo.setStatus(3);
				deleteFVOs.add(fvo);
			}
		}
		BatchOperateVO batchVO = new BatchOperateVO();
		
		if(updateFVOs.size()>0){
			Object[] objs = new Object[updateFVOs.size()];
			objs = updateFVOs.toArray(new FieldcontrastVO[0]);
			batchVO.setUpdObjs(objs);
//			int[] ai = new int[]{0};
//			batchVO.setUpdIndexs(ai);
		}
//		else{
//			int[] ai = new int[]{};
//			batchVO.setUpdIndexs(ai);
//			batchVO.setUpdObjs(new Object[0]);
//		}
		if(deleteFVOs.size()>0){
			Object[] objs = new Object[deleteFVOs.size()];
			objs = deleteFVOs.toArray(new FieldcontrastVO[0]);
			batchVO.setDelObjs(objs);
//			int[] ai = new int[]{0,0};
//			batchVO.setDelIndexs(ai);
		}
//		else{
//			int[] ai = new int[]{};
//			batchVO.setDelIndexs(ai);
//			batchVO.setDelObjs(new Object[0]);
//		}
		if(addnewFVOs.size()>0){
			Object[] objs = new Object[addnewFVOs.size()];
			for(int i=0; i<objs.length; i++){
				objs[i] = addnewFVOs.get(i);
			}
			batchVO.setAddObjs(objs);
//			int[] ai = new int[]{0};
//			batchVO.setAddIndexs(ai);
		}
//		else{
//			batchVO.setAddObjs(new Object[0]);
//			int[] ai = new int[]{};
//			batchVO.setAddIndexs(ai);
//		}
		
		//新增接口
		ifc.batchSave(batchVO);
		 
		if(null!=head.get("src_billtype")){
			return queryBillVoByPK(userid, head.get("src_billtype").toString());
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
	
	String pk_org = "";
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		String sql ="select pk_billtypeid as src_billtypepk,pk_billtypecode as src_billtype, billtypename as modifier,'5' as app_scene \n" +
					"  from bd_billtype\n" + 
					" where ((parentbilltype in ('264X', '265X', '261X') or pk_billtypecode in ('264X', '265X', '261X'))\n" + 
					"   and pk_billtypecode not in ('2647')\n" + 
					"   and nvl(islock, 'N') = 'N'\n" + 
					"   and (pk_group = '"+groupid+"' or pk_org = 'GLOBLE00000000000000'))\n" + 
					"   and nvl(dr,0)=0\n" + 
					" order by pk_billtypecode";
		ArrayList<FieldcontrastVO> list = (ArrayList<FieldcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(FieldcontrastVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new FieldcontrastVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			maps[i].put("PK", headVO.get("src_billtype"));// 自由
			maps[i].put("ibillstatus", -1);// 自由
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String billtype = super.getBilltype();
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","src_billtype");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
	 
		String groupid = InvocationInfoProxy.getInstance().getGroupId();   
		String sql = "SELECT * FROM er_fieldcontrast WHERE pk_org ='"+groupid+"' "+str+" and app_scene =5 ";
		ArrayList<FieldcontrastVO> list2 = (ArrayList<FieldcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(FieldcontrastVO.class));
		if (list2 != null && list2.size() >0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new FieldcontrastVO[0])); 
			billVO.setTableVO("er_fieldcontrast", maps); 
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO; 
		}
		return null;
	}
	
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		if(null!=head.get("src_billtype") && !"".equals(head.get("src_billtype")) ){ 
			String sql = " SELECT * FROM er_fieldcontrast " +
					     " WHERE pk_org='"+InvocationInfoProxy.getInstance().getGroupId()+"' " +
					     " and app_scene = 5 " +
					     " and src_billtype = '"+head.get("src_billtype")+"'";	
			ArrayList<FieldcontrastVO> list = (ArrayList<FieldcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(FieldcontrastVO.class));
			BatchOperateVO batchVO = new BatchOperateVO();
			Object[] objs = new Object[list.size()];
			if(null!=list && list.size()>0){
				objs = list.toArray(new FieldcontrastVO[0]);
				batchVO.setDelObjs(objs);
				//删除接口
				ifc.batchSave(batchVO);
			}
		}else{
			throw new BusinessException("主键不存在,删除失败!");
		} 
		return null;
	}
}

