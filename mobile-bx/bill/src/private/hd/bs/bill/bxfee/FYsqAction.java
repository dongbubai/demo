package hd.bs.bill.bxfee;
 
import java.util.ArrayList; 
import java.util.Collection; 
import java.util.Date;
import java.util.HashMap;  

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator; 
import nc.itf.uap.pf.IPFBusiAction; 
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;  
import nc.vo.erm.matterapp.AggMatterAppVO;
import nc.vo.erm.matterapp.MatterAppVO;
import nc.vo.erm.matterapp.MtAppDetailVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;  
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;  
import hd.bs.muap.approve.ApproveWorkAction; 
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;  
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO; 
import hd.vo.muap.pub.FormulaVO;
import hd.vo.muap.pub.QueryBillVO;

/**
 * 费用申请单 
 */
public class FYsqAction extends ApproveWorkAction{
	
	//表体页签编码
	String bodytabcode = "mtapp_detail";
	
	//费用申请单单据类型
	String pk_billtype = "261X";
	 
	static BaseDAO dao = new BaseDAO(); 
	IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
	
	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException { 
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] mtapp_details = bodys.get("mtapp_detail");
		String[] formula_str = new String[10];
		
		/**
		 * 1. 财务组织编辑后事件，财务组织赋值给申请单位，申请部门和费用承担部门清空
		 */
		if(aevo.getKey().equals("pk_org")){
			if(null!=head.get("pk_org") && !"".equals(head.get("pk_org"))){ 
				formula_str[0] = "apply_org->" + head.get("pk_org") + ";"; 
				formula_str[1] = "apply_dept->" + null + ";";
				formula_str[2] = "assume_dept->" + null + ";";
			}  
			FormulaVO formulaVO = new FormulaVO();
			formulaVO.setFormulas(formula_str);
			return formulaVO;
		}
		
