package hd.bs.mobile.bdoc;

import hd.bs.bill.bxfee.FeeBaseAction;
import hd.bs.muap.bdoc.DefaultDocAction;
import hd.itf.muap.pub.IMobileAction;
import hd.itf.muap.pub.IMobileBusiAction;
import hd.muap.pub.cache.CacheConfig;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.MDefDocVO;
import hd.vo.muap.pub.RetDefDocVO;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.uap.cpb.org.orgs.CpOrgVO;
import nc.uap.cpb.org.vos.CpUserVO;
import nc.vo.bd.countryzone.CountryZoneVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.bd.cust.areaclass.AreaclassVO;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.bd.income.IncomeVO;
import nc.vo.bd.material.branddoc.BrandDocVO;
import nc.vo.bd.prodline.ProdLineVO;
import nc.vo.bd.psn.PsnClVO; 
import nc.vo.bd.region.RegionVO; 
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.org.AdminOrgVO;
import nc.vo.org.DeptVO;
import nc.vo.org.GroupVO;
import nc.vo.org.HROrgVO;
import nc.vo.org.OrgVO;
import nc.vo.org.PostSeriesVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pubapp.AppContext;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.role.RoleVO;
import nc.vo.vorg.AdminOrgVersionVO;
import nc.vo.vorg.DeptVersionVO;

public class BdocAction extends DefaultDocAction implements IMobileBusiAction {

	public BdocAction() {
	}

