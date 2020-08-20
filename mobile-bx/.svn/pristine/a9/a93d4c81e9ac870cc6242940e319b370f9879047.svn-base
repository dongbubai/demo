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
import nc.itf.arap.prv.IBXBillPrivate;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.ep.bx.SqdlrVO; 
import nc.vo.er.indauthorize.IndAuthorizeVO; 
import nc.vo.pub.BusinessException; 
import nc.vo.uap.rbac.role.RoleVO;
/**
 * 6.MUJ00776  授权代理设置 
 */
public class GrantAgentSetAction extends ApproveWorkAction{
	
	IBXBillPrivate abm = NCLocator.getInstance().lookup(IBXBillPrivate.class);

	BaseDAO dao = new BaseDAO();
	/**
	 * 授权代理设置  固定功能注册编码
	 */
	String funnode = "MUJ00776";
	/**
	 * 保存-新增保存和修改保存
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] psnMap = bodys.get("er_indauthorize"); 
		HashMap<String, Object>[] deptMap = bodys.get("er_indauthorize2"); 

		ArrayList<SqdlrVO> sVOs = new ArrayList<SqdlrVO>(); 
		/**
		 * 表头的 代理本部门和代理所有人勾选
		 */
		if(null!=head.get("pk_role") && !"".equals(head.get("pk_role"))){
		}else{
			throw new BusinessException("角色不能为空!");
		}
		//代理本部门
		if(null!=head.get("issamedept") && head.get("issamedept").equals("Y")){
			SqdlrVO svo = new SqdlrVO();
			svo.setKeyword("issamedept");
			svo.setPk_user("true");
			svo.setType(0);
			svo.setPk_roler(head.get("pk_role").toString());
			svo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
			sVOs.add(svo);
		}
		//代理所有人
		if(null!=head.get("isall") && head.get("isall").equals("Y")){
			SqdlrVO svo = new SqdlrVO();
			svo.setKeyword("isall");
			svo.setPk_user("true");
			svo.setType(0);
			svo.setPk_roler(head.get("pk_role").toString());
			svo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
			sVOs.add(svo);
		}
		
		HashMap<String, String> psnmap = new HashMap<String, String>();
		HashMap<String, String> dpetmap = new HashMap<String, String>();
		
		/**
		 * 业务员页签
		 */
		for(int i=0; null!=psnMap && i<psnMap.length; i++){
			//更新或新增
			if(null!=psnMap[i].get("vostatus") && (psnMap[i].get("vostatus").equals(1)||psnMap[i].get("vostatus").equals(2))){
				if(null==psnMap[i].get("pk_user") || "".equals(psnMap[i].get("pk_user"))){
					throw new BusinessException("业务员不能为空!");
				}
				if(psnmap.containsKey(psnMap[i].get("pk_user"))){
					throw new BusinessException("第"+i+"行,存在重复业务员,不能保存!");
				}else{
					psnmap.put(psnMap[i].get("pk_user").toString(),psnMap[i].get("pk_user").toString());
				}
				SqdlrVO svo = new SqdlrVO();
				svo.setKeyword("busiuser");
				svo.setPk_user(psnMap[i].get("pk_user").toString());
				svo.setType(0);
				svo.setPk_roler(head.get("pk_role").toString());
				if(null!=psnMap[i].get("pk_org") && !"".equals(psnMap[i].get("pk_org"))){
					svo.setPk_org(psnMap[i].get("pk_org").toString());
				}
				svo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
				sVOs.add(svo);
			}
		}
		/**
		 * 部门页签
		 */
		for(int i=0; null!=deptMap && i<deptMap.length; i++){
			//更新或新增
			if(null!=deptMap[i].get("vostatus") && (deptMap[i].get("vostatus").equals(1)||deptMap[i].get("vostatus").equals(2))){
				if(null==deptMap[i].get("pk_deptdoc") || "".equals(deptMap[i].get("pk_deptdoc"))){
					throw new BusinessException("部门不能为空!");
				}
				if(dpetmap.containsKey(deptMap[i].get("pk_deptdoc"))){
					throw new BusinessException("第"+i+"行,存在重复部门,不能保存!");
				}else{
					dpetmap.put(deptMap[i].get("pk_deptdoc").toString(),deptMap[i].get("pk_deptdoc").toString());
				}
				SqdlrVO svo = new SqdlrVO();
				svo.setKeyword("pk_deptdoc");
				svo.setPk_user(deptMap[i].get("pk_deptdoc").toString());
				svo.setType(0);
				svo.setPk_roler(head.get("pk_role").toString());
				if(null!=deptMap[i].get("pk_org") && !"".equals(deptMap[i].get("pk_org"))){
					svo.setPk_org(deptMap[i].get("pk_org").toString());
				}
				svo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
				sVOs.add(svo);
			}
		}
		
		//调用接口
		if(sVOs.size()>0){
			abm.saveSqdlVO(sVOs, " dr=0 and pk_roler in ('"+head.get("pk_role").toString()+"') ");
		}
		 