		return null;
	}

	/**
	 * 保存-新增保存和修改保存
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] detailMap = bodys.get(bodytabcode);//费用申请单明细
		AggMatterAppVO aggvo = new AggMatterAppVO();// 集合VO
		MatterAppVO hvo = new MatterAppVO();// 费用申请单表头

		MtAppDetailVO[] bvos = null;// 数据库费用明细VO
		AggMatterAppVO cloneagg = new AggMatterAppVO();// 修改时传入的聚合VO封装VO
		// 原始的主表total数据
		String pk_mtapp_bill = "";// 表头主键
		String pk_org = "";
		
		/**
		 * 根据主表PK判断是保存还是修改
		 */
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			pk_mtapp_bill = head.get("PK").toString();
			Collection<AggMatterAppVO> generalVOC = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(AggMatterAppVO.class, " dr =0 and pk_mtapp_bill='" + pk_mtapp_bill + "' ", false);
			if (null == generalVOC || generalVOC.size() == 0) {// 修改完又删除了的情况
				throw new BusinessException("修改的订单已经被删除!");
			}
			// 数据库查询聚合VO
			AggMatterAppVO transVOs = generalVOC.toArray(new AggMatterAppVO[] {})[0];
			// 主表vo
			hvo = (MatterAppVO) transVOs.getParentVO();
			bvos = transVOs.getChildrenVO();
			hvo.setStatus(1);// 状态1是修改 
			hvo.setModifier(userid);

			MtAppDetailVO[] bclone = new MtAppDetailVO[bvos.length];
			for (int b = 0; b < bvos.length; b++) {// 将传进来的交通，其他费用遍历赋给克隆对象
				bclone[b] = new MtAppDetailVO();
				bclone[b] = (MtAppDetailVO) bvos[b].clone();
			}
			cloneagg.setParentVO((CircularlyAccessibleValueObject) hvo.clone());
			cloneagg.setChildrenVO(bclone);
		} else {
			hvo.setStatus(2);// 状态2是保存 
		}
		
		/**
		 * 设置所有的字段
		 */
		hvo.setPk_billtype(pk_billtype);
		hvo.setCreator(userid);
		
		if (null != head.get("pk_group") && !"".equals(head.get("pk_group"))) {
			hvo.setPk_group(head.get("pk_group").toString());
		}else{
			throw new BusinessException("集团不能空!");
		}
		
		if (null != head.get("pk_org") && !"".equals(head.get("pk_org"))) {
			pk_org = head.get("pk_org").toString();
			hvo.setPk_org(pk_org);
		}else{
			throw new BusinessException("组织不能空!");
		}
		String sql = "select * from org_orgs where pk_org ='"+pk_org+"' and isnull(dr,0)=0;";
		 
		ArrayList<OrgVO> busitypevos = (ArrayList<OrgVO>) dao.executeQuery(sql, new BeanListProcessor(OrgVO.class));
		if (busitypevos.size() > 0) {
			hvo.setPk_org_v(busitypevos.get(0).getPk_vid());
		}
		
		// 交易类型
		if (null != head.get("pk_tradetypeid") && !"".equals(head.get("pk_tradetypeid"))) {
			hvo.setPk_tradetypeid(head.get("pk_tradetypeid").toString());
			// 交易类型编码
			sql = "select * from bd_billtype where pk_billtypeid = '"+head.get("pk_tradetypeid").toString()+"' and isnull(dr,0)=0;";
			ArrayList<BilltypeVO> billlist = (ArrayList<BilltypeVO>) dao.executeQuery(sql, new BeanListProcessor(BilltypeVO.class));
			if(billlist.size() > 0){
				hvo.setPk_tradetype(billlist.get(0).pk_billtypecode);
			}
		}else{
			throw new BusinessException("交易类型pk_tradetypeid不能空!");
		}
		 
		// 事由
		if (null != head.get("reason") && !"".equals(head.get("reason"))) {
			hvo.setReason(head.get("reason").toString());
		}
		 

		//申请单位  apply_dept
		String apply_org  = "";
		if (null != head.get("apply_org") && !"".equals(head.get("apply_org"))) {
			apply_org  = head.get("apply_org").toString();
			hvo.setApply_org (apply_org);
		}else{
			throw new BusinessException("申请单位不能空!");
		}

		//单据日期
		if(null == head.get("PK") || "".equals(head.get("PK"))){
			hvo.setBilldate(new UFDate(new Date()));
		}
		//申请部门
		String apply_dept = null;
		if (null != head.get("apply_dept")) {
			apply_dept = head.get("apply_dept").toString();
			hvo.setApply_dept(apply_dept);
			hvo.setAssume_dept(apply_dept);
		} else {
			throw new BusinessException("申请部门不能为空!");
		}
		//申请人
		String billmaker = "";
		if (null != head.get("billmaker") && !"".equals(head.get("billmaker"))) {
			billmaker = head.get("billmaker").toString();
			hvo.setBillmaker(billmaker);
		} else {
			throw new BusinessException("申请人不能空!");
		}

		//本币汇率
		if (null != head.get("org_currinfo") && !"".equals(head.get("org_currinfo"))) {
			hvo.setOrg_currinfo(new UFDouble(head.get("org_currinfo").toString()));
		}

		//执行数
		if (null != head.get("exe_amount") && !"".equals(head.get("exe_amount"))) {
			hvo.setExe_amount(new UFDouble(head.get("exe_amount").toString()));
		}
		//关闭状态
		hvo.setClose_status(2);
		// 单据状态
		hvo.setBillstatus(1);
		// 审批状态
		hvo.setApprstatus(-1);
		hvo.setEffectstatus(0);
		
		hvo.setHasntbcheck(new UFBoolean(false));
		hvo.setIs_adjust(new UFBoolean(true));
		hvo.setIscostshare(new UFBoolean(false));
		hvo.setIsexpedited(new UFBoolean(false));
		hvo.setIsignoreatpcheck(new UFBoolean(false));
		hvo.setIsneedimag(new UFBoolean(false));
		hvo.setGlobal_amount(new UFDouble(0));
		hvo.setGlobal_currinfo(new UFDouble(0));
		hvo.setGlobal_exe_amount(new UFDouble(0));
		hvo.setGlobal_rest_amount(new UFDouble(0));
		hvo.setGroup_amount(new UFDouble(0));
		hvo.setGroup_currinfo(new UFDouble(0));
		hvo.setGroup_exe_amount(new UFDouble(0));
		hvo.setGroup_rest_amount(new UFDouble(0));

		//币种  pk_currtype
		if (null != head.get("pk_currtype")) {
			hvo.setPk_currtype(head.get("pk_currtype").toString());
		}

		if(null != head.get("defitem1") && !"".equals(head.get("defitem1"))){
			hvo.setDefitem1(head.get("defitem1").toString());
		}
		if(null != head.get("defitem2") && !"".equals(head.get("defitem2"))){
			hvo.setDefitem2(head.get("defitem2").toString());
		}
		if(null != head.get("defitem3") && !"".equals(head.get("defitem3"))){
			hvo.setDefitem3(head.get("defitem3").toString());
		}
		if(null != head.get("defitem4") && !"".equals(head.get("defitem4"))){
			hvo.setDefitem4(head.get("defitem4").toString());
		}
		if(null != head.get("defitem5") && !"".equals(head.get("defitem5"))){
			hvo.setDefitem5(head.get("defitem5").toString());
		}
		if(null != head.get("defitem6") && !"".equals(head.get("defitem6"))){
			hvo.setDefitem6(head.get("defitem6").toString());
		}
		if(null != head.get("defitem7") && !"".equals(head.get("defitem7"))){
			hvo.setDefitem7(head.get("defitem7").toString());
		}
		if(null != head.get("defitem8") && !"".equals(head.get("defitem8"))){
			hvo.setDefitem8(head.get("defitem8").toString());
		}
		if(null != head.get("defitem9") && !"".equals(head.get("defitem9"))){
			hvo.setDefitem9(head.get("defitem9").toString());
		}
		if(null != head.get("defitem10") && !"".equals(head.get("defitem10"))){
			hvo.setDefitem10(head.get("defitem10").toString());
		}
		if(null != head.get("defitem11") && !"".equals(head.get("defitem11"))){
			hvo.setDefitem11(head.get("defitem11").toString());
		}
		if(null != head.get("defitem12") && !"".equals(head.get("defitem12"))){
			hvo.setDefitem12(head.get("defitem12").toString());
		}
		if(null != head.get("defitem13") && !"".equals(head.get("defitem13"))){
			hvo.setDefitem13(head.get("defitem13").toString());
		}
		if(null != head.get("defitem14") && !"".equals(head.get("defitem14"))){
			hvo.setDefitem14(head.get("defitem14").toString());
		}
		if(null != head.get("defitem15") && !"".equals(head.get("defitem15"))){
			hvo.setDefitem15(head.get("defitem15").toString());
		}
		if(null != head.get("defitem16") && !"".equals(head.get("defitem16"))){
			hvo.setDefitem16(head.get("defitem16").toString());
		}
		if(null != head.get("defitem17") && !"".equals(head.get("defitem17"))){
			hvo.setDefitem17(head.get("defitem17").toString());
		}
		if(null != head.get("defitem18") && !"".equals(head.get("defitem18"))){
			hvo.setDefitem18(head.get("defitem18").toString());
		}
		if(null != head.get("defitem19") && !"".equals(head.get("defitem19"))){
			hvo.setDefitem19(head.get("defitem19").toString());
		} 
		if(null != head.get("defitem20") && !"".equals(head.get("defitem20"))){
			hvo.setDefitem20(head.get("defitem20").toString());
		}
		if(null != head.get("defitem21") && !"".equals(head.get("defitem21"))){
			hvo.setDefitem21(head.get("defitem21").toString());
		}
		if(null != head.get("defitem22") && !"".equals(head.get("defitem22"))){
			hvo.setDefitem22(head.get("defitem22").toString());
		}
		if(null != head.get("defitem23") && !"".equals(head.get("defitem23"))){
			hvo.setDefitem23(head.get("defitem23").toString());
		}
		if(null != head.get("defitem24") && !"".equals(head.get("defitem24"))){
			hvo.setDefitem24(head.get("defitem24").toString());
		}
		if(null != head.get("defitem25") && !"".equals(head.get("defitem25"))){
			hvo.setDefitem25(head.get("defitem25").toString());
		}
		if(null != head.get("defitem26") && !"".equals(head.get("defitem26"))){
			hvo.setDefitem26(head.get("defitem26").toString());
		}
		if(null != head.get("defitem27") && !"".equals(head.get("defitem27"))){
			hvo.setDefitem27(head.get("defitem27").toString());
		}
		if(null != head.get("defitem28") && !"".equals(head.get("defitem28"))){
			hvo.setDefitem28(head.get("defitem28").toString());
		}
		if(null != head.get("defitem29") && !"".equals(head.get("defitem29"))){
			hvo.setDefitem29(head.get("defitem29").toString());
		}
		if(null != head.get("defitem30") && !"".equals(head.get("defitem30"))){
			hvo.setDefitem30(head.get("defitem30").toString());
		}
		
		/**
		 * 费用申请单明细
		 */
		ArrayList<MtAppDetailVO> detailLists = new ArrayList<MtAppDetailVO>();
		UFDouble jm = new UFDouble();
		UFDouble htotal = new UFDouble();
		UFDouble total = new UFDouble();
		UFDouble orig_amount = new UFDouble();
		String rowno = "";
		if (null != detailMap && detailMap.length != 0) {
			for (int i = 0; i < detailMap.length; i++) {
				MtAppDetailVO detailVO = new MtAppDetailVO();
				if (null != head.get("PK") && !"".equals(head.get("PK"))) {
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "3".equals(detailMap[i].get("vostatus").toString())) {
						// 状态为删除
						if (null != hvo.getPk_mtapp_bill()) {
							String kpString = hvo.getPk_mtapp_bill();// 获取主键并赋值
							detailVO.setPk_mtapp_bill(kpString);
						}
						String bu = detailMap[i].get("pk_mtapp_detail").toString();// 获取行标签并赋值
						detailVO.setPk_mtapp_detail(bu);
						jm = new UFDouble(detailMap[i].get("orig_amount").toString());
						total = hvo.getOrig_amount().sub(jm);
						detailVO.setStatus(3);
					}
					// 新增状态
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "2".equals(detailMap[i].get("vostatus").toString())) {
						String kpString = hvo.getPk_mtapp_bill();// 获取主键并赋值
						detailVO.setPk_mtapp_bill(kpString);
						jm = new UFDouble(detailMap[i].get("orig_amount").toString());
						total = jm.add(hvo.getOrig_amount());
						detailVO.setStatus(2);
					}
					// 修改状态
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "1".equals(detailMap[i].get("vostatus").toString())) {
						// 状态为修改
						if (null != hvo.getPk_mtapp_bill()) {
							String kpString = hvo.getPk_mtapp_bill();// 获取主键并赋值
							detailVO.setPk_mtapp_bill(kpString);
						}
						String bu = detailMap[i].get("pk_mtapp_detail").toString();// 获取行标签并赋值
						// 数据库获取原来的数量
						String sqlm = "select * from er_mtapp_detail where dr = 0  and Pk_mtapp_detail = '" + bu + "' order by billdate ";
						ArrayList<MtAppDetailVO> list1 = (ArrayList<MtAppDetailVO>) dao.executeQuery(sqlm, new BeanListProcessor(MtAppDetailVO.class));
						orig_amount = list1.get(0).getOrig_amount();
						detailVO.setPk_mtapp_detail(bu);
						jm = new UFDouble(detailMap[i].get("orig_amount").toString());
						total = hvo.getOrig_amount().add(jm).sub(orig_amount);
						detailVO.setStatus(1);
					}
					hvo.setOrig_amount(total);
					hvo.setOrg_amount(total);
					hvo.setRest_amount(total);
					hvo.setMax_amount(total);
					hvo.setOrg_rest_amount(total);
				}
				rowno = (i + 1) + "";
				detailVO.setRowno(Integer.parseInt(rowno));
				//费用承担单位
				if (null != detailMap[i].get("assume_org") && !"".equals(detailMap[i].get("assume_org"))) {
					detailVO.setAssume_org(detailMap[i].get("assume_org").toString());
				}
				//费用承担部门
				if (null != detailMap[i].get("assume_dept") && !"".equals(detailMap[i].get("assume_dept"))) {
					detailVO.setAssume_dept(detailMap[i].get("assume_dept").toString());
				}
				//收支项目
				if (null != detailMap[i].get("pk_iobsclass") && !"".equals(detailMap[i].get("pk_iobsclass"))) {
					detailVO.setPk_iobsclass(detailMap[i].get("pk_iobsclass").toString());
				}
				//项目
				if (null != detailMap[i].get("pk_project") && !"".equals(detailMap[i].get("pk_project"))) {
					detailVO.setPk_project(detailMap[i].get("pk_iobsclass").toString());
				}
				//金额
				if (null != detailMap[i].get("orig_amount") && !"".equals(detailMap[i].get("orig_amount"))) {
					orig_amount = new UFDouble(detailMap[i].get("orig_amount").toString());
					htotal = orig_amount.add(htotal);
					detailVO.setOrig_amount(orig_amount);
					detailVO.setRest_amount(orig_amount);//余额
				}
				//执行数
				if (null != detailMap[i].get("exe_amount") && !"".equals(detailMap[i].get("exe_amount"))) {
					detailVO.setExe_amount(new UFDouble(detailMap[i].get("exe_amount").toString()));
				}
				//余额
				if (null != detailMap[i].get("rest_amount") && !"".equals(detailMap[i].get("rest_amount"))) {
					detailVO.setRest_amount(orig_amount);
				}
				
				detailVO.setReason(hvo.getReason());
				detailVO.setCustomer_ratio(new UFDouble(0));
				detailVO.setOrg_currinfo(hvo.getOrg_currinfo());
				detailVO.setPk_currtype(hvo.getPk_currtype());
				detailVO.setPk_group(hvo.getPk_group());
				detailVO.setPk_org(hvo.getPk_org());
				detailVO.setShare_ratio(new UFDouble(100));
				detailVO.setOrg_rest_amount(orig_amount);
				detailVO.setOrg_amount(orig_amount);
				detailVO.setMax_amount(orig_amount);
				detailVO.setPre_amount(new UFDouble(0));
				detailVO.setExe_amount(new UFDouble(0));
				detailVO.setOrg_exe_amount(new UFDouble(0));
				detailVO.setOrg_pre_amount(new UFDouble(0));
				detailVO.setGlobal_amount(new UFDouble(0));
				detailVO.setGlobal_currinfo(new UFDouble(0));
				detailVO.setGlobal_exe_amount(new UFDouble(0));
				detailVO.setGlobal_pre_amount(new UFDouble(0));
				detailVO.setGlobal_rest_amount(new UFDouble(0));
				detailVO.setGroup_amount(new UFDouble(0));
				detailVO.setGroup_currinfo(new UFDouble(0));
				detailVO.setGroup_exe_amount(new UFDouble(0));
				detailVO.setGroup_pre_amount(new UFDouble(0));
				detailVO.setGroup_rest_amount(new UFDouble(0));
				if(null == head.get("PK") || "".equals(head.get("PK"))){
					detailVO.setStatus(2);
				}
				
				
				if (null != detailMap[i].get("defitem1") && !"".equals(detailMap[i].get("defitem1"))) {
					detailVO.setDefitem1(detailMap[i].get("defitem1").toString());
				}
				if (null != detailMap[i].get("defitem2") && !"".equals(detailMap[i].get("defitem2"))) {
					detailVO.setDefitem2(detailMap[i].get("defitem2").toString());
				}
				if (null != detailMap[i].get("defitem3") && !"".equals(detailMap[i].get("defitem3"))) {
					detailVO.setDefitem3(detailMap[i].get("defitem3").toString());
				}
				if (null != detailMap[i].get("defitem4") && !"".equals(detailMap[i].get("defitem4"))) {
					detailVO.setDefitem4(detailMap[i].get("defitem4").toString());
				}
				if (null != detailMap[i].get("defitem5") && !"".equals(detailMap[i].get("defitem5"))) {
					detailVO.setDefitem5(detailMap[i].get("defitem5").toString());
				}
				if (null != detailMap[i].get("defitem6") && !"".equals(detailMap[i].get("defitem6"))) {
					detailVO.setDefitem6(detailMap[i].get("defitem6").toString());
				}
				if (null != detailMap[i].get("defitem7") && !"".equals(detailMap[i].get("defitem7"))) {
					detailVO.setDefitem7(detailMap[i].get("defitem7").toString());
				}
				if (null != detailMap[i].get("defitem8") && !"".equals(detailMap[i].get("defitem8"))) {
					detailVO.setDefitem8(detailMap[i].get("defitem8").toString());
				}
				if (null != detailMap[i].get("defitem9") && !"".equals(detailMap[i].get("defitem9"))) {
					detailVO.setDefitem9(detailMap[i].get("defitem9").toString());
				}
				if (null != detailMap[i].get("defitem10") && !"".equals(detailMap[i].get("defitem10"))) {
					detailVO.setDefitem10(detailMap[i].get("defitem10").toString());
				}
				if (null != detailMap[i].get("defitem11") && !"".equals(detailMap[i].get("defitem11"))) {
					detailVO.setDefitem11(detailMap[i].get("defitem11").toString());
				}
				if (null != detailMap[i].get("defitem12") && !"".equals(detailMap[i].get("defitem12"))) {
					detailVO.setDefitem12(detailMap[i].get("defitem12").toString());
				}
				if (null != detailMap[i].get("defitem13") && !"".equals(detailMap[i].get("defitem13"))) {
					detailVO.setDefitem13(detailMap[i].get("defitem13").toString());
				}
				if (null != detailMap[i].get("defitem14") && !"".equals(detailMap[i].get("defitem14"))) {
					detailVO.setDefitem14(detailMap[i].get("defitem14").toString());
				}
				if (null != detailMap[i].get("defitem15") && !"".equals(detailMap[i].get("defitem15"))) {
					detailVO.setDefitem15(detailMap[i].get("defitem15").toString());
				}
				if (null != detailMap[i].get("defitem16") && !"".equals(detailMap[i].get("defitem16"))) {
					detailVO.setDefitem16(detailMap[i].get("defitem16").toString());
				}
				if (null != detailMap[i].get("defitem17") && !"".equals(detailMap[i].get("defitem17"))) {
					detailVO.setDefitem17(detailMap[i].get("defitem17").toString());
				}
				if (null != detailMap[i].get("defitem18") && !"".equals(detailMap[i].get("defitem18"))) {
					detailVO.setDefitem18(detailMap[i].get("defitem18").toString());
				}
				if (null != detailMap[i].get("defitem19") && !"".equals(detailMap[i].get("defitem19"))) {
					detailVO.setDefitem19(detailMap[i].get("defitem19").toString());
				}
				if (null != detailMap[i].get("defitem20") && !"".equals(detailMap[i].get("defitem20"))) {
					detailVO.setDefitem20(detailMap[i].get("defitem20").toString());
				}
				if (null != detailMap[i].get("defitem21") && !"".equals(detailMap[i].get("defitem21"))) {
					detailVO.setDefitem21(detailMap[i].get("defitem21").toString());
				}
				if (null != detailMap[i].get("defitem22") && !"".equals(detailMap[i].get("defitem22"))) {
					detailVO.setDefitem22(detailMap[i].get("defitem22").toString());
				}
				if (null != detailMap[i].get("defitem23") && !"".equals(detailMap[i].get("defitem23"))) {
					detailVO.setDefitem23(detailMap[i].get("defitem23").toString());
				}
				if (null != detailMap[i].get("defitem24") && !"".equals(detailMap[i].get("defitem24"))) {
					detailVO.setDefitem24(detailMap[i].get("defitem24").toString());
				}
				if (null != detailMap[i].get("defitem25") && !"".equals(detailMap[i].get("defitem25"))) {
					detailVO.setDefitem25(detailMap[i].get("defitem25").toString());
				}
				if (null != detailMap[i].get("defitem26") && !"".equals(detailMap[i].get("defitem26"))) {
					detailVO.setDefitem26(detailMap[i].get("defitem26").toString());
				}
				if (null != detailMap[i].get("defitem27") && !"".equals(detailMap[i].get("defitem27"))) {
					detailVO.setDefitem27(detailMap[i].get("defitem27").toString());
				}
				if (null != detailMap[i].get("defitem28") && !"".equals(detailMap[i].get("defitem28"))) {
					detailVO.setDefitem28(detailMap[i].get("defitem28").toString());
				}
				if (null != detailMap[i].get("defitem29") && !"".equals(detailMap[i].get("defitem29"))) {
					detailVO.setDefitem29(detailMap[i].get("defitem29").toString());
				}
				if (null != detailMap[i].get("defitem30") && !"".equals(detailMap[i].get("defitem30"))) {
					detailVO.setDefitem30(detailMap[i].get("defitem30").toString());
				}
				
				detailLists.add(detailVO);
			}
		}
		if(null == head.get("PK") || "".equals(head.get("PK"))){
			hvo.setOrig_amount(htotal);
			hvo.setRest_amount(htotal);
			hvo.setOrg_amount(htotal);
			hvo.setMax_amount(htotal);
			hvo.setOrg_rest_amount(htotal);
			hvo.setExe_amount(new UFDouble(0));
			hvo.setOrg_exe_amount(new UFDouble(0));
		}
		//xzList.addAll(detailLists);
		aggvo.setParentVO(hvo);// 将表头设置到聚合VO中
		aggvo.setChildrenVO(detailLists.toArray(new MtAppDetailVO[] {}));
        

		Object o = null;// 初始化超类
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		// 更新还是增加进行判断
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			// 更新
			HashMap mapall = new HashMap(2);
			mapall.put("nc.bs.scmpub.pf.ORIGIN_VO_PARAMETER", new AggMatterAppVO[] { cloneagg });
			mapall.put("notechecked", "notechecked");
			try {
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					nc.pubitf.erm.matterapp.IErmMatterAppBillManage iIplatFormEntry = (nc.pubitf.erm.matterapp.IErmMatterAppBillManage) NCLocator.getInstance().lookup(nc.pubitf.erm.matterapp.IErmMatterAppBillManage.class.getName());
					o = iIplatFormEntry.updateVO(aggvo);
				} else {
					nc.pubitf.erm.matterapp.IErmMatterAppBillManage iIplatFormEntry = (nc.pubitf.erm.matterapp.IErmMatterAppBillManage) NCLocator.getInstance().lookup(nc.pubitf.erm.matterapp.IErmMatterAppBillManage.class.getName());
					o = iIplatFormEntry.updateVO(aggvo);
				}
			} catch (Exception e) {
				if (e.getMessage().contains("是否继续")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		} else {
			try {
				// 新增
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					nc.pubitf.erm.matterapp.IErmMatterAppBillManage iIplatFormEntry = (nc.pubitf.erm.matterapp.IErmMatterAppBillManage) NCLocator.getInstance().lookup(nc.pubitf.erm.matterapp.IErmMatterAppBillManage.class.getName());
					o = iIplatFormEntry.insertVO(aggvo);
				} else {
					nc.pubitf.erm.matterapp.IErmMatterAppBillManage iIplatFormEntry = (nc.pubitf.erm.matterapp.IErmMatterAppBillManage) NCLocator.getInstance().lookup(nc.pubitf.erm.matterapp.IErmMatterAppBillManage.class.getName());
					o = iIplatFormEntry.insertVO(aggvo);
				}
			} catch (Exception e) {
				if (e.getMessage().contains("是否继续")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		}

		AggMatterAppVO returnVO = (AggMatterAppVO) o;
		if (o != null) {
			MatterAppVO returnHVO = (MatterAppVO) returnVO.getParentVO();
			return queryBillVoByPK(userid, returnHVO.getPk_mtapp_bill());
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
		if(null!=head_querybillVO){
			BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
			billVO.setHeadVO(head_BillVO.getHeadVO());
			// 查询 表体
			QueryBillVO body_querybillVO = (QueryBillVO) queryPage_body(userid, condAggVO_body,1,1);
			if(null!=body_querybillVO){
				BillVO body_BillVO = body_querybillVO.getQueryVOs()[0];
				billVO.setTableVO(bodytabcode, body_BillVO.getTableVO(bodytabcode));
			}
		}
		return billVO;
	}

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	/**
	 * 无分页查询子表
	 */
	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		return null;
	}

	/**
	 * 分页查询主表
	 */
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String billtype = super.getBilltype();
		/**
		 * 交易类型条件
		 */
		String billtype_code = "";
		String sql = "";
		billtype_code = new FeeBaseAction().getbilltypeCode(billtype);
		if (billtype_code.length() > 1) {
			billtype_code = " and er_mtapp_bill.pk_tradetype ='" + billtype_code + "' ";
		}
		
		/**
		 * 单据查询
		 */
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","er_mtapp_bill.pk_mtapp_bill");
		map.put("PK","er_mtapp_bill.pk_mtapp_bill");
		map.put("mtapp_bill","er_mtapp_bill");
		map.put("mtapp_detail","er_mtapp_detail");
		map.put("pk_org","er_mtapp_bill.pk_org");
		StringBuffer str = reCondition(obj, true,map);
		
		// 查询sql语句并进行分页
		sql = "select *\n" +
			"  from (select rownum rowno, a.*\n" + 
			"          from (select distinct er_mtapp_bill.*\n" + 
			"                  from er_mtapp_bill\n" + 
			"                  left join er_mtapp_detail on er_mtapp_detail.pk_mtapp_bill=er_mtapp_bill.pk_mtapp_bill and isnull(er_mtapp_detail.dr,0)=0\n" + 
			"                 where isnull(er_mtapp_bill.dr, 0) = 0\n" + 
			"                   and creator = '"+userid+"'\n" + 
			"				  "+str+"\n" + 
			" 				  "+billtype_code+" \n" + 
			"                 order by er_mtapp_bill.billdate desc) a)\n" + 
			" where rowno between '"+startnum+"' and '"+endnum+"'";
		ArrayList<MatterAppVO> list = (ArrayList<MatterAppVO>) dao.executeQuery(sql, new BeanListProcessor(MatterAppVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new MatterAppVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		String pk_tradetype = "";
		for (int i = 0; i < maps.length; i++) {
			if(null!=maps[i].get("pk_tradetype")){
				pk_tradetype =" '"+maps[i].get("pk_tradetype")+"' ";
			}
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			// 单据状态处理
			// djzt单据状态 0=暂存，1=保存，2=审核，3=签字，-1=作废，
			if (null == maps[i].get("apprstatus") || "".equals(maps[i].get("apprstatus")) || -1 == Integer.parseInt(maps[i].get("apprstatus").toString())) {
				maps[i].put("ibillstatus", -1);// 自由
			} else if (null != maps[i].get("apprstatus") && 3 == Integer.parseInt(maps[i].get("apprstatus").toString())) {
				maps[i].put("ibillstatus", 3);// 提交
			} else {
				maps[i].put("ibillstatus", 1);// 审批通过
			}
			maps[i].put("billtype", 30);
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		//翻译 审批表头公式 
		new FeeBaseAction().excuteListShowFormulas(pk_tradetype, billVOs);
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	/**
	 * 分页查询子表
	 */
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_mtapp_bill");
		StringBuffer str = reCondition(obj, true,map);
		
		String sql = "select * from er_mtapp_detail where isnull(dr,0) = 0 " + str;
		ArrayList<MtAppDetailVO> list = (ArrayList<MtAppDetailVO>) dao.executeQuery(sql, new BeanListProcessor(MtAppDetailVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}

		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new MtAppDetailVO[0]));
		String[] formulas = new String[] { "pk_project_name->getcolvalue(bd_project,project_name,pk_project,pk_project)" };
		PubTools.execFormulaWithVOs(maps, formulas);

		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO(bodytabcode, maps);// 物料
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO; 
	}

	/**
	 * 删除
	 */
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_mtapp_bill");
		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, csaleorderid, false);
		Object o = pf.processAction("DELETE", pk_billtype, null, aggvos, new AggMatterAppVO[] { aggvos }, null);
		return null;
	}

	
	/**
	 * 提交
	 */
	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_mtapp_bill");
		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, csaleorderid, false);
		Object o = pf.processAction("SAVE", pk_billtype, null, aggvos, aggvos, null);
		return queryBillVoByPK(userid, csaleorderid);
	}

	/**
	 * 收回
	 */
	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_mtapp_bill");
		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, csaleorderid, false);
		Object o = pf.processAction("UNSAVE", pk_billtype, null, aggvos, new AggMatterAppVO[] { aggvos }, null);
		return queryBillVoByPK(userid, csaleorderid);
	} 
	

	/*@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO appInfo = (ApproveInfoVO) obj;
		if (null != appInfo.getBillid() && !"".equals(appInfo.getBillid())) {
			String csaleorderid = appInfo.getBillid();
			AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("APPROVE", "261X", null, aggvos, new AggMatterAppVO[] { aggvos }, null);
			return queryBillVoByPK(userid, csaleorderid);
		}
		return null;
	}*/

//	@Override
//	public Object unapprove(String userid, Object obj) throws BusinessException {
//		BillVO bill = (BillVO) obj;
//		String csaleorderid = (String) bill.getHeadVO().get("pk_mtapp_bill");
//		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, csaleorderid, false);
//		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
//		Object o = pf.processAction("UNAPPROVE", "261X", null, aggvos, new AggMatterAppVO[] { aggvos }, null);
//		return queryBillVoByPK(userid, csaleorderid);
//		
//	} 
}
