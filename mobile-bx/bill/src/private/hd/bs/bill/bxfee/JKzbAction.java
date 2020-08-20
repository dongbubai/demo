package hd.bs.bill.bxfee;

import hd.bs.muap.approve.ApproveWorkAction;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.FormulaVO;
import hd.vo.muap.pub.QueryBillVO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.pf.IPFBusiAction;
import nc.itf.uap.pf.IplatFormEntry;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.bd.bankdoc.BankdocVO;
import nc.vo.bd.inoutbusiclass.InoutBusiClassVO;
import nc.vo.ep.bx.BXBusItemVO;
import nc.vo.ep.bx.BxcontrastVO;
import nc.vo.ep.bx.JKBXVO;
import nc.vo.ep.bx.JKHeaderVO;
import nc.vo.ep.bx.JKVO;
import nc.vo.erm.matterapp.MatterAppVO;
import nc.vo.erm.matterapp.MtAppDetailVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

/**
 * 借款单
 */
public class JKzbAction extends ApproveWorkAction {

	static BaseDAO dao = new BaseDAO();

	// 静态方法用于收支项目筛选
	public static List<InoutBusiClassVO> getSzxmid(String szxmid) throws BusinessException {
		String sql = "select pk_inoutbusiclass, code, name, pk_parent\n" +
	            " from bd_inoutbusiclass\n" + 
				"  where nvl(dr,0)=0\n" + 
				"   and pk_inoutbusiclass  = '"+szxmid+"'\n" + 
				"   and pk_parent is not null\n" + 
				"   and pk_inoutbusiclass not in  (select  pk_parent\n" + 
				"                                    from bd_inoutbusiclass\n" + 
				"                                   where nvl(dr,0)=0\n" + 
				"                                     and pk_parent is not null)";
		ArrayList<InoutBusiClassVO> szxmidA = (ArrayList<InoutBusiClassVO>) dao.executeQuery(sql, new BeanListProcessor(InoutBusiClassVO.class));
		return szxmidA;
	}
	//东亮 2019年12月16日22:37:58
	public String zhbank(String zh) throws BusinessException {
		String sql = "select c.name from bd_bankaccsub a join bd_bankaccbas b\n" +
				"on a.pk_bankaccbas  = b.pk_bankaccbas\n" + 
				"join bd_bankdoc c on b.pk_bankdoc  = c.pk_bankdoc\n" + 
				"where   b.enablestate=2 and\n" + 
				"nvl(a.dr,0)= 0 \n" + 
				"and nvl(b.dr,0)= 0\n" + 
				"and nvl(c.dr,0)= 0\n" + 
				"and a.pk_bankaccsub  ='"+zh+"'";
		ArrayList<BankdocVO> yhList = (ArrayList<BankdocVO>) dao.executeQuery(sql, new BeanListProcessor(BankdocVO.class));
		if(null != yhList && yhList.size() > 0){
			String zhs = yhList.get(0).getName().toString();
			return zhs;
		}
		return null;
	}

	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException{
		if ("DEFAPPLY".equals(action)) {
			return new FeeBaseAction().queryPageMtapp(userid,billtype,obj);
		} 
		if(null != action && !"".equals(action)){
			if("DEFAPPLYOK".equals(action)){
				return new FeeBaseAction().getJKZBVO(obj,billtype.toString());
			}
		}
		return super.processAction(account, userid, billtype, action, obj);
	} 
	
	
	//东亮 复用
	public String[] formula(ArrayList<Object[]> cusmessage , String[] formula_str){
		if(null != cusmessage  && cusmessage.size() > 0){
			if(null != cusmessage.get(0)[4] && !"".equals(cusmessage.get(0)[4])){
				if(null != cusmessage.get(0)[0] && !"".equals(cusmessage.get(0)[0])){
					String org = cusmessage.get(0)[0].toString();
					formula_str[0] = "pk_fiorg->" + org + ";";
					formula_str[1] = "fydwbm->" + org + ";";
					formula_str[2] = "dwbm->" + org + ";"; 
					formula_str[5] = "pk_org->" + org + ";";
				}
				if(null != cusmessage.get(0)[3] && !"".equals(cusmessage.get(0)[3])){
					String dept = cusmessage.get(0)[3].toString();
					formula_str[3] = "fydeptid->" + dept + ";";
					formula_str[4] = "deptid->" + dept + ";";
				}
			}
		}else{
			formula_str[0] = "pk_fiorg->" + null + ";";
			formula_str[1] = "fydwbm->" + null + ";";
			formula_str[2] = "dwbm->" + null + ";"; 
			formula_str[5] = "pk_org->" + null + ";";
			formula_str[3] = "fydeptid->" + null + ";";
			formula_str[4] = "deptid->" + null + ";";
			formula_str[6] = "deptid->" + null + ";";
		}
		return formula_str;
	}


	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {

		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] jkbusitem = bodys.get("jk_busitem");
		HashMap<String, Object> head = bill.getHeadVO();// 表头