		if(null!=head.get("pk_role")){
			return queryBillVoByPK(userid, head.get("pk_role").toString());
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
		if(null!=body_querybillVO.getQueryVOs()){
			billVO = body_querybillVO.getQueryVOs()[0];
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	String pk_org = "";
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PK","pk_role");
		map.put("pk_org","org_orgs.pk_org");
		StringBuffer str = reCondition(obj, true,map);
		if(null!=str && str.toString().contains("pk_org")){
			pk_org = str.toString();
		}else if(null!=str && !str.toString().contains("pk_role")){
			String groupid = InvocationInfoProxy.getInstance().getGroupId();
			pk_org = " and sm_role.pk_org='"+groupid+"'";
		}
		//角色
		String sql ="SELECT sm_role.*,org_orgs.name as role_memo\n" +
					"FROM sm_role\n" + 
					"join org_orgs on org_orgs.pk_org=sm_role.pk_org\n" + 
					"where nvl(sm_role.dr,0)=0\n" + 
					"and  role_type = 1 "+pk_org+" ORDER BY role_code;";
		ArrayList<RoleVO> list = (ArrayList<RoleVO>) dao.executeQuery(sql, new BeanListProcessor(RoleVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		//代理部门
		if(pk_org.contains("org_orgs.pk_org ")){
			pk_org = pk_org.replace("org_orgs.pk_org ", "pk_org");
		}
		String sql1 = " SELECT * FROM er_indauthorize WHERE (keyword='issamedept' or keyword='isall') " +
					  " and pk_roler in (SELECT pk_role FROM sm_role WHERE role_type = 1 "+pk_org+") and nvl(dr,0)=0";
		ArrayList<IndAuthorizeVO> list1 = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql1, new BeanListProcessor(IndAuthorizeVO.class));
		
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new RoleVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			for(int e=0; e<list1.size(); e++){
				if(headVO.get("pk_role").equals(list1.get(e).getPk_roler())){
					if(list1.get(e).getKeyword().contains("issamedept")){
						maps[i].put("issamedept", true);// 代理部门
					}
					if(list1.get(e).getKeyword().contains("isall")){
						maps[i].put("isall", true);// 代理所有人
					}
				}
			}
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
		map.put("PPK","pk_roler");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
		if(null!=billtype && funnode.equals(billtype)){
			//MUJ00770固定【费用申请控制规则设置-集团】节点
			String groupid = InvocationInfoProxy.getInstance().getGroupId();
			pk_org = " and pk_org='"+groupid+"'";
		} 
		
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		String sql = "";
		String action = super.getAction();// 每次执行此方法，返回页签名称
		if(null!=action && !action.contains("QUERY")){
			// 业务员 
			sql = " SELECT er_indauthorize.*, bd_psnjob.pk_dept as pk_billtypeid " +
				  " FROM er_indauthorize " +
				  " join bd_psnjob on bd_psnjob.pk_psndoc=er_indauthorize.pk_user and enddutydate is null and ismainjob='Y' " +
				  " WHERE keyword='busiuser' "+str+" and nvl(er_indauthorize.dr,0)=0";
			ArrayList<IndAuthorizeVO> list = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
			if (list != null && list.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new IndAuthorizeVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas);
				for(int i=0; i<maps.length; i++){
					maps[i].put("pk_deptdoc", maps[i].get("pk_billtypeid"));
				}
				billVO.setTableVO("er_indauthorize", maps); 
			} 
			// 部门
			sql = " SELECT er_indauthorize.*, org_dept.pk_org as pk_org " +
				  " FROM er_indauthorize " +
				  " join org_dept on er_indauthorize.pk_user=org_dept.pk_dept " +
				  " WHERE keyword='pk_deptdoc' "+str+" and nvl(er_indauthorize.dr,0)=0";
			ArrayList<IndAuthorizeVO> list2 = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
			if (list2 != null && list2.size() >0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new IndAuthorizeVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas);
				for(int i=0; i<maps.length; i++){
					maps[i].put("pk_deptdoc", maps[i].get("pk_user"));
				}
				billVO.setTableVO("er_indauthorize2", maps); 
			} 
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}else if(null!=action && action.contains("QUERY")){
			// 业务员 
			if (action.endsWith("er_indauthorize")) {
				sql = " SELECT er_indauthorize.*, bd_psnjob.pk_dept as pk_billtypeid " +
					  " FROM er_indauthorize " +
					  " join bd_psnjob on bd_psnjob.pk_psndoc=er_indauthorize.pk_user and enddutydate is null and ismainjob='Y' " +
					  " WHERE keyword='busiuser' "+str+" and nvl(er_indauthorize.dr,0)=0";
				ArrayList<IndAuthorizeVO> list = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
				if (list != null && list.size() > 0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new IndAuthorizeVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas);
					for(int i=0; i<maps.length; i++){
						maps[i].put("pk_deptdoc", maps[i].get("pk_billtypeid"));
					}
					billVO.setTableVO("er_indauthorize", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}
			// 部门
			if (action.endsWith("er_indauthorize2")) {
				sql = " SELECT er_indauthorize.*, org_dept.pk_org as pk_org " +
					  " FROM er_indauthorize " +
					  " join org_dept on er_indauthorize.pk_user=org_dept.pk_dept " +
					  " WHERE keyword='pk_deptdoc' "+str+" and nvl(er_indauthorize.dr,0)=0";
				ArrayList<IndAuthorizeVO> list2 = (ArrayList<IndAuthorizeVO>) dao.executeQuery(sql, new BeanListProcessor(IndAuthorizeVO.class));
				if (list2 != null && list2.size() >0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new IndAuthorizeVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas);
					for(int i=0; i<maps.length; i++){
						maps[i].put("pk_deptdoc", maps[i].get("pk_user"));
					}
					billVO.setTableVO("er_indauthorize2", maps); 
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}
		}
		return null;
	}
}
