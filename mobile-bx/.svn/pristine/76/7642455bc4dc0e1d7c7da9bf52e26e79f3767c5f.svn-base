package hd.bs.bill.bxbd;

import java.util.ArrayList;
import java.util.HashMap;  
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.er.reimtype.IReimTypeService;
import nc.jdbc.framework.processor.BeanListProcessor; 
import nc.vo.er.djlx.DjLXVO; 
import nc.vo.er.reimrule.ReimRulerVO; 
import nc.vo.pub.BusinessException; 
import nc.vo.pub.lang.UFDouble;
import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools; 
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
/**
 * 10. MUJ00780 分摊标准设置
 */
public class BXStandardAction extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IReimTypeService its = NCLocator.getInstance().lookup(IReimTypeService.class);
	
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] rulerMap = bodys.get("er_reimruler"); 
		String billtype = "";
		String pk_group = "";
		String pk_org = "";
		if(null!=head.get("pk_billtype") && !"".equals(head.get("pk_billtype").toString().trim())){
			billtype = head.get("pk_billtype").toString();
		}else{
			throw new BusinessException("交易类型不能为空!");
		}
		if(null!=head.get("pk_group") && !"".equals(head.get("pk_group").toString().trim())){
			pk_group = head.get("pk_group").toString();
		}else{
			throw new BusinessException("集团不能为空!");
		}
		if(null!=head.get("pk_org") && !"".equals(head.get("pk_org").toString().trim())){
			pk_org = head.get("pk_org").toString();
		}else{
			throw new BusinessException("组织不能为空!");
		}
		int maxpri = -1;
		HashMap unmap = new HashMap();
		HashMap prioritymap = new HashMap();
		ArrayList<ReimRulerVO> rrvo = new ArrayList<ReimRulerVO>();
		boolean ispriority = false;
		
		for(int i=0; i<rulerMap.length; i++){
			if(null!=rulerMap[i].get("vostatus")&&(rulerMap[i].get("vostatus").equals(3)||rulerMap[i].get("vostatus").equals("3"))){
				continue;
			}
			ReimRulerVO vo = new ReimRulerVO();
			vo.setPk_billtype(billtype);
			vo.setPk_group(pk_group);
			vo.setPk_org(pk_org);
			if(null!=rulerMap[i].get("pk_reimtype") && !"".equals(rulerMap[i].get("pk_reimtype").toString().trim())){
				vo.setPk_reimtype(rulerMap[i].get("pk_reimtype").toString());
			}else{
				throw new BusinessException("第"+i+"行,报销类型不能为空!");
			}
			if(null!=rulerMap[i].get("pk_currtype") && !"".equals(rulerMap[i].get("pk_currtype").toString().trim())){
				vo.setPk_currtype(rulerMap[i].get("pk_currtype").toString());
			}else{
				throw new BusinessException("第"+i+"行,币种不能为空!");
			}
			if(null!=rulerMap[i].get("amount") && !"".equals(rulerMap[i].get("amount").toString().trim())){
				vo.setAmount(new UFDouble(rulerMap[i].get("amount").toString()));
			}else{
				throw new BusinessException("第"+i+"行,金额不能为空!");
			}
			String pk_position = null!=rulerMap[i].get("pk_position")?rulerMap[i].get("pk_position").toString():"";
			String pk_deptid = null!=rulerMap[i].get("pk_deptid")?rulerMap[i].get("pk_deptid").toString():"";
			String memo = null!=rulerMap[i].get("memo")?rulerMap[i].get("memo").toString():"";
			String priority = null!=rulerMap[i].get("priority")?rulerMap[i].get("priority").toString():"";
			
			String unstr = rulerMap[i].get("pk_reimtype").toString()+pk_deptid+pk_position+rulerMap[i].get("pk_currtype").toString()+rulerMap[i].get("amount")+memo+priority;
			if(unmap.containsKey(unstr)){
				throw new BusinessException("规则设置中包含各个维度全部相同的记录!");
			}else{
				unmap.put(unstr,unstr);
			}
			//新增或更新的优先级不能相同
			if(null!=rulerMap[i].get("vostatus")&&(rulerMap[i].get("vostatus").equals(1)||rulerMap[i].get("vostatus").equals("1")||
												   rulerMap[i].get("vostatus").equals(2)||rulerMap[i].get("vostatus").equals("2"))){
				if(null!=priority && !"".equals(priority)){
					if(prioritymap.containsKey(priority)){
						throw new BusinessException("优先级不能相同!");
					}else{
						prioritymap.put(priority, priority);
					}
					if(maxpri<Integer.parseInt(priority)){
						maxpri = Integer.parseInt(priority);
					}
					vo.setPriority(Integer.parseInt(priority));
				}else{
					ispriority = true;
				}
			}
			vo.setPk_position(pk_position);
			vo.setPk_deptid(pk_deptid);
			vo.setMemo_name(memo);
			vo.setMemo(memo);

			rrvo.add(vo);
		}
		
		if(ispriority){
			if(maxpri>=0){
				maxpri = maxpri+1;
			}
			if(maxpri==-1){
				maxpri = 0;
			}
			for(int i=0; i<rrvo.size(); i++){
				if(null==rrvo.get(i).getPriority()){
					rrvo.get(i).setPriority(maxpri);
					maxpri = maxpri+1;
				}
			}
		}
		
		List<ReimRulerVO> returnvo = its.saveReimRule(billtype,pk_group,pk_org,rrvo.toArray(new ReimRulerVO[0]));
		
		if (null!=returnvo && returnvo.size()>0) {
			return queryBillVoByPK(userid,pk_org+"-"+billtype);
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
		
		String pk_org = "";
		String djlxbm = "";
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();

			for (int i = 0; i < conVOs.length; i++) {
				if (conVOs[i].getField().contains("pk_org")) {
					pk_org = conVOs[i].getValue();
				}
				if (conVOs[i].getField().contains("PPK")) {
					String[] strs = conVOs[i].getValue().split("-");
					if(null!=strs && strs.length==2){
						pk_org = strs[0];
						djlxbm = " and djlxbm='"+strs[1]+"'";
					}
				}
			}
		}
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","er_sharerule.pk_sharerule");
		StringBuffer str = reCondition(obj, true,map);
		 
		String sql ="select djlxbm,djlxjc,djlxoid,'"+pk_org+"' as sfbz \n" +
					"from er_djlx\n" + 
					"where pk_group = '"+InvocationInfoProxy.getInstance().getGroupId()+"'\n" + 
					"and djdl in ( 'jk', 'bx' )\n" + 
					"and (bxtype=1 or bxtype is null)\n" + 
					"and nvl(dr,0)=0 \n" +
					" "+djlxbm+" " +
					"order by djlxbm \n";
		ArrayList<DjLXVO> list = (ArrayList<DjLXVO>) dao.executeQuery(sql, new BeanListProcessor(DjLXVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new DjLXVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			maps[i].put("ibillstatus", -1);// 自由
			maps[i].put("pk_billtype", headVO.get("djlxbm")); 
			maps[i].put("pk_org", headVO.get("sfbz")); 
			maps[i].put("pk_group", InvocationInfoProxy.getInstance().getGroupId()); 
			maps[i].put("PK", headVO.get("sfbz")+"-"+headVO.get("djlxbm")); 
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}
	
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String pk_org = "";
		String pk_billtype = "";
		String sql = "";
		if (obj != null) {
			ConditionAggVO conAggVO = (ConditionAggVO) obj;
			ConditionVO[] conVOs = conAggVO.getConditionVOs();
			for (int i = 0; i < conVOs.length; i++) {
				if (conVOs[i].getField().contains("PPK")) {
					String[] strs = conVOs[i].getValue().split("-");
					if(null!=strs && strs.length==2){
						pk_org = strs[0];
						pk_billtype = strs[1];
					}
					sql = "SELECT * FROM er_reimruler where pk_billtype='"+pk_billtype+"' and pk_org='"+pk_org+"' and nvl(dr,0)=0 order by priority desc";
				}
			}
		}
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_sharerule");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
		
		ArrayList<ReimRulerVO> list2 = (ArrayList<ReimRulerVO>) dao.executeQuery(sql, new BeanListProcessor(ReimRulerVO.class));
		if (list2 != null && list2.size() >0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new ReimRulerVO[0])); 
			String[] formulas = new String[] { "vostatus->1;" };
			PubTools.execFormulaWithVOs(maps, formulas);
			billVO.setTableVO("er_reimruler", maps); 
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO; 
		}
		return null;
	}
}
