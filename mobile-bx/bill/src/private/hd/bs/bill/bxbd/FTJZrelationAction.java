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
import nc.itf.erm.billcontrast.IErmBillcontrastManage; 
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.bd.meta.BatchOperateVO;  
import nc.vo.erm.billcontrast.BillcontrastVO;
import nc.vo.pub.BusinessException; 

/**
 * 13. MUJ00783 分摊结转单据对应设置-集团
 * 14. MUJ00784 分摊结转单据对应设置-组织
 */
public class FTJZrelationAction extends ApproveWorkAction{

	//分摊结转单据对应设置-集团  固定功能注册编码
	String funnode = "MUJ00783";
		
	BaseDAO dao = new BaseDAO();
	IErmBillcontrastManage abm = NCLocator.getInstance().lookup(IErmBillcontrastManage.class);
	
	/**
	 * 保存-新增保存和修改保存
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		
		BatchOperateVO batchVO = new BatchOperateVO();
		Object[] objs = new Object[1];
		
		BillcontrastVO vo = new BillcontrastVO();
		if(null!=head.get("src_tradetypeid") && !"".equals(head.get("src_tradetypeid").toString().trim())){
			vo.setSrc_tradetypeid(head.get("src_tradetypeid").toString());
		}else{
			throw new BusinessException("来源交易类型不能空");
		}
		if(null!=head.get("des_tradetypeid") && !"".equals(head.get("des_tradetypeid").toString().trim())){
			vo.setDes_tradetypeid(head.get("des_tradetypeid").toString());
		}else{
			throw new BusinessException("目标交易类型不能空");
		}
		String sql ="select bd_billtype.pk_billtypeid as src_tradetypeid,bd_billtype.pk_billtypecode as src_tradetype," +
					"		pb.pk_billtypeid as src_billtypeid ,pb.pk_billtypecode as src_billtype\n" +
					"from bd_billtype\n" + 
					"join bd_billtype pb on bd_billtype.parentbilltype=pb.pk_billtypecode\n" + 
					"where nvl(bd_billtype.dr,0)=0\n" + 
					"and nvl(pb.dr,0)=0\n" + 
					"and bd_billtype.pk_billtypeid in ('"+head.get("src_tradetypeid")+"','"+head.get("des_tradetypeid")+"')";
		ArrayList<BillcontrastVO> list = (ArrayList<BillcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(BillcontrastVO.class));
		if(null==list || list.size()<2){
			throw new BusinessException("交易类型在单据管理档案中不存在!");
		}else{
			for(int i=0; i<list.size(); i++){
				if(list.get(i).getSrc_tradetypeid().equals(head.get("src_tradetypeid"))){
					vo.setSrc_tradetype(list.get(i).getSrc_tradetype());
					vo.setSrc_billtypeid(list.get(i).getSrc_billtypeid());
					vo.setSrc_billtype(list.get(i).getSrc_billtype());
				}
				if(list.get(i).getSrc_tradetypeid().equals(head.get("des_tradetypeid"))){
					vo.setDes_tradetype(list.get(i).getSrc_tradetype());
					vo.setDes_billtypeid(list.get(i).getSrc_billtypeid());
					vo.setDes_billtype(list.get(i).getSrc_billtype());
				}
			}
		}
		vo.setDr(0);
		vo.setApp_scene(1);
		vo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
		String billtype = super.getBilltype();
		if(!funnode.equals(billtype)&& null!=head.get("pk_org") && !"".equals(head.get("pk_org"))){
			vo.setPk_org(head.get("pk_org").toString());
		}
		if(funnode.equals(billtype)&& null!=head.get("pk_org") && !"".equals(head.get("pk_org"))){
			vo.setPk_org(InvocationInfoProxy.getInstance().getGroupId());
		}
		if(null!=head.get("pk_billcontrast") && !"".equals(head.get("pk_billcontrast"))){
			vo.setPk_billcontrast(head.get("pk_billcontrast").toString());
		}
		objs[0] = vo;
		if(null!=head.get("pk_billcontrast") || (null!=head.get("vostatus")&&head.get("vostatus").toString().equals(1))){
			batchVO.setUpdObjs(objs);
		}else{
			batchVO.setAddObjs(objs);
		}
		abm.batchSave(batchVO);
		
		if(null!=head.get("pk_billcontrast") && !"".equals(head.get("pk_billcontrast"))){
			return queryBillVoByPK(userid,head.get("pk_billcontrast").toString());
		}
		sql =" select * from er_billcontrast where isnull(dr,0)=0 order by ts desc";
		list = (ArrayList<BillcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(BillcontrastVO.class));
		if (null!=list && list.size()>0) {
			return queryBillVoByPK(userid,list.get(0).getPk_billcontrast());
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
	
	String pk_org = "";
	/**
	 * 界面显示只有主表 
	 */
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_billcontrast");
		map.put("PK","pk_billcontrast");
		StringBuffer str = reCondition(obj, true,map);
		
		String billtype = super.getBilltype();
		
		if(null!=str){
			pk_org = str.toString();
		} 
		if(null!=billtype && billtype.equals(funnode) && null!=str && !str.toString().contains("pk_billcontrast")){
			//MUJ00783固定【分摊结转单据对应设置-集团】节点
			pk_org = " and pk_org='"+InvocationInfoProxy.getInstance().getGroupId()+"' ";
		}
		String sql = "select * from er_billcontrast where nvl(dr,0)=0 "+pk_org+" and pk_org<>'GLOBLE00000000000000' ";
		ArrayList<BillcontrastVO> list = (ArrayList<BillcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(BillcontrastVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new BillcontrastVO[0]));
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
	
	 
	
	/**
	 * 删除整行档案
	 */
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		if(null!=head.get("pk_billcontrast") && !"".equals(head.get("pk_billcontrast")) ){
			BatchOperateVO batchVO = new BatchOperateVO();
			Object[] objs = new Object[1];
			BillcontrastVO vo = new BillcontrastVO();
			vo.setPk_billcontrast(head.get("pk_billcontrast").toString());
			objs[0] = vo;
			batchVO.setDelObjs(objs);
			//删除接口
			abm.batchSave(batchVO);
		}else{
			throw new BusinessException("主键不存在,删除失败!");
		} 
		return null;
	}
}