	BaseDAO dao = new BaseDAO();
	static String pk_group = "";
	static {
		String sql = "select * from org_group where nvl(dr,0)=0";
		try {
			ArrayList<GroupVO> groupvos = (ArrayList<GroupVO>) new BaseDAO().executeQuery(sql, new BeanListProcessor(GroupVO.class));
			if (groupvos.size() > 0) {
				pk_group = groupvos.get(0).getPk_group();
			}
		} catch (DAOException e) {
			try {
				throw new BusinessException(e.getMessage());
			} catch (BusinessException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static String getString_ZeroLen(Object value) {
		if ( value==null || value.toString().trim().length()==0 ) {
			return  " " ;
		}
		return  value.toString().trim() ;
	}
	
	//dongl 处理PC列表 传入多PK拼接 ---优化去重
	public String getPKs(String pk){
		if(pk.contains(",")){
			//set去除重复
			Set<String> set = new HashSet<String>();
			String[] arrPK = pk.split(",");
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<arrPK.length;i++){
				set.add(arrPK[i].trim());
			}
			if(null != set && set.size() > 0){
				for(String str : set){
					buffer.append(str).append("'").append(",").append("'");
				}
				if(null != buffer && buffer.length() > 3){
					pk = buffer.substring(0,buffer.length()-3); 
				}
			}
		}
		return pk;
	}

	String pk_res = "0001ZU1000000003KK65";

	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		if (action.equals(IMobileAction.QUERY)) {
			if (userid == null || userid.trim().length() == 0) {
				throw new BusinessException("userid 参数不能为空！");
			}
			if (obj == null || !(obj instanceof ConditionAggVO)) {
				throw new BusinessException("obj 参数不能为空 并且 类型须为ConditionAggVO ！ ");
			}
			ConditionAggVO condAggVO = (ConditionAggVO) obj;
			if (condAggVO == null || condAggVO.getConditionVOs().length < 1) {
				throw new BusinessException("ConditionAggVO 参数内容不能为空 ！ ");
			}

			ConditionVO[] condVOs = condAggVO.getConditionVOs();
			String datatype = condVOs[0].getValue(); // 参照类型
			if (datatype.contains(",")) {
				String[] datatypes = datatype.split(",");
				if (datatypes.length > 1) {
					datatype = datatypes[0];
				}
			}
			String pk_org = condVOs[1].getValue(); // pk_org
			String pk_deptdoc = null;
			String pk_billtype = null;
			String pk_group = null;
			String pk = null;
			String pk_allorg = null;
			String pk_dept = null;
			String glorg = null;
			Integer stapply_mode = null;
			String pk_region = null;
			String pk_province = null;
			String pk_city = null;
			String pk_account = null;
			String pk_org_financial = null;
			String ccustomerid = null;
			String owner = null;
			String csendstockorgvid = null;
			String pk_material = null;
			String queryPk_org = null;
			String pk_cust = null;
			String pk_busitem = null;
			String pk_item = null;
			String cjkybje = null;
			String billmaker = null; 
			String receiver = null;
			String hbbm = null;
			String paytarget = null;
			String pk_docListCode = null;
			String scon = null;
			String and = "";
            String defitem14 = null;
			String defitem16 = null;			
                        for (int i = 0; i < condVOs.length; i++) {
				if (condVOs[i].getField().equals("pk_finacialorg") || condVOs[i].getField().equals("store") || (condVOs[i].getField().equals("pk_org") && null != condVOs[i].getDatatype() && 0 == condVOs[i].getDatatype())) {
					queryPk_org = condVOs[i].getValue();
				}
				// 参照主键
				if (condVOs[i].getField().equals("pk")) {
					//dongl 
					pk = condVOs[i].getValue();
					pk = getPKs(pk);
				}
				if (condVOs[i].getField().equals("pk_deptdoc") || condVOs[i].getField().equals("department")) {
					pk_deptdoc = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("pk_province")) {
					pk_province = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("pk_city")) {
					pk_city = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("pk_region")) {
					pk_region = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("billtype") || condVOs[i].getField().equals("head.billtype")) {
					pk_billtype = condVOs[i].getValue();
				}
 
				if (condVOs[i].getField().equals("pk_group")) {
					pk_group = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("pk_account")) {
					pk_account = condVOs[i].getValue();
				}
				// 服务单 赠品单 根据销售组织和当前登录人过滤客户
				if (condVOs[i].getField().equals("pk_org_financial") || condVOs[i].getField().equals("saleorg") || condVOs[i].getField().equals("khserviceHVO.pk_org_financial")) {
					pk_org_financial = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("pk_org.ccustomerid")) {
					ccustomerid = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("head.owner")) {
					owner = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("cmaterialid.csendstockorgvid")) {
					csendstockorgvid = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("cmaterialid.cmaterialid")) {
					pk_material = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("attr_hbzz")) {
					pk_allorg = condVOs[i].getValue();
				}

				// 参照主键
				/*if (condVOs[i].getField().equals("pk")) {
					pk = condVOs[i].getValue();
				}*/
				if (condVOs[i].getField().equals("pk_deptdoc") || condVOs[i].getField().equals("department")) {
					pk_deptdoc = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("billtype") || condVOs[i].getField().equals("pk_billtype")) {
					pk_billtype = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("and ")) {
					and = condVOs[i].getField();
				}
				if (condVOs[i].getField().equals("stapply_mode")) {
					stapply_mode = Integer.parseInt((condVOs[i].getValue() == null || condVOs[i].getValue().trim().length() == 0) ? "0" : condVOs[i].getValue());
				}
				if (condVOs[i].getField().equals("oldpk_dept") || condVOs[i].getField().equals("newpk_dept") || condVOs[i].getField().equals("pk_dept")|| condVOs[i].getField().equals("psndept")) {
					pk_dept = condVOs[i].getValue();
				}
				if (condVOs[i].getField().equals("oldpk_org") || condVOs[i].getField().equals("newpk_org") || condVOs[i].getField().equals("pk_org")) {
					glorg = condVOs[i].getValue();
				} 
				if (condVOs[i].getField().contains("pk_busitem")) {
					pk_busitem = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("pk_item")) {
					pk_item = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("cjkybje")) {
					cjkybje = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("cjkybje")) {
					cjkybje = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("fydwbm")) {
					pk_org = condVOs[i].getValue();// 费用承担单位
				}
				if (condVOs[i].getField().contains("hbbm")) {
					hbbm = condVOs[i].getValue();// 费用承担单位
				} 
				if (condVOs[i].getField().contains("paytarget")) {
					paytarget = condVOs[i].getValue();// 费用承担单位
				} 
				if(condVOs[i].getField().contains("customer")){
					pk_cust = condVOs[i].getValue();
				}
				if(condVOs[i].getField().contains("paytarget")){
					paytarget = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("receiver")) {
					receiver = condVOs[i].getValue();// 费用承担单位
				}
				//东亮 2020-2-19 13:52:03
				if (condVOs[i].getField().contains("assume_org")) {
					pk_org = condVOs[i].getValue();// 费用承担单位
				}
				if (condVOs[i].getField().contains("apply_org")) {
					pk_org = condVOs[i].getValue();// 费用承担单位
				}   
				if (condVOs[i].getField().equals("pk_org")) {
					pk_org = condVOs[i].getValue();// 费用承担单位
				} 
				// 单据类型
				if (condVOs[i].getField().startsWith("billtype")) {
					pk_billtype = condVOs[i].getField().substring(condVOs[i].getField().indexOf("e") + 1);
				}
				if (condVOs[i].getField().startsWith("doclist_")) {
					pk_docListCode = condVOs[i].getField().substring(condVOs[i].getField().indexOf("_") + 1);
				}
				//费用承担部门
				if (condVOs[i].getField().startsWith("pk_fiorg.fydwbm")) {
					pk_org = condVOs[i].getValue();
				}
				//费用承担部门
				if (condVOs[i].getField().startsWith("fydwbm.fydwbm")) {
					pk_org = condVOs[i].getValue();
				}
				//根据组织 过滤部门 
				if (condVOs[i].getField().startsWith("applyinfo.apply_org")||condVOs[i].getField().endsWith(".pk_org")) {
					pk_org = condVOs[i].getValue();
				}
				//根据部门 过滤人员 
				if ( condVOs[i].getField().endsWith(".pk_deptdoc")) {
					pk_deptdoc = condVOs[i].getValue();
				}
				//大数据量参照处理
				if (condVOs[i].getField().contains("SCON")) {
					scon = condVOs[i].getValue();
				}
				if(condVOs[i].getField().contains("fieldcontrast.src_billtype")){
					pk_billtype = condVOs[i].getValue();
				}
				if(condVOs[i].getField().contains("rule_data")){
					pk_billtype = "rule_data";
				}
				if(condVOs[i].getField().contains("er_sharerule.fieldname")){
					and = condVOs[i].getValue();
				}
                if (condVOs[i].getField().contains("defitem14")) {
					defitem14 = condVOs[i].getValue();
				}
				if (condVOs[i].getField().contains("defitem16")) {
					defitem16 = condVOs[i].getValue();
				}
			}
			if("借款交易类型".equals(datatype)){
				return jkBilltype(pk,billtype);
			}else if ("部门档案".equals(datatype) || "部门".equals(datatype) || "部门(协同)".equals(datatype) || "部门版本".equals(datatype)) {
				return deptDoc(pk_org, billtype, pk);
			} else if ("人员档案".equals(datatype)) {
				return psnDoc(pk_deptdoc, billtype, pk, pk_org);
			} else if ("人员".equals(datatype)) {
				return psncrmDoc(pk_deptdoc, billtype, pk, pk_org);
			}else if ("标准人员档案".equals(datatype)) {
				return standPsndoc(pk_deptdoc, billtype, pk, pk_org);
			} else if ("协同当前集团+业务单元".equals(datatype) || "协同业务单元(当前集团)".equals(datatype)) {
				return cooperGroupOrgDoc(pk_org, billtype, pk, pk_billtype, userid);
			} else if ("集团".equals(datatype)) {
				return groupDoc(billtype, pk);
			} else if ("业务单元+集团".equals(datatype)) {
				return allOrgs(billtype, pk);
			} else if ("销售组织".equals(datatype) || "公司".equals(datatype)) {
				return saleOrg(billtype, pk_billtype, pk, pk_org, userid);
			} else if ("结算成本域".equals(datatype)) {
				return costdomain(billtype, pk_billtype, pk, pk_org, userid);
			} else if ("全单位".equals(datatype)) {
				return company(billtype, pk_billtype, pk, pk_org, userid);
			} else if (datatype.contains("业务单元")) {
				return orgoid(billtype, pk_billtype, pk, pk_org, userid);
			} else if(datatype.equals("行政组织")){
				return adminorg(billtype, pk_billtype, pk, pk_org, userid);
			} else if ("行政组织版本".equals(datatype)) {
				return adminorg(pk, billtype);
			} else if(datatype.contains("财务组织")){
				return fanaceorg(billtype, pk_billtype, pk, pk_org, userid);
			} else if(datatype.contains("利润中心")||datatype.contains("成本中心")){
				return liacenterorg(billtype, pk_billtype, pk, pk_org, userid);
			} else if(datatype.contains("项目任务")){
				return pmwbs(billtype, pk,pk_org);
			} else if ("协同用户".equals(datatype)) {
				return cooperUserDoc(billtype, pk, pk_org, pk_allorg, pk_billtype, userid);
			} else if ("协同组织".equals(datatype)) {
				return xtOrg(billtype, pk_billtype, pk_org, pk, userid);
			} else if ("全局协同组织".equals(datatype)) {
				return xtOrg(billtype, pk_billtype, null, null, userid);
			} else if ("集团(所有)".equals(datatype)) {
				return groupDoc(billtype, pk);
			} else if (datatype.contains("交易类型")) {
				return billType(datatype, pk, pk_org, pk_billtype);
			} else if ("员工号".equals(datatype)) {
				return TBMPsndocRefModel(pk_org, pk, userid);
			} else if ("人力资源组织(所有)".equals(datatype)) {
				return HrOrgRefModel(pk, billtype);
			}  else if ("部门版本(所有)".equals(datatype)) {
				return orgdept(pk, billtype);
			} else if ("HR人员".equals(datatype)) {
				return hr_psndocRefModel(pk, billtype);
			} else if ("考勤类别".equals(datatype)) {
				return timeitemRefModel(pk_billtype, billtype, pk, pk_org);
			} else if ("考勤类别拷贝".equals(datatype)) {
				return timeitemCopyRefModel(pk_billtype, billtype, pk, pk_org);
			} else if ("人员工作记录(左树不含下级HR)".equals(datatype)) {
				return psnjobRefModel(pk, billtype, pk_org);
			} else if ("人员工作记录(入职申请)".equals(datatype)) {
				return psnjobApplyRefModel(pk, billtype, pk_org);
			} else if ("业务流程(入职申请)".equals(datatype)) {
				return businessApplyRefModel(pk, billtype, pk_org);
			} else if ("交易类型(入职申请)".equals(datatype)) {
				return billApplyType(datatype, pk, pk_org, pk_billtype);
			} else if ("加班类别".equals(datatype)) {
				return timeItemdoc(pk, billtype, pk_org);
			} else if ("异动类型".equals(datatype)) {// why
				return trnstypeRefModel(pk, billtype, pk_org, pk_group, pk_billtype);
			} else if ("人员类别".equals(datatype)) {// why
				return psnclvoRefModel(pk, billtype, pk_org);
			} else if ("部门HR".equals(datatype)) {// why
				return deptHrRefModel(pk, billtype, pk_org, glorg);
			} else if ("岗位HR".equals(datatype)) {// why
				return postRefModel(pk, billtype, pk_org, pk_dept, glorg);
			} else if ("岗位序列HR".equals(datatype)) {// why
				return postseriesRefModel(pk, billtype, pk_org);
			} else if ("职务(设置业务单元主键)".equals(datatype)) {// why
				return jobtypejob3RefModel(pk, billtype, pk_org);
			} else if ("职务类别HR".equals(datatype)) {// why
				return jobtypeRefModel(pk, billtype, pk_org, pk_group);
			} else if ("职级(设置职务主键)".equals(datatype)) {// why
				return postgradeRefModel(pk, billtype, pk_org);
			} else if ("职等HR".equals(datatype)) {// why
				return jobrankRefModel(pk, billtype, pk_org, pk_group); 
			} else if ("人员工作记录(调配)".equals(datatype)) {
				return psnjobdpRefModel(pk, billtype, pk_org, pk_group, stapply_mode == null ? 0 : stapply_mode, userid, pk_billtype);
			} else if ("销差员工号".equals(datatype)) {
				return XCsndocRefModel(pk_org, pk, userid);
			} else if ("转正员工号".equals(datatype)) {
				return ZZsndocRefModel(pk_org, pk_deptdoc, userid);
			} else if ("休假登记信息(销假)".equals(datatype)) {
				return psnjobxiaojRefModel(pk_org, pk, userid);
			} else if ("客户基本信息".equals(datatype) || "客户档案".equals(datatype)) {
				return customerDoc(userid, pk_org, billtype, pk, pk_billtype, queryPk_org,scon);
			} else if ("仓库".equals(datatype)) {
				return storDoc(billtype, pk, pk_org, csendstockorgvid);
			} else if ("物料".equals(datatype) || "物料（多版本）".equals(datatype) || "物料编码".equals(datatype)) {
				return meaterialDoc(pk_org, billtype, pk);
			} else if ("物料基本信息（多版本）-业务员".equals(datatype)) {
				return meaterialVPsnDoc(pk_org, userid, billtype, pk);
			} else if ("客户收货地址".equals(datatype)) {
				return custAdressDoc(pk_org, userid, billtype, condVOs, ccustomerid, pk);
			} else if ("营销组织".equals(datatype)) {
				return saleOrgDoc(pk_org, userid, billtype, pk);
			} else if ("营销部门".equals(datatype)) {
				return saleDeptDoc(pk_org, userid, billtype, condVOs, pk);
			} else if ("协同部门".equals(datatype)) {
				return crmDeptDoc(billtype, pk);
			} else if ("所负责门店".equals(datatype)) {
				return org_orgs(userid, billtype, pk);
			} else if ("零售商".equals(datatype)) {
				return shopkeeperDoc(pk_org, userid, billtype, condVOs, pk);
			} else if ("门店".equals(datatype)) {
				return shopcustDoc(pk_org, userid, billtype, condVOs, pk);
			} else if ("开票客户".equals(datatype)) {
				return invoiceCustomer(pk_org, userid, billtype, condVOs, pk);
			} else if ("费用类型".equals(datatype)) {
				return feeTypeDoc(billtype, pk);
			} else if ("会计期间".equals(datatype)||"会计期间档案".equals(datatype)) {
				return accountPeriodDoc(billtype, pk);
			} else if ("收支项目".equals(datatype)) {
				return incomeItemsDoc(pk_billtype, billtype, pk, pk_org);
			} else if ("协同当前集团+业务单元".equals(datatype) || "协同业务单元(当前集团)".equals(datatype)) {
				return cooperGroupOrgDoc(pk_org, billtype, pk, pk_billtype, userid);
			} else if ("集团".equals(datatype)) {
				return groupDoc(billtype, pk);
			} else if ("国家地区".equals(datatype)) {
				return countryRegionDoc(billtype, pk);
			} else if ("协同用户".equals(datatype)) {
				return cooperUserDoc(billtype, pk, pk_org, pk_allorg, pk_billtype, userid, queryPk_org);
			} else if ("CRM业务类型".equals(datatype) || "CRM商机业务类型".equals(datatype)) {
				return CRMBusiTypeDoc(billtype, datatype, pk_billtype, pk);
			} else if ("CRM客户业务类型".equals(datatype)) {
				return CRMCustTypeDoc(billtype, datatype, pk_billtype, pk);
			} else if ("客户基本分类".equals(datatype) || "客户".equals(datatype)) {
				return customerBaseClassDoc(billtype, pk,pk_org);
			} else if ("客户等级".equals(datatype)) {
				return customerLevel(billtype, pk);
			} else if (datatype.contains("地区分类")) {
				return regionTypeDoc(billtype, pk);
			} else if ("CRM客户".equals(datatype)) {
				return CRMCoustomerDoc(billtype, pk_billtype, pk_org, userid, pk, owner, queryPk_org);
			} else if ("省".equals(datatype)) {
				return provinceDoc(billtype, pk);
			} else if ("市".equals(datatype)) {
				return cityDoc(pk_province, billtype, pk);
			} else if ("区县".equals(datatype)) {
				return regionDoc(pk_city, billtype, pk);
			} else if ("乡镇".equals(datatype)) {
				return defDocTown(pk_region, billtype, pk);
			} else if ("经济类型(自定义档案)".equals(datatype)) {
				return defDocEcon(billtype, pk);
			} else if ("协同公共对象".equals(datatype)) {
				return cooperPubObj(billtype, pk);
			} else if ("影响因素交 易类型".equals(datatype) || "出入库类型".equals(datatype)) {// ||"出入库类型".equals(datatype)
				return tradeType(billtype, pk_billtype, pk_group, pk);
			} else if ("收款协议".equals(datatype)) {
				return incomeDoc(billtype, pk);
			} else if ("CRM线索业务类型".equals(datatype)) {
				return CRMXsBusiTypeDoc(billtype, pk);
			} else if ("商机".equals(datatype)) {
				return opportunity(datatype, pk_org, userid, pk);
			} else if ("产品线".equals(datatype)) {
				return prodline(datatype, pk);
			} else if ("结算方式".equals(datatype)) {
				return balatype(datatype, pk);
			} else if ("品牌档案".equals(datatype)) {
				return branddoc(datatype, pk);
			} else if ("币种档案".equals(datatype)) {
				return currtypedoc(datatype, pk);
			} else if ("协同组织".equals(datatype)) {
				return xtOrg(billtype, pk_billtype, pk_org, pk, userid);
			} else if ("全局协同组织".equals(datatype)) {
				return xtOrg(billtype, pk_billtype, null, null, userid);
			} else if ("所属门店".equals(datatype)) {
				return store(billtype, pk, pk_org, pk_billtype, userid);
			} else if ("集团(所有)".equals(datatype)) {
				return groupDoc(billtype, pk);
			} else if ("库存组织版本".equals(datatype) || "库存组织".equals(datatype)) {
				return sendstock(billtype, pk, pk_org);
			} else if ("CRM联系人".equals(datatype)) {
				return contact(billtype, pk_account, pk);
			} else if ("CRM联系人业务类型".equals(datatype)) {
				return CRMLxrBusiTypeDoc(pk_billtype, pk_org, userid, pk);
			} else if ("业务员".equals(datatype)) {
				return saleManDoc(billtype, pk, pk_org, pk_org_financial, userid, pk_billtype, queryPk_org);
			} else if ("订单类型".equals(datatype)) {
				return orderTypeDoc(billtype, pk);
			} else if ("crm客户收货地址".equals(datatype)) {
				return crmCustAdressDoc(pk_org, condVOs, billtype, pk);
			} else if ("crm客户档案".equals(datatype)) {
				return crmCustomerDoc(userid, pk_org_financial, billtype, pk);
			} else if ("销售计划申请单".equals(datatype)) {
				return salePlayApply(userid, billtype, pk);
			} else if ("客户来源(自定义档案)".equals(datatype)) {
				return customfrom(userid, billtype, pk);
			} else if ("投入产出客户".equals(datatype)) {
				return customertrcc(billtype, pk);
			}  else if ("crm收支项目".equals(datatype)) {
				return crmincomeItemsDoc(pk_billtype, billtype, pk);
			} else if (datatype.endsWith("MESSAGE") || datatype.contains("WK_")) {
				return queryApproveBillType(datatype);
			} else if (datatype.endsWith("WORKORG")) {
				return queryWorkNoteOrg();
			} else if ("特征码档案".equals(datatype)) {
				return ffile(datatype, pk, pk_material);
			} else if ("信息项".equals(datatype)) {
				// 收款通知单参照销售订单收款
				return saleOrderFee(pk_org, pk, pk_account);
			} else if ("供应商".equals(datatype) || "供应商档案".equals(datatype)) {
				return goodsAddress1(datatype, pk, pk_org,scon);
			} else if ("税码税率".equals(datatype)) { // zhujiaming
				return taxcode(datatype, pk, pk_org, pk_group);
			} else if ("报销单录入人".equals(datatype)) { // zhujiaming
				// 2018年12月8日12:59:01
				return taxcode3(datatype, pk, pk_org, userid);
			} else if ("费用单位".equals(datatype)) {// zhujiaming
				// 2018年12月8日12:59:01
				return cfanaceorgoid1(billtype, pk_billtype, pk, pk_org, userid);
			} else if ("费用部门".equals(datatype)) { // zhujiaming
				// 2018年12月8日12:59:01
				return deptDoc1(pk_org, billtype, pk);
			} else if ("报销类型".equals(datatype)) {
				return cfanaceorgoid2(billtype, pk_billtype, pk, pk_org, pk_group);
			} else if ("常用摘要".equals(datatype)) {
				return summary(datatype, pk, pk_org, pk_group);
			} else if ("申请摘要".equals(datatype)) {
				return summary2(datatype, pk, pk_org, pk_group);
			} else if ("使用权参照".equals(datatype)) {
				return bankacc(datatype, pk, pk_org, pk_group);
			} else if ("个人银行账户".equals(datatype)) {
				return psnBankacc(datatype, pk, pk_org, userid,receiver);
			} else if ("客商银行账户".equals(datatype)) {
				return custBankacc(datatype, pk, pk_org, userid, pk_cust,hbbm,paytarget);
			}  else if ("单位银行账户".equals(datatype)) {
				return fkyhzh(billtype, pk_billtype, pk, pk_org, userid);
			} else if ("申请单号".equals(datatype)) {
				return mtappCode(billtype, pk_billtype, pk, pk_org, userid, billmaker, cjkybje);
			} else if ("报销单号".equals(datatype)) {
				return jkcode(billtype, pk_billtype, pk, pk_org, userid, pk_busitem, cjkybje, pk_item);
			} else if ("项目".equals(datatype)) {
				return bxbdproject(billtype, pk);
			} else if (datatype.contains("550519e8-69fc-4d16-8d14-0f8e71d1f5aa")) { // 交通工具枚举，标准产品id都一样
				return enumvaluejttools(billtype, pk);
			} else if (datatype.contains("现金账户")) { // 交通工具枚举，标准产品id都一样
				return cashaccount(billtype, pk, pk_org);
			} else if (datatype.contains("发票号")) { // 发票号
				//是否启用 发票报销模块 H013
				UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
				if(booleanFP.booleanValue()){
					return fphDoc(billtype, pk,userid);
				}
			} else if (datatype.contains("发票类型")) { // 发票类型
				return fplxDoc(billtype, pk,pk_billtype);
			}else if ("用户".equals(datatype)) { // zhujiaming
				return smUser(userid, pk_billtype, pk, pk_org); // 2018年12月17日23:59:34
			}else if ("出差审批单号".equals(datatype)) { //东亮  2019年12月26日14:58:04
				return czvbillcode( pk_org,pk_billtype,pk, userid,pk_billtype); // 2018年12月17日23:59:34
			}else if("费用承担部门".equals(datatype)){
				return assume_dept(pk_org,billtype, pk);
			}else if ("签卡原因(自定义档案)".equals(datatype)) {
				return signreason();
			}else if("票夹名".equals(datatype)){
				return fppjbd(userid,pk,billtype);
			}else if("合并票".equals(datatype)){
				return fpbxbd(userid,pk,billtype);
			}else if ("险种".equals(datatype)) {
				return xianzhong(datatype,  pk, pk_org);
			}else if ("证件类别".equals(datatype)) {
				return psnidtype(pk);
			}else if ("申请控制维度".equals(datatype)) {
				return mtappCtrlDIM(pk,pk_billtype);
			}else if ("申请控制对象".equals(datatype)) {
				return mtappCtrlBilltype(pk,billtype);
			}else if ("分摊结转类型".equals(datatype)) {
				return ftjzBilltype(pk,billtype,and);
			}else if("分摊规则对象".equals(datatype)){
				return ftruleType(pk,billtype,and);
			}else if("分摊对象数据".equals(datatype)){
				return sruledata(pk,pk_billtype,and,billtype,pk_org,userid,queryPk_org,scon,datatype);
      		}else if("erm费用类型".equals(datatype)){
				return erm_billtype(pk,billtype,pk_org,defitem14,defitem16); 
			}else if("8eeaabfe-7644-4bd0-a954-f8971a14f079:地点档案".equals(datatype)){
				return bd_addressdoc(pk,billtype,pk_org); 
			}else if("5cc36540-e334-4b67-97bd-d5cc5d143f11".equals(datatype)){
				return xy_invoice(pk,billtype,pk_org); 
			}else if("资金计划项目".equals(datatype)){
				return cashproj(pk,billtype,pk_org); 
			}
		}
		return super.processAction(account, userid, billtype, action, obj);
	}	

	/**
	 * 标准人员档案
	 */
	public RetDefDocVO standPsndoc(String pk_deptdoc,String billtype,String pk,String pk_org) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_psndoc,code,name from bd_psndoc where nvl(dr,0)=0 and pk_psndoc in('" + pk + "')";
		}else if(null!=pk_deptdoc && !"".equals(pk_deptdoc)){
			sql="select distinct bd_psndoc.pk_psndoc, bd_psndoc.code, bd_psndoc.name\n" +
				"  from bd_psndoc\n" + 
				"  left outer join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc\n" + 
				" where enablestate = 2\n" + 
				"   and bd_psnjob.enddutydate = '~' or bd_psnjob.enddutydate is null\n" + 
				"   and bd_psnjob.pk_dept ='"+pk_deptdoc+"'\n" + 
				" order by bd_psndoc.code";
		} else if(null!=pk_org && !"".equals(pk_org)){
			sql= "select distinct bd_psndoc.pk_psndoc, bd_psndoc.code, bd_psndoc.name\n" +
				"  from bd_psndoc\n" + 
				"  left outer join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc\n" + 
				" where enablestate = 2\n" + 
				"   and bd_psnjob.enddutydate = '~' or bd_psnjob.enddutydate is null\n" + 
				"   and bd_psnjob.pk_dept in\n" + 
				"       (select pk_dept\n" + 
				"          from org_dept\n" + 
				"         where (pk_org = '"+pk_org+"' and enablestate = 2))\n" + 
				" order by bd_psndoc.code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	/**
	 * 分摊对象数据-子表参照,分摊规则设置-集团节点
	 */
	public RetDefDocVO sruledata(String pk,String pk_billtype,String and,String billtype,
								 String pk_org,String userid,String queryPk_org,String scon,String datatype) throws BusinessException{
		String sql = null;
		if(and.equals("承担单位")){
			return fanaceorg(billtype, pk_billtype, pk, pk_org, userid);
		}
		if(and.equals("承担部门")){
			return deptDoc(pk_org, billtype, pk); 
		}
		if(and.equals("利润中心")||and.equals("成本中心")){
			return liacenterorg(billtype,pk_billtype,pk, pk_org,userid);
		}
		if(and.equals("收支项目")){
			return incomeItemsDoc(pk_billtype, billtype, pk, pk_org);
		}
		if(and.equals("项目")){
			return bxbdproject(billtype, pk);
		}
		
		if(and.equals("项目任务")){
			return pmwbs(billtype, pk,pk_org);
		}
		if(and.equals("核算要素")){
			return pmwbs(billtype, pk,pk_org);
		}
		if(and.equals("客户")){
			return customerBaseClassDoc(billtype, pk,pk_org);
//			return customerDoc(userid, pk_org, billtype, pk, pk_billtype,queryPk_org,scon);
		}
		if(and.equals("供应商")){
			return goodsAddress1(datatype, pk, pk_org,scon);
		}
		if(and.equals("产品线")){
			return prodline(datatype, pk);
		}
		if(and.equals("品牌")){
			return branddoc(billtype,pk);
		}
		return null;
	}

	private Object cashproj(String pk, String billtype, String pk_org) throws BusinessException {
		String sql =
		        "select bd_crossrulerest.pk_restraint pk_restraint\n" +
		            "  from bd_crossrulerest bd_crossrulerest\n" + 
		            " where nvl(bd_crossrulerest.dr, 0) = 0\n" + 
		            "   and bd_crossrulerest.pk_rule in (select bd_crossrule.pk_rule\n" + 
		            "  from bd_crossrule bd_crossrule\n" + 
		            " where nvl(bd_crossrule.dr, 0) = 0\n" + 
		            "   and bd_crossrule.code in ('ER011'))";
		    String sqls = 
		        "select code, name, pk_fundplan\n" +
		            "  from bd_fundplan\n" + 
		            " where 11 = 11\n" + 
		            "   and pk_fundplan in\n" + 
		            "       ((select bd_fundplan1.pk_fundplan\n" + 
		            "          from bd_fundplan bd_fundplan1\n" + 
		            "         where ((exists\n" + 
		            "                (select 1\n" + 
		            "                    from bd_crossrestdata t\n" + 
		            "                   where bd_fundplan1.pk_fundplan = t.data\n" + 
		            "                     and t.pk_restraint in ("+sql+"))))))\n" + 
		            "   and (enablestate = 2 and (inoutdirect = 0 or inoutdirect = 1))\n" + 
		            "   and (( pk_org = '"+pk_group+"' or\n" + 
		            "       pk_org = '"+pk_org+"') and\n" + 
		            "       (exists (select 1\n" + 
		            "                   from bd_fundplanuse a\n" + 
		            "                  where a.pk_fundplan = bd_fundplan.pk_fundplan\n" + 
		            "                    and a.pk_org = '"+pk_org+"') or\n" + 
		            "        pk_org = '"+pk_org+"'))\n" + 
		            " order by code";
		if(null != pk && !"".equals(pk)){
			sqls = " select code, name, pk_fundplan from bd_fundplan where pk_fundplan in('"+pk+"') and nvl(dr,0) = 0";
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sqls.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	
	private Object xy_invoice(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer(
				"select enumsequence,name,enumsequence from  md_enumvalue where id = '5cc36540-e334-4b67-97bd-d5cc5d143f11' ");
		if(null != pk && !"".equals(pk)){
			sql.append(" and  enumsequence in('" + pk + "')");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	
	
	private Object bd_addressdoc(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer(
				"select bd_addressdoc.pk_addressdoc,\n" +
						"       bd_addressdoc.code,\n" + 
						"       bd_addressdoc.name,\n" + 
						"       bd_areacl.pk_areacl\n" + 
						"  from bd_addressdoc\n" + 
						"  join bd_areacl\n" + 
						"    on bd_addressdoc.pk_areacl = bd_areacl.pk_areacl");
		sql.append(" where ");
		if(null != pk && !"".equals(pk)){
			sql.append(" bd_addressdoc.pk_addressdoc in ('"+pk+"')");
		}else{
			sql.append(" bd_areacl.code like '02%'");
		}
		sql.append("   and isnull(bd_addressdoc.dr,0)= 0\n" +
				   "   and isnull(bd_areacl.dr,0)= 0"+
                   "   and bd_addressdoc.enablestate = 2\n" +
				   "   and bd_areacl.enablestate = 2")	;
		sql.append("   order by bd_addressdoc.code");
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	
	//dongliang 2020-7-15 15:57:54
	private Object erm_billtype(String pk, String billtype, String pk_org,String defitem14,String defitem16) throws BusinessException {
		StringBuffer sql = null;
		if(null != defitem14 && !"".equals(defitem14) && null != defitem16 && !"".equals(defitem16)){
			sql = new StringBuffer(
					"select distinct bd_defdoc.pk_defdoc ,bd_defdoc.code,bd_defdoc.name ,er_reimruler.amount ,er_reimtype.pk_reimtype\n" +
							"  from bd_defdoc bd_defdoc\n" + 
							"  join bd_defdoclist bd_defdoclist\n" + 
							"  on bd_defdoclist.pk_defdoclist = bd_defdoc.pk_defdoclist\n" + 
							"  join er_reimruler er_reimruler\n" + 
							"    on bd_defdoc.pk_defdoc = er_reimruler.def2\n" + 
							"  join er_reimtype er_reimtype\n" + 
							"    on er_reimtype.pk_reimtype = er_reimruler.pk_reimtype\n" + 
					        "  where bd_defdoclist.code = 'ERM001'");
			sql.append(" and er_reimruler.def1 = '"+defitem14+"'");
			sql.append(" and er_reimruler.def3 = '"+defitem16+"'");
			sql.append(" and nvl(bd_defdoc.dr,0)= 0\n" +
					   " and nvl(bd_defdoclist.dr,0)= 0\n" + 
					   " and nvl(er_reimtype.dr,0)= 0\n" + 
					   " and nvl(er_reimruler.dr,0)= 0"+
					   " and er_reimruler.pk_org = '"+pk_org+"'");
			sql.append(" order by bd_defdoc.code");
		}else{
			sql = new StringBuffer(
					"select bd_defdoc.pk_defdoc, bd_defdoc.code, bd_defdoc.name, bd_defdoc.name, bd_defdoc.name\n" +
							"  from bd_defdoc\n" + 
							"  join bd_defdoclist\n" + 
							"    on bd_defdoc.pk_defdoclist = bd_defdoclist.pk_defdoclist\n" + 
							"where bd_defdoclist.code = 'ERM001'\n" + 
							"   and nvl(bd_defdoc.dr, 0) = 0\n" + 
					"   and nvl(bd_defdoclist.dr, 0) = 0");
		}
		
		if(null != pk && !"".equals(pk)){
			sql = new StringBuffer("select pk_defdoc ,code,name  from bd_defdoc where pk_defdoc in ('"+pk+"') and nvl(dr,0) = 0");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				if(null != defitem14 && !"".equals(defitem14) && null != defitem16 && !"".equals(defitem16)){
					defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
					defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));
				}
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	
	
	private Object om_jobF(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer("select  om_job.jobcode, om_job.jobname,om_job.pk_job from om_job om_job where "+
				"  nvl(om_job.dr,0) = 0 ");
		if(null != pk && !"".equals(pk)){
			sql.append(" and om_job.pk_job in ('"+pk+"') " );
		}else{
			sql.append(" and om_job.enablestate = 2 ");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	
	/**
	 * 分摊规则对象,分摊规则设置-集团节点
	 */
	public RetDefDocVO pmwbs(String billtype,String pk,String pk_org ) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select wbs_code, wbs_name, pk_wbs from pm_wbs where pk_wbs in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql="select wbs_code, wbs_name, pk_wbs\n" +
				"  from pm_wbs\n" + 
				" where pk_org in ('"+pk_org+"')\n" + 
				"  and enablestate = 2\n" + 
				"  and (childprojectflag <> 'Y' or childprojectflag is null)\n" + 
				"  and nvl(dr,0) = 0\n" + 
				" order by wbs_code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	/**
	 * 分摊规则对象,分摊规则设置-集团节点
	 */
	public RetDefDocVO ftruleType(String pk,String billtype,String and) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select fieldcode,fieldname from er_sruleobj where fieldname in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql="select distinct replace(fullpath,'sruledata.','') as fullpath  , displayname\n" +
				"  from md_attr_power\n" + 
				"  join (select displayname,case when tableid='er_sruledata' then 'sruledata.'||name end as name\n" + 
				"          from md_column where tableid in ('er_sruledata')) col on col.name=md_attr_power.fullpath\n" + 
				" where beanid = 'cf21a746-807a-4cf8-911f-f397529ee06e'\n" + 
				"   and powertype = 'erm'\n" + 
				" order by fullpath";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	
	/**
	 * 借款交易类型,借款控制设置节点
	 */
	public RetDefDocVO ftjzBilltype(String pk,String billtype,String and) throws DAOException{
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_billtypecode, billtypename, pk_billtypeid from bd_billtype where pk_billtypeid in('" + pk + "')";
		} else {
			sql="select pk_billtypecode, billtypename, pk_billtypeid\n" +
				"  from bd_billtype\n" + 
				" where istransaction = 'Y'\n" + 
				"  and pk_group = '"+pk_group+"'\n" + 
				"  and nvl(islock, 'N') = 'N'\n" + 
				" "+and+"\n" + 
				" order by pk_billtypecode";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	
	/**
	 * 借款交易类型,借款控制设置节点
	 */
	public RetDefDocVO jkBilltype(String pk,String billtype) throws DAOException{
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select distinct PK_BILLTYPECODE, BILLTYPENAME from bd_billtype where PK_BILLTYPECODE in('" + pk + "')";
		} else {
			sql="select distinct bd_billtype.PK_BILLTYPECODE, bd_billtype.BILLTYPENAME\n" +
				"  from bd_billtype\n" + 
				" where ((pk_billtypecode in ('2631', '2632') or (pk_billtypecode like '263X-%'))\n" + 
				"   and islock = 'N'\n" + 
				"   and billtypename is not null\n" + 
				"   and (pk_group = '"+pk_group+"'))\n" + 
				" order by bd_billtype.PK_BILLTYPECODE";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	
	/**
	 * 申请控制维度
	 */
	public RetDefDocVO mtappCtrlBilltype(String pk,String billtype) throws DAOException{
		String groupid = InvocationInfoProxy.getInstance().getGroupId();
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_billtypecode, billtypename, pk_billtypeid from bd_billtype where pk_billtypeid in('" + pk + "')";
		} else {
			sql="select pk_billtypecode, billtypename, pk_billtypeid\n" +
				"  from bd_billtype\n" + 
				" where (istransaction = 'Y'\n" + 
				" and pk_group = '"+groupid+"'\n" + 
				" and nvl(islock, 'N') = 'N'\n" + 
				" and parentbilltype in ('263X', '264X')\n" + 
				" and pk_billtypecode not in ('2647', '264a')\n" + 
				" and istransaction = 'Y'\n" + 
				" and islock = 'N'\n" + 
				" and (pk_group = '"+groupid+"')\n" + 
				" and pk_billtypecode not in\n" + 
				"       (select djlxbm\n" + 
				"           from er_djlx\n" + 
				"          where djdl in ('jk', 'bx')\n" + 
				"            and bxtype = 2\n" + 
				"            and pk_group = '"+groupid+"'))\n" + 
				" order by pk_billtypecode";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 申请控制维度
	 */
	public RetDefDocVO mtappCtrlDIM(String pk,String billtype) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select fieldcode,fieldname from er_mtapp_cfield where fieldname in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql="select replace(fullpath,'mtapp_bill.','') as fullpath ,\n" +
				"       case when instr(fullpath,'mtapp_detail.')>0 then '费用申请单明细.'||displayname else displayname end as displayname\n" + 
				"  from md_attr_power\n" + 
				"  join (select displayname,case when tableid='er_mtapp_bill' then 'mtapp_bill.'||name\n" + 
				"               when tableid='er_mtapp_detail' then 'mtapp_bill.mtapp_detail.'||name end as name\n" + 
				"          from md_column where tableid in ('er_mtapp_bill','er_mtapp_detail')) col on col.name=md_attr_power.fullpath\n" + 
				" where beanid = 'e3167d31-9694-4ea1-873f-2ffafd8fbed8'\n" + 
				"   and powertype = 'erm'\n" + 
				"   and fullpath <> 'mtapp_bill.mtapp_detail'\n" + 
				" order by fullpath";
			if(null!=billtype && (billtype.startsWith("264") || billtype.startsWith("265"))){
				sql="select distinct replace(fullpath,'bxzb.costsharedetail.','csharedetail.') as fullpath  ,\n" +
					"       case when instr(fullpath,'bxzb.costsharedetail.')>0 then '费用分摊明细.'||displayname else displayname end as displayname\n" + 
					"  from md_attr_power\n" + 
					"  join (select displayname,case when tableid='er_cshare_detail' then 'bxzb.costsharedetail.'||name end as name\n" + 
					"          from md_column where tableid in ('er_cshare_detail')) col on col.name=md_attr_power.fullpath\n" + 
					" where beanid = 'd9b9f860-4dc7-47fa-a7d5-7a5d91f39290'\n" + 
					"   and powertype = 'erm'\n" + 
					" order by fullpath";
			}
			//分摊对象
			if(null!=billtype && billtype.startsWith("rule_data")){
				sql="select distinct replace(fullpath,'sharerule.rule_data.','rule_data.') as fullpath  ,\n" +
					"       case when instr(fullpath,'sharerule.rule_data.')>0 then '分摊数据.'||displayname else displayname end as displayname\n" + 
					"  from md_attr_power\n" + 
					"  join (select displayname,case when tableid='er_sruledata' then 'sharerule.rule_data.'||name end as name\n" + 
					"          from md_column where tableid in ('er_sruledata')) col on col.name=md_attr_power.fullpath\n" + 
					" where beanid = '8c16817a-0d13-49ef-930c-fb3a7f932cd8'\n" + 
					"   and powertype = 'erm'\n" + 
					" order by fullpath";
			}
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
				if(null!=billtype && (billtype.startsWith("264") || billtype.startsWith("265"))){
					defDocVOs[i].setDef1("er_cshare_detail");
				}
				if(null!=billtype && billtype.startsWith("261")){
					defDocVOs[i].setDef1("mtapp_detail");
				}
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	/**
	 * 证件类别
	 * 
	 * @author heyang 修改时间：2020-06-28 17:25
	 */
	public RetDefDocVO psnidtype(String pk) throws DAOException {
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_identitype,code,name from bd_psnidtype where  pk_identitype in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql = "select pk_identitype,code,name\n" + "from bd_psnidtype\n" + "where nvl(dr,0)=0\n" + "order by code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();

				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	/**
	 * 发票档案
	 */
	public RetDefDocVO fppjbd(String userid,String pk,String billtype) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select name,pk_fppj from erm_fppj where pk_fppj in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql="select erm_fppj.name,erm_fppj.pk_fppj\n" +
				"from erm_fpbd\n" + 
				"join erm_fppj on erm_fppj.pk_fpbd=erm_fpbd.pk_fpbd and nvl(erm_fppj.dr,0)=0\n" + 
				"where nvl(erm_fpbd.dr,0)=0\n" +
				"and erm_fpbd.cuserid='"+userid+"'\n" +
				"order by erm_fppj.rowno"; 
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	/**
	 * 合并票，即多张发票合并到一起，报销
	 */
	public RetDefDocVO fpbxbd(String userid,String pk,String billtype) throws DAOException{
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select name,pk_fpbx from erm_fpbx where pk_fpbx in('" + pk + "') and isnull(dr,0)=0";
		} else {
			sql="select erm_fpbx.name,erm_fpbx.pk_fpbx\n" +
				"from erm_fpbd\n" + 
				"join erm_fpbx on erm_fpbx.pk_fpbd=erm_fpbd.pk_fpbd and nvl(erm_fpbx.dr,0)=0\n" + 
				"where isnull(erm_fpbd.dr,0)=0\n" +
				"and erm_fpbd.cuserid='"+userid+"'\n" +
				"order by erm_fpbx.ts"; 
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	/**
	 * 利润中心
	 */
	public RetDefDocVO liacenterorg(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_liabilitycenter,code,name from org_liacenter where enablestate = 2 and pk_liabilitycenter in('" + pk + "')";
		} else {
			sql="select pk_liabilitycenter,code,name from org_liacenter where enablestate = 2 and pk_group = '"+pk_group+"'";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	
	/**
	 *  财务组织
	 */
	public RetDefDocVO fanaceorg (String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_financeorg,code,name from org_financeorg where isnull(dr,0)=0 and pk_financeorg  in('" + pk + "')";
		} else {
			sql="select pk_financeorg, code, name\n" +
				"  from (SELECT org_corp.code,\n" + 
				"               org_corp.name,\n" + 
				"               org_corp.pk_corp pk_financeorg,\n" + 
				"               org_corp.pk_fatherorg,\n" + 
				"               org_corp.pk_corp,\n" + 
				"               org_corp.pk_group,\n" + 
				"               org_financeorg.enablestate\n" + 
				"          FROM org_corp\n" + 
				"          LEFT outer JOIN org_financeorg ON org_corp.pk_corp = org_financeorg.pk_financeorg\n" + 
				"         WHERE org_corp.pk_corp IN\n" + 
				"               (SELECT pk_org\n" + 
				"                  FROM org_orgs\n" + 
				"                 WHERE orgtype5 = 'Y'\n" + 
				"                   and org_financeorg.enablestate = 2\n" + 
				"                   and org_financeorg.pk_group = '"+pk_group+"')\n" + 
				"        UNION\n" + 
				"        SELECT org_financeorg.code,\n" + 
				"               org_financeorg.name,\n" + 
				"               org_financeorg.pk_financeorg,\n" + 
				"               org_orgs.pk_corp pk_fatherorg,\n" + 
				"               org_orgs.pk_corp,\n" + 
				"               org_financeorg.pk_group,\n" + 
				"               org_financeorg.enablestate\n" + 
				"          FROM org_financeorg\n" + 
				"          LEFT outer JOIN org_orgs ON org_financeorg.pk_financeorg = org_orgs.pk_org\n" + 
				"         WHERE nvl(org_financeorg.dr, 0) = 0\n" + 
				"           and orgtype5 = 'Y'\n" + 
				"           and org_financeorg.enablestate = 2\n" + 
				"           and org_financeorg.pk_group = '"+pk_group+"'\n" + 
				"           AND org_financeorg.pk_financeorg <> org_orgs.pk_corp) org_financeorg_temp\n" + 
				" order by code";

		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				/*if (null == pk || "".equals(pk)) {
					defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
					defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				}*/
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	/**
	 * 1.业务单元  
	 */
	public RetDefDocVO orgoid(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = null;
		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer("select pk_financeorg,code,name from org_financeorg where isnull(dr,0)=0 and pk_financeorg  in('" + pk + "')");
		} else {
			//union all 上半部分有权限的单位，下半部分人员档案中任职单位
			querySQL = new StringBuffer(
							"select distinct pk_org,code,name \n" +
							"from (\n" + 
							"      select org.*\n" + 
							"       from sm_subject_org subject\n" + 
							"      inner join org_orgs org on subject.pk_org = org.pk_org\n" + 
							"      where subjectid in\n" + 
							"            (select pk_role\n" + 
							"               from sm_user_role\n" + 
							"              where cuserid = '"+userid+"')\n" + 
							"        and org.isbusinessunit = 'Y'\n" + 
							"        and org.enablestate = 2\n" + 
							"      union all\n" + 
							"      select org_orgs.*\n" + 
							"      from bd_psnjob psnjob\n" + 
							"      join sm_user sm  on psnjob.pk_psndoc = sm.pk_psndoc\n" + 
							"      join org_orgs  org_orgs  on org_orgs.pk_org = psnjob.pk_org\n" + 
							"      where nvl(psnjob.dr, 0) = 0\n" + 
							"      and nvl(sm.dr, 0) = 0\n" + 
							"      and nvl(org_orgs.dr, 0) = 0\n" + 
							"      and org_orgs.isbusinessunit = 'Y'\n" + 
							"      and sm.cuserid = '"+userid+"'\n" + 
							")\n" + 
							"order by code" );
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				/*if (null == pk || "".equals(pk)) {
					defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
					defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				}*/
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	 
	/**
	 * 1.行政组织  
	 */
	public RetDefDocVO adminorg(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		String sql = null;
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_adminorg, code, name, pk_vid,pk_fatherorg from org_adminorg where isnull(dr,0)=0 and pk_adminorg  in('" + pk + "')";
		} else {
			sql = "select pk_adminorg, code, name, pk_vid,pk_fatherorg\n" +
				"  from org_adminorg\n" + 
				" where nvl(dr,0)=0\n" + 
				"   and (enablestate = 1 or enablestate = 2 or enablestate = 3)\n" + 
				"   and pk_group = '"+pk_group+"'\n" + 
				" order by displayorder, code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				/*if (null == pk || "".equals(pk)) {
					defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
					defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				}*/
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	//东亮 2020-2-21 20:35:45 费用承担部门
	public RetDefDocVO assume_dept(String pk_org, String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer();

		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer("select pk_dept,code,name from org_dept where dr = 0  and pk_dept in('" + pk + "') and enablestate = 2 ");
		} else {
			querySQL = new StringBuffer("SELECT pk_dept,\n" + "	code,\n" + "	name,\n" + "	mnecode,\n" + "	pk_fatherorg,\n" + "	displayorder,\n" + "	innercode,\n" + "	pk_org\n" + "FROM\n" + "	org_dept\n" + "WHERE\n" + "	11 = 11\n" + "AND (\n" + "	(\n" + "		enablestate = 1\n"
					+ "		OR enablestate = 2\n" + "		OR enablestate = 3\n" + "	)\n" + ")\n" + "AND (\n" + "	(\n" + "		pk_group = '"+pk_group+"'\n" + "		AND pk_org = '" + pk_org + "'\n" + "	)\n" + ")\n" + "ORDER BY\n" + "	displayorder,\n" + "	code");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO; 
	} 

	public RetDefDocVO czvbillcode(String pk_org, String billtype, String pk,String userid,String pk_billtype) throws BusinessException {
		// 申请单参照
		StringBuffer querySQL = new StringBuffer( 
				"select distinct er_mtapp_detail.pk_mtapp_bill pk_mtapp_bill,\n" +
						"                er_mtapp_detail.billno        billno,\n" + 
						"                er_mtapp_detail.billdate      billdate," +
						"                er_mtapp_detail.assume_org    assume_org,\n" + 
						"                er_mtapp_bill.rest_amount     rest_amount,\n" + //可用金额    	
						"                er_mtapp_detail.apply_dept    apply_dept,\n" + 
						"                er_mtapp_detail.billmaker     billmaker,\n" +
						"                er_mtapp_bill.defitem13       defitem13,\n"+ 
						"                er_mtapp_bill.defitem14       defitem14,\n"+ 
						"                er_mtapp_bill.defitem27       defitem27,\n"+//9
						"                er_mtapp_bill.defitem26       defitem26,\n"+
						"                er_mtapp_bill.attach_amount   attach_amount, \n"+
						"                er_mtapp_bill.pk_org          pk_org ,\n"+
						"                er_mtapp_bill.apply_org       apply_org , \n"+
						"                er_mtapp_bill.apply_dept      apply_dept \n"+
						"  from er_mtapp_detail er_mtapp_detail\n" + 
						"  join er_mtapp_bill er_mtapp_bill\n" + 
						"    on er_mtapp_detail.pk_mtapp_bill = er_mtapp_bill.pk_mtapp_bill\n" + 
						" where er_mtapp_detail.orig_amount >\n" + 
						"       nvl((SELECT sum(p.exe_amount + p.pre_amount)\n" + 
						"             FROM er_mtapp_billpf p\n" + 
						"            WHERE p.pk_mtapp_detail = er_mtapp_detail.pk_mtapp_detail\n" + 
						"              and p.pk_djdl = 'bx'\n" + 
						"            GROUP BY p.pk_mtapp_detail),\n" + 
						"           0)\n" + 
						"   and er_mtapp_detail.billmaker in\n" + 
						"       (select pk_psndoc from sm_user where cuserid = '"+userid+"')\n" + 
						"   and er_mtapp_detail.pk_tradetype = '"+pk_billtype+"'\n" +
						"   and er_mtapp_bill.pk_org = '"+pk_org+"'" +
						//"   or er_mtapp_bill.billmaker = '"+userid+"'" + 
						"   and er_mtapp_detail.effectstatus = 1\n" + 
						"   and er_mtapp_detail.close_status = 2\n" + 
				"   and er_mtapp_detail.dr = 0\n");
		//投标付款单参照投标审批单  授权代理设置 总部对应分公司
		/**
		 * 有授权代理的人员可以总部付款，也可以分公司付款
		 * 没有授权代理的人员只能分公司付款
		 */
		String pk_roler = "1001D1100000000507TP"; 
		if("261X-Cxx-09".equals(pk_billtype)){
			// 申请单参照
			querySQL = new StringBuffer(
					"select distinct er_mtapp_detail.pk_mtapp_bill pk_mtapp_bill,\n" +
							"                er_mtapp_detail.billno        billno,\n" + 
							"                er_mtapp_detail.billdate      billdate,\n" + 
							"                er_mtapp_detail.assume_org   assume_org,\n" + 
							"                er_mtapp_bill.rest_amount     rest_amount,\n" + 
							"                er_mtapp_detail.apply_dept    apply_dept,\n" + 
							"                er_mtapp_detail.billmaker     billmaker,\n" + 
							"                er_mtapp_bill.defitem13       defitem13,\n" + 
							"                er_mtapp_bill.defitem14      defitem14,\n" + 
							"                er_mtapp_bill.defitem30      defitem30,\n"+//9
							"                er_mtapp_bill.defitem29      defitem29,\n"+
							"                er_mtapp_bill.defitem28      defitem28,\n"+
							"                er_mtapp_bill.defitem27      defitem27,\n"+
							"                er_mtapp_bill.defitem14      defitem14,\n"+
							"                er_mtapp_bill.pk_customer    pk_customer,\n"+
							"                er_mtapp_bill.defitem26      defitem26,\n"+
							"                er_mtapp_detail.assume_dept  assume_dept, \n"+//16
							"                er_mtapp_bill.pk_org         pk_org, \n"+
							"                er_mtapp_bill.attach_amount  attach_amount, \n"+
							"                er_mtapp_bill.apply_org      apply_org \n"+
							"  from er_mtapp_detail er_mtapp_detail\n" + 
							"  join er_mtapp_bill er_mtapp_bill\n" + 
							"    on er_mtapp_detail.pk_mtapp_bill = er_mtapp_bill.pk_mtapp_bill\n" + 
							" where er_mtapp_detail.orig_amount >\n" + 
							"       nvl((SELECT sum(p.exe_amount + p.pre_amount)\n" + 
							"             FROM er_mtapp_billpf p\n" + 
							"            WHERE p.pk_mtapp_detail = er_mtapp_detail.pk_mtapp_detail\n" + 
							"              and p.pk_djdl = 'bx'\n" + 
							"            GROUP BY p.pk_mtapp_detail),\n" + 
							"           0)\n" + 
					"   and er_mtapp_detail.billmaker in\n" ); 
			//东亮 2020-2-22 17:06:20 角色名称：报销审核（）
			String sql = 
					"select sm_role.pk_role \n" +
							"from sm_user\n" + 
							"join sm_user_role on sm_user_role.cuserid=sm_user.cuserid and nvl(sm_user_role.dr,0)=0\n" + 
							"join sm_role      on sm_role.pk_role=sm_user_role.pk_role and nvl(sm_role.dr,0)=0\n" + 
							"where nvl(sm_user.dr,0)=0\n" + 
							"and sm_user.cuserid='"+userid+"'and sm_role.pk_role = '"+pk_roler+"'";
			ArrayList<RoleVO> rolevoList = (ArrayList<RoleVO>) dao.executeQuery(sql, new BeanListProcessor(RoleVO.class));
			if(null != rolevoList && rolevoList.size() > 0){
				querySQL.append(	"   (select pk_user\n" + 
						"                  from er_indauthorize\n" +  
						"                 where type = 0\n" + 
						"                   and keyword = 'busiuser'\n" + 
						"                   and pk_roler in\n" + 
						"                       ('"+pk_roler+"'))\n" + 
						//"   and er_mtapp_detail.pk_tradetype = '"+pk_billtype+"'\n" +
						"   and er_mtapp_bill.pk_org = '"+pk_org+"'" + 
						"   and er_mtapp_detail.effectstatus = 1\n" + 
						"   and er_mtapp_detail.close_status = 2\n" +
						"   or er_mtapp_bill.creator = '"+userid+"'" +
						"   and er_mtapp_bill.billstatus = 3" +
						"   and nvl(er_mtapp_bill.dr,0) = 0" +
						"   and nvl(er_mtapp_detail.dr,0) = 0" +
						"   and er_mtapp_bill.pk_tradetype = '"+pk_billtype+"'" + 
						"   and er_mtapp_detail.dr = 0 ");
			}else{				
				querySQL.append("  (select pk_psndoc from sm_user where cuserid = '"+userid+"')\n" + 
						"   and er_mtapp_detail.pk_tradetype = '"+pk_billtype+"'\n" +
						"   and er_mtapp_bill.pk_org = '"+pk_org+"'" + 
						"   and er_mtapp_detail.effectstatus = 1\n" + 
						"   and er_mtapp_detail.close_status = 2\n" + 
						"   and er_mtapp_detail.dr = 0 ");

			}

		}
		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer(
			 "SELECT er_mtapp_detail.pk_mtapp_bill,er_mtapp_detail.billno FROM er_mtapp_bill er_mtapp_detail WHERE er_mtapp_detail.pk_mtapp_bill in ('" + pk + "') and nvl(er_mtapp_detail.dr,0) = 0 ");
		} 
		querySQL.append(" order by er_mtapp_detail.billdate desc");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			if (null == pk || "".equals(pk)) {
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(obj2[2].toString().substring(0,10))); // 日期
				defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3])); // 申请单位
				defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4])); // 表头金额
				defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[5])); // 部门
				defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(obj2[6]));
				defDocVOs[i].setDef9(PuPubVO.getString_TrimZeroLenAsNull(obj2[10]));
				defDocVOs[i].setDef10(PuPubVO.getString_TrimZeroLenAsNull(obj2[11]));
				defDocVOs[i].setDef11(PuPubVO.getString_TrimZeroLenAsNull(obj2[12]));
				defDocVOs[i].setDef12(PuPubVO.getString_TrimZeroLenAsNull(obj2[13]));
				defDocVOs[i].setDef13(PuPubVO.getString_TrimZeroLenAsNull(obj2[14]));
				if("261X-Cxx-04".equals(pk_billtype)){
					defDocVOs[i].setDef7(PuPubVO.getString_TrimZeroLenAsNull(obj2[7]));
				}
				//大额审批单和固定资产采购审批单的备注
				else if(("261X-Cxx-10".equals(pk_billtype))||("261X-Cxx-11".equals(pk_billtype))){
					defDocVOs[i].setDef7(PuPubVO.getString_TrimZeroLenAsNull(obj2[9]));
				}
				//东亮 2020-2-20 10:39:14
				else if("261X-Cxx-09".equals(pk_billtype)){
					if("0001C110000000000I63".equals(pk_org)){
						defDocVOs[i].setDef3("1001C11000000000DC6W"); // 费用承担部门
					}
					defDocVOs[i].setDef8(PuPubVO.getString_TrimZeroLenAsNull(obj2[9]));
					defDocVOs[i].setDef9(PuPubVO.getString_TrimZeroLenAsNull(obj2[10]));
					defDocVOs[i].setDef10(PuPubVO.getString_TrimZeroLenAsNull(obj2[11]));
					defDocVOs[i].setDef7(PuPubVO.getString_TrimZeroLenAsNull(obj2[12]));
					defDocVOs[i].setDef12(PuPubVO.getString_TrimZeroLenAsNull(obj2[13]));
					defDocVOs[i].setDef13(PuPubVO.getString_TrimZeroLenAsNull(obj2[14]));
					defDocVOs[i].setDef14(PuPubVO.getString_TrimZeroLenAsNull(obj2[15]));
					defDocVOs[i].setDef15(PuPubVO.getString_TrimZeroLenAsNull(obj2[17]));
					defDocVOs[i].setDef16(PuPubVO.getString_TrimZeroLenAsNull(obj2[18]));
					defDocVOs[i].setDef17(PuPubVO.getString_TrimZeroLenAsNull(obj2[19]));
				}else{ 
					defDocVOs[i].setDef7(PuPubVO.getString_TrimZeroLenAsNull(obj2[8]));
				}

			}else{
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}

			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + "【" + defDocVOs[i].getDef6() + "】【" + defDocVOs[i].getDef2() + "】【" + defDocVOs[i].getDef7() + "】");
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
	/**
	 * 用户列表
	 */
	public RetDefDocVO smUser(String userid, String pk_billtype, String pk, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer("select user_code,user_name,cuserid from sm_user where isnull(dr,0)=0 and user_type = 1 and enablestate = 2 and cuserid ='" + userid + "'");
		if (null != pk_org) {
			sql = new StringBuffer("select user_code,user_name,cuserid from sm_user where isnull(dr,0)=0 and user_type = 1 and enablestate = 2 and pk_org ='" + pk_org + "'");
		}
		if(null!=pk){
			sql = new StringBuffer("select user_code,user_name,cuserid from sm_user where isnull(dr,0)=0 and cuserid in('" + pk + "')");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(pk_billtype);
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setShowvalue(psnojb[0] + defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}
 
  	private Object fplxDoc(String billtype, String pk,String pk_billtype) throws BusinessException {
		StringBuffer querySQL = null;
		if(null != pk_billtype && "fplx".equals(pk_billtype)){
			querySQL = new StringBuffer(" select distinct fplx,fplx,fplx from erm_fpgl  where nvl(dr,0)=0");
			if(null != pk && !"".equals(pk)){
				querySQL.append(" and fplx in ('"+pk+"')");
			}
		}else{
			querySQL = new StringBuffer("select bd_defdoc.PK_DEFDOC,bd_defdoc.name from " + "bd_defdoc join bd_defdoclist " + "on bd_defdoclist.pk_defdoclist=bd_defdoc.pk_defdoclist " + "and nvl(bd_defdoclist.dr,0)=0 " + "where nvl(bd_defdoc.dr,0)=0 and bd_defdoclist.code = 'ZDY022'");
			if(null != pk && !"".equals(pk)){
				querySQL.append(" and bd_defdoc.pk_defdoc in ('"+pk+"')");
			}
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;

		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));

				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}
  	
  	//处理合并发票 
  	public MDefDocVO[] invoiceHbName(String pk)throws BusinessException{
  		StringBuffer querySQL = new StringBuffer(
					"select pk_fpgl, fplx, fphm, money\n" +
							"  from erm_fpgl\n" + 
							" where nvl(dr, 0) = 0 and pk_fpgl in ('"+pk+"') ");
  		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
  		if (list.size() == 0 && (null == pk || "".equals(pk))) {
  			return null;
  		}
  		MDefDocVO[] defDocVOs = null;
  		if(pk.contains(",")){
  			StringBuffer invoiceIDs = new StringBuffer();
  	  		StringBuffer invoiceCodes = new StringBuffer();
  	  		String invoicePK = "";
  	  		String invoiceCode = "";
  	  		for (int i = 0; i < list.size(); i++) {
  	  			invoiceIDs.append(getString_ZeroLen(list.get(i)[0].toString())).append(",");
  	  			invoiceCodes.append(getString_ZeroLen(list.get(i)[2].toString())).append(",");
  	  			
  	  		}
  	  		invoicePK = invoiceIDs.substring(0,invoiceIDs.length()-1);
  			invoiceCode = invoiceCodes.substring(0,invoiceCodes.length()-1);
  			//通过判断","个数 ，以此判断pk和code数量是否匹配
  			int invoiceIdNum = invoicePK.length() - invoicePK.replace(",", "").length();
  			int invoiceIdCode = invoiceCode.length() - invoiceCode.replace(",", "").length();
  			if(invoiceIdNum != invoiceIdCode){
  				throw new BusinessException("发票号数量不匹配！");
  			}
  			defDocVOs = new MDefDocVO[1];
  			defDocVOs[0] = new MDefDocVO();
  			defDocVOs[0].setPk(invoicePK);
  			defDocVOs[0].setCode(invoiceCode);
  			defDocVOs[0].setName(invoiceCode);
  			defDocVOs[0].setShowvalue(defDocVOs[0].getName());
  		}else{
  			if (list != null && list.size() > 0) {
  	  			defDocVOs = new MDefDocVO[list.size()];
  	  			for (int i = 0; i < list.size(); i++) {
  	  				Object[] obj2 = list.get(i);
  	  				defDocVOs[i] = new MDefDocVO();
  	  				defDocVOs[i].setPk(getString_ZeroLen(obj2[0]));
  	  				defDocVOs[i].setName(getString_ZeroLen(obj2[2]) == null ? "无" : PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
  	  				defDocVOs[i].setCode(getString_ZeroLen(obj2[2]));
  	  			    defDocVOs[i].setShowvalue(defDocVOs[0].getName());
  	  			}
  	  		}
  		}
  		
  		return defDocVOs;
  		
  	}

	/**
	 * 1. 发票号 
	 */
  	public RetDefDocVO fphDoc(String billtype, String pk,String userid) throws BusinessException {
  		//合并发票要另处理下
  		if(null != pk && !"".equals(pk)){
  			MDefDocVO[] defDocVOs =  invoiceHbName(pk);
  			RetDefDocVO refDocVO = new RetDefDocVO();
  			if(null != defDocVOs && defDocVOs.length > 0){
  				refDocVO.setDefdocvos(defDocVOs);
  				return refDocVO;
  			}
  			return null;
  		}else{
  			//定额发票money是null  这里显示 价税合计金额
  			StringBuffer querySQL = new StringBuffer(
  					"select pk_fpgl, fplx, fphm, (case when cyzt= '此票类型不支持查验' then jshj else money end )money\n" +
  							"  from erm_fpgl\n" + 
  							" where nvl(dr, 0) = 0\n" + 
  							"   and erm_fpgl.FPLX is not null\n" + 
  							"   and erm_fpgl.FPHM is not null\n" + 
  							"   and billmaker = '"+userid+"' and cyzt <> '作废' \n" );
  			//是否启用业务参数 H014 Y 启用
  			UFBoolean booleanFP = new FeeBaseAction().sysinitFPCheck();
  			if(booleanFP.booleanValue()){
  				querySQL.append("   and cyzt <> '未查验' ");
  			}
  			querySQL.append("   and billversionpk is null ");
  			/*if(null != pk && !"".equals(pk)){
  			querySQL = new StringBuffer(
  					"select pk_fpgl, fplx, fphm, money\n" +
  							"  from erm_fpgl\n" + 
  							" where nvl(dr, 0) = 0 and pk_fpgl in ('"+pk+"')");
  		}*/
  			ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
  			if (list.size() == 0 && (null == pk || "".equals(pk))) {
  				return null;
  			}
  			MDefDocVO[] defDocVOs = null;
  			if (list != null && list.size() > 0) {
  				defDocVOs = new MDefDocVO[list.size()];
  				for (int i = 0; i < list.size(); i++) {
  					Object[] obj2 = list.get(i);
  					defDocVOs[i] = new MDefDocVO();
  					defDocVOs[i].setBilltype(billtype);
  					defDocVOs[i].setPk(getString_ZeroLen(obj2[0]));
  					defDocVOs[i].setName(getString_ZeroLen(obj2[2]) == null ? "无" : PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
  					defDocVOs[i].setDef1(getString_ZeroLen(obj2[1]));
  					defDocVOs[i].setDef2(getString_ZeroLen(obj2[3]));
  					defDocVOs[i].setShowvalue(defDocVOs[i].getDef1() + "    " + defDocVOs[i].getName() + "    " + defDocVOs[i].getDef2());
  				}
  			}
  			RetDefDocVO refDocVO = new RetDefDocVO();
  			if(null != defDocVOs && defDocVOs.length > 0){
  				refDocVO.setDefdocvos(defDocVOs);
  				return refDocVO;
  			}
  			return null;
  		}
  	}

	// ==================================================================================
	// ==================================================================================
	/**
	 * 1. 部门档案
	 */
	public RetDefDocVO deptDoc(String pk_org, String billtype, String pk) throws BusinessException {
		/*
		 * String condition =
		 * " isnull(dr,0)=0  and enablestate  = 2  and pk_dept in (\n" +
		 * " SELECT bd_crossrestdata.data AS data\n" +
		 * " FROM bd_crossrestdata bd_crossrestdata\n" +
		 * " WHERE bd_crossrestdata.pk_restraint IN (SELECT bd_crossrulerest.pk_restraint \n"
		 * + " FROM bd_crossrulerest bd_crossrulerest\n" +
		 * " WHERE bd_crossrulerest.pk_rule IN (SELECT top 1 pk_rule\n" +
		 * " FROM bd_crossrule bd_crossrule\n" + " WHERE pk_org IN ( '" + pk_org
		 * + "' )\n" + " AND bd_crossrule.enablestate = 2 \n" +
		 * " and name like '部门%' ))) order by code ";
		 */
		String condition = null;
		if(null != pk_org && !"".equals(pk_org)){
			 condition = "pk_org = '" + pk_org + "' and isnull(dr,0)=0  and enablestate  = 2 order by code ";
		}else{
			 condition = " isnull(dr,0)=0  and enablestate  = 2 order by code ";
		}
		ArrayList<DeptVO> list = new ArrayList<DeptVO>();
		if (null != pk && !"".equals(pk)) {
			//pk = getPKs(pk);
			list = (ArrayList<DeptVO>) dao.retrieveByClause(DeptVO.class, " isnull(dr,0)=0 and pk_dept in ('" + pk + "')  ");
			if (list.size() == 0) {
				list = (ArrayList<DeptVO>) dao.retrieveByClause(DeptVO.class, " isnull(dr,0)=0 and pk_vid in('" + pk + "') ");
			}
		} else {
			list = (ArrayList<DeptVO>) dao.retrieveByClause(DeptVO.class, condition);
		}
		/*
		 * if (list.size() == 0 && (null == pk || "".equals(pk))) { throw new
		 * BusinessException("没有数据!"); }
		 */
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				DeptVO deptdocVO = (DeptVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_dept());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null; 
	}

	/**
	 * 2.人员档案
	 */
	public RetDefDocVO psnDoc(String pk_deptdoc, String billtype, String pk, String pk_org) throws BusinessException {
		UFDate date = new UFDate();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String time = sdf.format(date);
		StringBuffer sql = new StringBuffer("select  bd_psndoc.code,bd_psndoc.name," + "tbm_psndoc.enddate,org_adminorg.name,org_dept.name,bd_psndoc.pk_group,bd_psndoc.pk_org,bd_psndoc.pk_psndoc,hi_psnjob.pk_psnorg,"
				+ "hi_psnjob.pk_psnjob,hi_psnjob.pk_psncl,hi_psnjob.pk_org,hi_psnjob.pk_dept,hi_psnjob.pk_job,hi_psnjob.pk_post," + "org_adminorg.PK_VID as pk_org_v,org_dept.pk_vid as pk_dept_v from   tbm_psndoc inner join hi_psnjob on "
				+ "tbm_psndoc.pk_psnjob = hi_psnjob.pk_psnjob  inner join hi_psnorg on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg" + " inner join bd_psndoc on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc left outer join org_adminorg on "
				+ "org_adminorg.pk_adminorg = hi_psnjob.pk_org left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept " + "left outer join om_post on om_post.pk_post = hi_psnjob.pk_post   where (tbm_psndoc.pk_tbm_psndoc in (select "
				+ "pk_tbm_psndoc from tbm_psndoc where   (tbm_psndoc.enddate =(select max(enddate) from tbm_psndoc psndoc2 where " + "psndoc2.pk_org = '" + pk_org + "' and psndoc2.pk_psndoc=tbm_psndoc.pk_psndoc) and tbm_psndoc.enddate > '" + time + "')) )  " + "and tbm_psndoc.enddate >= '" + time
				+ "'  order by bd_psndoc.code ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and bd_psndoc.pk_psndoc   in('" + pk + "') ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[3]));
				defdocVO.setDef2(PubTools.getStringValue(psnojb[4]));
				defdocVO.setDef3(PubTools.getStringValue(psnojb[5]));
				defdocVO.setDef4(PubTools.getStringValue(psnojb[6]));
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 2.人员
	 */
	public RetDefDocVO psncrmDoc(String pk_deptdoc, String billtype, String pk, String pk_org) throws BusinessException {
		/**
		 * dongliang 2018-11-22 11:01:52 sql 添加条件 启用状态 和 pk_org
		 */
		StringBuffer sql = new StringBuffer(
				"select bd_psndoc.code,bd_psndoc.name,bd_psndoc.pk_psndoc,bd_psndoc.mobile,bd_psndoc.email,org_dept.name deptname,org_corp.name unitname  " +
				" from bd_psndoc "
				+ "inner join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc " 
				+ "inner join org_dept on bd_psnjob.pk_dept = org_dept.pk_dept " 
				+ "inner join org_corp on org_corp.pk_corp = org_dept.pk_org " 
				+ "where isnull(bd_psndoc.dr, 0) = 0 " 
				+ "and isnull(org_dept.dr, 0) = 0 and org_dept.pk_org = '"+pk_org+"' "
				+ " and bd_psndoc.enablestate = 2" 
				+ " and isnull(org_corp.dr, 0) = 0 " + " " + " and bd_psnjob.ismainjob='Y' ");
		if (null != pk && !"".equals(pk)) {
			sql = new StringBuffer(" select code,name,pk_psndoc ,code,code,code,code from  bd_psndoc where pk_psndoc in ('" + pk + "') ");
		} else if (null != pk_deptdoc) {
			sql.append(" and org_dept.pk_dept = '" + pk_deptdoc + "' ");
		}
		//sql.append(" order by bd_psndoc.name ");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[3]));
				defdocVO.setDef2(PubTools.getStringValue(psnojb[4]));
				defdocVO.setDef3(PubTools.getStringValue(psnojb[5]));
				defdocVO.setDef4(PubTools.getStringValue(psnojb[6]));
				defdocVO.setShowvalue(defdocVO.getName() + psnojb[0] + psnojb[5]);
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 17.协同当前集团+业务单元
	 */
	public RetDefDocVO cooperGroupOrgDoc(String pk_org, String billtype, String pk, String pk_billtype, String userid) throws BusinessException {
		String sql = "";
		ArrayList<CpOrgVO> list = new ArrayList<CpOrgVO>();
		if (null != pk && !"".equals(pk)) {
			sql = "select * from cp_orgs where pk_org in('" + pk + "') and isnull(dr,0)=0  order by code ";
			list = (ArrayList<CpOrgVO>) dao.executeQuery(sql, new BeanListProcessor(CpOrgVO.class));
		} else {
			sql = "select * from cp_orgs where pk_org = '" + pk_org + "' and isnull(dr,0)=0  order by code ";
			list = (ArrayList<CpOrgVO>) dao.executeQuery(sql, new BeanListProcessor(CpOrgVO.class));
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				CpOrgVO deptdocVO = (CpOrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_org());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 20.协同用户
	 */
	public RetDefDocVO cooperUserDoc(String billtype, String pk, String pk_org, String pk_allorg, String pk_billtype, String userid) throws BusinessException {
		if (pk_allorg != null) {
			pk_org = pk_allorg;
		}
		ArrayList<SuperVO> list = new ArrayList<SuperVO>();

		String condition = "1=1 and isnull(dr,0)=0 order by user_code ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  cuserid in ('" + pk + "') and 1=1 and isnull(dr,0)=0  order by user_code ");
		}
		list = (ArrayList<SuperVO>) dao.retrieveByClause(UserVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				UserVO deptdocVO = (UserVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getUser_code());
				defdocVO.setName(deptdocVO.getUser_name());
				defdocVO.setPk(deptdocVO.getCuserid());
				defdocVO.setShowvalue(deptdocVO.getUser_code() + " " + deptdocVO.getUser_name());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 40.协同组织
	 */
	public RetDefDocVO xtOrg(String billtype, String pk_billtype, String pk_org, String pk, String userid) throws BusinessException {
		String condition = "";
		if (pk_org == null && pk == null) {
			condition = " orglevel=2 and modifier<>'~' and orgtype10='Y' and  isnull(dr,0)=0  order by code ";
		} else {
			if (null != pk && !"".equals(pk)) {
				condition = " pk_org in('" + pk + "') and isnull(dr,0)=0  order by code ";
			} else {
				// 其他没权限的节点
				condition = " pk_org = '" + pk_org + "' and isnull(dr,0)=0  order by code ";
			}
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(CpOrgVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				CpOrgVO docVO = (CpOrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(pk_billtype);
				defdocVO.setCode(docVO.getCode());
				defdocVO.setName(docVO.getName());
				defdocVO.setPk(docVO.getPk_org());
				defdocVO.setShowvalue(docVO.getCode() + " " + docVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 交易类型(入职申请) 
	 */
	private Object billApplyType(String datatype, String pk, String pk_org, String pk_billtype) throws BusinessException {
		String sql = " select pk_billtypeid,pk_billtypecode,billtypename from  bd_billtype   where ( istransaction = 'Y' and   1=1  )  and (  parentbilltype = '6101' )  order by pk_billtypecode ";
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_billtypeid,pk_billtypecode,billtypename from bd_billtype where istransaction = 'Y'  AND pk_billtypeid  in('" + pk + "') ";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 考勤类别拷贝 
	 */
	@SuppressWarnings("unchecked")
	private Object timeitemCopyRefModel(String pk_billtype, String billtype, String pk, String pk_org) throws BusinessException {
		Integer itemtype = null;
		if ("6403".equals(pk_billtype)) {
			itemtype = 2;
		} else if ("6404".equals(pk_billtype)) {
			itemtype = 0;
		}
		StringBuffer sql = new StringBuffer("select tbm_timeitemcopy.pk_timeitemcopy,tbm_timeitem.timeitemcode,tbm_timeitem.timeitemname from " + "tbm_timeitemcopy inner join tbm_timeitem on tbm_timeitem.pk_timeitem=tbm_timeitemcopy.pk_timeitem and tbm_timeitemcopy.pk_org='" + pk_org + "'  "
				+ "where 11 = 11 ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and (tbm_timeitemcopy.pk_timeitemcopy in('" + pk + "') ) ");
		} else {
			sql.append(" and tbm_timeitem.itemtype=" + itemtype + "");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[2]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[0]));
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 考勤类别 
	 */
	@SuppressWarnings("unchecked")
	private Object timeitemRefModel(String pk_billtype, String billtype, String pk, String pk_org) throws BusinessException {
		Integer itemtype = null;
		if ("6403".equals(pk_billtype)) {
			itemtype = 2;
		} else if ("6404".equals(pk_billtype)) {
			itemtype = 0;
		}
		StringBuffer sql = new StringBuffer("select tbm_timeitem.pk_timeitem,tbm_timeitem.timeitemcode,tbm_timeitem.timeitemname,tbm_timeitem.itemtype, tbm_timeitemcopy.pk_timeitemcopy,"
				+ " tbm_timeitemcopy.pk_org, tbm_timeitemcopy.timeitemunit, tbm_timeitemcopy.leavesetperiod from tbm_timeitem inner join tbm_timeitemcopy " + "on tbm_timeitem.pk_timeitem = tbm_timeitemcopy.pk_timeitem and tbm_timeitemcopy.pk_org = '" + pk_org + "' where 11 = 11 ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and (tbm_timeitem.pk_timeitem in('" + pk + "') )order by tbm_timeitem.timeitemcode");
		} else {
			sql.append("and ( tbm_timeitem.itemtype = " + itemtype + " and tbm_timeitemcopy.enablestate = 2 ) order by tbm_timeitem.timeitemcode");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[2]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[0]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[4]));// 出差类别copy
				// pk_awaytypecopy
				defdocVO.setDef2(PubTools.getStringValue(psnojb[6]));// 考勤单位
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}

		return null;
	} 
	
	/**
	 * 部门版本(所有) 
	 */
	@SuppressWarnings("unchecked")
	private Object orgdept(String pk, String billtype) throws BusinessException {
		ArrayList<DeptVersionVO> list = new ArrayList<DeptVersionVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<DeptVersionVO>) dao.retrieveByClause(DeptVersionVO.class, " isnull(dr,0)=0 and pk_vid in('" + pk + "') ");
		} else {
			list = (ArrayList<DeptVersionVO>) dao.retrieveByClause(DeptVersionVO.class, " isnull(dr,0)=0 order by code asc ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				DeptVersionVO hrorgdocVO = (DeptVersionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getCode());
				defdocVO.setName(hrorgdocVO.getName());
				defdocVO.setPk(hrorgdocVO.getPk_vid());
				defdocVO.setShowvalue(hrorgdocVO.getCode() + " " + hrorgdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 行政组织版本 
	 */
	@SuppressWarnings("unchecked")
	private Object adminorg(String pk, String billtype) throws BusinessException {
		ArrayList<AdminOrgVersionVO> list = new ArrayList<AdminOrgVersionVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<AdminOrgVersionVO>) dao.retrieveByClause(AdminOrgVersionVO.class, " isnull(dr,0)=0 and pk_vid in('" + pk + "') ");
		} else {
			list = (ArrayList<AdminOrgVersionVO>) dao.retrieveByClause(AdminOrgVersionVO.class, "isnull(dr,0)=0 order by code asc ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				AdminOrgVersionVO hrorgdocVO = (AdminOrgVersionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getCode());
				defdocVO.setName(hrorgdocVO.getName());
				defdocVO.setPk(hrorgdocVO.getPk_vid());
				defdocVO.setShowvalue(hrorgdocVO.getCode() + " " + hrorgdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 人力资源组织(所有) 
	 */
	@SuppressWarnings("unchecked")
	private Object HrOrgRefModel(String pk, String billtype) throws BusinessException {
		ArrayList<HROrgVO> list = new ArrayList<HROrgVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<HROrgVO>) dao.retrieveByClause(HROrgVO.class, " isnull(dr,0)=0 and pk_hrorg  in('" + pk + "') ");
			if (list.size() == 0) {
				list = (ArrayList<HROrgVO>) dao.retrieveByClause(HROrgVO.class, " isnull(dr,0)=0 and pk_vid  in('" + pk + "') ");// 不清楚用途
			}
		} else {
			list = (ArrayList<HROrgVO>) dao.retrieveByClause(HROrgVO.class, "isnull(dr,0)=0 order by code asc ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				HROrgVO hrorgdocVO = (HROrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getCode());
				defdocVO.setName(hrorgdocVO.getName());
				defdocVO.setPk(hrorgdocVO.getPk_hrorg());
				defdocVO.setShowvalue(hrorgdocVO.getCode() + " " + hrorgdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 员工号 
	 */
	@SuppressWarnings("unchecked")
	private Object TBMPsndocRefModel(String pk_org, String pk, String userid) throws BusinessException {
		UFDate busDate = new UFDate();
		UFLiteralDate busLiteralDate = UFLiteralDate.getDate(busDate.toString().substring(0, 10));
		UFLiteralDate lastPeriodBeginDate = busLiteralDate.getDateBefore(60);
		StringBuffer sql = new StringBuffer("select bd_psndoc.code, bd_psndoc.name, clerkcode, tbm_psndoc.timecardid, tbm_psndoc.begindate, tbm_psndoc.enddate, " + "org_adminorg.name, org_dept.name, bd_psndoc.pk_group, bd_psndoc.pk_org, bd_psndoc.pk_psndoc, hi_psnjob.pk_psnorg, "
				+ "hi_psnjob.pk_psnjob, hi_psnjob.pk_psncl, hi_psnjob.pk_org, hi_psnjob.pk_dept,om_job.jobname,om_post.postname, "
				+ "org_adminorg.PK_VID pk_org_v, org_dept.pk_vid pk_dept_v,bd_psndoc.id,bd_psndoc.mobile ,om_post.pk_post,hi_psnjob.pk_job,hi_psnjob. pk_jobgrade from tbm_psndoc inner join hi_psnjob on tbm_psndoc.pk_psnjob = hi_psnjob.pk_psnjob inner join "
				+ "hi_psnorg on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg inner join bd_psndoc on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc left outer join " + "org_adminorg on org_adminorg.pk_adminorg = hi_psnjob.pk_org left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept left outer "
				+ "join om_post on om_post.pk_post = hi_psnjob.pk_post left outer join om_job on om_job.pk_job = hi_psnjob.pk_job " + "left join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc where 1 = 1 and ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" (hi_psnjob.pk_psnjob in('" + pk + "') )order by bd_psndoc.code");
		} else {
			sql.append(" cuserid = '" + userid + "' and ( tbm_psndoc.pk_tbm_psndoc in ( select pk_tbm_psndoc from tbm_psndoc where " + "( tbm_psndoc.enddate = ( select max ( enddate ) from tbm_psndoc psndoc2 where psndoc2.pk_org = '" + pk_org + "' and psndoc2.pk_psndoc = tbm_psndoc.pk_psndoc ) "
					+ "and tbm_psndoc.enddate > '" + lastPeriodBeginDate + "' ) ) ) and ( hi_psnjob.pk_org || hi_psnjob.pk_dept like '%" + pk_org + "%' ) order by bd_psndoc.code");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[0]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[12]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[10]));// 人员编码
				// pk_psndoc
				defdocVO.setDef2(PubTools.getStringValue(psnojb[11]));// 人员组织关系pk_psnorg
				defdocVO.setDef3(PubTools.getStringValue(psnojb[18]));// 组织
				// pk_org_v
				defdocVO.setDef4(PubTools.getStringValue(psnojb[19]));// 部门
				// pk_dept_v
				defdocVO.setDef5(PubTools.getStringValue(psnojb[16]));// 职务名称
				defdocVO.setDef6(PubTools.getStringValue(psnojb[17]));// 岗位名称
				defdocVO.setDef7(PubTools.getStringValue(psnojb[20]));// 身份证号
				defdocVO.setDef8(PubTools.getStringValue(psnojb[21]));// 手机号
				defdocVO.setDef9(PubTools.getStringValue(psnojb[13]));//人员类别
				defdocVO.setDef10(PubTools.getStringValue(psnojb[22]));//岗位pk
				defdocVO.setDef11(PubTools.getStringValue(psnojb[9]));//组织
				defdocVO.setDef12(PubTools.getStringValue(psnojb[15]));//部门
				defdocVO.setDef13(PubTools.getStringValue(psnojb[23]));//职务主键
				defdocVO.setDef14(PubTools.getStringValue(psnojb[24]));//职级
				defdocVO.setShowvalue(defdocVO.getName() + " " + defdocVO.getCode());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 人员工作记录(左树不含下级HR) 
	 */
	@SuppressWarnings("unchecked")
	private Object psnjobRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer("select bd_psndoc.code, bd_psndoc.name, clerkcode, org_orgs.name, org_dept.name, om_post.postname, " + "bd_psndoc.pk_group, bd_psndoc.pk_org, bd_psndoc.pk_psndoc, hi_psnjob.pk_psnorg, hi_psnjob.pk_psnjob, "
				+ "hi_psnjob.pk_psncl, hi_psnjob.pk_dept, hi_psnjob.pk_job, hi_psnjob.pk_post, hi_psnjob.ismainjob, hi_psnjob.assgid, " + "hi_psnjob.showorder, idtype, id from bd_psndoc inner join hi_psnorg on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc "
				+ "inner join hi_psnjob on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg left outer join org_orgs on org_orgs.pk_org = hi_psnjob.pk_org " + "left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept left outer join om_post on om_post.pk_post = hi_psnjob.pk_post "
				+ "where 11 = 11 and hi_psnjob.poststat = 'Y' and hi_psnorg.indocflag = 'Y' and " + "( hi_psnjob.pk_org = '" + pk_org + "' or hi_psnjob.pk_dept = '" + pk_org + "' ) and " + "( hi_psnjob.pk_hrorg = '" + pk_org + "' and hi_psnjob.lastflag = 'Y' and hi_psnjob.ismainjob = 'Y' ) "
				+ "and hi_psnjob.pk_org in ( select pk_adminorg from org_admin_enable )");
		if (null != pk && !"".equals(pk)) {
			sql.append("and hi_psnjob.pk_psnjob in('" + pk + "')");
		} else {
			sql.append(" and hi_psnjob.pk_psnjob not in " + "( select pk_psnjob from hi_psnjob where pk_psnorg in ( select pk_psnorg from hi_psndoc_keypsn where pk_keypsn_grp " + "not in ( select pk_keypsn_group from hi_keypsn_group where 1 = 1 ) ) ) order by hi_psnjob.showorder, bd_psndoc.code");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[10]));
				defdocVO.setShowvalue(defdocVO.getCode() + " " + defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 人员工作记录(入职申请) 
	 */
	@SuppressWarnings("unchecked")
	private Object psnjobApplyRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer(
				"select  bd_psndoc.code,bd_psndoc.name,clerkcode,org_orgs.name,org_dept.name,om_post.postname,bd_psndoc.pk_group,bd_psndoc.pk_org,bd_psndoc.pk_psndoc,hi_psnjob.pk_psnorg,hi_psnjob.pk_psnjob,hi_psnjob.pk_psncl,hi_psnjob.pk_dept,hi_psnjob.pk_job,hi_psnjob.pk_post,hi_psnjob.ismainjob,hi_psnjob.assgid,hi_psnjob.showorder,idtype,bd_psncl.name,hi_psnjob.begindate,id,	bd_psndoc.sex,bd_psndoc.mobile ,om_jobtype.jobtypename,hi_psndoc_edu.education, hi_psndoc_edu.major,hi_psndoc_edu.school,bd1.name,bd_defdoc.name  from   bd_psndoc inner join hi_psnorg on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc  inner join hi_psnjob  on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg   left outer join org_orgs on org_orgs.pk_org = hi_psnjob.pk_org  left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept  left outer join om_post on om_post.pk_post = hi_psnjob.pk_post  LEFT OUTER JOIN bd_psncl ON bd_psncl.pk_psncl = hi_psnjob.pk_psncl LEFT OUTER JOIN om_jobtype ON om_jobtype.pk_jobtype = hi_psnjob.series LEFT JOIN hi_psndoc_edu ON hi_psndoc_edu.pk_psndoc  = bd_psndoc.pk_psndoc LEFT JOIN bd_defdoc ON bd_psndoc.prof = bd_defdoc.pk_defdoc LEFT JOIN bd_defdoc bd1 ON bd_psndoc.titletechpost  = bd1.pk_defdoc where 11=11 ");
		if (null != pk && !"".equals(pk)) {
			sql.append("and hi_psnjob.pk_psnjob in('" + pk + "')");
		} else {
			sql.append("and hi_psnjob.trnsevent = 1 and hi_psnorg.indocflag <> 'Y' and hi_psnjob.pk_psnjob not in (select pk_psnjob from hi_entryapply where approve_state in (-1,3,2,1)) and ( hi_psnjob.pk_org = '"
					+ pk_org
					+ "' or hi_psnjob.pk_dept = '"
					+ pk_org
					+ "') and ( hi_psnjob.pk_hrorg = '"
					+ pk_org
					+ "' and hi_psnjob.lastflag = 'Y' and hi_psnjob.ismainjob = 'Y' )  and hi_psnjob.pk_org in (select pk_adminorg from org_admin_enable)  and hi_psnjob.pk_psnjob not in (  select pk_psnjob from hi_psnjob where pk_psnorg in ( select pk_psnorg from hi_psndoc_keypsn where pk_keypsn_grp not in ( select pk_keypsn_group from hi_keypsn_group where  1 = 1 ) )  )");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[10]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[13]));// 职务			
				defdocVO.setDef2(PubTools.getStringValue(psnojb[23]));// 电话
				defdocVO.setDef3(PubTools.getStringValue(psnojb[24]));//入职类型
				defdocVO.setDef4(PubTools.getStringValue(psnojb[25]));//学历
				defdocVO.setDef5(PubTools.getStringValue(psnojb[26]));// //专业
				defdocVO.setDef6(PubTools.getStringValue(psnojb[27]));//学校
				defdocVO.setDef7(PubTools.getStringValue(psnojb[22]));// 性别	
				defdocVO.setDef8(PubTools.getStringValue(psnojb[28]));// 职称
				defdocVO.setDef9(PubTools.getStringValue(psnojb[29]));// 职业资格
				defdocVO.setDef11(PubTools.getStringValue(psnojb[19]));// 人员类别
				defdocVO.setDef12(PubTools.getStringValue(psnojb[4]));// 部门
				defdocVO.setDef13(PubTools.getStringValue(psnojb[5]));// 岗位
				defdocVO.setDef14(PubTools.getStringValue(psnojb[8]));// 人员信息
				defdocVO.setDef15(PubTools.getStringValue(psnojb[20]));// 生效日期
				defdocVO.setShowvalue(defdocVO.getCode() + " " + defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 业务流程(入职申请) 
	 */
	@SuppressWarnings("unchecked")
	private Object businessApplyRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		String sql = " SELECT busicode,businame,pk_busitype from bd_busitype  where ( validity=1 )  and busiprop = 6  and ( isnull(pk_org,'~') = '~' or pk_org = '" + pk_org + "')  and validity =1 and primarybilltype like '6101%'  order by busicode ";
		if (null != pk && !"".equals(pk)) {
			sql = "SELECT busicode,businame,pk_busitype from bd_busitype where pk_busitype in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 交易类型 
	 */
	private Object billType(String datatype, String pk, String pk_org, String pk_billtype) throws BusinessException {
		String sql = "select pk_billtypeid,pk_billtypecode,billtypename from bd_billtype where istransaction = 'Y' and isnull(dr,0) = 0  and isnull(islock, 'N')='N' ";
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_billtypeid,pk_billtypecode,billtypename from bd_billtype where ((pk_group<>'global00000000000000' and pk_billtypecode in ('"+pk+"')) or pk_billtypeid in ('"+pk+"')) and nvl(dr,0)=0";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 加班类别
	 */
	private Object timeItemdoc(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer("select tbm_timeitem.pk_timeitem,tbm_timeitem.timeitemcode,tbm_timeitem.timeitemname,tbm_timeitem.itemtype, tbm_timeitemcopy.pk_timeitemcopy,"
				+ " tbm_timeitemcopy.pk_org, tbm_timeitemcopy.timeitemunit, tbm_timeitemcopy.leavesetperiod from tbm_timeitem inner join tbm_timeitemcopy " + "on tbm_timeitem.pk_timeitem = tbm_timeitemcopy.pk_timeitem and tbm_timeitemcopy.pk_org = '" + pk_org + "' where 11 = 11 and"
				+ " islactation = 'N' ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and (tbm_timeitem.pk_timeitem in('" + pk + "') )order by tbm_timeitem.timeitemcode");
		} else {
			sql.append("and ( tbm_timeitem.itemtype = '1' and tbm_timeitemcopy.enablestate = 2 ) order by tbm_timeitem.timeitemcode");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[2]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[0]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[4]));// 加班类别copy
				defdocVO.setDef2(PubTools.getStringValue(psnojb[6]));// 加班单位
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}

		return null;
	}

	/**
	 * 异动类型 
	 */
	private Object trnstypeRefModel(String pk, String billtype, String pk_org, String pk_group, String pk_billtype) throws BusinessException {
		StringBuffer sql = new StringBuffer("select pk_trnstype,trnstypecode, trnstypename from hr_trnstype where ");
		if (null != pk && !"".equals(pk)) {
			sql.append("pk_trnstype in('" + pk + "')");
		} else {
			// 组织为全局globle
			sql.append("( 1 = 1 and enablestate = 2 and trnsevent = " + ("6113".equals(pk_billtype) ? 3 : 4) + " ) " + "and ( ( ( pk_org = 'GLOBLE00000000000000' or pk_group = '" + pk_group + "' ) ) ) order by trnstypecode");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	 

	/**
	 * 行政组织 
	 */
	private Object adminorgRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		ArrayList<AdminOrgVO> list = new ArrayList<AdminOrgVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<AdminOrgVO>) dao.retrieveByClause(AdminOrgVO.class, " isnull(dr,0)=0 and pk_adminorg in('" + pk + "') ");
		} else {
			list = (ArrayList<AdminOrgVO>) dao.retrieveByClause(AdminOrgVO.class, " isnull(dr,0)=0 order by displayorder,code asc ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				AdminOrgVO hrorgdocVO = (AdminOrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getCode());
				defdocVO.setName(hrorgdocVO.getName());
				defdocVO.setPk(hrorgdocVO.getPk_adminorg());
				defdocVO.setShowvalue(hrorgdocVO.getCode() + " " + hrorgdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 人员类别 
	 */
	@SuppressWarnings("unchecked")
	private Object psnclvoRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		ArrayList<PsnClVO> list = new ArrayList<PsnClVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<PsnClVO>) dao.retrieveByClause(PsnClVO.class, " isnull(dr,0)=0 and pk_psncl in('" + pk + "') ");
		} else {
			list = (ArrayList<PsnClVO>) dao.retrieveByClause(PsnClVO.class, "( enablestate = 2 ) and isnull(dr,0)=0  order by code ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				PsnClVO hrorgdocVO = (PsnClVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getCode());
				defdocVO.setName(hrorgdocVO.getName());
				defdocVO.setPk(hrorgdocVO.getPk_psncl());
				defdocVO.setShowvalue(hrorgdocVO.getCode() + " " + hrorgdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 部门HR 
	 */
	private Object deptHrRefModel(String pk, String billtype, String pk_org, String glorg) throws BusinessException {
		StringBuffer sql = new StringBuffer("select org_dept.code, org_dept.name, org_orgs.name, org_dept.pk_dept, org_dept.pk_fatherorg, " + "org_dept.pk_group, org_dept.pk_org, org_dept.hrcanceled, org_dept.innercode, org_dept.principal, "
				+ "org_dept.createdate, org_dept.displayorder, org_orgs.code org_code, org_orgs.name org_name " + "from org_dept inner join org_orgs on org_orgs.pk_org = org_dept.pk_org inner join org_adminorg "
				+ "on org_dept.pk_org = org_adminorg.pk_adminorg and org_adminorg.enablestate = 2 where 11 = 11 and ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" org_dept.pk_dept in('" + pk + "')");
		} else {
			sql.append("hrcanceled = 'N' and depttype <> 1 and ( org_dept.pk_org = '" + glorg + "' and org_dept.pk_org " + "in ( select pk_adminorg from org_admin_enable ) and org_dept.enablestate = 2 and org_dept.hrcanceled = 'N' ) " + "order by org_code, org_dept.displayorder, org_dept.code");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 岗位HR 
	 */
	private Object postRefModel(String pk, String billtype, String pk_org, String pk_dept, String glorg) throws BusinessException {
		StringBuffer sql = new StringBuffer("select om_post.postcode, om_post.postname, org_dept.name, org_orgs.name, om_post.pk_dept, " + "om_post.pk_job, om_post.pk_post, om_post.suporior, om_post.employment, om_post.worktype, om_post.pk_org, "
				+ "org_orgs.code org_code, org_orgs.name org_name, org_dept.code dept_code, org_dept.name dept_name from om_post " + "inner join org_dept on om_post.pk_dept = org_dept.pk_dept inner join org_orgs on org_dept.pk_org = org_orgs.pk_org " + "where 11 = 11 ");
		if (pk_dept != null && !"".equals(pk_dept)) {
			sql.append("and org_dept.pk_dept='" + pk_dept + "'");
		}
		if (null != pk && !"".equals(pk)) {
			sql.append(" and om_post.pk_post in('" + pk + "')");
		} else {
			sql.append("and ( isnull ( om_post.hrcanceled, '~' ) = '~' or om_post.hrcanceled = 'N' ) " + "and ( ( isnull ( om_post.isstd, '~' ) = '~' or om_post.isstd = 'N' ) and om_post.pk_org = '" + glorg + "' ) " + "order by org_code, dept_code, postcode");
		}
		@SuppressWarnings("unchecked") 
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[6]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 岗位序列HR 
	 */
	@SuppressWarnings("unchecked")
	private Object postseriesRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		ArrayList<PostSeriesVO> list = new ArrayList<PostSeriesVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<PostSeriesVO>) dao.retrieveByClause(PostSeriesVO.class, " isnull(dr,0)=0 and pk_postseries in('" + pk + "') ");
		} else {
			list = (ArrayList<PostSeriesVO>) dao.retrieveByClause(PostSeriesVO.class, " ( ( enablestate in ( 2, 1 ) ) " + "and ( ( pk_org = '" + pk_org + "' or pk_group = '" + pk_group + "' ) ) ) order by postseriescode ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				PostSeriesVO hrorgdocVO = (PostSeriesVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(hrorgdocVO.getPostseriescode());
				defdocVO.setName(hrorgdocVO.getPostseriesname());
				defdocVO.setPk(hrorgdocVO.getPk_postseries());
				defdocVO.setShowvalue(hrorgdocVO.getPostseriescode() + " " + hrorgdocVO.getPostseriesname());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 职务(设置业务单元主键)
	 */
	private Object jobtypejob3RefModel(String pk, String billtype,
			String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer(
				"select jobcode,jobname,pk_job from om_job "
						+ "where 1=1 and 	");
		if (null != pk && !"".equals(pk)) {
			sql.append("  pk_job in('" + pk + "')");
		} else {
			sql.append("( ( enablestate in ( 2, 1 ) ) and ( nvl(dr,0)=0 ) ) "
					+ "order by jobcode");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(
				sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i]
						.setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO
						.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO
						.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode()+" "+defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 职务类别HR 
	 */
	private Object jobtypeRefModel(String pk, String billtype, String pk_org, String pk_group) throws BusinessException {
		StringBuffer sql = new StringBuffer("select jobtypecode, jobtypename, pk_jobtype, father_pk, pk_grade_source from om_jobtype " + "where 11 = 11 and 	");
		if (null != pk && !"".equals(pk)) {
			sql.append("  pk_jobtype in('" + pk + "')");
		} else {
			sql.append("( ( enablestate in ( 2, 1 ) ) and ( ( pk_org = '" + pk_org + "' or pk_group = '" + pk_group + "' ) ) ) order by jobtypecode");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 职级(设置职务主键) 
	 */
	private Object postgradeRefModel(String pk, String billtype, String pk_org) throws BusinessException {
		StringBuffer sql = new StringBuffer("select om_joblevel.code, om_joblevel.name, om_joblevelsys.code syscode, om_joblevelsys.name sysname, " + "pk_joblevel, om_joblevelsys.pk_joblevelsys from om_joblevel inner join om_joblevelsys "
				+ "on om_joblevel.pk_joblevelsys = om_joblevelsys.pk_joblevelsys where 11 = 11 ");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and om_joblevel.pk_joblevel in('" + pk + "')");
		} else {
			sql.append("order by pk_joblevel");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 职等HR 
	 */
	private Object jobrankRefModel(String pk, String billtype, String pk_org, String pk_group) throws BusinessException {
		StringBuffer sql = new StringBuffer("select jobrankcode, jobrankname, pk_jobrank, jobrankorder from om_jobrank " + "where 11 = 11 and ");
		if (null != pk && !"".equals(pk)) {
			sql.append("  pk_jobrank in('" + pk + "')");
		} else {
			sql.append("( ( enablestate in ( 2, 1 ) ) and ( ( pk_org = '" + pk_org + "' or pk_group = '" + pk_group + "' ) ) ) order by jobrankorder ");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 

	/**
	 * 人员工作记录(调配)
	 */
	private Object psnjobdpRefModel(String pk, String billtype, String pk_org,
			String pk_group2, int stapply_mode, String userid,
			String pk_billtype) throws BusinessException {
		StringBuffer sql = new StringBuffer();
		if (stapply_mode == 3) {// 调入
			sql = new StringBuffer(
					"select bd_psndoc.code, bd_psndoc.name,hi_psnjob.pk_psnjob,bd_psndoc.mobile,om_post.pk_post, clerkcode, hi_psnjob.pk_org, hi_psnjob.pk_dept as deptname,"
							+ " om_post.postname,hi_psnjob.pk_psncl,hi_psnjob.pk_job,hi_psnjob.pk_jobgrade  from bd_psndoc inner join hi_psnorg on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc "
							+ "inner join hi_psnjob on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg left outer join org_orgs on org_orgs.pk_org = hi_psnjob.pk_org left outer join "
							+ "org_dept on org_dept.pk_dept = hi_psnjob.pk_dept left outer join om_post on om_post.pk_post = hi_psnjob.pk_post ");
			if ("6115".equals(pk_billtype)) {
				sql.append(" left join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc where 1=1 and sm_user.cuserid = '" + userid + "' and ");
			} else {
				sql.append(" where 1 = 1 and  ");
			}
			if (null != pk && !"".equals(pk)) {
				sql.append("  hi_psnjob.pk_psnjob in('" + pk + "')");
			} else {
				sql.append("( hi_psnorg.indocflag = 'Y' ) " + "and hi_psnjob.pk_group = '" + pk_group2 + "' and hi_psnjob.pk_hrorg <> '" + pk_org + "' and hi_psnjob.pk_org <> '" + pk_org + "' "
						+ "and hi_psnjob.lastflag = 'Y' and hi_psnjob.ismainjob = 'Y' and hi_psnjob.pk_psnjob in ( select psnjob.pk_psnjob from hi_psnjob psnjob "
						+ "inner join hi_psnorg psnorg on psnjob.pk_psnorg = psnorg.pk_psnorg where ( psnorg.endflag = 'N' or nvl ( psnorg.endflag, '~' ) = '~' ) "
						+ "and psnorg.psntype = 0 and psnorg.lastflag = 'Y' and psnorg.indocflag = 'Y' and ( psnjob.endflag = 'N' or nvl ( psnorg.endflag, '~' ) = '~' ) "
						+ "and psnjob.lastflag = 'Y' and psnjob.ismainjob = 'Y' )  and hi_psnjob.pk_psnjob not in ( select pk_psnjob from hi_psnjob where pk_psnorg in "
						+ "( select pk_psnorg from hi_psndoc_keypsn where pk_keypsn_grp not in ( select pk_keypsn_group from hi_keypsn_group where 1 = 1 ) ) )");
			}
		} else if (stapply_mode == 2 || stapply_mode == 1) {// 2调出 1组织内调配
			sql = new StringBuffer(
					"select bd_psndoc.code, bd_psndoc.name, hi_psnjob.pk_psnjob,bd_psndoc.mobile,om_post.pk_post, clerkcode, hi_psnjob.pk_org, hi_psnjob.pk_dept as deptname, om_post.postname ,hi_psnjob.pk_psncl,hi_psnjob.pk_job ,"
							+ "hi_psnjob.pk_jobgrade from bd_psndoc inner join hi_psnorg on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc inner join hi_psnjob on hi_psnorg.pk_psnorg "
							+ "= hi_psnjob.pk_psnorg left outer join org_orgs on org_orgs.pk_org = hi_psnjob.pk_org left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept"
							+ " left outer join om_post on om_post.pk_post = hi_psnjob.pk_post ");
			if ("6115".equals(pk_billtype)) {
				sql.append(" left join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc where 1=1 and sm_user.cuserid = '" + userid + "' and ");
			} else {
				sql.append(" where 1 = 1 and  ");
			}
			if (null != pk && !"".equals(pk)) {
				sql.append("  hi_psnjob.pk_psnjob in('" + pk + "')");
			} else {
				sql.append("hi_psnjob.pk_psnjob in ( select psnjob.pk_psnjob from hi_psnjob "
						+ "psnjob inner join hi_psnorg psnorg on psnjob.pk_psnorg = psnorg.pk_psnorg where ( psnorg.endflag = 'N' or nvl ( psnorg.endflag, '~' ) = '~' ) and "
						+ "psnorg.psntype = 0 and psnorg.lastflag = 'Y' and psnorg.indocflag = 'Y' and ( psnjob.endflag = 'N' or nvl ( psnorg.endflag, '~' ) = '~' ) and "
						+ "psnjob.lastflag = 'Y' and psnjob.ismainjob = 'Y' )  and ( hi_psnjob.pk_hrorg = '" + pk_org + "' and hi_psnjob.lastflag = 'Y' and "
						+ "hi_psnjob.ismainjob = 'Y' ) and hi_psnjob.pk_org in ( select pk_adminorg from org_admin_enable ) and hi_psnjob.pk_psnjob not in "
						+ "( select pk_psnjob from hi_psnjob where pk_psnorg in ( select pk_psnorg from hi_psndoc_keypsn where pk_keypsn_grp not in ( select pk_keypsn_group "
						+ "from hi_keypsn_group where 1 = 1 ) ) )");
			}
		} else {
			if (pk != null) {
				sql.append("SELECT bd_psndoc.code, bd_psndoc.name,hi_psnjob.pk_psnjob,bd_psndoc.mobile,om_post.pk_post, clerkcode, hi_psnjob.pk_org, hi_psnjob.pk_dept as deptname,om_post.postname ,hi_psnjob.pk_psncl ,hi_psnjob.pk_job,hi_psnjob.pk_jobgrade  FROM bd_psndoc "
						+ "INNER JOIN hi_psnorg ON hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc "
						+ "INNER JOIN hi_psnjob ON hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg LEFT OUTER JOIN om_post ON om_post.pk_post = hi_psnjob.pk_post WHERE 1 = 1 and hi_psnjob.pk_psnjob = '"
						+ pk + "'");
			} else {
				throw new BusinessException("请先选择调配方式！");
			}
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			throw new BusinessException("没有数据！");
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));//工作记录主键
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));//人员编码
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));//人员名称
				defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));//手机号
				defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));//岗位
				defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[6]));//原组织
				defDocVOs[i].setDef4(PuPubVO.getString_TrimZeroLenAsNull(obj2[7]));//原部门
				defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(obj2[9]));//原人员类别
				defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(obj2[10]));//职务
				defDocVOs[i].setDef7(PuPubVO.getString_TrimZeroLenAsNull(obj2[11]));//职级
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 

	/**
	 * 销差员工号
	 */
	private Object XCsndocRefModel(String pk_org, String pk, String userid) throws BusinessException {
		StringBuffer sql = new StringBuffer("select bd_psndoc.code,bd_psndoc.name,tbm_awayreg.pk_awayreg,tbm_awayreg.pk_psndoc,tbm_awayreg.pk_psnjob,tbm_awayreg.pk_org_v,tbm_awayreg.awaybegintime,tbm_awayreg.awayaddress,"
				+ "tbm_awayreg.pk_org,tbm_awayreg.awayendtime,tbm_awayreg.pk_billsourceb,tbm_awayreg.awayhour,tbm_awayreg.pk_awaytype,tbm_awayreg.pk_psnorg,"
				+ "tbm_awayreg.pk_billsourceh,tbm_awayreg.pk_adminorg,tbm_awayreg.awaybegindate,tbm_awayreg.awayenddate,tbm_awayreg.pk_dept_v,tbm_awayreg.pk_awaytypecopy,tbm_timeitem.timeitemname ,hi_psnjob.pk_dept "
				+ "from tbm_awayreg left join bd_psndoc on tbm_awayreg.pk_psndoc = bd_psndoc.pk_psndoc left join tbm_timeitem on tbm_awayreg.pk_awaytype = tbm_timeitem.pk_timeitem "
				+ "left join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc left join hi_psnjob on tbm_awayreg.pk_psnjob = hi_psnjob.pk_psnjob  where 1=1 and ");
		if (null != pk && !"".equals(pk)) {
			sql.append("tbm_awayreg.pk_awayreg in('" + pk + "')");
		} else {
			sql.append("sm_user.cuserid = '" + userid + "' and tbm_awayreg.pk_awayreg not in (select pk_awayreg from tbm_awayoff where approve_state NOT IN (0, 1)) ");
		}
		sql.append("and tbm_awayreg.awaybegindate > (select isnull (max(enddate), '1971') from tbm_period where sealflag = 'Y' and pk_org = '" + pk_org + "') and tbm_awayreg.billsource = 0");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[0]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[4]));// 员工号
				defdocVO.setDef2(PubTools.getStringValue(psnojb[3]));// 人员基本信息
				// pk_psndoc
				defdocVO.setDef3(PubTools.getStringValue(psnojb[5]));// 组织
				// pk_org_v
				defdocVO.setDef4(PubTools.getStringValue(psnojb[6]));// 开始时间
				// awaybegintime
				defdocVO.setDef5(PubTools.getStringValue(psnojb[7]));// 目的地
				// awayaddress
				defdocVO.setDef6(PubTools.getStringValue(psnojb[9]));// 结束时间
				// awayendtime
				defdocVO.setDef7(PubTools.getStringValue(psnojb[11]));// 登记单时长
				// awayhour
				defdocVO.setDef8(PubTools.getStringValue(psnojb[12]));// 出差类别
				// pk_awaytype
				defdocVO.setDef9(PubTools.getStringValue(psnojb[13]));// 组织关系
				// pk_psnorg
				defdocVO.setDef10(PubTools.getStringValue(psnojb[16]));// 开始日期
				// awaybegindate
				defdocVO.setDef11(PubTools.getStringValue(psnojb[17]));// 结束日期
				// awayenddate
				defdocVO.setDef12(PubTools.getStringValue(psnojb[18]));// 部门
				// pk_dept_v
				defdocVO.setDef13(PubTools.getStringValue(psnojb[19]));// 出差类别copy
				// pk_awaytypecopy
				defdocVO.setDef14(PubTools.getStringValue(psnojb[8]));// 组织
				// pk_org
				defdocVO.setDef15(PubTools.getStringValue(psnojb[20]));// 出差类别
				// timeitemname
//				defdocVO.setDef16(PubTools.getStringValue(psnojb[21]));// 职务级别
				defdocVO.setDef16(PubTools.getStringValue(psnojb[21]));// 部门
				defdocVO.setShowvalue(defdocVO.getName() + " " + defdocVO.getCode() + " " + defdocVO.getDef20() + " " + defdocVO.getDef15());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 转正员工号
	 */
	private Object ZZsndocRefModel(String pk_org, String pk_deptdoc, String userid) throws BusinessException {
		String sql = "select bd_psndoc.code, bd_psndoc.name, clerkcode, org_orgs.name, org_dept.name, om_post.postname, bd_psndoc.pk_group," + "bd_psndoc.pk_org, bd_psndoc.pk_psndoc, hi_psnjob.pk_psnorg, hi_psnjob.pk_psnjob, hi_psnjob.pk_psncl, hi_psnjob.pk_dept, hi_psnjob.pk_job,"
				+ "hi_psnjob.pk_post, hi_psnjob.ismainjob, hi_psnjob.assgid, hi_psnjob.showorder, idtype, id,hi_psnjob.begindate,bd_psndoc.mobile,hi_psnjob.trial_type ,hi_psnjob.pk_jobgrade,hi_psnjob.pk_postseries  from "
				+ "bd_psndoc inner join hi_psnorg on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc inner join hi_psnjob on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg " + "inner join sm_user on sm_user.pk_psndoc=bd_psndoc.pk_psndoc "
				+ "left outer join org_orgs on org_orgs.pk_org = hi_psnjob.pk_org left outer join org_dept on org_dept.pk_dept = hi_psnjob.pk_dept " + "left outer join om_post on om_post.pk_post = hi_psnjob.pk_post where 11 = 11  and sm_user.cuserid='" + userid + "' and hi_psnjob.pk_psnjob in "
				+ "( select psnjob.pk_psnjob from hi_psnjob psnjob inner join hi_psnorg psnorg on psnjob.pk_psnorg = psnorg.pk_psnorg where " + "( psnorg.endflag = 'N' or isnull ( psnorg.endflag, '~' ) = '~' ) and psnorg.psntype = 0 and psnorg.lastflag = 'Y' and psnorg.indocflag = 'Y' and "
				+ "( psnjob.endflag = 'N' or isnull ( psnorg.endflag, '~' ) = '~' ) and psnjob.lastflag = 'Y' and psnjob.ismainjob = 'Y' ) and hi_psnjob.ismainjob = 'Y' " + "and hi_psnjob.trial_flag = 'Y' and ( hi_psnjob.pk_org = '" + pk_org + "' or hi_psnjob.pk_dept = '" + pk_deptdoc + "' ) "
				+ "and ( hi_psnjob.pk_hrorg = '" + pk_org + "' and hi_psnjob.lastflag = 'Y' and hi_psnjob.ismainjob = 'Y' ) and hi_psnjob.pk_org in " + "( select pk_adminorg from org_admin_enable ) and hi_psnjob.pk_psnjob not in ( select pk_psnjob from hi_psnjob where pk_psnorg in "
				+ "( select pk_psnorg from hi_psndoc_keypsn where pk_keypsn_grp not in ( select pk_keypsn_group from hi_keypsn_group where 1 = 1 ) ) )" + " order by hi_psnjob.showorder, bd_psndoc.code";
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[10]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[11]));// 人员类别
				defdocVO.setDef2(PubTools.getStringValue(psnojb[12]));// 部门
				defdocVO.setDef3(PubTools.getStringValue(psnojb[7]));// 组织
				defdocVO.setDef4(PubTools.getStringValue(psnojb[8]));// 人员信息
				defdocVO.setDef5(PubTools.getStringValue(psnojb[9]));// 人员组织关系
				defdocVO.setDef6(PubTools.getStringValue(psnojb[14]));// 岗位
				defdocVO.setDef7(PubTools.getStringValue(psnojb[20]));// 生效日期
				defdocVO.setDef8(PubTools.getStringValue(psnojb[21]));// 手机号
				defdocVO.setDef9(PubTools.getStringValue(psnojb[22]));// 试用类型
				defdocVO.setDef10(PubTools.getStringValue(psnojb[23]));// 职务职级
				defdocVO.setDef12(PubTools.getStringValue(psnojb[13]));// 职务
				defdocVO.setDef13(PubTools.getStringValue(psnojb[24]));// 岗位序列
				defdocVO.setShowvalue(defdocVO.getCode() + " " + defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 休假登记信息(销假)
	 */
	private Object psnjobxiaojRefModel(String pk_org, String pk, String userid) throws BusinessException {
		StringBuffer sql = new StringBuffer();
		sql.append("select bd_psndoc.name,bd_psndoc.code,tbm_leavereg.pk_leavereg,tbm_timeitem.timeitemname,tbm_leavereg.pk_leavetype,tbm_leavereg.pk_leavetypecopy,tbm_leavereg.pk_psnjob,tbm_leavereg.pk_psndoc,hi_psnjob.pk_org,hi_psnjob.pk_dept,hi_psnjob.pk_job,hi_psnjob.pk_post, ")
		.append("tbm_leavereg.leaveyear,tbm_leavereg.leavemonth,tbm_leavereg.leavebegintime,tbm_leavereg.leaveendtime,tbm_leavereg.leavehour,tbm_leavereg.relatetel,tbm_leavereg.resteddayorhour,tbm_leavereg.leavebegindate,tbm_leavereg.leaveenddate,tbm_leavereg.realdayorhour, ")
		.append("tbm_leavereg.restdayorhour,tbm_leavereg.freezedayorhour,tbm_leavereg.usefuldayorhour,tbm_leavereg.lactationholidaytype,tbm_leavereg.lactationhour,tbm_leavereg.islactation  ")
		.append("from tbm_leavereg LEFT JOIN bd_psndoc ON tbm_leavereg.pk_psndoc = bd_psndoc.pk_psndoc LEFT JOIN tbm_timeitem ON tbm_leavereg.pk_leavetype = tbm_timeitem.pk_timeitem left join hi_psnjob on tbm_leavereg.pk_psnjob = hi_psnjob.pk_psnjob left join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc WHERE isnull(bd_psndoc.dr,0) = 0 and isnull(tbm_timeitem.dr,0) = 0 and isnull(hi_psnjob.dr,0) = 0 AND tbm_leavereg.leaveenddate > ( SELECT NVL (MAX(enddate), '1971') FROM tbm_period WHERE sealflag = 'Y' AND pk_org = '"
				+ pk_org + "' ) AND tbm_leavereg.billsource = 0 ");
		if (pk == null || "".equals(pk)) {
			sql.append("AND tbm_leavereg.pk_leavereg NOT IN ( SELECT pk_leavereg FROM TBM_LEAVEOFF WHERE APPROVE_STATE NOT IN (0, 1)) ");
		} else {
			sql.append("AND tbm_leavereg.pk_leavereg in('" + pk + "')");
		}
		sql.append(" and sm_user.cuserid = '" + userid + "'");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[1]));
				defdocVO.setName(PubTools.getStringValue(psnojb[0]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[4]));// 休假类别
				defdocVO.setDef2(PubTools.getStringValue(psnojb[5]));// 休假类别copy
				defdocVO.setDef3(PubTools.getStringValue(psnojb[6]));// 人员工作记录
				defdocVO.setDef4(PubTools.getStringValue(psnojb[7]));// 人员基本信息
				defdocVO.setDef5(PubTools.getStringValue(psnojb[8]));// 组织
				defdocVO.setDef6(PubTools.getStringValue(psnojb[9]));// 部门
				defdocVO.setDef7(PubTools.getStringValue(psnojb[12]));// 休假年度
				defdocVO.setDef8(PubTools.getStringValue(psnojb[13]));// 休假期间
				defdocVO.setDef9(PubTools.getStringValue(psnojb[14]));// 开始时间
				defdocVO.setDef10(PubTools.getStringValue(psnojb[15]));// 结束时间
				defdocVO.setDef11(PubTools.getStringValue(psnojb[16]));// 休假时长
				defdocVO.setDef12(PubTools.getStringValue(psnojb[19]));// 开始日期
				defdocVO.setDef13(PubTools.getStringValue(psnojb[20]));// 结束日期
				defdocVO.setDef14(PubTools.getStringValue(psnojb[25]));// 哺乳时段
				defdocVO.setDef15(PubTools.getStringValue(psnojb[26]));// 单日哺乳时长
				defdocVO.setDef16(PubTools.getStringValue(psnojb[10]));// 岗位
				defdocVO.setDef17(PubTools.getStringValue(psnojb[11]));// 职能
//				defdocVO.setDef16(PubTools.getStringValue(psnojb[28]));// 职务级别
				defdocVO.setShowvalue(defdocVO.getDef9() + "-" + defdocVO.getDef10() + " " + defdocVO.getCode() + " " + defdocVO.getName().toString() + "  " + PubTools.getStringValue(psnojb[3]));
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}
 

	/**
	 * 3.客户基本信息 
	 */
	public RetDefDocVO customerDoc(String userid, String pk_org, String billtype, String pk, String pk_billtype, String queryPk_org,String scon) throws BusinessException {
		// 客商（ 根据 登陆用户->业务员->所属客商 ）
		// StringBuffer querySQL = new
		// StringBuffer(" select distinct cust.pk_customer,\n" +
		// "       cust.code,\n" + "       cust.name,\n" +
		// "       stor.pk_stordoc,\n"
		// + "       (select max(pk_address)\n" +
		// "          from bd_custaddress addr\n" +
		// "         where addr.dr = 0\n" +
		// "           and addr.isdefault = 'Y'\n"
		// + "           and addr.pk_customer = cust.pk_customer)\n" +
		// "  from bd_customer cust\n" +
		// " inner join bd_custsale csale on cust.pk_customer = csale.pk_customer\n"
		// + " inner join sm_user sm on (csale.respperson = sm.pk_psndoc)\n" +
		// "  left join bd_stordoc stor on (cust.def8 = stor.name and stor.dr = 0)\n"
		// + " where cust.dr = 0\n"
		// + "   and csale.dr = 0\n" + "   and sm.dr = 0\n" +
		// "  and sm.cuserid = '" + userid + "'\n" + "   and csale.pk_org = '" +
		// pk_org + "'\n"
		// + "   and isnull(cust.frozenflag, 'N') != 'Y'\n" +
		// "   and cust.enablestate = 2\n" + " ");

		/*
		 * StringBuffer querySQL = new StringBuffer(
		 * " select distinct bd_customer.pk_customer,bd_customer.code,bd_customer.name,bd_customer.custprop\n "
		 * + " from bd_customer \n" +
		 * " left join bd_stordoc stor on (bd_customer.def8 = stor.name and stor.dr = 0) "
		 * + " where \n" + " isnull(bd_customer.dr,0)=0\n" +
		 * " and isnull(bd_customer.frozenflag, 'N') != 'Y'\n" +
		 * " and bd_customer.enablestate = 2 \n" );
		 */
	    //dongl 数据量大的数据分页显示 + PC端模糊查询
		String strSQL = "select pk_customer, code, name\n";
		strSQL = strSQL + " from (SELECT ROWNUM AS rowno, bd_customer.* from bd_customer  where nvl(dr,0)=0 ";
		strSQL = strSQL +" and (enablestate = 2)\n" +
				"   and pk_customer in\n" + 
				"   (select pk_customer\n" + 
				"   from bd_custorg\n" + 
				"   where pk_org in ('"+pk_org+"')\n" + 
				"   and enablestate = 2\n" + 
				"   union\n" + 
				"   select pk_customer\n" + 
				"   from bd_customer\n" + 
				"   where (pk_org = '"+pk_group+"' or\n" + 
				"   pk_org = '"+pk_org+"'))";
		if(null != scon && scon.length() >0 ){
			strSQL = strSQL +" and ( code like '%"+scon+"%' or name like '%"+scon+"%' ) ";
		}
		strSQL = strSQL +" AND ROWNUM <= 50) table_customer WHERE table_customer.rowno >= 0";
		
		if (null != pk && !"".equals(pk)) {
			strSQL ="select pk_customer, code, name\n" +
				    "  from bd_customer\n" + 
				    " where isnull(dr,0)=0\n" + 
				    " and pk_customer in ('" + pk + "')  ";
		} 

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(strSQL, new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
//			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3])); // 所属仓库
			// defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));
			// //
			// 默认地址
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 4.仓库
	 */
	public RetDefDocVO storDoc(String billtype, String pk, String pk_org, String csendstockorgvid) throws BusinessException {
		// 仓库
		// String pk_org = "0001E410000000004I7R"; // 李彬 2016年9月7日09:52:55
		// （袁颖，惠乐之源下单 发货是在徳股份，所以 用
		// 徳股份的仓库）
		/*
		 * StringBuffer querySQL = new
		 * StringBuffer("select ").append(" stor.pk_stordoc "
		 * ).append(",stor.code ").append(",stor.name ").
		 * append(" from bd_stordoc stor ").append(" where stor.dr=0 ")
		 * .append(" and stor.pk_org = '" + pk_org + "' ");
		 */
		StringBuffer querySQL = new StringBuffer("select pk_stordoc, code, name, storaddr\n" + "  from bd_stordoc stor\n" + " where (enablestate = 2 and gubflag = 'N' and isdirectstore = 'N')\n" + "   and (enablestate = 2)\n" + "   and ((pk_stordoc in\n" + "       (select pk_stordoc\n"
				+ "            from bd_agentstore\n" + "           where pk_stockorg = '" + pk_org + "') or\n" + "       pk_org = '" + pk_org + "'))\n"

				);
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and name<>'直运仓' and stor.pk_stordoc in('" + pk + "') order by stor.code");
		} else {
			querySQL.append(" and name<>'直运仓' order by stor.code ");
		}
		if (null != csendstockorgvid && !"".equals(csendstockorgvid)) {
			querySQL = new StringBuffer("select bd_stordoc.pk_stordoc,bd_stordoc.code,bd_stordoc.name ").append(" from bd_stordoc\n" + "  join org_orgs on org_orgs.pk_org=bd_stordoc.pk_org and isnull(org_orgs.dr,0)=0\n" + " where isnull(bd_stordoc.dr, 0) = 0\n" + "   and bd_stordoc.name<>'直运仓'\n"
					+ "   and org_orgs.pk_vid = '" + csendstockorgvid + "' ");
		}
		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer("select bd_stordoc.pk_stordoc,bd_stordoc.code,bd_stordoc.name ").append(" from bd_stordoc where isnull(bd_stordoc.dr, 0) = 0 and bd_stordoc.pk_stordoc  in('" + pk + "') ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 5.物料 
	 */
	public RetDefDocVO meaterialDoc(String pk_org, String billtype, String pk) throws BusinessException {
		// 物料基本信息（多版本）
		StringBuffer querySQL = new StringBuffer("select ").append(" wl.pk_material ").append(",wl.code ").append(",wl.name ").append(",wl.materialspec ").append(",wl.materialtype ").append(",wl.pk_measdoc ")
				// 计量单位pk
				.append(",md.name , bd_marbasclass.name, bd_marbasclass.pk_marbasclass")
				// 计量单位name
				.append(" from bd_material wl join bd_marbasclass on wl.pk_marbasclass=bd_marbasclass.pk_marbasclass and isnull(bd_marbasclass.dr,0)=0").append(" inner join bd_materialsale wlsale on wl.pk_material = wlsale.pk_material ")
				.append(" left join bd_measdoc md on md.pk_measdoc = wl.pk_measdoc ").append(" where wl.dr=0 and wlsale.dr=0 and wl.enablestate= 2 ").append(" and wlsale.pk_org = '" + pk_org + "' ")
				// .append(" and ( wl.code like '21%' or wl.code like '23%' or wl.code like '25%' ) ")
				;
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and wl.pk_material in('" + pk + "') order by wl.code  ");
		} else {
			querySQL.append(" order by wl.code ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			throw new BusinessException("客户没维护地址!");
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3])); // 规格
			defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4])); // 型号
			defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[5])); // 计量单位pk
			defDocVOs[i].setDef4(PuPubVO.getString_TrimZeroLenAsNull(obj2[6])); // 计量单位name
			defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(obj2[7])); // 物料分类name
			defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(obj2[8])); // 物料分类pk
			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName() + "【" + defDocVOs[i].getDef1() + "】【" + defDocVOs[i].getDef2() + "】");
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 6.物料基本信息（多版本）-业务员
	 */
	public RetDefDocVO meaterialVPsnDoc(String pk_org, String userid, String billtype, String pk) throws BusinessException {
		// 物料基本信息（多版本）
		StringBuffer querySQL = new StringBuffer("select ").append(" wl.pk_material ").append(",wl.code ").append(",wl.name ").append(",wl.materialspec ")
				.append(",wl.materialtype ")
				.append(",wl.pk_measdoc ")
				// 计量单位pk
				.append(",md.name ")
				// 计量单位name
				.append(" from bd_material wl ").append(" inner join bd_materialsale wlsale on wl.pk_material = wlsale.pk_material ").append(" left join bd_measdoc md on md.pk_measdoc = wl.pk_measdoc ").append(" where wl.dr=0 and wlsale.dr=0 and wl.enablestate=2 ")
				.append(" and wlsale.pk_org = '" + pk_org + "' ").append(" and wl.pk_material in ( ").append("	select yw.materialvid ").append("	from dqyyx_jh_ywyinfo y ").append("	inner join dqyyx_jh_ywyinfo_wl yw on y.pk_dqyyx_jh_ywyinfo = yw.pk_dqyyx_jh_ywyinfo ")
				.append("	where y.dr=0 and yw.dr=0 ").append("	and y.ywy = '" + userid + "' ").append(" ) ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and wl.pk_material in('" + pk + "') order by wl.code");
		} else {
			querySQL.append(" order by wl.code ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3])); // 规格
			defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[4])); // 型号
			defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[5])); // 计量单位pk
			defDocVOs[i].setDef4(PuPubVO.getString_TrimZeroLenAsNull(obj2[6])); // 计量单位name

			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName() + "【" + defDocVOs[i].getDef1() + "】【" + defDocVOs[i].getDef2() + "】");
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	 

	/**
	 * 8.客户收获地址
	 */
	public RetDefDocVO custAdressDoc(String pk_org, String userid, String billtype, ConditionVO[] condVOs, String ccustomerid, String pk) throws BusinessException {
		// String pk_customer = condVOs.length > 2 ?
		// PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) : null;
		// // 客户收获地址
		// StringBuffer querySQL = new
		// StringBuffer("select distinct ").append(" a.pk_address ").append(",a.detailinfo ").append(",ca.isdefault ").append(" from bd_address a ")
		// .append(" inner join bd_custaddress ca on a.pk_address = ca.pk_address ").append(" inner join bd_customer cust on ca.pk_customer = cust.pk_customer ")
		// .append(" inner join bd_custsale csale on cust.pk_customer = csale.pk_customer ").append(" inner join sm_user sm on (csale.respperson = sm.pk_psndoc) ")
		// .append(" where a.dr=0 and ca.dr=0 ").append(" and sm.cuserid   = '"
		// + userid + "' ").append(" and csale.pk_org = '" + pk_org + "' ")
		// .append(pk_customer == null ? "" : " and ca.pk_customer = '" +
		// pk_customer + "' ").append(" order by ca.isdefault desc ");
		// String sql =
		// "select bd_custaddress.pk_address, bd_region.name|| bd_address.detailinfo||'  '||bd_linkman.name||'  '||bd_linkman.address||'  '||bd_linkman.cell||'  '||bd_linkman.phone as def1\n"
		String sql = "select bd_custaddress.pk_address, bd_region.name|| bd_address.detailinfo as def1,bd_linkman.name,bd_linkman.cell " + "from bd_custaddress\n" + "left join bd_address on bd_custaddress.pk_address=bd_address.pk_address and isnull(bd_address.dr,0)=0\n"
				+ "left join bd_region  on bd_address.city=bd_region.pk_region and isnull(bd_region.dr,0)=0\n" + "left join bd_linkman on bd_custaddress.pk_linkman=bd_linkman.pk_linkman and isnull(bd_linkman.dr,0)=0\n" + "where isnull(bd_custaddress.dr,0)=0\n" + "and pk_customer='" + ccustomerid
				+ "'";
		if (null != pk && !"".equals(pk)) {
			sql = "select bd_custaddress.pk_address, bd_region.name|| bd_address.detailinfo as def1,bd_linkman.name,bd_linkman.cell " + "from bd_custaddress\n" + "left join bd_address on bd_custaddress.pk_address=bd_address.pk_address and isnull(bd_address.dr,0)=0\n"
					+ "left join bd_region  on bd_address.city=bd_region.pk_region and isnull(bd_region.dr,0)=0\n" + "left join bd_linkman on bd_custaddress.pk_linkman=bd_linkman.pk_linkman and isnull(bd_linkman.dr,0)=0\n" + "where isnull(bd_custaddress.dr,0)=0\n"
					+ "and bd_custaddress.pk_address in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			// defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setDef8(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setDef9(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 9.营销组织
	 */
	public RetDefDocVO saleOrgDoc(String pk_org, String userid, String billtype, String pk) throws BusinessException {
		if (!CacheConfig.getUserMap().containsKey(userid))
			return null;

		StringBuffer querySQL = new StringBuffer("select ").append("pk_dqyyx_yxfy_yxzzdoc ").append(",saleorgcode ").append(",saleorgname ").append(" from ").append(" dqyyx_yxfy_yxzzdoc ").append(" where isnull(dr,0)=0 ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and pk_dqyyx_yxfy_yxzzdoc in('" + pk + "') ");
		} else {
			querySQL.append(" and pk_org = '" + pk_org + "' ");
		}
		if ("O".equals(CacheConfig.getUserMap().get(userid)[2])) {
			String value = CacheConfig.getUserMap().get(userid)[0];
			// 登录用户属于 营销组织管理员，查询负责的营销组织
			querySQL.append(" and (person1='" + value + "' or person2 = '" + value + "' or person3 = '" + value + "' or person4 = '" + value + "' or person5 = '" + value + "') ");
		}
		querySQL.append(" order by saleorgcode ");

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 10.营销部门
	 */
	public RetDefDocVO saleDeptDoc(String pk_org, String userid, String billtype, ConditionVO[] condVOs, String pk) throws BusinessException {

		if (!CacheConfig.getUserMap().containsKey(userid))
			return null;

		StringBuffer querySQL = new StringBuffer("select ").append(" b.pk_dqyyx_yxfy_yxzzdoc_b ").append(",b.saledeptcode ").append(",b.saledeptname ").append(" from dqyyx_yxfy_yxzzdoc_b b").append(" inner join dqyyx_yxfy_yxzzdoc h on h.pk_dqyyx_yxfy_yxzzdoc = b.pk_dqyyx_yxfy_yxzzdoc ")
				.append(" where isnull(b.dr,0)=0 ").append(" and isnull(h.dr,0) = 0 ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and b.pk_dqyyx_yxfy_yxzzdoc_b in('" + pk + "') ");
		} else {
			querySQL.append(" and h.pk_org = '" + pk_org + "' ");
		}
		if (null == pk && "D".equals(CacheConfig.getUserMap().get(userid)[2])) {
			// 登录用户属于部门负责人，查询负责的部门
			String value = CacheConfig.getUserMap().get(userid)[0];
			querySQL.append(" and (b.person1='" + value + "' or b.person2 = '" + value + "' or b.person3 = '" + value + "' or b.person4 = '" + value + "' or b.person5 = '" + value + "') ");
		} else {
			// 根据上级组织过滤，添加过滤条件
			if (condVOs.length <= 2 || PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) == null) {
				// APP没有传递 上级组织
				return null;
			}
			String[] orgpks = condVOs[2].getValue().split(",");

			querySQL.append(" and b.pk_dqyyx_yxfy_yxzzdoc in (''");
			for (String orgpk : orgpks) {
				querySQL.append(",'" + orgpk + "'");
			}
			querySQL.append(") ");
		}
		querySQL.append(" order by b.saledeptcode ");

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 11.零售商
	 */
	public RetDefDocVO shopkeeperDoc(String pk_org, String userid, String billtype, ConditionVO[] condVOs, String pk) throws BusinessException {

		if (!CacheConfig.getUserMap().containsKey(userid))
			return null;

		String[] deptpks = condVOs[2].getValue().split(",");
		StringBuffer querySQL = new StringBuffer("select ").append("pk_dqyyx_yxfy_lss ").append(",code ").append(",name ").append(" from ").append(" dqyyx_yxfy_lss ").append(" where isnull(dr,0)=0 ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and b.pk_dqyyx_yxfy_yxzzdoc_b in('" + pk + "') ");
		} else {
			querySQL.append(" and h.pk_org = '" + pk_org + "' ");
		}
		if (null == pk && "L".equals(CacheConfig.getUserMap().get(userid)[2])) {
			// 登录用户属于零售商负责人，查询负责的零售商
			String value = CacheConfig.getUserMap().get(userid)[0];
			querySQL.append(" and (person1='" + value + "' or person2 = '" + value + "' or person3 = '" + value + "' or person4 = '" + value + "' or person5 = '" + value + "') ");
		} else {
			// 根据上级营销部门过滤，添加过滤条件
			if (condVOs.length <= 2 || PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) == null)
				return null;
			querySQL.append(" and pk_dqyyx_yxfy_yxzzdoc_b in (''");
			for (String deptpk : deptpks) {
				querySQL.append(",'" + deptpk + "'");
			}
			querySQL.append(") ");
		}

		querySQL.append(" order by code ");

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}

		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);

		return refDocVO;
	}

	/**
	 * 12.门店（客户档案）
	 */
	public RetDefDocVO shopcustDoc(String pk_org, String userid, String billtype, ConditionVO[] condVOs, String pk) throws BusinessException {

		if (!CacheConfig.getUserMap().containsKey(userid))
			return null;

		String[] lsspks = condVOs[2].getValue().split(",");
		StringBuffer querySQL = new StringBuffer("select ").append("pk_customer ").append(",code ").append(",name ").append(" from ").append(" bd_customer ").append(" where isnull(dr,0)=0 ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and pk_customer in('" + pk + "') ");
		}

		if ("M".equals(CacheConfig.getUserMap().get(userid)[2])) {
			// 登录用户属于门店的专管业务员，查询负责的门店
			String value = CacheConfig.getUserMap().get(userid)[0];
			querySQL.append(" and def23 != '~' ");
			querySQL.append(" and pk_customer in (");
			querySQL.append(" select pk_customer from bd_custsale where isnull(dr,0)=0 ");
			querySQL.append(" and respperson = '" + value + "' and pk_org = '" + pk_org + "' ");
			querySQL.append(" )");

		} else {
			// 根据上级零售商过滤，添加过滤条件
			if (condVOs.length <= 2 || PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) == null)
				return null;

			querySQL.append(" and def23 in (''");
			for (String lsspk : lsspks) {
				querySQL.append(",'" + lsspk + "'");
			}
			querySQL.append(") ");
		}

		querySQL.append(" order by code ");

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}

		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);

		return refDocVO;
	}

	/**
	 * 13.开票客户
	 */
	public RetDefDocVO invoiceCustomer(String pk_org, String userid, String billtype, ConditionVO[] condVOs, String pk) throws BusinessException {
		// 根据客户id带出
		String pk_customer = condVOs.length > 2 ? PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) : null;
		if (pk_customer == null)
			return null;
		StringBuffer querySQL = new StringBuffer("select ").append("sale.billingcust ").append(",cust.code ").append(",cust.name ").append(" from bd_custsale sale ").append(" inner join bd_customer cust on sale.billingcust = cust.pk_customer ").append(" where isnull(sale.dr,0) = 0 ")
				.append(" and sale.pk_customer = '" + pk_customer + "' ").append(" and sale.pk_org = '" + pk_org + "'");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and sale.pk_custsale in('" + pk + "') ");
		} else {
			querySQL.append(" and sale.pk_org = '" + pk_org + "'");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list == null || list.size() == 0) {
			// 没有开票客户，默认返回客户本身pk，code，name
			ArrayList<Object[]> cust = (ArrayList<Object[]>) dao.executeQuery("select pk_customer,code,name from bd_customer where pk_customer = '" + pk_customer + "'", new ArrayListProcessor());
			MDefDocVO[] defDocVOs = new MDefDocVO[1];
			defDocVOs[0].setBilltype(billtype);
			defDocVOs[0].setPk(PuPubVO.getString_TrimZeroLenAsNull(cust.get(0)[0]));
			defDocVOs[0].setCode(PuPubVO.getString_TrimZeroLenAsNull(cust.get(0)[1]));
			defDocVOs[0].setName(PuPubVO.getString_TrimZeroLenAsNull(cust.get(0)[2]));
			defDocVOs[0].setShowvalue(defDocVOs[0].getCode());
			RetDefDocVO refDocVO = new RetDefDocVO();
			refDocVO.setDefdocvos(defDocVOs);
			return refDocVO;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 14.费用类型
	 */
	public RetDefDocVO feeTypeDoc(String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select ").append(" pk_billtypeid  ").append(",pk_billtypecode  ").append(", billtypename  ").append(" from ").append(" bd_billtype ").append(" where isnull(dr,0)=0 ").append(" and parentbilltype ='35' ").append(" and islock = 'N' ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and pk_billtypeid in('" + pk + "') ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 15.会计期间
	 */
	public RetDefDocVO accountPeriodDoc(String billtype, String pk) throws BusinessException {

		String sql ="select pk_accperiodmonth,substr(yearmth,0,4) as endmonth,yearmth\n" +
					"  from bd_accperiodmonth\n" + 
					" where nvl(dr,0) = 0\n" + 
					" order by yearmth";
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 16.收支项目
	 */
	public RetDefDocVO incomeItemsDoc(String pk_billtype, String billtype, String pk, String pk_org) throws BusinessException {
		/*String querySQL = "select distinct pk_inoutbusiclass ,code ,name ,pk_parent\n" + "from bd_crossrestdata bd_crossrestdata\n" + "join bd_inoutbusiclass on bd_crossrestdata.data=bd_inoutbusiclass.pk_inoutbusiclass\n" + "where bd_crossrestdata.pk_restraint in\n"
				+ "(SELECT bd_crossrulerest.pk_restraint AS pk_restraint\n" + "  FROM bd_crossrulerest bd_crossrulerest\n" + " WHERE bd_crossrulerest.pk_rule IN\n" + "       (SELECT DISTINCT bd_crossrulescope.pk_rule\n" + "          FROM bd_crossrule\n"
				+ "         INNER JOIN bd_crossrulescope ON bd_crossrule.pk_rule = bd_crossrulescope.pk_rule\n" + "         INNER JOIN bd_crossbusimap ON bd_crossrulescope.pk_scope = bd_crossbusimap.pk_scope\n" + "         WHERE bd_crossrule.enablestate = 2\n"
				+ "           AND bd_crossrulescope.pk_entity in\n" + "               (select pk_billtypeid\n" + "                  from bd_billtype )\n" + "  )) order by code";
		if (null != pk_billtype && "2647".equals(pk_billtype)) {
			querySQL = "select pk_inoutbusiclass ,code ,name ,pk_parent \n" + "  from bd_inoutbusiclass\n" + " where (enablestate = 2)\n" + "   and ((exists\n" + "        (select 1\n" + "            from bd_inoutuse a\n"
					+ "           where a.pk_inoutbusiclass = bd_inoutbusiclass.pk_inoutbusiclass\n" + "             and a.pk_org = '" + pk_org + "') or\n" + "        pk_org = '" + pk_org + "'))\n" + " order by code";
		}
		querySQL = "select pk_inoutbusiclass ,code ,name ,pk_parent \n" + "  from bd_inoutbusiclass\n" + " where (enablestate = 2)\n" + "   and ((exists\n" + "        (select 1\n" + "            from bd_inoutuse a\n"
				+ "           where a.pk_inoutbusiclass = bd_inoutbusiclass.pk_inoutbusiclass\n" + "             and a.pk_org = '" + pk_org + "') or\n" + "        pk_org = '" + pk_org + "'))\n" + " order by code";

		if (null != pk && !"".equals(pk)) {
			if (pk.contains(",")) {
				String[] pks = pk.split(",");
				for (int i = 0; i < pks.length; i++) {
					// 汉字的Unicode取值范围
					String regex = "[\u4e00-\u9fa5]";
					Pattern pattern = Pattern.compile(regex);
					Matcher match = pattern.matcher(pks[i]);
					if (!match.find() && pks[i].length() == 20) {
						pk = pks[i];
					}
				}
			}
			querySQL = " select pk_inoutbusiclass ,code ,name ,pk_parent\n" + " from bd_inoutbusiclass where isnull(dr,0)=0 and pk_inoutbusiclass    in('" + pk + "')  order by code";
		}
		 */ 
		/*	String sql = "select pk_inoutbusiclass, code, name, pk_parent\n" +
		        "              from bd_inoutbusiclass\n" + 
		        "             where pk_inoutbusiclass  in(select distinct pk_inoutbusiclass\n" + 
		        "        from bd_crossrestdata bd_crossrestdata\n" + 
		        "        join bd_inoutbusiclass on bd_crossrestdata.data=bd_inoutbusiclass.pk_inoutbusiclass\n" + 
		        "        where bd_crossrestdata.pk_restraint in\n" + 
		        "        (SELECT bd_crossrulerest.pk_restraint AS pk_restraint\n" + 
		        "          FROM bd_crossrulerest bd_crossrulerest\n" + 
		        "         WHERE bd_crossrulerest.pk_rule IN\n" + 
		        "               (SELECT DISTINCT bd_crossrulescope.pk_rule\n" + 
		        "                  FROM bd_crossrule\n" + 
		        "                INNER JOIN bd_crossrulescope ON bd_crossrule.pk_rule = bd_crossrulescope.pk_rule\n" + 
		        "                 INNER JOIN bd_crossbusimap ON bd_crossrulescope.pk_scope = bd_crossbusimap.pk_scope\n" + 
		        "              WHERE bd_crossrule.enablestate = 2\n" + 
		        "                   AND bd_crossrulescope.pk_entity in\n" + 
		        "                       (select pk_billtypeid\n" + 
		        "                          from bd_billtype\n" + 
		        "                         where pk_billtypecode = '"+ pk_billtype +"')\n" + 
		        "          )))order by code;";*/

		String sql ="select  b.pk_inoutbusiclass, b.code, b.name, b.pk_parent,a.code as def1\n" +
					"from  (select pk_inoutbusiclass, code, name, pk_parent\n" + 
					"        from bd_inoutbusiclass\n" + 
					"       where nvl(dr,0)=0\n" + 
					"         and pk_parent is not null\n" + 
					"         and pk_inoutbusiclass not in (select  pk_parent\n" + 
					"                                        from bd_inoutbusiclass\n" + 
					"                                       where nvl(dr,0)=0\n" + 
					"                                         and pk_parent is not null)) a\n" + 
					" right join\n" + 
					"(select pk_inoutbusiclass, code, name, pk_parent\n" + 
					"              from bd_inoutbusiclass\n" + 
					"             where pk_inoutbusiclass  in (select pk_inoutbusiclass from bd_inoutbusiclass)\n" + 
					"          and pk_inoutbusiclass in(select distinct pk_inoutbusiclass\n" + 
					"        from bd_crossrestdata bd_crossrestdata\n" + 
					"        join bd_inoutbusiclass on bd_crossrestdata.data=bd_inoutbusiclass.pk_inoutbusiclass\n" + 
					"        where bd_crossrestdata.pk_restraint in\n" + 
					"        (SELECT bd_crossrulerest.pk_restraint AS pk_restraint\n" + 
					"          FROM bd_crossrulerest bd_crossrulerest\n" + 
					"         WHERE bd_crossrulerest.pk_rule IN\n" + 
					"               (SELECT DISTINCT bd_crossrulescope.pk_rule\n" + 
					"                  FROM bd_crossrule\n" + 
					"                INNER JOIN bd_crossrulescope ON bd_crossrule.pk_rule = bd_crossrulescope.pk_rule\n" + 
					"                 INNER JOIN bd_crossbusimap ON bd_crossrulescope.pk_scope = bd_crossbusimap.pk_scope\n" + 
					"              WHERE bd_crossrule.enablestate = 2\n" + 
					"                   AND bd_crossrulescope.pk_entity in\n" + 
					"                       (select pk_billtypeid\n" + 
					"                          from bd_billtype\n" + 
					"                         where pk_billtypecode = '"+pk_billtype+"')\n" + 
					"                  )))order by code) b  on a.pk_inoutbusiclass = b.pk_inoutbusiclass\n" ;

		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list1 = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		//如果sql不为空，说明有控制规则，然后sql会在list中再次执行
		if (list1 == null || list1.size() == 0) {
			//如果为空，说明没有控制规则，那么走以下sql
			sql="select pk_inoutbusiclass, code, name, pk_parent,code as def1\n" +
				"from bd_inoutbusiclass\n" + 
				"where pk_inoutbusiclass not in (select pk_parent from bd_inoutbusiclass)\n" + 
				"and pk_inoutbusiclass in(select distinct pk_inoutbusiclass\n" + 
				" 						  from bd_inoutbusiclass\n" + 
				" 						 where isnull(dr,0)=0 \n" + 
				"                         and (enablestate = 2)\n" + 
				"  						  and ((exists (select 1\n" + 
				"            							from bd_inoutuse a\n" + 
				"           							where a.pk_inoutbusiclass = bd_inoutbusiclass.pk_inoutbusiclass\n" + 
				"            							 and a.pk_org = '"+ pk_org +"') or pk_org = '"+ pk_org +"')))order by code;";
		}
		//如果pk不为空，说明是查询，回显走以下sql，sql优先级1.2.3
		if (null != pk && !"".equals(pk)) {
			sql = " select pk_inoutbusiclass ,code ,name ,pk_parent,''\n" + " from bd_inoutbusiclass where isnull(dr,0)=0 and pk_inoutbusiclass in('" + pk + "')  order by code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			// return null;
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()]; 
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			/*S*/
			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
		}

		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);

		return refDocVO;
	}

	/**
	 * 18.集团
	 */
	public RetDefDocVO groupDoc(String billtype, String pk) throws BusinessException {
		String condition = "1=1 and isnull(dr,0)=0  order by code ";

		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(GroupVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				GroupVO deptdocVO = (GroupVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_group());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}
	
	/**
	 * 18.业务单元+集团
	 */
	public RetDefDocVO allOrgs(String billtype, String pk) throws BusinessException {
		String sql = "";
		if(null!=pk){
			sql="select code,name,pk_org,pk_vid,pk_corp\n" +
				"  from org_orgs\n" + 
				"  where isnull(dr,0)=0\n" + 
				"  and pk_org in('" + pk + "')";
		}else{
			sql="select code,name,pk_org,pk_vid,pk_corp\n" +
				"  from org_orgs\n" + 
				" where (isbusinessunit = 'Y' or pk_org in (select pk_group from org_group))\n" + 
				"   and (enablestate = 2)\n" +
				"   and isnull(dr,0)=0" + 
				" order by code";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()]; 
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 19.国家地区
	 */
	public RetDefDocVO countryRegionDoc(String billtype, String pk) throws BusinessException {
		String condition = "1=1 and isnull(dr,0)=0  order by code ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_country in('" + pk + "') and 1=1 and isnull(dr,0)=0  order by code ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(CountryZoneVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				CountryZoneVO deptdocVO = (CountryZoneVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_country());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 20.协同用户
	 */
	public RetDefDocVO cooperUserDoc(String billtype, String pk, String pk_org, String pk_allorg, String pk_billtype, String userid, String queryPk_org) throws BusinessException {
		if (pk_allorg != null) {
			pk_org = pk_allorg;
		}
		ArrayList<SuperVO> list = new ArrayList<SuperVO>();

		String condition = "1=1 and isnull(dr,0)=0  order by user_code ";
		if (null != pk && !"".equals(pk)) {
			condition = (" cuserid in('" + pk + "') and 1=1 and isnull(dr,0)=0  order by user_code ");
		}
		list = (ArrayList<SuperVO>) dao.retrieveByClause(CpUserVO.class, condition);

		// 数据权限节点
		if (null != pk_billtype
				&& (pk_billtype.equals("MUJ00209") || pk_billtype.equals("MUJ00216") || pk_billtype.equals("MUJ00214") || pk_billtype.equals("MUJ00219") || pk_billtype.equals("MUJ00205") || pk_billtype.equals("MUJ00203") || pk_billtype.equals("MUJ00208") || pk_billtype.equals("HMH10508")
						|| pk_billtype.equals("MUJ00204") || pk_billtype.equals("MUJ00207") || pk_billtype.equals("SA07") || pk_billtype.equals("SA08") || pk_billtype.equals("SA08"))) {
			// 根据用户查询角色是否有数据权限
			// ArrayList<DataRightVO> role = getRole(userid);

			String sql = null;
			// if(role.size()>0){
			// //数据权限
			// if(null!=queryPk_org){
			// sql =
			// "select * from cp_user where pk_org='"+queryPk_org+"' and isnull(dr,0）=0 and enablestate = 2";
			// }else if(null!=pk_org){
			// sql =
			// "select * from cp_user where pk_org='"+pk_org+"' and isnull(dr,0）=0 and enablestate = 2";
			// }
			// }else{
			// //角色没权限，只查当前登录的业务员
			// sql =
			// "select * from cp_user where cuserid='"+userid+"' and isnull(dr,0）=0 and enablestate = 2";
			// }
			list = (ArrayList<SuperVO>) dao.executeQuery(sql, new BeanListProcessor(CpUserVO.class));
		}

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				CpUserVO deptdocVO = (CpUserVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getUser_code());
				defdocVO.setName(deptdocVO.getUser_name());
				defdocVO.setPk(deptdocVO.getCuserid());
				defdocVO.setShowvalue(deptdocVO.getUser_code() + " " + deptdocVO.getUser_name());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 21.CRM业务类型
	 */
	public RetDefDocVO CRMBusiTypeDoc(String billtype, String datatype, String pk_billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "1=1 and enablestate=2 and isnull(dr,0)=0  order by code ";

		if ("CRM商机业务类型".equals(datatype)) {
			condition = " pk_group<>'GLOBLE00000000000000' and bus_type=4 and " + condition;
		}
		// 收款通知单业务类型
		if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("MUJ00208"))) {
			condition = " bus_type='24' and pk_group<>'GLOBLE00000000000000' and " + condition;
		}
		if (null != billtype && !"".equals(billtype) && (billtype.equals("MUE1"))) {
			// condition =
			// " code in('YWLX0002' ,'YWLX0011' )and 1=1 and isnull(dr,0)=0  order by code ";
		}
		if (null != pk && !"".equals(pk)) {
			condition = (" pk_biz_type in('" + pk + "') and 1=1 and isnull(dr,0)=0  order by code ");
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(BizTypeVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// BizTypeVO deptdocVO = (BizTypeVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(deptdocVO.getCode());
		// defdocVO.setName(deptdocVO.getVname());
		// defdocVO.setPk(deptdocVO.getPk_biz_type());
		// defdocVO.setShowvalue(deptdocVO.getCode() + " " +
		// deptdocVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	/**
	 * 21.CRM客户业务类型
	 */
	public RetDefDocVO CRMCustTypeDoc(String billtype, String datatype, String pk_billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = " vname in('主客户' ,'企业客户' ) and 1=1 and isnull(dr,0)=0 order by code ";

		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(BizTypeVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// BizTypeVO deptdocVO = (BizTypeVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(deptdocVO.getCode());
		// defdocVO.setName(deptdocVO.getVname());
		// defdocVO.setPk(deptdocVO.getPk_biz_type());
		// defdocVO.setShowvalue(deptdocVO.getCode() + " " +
		// deptdocVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	/**
	 * 21.CRM线索业务类型
	 */
	public RetDefDocVO CRMXsBusiTypeDoc(String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "1=1 and enablestate=2 and isnull(dr,0)=0 and bus_type='3'  and  pk_group<>'GLOBLE00000000000000'  order by code ";
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(BizTypeVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// BizTypeVO deptdocVO = (BizTypeVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(deptdocVO.getCode());
		// defdocVO.setName(deptdocVO.getVname());
		// defdocVO.setPk(deptdocVO.getPk_biz_type());
		// defdocVO.setShowvalue(deptdocVO.getCode() + " " +
		// deptdocVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	/**
	 * 22.客户基本分类
	 * dongliang  2020年2月13日12:57:49
	 */
	public RetDefDocVO customerBaseClassDoc(String billtype, String pk,String pk_org) throws BusinessException {
		// 业务类型

		StringBuffer querySQL = new StringBuffer(/*
				"select pk_customer, code, name, mnecode, null type4, pk_customer, pk_custclass\n" +
						"  from bd_customer\n" + 
						" where 11 = 11\n" + 
						"   and (enablestate = 2)\n" + 
						"   and ((pk_customer in\n" + 
						"       (select pk_customer\n" + 
						"            from bd_custorg\n" + 
						"           where pk_org in ('"+pk_org+"')\n" + 
						"             and enablestate = 2\n" + 
						"          union\n" + 
						"          select pk_customer\n" + 
						"            from bd_customer\n" + 
						"           where (pk_org = '"+pk_org+"' or\n" + 
						"                 pk_org = '"+pk_org+"'))))\n" + 
						"\n" + 
				" order by code"*/

				"select pk_customer, code, name\n" +
				"  from bd_customer\n" + 
				" where 11 = 11\n" + 
				"   and (enablestate = 2)\n" + 
				"   and ((pk_customer in\n" + 
				"       (select pk_customer\n" + 
				"            from bd_custorg\n" + 
				"           where pk_org in ('"+pk_org+"')\n" + 
				"             and enablestate = 2\n" + 
				"          union\n" + 
				"          select pk_customer\n" + 
				"            from bd_customer\n" + 
				"           where (pk_org = '"+pk_group+"' or\n" + 
				"                 pk_org = '"+pk_org+"'))))\n" + 
				"   and pk_custclass in\n" + 
				"       (select pk_custclass\n" + 
				"          from bd_custclass\n" + 
				"         where ((pk_org = '"+pk_group+"' or\n" + 
				"               pk_org = '"+pk_org+"') and enablestate = 2))\n" + 
				" order by code");
		if(null != pk && !"".equals(pk)){
			querySQL = new StringBuffer(" select pk_customer, code, name from bd_customer where nvl(dr,0) = 0 and pk_customer in ('"+pk+"')");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 23.客户等级
	 */
	public RetDefDocVO customerLevel(String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "1=1  and isnull(dr,0)=0 order by vcode ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_customerlevel in('" + pk + "') and 1=1 and isnull(dr,0)=0 order by vcode  ");
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(CustomerLevelVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// CustomerLevelVO deptdocVO = (CustomerLevelVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(deptdocVO.getVcode());
		// defdocVO.setName(deptdocVO.getVname());
		// defdocVO.setPk(deptdocVO.getPk_customerlevel());
		// defdocVO.setShowvalue(deptdocVO.getVcode() + " " +
		// deptdocVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	/**
	 * 24.地区分类
	 */
	public RetDefDocVO regionTypeDoc(String billtype, String pk) throws BusinessException {
		String condition = "1=1  and  enablestate = 2  and isnull(dr,0)=0  order by code ";
		if (null != pk && !"".equals(pk)) {
			condition = (" pk_areacl in('" + pk + "') and 1=1 and isnull(dr,0)=0  order by code ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(AreaclassVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				AreaclassVO deptdocVO = (AreaclassVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_areacl());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 25.CRM客户
	 */
	public RetDefDocVO CRMCoustomerDoc(String billtype, String pk_billtype, String pk_org, String userid, String pk, String owner, String queryPk_org) throws BusinessException {

		String pk_group = "0001A510000000000HV2";
		// 业务类型
		String cond = "((1 = 1 AND pk_account <> 'null' AND enablestate = 2 AND pk_group = '" + AppContext.getInstance().getPkGroup() + "') AND  cuma_account.enablestate = '2') AND enablestate = 2 AND isnull(dr, 0) = 0 ";
		String condition = "";

		// 根据单据设置,读单据模板参数,过滤参照
		if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("MUJ00208"))) {
			condition = " bofficialaccount='Y'/*正式客户*/ and creator='" + userid + "' and " + cond; // and
			// pk_org='"
			// +
			// pk_org
			// +
			// "'
		}
		if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("MUJ00203"))) {
			condition = " creator='" + userid + "' and " + cond; // pk_org='" +
			// pk_org +
			// "' and
		}
		if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("MUJ00209"))) {
			condition = " pk_account in (select data_id " + "  from CAPUB_REL_USER " + "  where pk_entity = '7ec0ecab-9ede-40a8-b3cc-c2ba0506c837' " + "   and pk_user = '" + userid + "' " + "   and usertype = 2 " + "   and dr = 0 ) " + " and creator='" + userid + "' and " + cond; // pk_org='"
			// +
			// pk_org
			// +
			// "'
			// and
		}

		if (null != owner && !"".equals(owner)) {
			condition = ("  creator  ='" + owner + "' and isnull(dr,0)=0 ");
		}
		ArrayList<SuperVO> list = new ArrayList<SuperVO>();
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_account    in('" + pk + "') and 1=1 and isnull(dr,0)=0 ");
			// list = (ArrayList<SuperVO>) dao.retrieveByClause(AccountVO.class,
			// condition);//客户根据分配的业务单元查询
		} else {
			String con = "";
			String isorg = " pk_account in ( select pk_account from cuma_account where enablestate = '2' and isnull (dr, 0) = 0 and creator = '" + userid + "' )  and isnull (dr, 0) = 0 and enablestate = '2' ";
			if ("".equals(condition)) {
				con = isorg;
			} else if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("MUJ00209") || pk_billtype.equals("MUJ00203") || pk_billtype.equals("MUJ00214"))) {
				con = " creator='" + userid + "' and " + condition;
			} else {
				con = isorg + " and " + condition;
			}

			// list = (ArrayList<SuperVO>)
			// dao.retrieveByClause(AccountVO.class,con);//客户根据分配的业务单元查询
			String sql = "select * from cuma_account where " + con;
			// list = (ArrayList<SuperVO>) dao.executeQuery(sql, new
			// BeanListProcessor(AccountVO.class));

			// 数据权限,根据用户的角色,角色是否分配权限. 有分配则查询有分配组织的数据, 没有角色权限则只能查询自已的数据
			String pk_entity = "97e418d2-d8cd-44d8-8ac6-d14bf9da52aa";

			if (null != pk_billtype && (pk_billtype.equals("MUJ00209") || pk_billtype.equals("MUJ00216") || pk_billtype.equals("MUJ00214") || pk_billtype.equals("MUJ00219") || pk_billtype.equals("MUJ00205") || pk_billtype.equals("MUJ00203") || pk_billtype.equals("MUJ00208"))) {
				// 根据用户查询角色是否有数据权限
				// ArrayList<DataRightVO> role = getRole(userid);
				//
				// //分配权限的业务员，根据queryPk_org查询模板的组织字段过滤客户
				// if(role.size()>0){
				//
				// //根据搜索查询框的组织查询
				// if(null!=queryPk_org){
				// sql =
				// "select * from cuma_account where pk_org='"+queryPk_org+"' and isnull(dr,0)=0";
				// list = (ArrayList<SuperVO>) dao.executeQuery(sql, new
				// BeanListProcessor(AccountVO.class));
				//
				// }else if(null!=pk_org){
				// //新增时，根据登录组织查询
				// sql =
				// "select * from cuma_account where pk_org='"+pk_org+"' and isnull(dr,0)=0";
				// list = (ArrayList<SuperVO>) dao.executeQuery(sql, new
				// BeanListProcessor(AccountVO.class));
				// }
				// }
			}
			// String data_sql ="select * " +
			// " from cuma_account" +
			// " where enablestate = '2' " +
			// " and ( pk_account in( select data_id\n" +
			// "          from CAPUB_REL_USER\n" +
			// "         where pk_group = '"+pk_group+"'\n" +
			// "           and pk_entity in (SELECT mdid\n" +
			// "                               FROM sm_permission_res\n" +
			// "                              WHERE resourcecode = 'account')\n"
			// +
			// "           and pk_user = '" + userid + "'\n" +
			// "           and usertype = 2\n" +
			// "           and isnull(dr,0)= 0)" +
			// " or pk_org in (SELECT pk_org\n" +
			// "                FROM cp_orgs\n" +
			// "               WHERE (orglevel = '1' OR orglevel = '2')\n" +
			// "                 AND pk_orglevel1 = '"+pk_group+"'\n" +
			// "                 and pk_org in (select distinct pk_org\n" +
			// "                                  from cp_roleorg a\n" +
			// "                                  left outer join cp_userrole b\n"
			// +
			// "                                    on a.pk_role = b.pk_role\n"
			// +
			// "                                  left outer join cp_roleresp c\n"
			// +
			// "                                    on c.pk_role = b.pk_role\n"
			// +
			// "                                  left outer join cp_resp_res d\n"
			// +
			// "                                    on c.pk_responsibility = d.pk_responsibility\n"
			// +
			// "                                 where b.pk_user = '" + userid +
			// "'\n" +
			// "                                   and d.pk_res = '0001ZU1000000003KK65')\n"
			// +
			// "               )\n" +
			// "               )";

			// String sqlall =
			// " select distinct * from ( "+sql1+" union all "+data_sql+" ) ";
			// ArrayList<AccountVO> dataList = (ArrayList<AccountVO>)
			// dao.executeQuery(sqlall, new BeanListProcessor(AccountVO.class));
			// if(dataList.size()>0){
			// list.addAll(dataList);
			// }
		}

		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				// AccountVO accountVO = (AccountVO) list.get(i);
				// MDefDocVO defdocVO = new MDefDocVO();
				// defdocVO.setBilltype(billtype);
				// defdocVO.setCode(accountVO.getVcode());
				// defdocVO.setName(accountVO.getVname());
				// defdocVO.setPk(accountVO.getPk_account());
				// defdocVO.setShowvalue(accountVO.getVname());
				// listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	 

	/**
	 * 27.省
	 */
	public RetDefDocVO provinceDoc(String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "(1 = 1 and pk_father = '~' and ( enablestate = 2 )  and isnull(dr, 0) = 0) ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_region in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(RegionVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				RegionVO regionVO = (RegionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(regionVO.getCode());
				defdocVO.setName(regionVO.getName());
				defdocVO.setPk(regionVO.getPk_region());
				defdocVO.setShowvalue(regionVO.getCode() + " " + regionVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 28.市
	 */
	public RetDefDocVO cityDoc(String pk_province, String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "(1 = 1 and pk_father = '" + pk_province + "' and ( enablestate = 2 )  and isnull(dr, 0) = 0) ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_region in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(RegionVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				RegionVO regionVO = (RegionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(regionVO.getCode());
				defdocVO.setName(regionVO.getName());
				defdocVO.setPk(regionVO.getPk_region());
				defdocVO.setShowvalue(regionVO.getCode() + " " + regionVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 29.区县 //业务类型
	 */
	public RetDefDocVO regionDoc(String pk_city, String billtype, String pk) throws BusinessException {
		String condition = "(1 = 1 and pk_father = '" + pk_city + "' and ( enablestate = 2 )  and isnull(dr, 0) = 0) ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_region in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(RegionVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				RegionVO regionVO = (RegionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(regionVO.getCode());
				defdocVO.setName(regionVO.getName());
				defdocVO.setPk(regionVO.getPk_region());
				defdocVO.setShowvalue(regionVO.getCode() + " " + regionVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 30.乡镇
	 */
	public RetDefDocVO defDocTown(String pk_region, String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "(1 = 1 and pk_father = '" + pk_region + "' and ( enablestate = 2 )  and isnull(dr, 0) = 0) ";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_region in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(RegionVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				RegionVO regionVO = (RegionVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(regionVO.getCode());
				defdocVO.setName(regionVO.getName());
				defdocVO.setPk(regionVO.getPk_region());
				defdocVO.setShowvalue(regionVO.getCode() + " " + regionVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 31.经济类型(自定义档案)
	 */
	public RetDefDocVO defDocEcon(String billtype, String pk) throws BusinessException {
		// 业务类型
		String condition = "11 = 11 and ( enablestate = 2 ) and ( ( 1 = 1 ) and pk_defdoclist = '1009ZZ100000000034NZ' ) order by code";
		if (null != pk && !"".equals(pk)) {
			condition = ("  pk_defdoc in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(DefdocVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				DefdocVO defdoc = (DefdocVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(defdoc.getCode());
				defdocVO.setName(defdoc.getName());
				defdocVO.setPk(defdoc.getPk_defdoc());
				defdocVO.setShowvalue(defdoc.getCode() + " " + defdoc.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	 

	/**
	 * 32.协同公共对象
	 */
	public RetDefDocVO cooperPubObj(String billtype, String pk) throws BusinessException {
		String condition = "";
		condition = "select pk_doc,doc_code,doc_name from cp_doc where isnull(dr,0)=0";
		if (null != pk && !"".equals(pk)) {
			condition = (" select pk_doc,doc_code,doc_name from cp_doc where pk_doc in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(condition, new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 32.1 结算成本域 dl Cost domain 成本域
	 */
	public RetDefDocVO costdomain(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select pk_costregion,code,name from org_costregion where isnull(dr,0)=0 and pk_org='" + pk_org + "' and 1=1 ");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	

	/**
	 * 32.3 单位银行账户 dongliang
	 */
	public RetDefDocVO fkyhzh(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select  pk_banktype,code,name from bd_banktype where 11 = 11 and nvl(dr,0)=0 order by code ");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				//				if(null!=obj2[1] && obj2[1].toString().length()>8){
				//					String obj21 = obj2[1].toString();
				//					int num = obj21.length()-8;
				//					String start = obj21.substring(0,4);
				//					String end = obj21.substring(obj21.length()-4,obj21.length());
				//					StringBuffer nums = new StringBuffer(start);
				//					for(int n=0; n<num; n++){
				//						nums.append("*");
				//					}
				//					nums.append(end);
				//					defDocVOs[i].setCode(nums.toString());
				////					defDocVOs[i].setName(nums.toString());
				//				}else{
				//					defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				////					defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				//				}
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 32.3 报销单号 dongliang 2018年12月10日10:55:20
	 */
	public RetDefDocVO jkcode(String billtype, String pk_billtype, String pk, String pk_org, String userid, String pk_busitem, String cjkybje, String pk_item) throws BusinessException {
		if (null == cjkybje || "".equals(cjkybje)) {
			cjkybje = "0";
		}
		StringBuffer querySQL = new StringBuffer("select a.pk_busitem, a.name, a.djbh, a.ybje, a.djlxbm, a.jkbxr,sum(a.yjye) as yjye ,isnull(a.billno,'空') as billno " + "from (select b.pk_busitem, d.name,a.djbh, b.ybje,a.djlxbm,a.jkbxr,a.yjye, m.billno\n" + "  from er_busitem b\n"
				+ "  join er_jkzb a\n" + "    on a.pk_jkbx = b.pk_jkbx  " + " LEFT JOIN er_mtapp_detail m  " + " ON b.pk_mtapp_detail = m.pk_mtapp_detail\n" + "   left join bd_inoutbusiclass d\n" + "    on d.pk_inoutbusiclass  = b.szxmid\n" + "    join bd_psndoc p\n"
				+ "    on p.pk_psndoc = a.jkbxr\n" + "    join sm_user u\n" + "    on p.pk_psndoc = u.pk_psndoc" + " where    a.yjye > 0\n" + "   and a.dr = 0\n" + " and a.pk_org = '"+pk_org+"'  and u.cuserid = '" + userid + "'\n" + "   and a.djzt = 3\n"
				+ "   and isnull(b.dr, 0) = 0 and  ( b.yjye > 0 and b.pk_jkbx in (select pk_jkbx from er_busitem where pk_item is null or pk_item = '~' or  srcbilltype ='2611'))\n" + " union all\n" +

				"  select b.pk_busitem, d.name, a.djbh, b.ybje, a.djlxbm, a.jkbxr, " + cjkybje + "  as yjye ,m.billno \n" + "        from er_busitem b\n" + "        join er_jkzb a on a.pk_jkbx = b.pk_jkbx\n" + " LEFT JOIN er_mtapp_detail m  " + " ON b.pk_mtapp_detail = m.pk_mtapp_detail\n"
				+ "        left join bd_inoutbusiclass d on d.pk_inoutbusiclass = b.szxmid\n" + "        where b.pk_busitem='" + pk_busitem + "' " + " ) a " + "group by a.pk_busitem, a.name, a.djbh, a.ybje, a.djlxbm, a.jkbxr, a.billno order by a.djbh\n");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));// pk
				defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[1])); // 收支项目
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));// 单号
				defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[6])); // 原币金额
				defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(obj2[0])); // 数据来源
				defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(obj2[7])); // 数据来源
				defDocVOs[i].setShowvalue("申请单：【" + defDocVOs[i].getDef6() + "】单号：【" + defDocVOs[i].getName() + "】收支项目：【" + defDocVOs[i].getDef1() + "】冲借款金额：【" + defDocVOs[i].getDef3() + "】 ");
			}
		}
		if (null != pk) {
			defDocVOs = new MDefDocVO[1];

			defDocVOs[0] = new MDefDocVO();
			defDocVOs[0].setBilltype(billtype);
			defDocVOs[0].setPk(pk);// pk
			defDocVOs[0].setCode(pk); // 收支项目
			defDocVOs[0].setName(pk);// 单号
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 32.3 申请单号 dongliang 2019年3月13日15:40:50
	 */
	public RetDefDocVO mtappCode(String billtype, String pk_billtype, String pk, String pk_org, String userid, String billmaker, String cjkybje) throws BusinessException {
		String usable_amout = "";
		String rest_amount = "";
		String pk_djdl = "";
		if ("264".equals(pk_billtype)) {
			usable_amout = "er_mtapp_detail.orig_amount - isnull(p.exe_amount,0) as usable_amout";
			pk_djdl = "pk_djdl in ( N'bx')";
		}
		if ("263".equals(pk_billtype)) {
			usable_amout = "er_mtapp_detail.orig_amount - isnull(p.exe_amount,0) as usable_amout";
			rest_amount = " and er_mtapp_detail.rest_amount > 0";
			pk_djdl = "pk_djdl in ( N'bx' , N'jk' )";
		}
		if (null == pk) {
			StringBuffer querySQL = new StringBuffer("select pk_mtapp_detail, name, orig_amount, billno, usable_amout\n" + "  from (SELECT  er_mtapp_detail.orig_amount,p.exe_amount,er_mtapp_detail.billno,er_mtapp_detail.pk_mtapp_detail,\n" + "               name,\n" + "               "
					+ usable_amout + "\n" + "          FROM er_mtapp_detail er_mtapp_detail\n" + "          join er_mtapp_bill a\n" + "            on er_mtapp_detail.pk_mtapp_bill = a.pk_mtapp_bill\n" + "          left join bd_inoutbusiclass d\n"
					+ "            on d.pk_inoutbusiclass = er_mtapp_detail.pk_iobsclass\n" + "          LEFT JOIN (select sum ( exe_amount )as exe_amount,pk_mtapp_detail from er_mtapp_billpf where  " + pk_djdl + "	GROUP BY pk_mtapp_detail) p\n"
					+ "            on p.pk_mtapp_detail = er_mtapp_detail.pk_mtapp_detail\n" + "         WHERE er_mtapp_detail.orig_amount > isnull((SELECT SUM(p.exe_amount + p.pre_amount)\n" + "                                      FROM er_mtapp_billpf p\n"
					+ "                                     WHERE p.pk_mtapp_detail =\n" + "                                           er_mtapp_detail.pk_mtapp_detail\n" + "                                       AND p.pk_djdl = N'bx' \n"
					+ "                                     GROUP BY p.pk_mtapp_detail),\n" + "                                    0)\n" + "           AND er_mtapp_detail.billmaker IN\n" + "               (select p.pk_psndoc\n" + "                  from sm_user s\n"
					+ "                  join bd_psndoc p\n" + "                    on s.pk_psndoc = p.pk_psndoc\n" + "                 where s.cuserid = '" + userid + "')\n" + "           AND er_mtapp_detail.pk_tradetype IN (N'2611')\n" + "           AND er_mtapp_detail.effectstatus = 1\n"
					+ "           AND er_mtapp_detail.close_status = 2  " + rest_amount + "\n" + "           AND er_mtapp_detail.dr = 0)b\n" + " group by pk_mtapp_detail, name, orig_amount, billno, usable_amout\n" + " ORDER BY billno");
			ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
			if (list.size() == 0 && (null == pk || "".equals(pk))) {
				return null;
			}
			MDefDocVO[] defDocVOs = null;
			if (list != null && list.size() > 0) {
				defDocVOs = new MDefDocVO[list.size()];
				for (int i = 0; i < list.size(); i++) {
					Object[] obj2 = list.get(i);
					defDocVOs[i] = new MDefDocVO();
					defDocVOs[i].setBilltype(billtype);
					defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));// pk
					defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[1])); // 收支项目
					defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));// 单号
					DecimalFormat df = new DecimalFormat("0.00");
					df.setRoundingMode(RoundingMode.HALF_UP);
					defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(df.format(obj2[2])));
					defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));// 金额
					if ("0E-8".equals(defDocVOs[i].getDef6())) {
						defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(df.format(0))); // 数据来源
					} else {
						defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(df.format(obj2[4]))); // 数据来源
					}

					defDocVOs[i].setShowvalue("单号：【" + defDocVOs[i].getName() + "】收支项目：【" + defDocVOs[i].getDef1() + "】金额：【" + defDocVOs[i].getDef3() + "】 " + "可用金额：【" + defDocVOs[i].getDef5() + "】 ");
				}
			}
			RetDefDocVO refDocVO = new RetDefDocVO();
			refDocVO.setDefdocvos(defDocVOs);
			return refDocVO;
		}
		if (null != pk) {
			String query = "select pk_mtapp_bill,billno from er_mtapp_bill where  pk_mtapp_bill in ('" + pk + "')  and dr=0";
			ArrayList<Object[]> lists = (ArrayList<Object[]>) dao.executeQuery(query.toString(), new ArrayListProcessor());
			if (lists.size() == 0 && (null == pk || "".equals(pk))) {
				return null;
			}
			MDefDocVO[] defDocVOs = null;
			if (lists != null && lists.size() > 0) {
				defDocVOs = new MDefDocVO[lists.size()];
				for (int i = 0; i < lists.size(); i++) {
					Object[] obj2 = lists.get(i);
					defDocVOs[i] = new MDefDocVO();
					defDocVOs[i].setBilltype(billtype);
					defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));// pk
					defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1])); // 收支项目
					defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));// 单号
					defDocVOs[i].setShowvalue(defDocVOs[i].getName());
				}
			}
			RetDefDocVO refDocVO = new RetDefDocVO();
			refDocVO.setDefdocvos(defDocVOs);
			return refDocVO;
		}
		return null;
	}
 
 
	/**
	 * 32.6 费用支付单位 dongliang 2018年11月22日14:05:19
	 */
	public RetDefDocVO company(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select pk_vid,code,name\n" + 
	"  from org_orgs_v\n" + " where 11 = 11\n" + "   and (enablestate = 2)\n" + 
				"   and (isbusinessunit = 'Y' and pk_group = '0001A110000000000638' and\n" + 
	"       vstartdate =\n"
				+ "       isnull((select max(a.vstartdate) vstartdate\n" + 
	"              from org_orgs_v a\n" + 
				"             where vstartdate <= '2018-11-22 23:59:59'\n" + 
	"               and a.pk_org = org_orgs_v.pk_org),\n" + "            (select min(a.vstartdate) vstartdate\n"
				+ "               from org_orgs_v a\n" + "              where vstartdate > '2018-11-22 23:59:59'\n" + "                and a.pk_org = org_orgs_v.pk_org)) and\n" + "       org_orgs_v.enablestate = 2)\n" + " order by code");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 33. 销售组织
	 */
	public RetDefDocVO saleOrg(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select pk_salesorg,code,name from org_salesorg where isnull(dr,0)=0  and 1=1");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and pk_salesorg in('" + pk + "')");
		}
		if (null != pk_billtype && !"".equals(pk_billtype) && (pk_billtype.equals("SA09") || pk_billtype.equals("SA10"))) {
			querySQL.append(" and pk_fatherorg='0001A5100000000031OL' or pk_salesorg='" + pk_org + "'");
		}
		if (null != pk_billtype && pk_billtype.equals("MUJ00102")) {
			querySQL.append(" and pk_salesorg in (SELECT pk_org\n" + "  FROM cp_orgs\n" + " WHERE (orglevel = '1' OR orglevel = '2')\n" + "   AND pk_orglevel1 = '" + pk_group + "'\n " + "   and pk_orglevel1<>pk_org " + "   and pk_org in (select distinct pk_org\n"
					+ "                    from cp_roleorg a\n" + "                    left outer join cp_userrole b on a.pk_role = b.pk_role\n" + "                    left outer join cp_roleresp c on c.pk_role = b.pk_role\n"
					+ "                    left outer join cp_resp_res d on c.pk_responsibility = d.pk_responsibility\n" + "                   where b.pk_user = '" + userid + "'\n" + "                     and d.pk_res = '" + pk_res + "')\n" + " )");
		}
		querySQL.append(" order by code ");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 33. 出入库类型
	 */
	public RetDefDocVO tradeType(String billtype, String pk_billtype, String pk_group, String pk) throws BusinessException {
		String andsql = "";
		if (null != pk_billtype && !"".equals(pk_billtype) && null != pk_group && !"".equals(pk_group)) {
			andsql = " isnull(dr,0)=0 and parentbilltype='" + pk_billtype + "' and (islock<>'Y' or islock is null) and pk_group<>'global00000000000000' and pk_group = '" + pk_group + "' order by pk_billtypecode";
		}
		if (null != pk_billtype && !"".equals(pk_billtype)) {
			andsql = " isnull(dr,0)=0 and parentbilltype='" + pk_billtype + "' and (islock<>'Y' or islock is null) and pk_group<>'global00000000000000' order by pk_billtypecode";
		}

		if (null != pk_billtype && !"".equals(pk_billtype) && null != pk_group && !"".equals(pk_group)) {
			andsql = " isnull(dr,0)=0 and pk_billtypecode='" + pk_billtype + "' and (islock<>'Y' or islock is null) and pk_group<>'global00000000000000'  order by pk_billtypecode";
		}
		if (null != pk_billtype && !"".equals(pk_billtype) && null != pk_group && !"".equals(pk_group) && pk_billtype.equals("2611")) {
			andsql = " dr = 0 and pk_billtypecode='" + pk_billtype + "' and (islock<>'Y' or islock is null) and pk_group<>'global00000000000000'  order by pk_billtypecode";
		}
		if (null != pk_billtype && "2611".equals(pk_billtype) && (null == pk_group || "".equals(pk_group))) {
			andsql = " pk_billtypecode = '2611' ";
		}
		if (null != pk && !"".equals(pk)) {
            pk = getPKs(pk);			
			andsql = (" pk_billtypeid  in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}

		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(BilltypeVO.class, andsql);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				BilltypeVO saleorgVO = (BilltypeVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(saleorgVO.getPk_billtypecode());
				defdocVO.setName(saleorgVO.getBilltypename());
				defdocVO.setPk(saleorgVO.getPk_billtypeid());
				defdocVO.setShowvalue(saleorgVO.getPk_billtypecode() + " " + saleorgVO.getBilltypename());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 34. 收款协议
	 */
	public RetDefDocVO incomeDoc(String billtype, String pk) throws BusinessException {
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveAll(IncomeVO.class);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				IncomeVO saleorgVO = (IncomeVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(saleorgVO.getCode());
				defdocVO.setName(saleorgVO.getName());
				defdocVO.setPk(saleorgVO.getPk_income());
				defdocVO.setShowvalue(saleorgVO.getCode() + " " + saleorgVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 35. 商机
	 */
	public RetDefDocVO opportunity(String billtype, String pk_org, String userid, String pk) throws BusinessException {
		String condition = " pk_org='" + pk_org + "' and owner='" + userid + "' ";
		if (null != pk && !"".equals(pk)) {
			condition = (" pk_opportunity in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(OpportunityVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// OpportunityVO saleorgVO = (OpportunityVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setName(saleorgVO.getVname());
		// defdocVO.setPk(saleorgVO.getPk_opportunity());
		// defdocVO.setShowvalue(saleorgVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	/**
	 * 36. 产品线
	 */
	public RetDefDocVO prodline(String billtype, String pk) throws BusinessException {
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveAll(ProdLineVO.class);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				ProdLineVO saleorgVO = (ProdLineVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setName(saleorgVO.getName());
				defdocVO.setCode(saleorgVO.getCode());
				defdocVO.setPk(saleorgVO.getPk_prodline());
				defdocVO.setShowvalue(saleorgVO.getCode() + " " + saleorgVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 37.结算方式
	 */
	public RetDefDocVO balatype(String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select pk_balatype,name,code from bd_balatype where isnull(dr,0)=0 and enablestate=2 order by code");
		if(null != pk && !"".equals(pk)){
			querySQL = new StringBuffer("select pk_balatype,name,code from bd_balatype where isnull(dr,0)=0 and enablestate=2 and pk_balatype in ('"+pk+"')");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 ) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;

		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));

				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * currtypedoc 38.品牌档案
	 */
	public RetDefDocVO branddoc(String billtype, String pk) throws BusinessException {
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(BrandDocVO.class, " enablestate =2 and isnull(dr,0)=0 ");
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				BrandDocVO saleorgVO = (BrandDocVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setName(saleorgVO.getName());
				defdocVO.setCode(saleorgVO.getCode());
				defdocVO.setPk(saleorgVO.getPk_brand());
				defdocVO.setShowvalue(saleorgVO.getCode() + " " + saleorgVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 39.币种档案
	 */
	public RetDefDocVO currtypedoc(String billtype, String pk) throws BusinessException {
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveAll(CurrtypeVO.class);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				CurrtypeVO saleorgVO = (CurrtypeVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setName(saleorgVO.getName());
				defdocVO.setCode(saleorgVO.getCode());
				defdocVO.setPk(saleorgVO.getPk_currtype());
				defdocVO.setShowvalue(saleorgVO.getCode() + " " + saleorgVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 41.所属门店
	 */
	public RetDefDocVO store(String billtype, String pk, String pk_org, String pk_billtype, String userid) throws BusinessException {
		// String condition =
		// " 11 = 11 and ( enablestate = 2 ) and isnull(dr,0)=0  and pk_org='"+pk_org+"' order by code ";
		String condition = "";
		ArrayList<OrgVO> list = new ArrayList<OrgVO>();
		if (null != pk && !"".equals(pk)) {
			condition = "select * from cp_orgs where pk_org in('" + pk + "') and isnull(dr,0)=0  order by code ";
			list = (ArrayList<OrgVO>) dao.executeQuery(condition, new BeanListProcessor(OrgVO.class));
		} else {
			// 有权限的节点
			if (null != pk_billtype && (pk_billtype.equals("SA07") || pk_billtype.equals("SA08"))) {
				condition = "SELECT *\n" + "  FROM cp_orgs\n" + " WHERE (orglevel = '1' OR orglevel = '2')\n" + "   AND pk_orglevel1 = '" + pk_group + "'\n " + "   and pk_orglevel1<>pk_org " + "   and pk_org in (select distinct pk_org\n" + "                    from cp_roleorg a\n"
						+ "                    left outer join cp_userrole b on a.pk_role = b.pk_role\n" + "                    left outer join cp_roleresp c on c.pk_role = b.pk_role\n" + "                    left outer join cp_resp_res d on c.pk_responsibility = d.pk_responsibility\n"
						+ "                   where b.pk_user = '" + userid + "'\n" + "                     and d.pk_res = '" + pk_res + "')\n" + " ORDER BY code";
				list = (ArrayList<OrgVO>) dao.executeQuery(condition, new BeanListProcessor(OrgVO.class));
			} else {
				// 其他没权限的节点
				condition = "select * from cp_orgs where pk_org = '" + pk_org + "' and isnull(dr,0)=0  order by code ";
				list = (ArrayList<OrgVO>) dao.executeQuery(condition, new BeanListProcessor(OrgVO.class));
			}
		}
		// list = (ArrayList<SuperVO>) dao.retrieveByClause(OrgVO.class,
		// condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				OrgVO docVO = (OrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(docVO.getCode());
				defdocVO.setName(docVO.getName());
				defdocVO.setPk(docVO.getPk_org());
				defdocVO.setShowvalue(docVO.getCode() + " " + docVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 42.CRM联系人
	 */
	public RetDefDocVO contact(String billtype, String pk_account, String pk) throws BusinessException {
		String condition = " 11 = 11 and ( enablestate = 2 ) and isnull(dr,0)=0 and pk_account='" + pk_account + "' ";
		if (null != pk && !"".equals(pk)) {
			condition = (" pk_contact in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(ContactVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// ContactVO docVO = (ContactVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(docVO.getVcode());
		// defdocVO.setName(docVO.getVname());
		// defdocVO.setPk(docVO.getPk_contact());
		// if(null!=docVO.getVcode() && !"".equals(docVO.getVcode())){
		// defdocVO.setShowvalue(docVO.getVcode() + " " + docVO.getVname());
		// }else{
		// defdocVO.setShowvalue(docVO.getVname());
		// }
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	// 业务员
	public RetDefDocVO saleManDoc(String billtype, String pk, String pk_org, String pk_org_financial, String userid, String pk_billtype, String queryPk_org) throws BusinessException {
		StringBuffer querySQL = new StringBuffer();
		if (null != pk && !"".equals(pk)) {
			querySQL.append("select bd_psndoc.code," + "bd_psndoc.name," + "bd_psndoc.pk_psndoc," + "bd_psndoc.mobile," + "org_dept.name deptname," + "org_dept.pk_dept  pk_dept ," + "org_corp.name unitname " + "from bd_psndoc " + "inner join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc "
					+ "inner join org_dept on bd_psnjob.pk_dept = org_dept.pk_dept " + "inner join org_corp on org_corp.pk_corp = org_dept.pk_org " + "where isnull(bd_psndoc.dr, 0) = 0 " + "and isnull(org_dept.dr, 0) = 0 " + "and isnull(org_corp.dr, 0) = 0 ");
			if (pk_org_financial != null && pk_org_financial.length() > 0) {
				querySQL.append(" and bd_psndoc.pk_org='" + pk_org_financial + "'");
			} else {
				querySQL.append(" and bd_psndoc.pk_org='" + pk_org + "'");
			}
			querySQL.append("and bd_psnjob.ismainjob='Y' and bd_psndoc.pk_psndoc  in('" + pk + "') " + "order by bd_psndoc.name");
		} else {
			querySQL.append("select bd_psndoc.code," + "bd_psndoc.name," + "bd_psndoc.pk_psndoc," + "bd_psndoc.mobile," + "org_dept.name deptname," + "org_dept.pk_dept  pk_dept ," + "org_corp.name unitname " + "from bd_psndoc " + "inner join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc "
					+ "inner join org_dept on bd_psnjob.pk_dept = org_dept.pk_dept " + "inner join org_corp on org_corp.pk_corp = org_dept.pk_org " + "where isnull(bd_psndoc.dr, 0) = 0 " + "and isnull(org_dept.dr, 0) = 0 " + "and isnull(org_corp.dr, 0) = 0 ");
			if (pk_org_financial != null && pk_org_financial.length() > 0) {
				querySQL.append(" and org_dept.pk_org='" + pk_org_financial + "'");
			} else {
				querySQL.append(" and bd_psndoc.pk_org='" + pk_org + "'");
			}
			querySQL.append("and bd_psnjob.ismainjob='Y' " + "order by bd_psndoc.name");
		}

		// 权限
		if (null != pk_billtype && pk_billtype.equals("MUJ00102") && null != queryPk_org) {
			ArrayList role = getRole(userid);
			querySQL = new StringBuffer("select bd_psndoc.code," + "bd_psndoc.name," + "bd_psndoc.pk_psndoc," + "bd_psndoc.mobile," + "org_dept.name deptname," + "org_dept.pk_dept  pk_dept ," + "org_corp.name unitname " + "from bd_psndoc "
					+ "inner join bd_psnjob on bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc " + "inner join org_dept on bd_psnjob.pk_dept = org_dept.pk_dept " + "inner join org_corp on org_corp.pk_corp = org_dept.pk_org " + "where isnull(bd_psndoc.dr, 0) = 0 " + "and isnull(org_dept.dr, 0) = 0 "
					+ "and isnull(org_corp.dr, 0) = 0 ");

			if (role.size() == 0) {
				querySQL.append(" and bd_psndoc.pk_psndoc in (select pk_psndoc from sm_user where cuserid='" + userid + "' and isnull(dr,0)=0) ");
			}
			querySQL.append(" and org_dept.pk_org = '" + queryPk_org + "' ");
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[2]));
				defdocVO.setDef1(PubTools.getStringValue(psnojb[3]));
				defdocVO.setDef2(PubTools.getStringValue(psnojb[4]));
				defdocVO.setDef3(PubTools.getStringValue(psnojb[5]));
				defdocVO.setDef4(PubTools.getStringValue(psnojb[6]));
				defdocVO.setShowvalue(defdocVO.getName() + psnojb[0] + psnojb[4]);// 2018-11-14
				// 10:03:52
				// dl
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	// 订单类型
	public RetDefDocVO orderTypeDoc(String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select ").append(" pk_billtypeid  ").append(", billtypename  ").append(" from ").append(" bd_billtype ").append(" where isnull(dr,0)=0 ").append(" and pk_billtypecode='30-Cxx-08' ").append(" and islock = 'N' ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append("and  pk_billtypeid in('" + pk + "') and 1=1 and isnull(dr,0)=0   ");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 8.客户收获地址
	 */
	public RetDefDocVO crmCustAdressDoc(String pk_org, ConditionVO[] condVOs, String billtype, String pk) throws BusinessException {
		String customer = condVOs.length > 2 ? PuPubVO.getString_TrimZeroLenAsNull(condVOs[2].getValue()) : null;
		if (customer == null || customer.length() == 0) {
			throw new BusinessException("请先选择主表的客户！");
		}

		// 客户收获地址
		StringBuffer querySQL = new StringBuffer("SELECT cust.pk_address,county. NAME || ' ' || regin. NAME || ' ' || region. NAME || ' ' || reg. NAME || ' ' || address.detailinfo AS addressname,"
				+ " linkman.name,linkman.cell FROM bd_customer cus LEFT JOIN bd_custaddress cust ON cust.pk_customer = cus.pk_customer" + " LEFT JOIN bd_address address ON address.pk_address = cust.pk_address LEFT JOIN bd_countryzone county ON county.pk_country = address.country"
				+ " LEFT JOIN bd_region region ON region.pk_region = address.city LEFT JOIN bd_region regin ON address.province = regin.pk_region" + " LEFT JOIN bd_region reg ON address.vsection = reg.pk_region left join bd_linkman linkman on linkman.pk_linkman = cust.pk_linkman ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append("where cust.pk_address in('" + pk + "')");
		} else {
			querySQL.append("where cust.pk_customer='" + customer + "'");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		for (int i = 0; i < list.size(); i++) {
			Object[] obj = list.get(i);
			if (obj[0] != null) {
				dao.executeUpdate("update bd_custaddress set def1='" + obj[1] + "' where  pk_address='" + obj[0] + "'");
			}
		}
		if (list == null || list.size() == 0) {
			throw new BusinessException("该客户暂时没有维护收货地址！");
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));// 联系人
			defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));// 联系人手机号
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 8.销售计划申请单
	 */
	public RetDefDocVO salePlayApply(String userid, String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer("select hh.pk_salesapply_h,HH.billno,HH.store,HH.billdate,HH.money," + "HH.lastmoney,hh.vdef2 from salesapply_h hh where hh.approvestatus='End' ");
		if (null != pk && !"".equals(pk)) {
			querySQL.append("and hh.pk_salesapply_h in('" + pk + "')  ");
		} else {
			querySQL.append("and hh.creator='" + userid + "'");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			throw new BusinessException("当前用户没有有效的销售计划申请单！");
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));// 所属门店
			// org
			defDocVOs[i].setDef2(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));// 单据日期
			defDocVOs[i].setDef3(PuPubVO.getString_TrimZeroLenAsNull(obj2[4]));// 销售金额
			defDocVOs[i].setDef4(PuPubVO.getString_TrimZeroLenAsNull(obj2[5]));// 去年销售金额
			defDocVOs[i].setDef5(PuPubVO.getString_TrimZeroLenAsNull(obj2[6]));// 年度
			defDocVOs[i].setDef6(PuPubVO.getString_TrimZeroLenAsNull(new UFDouble(0)));// 年度
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	// crm客户档案
	public RetDefDocVO crmCustomerDoc(String userid, String pk_org_financial, String billtype, String pk) throws BusinessException {

		StringBuffer querySQL = new StringBuffer();
		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer(" select pk_customer,name from bd_customer  where pk_customer IN (SELECT acc.pk_customer FROM  " + " cuma_account acc LEFT JOIN capub_rel_user rel ON rel.data_id = acc.pk_account LEFT JOIN bd_custsale sale ON "
					+ " sale.pk_customer = acc.pk_customer WHERE acc.pk_customer in('" + pk + "'))");
		} else {
			if (pk_org_financial == null || pk_org_financial.length() == 0) {
				throw new BusinessException("请选择主表的销售组织！");
			}
			querySQL = new StringBuffer(" select pk_customer,name from bd_customer  where pk_customer IN (SELECT acc.pk_customer FROM  " + " cuma_account acc LEFT JOIN capub_rel_user rel ON rel.data_id = acc.pk_account LEFT JOIN bd_custsale sale ON "
					+ " sale.pk_customer = acc.pk_customer WHERE rel.pk_user = '" + userid + "' AND sale.pk_org = '" + pk_org_financial + "')");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			throw new BusinessException("当前登录人暂时没有拥有的客户！");
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	public RetDefDocVO crmDeptDoc(String billtype, String pk) throws BusinessException {
		String sql = "";
		if (null != pk && !"".equals(pk)) {
			sql = " select pk_org,code,name from  cp_orgs where pk_org in('" + pk + "')  ";
		} else {
			sql = " select pk_org,code,name from  cp_orgs where pk_org in(select pk_dept from cp_user where  pk_base_doc in(select pk_psndoc from sm_user  where isnull(dr,0)=0))";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			throw new BusinessException("该用户暂时没有所属部门！");
		}
		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);
			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getName());
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 所负责门店查询（报表用）wanghy
	 */
	public RetDefDocVO org_orgs(String userid, String billtype, String pk) throws BusinessException {
		String condition = " 11 = 11 and ( enablestate = 2 ) and isnull(dr,0)=0 and pk_org in(SELECT cp_roleorg.pk_org FROM cp_userrole LEFT JOIN cp_role ON cp_userrole.pk_role = cp_role.pk_role " + "LEFT JOIN cp_roleorg ON cp_userrole.pk_role = cp_roleorg.pk_role WHERE cp_userrole.pk_user = '"
				+ userid + "' " + "AND cp_role.rolecode LIKE 'QY%' AND NVL (cp_userrole.dr, 0) = 0) order by code ";
		if (null != pk && !"".equals(pk)) {
			condition = " pk_org in('" + pk + "') and 1=1 and isnull(dr,0)=0 order by code  ";
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(OrgVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				OrgVO docVO = (OrgVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(docVO.getCode());
				defdocVO.setName(docVO.getName());
				defdocVO.setPk(docVO.getPk_org());
				defdocVO.setShowvalue(docVO.getCode() + " " + docVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	private Object customfrom(String userid, String billtype, String pk) throws BusinessException {
		/**
		 * 客户来源(自定义档案)
		 */
		// 业务类型
		String condition = "1=1 and isnull(dr,0)=0 and pk_defdoclist='1001A810000000000UO8'  and code !='01'";
		if (null != pk && !"".equals(pk)) {
			condition = " pk_defdoc in('" + pk + "')  ";
		}
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(DefdocVO.class, condition);
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				DefdocVO deptdocVO = (DefdocVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(deptdocVO.getCode());
				defdocVO.setName(deptdocVO.getName());
				defdocVO.setPk(deptdocVO.getPk_defdoc());
				defdocVO.setShowvalue(deptdocVO.getCode() + " " + deptdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}

	private Object CRMLxrBusiTypeDoc(String billtype, String pk_org, String userid, String pk) throws BusinessException {
		/**
		 * 21.CRM联系人业务类型
		 */
		// 业务类型
		String condition = "1=1 and enablestate=2 and isnull(dr,0)=0 and  code ='YWLX0023' order by code ";
		if (null != pk && !"".equals(pk)) {
			condition = " pk_biz_type in('" + pk + "')  ";
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(BizTypeVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// BizTypeVO deptdocVO = (BizTypeVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(billtype);
		// defdocVO.setCode(deptdocVO.getCode());
		// defdocVO.setName(deptdocVO.getVname());
		// defdocVO.setPk(deptdocVO.getPk_biz_type());
		// defdocVO.setShowvalue(deptdocVO.getCode() + " " +
		// deptdocVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	private Object CRMKhBusiTypeDoc(String pk_billtype, String pk_org, String userid, String pk) throws BusinessException {

		// 业务类型
		String cond = "((1 = 1 AND pk_account <> 'null' AND enablestate = 2 AND pk_group = '" + AppContext.getInstance().getPkGroup() + "') AND  cuma_account.enablestate = '2') AND enablestate = 2 AND isnull(dr, 0) = 0 "; // ORDER
		// BY
		// creationtime
		// DESC,
		// pk_account
		// DESC
		String condition = "";
		condition = " bofficialaccount='Y' and pk_org='" + pk_org + "' and creator='" + userid + "' and " + cond;
		if (null != pk && !"".equals(pk)) {
			condition = " pk_account in('" + pk + "')  ";
		}
		// ArrayList<SuperVO> list = (ArrayList<SuperVO>)
		// dao.retrieveByClause(AccountVO.class, condition);
		// if(list.size()==0 && (null == pk || "".equals(pk))){
		// return null;
		// }
		// if (list != null && list.size() > 0) {
		// RetDefDocVO refdefdocVO = new RetDefDocVO();
		// ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		// for (int i = 0; i < list.size(); i++) {
		// AccountVO accountVO = (AccountVO) list.get(i);
		// MDefDocVO defdocVO = new MDefDocVO();
		// defdocVO.setBilltype(pk_billtype);
		// defdocVO.setCode(accountVO.getVcode());
		// defdocVO.setName(accountVO.getVname());
		// defdocVO.setPk(accountVO.getPk_account());
		// defdocVO.setShowvalue(accountVO.getVcode() + " " +
		// accountVO.getVname());
		// listdefdocVO.add(defdocVO);
		// }
		// refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		// return refdefdocVO;
		// }
		return null;
	}

	private Object customertrcc(String billtype, String pk) throws BusinessException {
		StringBuffer sql = new StringBuffer("select bd_customer.pk_customer,bd_customer.code,bd_customer.name from bd_customer where 11 = 11");
		if (null != pk && !"".equals(pk)) {
			sql.append(" and pk_customer in('" + pk + "')");
		} else {
			sql.append(" and isnull(dr,0)=0 order by code ");
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 16.crm收支项目
	 */
	public RetDefDocVO crmincomeItemsDoc(String pk_billtype, String billtype, String pk) throws BusinessException {

		StringBuffer querySQL = new StringBuffer("select ").append(" pk_inoutbusiclass  ").append(",code  ").append(",name  ").append(" from ").append(" bd_inoutbusiclass ").append(" where dr=0 and  pk_parent！='~'");
		if (null != pk && !"".equals(pk)) {
			querySQL.append(" and pk_inoutbusiclass in('" + pk + "')  order by code");
		} else if (null != pk_billtype && !"".equals(pk_billtype) && null != pk_group && !"".equals(pk_group) && pk_billtype.equals("263X-Cxx-JKD")) {
			querySQL.append(" order by code ");
		} else {
			querySQL.append(" order by code ");
		}
		if (null != pk_billtype && !"".equals(pk_billtype) && null != pk_group && !"".equals(pk_group) && pk_billtype.equals("263X-Cxx-JKD")) {
			querySQL = new StringBuffer("select code, name, mnecode, pk_inoutbusiclass, pk_parent\n" + "             from bd_inoutbusiclass\n" + "            where 11 = 11\n" + "              and pk_inoutbusiclass in\n" + "                  ((select bd_inoutbusiclass1.pk_inoutbusiclass\n"
					+ "                     from bd_inoutbusiclass bd_inoutbusiclass1\n" + "                    where ((exists\n" + "                           (select 1\n" + "                               from bd_crossrestdata t\n"
					+ "                              where bd_inoutbusiclass1.pk_inoutbusiclass =\n" + "                                    t.data\n" + "                                and t.pk_restraint = '1001A1100000000617MG')) or\n" + "                          (exists\n"
					+ "                           (select 1\n" + "                               from bd_crossrestdata t\n" + "                              where bd_inoutbusiclass1.pk_inoutbusiclass =\n" + "                                    t.data\n"
					+ "                                and t.pk_restraint = '1001A1100000000617MF')) or\n" + "                          (exists\n" + "                           (select 1\n" + "                               from bd_crossrestdata t\n"
					+ "                              where bd_inoutbusiclass1.pk_inoutbusiclass =\n" + "                                    t.data\n" + "                                and t.pk_restraint = '1001A1100000000617ME')))))\n" + "              and (enablestate = 2)\n"
					+ "              and ((exists (select 1\n" + "                              from bd_inoutuse a\n" + "                             where a.pk_inoutbusiclass =\n" + "                                   bd_inoutbusiclass.pk_inoutbusiclass\n"
					+ "                               and a.pk_org = '0001A1100000000026R6') or\n" + "                   pk_org = '0001A1100000000026R6'))\n" + "            order by code");

		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());

		if (list == null || list.size() == 0) {
			return null;
		}

		MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object[] obj2 = list.get(i);

			defDocVOs[i] = new MDefDocVO();
			defDocVOs[i].setBilltype(billtype);
			defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
			defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
			defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
		}

		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);

		return refDocVO;
	}

	// 发货库存组织版本
	private Object sendstock(String billtype, String pk, String pk_org) throws BusinessException {

		// StringBuffer sql = new
		// StringBuffer("select org_stockorg_v.pk_stockorg,org_stockorg_v.pk_vid,org_stockorg_v.shortname\n"
		// +
		// "from org_stockorg_v\n" +
		// "join org_orgs on org_stockorg_v.code=org_orgs.code and isnull(org_orgs.dr,0)=0\n"
		// +
		// "where isnull(org_stockorg_v.dr,0)=0\n") ;
		//
		// if (null != pk && !"".equals(pk)) {
		// sql.append(" and org_stockorg_v.pk_vid    in('" + pk + "')");
		// }else{
		// sql.append(" and org_orgs.pk_org='"+pk_org+"' ");
		// }
		String sql = "";
		if (null != pk && !"".equals(pk)) {
			sql = "select pk_stockorg, pk_vid, name from org_stockorg_v where isnull(dr,0)=0 and pk_stockorg in('" + pk + "') and islastversion='Y'";
		} else {
			sql = " select distinct pk_stockorg, pk_vid, name\n" + "  from org_stockorg_v\n" + " where 11 = 11\n" + "   and  enablestate = 2\n" + "   and pk_vid in (select pk_vid\n" + "                    from org_orgs\n" + "                    where pk_org in (select distinct target\n"
					+ "                                      from org_relation\n" + "                                     where name like '%雍禾%' " +
					// "pk_relationtype = 'SALESTOCKCONSIGN0000'\n" +
					// "                                       and sourcer = '"+pk_org+"'\n"
					// +
					"                                       and enablestate = 2\n" + "                                    )\n" + "                  )";
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				// defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 查询审批单据类型 jinfei 2018-7-1
	 */
	private RetDefDocVO queryApproveBillType(String doctype) throws BusinessException {
		String[] str = doctype.split("_");
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		String sql = "select distinct isnull((select billtypename " + "from bd_billtype " + "where pk_billtypecode = vsourcebilltype " + "and (pk_group = '" + pk_group + "' or pk_group = '~')), " + "(select fun_name " + "from sm_funcregister " + "where funcode = vsourcebilltype "
				// + "and isnull(dr, 0) = 0)) name,vsourcebilltype pk " +
				// "from muap_message where vmsgtype = '" + str[0] +
				// "' order by vsourcebilltype ";
				+ "and isnull(dr, 0) = 0)) name,vsourcebilltype pk " + "from muap_message  order by vsourcebilltype ";

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				// defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				// defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 物料特征码
	 */
	private Object ffile(String billtype, String pk, String pk_material) throws BusinessException {
		String sql = "";
		if (null != pk && !"".equals(pk) && !"''".equals(pk)) {
			sql = "select cffileid,cmaterialid,vskucode from bd_ffile where isnull(dr,0)=0 and cffileid  in('" + pk + "')";
		} else {
			sql = "select cffileid,cmaterialid,vskucode from bd_ffile where isnull(dr,0)=0 and cmaterialid='" + pk_material + "' order by vskucode asc";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	private RetDefDocVO queryWorkNoteOrg() throws BusinessException {
		String userid = InvocationInfoProxy.getInstance().getUserId();
		String sql = "select org_orgs.pk_org,org_orgs.code,org_orgs.name from ( " + "select distinct pk_org from pub_workflownote where isnull(dr,0)=0 and checkman='" + userid + "' " + "union " + "select distinct pk_org from wfm_task where isnull(dr,0)=0 and pk_owner='" + userid + "' "
				+ ") orgunion left join org_orgs on orgunion.pk_org = org_orgs.pk_org  order by org_orgs.code";

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				// defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 收款通知单根据销售订单收款
	 */
	private RetDefDocVO saleOrderFee(String pk_org, String pk, String pk_account) throws BusinessException {
		if ((null == pk || "".equals(pk)) && (null == pk_account || "".equals(pk_account))) {
			throw new BusinessException("请先选择客户");
		}
		String userid = InvocationInfoProxy.getInstance().getUserId();
		String sql = " select csaleorderid,vbillcode,ccustomerid,vname,ntotalorigmny,nreceivedmny," + " 		 isnull(ntotalorigmny,0)-isnull(nreceivedmny,0) as balmny\n" + " from so_saleorder\n" + " join cuma_account on so_saleorder.ccustomerid=cuma_account.pk_customer\n"
				+ " where isnull(so_saleorder.dr,0)=0\n" + " and isnull(cuma_account.dr,0)=0" +
				// " and vbillcode not in (select vdef1 from sfa_gatheringnotebill where pk_org='"+pk_org+"' and isnull(dr,0)=0 and vdef1 is not null and vdef1!='~') \n"
				// +
				" and (isnull(ntotalorigmny,0)-isnull(nreceivedmny,0))!=0\n" + " and so_saleorder.pk_org='" + pk_org + "'\n" + " and pk_account='" + pk_account + "'" + " order by vbillcode ";
		if (null != pk && !"".equals(pk)) {
			sql = " select csaleorderid,vbillcode,ccustomerid,vname,ntotalorigmny,nreceivedmny," + " 		 isnull(ntotalorigmny,0)-isnull(nreceivedmny,0) as balmny\n" + " from so_saleorder\n" + " join cuma_account on so_saleorder.ccustomerid=cuma_account.pk_customer\n"
					+ " where isnull(so_saleorder.dr,0)=0\n" + " and isnull(cuma_account.dr,0)=0\n" + " and so_saleorder.pk_org='" + pk_org + "'\n" + " and vbillcode  in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue("销售订单号：" + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]) + "\n客户： " + PuPubVO.getString_TrimZeroLenAsNull(obj2[3]) + "\n价税合计： " + PuPubVO.getString_TrimZeroLenAsNull(obj2[4]) + "\n欠款余额： " + PuPubVO.getString_TrimZeroLenAsNull(obj2[6]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 
	/**
	 * 供应商参照 zhujiaming
	 */
	private RetDefDocVO goodsAddress1(String datatype, String pk, String pk_org,String scon) throws BusinessException {
		String strSQL = ""; 
		if (null != pk_org && !"".equals(pk_org)) {
			strSQL =  " select pk_supplier, code, name from ";
			strSQL = strSQL + " (SELECT ROWNUM AS rowno, bd_supplier.* from bd_supplier";
			strSQL = strSQL + " where nvl(dr,0)=0\n" + 
					"   and (enablestate = 2)\n" + 
					"   and ((pk_supplier in\n" + 
					"       (select pk_supplier\n" + 
					"            from bd_suporg\n" + 
					"           where pk_org in ('"+pk_org+"')\n" + 
					"             and enablestate = 2\n" + 
					"             and nvl(dr,0)=0\n" + 
					"          union\n" + 
					"          select pk_supplier\n" + 
					"            from bd_supplier\n" + 
					"           where nvl(dr,0)=0\n" + 
					"            and (pk_org = '"+pk_group+"' or\n" + 
					"                 pk_org = '"+pk_org+"'))))\n" ;
			if(scon!=null&&scon.length()>0){
				strSQL = strSQL + " AND (code like '%"+scon+"%' or name like '%"+scon+"%')";
			}
			strSQL = strSQL + " AND ROWNUM <= 50) table_supplier WHERE table_supplier.rowno >= 0;";
		}

		if (null != pk && !"".equals(pk)) {
			strSQL = "select pk_supplier, code, name\n" + 
				  "from bd_supplier\n" + 
				  "where isnull(dr,0)=0\n" + 
				  "and (enablestate = 2)\n" + 
				  "and pk_supplier  in ('"+pk+"')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(strSQL.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 税码税率参照 zhujiaming
	 */
	private RetDefDocVO taxcode(String datatype, String pk, String pk_org, String pk_group) throws BusinessException {
		String sql = "";
        if (null != pk && !"".equals(pk)) {
			sql = "select pk_taxcode, code, description, code from bd_taxcode where isnull(dr,0) = 0 and pk_taxcode in  （'" + pk + "'）";
		} else {
			sql = "select bd_taxcode.pk_taxcode,\n" +
							"       bd_taxcode.code,\n" + 
							"       bd_taxcode.description,\n" + 
							"       bd_taxrate.taxrate\n" + 
							"  from bd_taxcode\n" + 
							"  join bd_taxrate\n" + 
							"    on bd_taxcode.pk_taxcode = bd_taxrate.pk_taxcode\n" + 
							" where nvl(bd_taxcode.dr, 0) = 0\n" + 
							"   and nvl(bd_taxrate.dr, 0) = 0";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setDef1(PuPubVO.getString_TrimZeroLenAsNull(obj2[3]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 

	/**
	 * 报销单录入人参照 zhujiaming 2018年12月8日13:23:35
	 */
	private RetDefDocVO taxcode3(String datatype, String pk, String pk_org, String userid) throws BusinessException {// 0001A110000000000638
		String sql = "";
		if (null != userid && !"".equals(userid)) {
			sql = "select code,name,pk_psndoc from sm_user  where pk_psndoc in('" + pk + "')";
		} else {
			sql = "select code,name,pk_psndoc from bd_psndoc where nvl(dr,0) = 0";
		}
		if(null != pk && !"".equals(pk)){
			sql = "select  code,name,pk_psndoc from bd_psndoc  where pk_psndoc in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 32.2 报销单财务组织 朱佳明 2018年12月8日16:16:24
	 */
	public RetDefDocVO cfanaceorgoid1(String billtype, String pk_billtype, String pk, String pk_org, String userid) throws BusinessException {
		StringBuffer querySQL = new StringBuffer();// 查询时，PK=R8，org=R9，第一次打开是PK
		// = org

		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer("select pk_financeorg,code,name from org_financeorg where dr=0 and pk_financeorg in('" + pk + "')");
		} else {
			querySQL = new StringBuffer("SELECT pk_financeorg,code,name from org_financeorg\n" + "WHERE\n" + "	11 = 11\n" + "AND (\n" + "	(\n" + "		enablestate = 1\n" + "		OR enablestate = 2\n" + "		OR enablestate = 3\n" + "	)\n" + ")\n" + "AND (\n" + "	(\n"
					+ "		pk_group = '"+pk_group+"'\n" + "	)\n" + ")\n" + "ORDER BY\n" + "	\n" + "	code");
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}

	/**
	 * 1. 报销单部门
	 * 
	 * @throws BusinessException
	 *             2018年11月22日15:36:19 dongliang 修改部门启用状态
	 */
	public RetDefDocVO deptDoc1(String pk_org, String billtype, String pk) throws BusinessException {
		StringBuffer querySQL = new StringBuffer();

		if (null != pk && !"".equals(pk)) {
			querySQL = new StringBuffer("select pk_dept,code,name from org_dept where dr = 0  and pk_dept in('" + pk + "')");
		} else {
			querySQL = new StringBuffer("SELECT pk_dept,\n" + "	code,\n" + "	name,\n" + "	mnecode,\n" + "	pk_fatherorg,\n" + "	displayorder,\n" + "	innercode,\n" + "	pk_org\n" + "FROM\n" + "	org_dept\n" + "WHERE\n" + "	11 = 11\n" + "AND (\n" + "	(\n" + "\n"
					+ "		enablestate = 2\n" + "\n" + "	)\n" + ")\n" + "AND (\n" + "	(\n" + "		pk_group = '"+pk_group+"'\n" + "		AND pk_org = N'" + pk_org + "'\n" + "	)\n" + ")\n" + "ORDER BY\n" + "	displayorder,\n" + "	code");
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;

	}

	/**
	 * 32.2 报销类型
	 */
	public RetDefDocVO cfanaceorgoid2(String billtype, String pk_billtype, String pk, String pk_org, String pk_group) throws BusinessException {
		String querySQL = "";
		if (null != pk && !"".equals(pk)) {
			querySQL = "select code, name, pk_reimtype from er_reimtype where pk_reimtype  in('" + pk + "') and (nvl(dr, 0) = 0) and (inuse = 'N') order by code";
		} else {
			querySQL = "select code, name, pk_reimtype from er_reimtype where (isnull(dr, 0) = 0  ) and (inuse = 'N') order by code";
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(querySQL.toString(), new ArrayListProcessor());
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			return null;
		}
		MDefDocVO[] defDocVOs = null;
		if (list != null && list.size() > 0) {
			defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setBilltype(billtype);
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
		}
		RetDefDocVO refDocVO = new RetDefDocVO();
		refDocVO.setDefdocvos(defDocVOs);
		return refDocVO;
	}


	/**
	 * 公共查询，用户是否有角色权限数据
	 */
	public ArrayList getRole(String userid) throws DAOException {
		// 先查询是否有数据,有直接查询,否则继续权限查询
		String roledata = "SELECT *\n" + "  FROM capub_dataright\n" + " WHERE isnull(dr,0)=0" + "   and pk_role in (select pk_role\n" + "                    from sm_role\n" + "                   where isnull(dr,0)=0" + "					  and pk_role in (select pk_role\n"
				+ "                                       from sm_user_role\n" + "                                      where isnull(dr,0)=0 and cuserid = '" + userid + "'\n" + "                                     )\n" + "                     and pk_group = '" + pk_group + "')\n"
				+ "   and pk_operation in (select pk_res_operation\n" + "                          from sm_res_operation\n" + "                         where operationcode = 'use')\n" + "   and pk_permissionrule in (select ruleid from sm_perm_data where isnull(dr,0)=0 and operationcode = 'use')\n"
				+ "   and operationtype = 1";
		// ArrayList<DataRightVO> role = (ArrayList<DataRightVO>)
		// dao.executeQuery(roledata, new BeanListProcessor(DataRightVO.class));
		return null;
	}

	/**
	 * 摘要
	 */
	private RetDefDocVO summary(String datatype, String pk, String pk_org, String pk_group) throws BusinessException {// 0001A110000000000638
		String sql = "SELECT code, summaryname,pk_summary\n" + "  FROM fipub_summary\n" + " WHERE isnull(dr,0)=0\n" + " and  pk_org = '" + pk_org + "'\n" + " ORDER BY code";
		if (null != pk && !"".equals(pk)) {
			sql = " SELECT  code, summaryname,pk_summary\n" + "  FROM fipub_summary\n" + " WHERE isnull(dr,0)=0\n" + " and (summaryname like '%" + pk + "%' or pk_summary in('" + pk + "'))\n";
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 申请摘要
	 */
	private RetDefDocVO summary2(String datatype, String pk, String pk_org, String pk_group) throws BusinessException {// 0001A110000000000638
		String sql = "SELECT code, summaryname, summaryname\n" + "  FROM fipub_summary\n" + " WHERE isnull(dr,0)=0\n" + " and  pk_org = '" + pk_org + "'\n" + " ORDER BY code";
		if (null != pk && !"".equals(pk)) {
			sql = " SELECT code, summaryname, summaryname\n" + "  FROM fipub_summary\n" + " WHERE isnull(dr,0)=0\n" + " and (summaryname like '%" + pk + "%' or pk_summary   in('" + pk + "'))\n";
		}

		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + defDocVOs[i].getName());
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 单位银行账户
	 */
	private RetDefDocVO bankacc(String datatype, String pk, String pk_org, String pk_group) throws BusinessException {// 0001A110000000000638
		String sql = "select distinct bd_bankaccsub.accnum,\n" + "                bd_bankaccsub.accname,\n" + "                bd_bankaccsub.pk_bankaccsub\n" + "  from bd_bankaccbas\n" + " INNER JOIN bd_bankaccsub ON bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n"
				+ " where pk_currtype = '1002Z0100000000001K1' and acctype not in ( '1', '2')\n" + "   and enablestate = 2\n" + "   and bd_bankaccsub.pk_bankaccsub in (select pk_bankaccsub\n" + "                                         from bd_bankaccuse\n"
				+ "                                        where pk_org = '" + pk_org + "'\n" + "                                          and enablestate = 2)\n" + " order by bd_bankaccsub.accnum";
		if (null != pk && !"".equals(pk)) {
			sql = " SELECT accnum, accname, pk_bankaccsub FROM bd_bankaccsub WHERE bd_bankaccsub.pk_bankaccsub in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 个人银行账户
	 */
	private RetDefDocVO psnBankacc(String datatype, String pk, String pk_org, String userid,String receiver) throws BusinessException {// 0001A110000000000638


		String sql ="select accnum,\n" +
					"       accname,\n" + 
					"       pk_bankaccsub,\n" + 
					"       pk_bankdoc,\n" + 
					"       pk_banktype,\n" + 
					"       pk_currtype,\n" + 
					"       payacc,\n" + 
					"       isexpenseacc,\n" +  
					"       pk_bankaccbas,\n" + 
					"       enablestate,\n" + 
					"       pk_org,\n" + 
					"       pk_psndoc\n" + 
					"  from (SELECT bd_bankaccsub.accnum,\n" + 
					"               bd_bankaccsub.accname,\n" + 
					"               pk_bankdoc,\n" + 
					"               pk_banktype,\n" + 
					"               pk_currtype,\n" + 
					"               payacc,\n" + 
					"               isexpenseacc,\n" + 
					"               bd_psnbankacc.pk_bankaccsub pk_bankaccsub,\n" + 
					"               bd_psnbankacc.pk_bankaccbas pk_bankaccbas,\n" + 
					"               enablestate,\n" + 
					"               bd_psnbankacc.pk_org        pk_org,\n" + 
					"               pk_psndoc\n" + 
					"          FROM bd_bankaccbas, bd_bankaccsub, bd_psnbankacc\n" + 
					"         WHERE bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n" + 
					"           AND bd_bankaccsub.pk_bankaccsub = bd_psnbankacc.pk_bankaccsub\n" + 
					"           AND bd_bankaccsub.pk_bankaccbas = bd_psnbankacc.pk_bankaccbas\n" + 
					"           AND bd_psnbankacc.pk_bankaccsub != '~') bd_psnbankacctmp\n" + 
					" where (pk_psndoc = '"+receiver+"' and\n" + 
					"       pk_currtype = '1002Z0100000000001K1')\n" + 
					"   and (enablestate = 2)\n" + 
					"   and (pk_psndoc = '"+receiver+"')\n" + 
					" order by accnum";


		if (null != pk && !"".equals(pk)) {
			sql = "select accnum, accname, pk_bankaccsub from bd_bankaccsub where pk_bankaccsub in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO(); 
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				 
				if(null!=obj2[0] && obj2[0].toString().length()>8){
					String obj21 = obj2[0].toString();
					int num = obj21.length()-8;
					String start = obj21.substring(0,4);
					String end = obj21.substring(obj21.length()-4,obj21.length());
					StringBuffer nums = new StringBuffer(start);
					for(int n=0; n<num; n++){
						nums.append("*");
					}
					nums.append(end);
					defDocVOs[i].setCode(nums.toString());
					defDocVOs[i].setName(nums.toString());
				}else{
					defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
					defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				}
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}

	/**
	 * 供应商银行账户
	 */
	private RetDefDocVO custBankacc(String datatype, String pk, String pk_org, String userid, String pk_cust,String hbbm,String paytarget) throws BusinessException {
		String zh = "";
		String accclass = "";
		if(null != paytarget && !"".equals(paytarget)){
			if(null != paytarget && "2".equals(paytarget)){
				zh = pk_cust;//kehu 
				accclass = "and accclass = '1'";
			}else if(null != paytarget && "1".equals(paytarget)){
				zh = hbbm;
				accclass = "and accclass = '3'";
			}else{
				zh = "";
			}
		}else{
			zh = pk_cust;//kehu 
		}

		String sql ="select distinct accnum,\n" +
					"                accname,\n" + 
					"                pk_bankaccsub,\n" + 
					"                pk_bankdoc,\n" + 
					"                pk_banktype,\n" + 
					"                pk_currtype,\n" + 
					"                pk_bankaccbas,\n" + 
					"                enablestate,\n" + 
					"                accountproperty,\n" + 
					"                isinneracc,\n" + 
					"                pk_custbank\n" + 
					"  from (SELECT bd_bankaccsub.accnum,\n" + 
					"               bd_bankaccsub.accname,\n" + 
					"               bd_custbank.pk_bankaccsub pk_bankaccsub,\n" + 
					"               pk_bankdoc,\n" + 
					"               pk_banktype,\n" + 
					"               pk_currtype,\n" + 
					"               bd_custbank.pk_bankaccbas pk_bankaccbas,\n" + 
					"               enablestate,\n" + 
					"               pk_cust,\n" + 
					"               accountproperty,\n" + 
					"               isinneracc,\n" + 
					"               bd_custbank.accclass,\n" + 
					"               pk_custbank\n" + 
					"          FROM bd_bankaccbas, bd_bankaccsub, bd_custbank\n" + 
					"         WHERE bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n" + 
					"           AND bd_bankaccsub.pk_bankaccsub = bd_custbank.pk_bankaccsub\n" + 
					"           AND bd_bankaccsub.pk_bankaccbas = bd_custbank.pk_bankaccbas\n" + 
					"           AND bd_custbank.pk_bankaccsub != '~') bd_psnbankacctmp\n" + 
					" where (pk_currtype = '1002Z0100000000001K1' and enablestate = '2' "+accclass+")\n" + 
					"   and (enablestate = 2)\n" + 
					"   and (pk_cust = '"+zh+"')\n" + 
					" order by accnum";

		if (null != pk && !"".equals(pk)) {
			sql = "select accnum, accname, pk_bankaccsub from bd_bankaccsub where pk_bankaccsub in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				//				if(null!=obj2[0] && obj2[0].toString().length()>8){
				//					String obj21 = obj2[0].toString();
				//					int num = obj21.length()-8;
				//					String start = obj21.substring(0,4);
				//					String end = obj21.substring(obj21.length()-4,obj21.length());
				//					StringBuffer nums = new StringBuffer(start);
				//					for(int n=0; n<num; n++){
				//						nums.append("*");
				//					}
				//					nums.append(end);
				//					defDocVOs[i].setCode(nums.toString());
				//					defDocVOs[i].setName(nums.toString());
				//				}else{
				//					defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				//					defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				//				}
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 
	/**
	 * 项目
	 */
	private RetDefDocVO bxbdproject(String datatype, String pk) throws BusinessException {// 0001A110000000000638
		String sql = "select project_code,project_name,pk_project from bd_project where isnull(dr,0)=0 order by project_code";
		String xmpk = "";
		if (null != pk && !"".equals(pk)) {
			//xmpk = pk.substring(3);
			sql = "select project_code,project_name,pk_project from bd_project where isnull(dr,0)=0 and pk_project  in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
 
	/**
	 * 交通工具-枚举
	 */
	private RetDefDocVO enumvaluejttools(String datatype, String pk) throws BusinessException {// 0001A110000000000638
		String sql = "select enumsequence,name,enumsequence from md_enumvalue where id='550519e8-69fc-4d16-8d14-0f8e71d1f5aa' and nvl(dr,0)=0 ";
		if (null != pk && !"".equals(pk)) {
			sql = "select enumsequence,name,enumsequence from md_enumvalue where id='550519e8-69fc-4d16-8d14-0f8e71d1f5aa' and nvl(dr,0)=0 and enumsequence in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	}
	/**
	 * HR人员
	 * 
	 * @param pk
	 * @param billtype
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private Object hr_psndocRefModel(String pk, String billtype) throws BusinessException {
		ArrayList<PsndocVO> list = new ArrayList<PsndocVO>();
		if (null != pk && !"".equals(pk)) {
			list = (ArrayList<PsndocVO>) dao.retrieveByClause(PsndocVO.class, " nvl(dr,0)=0 and pk_psndoc in('" + pk + "') ");
		} else {
			list = (ArrayList<PsndocVO>) dao.retrieveByClause(PsndocVO.class, "nvl(dr,0)=0 order by code asc ");
		}
		if (list.size() == 0 && (null == pk || "".equals(pk))) {
			throw new BusinessException("没有数据!");
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				PsndocVO hrorgdocVO = (PsndocVO) list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setBilltype(billtype);
				defdocVO.setCode(PubTools.getStringValue(hrorgdocVO.getCode()));
				defdocVO.setName(PubTools.getStringValue(hrorgdocVO.getName()));
				defdocVO.setPk(hrorgdocVO.getPk_psndoc());
				defdocVO.setShowvalue(PubTools.getStringValue(hrorgdocVO.getCode()) + " " + PubTools.getStringValue(hrorgdocVO.getName()));
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}
	/**
	 * 签卡原因(自定义档案)
	 */
	private Object signreason() throws BusinessException {
		StringBuffer sql = new StringBuffer();
		sql.append("select code, name, memo, mnecode, pk_defdoc from bd_defdoc where nvl(dr,0) = 0 and enablestate = 2 and pk_defdoclist = '1002Z71000000001VJBZ' order by code");
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list.size() == 0) {
			throw new BusinessException("没有数据!");
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
			for (int i = 0; i < list.size(); i++) {
				Object[] psnojb = list.get(i);
				MDefDocVO defdocVO = new MDefDocVO();
				defdocVO.setCode(PubTools.getStringValue(psnojb[0]));
				defdocVO.setName(PubTools.getStringValue(psnojb[1]));
				defdocVO.setPk(PubTools.getStringValue(psnojb[4]));
				defdocVO.setShowvalue(defdocVO.getName());
				listdefdocVO.add(defdocVO);
			}
			refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
			return refdefdocVO;
		}
		return null;
	}


	/**
	 * 现金账户
	 */
	private RetDefDocVO cashaccount(String datatype, String pk, String pk_org) throws BusinessException {// 0001A110000000000638
		String sql = "select code,name,pk_cashaccount from bd_cashaccount where nvl(dr,0)=0 and pk_org='" + pk_org + "' order by code";
		if (null != pk && !"".equals(pk)) {
			sql = "select code,name,pk_cashaccount from bd_cashaccount where nvl(dr,0)=0 and pk_cashaccount  in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	} 
	/**
	 * 险种
	 */
	private Object xianzhong(String datatype, String pk, String pk_org) throws BusinessException {
		String sql = "SELECT bm_bmclass.code, bm_bmclass. NAME, bm_bmclass.pk_bm_class, bm_bmclass.pk_org ,org_orgs. NAME orgname  FROM bm_bmclass"
				+" INNER JOIN org_orgs ON bm_bmclass.pk_org = org_orgs.pk_org WHERE	(bm_bmclass.pk_bm_class IN ("
				+" SELECT DISTINCT	bm_data.pk_bm_class FROM	bm_data INNER JOIN hi_psnjob ON bm_data.pk_psnjob = hi_psnjob.pk_psnjob"
				+" INNER JOIN bm_bmclass ON bm_data.pk_bm_class = bm_bmclass.pk_bm_class LEFT OUTER JOIN org_orgs ON bm_data.pk_org = org_orgs.pk_org"
				+" LEFT OUTER JOIN bm_period ON bm_period.pk_periodscheme = bm_bmclass.pk_periodscheme AND bm_period. YEAR = bm_data.cyear"
				+" AND bm_period.period = bm_data.cperiod WHERE	bm_data.accountstate = 0 AND bm_data.checkflag = 'Y'"
				+" AND bm_data.pk_psndoc = '100111100000000001L5'	)	)ORDER BY	bm_bmclass.pk_org,bm_bmclass.code";
		if (null != pk && !"".equals(pk)) {
			sql = "select bm_bmclass.code, bm_bmclass. NAME, bm_bmclass.pk_bm_class, bm_bmclass.pk_org from bm_bmclass where nvl(dr,0)=0 and bm_bmclass.pk_bm_class  in('" + pk + "')";
		}
		ArrayList<Object[]> list = (ArrayList<Object[]>) dao.executeQuery(sql.toString(), new ArrayListProcessor());
		if (list == null || list.size() == 0) {
			return null;
		}
		if (list != null && list.size() > 0) {
			RetDefDocVO refdefdocVO = new RetDefDocVO();
			MDefDocVO[] defDocVOs = new MDefDocVO[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				defDocVOs[i] = new MDefDocVO();
				defDocVOs[i].setPk(PuPubVO.getString_TrimZeroLenAsNull(obj2[2]));
				defDocVOs[i].setCode(PuPubVO.getString_TrimZeroLenAsNull(obj2[0]));
				defDocVOs[i].setName(PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
				defDocVOs[i].setShowvalue(defDocVOs[i].getCode() + " " + PuPubVO.getString_TrimZeroLenAsNull(obj2[1]));
			}
			refdefdocVO.setDefdocvos(defDocVOs);
			return refdefdocVO;
		}
		return null;
	} 
}
