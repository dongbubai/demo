package hd.bs.bill.bxbd;

import java.util.ArrayList;
import java.util.HashMap; 
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator; 
import nc.jdbc.framework.processor.BeanListProcessor; 
import nc.vo.bd.meta.BatchOperateVO; 
import nc.vo.er.expensetype.ExpenseTypeVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;  
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
import nc.itf.er.expensetype.IExpenseTypeService;
/**
 * 8. MUJ00778 费用类型设置
 */
public class FeeTypeSetAction  extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IExpenseTypeService iets = NCLocator.getInstance().lookup(IExpenseTypeService.class);
	
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		
		BatchOperateVO batchVO = new BatchOperateVO();
		Object[] objs = new Object[1];
		
		ExpenseTypeVO vo = new ExpenseTypeVO();
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
		String pk_expensetype = "";
		if(null!=head.get("pk_expensetype") && !"".equals(head.get("pk_expensetype")) ){
			vo.setPk_expensetype(head.get("pk_expensetype").toString());
			pk_expensetype = " and pk_expensetype<>'"+head.get("pk_expensetype")+"' ";
		}
		String sql = "select * from  er_expensetype where isnull(dr,0)=0 "+pk_expensetype+" and (code='"+head.get("code")+"' or name='"+head.get("name")+"') and pk_group<>'global00000000000000'";
		ArrayList<ExpenseTypeVO> list = (ArrayList<ExpenseTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ExpenseTypeVO.class));
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
		if(null!=head.get("pk_expensetype") || (null!=head.get("vostatus")&&head.get("vostatus").toString().equals(1))){
			batchVO.setUpdObjs(objs);
		}else{
			batchVO.setAddObjs(objs);
		}
		iets.batchSaveExpenseType(batchVO);
		sql =" select * from  er_expensetype where isnull(dr,0)=0 order by ts desc";
		list = (ArrayList<ExpenseTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ExpenseTypeVO.class));
		if (null!=list && list.size()>0) {
			return queryBillVoByPK(userid,list.get(0).getPk_expensetype());
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
		map.put("PPK","pk_expensetype");
		StringBuffer str = reCondition(obj, true,map);
		if(null!=str && str.toString().contains("pk_org")){
			str = new StringBuffer();
		} 
		 
		String sql =" select * from  er_expensetype where isnull(dr,0)=0 "+str+" and pk_group<>'global00000000000000' order by ts ";
		ArrayList<ExpenseTypeVO> list = (ArrayList<ExpenseTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ExpenseTypeVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new ExpenseTypeVO[0]));
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
		
		BatchOperateVO batchVO = new BatchOperateVO();
		Object[] objs = new Object[1];
		
		ExpenseTypeVO vo = new ExpenseTypeVO(); 
		if(null!=head.get("pk_expensetype") && !"".equals(head.get("pk_expensetype")) ){
			vo.setPk_expensetype(head.get("pk_expensetype").toString());
		}else{
			throw new BusinessException("主键不存在,删除失败!");
		} 
		objs[0] = vo;
		batchVO.setDelObjs(objs);
		iets.batchSaveExpenseType(batchVO);
		
		return null;
	}
}
