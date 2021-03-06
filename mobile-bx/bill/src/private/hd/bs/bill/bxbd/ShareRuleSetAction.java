package hd.bs.bill.bxbd;

import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.bill.bxbd.ShareruleDefVO;
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.FormulaVO;
import hd.vo.muap.pub.QueryBillVO; 
import java.util.ArrayList;
import java.util.HashMap; 
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator; 
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pubitf.erm.sharerule.IErShareruleManage; 
import nc.vo.erm.sharerule.AggshareruleVO;
import nc.vo.erm.sharerule.ShareruleDataVO;
import nc.vo.erm.sharerule.ShareruleObjVO;
import nc.vo.erm.sharerule.ShareruleVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
/**
 * 11. MUJ00781 分摊规则设置-集团
 * 12. MUJ00782 分摊规则设置-组织
 */
public class ShareRuleSetAction extends ApproveWorkAction{

	//分摊结转单据对应设置-集团  固定功能注册编码
	String funnode = "MUJ00781";
		
	BaseDAO dao = new BaseDAO();
	IErShareruleManage abm = NCLocator.getInstance().lookup(IErShareruleManage.class);
	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException { 
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();//集合VO ，bill
		HashMap<String, Object> head = bill.getHeadVO();//表头
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();//表体
		HashMap<String, Object>[] rdatas = bodys.get("er_sruledata");//其他费用
		String[] formula_str = new String[50];
		if(aevo.getKey().equals("fieldname")){
			if(null!=head.get("pk_sharerule") && !"".equals(head.get("pk_sharerule"))){
				String sql = "select * from er_sruleobj where pk_sharerule='"+head.get("pk_sharerule")+"';";
				ArrayList<ShareruleObjVO> list1 = (ArrayList<ShareruleObjVO>) dao.executeQuery(sql, new BeanListProcessor(ShareruleObjVO.class));
				if(null!=list1 && list1.size()>0){
					/**
					 * 修改时，主表的分摊对象不能修改
					 */
					formula_str[0] = "fieldcode->"+list1.get(0).getFieldcode()+";";
					formula_str[1] = "fieldname->"+list1.get(0).getFieldname()+";";
					FormulaVO formulaVO = new FormulaVO();
					formulaVO.setFormulas(formula_str);
					return formulaVO;
				}
			}
		}
		return null;
	}
	/**
	 * 保存-新增保存和修改保存
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		String billtype = super.getBilltype();
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] sdataMap = bodys.get("er_sruledata");//分摊数据
		
		AggshareruleVO avo = new AggshareruleVO();
		//表头
		ShareruleVO hvo = new ShareruleVO();
		if(null==head.get("rule_type") || "".equals(head.get("rule_type"))){
			throw new BusinessException("分摊方式不能空");
		}else{
			hvo.setRule_type(Integer.parseInt(head.get("rule_type").toString()));
		}
		
		if(null==head.get("rule_code") || "".equals(head.get("rule_code"))){
			throw new BusinessException("编码不能空");
		}else{
			hvo.setRule_code(head.get("rule_code").toString());
		}
		
		if(null==head.get("rule_name") || "".equals(head.get("rule_name"))){
			throw new BusinessException("名称不能空");
		}else{
			hvo.setRule_name(head.get("rule_name").toString());
		}
		
		if(null==head.get("fieldcode") || "".equals(head.get("fieldcode"))){
			throw new BusinessException("分摊对象不能空");
		}else{
			hvo.setRuleobj_name(head.get("fieldcode").toString());
		}
		
		if(null==head.get("pk_group") || "".equals(head.get("pk_group"))){
			throw new BusinessException("集团不能空");
		}else{
			hvo.setPk_group(head.get("pk_group").toString());
		}
		
		if(null==head.get("pk_org") || "".equals(head.get("pk_org"))){
			throw new BusinessException("组织不能空");
		}else{
			hvo.setPk_org(head.get("pk_org").toString());
		}
		if(null!=billtype && funnode.equals(billtype)){
			hvo.setPk_org(InvocationInfoProxy.getInstance().getGroupId());
		} 
		hvo.setDr(0);
		
		
		//表体1
		ShareruleObjVO[] ovo = new ShareruleObjVO[1];
		ovo[0] = new ShareruleObjVO();
		ovo[0].setDr(0);
		if(null==head.get("fieldcode") || "".equals(head.get("fieldcode"))){
			throw new BusinessException("分摊对象不能空");
		}else{
			ovo[0].setFieldcode(head.get("fieldcode").toString());
		}
		if(null==head.get("fieldname") || "".equals(head.get("fieldname"))){
			throw new BusinessException("分摊对象不能空");
		}else{
			ovo[0].setFieldname(head.get("fieldname").toString());
		}
		
		
		if(null!=head.get("vostatus") && head.get("vostatus").equals(1)){
			//更新
			hvo.setStatus(1);
			ovo[0].setStatus(1);
			if(null==head.get("pk_sharerule") || "".equals(head.get("pk_sharerule"))){
				throw new BusinessException("主键不存在,修改失败");
			}else{
				hvo.setPk_sharerule(head.get("pk_sharerule").toString());
				ovo[0].setPk_sharerule(head.get("pk_sharerule").toString());
			}
			if(null==head.get("pk_sruleobj") || "".equals(head.get("pk_sruleobj"))){
				throw new BusinessException("分摊主键不存在,修改失败");
			}else{
				ovo[0].setPk_sruleobj(head.get("pk_sruleobj").toString());
			}
		}
		if(null!=head.get("vostatus") && head.get("vostatus").equals(2)){
			//新增
			hvo.setStatus(2);
			ovo[0].setStatus(2);
		}
		avo.setParentVO(hvo);
		avo.setTableVO("rule_obj", ovo);
		
		
		//表体2
		HashMap smap = new HashMap();
		ShareruleDataVO[] dvos = new ShareruleDataVO[sdataMap.length];
		for(int i=0; i<sdataMap.length; i++){
			dvos[i] = new ShareruleDataVO();
			if(smap.containsKey(sdataMap[i].get("defitem1"))){
				throw new BusinessException("分摊数据对象存在重复行，请修改！");
			}else{
				smap.put(sdataMap[i].get("defitem1"), sdataMap[i].get("defitem1"));
			}
			
			if(null!=sdataMap[i] && null!=sdataMap[i].get("defitem1")){
				dvos[i].setAttributeValue(head.get("fieldcode").toString(), sdataMap[i].get("defitem1"));
//					dvos[i].setHbbm(sdataMap[i].get("defitem1").toString());
				dvos[i].setDefitem1(sdataMap[i].get("defitem1").toString());
			}
			if(null!=sdataMap[i] && null!=sdataMap[i].get("pk_cshare_detail")){
				dvos[i].setPk_cshare_detail(sdataMap[i].get("pk_cshare_detail").toString());
			}
			if(null!=head.get("pk_sharerule") && null!=head.get("pk_sharerule")){
				dvos[i].setPk_sharerule(head.get("pk_sharerule").toString());
			}
			dvos[i].setDr(0);
			if(null!=sdataMap[i].get("vostatus") && sdataMap[i].get("vostatus").equals(1)){
				//更新 pk_cshare_detail
				dvos[i].setStatus(1);
			}
			if(null!=sdataMap[i].get("vostatus") && sdataMap[i].get("vostatus").equals(2)){
				//新增
				dvos[i].setStatus(2);
			}
			if(null!=sdataMap[i].get("vostatus") && sdataMap[i].get("vostatus").equals(3)){
				//删除 pk_cshare_detail
				dvos[i].setStatus(3);
			}
			/**
			 * 按平均分摊
			 */
			if(null!=head.get("rule_type") && (head.get("rule_type").equals("1")||head.get("rule_type").equals(1))){
				dvos[i].setShare_ratio(null);
				dvos[i].setAssume_amount(null);
			}
			/**
			 * 按比例分摊
			 */
			if(null!=head.get("rule_type") && (head.get("rule_type").equals("2")||head.get("rule_type").equals(2))){
				if(null!=sdataMap[i] && null!=sdataMap[i].get("share_ratio")){
					dvos[i].setShare_ratio(new UFDouble(sdataMap[i].get("share_ratio").toString()));
				}else{
					throw new BusinessException("分摊方式:按比例分摊,表体分摊比例不能空!");
				}
			}
			/**
			 * 按金额分摊
			 */
			if(null!=head.get("rule_type") && (head.get("rule_type").equals("3")||head.get("rule_type").equals(3))){
				if(null!=sdataMap[i] && null!=sdataMap[i].get("assume_amount")){
					dvos[i].setAssume_amount(new UFDouble(sdataMap[i].get("assume_amount").toString()));
				}else{
					throw new BusinessException("分摊方式:按金额分摊,表体承担金额不能空!");
				}
			}
		}
		avo.setTableVO("rule_data", dvos);
		avo.setIsBillLock(true);
		avo.setSendMessage(false);
		 
		AggshareruleVO ravo = new AggshareruleVO();
		if(null!=head.get("vostatus") && head.get("vostatus").equals(1)){
			//更新接口
			ravo = abm.updateVO(avo);
		}
		if(null!=head.get("vostatus") && head.get("vostatus").equals(2)){
			//新增接口
			ravo = abm.insertVO(avo);
		}
		if(null!=ravo && null!=ravo.getParentVO().getPrimaryKey() && !"".equals(ravo.getParentVO().getPrimaryKey() )){
			return queryBillVoByPK(userid,ravo.getParentVO().getPrimaryKey() );
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
		
		// 查询 表体
		QueryBillVO body_querybillVO = (QueryBillVO) queryPage_body(userid, condAggVO_body,1,1);
		// 查询 表体  
		if(null!=body_querybillVO && null!=body_querybillVO.getQueryVOs()){
			billVO = body_querybillVO.getQueryVOs()[0];
		}
		if(null!=head_querybillVO && null!=head_querybillVO.getQueryVOs()){
			BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
			billVO.setHeadVO(head_BillVO.getHeadVO());
		}
		return billVO;
	}
	
	String pk_org = "";
	/**
	 * 界面显示只有主表 
	 */
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","er_sharerule.pk_sharerule");
		map.put("PK","er_sharerule.pk_sharerule");
		StringBuffer str = reCondition(obj, true,map);
		
		String billtype = super.getBilltype();
		
		if(null!=str){
			pk_org = str.toString();
		} 
		if(null!=billtype && billtype.equals(funnode) && null!=str && !str.toString().contains("pk_sharerule")){
			//MUJ00781固定【分摊规则设置-集团】节点
			pk_org = " and pk_org='"+InvocationInfoProxy.getInstance().getGroupId()+"' ";
		}
		String sql ="select er_sharerule.*, fieldname as ruleobj_name,fieldcode as rule_name2,pk_sruleobj as rule_name3 \n" +
					"from er_sharerule\n" + 
					"join er_sruleobj on er_sruleobj.pk_sharerule=er_sharerule.pk_sharerule and nvl(er_sruleobj.dr,0)=0\n" + 
					"where nvl(er_sharerule.dr,0)=0" +
					" "+pk_org+"";

		ArrayList<ShareruleDefVO> list = (ArrayList<ShareruleDefVO>) dao.executeQuery(sql, new BeanListProcessor(ShareruleDefVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new ShareruleDefVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			PubTools.execFormulaWithVOs(maps, formulas);
			maps[i].put("ibillstatus", -1);// 自由
			maps[i].put("fieldname", headVO.get("ruleobj_name")); 
			maps[i].put("fieldcode", headVO.get("rule_name2")); 
			maps[i].put("pk_sruleobj", headVO.get("rule_name3")); 
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}
	
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_sharerule");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
	 
		String groupid = InvocationInfoProxy.getInstance().getGroupId();   
		String sql = "SELECT * FROM er_sruledata WHERE nvl(dr,0)=0 "+str+" ";
		ArrayList<ShareruleDataVO> list2 = (ArrayList<ShareruleDataVO>) dao.executeQuery(sql, new BeanListProcessor(ShareruleDataVO.class));
		if (list2 != null && list2.size() >0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new ShareruleDataVO[0])); 
			String[] formulas = new String[] { "vostatus->1;" };
			PubTools.execFormulaWithVOs(maps, formulas);
			billVO.setTableVO("er_sruledata", maps); 
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
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] sdataMap = bodys.get("er_sruledata");//分摊数据
		
		AggshareruleVO avo = new AggshareruleVO();
		//表头
		ShareruleVO hvo = new ShareruleVO();
		if(null==head.get("pk_group") || "".equals(head.get("pk_group"))){
			throw new BusinessException("集团不能空");
		} 
		if(null==head.get("pk_org") || "".equals(head.get("pk_org"))){
			throw new BusinessException("组织不能空");
		} 
		if(null==head.get("pk_sharerule") || "".equals(head.get("pk_sharerule"))){
			throw new BusinessException("主键不存在,删除失败");
		}else{
			hvo.setPk_sharerule(head.get("pk_sharerule").toString());
			hvo.setPk_group(head.get("pk_group").toString());
			hvo.setPk_org(head.get("pk_org").toString());
		}
		hvo.setDr(0);
		hvo.setStatus(3);
		avo.setParentVO(hvo);
		
		//表体1
		String sql = "select * from er_sruleobj where pk_sharerule='"+head.get("pk_sharerule")+"'";
		ArrayList<ShareruleObjVO> list1 = (ArrayList<ShareruleObjVO>) dao.executeQuery(sql, new BeanListProcessor(ShareruleObjVO.class));
		ShareruleObjVO[] ovo =list1.toArray(new ShareruleObjVO[0]);
		avo.setTableVO("rule_obj", ovo);
		
		//表体2
		sql = "select * from er_sruleobj where pk_sharerule='"+head.get("pk_sharerule")+"'";
		ArrayList<ShareruleDataVO> list2 = (ArrayList<ShareruleDataVO>) dao.executeQuery(sql, new BeanListProcessor(ShareruleDataVO.class));
		ShareruleDataVO[] dvos =list2.toArray(new ShareruleDataVO[0]);
		avo.setTableVO("rule_data", dvos);
		avo.setIsBillLock(true);
		avo.setSendMessage(false);
		 
		abm.deleteVO(avo);
		return null;
	}
}
 