		String[] formula_str = new String[8];		
//		if(aevo.getKey().equals("pk_fiorg")){
//			if(null!=head.get("pk_fiorg") && !"".equals(head.get("pk_fiorg"))){
//				ArrayList<Object[]> cusmessage = (ArrayList<Object[]>) new FindBodyAction().getOrg_orgs(userid, head.get("pk_fiorg").toString());
//				formula_str = formula(cusmessage,formula_str);
//				FormulaVO formulaVO = new FormulaVO();
//				formulaVO.setFormulas(formula_str);
//				return formulaVO;
//
//			}
//		}
//
//		//表头编辑后事件
//		if(aevo.getKey().equals("hbbm")){
//			if(null!=head.get("hbbm")){
//				String sql = "SELECT DISTINCT accnum,\n" +
//						"                accname, \n" +
//						"                pk_bankaccsub \n" +
//						"  FROM (SELECT bd_bankaccsub.accnum,\n" +
//						"               bd_bankaccsub.accname,\n" +
//						"               pk_bankdoc,\n" +
//						"               pk_banktype,\n" +
//						"               pk_currtype,\n" +
//						"               bd_custbank.pk_bankaccsub AS pk_bankaccsub,\n" +
//						"               bd_custbank.pk_bankaccbas AS pk_bankaccbas,\n" +
//						"               enablestate,\n" +
//						"               pk_cust,\n" +
//						"               accountproperty,\n" +
//						"               isinneracc,\n" +
//						"               bd_custbank.accclass,\n" +
//						"               pk_custbank\n" +
//						"          FROM bd_bankaccbas, bd_bankaccsub, bd_custbank\n" +
//						"         WHERE bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n" +
//						"           AND bd_bankaccsub.pk_bankaccsub = bd_custbank.pk_bankaccsub\n" +
//						"           AND bd_bankaccsub.pk_bankaccbas = bd_custbank.pk_bankaccbas\n" +
//						"           AND bd_custbank.pk_bankaccsub != '~') bd_psnbankacctmp\n" +
//						" WHERE (pk_currtype = '1002Z0100000000001K1' AND enablestate = '2' AND accclass = '3')\n" +
//						"   AND (enablestate = 2)\n" +
//						"   AND (pk_cust = '"+head.get("hbbm").toString()+"')\n" +
//						" ORDER BY accnum";
//				ArrayList<Object[]> zylist = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
//				if(zylist.size()>0){
//					formula_str[0] = "custaccount->" + zylist.get(0)[2] + ";";
//				}
//				FormulaVO formulaVO = new FormulaVO();
//				formulaVO.setFormulas(formula_str);
//				return formulaVO;
//			}
//		}
//
//		//表头编辑后事件  单位银行账户带出单位银行开户行
//		if(aevo.getKey().equals("fkyhzh")){
//			if(null != head.get("fkyhzh")){
//				String sql = "select * from bd_bankaccbas b1 join bd_banktype b2 on b1.pk_banktype=b2.pk_banktype  where  accnum='"+head.get("fkyhzh_code")+"' ";   // +head.get("fkyhzh")+ 12001860800052514903    
//				ArrayList<BankAccbasVO> yhList = (ArrayList<BankAccbasVO>) dao.executeQuery(sql, new BeanListProcessor(BankAccbasVO.class));
//				if(yhList.size() > 0){
//					formula_str[0] = "bank->"+yhList.get(0).getAccname().toString()+";";
//
//					FormulaVO formulaVO = new FormulaVO();
//					formulaVO.setFormulas(formula_str);
//					return formulaVO;
//				}
//			}
//		}
//
//
		//东亮 。。。。2020年1月11日11:59:35 表头收款人编辑后事件
		if(aevo.getKey().equals("receiver")){
			if(null != head.get("receiver") && !"".equals(head.get("receiver")) ){
				String skyhzh = new FeeBaseAction().getSkyhzh(head.get("receiver").toString());
				if(null != skyhzh && !"".equals(skyhzh)){
					formula_str[1] = "skyhzh->"+skyhzh+";";
				}else{
					formula_str[1] = "skyhzh->"+null+";";
				}
			}
			FormulaVO formulaVO = new FormulaVO();
			formulaVO.setFormulas(formula_str);
			return formulaVO;
		}

//		/*//东亮 。。。。2020年1月11日11:59:35 表头客户编辑后事件
//		if(aevo.getKey().equals("customer")){
//			String sql = 
//
//					"select distinct bd_bankdoc.name,bd_bankaccsub.pk_bankaccsub\n" +
//							"            from bd_bankaccbas bd_bankaccbas\n" + 
//							"            join bd_bankaccsub bd_bankaccsub\n" + 
//							"              on bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n" + 
//							"            join bd_custbank bd_custbank\n" + 
//							"              on bd_bankaccsub.pk_bankaccbas = bd_custbank.pk_bankaccbas\n" + 
//							"            join bd_bankdoc bd_bankdoc\n" + 
//							"              on bd_bankdoc.pk_bankdoc = bd_bankaccbas.pk_bankdoc\n" + 
//							"              where bd_bankaccbas.enablestate = 2\n" + 
//							"              --and bd_bankaccsub.isdefault = 'Y'\n" + 
//							"              and bd_psnbankacc.pk_cust = '\"\"'";
//
//			ArrayList<Object[]> cusmessage = new ArrayList<Object[]>();
//			cusmessage = (ArrayList<Object[]>) dao.executeQuery(sql,new ArrayListProcessor());
//			if (cusmessage.size() > 0) {
//				String skyhzh  = cusmessage.get(0)[1].toString();
//				formula_str[1] = "skyhzh->"+skyhzh+";";
//				String zyx28  = cusmessage.get(0)[0].toString();
//				formula_str[0] = "zyx28->"+zyx28+";";
//			}
//			FormulaVO formulaVO = new FormulaVO();
//			formulaVO.setFormulas(formula_str);
//			return formulaVO;
//
//		}*/
//
//		//表头编辑后事件  客商银行账户
//		if(aevo.getKey().equals("custaccount")){
//			if(null != head.get("custaccount") ){
//				String zyx26  = zhbank(head.get("custaccount").toString());
//				formula_str[0] = "zyx26->"+zyx26+";";
//
//				FormulaVO formulaVO = new FormulaVO();
//				formulaVO.setFormulas(formula_str);
//				return formulaVO;
//			}
//		}
//
//		//个人银行账户
//		if(aevo.getKey().equals("skyhzh")){
//			if(null != head.get("skyhzh")){
//				String zyx28  = zhbank(head.get("skyhzh").toString());
//				formula_str[0] = "zyx28->"+zyx28+";";
//				FormulaVO formulaVO = new FormulaVO();
//				formulaVO.setFormulas(formula_str);
//				return formulaVO;
//			}
//		}
//
//		/**
//		 * dongliang 2019年3月13日20:42:42   费用申请单编辑后事件	
//		 */
//		// 借款明细 表体编辑后事件
		if (null != jkbusitem && jkbusitem.length > 0) {
			for(int j = 0;j < jkbusitem.length; j++){
				// 收支项目筛选
				if (aevo.getKey().equals("szxmid")) {
					if(null != jkbusitem[j].get("szxmid") && !"".equals(jkbusitem[j].get("szxmid"))){
						String szxmidString = jkbusitem[j].get("szxmid").toString();
						ArrayList<InoutBusiClassVO> szx = (ArrayList<InoutBusiClassVO>) JKzbAction.getSzxmid(szxmidString);
						if(null == szx || szx.size() == 0){
							throw new BusinessException("请选择末级收支项目");
						}
						FormulaVO formulaVO = new FormulaVO();
						formulaVO.setFormulas(formula_str);
						return formulaVO;
					}
				}
			}
		}
		return null;
		
//
//
//
//				if(aevo.getKey().equals("pk_item")){
//					if(null != jkbusitem[j].get("pk_item") && !"".equals(jkbusitem[j].get("pk_item"))){
//						String pk_mtapp_detail = jkbusitem[j].get("pk_item").toString();
//						String sql = "SELECT  pk_iobsclass,\n" +
//								"	er_mtapp_detail.orig_amount - isnull(p.exe_amount,0) as usable_amout\n" +
//								"FROM\n" +
//								"	er_mtapp_detail er_mtapp_detail\n" +
//								" join er_mtapp_bill a\n" +
//								" on er_mtapp_detail.pk_mtapp_bill = a.pk_mtapp_bill\n" +
//								"LEFT JOIN (\n" +
//								"	SELECT\n" +
//								"		SUM (exe_amount) AS exe_amount,\n" +
//								"		pk_mtapp_detail\n" +
//								"	FROM\n" +
//								"		er_mtapp_billpf\n" +
//								"	WHERE\n" +
//								"		pk_djdl IN (N'bx' , N'jk')\n" +
//								"	GROUP BY\n" +
//								"		pk_mtapp_detail\n" +
//								") p \n" +
//								"ON\n" +
//								" p.pk_mtapp_detail = er_mtapp_detail.pk_mtapp_detail\n" +
//								"WHERE\n" +
//								"	er_mtapp_detail.pk_mtapp_detail = '"+pk_mtapp_detail+"'";
//						ArrayList<MtAppDetailVO> detail = (ArrayList<MtAppDetailVO>) dao.executeQuery(sql, new BeanListProcessor(MtAppDetailVO.class));
//						if(null != detail && detail.size() > 0){
//							String pk_iobsclass = detail.get(0).getPk_iobsclass();
//							formula_str[0] = "szxmid->" + pk_iobsclass + ";";
//							String usable_amout = detail.get(0).getUsable_amout().toString();
//							formula_str[1] = "amount->" + usable_amout + ";";
//							FormulaVO formulaVO = new FormulaVO();
//							formulaVO.setFormulas(formula_str);
//							return formulaVO;
//						}	
//					}
//
//				}
//			}
//		}

	}
	
	static HashMap<String, String> summap = new HashMap<String, String>();
	static HashMap<String, String> invoiceDefMap = new HashMap<String, String>();
	static HashMap<String, String> billtypeMap = new HashMap<String, String>();
	static HashMap<String, String> deptMap = new HashMap<String, String>();
	static {
		try {
			// 事由
			String sql = " SELECT code, summaryname, pk_summary\n" + "  FROM fipub_summary\n" + " WHERE isnull(dr,0)=0\n";
			BaseDAO dao = new BaseDAO();
			ArrayList<Object[]> zylist;

			zylist = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
			for (int l = 0; l < zylist.size(); l++) {
				summap.put(zylist.get(l)[1].toString(), zylist.get(l)[2].toString());
			}
			// 发票情况说明
			sql = "select bd_defdoc.code,bd_defdoc.name,bd_defdoc.pk_defdoc \n" + "from bd_defdoc \n" + "join bd_defdoclist on bd_defdoc.pk_defdoclist=bd_defdoclist.pk_defdoclist\n" + " where isnull(bd_defdoc.dr,0)=0\n" + "  and isnull(bd_defdoclist.dr,0)=0\n"
					+ "  and bd_defdoclist.name like '%发票%' " + "  and bd_defdoc.code in ('01')";
			zylist = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
			for (int l = 0; l < zylist.size(); l++) {
				invoiceDefMap.put(zylist.get(l)[2].toString(), zylist.get(l)[0].toString());
			}

			// 交易类型
			sql = "SELECT pk_billtypecode,pk_billtypeid FROM bd_billtype where isnull(dr, 0)=0 and pk_billtypecode like '26%'";
			ArrayList<Object[]> billtypelist = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
			for (int i = 0; i < billtypelist.size(); i++) {
				billtypeMap.put(billtypelist.get(i)[1].toString(), billtypelist.get(i)[0].toString());
			}
			
			//部门
			sql = "select * from org_dept where isnull(dr,0)=0 and islastversion='Y'";
			ArrayList<DeptVO> deptlist = (ArrayList<DeptVO>) dao.executeQuery(sql.toString(), new BeanListProcessor(DeptVO.class));
			for (int i = 0; i < deptlist.size(); i++) {
				deptMap.put(deptlist.get(i).getPk_dept(),deptlist.get(i).getPk_vid());
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}

	String pk_dept,pk_org = null;
	String jkbxr = "";
	String receiver = "";
	/**
	 * 封装主表
	 */
	public void setHVO(JKHeaderVO hvo,HashMap<String, Object> head,String userid) throws BusinessException{
		if (null == head.get("pk_org") || "".equals(head.get("pk_org"))) {
			throw new BusinessException("组织不能空!");
		}

		if(null!=head.get("pk_tradetypeid") && !"".equals(head.get("pk_tradetypeid"))){
			String tradetypeid = head.get("pk_tradetypeid").toString();
			hvo.setPk_tradetypeid(tradetypeid);
			hvo.setDjlxbm(billtypeMap.get(tradetypeid));
			if(null==billtypeMap.get(tradetypeid) ){
				throw new BusinessException("单据类型编码不能空!");
			}
		}else{
			throw new BusinessException("交易类型不能空!");
		}
 
		if(null!=head.get("fydeptid") ){
			pk_dept = head.get("fydeptid").toString();
		}else{
			throw new BusinessException("部门不能为空!");
		}

		if (null != head.get("jkbxr") && !"".equals(head.get("jkbxr"))) {
			jkbxr = head.get("jkbxr").toString();
		} else {
			throw new BusinessException("借款报销人不能空!");
		}
		
		// 给组织赋值
		pk_org = head.get("pk_org").toString();
		hvo.setPk_org(pk_org);
		String sql = "select pk_vid from org_orgs where pk_org = '" + pk_org + "' and isnull(dr,0)=0;";
		ArrayList<OrgVO> busitypevos = (ArrayList<OrgVO>) dao.executeQuery(sql, new BeanListProcessor(OrgVO.class));
		if (busitypevos.size() > 0) {
			hvo.setPk_org_v(busitypevos.get(0).getPk_vid());
		}
		// 给集团赋值
		String pk_group = head.get("pk_group").toString();
		hvo.setPk_group(pk_group);
		// 财务组织
		hvo.setPk_fiorg(pk_org);
		// 原支付组织
		hvo.setPk_payorg(pk_org);
		hvo.setPk_payorg_v(busitypevos.get(0).getPk_vid());// 支付单位
		// 原报销人单位
		hvo.setDwbm(pk_org);
		hvo.setDwbm_v(busitypevos.get(0).getPk_vid());
		// 费用承担单位 fydwbm
		hvo.setFydwbm(pk_org);
		hvo.setFydwbm_v(busitypevos.get(0).getPk_vid());
		if(null==head.get("PK")){
			hvo.setCreationtime(new UFDateTime(new Date()));
			hvo.setDjrq(new UFDate(new Date()));
		}
		
		if (null != head.get("creator") && !"".equals(head.get("creator"))) {
			hvo.setCreator( head.get("creator").toString());// 创建人 shi 怎么来的啊
		}else{
			hvo.setCreator(userid);
		}
		
		hvo.setIsmashare(new UFBoolean(false));
		hvo.setIsneedimag(new UFBoolean(false));
		hvo.setQcbz(new UFBoolean(false));
		//		hvo.setPaytarget(0);
		// 币种
		if (null != head.get("bzbm")) {
			hvo.setBzbm(head.get("bzbm").toString());
		} 
		// 事由
		if (null != head.get("zy") && !"".equals(head.get("zy"))) {
			hvo.setZy(head.get("zy").toString());
		}
		// 单据状态
		hvo.setDjzt(1);
		// 审批状态
		hvo.setSpzt(-1);
		// 单位银行账号
		if (null != head.get("fkyhzh") && !"".equals(head.get("fkyhzh"))) {
			hvo.setFkyhzh(head.get("fkyhzh").toString());
		}
		if (null != head.get("skyhzh") && !"".equals(head.get("skyhzh"))) {
			hvo.setSkyhzh(head.get("skyhzh").toString());
		}
		 
		//供应商
		if(null != head.get("hbbm")){
			hvo.setHbbm(head.get("hbbm").toString());
		}
		//供应商银行账户
		if(null != head.get("custaccount")){
			hvo.setCustaccount(head.get("custaccount").toString());
		}
		if(null != head.get("paytarget")){
			hvo.setPaytarget(Integer.parseInt(head.get("paytarget").toString()));
		}
 
		/**
		 * 原报销人部门
		 */
		if (null != head.get("deptid") && !"".equals(head.get("deptid"))) {
			pk_dept = head.get("deptid").toString();
			hvo.setDeptid(pk_dept);
		}
 
		if (deptMap.containsKey(pk_dept)) {
			hvo.setDeptid_v(deptMap.get(pk_dept));
		}
 
		// 费用承担部门
		String fydeptid = "";
		if (null != head.get("fydeptid") && !"".equals(head.get("fydeptid"))) {
			fydeptid = head.get("fydeptid").toString();
			hvo.setFydeptid(fydeptid);
			hvo.setFydeptid_v(deptMap.get(fydeptid)); 
		} 
		if (null != head.get("cashitem") && !"".equals(head.get("cashitem"))) {
			hvo.setCashitem(head.get("cashitem").toString()); 
		}
		if (null != head.get("cashproj") && !"".equals(head.get("cashproj"))) {
			hvo.setCashproj(head.get("cashproj").toString()); 
		}
		if (null != head.get("center_dept") && !"".equals(head.get("center_dept"))) {
			hvo.setCenter_dept(head.get("center_dept").toString()); 
		}
		if (null != head.get("checktype") && !"".equals(head.get("checktype"))) {
			hvo.setChecktype(head.get("checktype").toString()); 
		}
		if (null != head.get("customer") && !"".equals(head.get("customer"))) {
			hvo.setCustomer(head.get("customer").toString()); 
		}
		if (null != head.get("fjzs") && !"".equals(head.get("fjzs"))) {
			hvo.setFjzs(Integer.parseInt(head.get("fjzs").toString()));// 附件张数
		}
		if (null != head.get("pk_item") && !"".equals(head.get("pk_item"))) {
			hvo.setPk_item( head.get("pk_item").toString()); 
		}
		if (null != head.get("szxmid") && !"".equals(head.get("szxmid"))) {
			hvo.setSzxmid( head.get("szxmid").toString()); 
		}
		if (null != head.get("reimrule") && !"".equals(head.get("reimrule"))) {
			hvo.setReimrule(head.get("reimrule").toString()); 
		}
		if (null != head.get("projecttask") && !"".equals(head.get("projecttask"))) {
			hvo.setProjecttask(head.get("projecttask").toString()); 
		}
		if (null != head.get("pk_resacostcenter") && !"".equals(head.get("pk_resacostcenter"))) {
			hvo.setPk_resacostcenter(head.get("pk_resacostcenter").toString()); 
		}
		if (null != head.get("pk_proline") && !"".equals(head.get("pk_proline"))) {
			hvo.setPk_proline(head.get("pk_proline").toString()); 
		}
		if (null != head.get("pk_matters") && !"".equals(head.get("pk_matters"))) {
			hvo.setPk_matters(head.get("pk_matters").toString()); 
		}
		if (null != head.get("tbb_period") && !"".equals(head.get("tbb_period"))) {
			hvo.setTbb_period(new UFDate(head.get("tbb_period").toString())); 
		}
		
		if (null != head.get("pk_cashaccount") && !"".equals(head.get("pk_cashaccount"))) {
			hvo.setPk_cashaccount(head.get("pk_cashaccount").toString()); 
		}
		if (null != head.get("pk_campaign") && !"".equals(head.get("pk_campaign"))) {
			hvo.setPk_campaign(head.get("pk_campaign").toString()); 
		}
		if (null != head.get("pk_brand") && !"".equals(head.get("pk_brand"))) {
			hvo.setPk_brand(head.get("pk_brand").toString()); 
		}
		if (null != head.get("jobid") && !"".equals(head.get("jobid"))) {
			hvo.setJobid(head.get("jobid").toString()); 
		}
		
		if (null != head.get("iscostshare") && !"".equals(head.get("iscostshare"))) {
			hvo.setIscostshare(new UFBoolean(head.get("iscostshare").toString())); 
		}
		if (null != head.get("isexpamt") && !"".equals(head.get("isexpamt"))) {
			hvo.setIsexpamt(new UFBoolean(head.get("isexpamt").toString())); 
		}
		hvo.setDjzt(1);// 单据状态
		hvo.setSxbz(0);// 生效状态
		if (null != head.get("qzzt") && !"".equals(head.get("qzzt"))) {
			hvo.setQzzt(Integer.parseInt(head.get("qzzt").toString()));// 清账状态
		}
		if (null != head.get("jsfs") && !"".equals(head.get("jsfs"))) {
			hvo.setJsfs( head.get("jsfs").toString());// 结算方式
		}
		if (null != head.get("jkbxr") && !"".equals(head.get("jkbxr"))) {
			hvo.setJkbxr( head.get("jkbxr").toString());// 借款人
		}
		if (null != head.get("kjnd") && !"".equals(head.get("kjnd"))) {
			hvo.setKjnd( head.get("kjnd").toString());// 会计年度
		}
		if (null != head.get("kjqj") && !"".equals(head.get("kjqj"))) {
			hvo.setKjqj( head.get("kjqj").toString());// 会计期间
		}
		if (null != head.get("djbh") && !"".equals(head.get("djbh"))) {
			hvo.setDjbh(head.get("djbh").toString());// 单据编号
		}
		hvo.setContrastenddate(new UFDate());// 冲销完成日期
		hvo.setDjdl("jk");// 单据大类 
		hvo.setSpzt(-1);// 审批状态
		
		// 报销人
		hvo.setJkbxr(jkbxr);
		hvo.setReceiver(jkbxr);
				
		hvo.setFlexible_flag(new UFBoolean(false));
		hvo.setIsinitgroup(new UFBoolean(false));
		hvo.setIsexpedited(new UFBoolean(false)); 
		hvo.setIscheck(new UFBoolean(false));
		// 生效状态
		hvo.setSxbz(0);
		hvo.setOperator(userid);
		hvo.setBbhl(new UFDouble(1)); 

		hvo.setGroupzfbbje(new UFDouble(0));
		hvo.setGrouphkbbje(new UFDouble(0));
		hvo.setGroupcjkbbje(new UFDouble(0)); 
		hvo.setGroupbbhl(new UFDouble(0));
		hvo.setGlobalzfbbje(new UFDouble(0));
		hvo.setGlobalhkbbje(new UFDouble(0));
		hvo.setGlobalcjkbbje(new UFDouble(0)); 
		hvo.setGlobalbbhl(new UFDouble(0));

		
		if (null != head.get("zyx1") && !"".equals(head.get("zyx1"))) {
			hvo.setZyx1( head.get("zyx1").toString()); 
		}
		if (null != head.get("zyx2") && !"".equals(head.get("zyx2"))) {
			hvo.setZyx2( head.get("zyx2").toString()); 
		}
		if (null != head.get("zyx3") && !"".equals(head.get("zyx3"))) {
			hvo.setZyx3( head.get("zyx3").toString()); 
		}
		if (null != head.get("zyx4") && !"".equals(head.get("zyx4"))) {
			hvo.setZyx4( head.get("zyx4").toString()); 
		}
		if (null != head.get("zyx5") && !"".equals(head.get("zyx5"))) {
			hvo.setZyx5( head.get("zyx5").toString()); 
		}
		if (null != head.get("zyx6") && !"".equals(head.get("zyx6"))) {
			hvo.setZyx6( head.get("zyx6").toString()); 
		}
		if (null != head.get("zyx7") && !"".equals(head.get("zyx7"))) {
			hvo.setZyx7( head.get("zyx7").toString()); 
		}
		if (null != head.get("zyx8") && !"".equals(head.get("zyx8"))) {
			hvo.setZyx8( head.get("zyx8").toString()); 
		}
		if (null != head.get("zyx9") && !"".equals(head.get("zyx9"))) {
			hvo.setZyx9( head.get("zyx9").toString()); 
		}
		if (null != head.get("zyx10") && !"".equals(head.get("zyx10"))) {
			hvo.setZyx10( head.get("zyx10").toString()); 
		} 
		if (null != head.get("zyx11") && !"".equals(head.get("zyx11"))) {
			hvo.setZyx11( head.get("zyx11").toString()); 
		}
		if (null != head.get("zyx12") && !"".equals(head.get("zyx12"))) {
			hvo.setZyx12( head.get("zyx12").toString()); 
		}
		if (null != head.get("zyx13") && !"".equals(head.get("zyx13"))) {
			hvo.setZyx13( head.get("zyx13").toString()); 
		}
		if (null != head.get("zyx14") && !"".equals(head.get("zyx14"))) {
			hvo.setZyx14( head.get("zyx14").toString()); 
		}
		if (null != head.get("zyx15") && !"".equals(head.get("zyx15"))) {
			hvo.setZyx15( head.get("zyx15").toString()); 
		}
		if (null != head.get("zyx16") && !"".equals(head.get("zyx16"))) {
			hvo.setZyx16( head.get("zyx16").toString()); 
		}
		if (null != head.get("zyx17") && !"".equals(head.get("zyx17"))) {
			hvo.setZyx17( head.get("zyx17").toString()); 
		}
		if (null != head.get("zyx18") && !"".equals(head.get("zyx18"))) {
			hvo.setZyx18( head.get("zyx18").toString()); 
		}
		if (null != head.get("zyx19") && !"".equals(head.get("zyx19"))) {
			hvo.setZyx19( head.get("zyx19").toString()); 
		}
		if (null != head.get("zyx20") && !"".equals(head.get("zyx20"))) {
			hvo.setZyx20( head.get("zyx20").toString()); 
		} 
		if (null != head.get("zyx21") && !"".equals(head.get("zyx21"))) {
			hvo.setZyx21( head.get("zyx21").toString()); 
		}
		if (null != head.get("zyx22") && !"".equals(head.get("zyx22"))) {
			hvo.setZyx22( head.get("zyx22").toString()); 
		}
		if (null != head.get("zyx23") && !"".equals(head.get("zyx23"))) {
			hvo.setZyx23( head.get("zyx23").toString()); 
		}
		if (null != head.get("zyx24") && !"".equals(head.get("zyx24"))) {
			hvo.setZyx24( head.get("zyx24").toString()); 
		}
		if (null != head.get("zyx25") && !"".equals(head.get("zyx25"))) {
			hvo.setZyx25( head.get("zyx25").toString()); 
		}
		if (null != head.get("zyx26") && !"".equals(head.get("zyx26"))) {
			hvo.setZyx26( head.get("zyx26").toString()); 
		}
		if (null != head.get("zyx27") && !"".equals(head.get("zyx27"))) {
			hvo.setZyx27( head.get("zyx27").toString()); 
		}
		if (null != head.get("zyx28") && !"".equals(head.get("zyx28"))) {
			hvo.setZyx28( head.get("zyx28").toString()); 
		}
		if (null != head.get("zyx29") && !"".equals(head.get("zyx29"))) {
			hvo.setZyx29( head.get("zyx29").toString()); 
		}
		if (null != head.get("zyx30") && !"".equals(head.get("zyx30"))) {
			hvo.setZyx30( head.get("zyx30").toString()); 
		} 
	}
	
	
	/**
	 * 封装busiitem子表
	 */
	public void setBusiitemBVO(JKHeaderVO hvo,HashMap<String, Object> head,BXBusItemVO busiitemBVO,HashMap<String, Object> bxbusitem){
		if(null!=bxbusitem.get("defitem1") && !"".equals(bxbusitem.get("defitem1"))){
			busiitemBVO.setDefitem1(bxbusitem.get("defitem1").toString());
		}
		if(null!=bxbusitem.get("defitem2") && !"".equals(bxbusitem.get("defitem2"))){
			busiitemBVO.setDefitem2(bxbusitem.get("defitem2").toString());
		}
		if(null!=bxbusitem.get("defitem3") && !"".equals(bxbusitem.get("defitem3"))){
			busiitemBVO.setDefitem3(bxbusitem.get("defitem3").toString());
		}
		if(null!=bxbusitem.get("defitem4") && !"".equals(bxbusitem.get("defitem4"))){
			busiitemBVO.setDefitem4(bxbusitem.get("defitem4").toString());
		}
		if(null!=bxbusitem.get("defitem5") && !"".equals(bxbusitem.get("defitem5"))){
			busiitemBVO.setDefitem5(bxbusitem.get("defitem5").toString());
		}
		if(null!=bxbusitem.get("defitem6") && !"".equals(bxbusitem.get("defitem6"))){
			busiitemBVO.setDefitem6(bxbusitem.get("defitem6").toString());
		}
		if(null!=bxbusitem.get("defitem7") && !"".equals(bxbusitem.get("defitem7"))){
			busiitemBVO.setDefitem7(bxbusitem.get("defitem7").toString());
		}
		if(null!=bxbusitem.get("defitem8") && !"".equals(bxbusitem.get("defitem8"))){
			busiitemBVO.setDefitem8(bxbusitem.get("defitem8").toString());
		}
		if(null!=bxbusitem.get("defitem9") && !"".equals(bxbusitem.get("defitem9"))){
			busiitemBVO.setDefitem9(bxbusitem.get("defitem9").toString());
		}
		if(null!=bxbusitem.get("defitem10") && !"".equals(bxbusitem.get("defitem10"))){
			busiitemBVO.setDefitem10(bxbusitem.get("defitem10").toString());
		}
		if(null!=bxbusitem.get("defitem11") && !"".equals(bxbusitem.get("defitem11"))){
			busiitemBVO.setDefitem11(bxbusitem.get("defitem11").toString());
		}
		if(null!=bxbusitem.get("defitem12") && !"".equals(bxbusitem.get("defitem12"))){
			busiitemBVO.setDefitem12(bxbusitem.get("defitem12").toString());
		}
		if(null!=bxbusitem.get("defitem13") && !"".equals(bxbusitem.get("defitem13"))){
			busiitemBVO.setDefitem13(bxbusitem.get("defitem13").toString());
		}
		if(null!=bxbusitem.get("defitem14") && !"".equals(bxbusitem.get("defitem14"))){
			busiitemBVO.setDefitem14(bxbusitem.get("defitem14").toString());
		}
		if(null!=bxbusitem.get("defitem15") && !"".equals(bxbusitem.get("defitem15"))){
			busiitemBVO.setDefitem15(bxbusitem.get("defitem15").toString());
		}
		if(null!=bxbusitem.get("defitem16") && !"".equals(bxbusitem.get("defitem16"))){
			busiitemBVO.setDefitem16(bxbusitem.get("defitem16").toString());
		}
		if(null!=bxbusitem.get("defitem17") && !"".equals(bxbusitem.get("defitem17"))){
			busiitemBVO.setDefitem17(bxbusitem.get("defitem17").toString());
		}
		if(null!=bxbusitem.get("defitem18") && !"".equals(bxbusitem.get("defitem18"))){
			busiitemBVO.setDefitem18(bxbusitem.get("defitem18").toString());
		}
		if(null!=bxbusitem.get("defitem19") && !"".equals(bxbusitem.get("defitem19"))){
			busiitemBVO.setDefitem19(bxbusitem.get("defitem19").toString());
		}
		if(null!=bxbusitem.get("defitem20") && !"".equals(bxbusitem.get("defitem20"))){
			busiitemBVO.setDefitem20(bxbusitem.get("defitem20").toString());
		} 
		if(null!=bxbusitem.get("defitem21") && !"".equals(bxbusitem.get("defitem21"))){
			busiitemBVO.setDefitem21(bxbusitem.get("defitem21").toString());
		}
		if(null!=bxbusitem.get("defitem22") && !"".equals(bxbusitem.get("defitem22"))){
			busiitemBVO.setDefitem22(bxbusitem.get("defitem22").toString());
		}
		if(null!=bxbusitem.get("defitem23") && !"".equals(bxbusitem.get("defitem23"))){
			busiitemBVO.setDefitem23(bxbusitem.get("defitem23").toString());
		}
		if(null!=bxbusitem.get("defitem24") && !"".equals(bxbusitem.get("defitem24"))){
			busiitemBVO.setDefitem24(bxbusitem.get("defitem24").toString());
		}
		if(null!=bxbusitem.get("defitem25") && !"".equals(bxbusitem.get("defitem25"))){
			busiitemBVO.setDefitem25(bxbusitem.get("defitem25").toString());
		}
		if(null!=bxbusitem.get("defitem26") && !"".equals(bxbusitem.get("defitem26"))){
			busiitemBVO.setDefitem26(bxbusitem.get("defitem26").toString());
		}
		if(null!=bxbusitem.get("defitem27") && !"".equals(bxbusitem.get("defitem27"))){
			busiitemBVO.setDefitem27(bxbusitem.get("defitem27").toString());
		}
		if(null!=bxbusitem.get("defitem28") && !"".equals(bxbusitem.get("defitem28"))){
			busiitemBVO.setDefitem28(bxbusitem.get("defitem28").toString());
		}
		if(null!=bxbusitem.get("defitem29") && !"".equals(bxbusitem.get("defitem29"))){
			busiitemBVO.setDefitem29(bxbusitem.get("defitem29").toString());
		}
		if(null!=bxbusitem.get("defitem30") && !"".equals(bxbusitem.get("defitem30"))){
			busiitemBVO.setDefitem30(bxbusitem.get("defitem30").toString());
		} 
		if(null!=bxbusitem.get("defitem31") && !"".equals(bxbusitem.get("defitem31"))){
			busiitemBVO.setDefitem31(bxbusitem.get("defitem31").toString());
		}
		if(null!=bxbusitem.get("defitem32") && !"".equals(bxbusitem.get("defitem32"))){
			busiitemBVO.setDefitem32(bxbusitem.get("defitem32").toString());
		}
		if(null!=bxbusitem.get("defitem33") && !"".equals(bxbusitem.get("defitem33"))){
			busiitemBVO.setDefitem33(bxbusitem.get("defitem33").toString());
		}
		if(null!=bxbusitem.get("defitem34") && !"".equals(bxbusitem.get("defitem34"))){
			busiitemBVO.setDefitem34(bxbusitem.get("defitem34").toString());
		}
		if(null!=bxbusitem.get("defitem35") && !"".equals(bxbusitem.get("defitem35"))){
			busiitemBVO.setDefitem35(bxbusitem.get("defitem35").toString());
		}
		if(null!=bxbusitem.get("defitem36") && !"".equals(bxbusitem.get("defitem36"))){
			busiitemBVO.setDefitem36(bxbusitem.get("defitem36").toString());
		}
		if(null!=bxbusitem.get("defitem37") && !"".equals(bxbusitem.get("defitem37"))){
			busiitemBVO.setDefitem37(bxbusitem.get("defitem37").toString());
		}
		if(null!=bxbusitem.get("defitem38") && !"".equals(bxbusitem.get("defitem38"))){
			busiitemBVO.setDefitem38(bxbusitem.get("defitem38").toString());
		}
		if(null!=bxbusitem.get("defitem39") && !"".equals(bxbusitem.get("defitem39"))){
			busiitemBVO.setDefitem39(bxbusitem.get("defitem39").toString());
		}
		if(null!=bxbusitem.get("defitem40") && !"".equals(bxbusitem.get("defitem40"))){
			busiitemBVO.setDefitem40(bxbusitem.get("defitem40").toString());
		} 
		if(null!=bxbusitem.get("defitem41") && !"".equals(bxbusitem.get("defitem41"))){
			busiitemBVO.setDefitem41(bxbusitem.get("defitem41").toString());
		}
		if(null!=bxbusitem.get("defitem42") && !"".equals(bxbusitem.get("defitem42"))){
			busiitemBVO.setDefitem42(bxbusitem.get("defitem42").toString());
		}
		if(null!=bxbusitem.get("defitem43") && !"".equals(bxbusitem.get("defitem43"))){
			busiitemBVO.setDefitem43(bxbusitem.get("defitem43").toString());
		}
		if(null!=bxbusitem.get("defitem44") && !"".equals(bxbusitem.get("defitem44"))){
			busiitemBVO.setDefitem44(bxbusitem.get("defitem44").toString());
		}
		if(null!=bxbusitem.get("defitem45") && !"".equals(bxbusitem.get("defitem45"))){
			busiitemBVO.setDefitem45(bxbusitem.get("defitem45").toString());
		}
		if(null!=bxbusitem.get("defitem46") && !"".equals(bxbusitem.get("defitem46"))){
			busiitemBVO.setDefitem46(bxbusitem.get("defitem46").toString());
		}
		if(null!=bxbusitem.get("defitem47") && !"".equals(bxbusitem.get("defitem47"))){
			busiitemBVO.setDefitem47(bxbusitem.get("defitem47").toString());
		}
		if(null!=bxbusitem.get("defitem48") && !"".equals(bxbusitem.get("defitem48"))){
			busiitemBVO.setDefitem48(bxbusitem.get("defitem48").toString());
		}
		if(null!=bxbusitem.get("defitem49") && !"".equals(bxbusitem.get("defitem49"))){
			busiitemBVO.setDefitem49(bxbusitem.get("defitem49").toString());
		}
		if(null!=bxbusitem.get("defitem50") && !"".equals(bxbusitem.get("defitem50"))){
			busiitemBVO.setDefitem50(bxbusitem.get("defitem50").toString());
		} 
		
		
		busiitemBVO.setGlobalbbje(new UFDouble(0));
		busiitemBVO.setGlobalcjkbbje(new UFDouble(0));
		busiitemBVO.setGlobalhkbbje(new UFDouble(0));
		busiitemBVO.setGroupbbje(new UFDouble(0));
		busiitemBVO.setGlobalzfbbje(new UFDouble(0));
		busiitemBVO.setGroupbbje(new UFDouble(0));
		busiitemBVO.setGroupcjkbbje(new UFDouble(0));
		busiitemBVO.setGrouphkbbje(new UFDouble(0));
		busiitemBVO.setGroupzfbbje(new UFDouble(0));
		busiitemBVO.setHkbbje(new UFDouble());
		busiitemBVO.setHkybje(new UFDouble());
		busiitemBVO.setHkbbje(new UFDouble());
		busiitemBVO.setGlobalbbye(new UFDouble(0));
		busiitemBVO.setGroupbbye(new UFDouble(0));
		busiitemBVO.setDeptid(pk_dept);
		busiitemBVO.setDwbm(pk_org); 
		busiitemBVO.setJkbxr(jkbxr);
		busiitemBVO.setReceiver(jkbxr);
		 
	 	
		if (null != bxbusitem.get("pk_busitem") && !"".equals(bxbusitem.get("pk_busitem"))) {
			busiitemBVO.setPk_busitem( bxbusitem.get("pk_busitem").toString());// 借款单业务行标识
		}
		if (null != bxbusitem.get("pk_jkbx") && !"".equals(bxbusitem.get("pk_jkbx"))) {
			busiitemBVO.setPk_jkbx( bxbusitem.get("pk_jkbx").toString());// 借款单标识
		}
		 
		
		//报销类型
		if (null != bxbusitem.get("pk_reimtype") && !"".equals(bxbusitem.get("pk_reimtype"))) {
			busiitemBVO.setPk_reimtype(bxbusitem.get("pk_reimtype").toString());
		}
		if (null != bxbusitem.get("szxmid") && !"".equals(bxbusitem.get("szxmid"))) {
			busiitemBVO.setSzxmid(bxbusitem.get("szxmid").toString());
		} 
		//如果表体供应商存在，则赋值给表体
		if(null!=head.get("hbbm") && !"".equals(head.get("hbbm"))){
			busiitemBVO.setHbbm(head.get("hbbm").toString()); 
		}
		//个人银行账户
		busiitemBVO.setSkyhzh(hvo.getSkyhzh());
		
		//项目名称
		if(null !=  bxbusitem.get("jobid") && !"".equals(bxbusitem.get("jobid"))){
			busiitemBVO.setJobid(bxbusitem.get("jobid").toString());		
		}  
		if(null !=  bxbusitem.get("customer") && !"".equals(bxbusitem.get("customer"))){
			busiitemBVO.setCustomer(bxbusitem.get("customer").toString());
		}else{
			busiitemBVO.setCustomer(hvo.getCustomer());
		}
		 
		//客商银行账户
		if(null !=  bxbusitem.get("custaccount") && !"".equals(bxbusitem.get("custaccount"))){
			busiitemBVO.setCustaccount(bxbusitem.get("custaccount").toString());
		}else if(null!=head.get("custaccount") && !"".equals(head.get("custaccount"))){
			busiitemBVO.setCustaccount(head.get("custaccount").toString());
		}
		 
		//收款对象
		if(null != bxbusitem.get("paytarget") && !"".equals(bxbusitem.get("paytarget"))){
			busiitemBVO.setPaytarget(Integer.parseInt(bxbusitem.get("paytarget").toString()));
		}else{
			if(null != head.get("paytarget") && !"".equals(head.get("paytarget"))){
				busiitemBVO.setPaytarget(Integer.parseInt(head.get("paytarget").toString()));
			}
		}
		
		if (null != bxbusitem.get("deptid") && !"".equals(bxbusitem.get("deptid"))) {
			busiitemBVO.setDeptid(pk_dept);
		}
		if (null != bxbusitem.get("dwbm") && !"".equals(bxbusitem.get("dwbm"))) {
			busiitemBVO.setDwbm(bxbusitem.get("dwbm").toString());
		} 
		if(null != bxbusitem.get("receiver") && !"".equals(bxbusitem.get("receiver"))){
			busiitemBVO.setReceiver(bxbusitem.get("receiver").toString());
		} 
		if(null != bxbusitem.get("skyhzh") && !"".equals(bxbusitem.get("skyhzh"))){
			busiitemBVO.setSkyhzh(bxbusitem.get("skyhzh").toString());
		}
		
		//参照生单 
		if(null != bxbusitem.get("pk_mtapp_detail") && !"".equals(bxbusitem.get("pk_mtapp_detail"))){ 
			busiitemBVO.setPk_mtapp_detail(bxbusitem.get("pk_mtapp_detail").toString());
		}
		if(null != bxbusitem.get("pk_item") && !"".equals(bxbusitem.get("pk_item"))){
			busiitemBVO.setPk_item(bxbusitem.get("pk_item").toString());
		}
		if(null != bxbusitem.get("srcbilltype") && !"".equals(bxbusitem.get("srcbilltype"))){
			busiitemBVO.setSrcbilltype(bxbusitem.get("srcbilltype").toString());
			busiitemBVO.setSrctype("261X");
		} 
	}
	
	@Override
	public Object save(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;// 聚合
		HashMap<String, Object> head = bill.getHeadVO();// 表头
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();// 表体

		HashMap<String, Object>[] materialbs = bodys.get("jk_busitem");// 页签

		JKVO aggvo = new JKVO();// 聚合
		JKVO transVOs = null;// 修改时数据库获取的聚合VO
		JKVO cloneagg = new JKVO();
		JKHeaderVO hvo = new JKHeaderVO();// 表头
		//新加  费用申请单
		BXBusItemVO[] bvos = null;// 交通费用，其他费用
		
		//dongl校验借款单在参照费用申请单时不能新增单据子表
		if(null != head.get("pk_item") && !"".equals(head.get("pk_item"))){
			jy_Mtapp_bill(materialbs,head.get("pk_item").toString());
		}

		// 判断是增加还是修改，然后确定单据状态
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			String pk_jkbx = head.get("PK").toString();
			Collection<JKVO> generalVOC = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(JKVO.class, " isnull(dr,0)=0 and pk_jkbx='" + pk_jkbx + "' ", false);
			if (null == generalVOC || generalVOC.size() == 0) {
				throw new BusinessException("修改的订单已经被删除!");
			}
			transVOs = generalVOC.toArray(new JKVO[] {})[0];
			hvo = (JKHeaderVO) transVOs.getParentVO();// getJkHeadVOs();
			bvos = transVOs.getChildrenVO();
			BXBusItemVO[] bclone = new BXBusItemVO[bvos.length];
			for (int b = 0; b < bvos.length; b++) {
				bclone[b] = new BXBusItemVO();
				bclone[b] = (BXBusItemVO) bvos[b].clone();
			}
			cloneagg.setParentVO((CircularlyAccessibleValueObject) hvo.clone());
			cloneagg.setChildrenVO(bclone);
			hvo.setStatus(1);
		} else {
			hvo.setStatus(2);
		}

		/**
		 * 设置表头字段
		 */
		UFDouble ybje = new UFDouble(0);
		//新加
		BXBusItemVO bvo = null;
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
		Date day = c.getTime();
		String str = new SimpleDateFormat("yyyy-MM-dd").format(day);
		hvo.setZhrq(new UFDate(str));// 最迟还款日new Date() 
		if(null != head.get("zhrq") && !"".equals(head.get("zhrq"))){
			if(new UFDate().after(new UFDate(head.get("zhrq").toString()))){
				throw new BusinessException("最迟还款日不能小于当前日期！");
			}
			hvo.setZhrq(new UFDate(head.get("zhrq").toString()));
		}
		hvo.setPayflag(1);// 清账状态
		if (null != head.get("pk_jkbx") && !"".equals(head.get("pk_jkbx"))) {
			hvo.setPk_jkbx( head.get("pk_jkbx").toString());
		}
		  
		/**
		 * 1.设置主表的字段
		 */
		setHVO(hvo,head,userid);


		/**
		 * 子表
		 */
		UFDouble amount = new UFDouble(0);
		UFDouble amounts = new UFDouble(0);
		UFDouble hvoTotal = new UFDouble(0);
		ArrayList<BXBusItemVO> bvosn = new ArrayList<BXBusItemVO>();
		//新加 
		Integer rowno = 0;
		if (null != materialbs && materialbs.length > 0) {
			for (int i = 0; i < materialbs.length; i++) {
				rowno = (i + 1) + 0;
				bvo = new BXBusItemVO();
				if(null != head.get("PK") && !"".equals(head.get("PK"))){
					// 删除
					if (null != materialbs[i].get("vostatus")  && "3".equals(materialbs[i].get("vostatus").toString())) {
						//界面数据
						amount = new UFDouble(materialbs[i].get("amount").toString());
						hvoTotal = hvo.getTotal().sub(amount);
						bvo.setPk_jkbx(materialbs[i].get("pk_jkbx").toString());
						bvo.setPk_busitem(materialbs[i].get("pk_busitem").toString());
					}
					// 更新
					if (null != materialbs[i].get("vostatus")  && "1".equals(materialbs[i].get("vostatus").toString())) {
						//界面数据
						amount = new UFDouble(materialbs[i].get("amount").toString());
						bvo.setPk_jkbx(materialbs[i].get("pk_jkbx").toString());
						String pk_busitem = materialbs[i].get("pk_busitem").toString();
						bvo.setPk_busitem(pk_busitem);
						String strWhere = " isnull(dr,0) = 0 and pk_busitem  = '"+pk_busitem+"'";
						ArrayList<SuperVO> bodylist = (ArrayList<SuperVO>) dao.retrieveByClause(BXBusItemVO.class, strWhere);
						if(null != bodylist && bodylist.size() >0){
							hvoTotal = amount.add(hvo.getTotal()).sub(new UFDouble(bodylist.get(0).getAttributeValue("amount").toString()));
						}
					}
					// 新增
					if (null != materialbs[i].get("vostatus")  && "2".equals(materialbs[i].get("vostatus").toString())) {
						//界面数据
						amount = new UFDouble(materialbs[i].get("amount").toString());
						hvoTotal = hvo.getTotal().add(amount);
					}
					hvo.setTotal(hvoTotal);
				}
				//表体设置状态
				UFDouble amountN = new UFDouble(0);
				if(null != materialbs[i].get("amount") && !"".equals(materialbs[i].get("amount"))){
					amounts = new UFDouble(materialbs[i].get("amount").toString()).add(amounts);
                    amountN = new UFDouble(materialbs[i].get("amount").toString());
                    bvo.setAmount(amountN);
					bvo.setBbje(amountN);// 本币金额
					bvo.setBbye(amountN);// 本币余额
					bvo.setGroupbbje(amountN);
					bvo.setGlobalbbje(amountN); 

					bvo.setYbje(amountN);// 原币金额
					bvo.setYbye(amountN);// 原币余额
					bvo.setYjye(amountN);// 预计余额
					bvo.setZfbbje(amountN);// 支付本币金额
					bvo.setZfybje(amountN);// 支付金额

					bvo.setAttributeValue("yjye", amountN);
					bvo.setAttributeValue("bbye", amountN);
					bvo.setAttributeValue("ybye", amountN);
					bvo.setGlobalbbye(new UFDouble(0));
					bvo.setGroupbbye(new UFDouble(0));
				}
				if(null != head.get("PK") && !"".equals(head.get("PK"))){
					if (null != materialbs[i].get("vostatus")  && !"2".equals(materialbs[i].get("vostatus"))) {
						amounts = hvoTotal;
					}
				}
				if( null != materialbs[i].get("vostatus") && !"".equals(materialbs[i].get("vostatus"))){
					bvo.setStatus(Integer.parseInt(materialbs[i].get("vostatus").toString()));
				}
				bvo.setTablecode("jk_busitem");// 页签编码
				bvo.setRowno(rowno);// 行号
				/**
				 * 2.封装第1个子表
				 */
				setBusiitemBVO(hvo,head,bvo,materialbs[i]);

				bvosn.add(bvo);
			}
		}
		hvo.setBbhl(new UFDouble(1));// 本币汇率
		hvo.setBbje(new UFDouble(amounts));// 借款本币金额
		//hvo.setTotal(new UFDouble(total));// 合计金额
		hvo.setBbye(new UFDouble(amounts));// 本币余额
		hvo.setGroupbbje(new UFDouble(amounts));
		hvo.setGlobalbbje(new UFDouble(amounts));
		hvo.setYbje(new UFDouble(amounts));// 借款原币金额
		hvo.setYbye(new UFDouble(amounts));// 原币余额
		hvo.setYjye(new UFDouble(ybje));// 预计余额
		hvo.setZfbbje(new UFDouble(amounts));// 支付本币金额
		hvo.setZfybje(new UFDouble(amounts));// 支付原币金额
		hvo.setIsmashare(new UFBoolean(false));
		
		if(null == head.get("PK") || "".equals(head.get("PK"))){
			hvo.setTotal(amounts);
		}else{
			hvo.setYjye(new UFDouble(hvo.getYbje()));
		}
		
		HashMap eParam = new HashMap();
		if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {

			eParam.put("notechecked", "notechecked");
			HashMap<String, UFBoolean> map = new HashMap<String, UFBoolean>();
			map.put("ATPCheck", new UFBoolean(false));
			eParam.put("SCMResumeExceptionResult", map);

		}

		aggvo.setParentVO(hvo);// 将表头设置到聚合VO中
		// 将表体对象集合设置到聚合VO中
		aggvo.setChildrenVO(bvosn.toArray(new BXBusItemVO[] {}));
		//dongl 费用申请单封装 matterAppVOfind(id)
		if(null != head.get("pk_item") && !"".equals(head.get("pk_item"))){ 
			String pk_mtapp_bill = head.get("pk_item").toString();
			MatterAppVO matterHeadVO = (MatterAppVO) new FeeBaseAction().matterAppVOfind(pk_mtapp_bill);
			if(null != matterHeadVO){
				aggvo.setMaheadvo(matterHeadVO);
			}
		}
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		nc.itf.arap.pub.IBXBillPublic iIplatFormEntry = (nc.itf.arap.pub.IBXBillPublic) NCLocator.getInstance().lookup(nc.itf.arap.pub.IBXBillPublic.class.getName());
		
		Object o = null;// 初始化超类
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			// 更新
			try {
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					IplatFormEntry iIplatFormEntry1 = (IplatFormEntry) NCLocator.getInstance().lookup(IplatFormEntry.class.getName());
					o = iIplatFormEntry1.processAction("WRITE", hvo.getDjlxbm(), null, aggvo, null, eParam);
				} else {
					o = iIplatFormEntry.update(new JKBXVO[] { aggvo });// new
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
					 o = iIplatFormEntry.save(new JKBXVO[] { aggvo });
				} else {
					 o = iIplatFormEntry.save(new JKBXVO[] { aggvo });
				}
			} catch (Exception e) {
				if (e.getMessage().contains("是否继续")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		}


		JKBXVO[] returnVO = (JKBXVO[]) o;
		if (o != null && returnVO.length > 0) {
			JKHeaderVO returnHVO = (JKHeaderVO) returnVO[0].getParentVO();
			String pk = returnHVO.getPk_jkbx();
			dao.executeUpdate("update er_busitem set globalbbye=ybje,groupbbye=ybje,bbye=ybje,ybye=ybje,yjye=ybje where pk_jkbx='"+pk+"'");
			return queryBillVoByPK(userid, returnHVO.getPk_jkbx());
		}
		return null;

	}

	private void jy_Mtapp_bill(HashMap<String, Object>[] materialbs,String pk_item) throws BusinessException {		
		String strWhere = " nvl(dr,0) = 0 and pk_mtapp_bill  = '"+pk_item+"'";
		ArrayList<SuperVO> bodylist = (ArrayList<SuperVO>) dao.retrieveByClause(MtAppDetailVO.class, strWhere);
		if(null != bodylist && bodylist.size() > 0){
			if(bodylist.size() < materialbs.length){
				throw new BusinessException("参照申请单时不可新增子表");
			}
		}
	}
	
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
		if(null==head_querybillVO){
			return null;
		}
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

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int start, int endnum) throws BusinessException {
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","er_jkzb.pk_jkbx");
		map.put("PK","er_jkzb.pk_jkbx");
		map.put("arap_jkzb", "er_jkzb");
		map.put("zfdwbm", "er_jkzb");
		map.put("fydwbm", "er_jkzb");
		map.put("dwbm", "er_jkzb");
		map.put("bx_receiver", "er_jkzb");
		map.put("jk_busitem", "er_busitem");
		map.put("arap_bxbusitem", "er_busitem");
		map.put("other", "er_busitem");
		map.put("pk_org", "er_jkzb.pk_org");
		map.put("head", "er_jkzb");
		StringBuffer str = reCondition(obj, true,map);
		
		String billtype = super.getBilltype();
		String billtype_code = "";
		String sql = "";
		billtype_code = new FeeBaseAction().getbilltypeCode(billtype);
		if (billtype_code.length() > 1) {
			billtype_code = " and er_jkzb.djlxbm ='" + billtype_code + "' ";
		}

		// 查询sql语句并进行分页
		sql ="select *\n" +
			"  from (select rownum rowno, a.*\n" + 
			"          from (select distinct er_jkzb.*\n" + 
			"                  from er_jkzb \n" +
			"				   left join er_busitem on er_jkzb.pk_jkbx=er_busitem.pk_jkbx and nvl(er_busitem.dr,0)=0\n " +
			"				   left join er_bxcontrast on er_jkzb.pk_jkbx =er_bxcontrast.pk_bxd and nvl(er_bxcontrast.dr, 0) = 0 \n " + 
			"                 where nvl(er_jkzb.dr, 0) = 0\n" + 
			"                   and creator = '"+userid+"'\n" + 
			"                   "+str+"\n" + 
			"                   "+billtype_code+"\n" + 
			"                 order by er_jkzb.djrq desc) a)\n" + 
			" where rowno between "+start+" and "+endnum+""; 
		
		ArrayList<JKHeaderVO> list = (ArrayList<JKHeaderVO>) dao.executeQuery(sql, new BeanListProcessor(JKHeaderVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new JKHeaderVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		String djbm = "";
		for (int i = 0; i < maps.length; i++) {
			if(null != maps[i].get("djlxbm")){
				djbm =" '"+maps[i].get("djlxbm")+"' ";
			}
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			// 单据状态处理
			// djzt单据状态 0=暂存，1=保存，2=审核，3=签字，-1=作废，
			if (null == maps[i].get("spzt") || "".equals(maps[i].get("spzt")) || -1 == Integer.parseInt(maps[i].get("spzt").toString())) {
				maps[i].put("ibillstatus", -1);// 自由
			} else if (null != maps[i].get("spzt") && 3 == Integer.parseInt(maps[i].get("spzt").toString())) {
				maps[i].put("ibillstatus", 3);// 提交
			} else {
				maps[i].put("ibillstatus", 1);// 审批通过
			}
			if(null!=maps[i].get("zy")){
				for(Map.Entry<String,String> su:summap.entrySet()){
					String key = su.getKey();
					if(maps[i].get("zy").toString().equals(key)){
						maps[i].put("zy",su.getValue());
					} 
				}
			}
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;

		}
		//翻译 审批表头公式 
		new FeeBaseAction().excuteListShowFormulas(djbm, billVOs);
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}
 
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
		// 表体查询语句
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_jkbx") ;
		String condition2 = condition.toString().replace("PPK", "pk_jkd") ;
		
		String sql = "select * from er_busitem where isnull(dr,0)=0 " + condition1 + " order by rowno ";
		ArrayList<BXBusItemVO> list = (ArrayList<BXBusItemVO>) dao.executeQuery(sql, new BeanListProcessor(BXBusItemVO.class));

		if (list != null && list.size()> 0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new BXBusItemVO[0]));
			/**
			 * vostatus->1 界面未编辑，也会回传给后台
			 */
			String[] formulas = new String[] { "vostatus->1;" };
			PubTools.execFormulaWithVOs(maps, formulas);
			billVO.setTableVO("jk_busitem", maps);
		}
		
		String sqls = "select * from er_bxcontrast where isnull(dr,0)=0 " + condition2;
		ArrayList<BxcontrastVO> listc = (ArrayList<BxcontrastVO>) dao.executeQuery(sqls, new BeanListProcessor(BxcontrastVO.class));
		if (listc != null && listc.size() > 0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(listc.toArray(new BxcontrastVO[0]));
			/**
			 * vostatus->1 界面未编辑，也会回传给后台
			 */
			String[] formulas = new String[] { "vostatus->1;" };
			PubTools.execFormulaWithVOs(maps, formulas);
			billVO.setTableVO("er_bxcontrast", maps);
		}
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if(null!=bill.getHeadVO().get("pk_jkbx")){
			String csaleorderid =  bill.getHeadVO().get("pk_jkbx").toString();
			JKVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("DELETE", "263X", null, aggvos, new JKBXVO[] { aggvos }, null);
		}
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if(null!=bill.getHeadVO().get("pk_jkbx")){
			String csaleorderid = bill.getHeadVO().get("pk_jkbx").toString();
			JKVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("SAVE", "263X", null, aggvos, aggvos, null);
			return queryBillVoByPK(userid, csaleorderid);
		}
		return null;
	}



/*	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_jkbx");
		JKVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, csaleorderid, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		Object o = pf.processAction("UNAPPROVE", "263X", null, aggvos, new JKVO[] { aggvos }, null);
		return queryBillVoByPK(userid, csaleorderid);
		
	}*/


	 
	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if(null!=bill.getHeadVO().get("pk_jkbx")){
			String pk_jkbx =  bill.getHeadVO().get("pk_jkbx").toString();
			JKVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, pk_jkbx, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processBatch("UNSAVE", "263X-Cxx-JKD", new JKVO[] { aggvos }, null, null, null);
			return queryBillVoByPK(userid, pk_jkbx);
		}
		return null;
	}

}
