package hd.bs.bill.bxbd;

import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
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
import nc.itf.er.reimtype.IReimTypeService;
import nc.itf.uap.bd.refcheck.IReferenceCheck;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.bd.meta.BatchOperateVO; 
import nc.vo.er.reimtype.ReimTypeVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
/**
 * 9. MUJ00779 报销类型设置
 */
public class BXTypeSetAction extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IReimTypeService irts = NCLocator.getInstance().lookup(IReimTypeService.class);
	IReferenceCheck del = NCLocator.getInstance().lookup(IReferenceCheck.class);
	
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		
		BatchOperateVO batchVO = new BatchOperateVO();
		Object[] objs = new Object[1];
		
		ReimTypeVO vo = new ReimTypeVO();
		if(null!=head.get("code") && !"".equals(head.get("code").toString().trim())){
			vo.setCode(head.get("code").toString());
		}else{
			throw new BusinessException("需要录入编码项!");
		}
		if(null!=head.get("name") && !"".equals(head.get("name").toString().trim())){
			vo.setName(head.get("name").toString());
		}else{
			throw new BusinessException("需要录入名称项!");
		}
		String pk_reimtype = "";
		if(null!=head.get("pk_reimtype") && !"".equals(head.get("pk_reimtype")) ){
			vo.setPk_reimtype(head.get("pk_reimtype").toString());
			pk_reimtype = " and pk_reimtype<>'"+head.get("pk_reimtype")+"' ";
		}
		String sql = "select * from er_reimtype where isnull(dr,0)=0 "+pk_reimtype+" and (code='"+head.get("code")+"' or name='"+head.get("name")+"') ";
		ArrayList<ReimTypeVO> list = (ArrayList<ReimTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ReimTypeVO.class));
		if(null!=list && list.size()>0){
			throw new BusinessException("编码或名称重复，请重新输入!");
		}
		
		if(null!=head.get("inuse") && !"".equals(head.get("inuse"))){
			vo.setInuse(new UFBoolean(head.get("inuse").toString()));
		}else{
			vo.setInuse(new UFBoolean(false));
		}
		vo.setDr(0);
		vo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
		vo.setMemo(null!=head.get("memo")?head.get("memo").toString():"");
		objs[0] = vo;
		if(null!=head.get("pk_reimtype") || (null!=head.get("vostatus")&&head.get("vostatus").toString().equals(1))){
			batchVO.setUpdObjs(objs);
		}else{
			batchVO.setAddObjs(objs);
		}
		irts.batchSaveReimType(batchVO);
		sql =" select * from er_reimtype where isnull(dr,0)=0 order by ts desc";
		list = (ArrayList<ReimTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ReimTypeVO.class));
		if (null!=list && list.size()>0) {
			return queryBillVoByPK(userid,list.get(0).getPk_reimtype());
		}
		return null;
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

		// 查询 表头
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_reimtype");
		StringBuffer str = reCondition(obj, true,map);
		if(null!=str && str.toString().contains("pk_org")){
			str = new StringBuffer();
		} 
		 
		String sql =" select * from er_reimtype where isnull(dr,0)=0 "+str+" order by ts ";
		ArrayList<ReimTypeVO> list = (ArrayList<ReimTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ReimTypeVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new ReimTypeVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			PubTools.execFormulaWithVOs(maps, formulas);
			maps[i].put("ibillstatus", -1);// 自由
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
		if(null!=head.get("pk_reimtype") && !"".equals(head.get("pk_reimtype")) ){
			//删除前检查接口
			del.isReferenced("er_reimtype",head.get("pk_reimtype").toString());
			BatchOperateVO batchVO = new BatchOperateVO();
			Object[] objs = new Object[1];
			ReimTypeVO vo = new ReimTypeVO();
			vo.setPk_reimtype(head.get("pk_reimtype").toString());
			objs[0] = vo;
			batchVO.setDelObjs(objs);
			//删除接口
			irts.batchSaveReimType(batchVO);
		}else{
			throw new BusinessException("主键不存在,删除失败!");
		} 
		return null;
	}
}
