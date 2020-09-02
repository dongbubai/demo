package hd.bs.bill.bxfee;

import hd.bs.mobile.bdoc.BdocAction;
import hd.bs.muap.approve.ApproveWorkAction; 
import hd.bs.muap.file.AttachmentAction;
import hd.itf.muap.pub.IMobileAction;
import hd.muap.pub.internet.FileService;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField; 
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.AttachmentListVO;
import hd.vo.muap.pub.AttachmentVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO; 
import hd.vo.muap.pub.FormulaVO;
import hd.vo.muap.pub.QueryBillVO; 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap; 
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;  
import java.util.Map.Entry;

import org.apache.pdfbox.util.ErrorLogger;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.uap.lock.PKLock;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFBusiAction;
import nc.itf.uap.pf.IPfExchangeService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor; 
import nc.md.persist.framework.MDPersistenceService;  
import nc.vo.bd.cashaccount.CashAccountVO;
import nc.vo.bd.inoutbusiclass.InoutBusiClassVO; 
import nc.vo.ep.bx.BXBusItemVO;
import nc.vo.ep.bx.BXHeaderVO;
import nc.vo.ep.bx.BXVO;
import nc.vo.ep.bx.BxcontrastVO;
import nc.vo.ep.bx.JKHeaderVO;
import nc.vo.ep.bx.JKVO;
import nc.vo.er.reimtype.ReimTypeVO; 
import nc.vo.erm.common.MessageVO;
import nc.vo.erm.costshare.CShareDetailVO; 
import nc.vo.erm.fppj.FpglVO;
import nc.vo.erm.matterapp.MatterAppVO;
import nc.vo.muap.muap05.BillyqBVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;   

/**
 * 1.报销单
 * 2.还款单
 */
public class BXzbAction extends ApproveWorkAction {
	
	public static String currenttabcode = "";
	public static String fphm = "";

	IUAPQueryBS bx = NCLocator.getInstance().lookup(IUAPQueryBS.class);
	
	public String getNullorString(Object str) {
		if (null == str || "".equals(str)) {
			return "";
		} else {
			return str.toString();
		}
	}
	
	//东亮  2020-3-18 15:43:38  判断字符串是否为数字(包括小数)
	public static boolean isNumber(String str){

        String reg = "^[0-9]+(.[0-9]+)?$";

        return str.matches(reg);

    }

	static BaseDAO dao = new BaseDAO();
	//dongl 09点51分 动态获取 发票号的字段
	public static String getFphm(String billtype) throws BusinessException {

		String sql = "select muap_billyq_b.*\n" +
				"  from muap_bill_h\n" + 
				"  join muap_billyq_b\n" + 
				"  on muap_bill_h.pk_billconfig_h = muap_billyq_b.pk_billconfig_h\n" + 
				"  where nvl(muap_bill_h.dr, 0) = 0\n" + 
				"  and nvl(muap_billyq_b.dr, 0) = 0\n" + 
				"  and muap_bill_h.vmobilebilltype = '"+billtype+"' and muap_billyq_b.refdata = '发票号'";
		ArrayList<BillyqBVO> yqbList = (ArrayList<BillyqBVO>) dao.executeQuery(sql, new BeanListProcessor(BillyqBVO.class));
		if(null != yqbList && yqbList.size() > 0){
			return yqbList.get(0).getItemcode();
		}
		return null;
	}
	
	
	//加锁处理
	public void isLockSave(String cuserid ,String invoicePK) throws BusinessException{
		PKLock lock = PKLock.getInstance();
		Long threadid = Thread.currentThread().getId();
		try{
			//加锁方法
		   	InvocationInfoProxy.getInstance().setUserId(String.valueOf(threadid)); //线程加锁
			Boolean islock = lock.addDynamicLock(invoicePK);
			if( !islock ){
				ErrorLogger.log("发票号开始加锁处理！");
				return;
			}
			InvocationInfoProxy.getInstance().setUserId(cuserid);
		}catch (Exception e) {
			throw new BusinessException("发票号不能重复！");
		}finally{
			if(null!=lock){
				lock.releaseLocks(String.valueOf(threadid), InvocationInfoProxy.getInstance().getUserDataSource());//释放锁
			}
		}
	}
	
	//dongl 09点51分 动态获取 发票号的字段
	public static ArrayList<BillyqBVO> getFphm(String billtype,String refdate) throws BusinessException {

		String sql = "select muap_billyq_b.*\n" +
				"  from muap_bill_h\n" + 
				"  join muap_billyq_b\n" + 
				"  on muap_bill_h.pk_billconfig_h = muap_billyq_b.pk_billconfig_h\n" + 
				"  where nvl(muap_bill_h.dr, 0) = 0\n" + 
				"  and nvl(muap_billyq_b.dr, 0) = 0\n" + 
				"  and muap_bill_h.vmobilebilltype = '"+billtype+"' and muap_billyq_b.itemcode = '"+refdate+"'" +
				"  and iscardshow = 'Y'";
		ArrayList<BillyqBVO> yqbList = (ArrayList<BillyqBVO>) dao.executeQuery(sql, new BeanListProcessor(BillyqBVO.class));
		if(null != yqbList && yqbList.size() > 0){
			return yqbList;
		}
		return null;
	}
	
	String account1 = "";
	String billtype1 = "";

	@SuppressWarnings("unchecked")
	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		
		account1 = account;
		billtype1 = billtype;
		
		//费用申请单拉单 dongl
		if("DEFAPPLYOK".equals(action)){
			return new FeeBaseAction().getBXVO(obj,billtype.toString());
		}
		//参照费用申请单 dongl
		if ("DEFAPPLY".equals(action)) {
			return new FeeBaseAction().queryPageMtapp(userid,billtype,obj);
		} 
		//自定义按钮发票报销 dongl      合并报销发票按钮
		if ("DEFFP".equals(action) || "DEFFPCOMBX".equals(action)) {
			//是否启用 发票报销模块 H013
			UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
			if(booleanFP.booleanValue()){
				BillVO bill = (BillVO) obj;
		  		if(null != bill.getHeadVO().get("currenttabcode") && !"".equals(bill.getHeadVO().get("currenttabcode"))){
		  			currenttabcode = bill.getHeadVO().get("currenttabcode").toString();
		  		}
				return new FeeBaseAction().queryInvoice(userid,billtype,obj,action);
			}
		} 
		//自定义发票按钮 DEFFPOK 处理APP子表自增多行        合并报销发票按钮OK
		if ("DEFFPOK".equals(action) || "DEFFPCOMBXOK".equals(action)) {
			//是否启用 发票报销模块 H013
			UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
			if(booleanFP.booleanValue()){
				String invoicehm = getFphm(billtype);
				String billtypecode = new FeeBaseAction().getbilltypeCode(billtype);
				//单据设置第二个页签未配置发票字段 就不新增发票
				ArrayList<BillyqBVO> yqList  = getFphm(billtype,invoicehm);
				if(null != yqList && yqList.size() > 0){
					for(BillyqBVO pageStrs : yqList){
						if(pageStrs.getTabcode().equals(currenttabcode)){
							if("DEFFPOK".equals(action)){
								return new FeeBaseAction().fillInvoice(userid,billtype,obj,invoicehm,billtypecode,currenttabcode);
							}
							if("DEFFPCOMBXOK".equals(action)){
								return new FeeBaseAction().actionok(userid,billtype,obj,invoicehm,billtypecode,currenttabcode,action);

							}
						}
					}
				}
			}
			return null;
		} 
		
		/**
		 * app联查发票附件按钮   
		 * 功能注册 按钮 制单  DEFFILEFP    发票附件  deffilefp.DOWNLOAD  下载     deffilefp.PREVIEW  预览
		 */
		if ("DEFFILEFP".equals(action)) {
			//根据当前单据功能注册编码查找单据设置节点，子表的【参照数据】为【发票号】的字段名称的值，即发票pk
			String funcode = super.getBilltype();
			String sql ="select muap_billyq_b.itemcode\n" +
						"from muap_bill_h\n" + 
						"join muap_billyq_b on muap_billyq_b.pk_billconfig_h=muap_bill_h.pk_billconfig_h and isnull(muap_billyq_b.dr,0)=0 \n" + 
						"where vmobilebilltype='"+funcode+"'\n" + 
						"and trim(refdata)='发票号' " +
						" and isnull(muap_bill_h.dr,0)=0 ";
			ArrayList<BillyqBVO> mupqb = (ArrayList<BillyqBVO>) dao.executeQuery(sql, new BeanListProcessor(BillyqBVO.class));
			if(null!=mupqb && mupqb.size()>0){
				BillVO bill = (BillVO) obj;
				HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
				HashMap<String, Object>[] bxbusitem = bodys.get("arap_bxbusitem");//交通费用
				HashMap<String, Object>[] other = bodys.get("other");//其他费用
				AttachmentAction aac = new AttachmentAction();
				ArrayList<AttachmentVO> raVOList = new ArrayList<AttachmentVO>();
				for(int i=0; null!=bxbusitem && i<bxbusitem.length; i++){
					AttachmentVO avo = new AttachmentVO();
					if(null!=bxbusitem[i].get(mupqb.get(0).getItemcode())){
						avo.setFileroot(bxbusitem[i].get(mupqb.get(0).getItemcode()).toString());
						//根据发票pk，查询发票附件
						Object oVO = aac.processAction(account,userid,billtype,"QUERY", avo);
						AttachmentListVO atListVO = (AttachmentListVO) oVO;
						AttachmentVO[] rvos = atListVO.getAttachmentVOs();
						if(null!=rvos && rvos.length>0){
							raVOList.addAll(Arrays.asList(rvos));
						}
					}
				}
				for(int i=0; null!=other && i<other.length; i++){
					AttachmentVO avo = new AttachmentVO();
					if(null!=other[i].get(mupqb.get(0).getItemcode())){
						avo.setFileroot(other[i].get(mupqb.get(0).getItemcode()).toString());
						//根据发票pk，查询发票附件
						Object oVO = aac.processAction(account,userid,billtype,"QUERY", avo);
						AttachmentListVO atListVO = (AttachmentListVO) oVO;
						AttachmentVO[] rvos = atListVO.getAttachmentVOs();
						if(null!=rvos && rvos.length>0){
							raVOList.addAll(Arrays.asList(rvos));
						}
					}
				}
				if(null!=raVOList && raVOList.size()>0){
					AttachmentListVO aVOs = new AttachmentListVO();
					aVOs.setAttachmentVOs(raVOList.toArray(new AttachmentVO[0]));
					return aVOs;
				}
			}
			return null;
		} 


		if("DEFREFOK".equals(action)){
			if (obj instanceof BillVO) {

				String billtypecode = new FeeBaseAction().getbilltypeCode(billtype);
				BillVO	bill = (BillVO) obj;
				//要把发票和还款单分开  还款单参照借款单功能
				if(null == bill.getHeadVO().get("fphm") || "".equals(bill.getHeadVO().get("fphm"))){
					//把借款单的子表信息 返回给还款单子表，由于字段较少 此处用 对冲借款的子表重新封装的形式
					if(billtypecode.startsWith("264")){
						String pk_jkbx = "";
						if(null != bill.getHeadVO().get("pk_jkbx") && !"".equals(bill.getHeadVO().get("pk_jkbx"))){
							pk_jkbx = bill.getHeadVO().get("pk_jkbx").toString();
							String jkbxr = "";
							if(null != bill.getHeadVO().get("jkbxr") && !"".equals(bill.getHeadVO().get("jkbxr"))){
								jkbxr = bill.getHeadVO().get("jkbxr").toString();
							}
							String djbh = "";
							if(null != bill.getHeadVO().get("djbh") && !"".equals(bill.getHeadVO().get("djbh"))){
								djbh = bill.getHeadVO().get("djbh").toString();
							}
							HashMap<String, Object>[] busitemMap = transNCVOTOMap(MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, pk_jkbx, false).getBxBusItemVOS());
							for(int j=0;j<busitemMap.length;j++){
								busitemMap[j].put("jkbxr", jkbxr);
								busitemMap[j].put("cjkybje", busitemMap[j].get("yjye"));
								busitemMap[j].put("szxmid", busitemMap[j].get("szxmid"));
								busitemMap[j].put("pk_busitem", busitemMap[j].get("pk_busitem"));
								busitemMap[j].put("jkdjbh", djbh);
								//费用申请单号
								String strWhere = " nvl(dr,0) = 0 and pk_jkbx  = '"+busitemMap[j].get("pk_item")+"'";
								@SuppressWarnings("unchecked")
								ArrayList<SuperVO> bodylist = (ArrayList<SuperVO>) dao.retrieveByClause(JKHeaderVO.class, strWhere);
								if(null != bodylist && bodylist.size() > 0){
									busitemMap[j].put("pk_item.billno", bodylist.get(0).getAttributeValue("djbh"));
								}
							}
							BillVO billVO = new BillVO();
							billVO.setTableVO("er_bxcontrast", busitemMap);
							return billVO;
						}
					}
				}
			}

			//发票拉单转换 为 报销单数据 dongl
			//单条
			if (obj instanceof BillVO) {
				//是否启用 发票报销模块 H013
				UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
				if(booleanFP.booleanValue()){
					BillVO	bill = (BillVO) obj;
					String billtypecode = new FeeBaseAction().getbilltypeCode(billtype);
					if(null != bill.getHeadVO().get("pk_fpgl") && !"".equals(bill.getHeadVO().get("pk_fpgl"))){
						String invoicehm = getFphm(billtype);
						if(null != invoicehm && !"".equals(invoicehm)){
							return getInvoiceCount(billtypecode,bill.getHeadVO().get("pk_fpgl").toString(),invoicehm);
						}
					}
				}
			}
			//pc 传AGGVO数组
			//多条
			if (obj instanceof QueryBillVO) {
				//是否启用 发票报销模块 H013
				UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
				if(booleanFP.booleanValue()){
					QueryBillVO qbvos = (QueryBillVO) obj;
					BillVO[] bvos = qbvos.getQueryVOs();
					ArrayList<BillVO> bilist = new ArrayList<BillVO>();
					bilist.addAll(Arrays.asList(bvos));
					QueryBillVO qbillVO = new QueryBillVO();
					BillVO[] billVOs = new BillVO[bilist.size()]; 
					BillVO billVO = new BillVO();
					if(null != bilist && bilist.size() > 0 ){
						String billtypecode = new FeeBaseAction().getbilltypeCode(billtype);
						for(int i=0;i< bilist.size();i++){
							if(null != bilist.get(i).getHeadVO().get("pk_fpgl") && !"".equals(bilist.get(i).getHeadVO().get("pk_fpgl"))){
								String invoicehm = getFphm(billtype);
								if(null != invoicehm && !"".equals(invoicehm)){
									billVO =  getInvoiceCount(billtypecode,bilist.get(i).getHeadVO().get("pk_fpgl").toString(),invoicehm);
									billVOs[i] = billVO;
								}
							}
						}
						qbillVO.setQueryVOs(billVOs);
						return qbillVO;
					}
				}
			}

		}
		
		return super.processAction(account, userid, billtype, action, obj);
	}
	
	//PC 发票点击OK 复用
	public BillVO getInvoiceCount(String billtypecode,String pk_fpgl,String invoicehm) throws BusinessException{
		IPfExchangeService exchangeservice = NCLocator.getInstance().lookup(IPfExchangeService.class);
		nc.vo.erm.fppj.AggFppjHVO fpAggvo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(nc.vo.erm.fppj.AggFppjHVO.class, pk_fpgl, false);
		if(null != fpAggvo.getAllChildrenVO() && fpAggvo.getAllChildrenVO().length >0){
			//发票转换报销单 数据
			BXBusItemVO[] bxbusitem = (BXBusItemVO[]) exchangeservice.runChangeData("FP01", billtypecode, fpAggvo, null).getChildrenVO();
			//map去除重复
			ArrayList<BXBusItemVO> buList = new ArrayList<BXBusItemVO>();
			buList.addAll(Arrays.asList(bxbusitem));
			Map<String , String> map = new HashMap<String , String>();
			ArrayList<BXBusItemVO> newList = new ArrayList<BXBusItemVO>();
			if(null != buList && buList.size() > 0){
				for(int i= 0;i< buList.size();i++){
					if(!map.containsKey(buList.get(i).getAttributeValue(invoicehm).toString())){
						map.put(buList.get(i).getAttributeValue(invoicehm).toString(), buList.get(i).getAttributeValue(invoicehm).toString());
						newList.add(buList.get(i));
					}
				}
			}
			HashMap<String, Object>[] bvo = transNCVOTOMap(newList.toArray(new BXBusItemVO[0]));
			if(null != bvo && bvo.length > 0){
				BillVO billVO = new BillVO();
				billVO.setTableVO("arap_bxbusitem", bvo);
				return billVO;
			}
		}
		return null;
	}


	// 静态方法用于收支项目筛选
	public static List<InoutBusiClassVO> getSzxmid(String szxmid) throws BusinessException {

		String sql = "select pk_inoutbusiclass, code, name, pk_parent\n" +
		            " from bd_inoutbusiclass\n" + 
					"  where nvl(dr,0)=0\n" + 
					"   and pk_inoutbusiclass  = '"+szxmid+"'\n" + 
					//"   and pk_parent is not null\n" + 
					"   and pk_inoutbusiclass not in  (select  pk_parent\n" + 
					"                                    from bd_inoutbusiclass\n" + 
					"                                   where nvl(dr,0)=0\n" + 
					"                                     and pk_parent is not null)"; 

		ArrayList<InoutBusiClassVO> szxmidA = (ArrayList<InoutBusiClassVO>) dao.executeQuery(sql, new BeanListProcessor(InoutBusiClassVO.class));
		
		return szxmidA;
	}
	
	public Object province(String billtype, String pk_org,String defitem41) throws BusinessException {
		//省内外
		//根据报销类型和地区类别，查询出差补贴
		String sql = 

				"SELECT a.amount as code, a.Pk_reimtype\n" +
				"  FROM er_reimruler a\n" + 
				"  join bd_billtype b\n" + 
				"    on a.pk_billtype = b.pk_billtypecode\n" + 
				" WHERE nvl(a.dr, 0) = 0 and nvl(b.dr, 0) = 0\n" + 
				"   and b.pk_billtypeid = '"+billtype+"'\n" + 
				"   and a.pk_org = '"+pk_org+"'\n" + 
				"   and a.def1 = '"+defitem41+"'";

		ArrayList<ReimTypeVO> reimList = (ArrayList<ReimTypeVO>) dao.executeQuery(sql, new BeanListProcessor(ReimTypeVO.class));
		if(null != reimList && reimList.size() > 0){
			return reimList;
		}
		return null;
	}
	
	//东亮 复用
	public String[] formula(ArrayList<Object[]> cusmessage , String[] formula_str){
		if(null != cusmessage && cusmessage.size() > 0){
			if(null != cusmessage.get(0)[0] && !"".equals(cusmessage.get(0)[0])){
				String org = cusmessage.get(0)[0].toString();
				formula_str[0] = "pk_payorg->" + org + ";";
				formula_str[1] = "fydwbm->" + org + ";";
				formula_str[2] = "dwbm->" + org + ";";
				formula_str[5] = "pk_org->" + org + ";";
			}
			if(null != cusmessage.get(0)[3] && !"".equals(cusmessage.get(0)[3])){
				String dept = cusmessage.get(0)[3].toString();
				formula_str[3] = "fydeptid->" + dept + ";";
				formula_str[4] = "deptid->" + dept + ";";
			}
		}else{
			formula_str[0] = "pk_payorg->" + null + ";";
			formula_str[1] = "fydwbm->" + null + ";";
			formula_str[2] = "dwbm->" + null + ";";
			formula_str[3] = "fydeptid->" + null + ";";
			formula_str[4] = "deptid->" + null + ";";
			formula_str[5] = "pk_org->" + null + ";";
		}
		return formula_str;
	}
	
	
	/**
	 * 有发票的子表单据修改时修改其他字段
	 * 发票号字段加锁，会验证数据库中是否发票被报销
	 * 
	 */
	public void checkForInvoice(HashMap<String, Object>[] bxbusitem,BXBusItemVO[] dbBusiitems,StringBuffer fphms,HashMap<String, Object> head,String fphm,String userid)throws BusinessException {
		
		HashMap mapfp = new HashMap();
		String invoicePK = "";
		Boolean invoiceBoolean = new Boolean(true);
		List<BXBusItemVO> dbBusiitemsList = new ArrayList<BXBusItemVO>();
		if(null != dbBusiitems && dbBusiitems.length > 0){
			dbBusiitemsList = Arrays.asList(dbBusiitems);
		}
		//1wStringBuffer pk_busiitem = new StringBuffer();
		//子表有发票号  修改子表其他字段
		if(null != bxbusitem && bxbusitem.length > 0){
			for(int i=0; i<bxbusitem.length; i++){
				if(null!=bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm))){
					//锁发票字段时 取 发票pk
					invoicePK = bxbusitem[i].get(fphm).toString();
					fphms.append("'").append(bxbusitem[i].get(fphm)).append("',");
					if(null != bxbusitem[i].get("pk_busitem") && !"".equals(bxbusitem[i].get("pk_busitem"))){
						String pk_busitem = bxbusitem[i].get("pk_busitem").toString();
						if(null != dbBusiitemsList && dbBusiitemsList.size() > 0){
							for(int d = 0;d<dbBusiitemsList.size();d++){
								if(null != dbBusiitemsList.get(d).getPk_busitem() && !"".equals(null != dbBusiitemsList.get(d).getPk_busitem())){
									String db_pk_busitem =  dbBusiitemsList.get(d).getPk_busitem().toString();
									if(null != dbBusiitemsList.get(d).getAttributeValue(fphm) && !"".equals(dbBusiitemsList.get(d).getAttributeValue(fphm))){
										String db_invoicePK = dbBusiitemsList.get(d).getAttributeValue(fphm).toString();
										if(pk_busitem.equals(db_pk_busitem) && db_invoicePK.equals(invoicePK)){
											invoiceBoolean = false;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		/**
		 * 如果是修改单据  不重新从数据库查询数据
		 * 锁发票字段  会重新从数据库查询 抛出异常
		 */
		if(invoiceBoolean.booleanValue()){
			//发票是否已经报销
			if(fphms.length()>0){
				String sql = " select * from erm_fpgl where nvl(dr,0)=0 and billversionpk='Y' " +
						" and pk_fpgl in ("+fphms.substring(0,fphms.length()-1)+")" ;
					//	" "+pk_jkbxo+"";
				ArrayList<nc.vo.erm.fppj.FpglVO> fpvos = (ArrayList<nc.vo.erm.fppj.FpglVO>) dao.executeQuery(sql, new BeanListProcessor(nc.vo.erm.fppj.FpglVO.class));
				if(null != fpvos && fpvos.size() > 0){
					for(FpglVO billversionpk : fpvos){
						if(null != billversionpk.getBillversionpk() && !"".equals(billversionpk.getBillversionpk())){
							String StringBoolean = billversionpk.getBillversionpk().toString();
							if("Y".equals(StringBoolean) && null != billversionpk.getSrcbillid() && !"".equals(billversionpk.getSrcbillid())){
								throw new BusinessException("发票号已被报销:"+billversionpk.getFphm());
							}
						}
					}
				}
				/*fphms = new StringBuffer();
				for(int i=0; i<fpvos.size(); i++){
					fphms.append(fpvos.get(i).getFphm()).append(",");
				}
				if(fphms.length()>0){
					throw new BusinessException("发票号已被报销:"+fphms);
				}*/
			}
		}
	}
	
	
	
	//发票验重   save()
	public void  iterateInvoice(HashMap<String, Object>[] bxbusitem,String fphm) throws BusinessException{
		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		if(null != bxbusitem && bxbusitem.length > 0){
			for(int i=0;i<bxbusitem.length;i++){
				if((null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm)))){
					if(hashMap.containsKey(bxbusitem[i].get(fphm))){
						String strWhere = " nvl(dr,0) = 0 and pk_fpgl  = '"+bxbusitem[i].get(fphm)+"'";
						ArrayList<SuperVO> headlist = (ArrayList<SuperVO>) dao.retrieveByClause(nc.vo.erm.fppj.FpglVO.class, strWhere);
						if(null != headlist && headlist.size() > 0){
							throw new BusinessException("发票号不能重复:"+(null!=bxbusitem[i].get(fphm)?headlist.get(0).getAttributeValue("fphm"):""));
						}
					}else{
						if((null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm)))){
							hashMap.put(bxbusitem[i].get(fphm).toString(), 1);
						}
					}
				}
			}
		}
	}
	

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();//集合VO ，bill
		HashMap<String, Object> head = bill.getHeadVO();//表头
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();//表体
		IUAPQueryBS bx = NCLocator.getInstance().lookup(IUAPQueryBS.class); 
		HashMap<String, Object>[] other = bodys.get("other");//其他费用
		HashMap<String, Object>[] bxbusitem = bodys.get("arap_bxbusitem");//交通费用
		HashMap<String, Object>[] bxcontrast = bodys.get("er_bxcontrast");//冲销明细
		HashMap<String, Object>[] cshare = bodys.get("er_cshare_detail");//分摊明细
		String[] formula_str = new String[50];
		//默认个人银行账户
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

		//默认职务
		if(aevo.getKey().equals("zyx1")){
			ArrayList<Object[]> zyx1 = new FeeBaseAction().getOm_job(userid, null);
			if(null != zyx1 && zyx1.size() > 0){
				if(null != zyx1.get(0)[0] && !"".equals(zyx1.get(0)[0])){
					formula_str[1] = "zyx1->"+zyx1.get(0)[0].toString()+";";
				}
			}
			FormulaVO formulaVO = new FormulaVO();
			formulaVO.setFormulas(formula_str);
			return formulaVO;
		}

		/**结算方式带出现金账户 
		 * NC为固定公式 pk 写死  这里和NC保持一致
		 */
		if(aevo.getKey().equals("jsfs")){
			if(null != head.get("jsfs") && !"".equals(head.get("jsfs")) ){
				String jshj = head.get("jsfs").toString();
				if("0001Z0100000000000XZ".equals(jshj) || "0001Z0100000000000Y0".equals(jshj)){
					String strWhere = " nvl(dr,0) = 0 and pk_org  = '"+head.get("pk_org")+"'";
					ArrayList<SuperVO> CashAccountlist = (ArrayList<SuperVO>) dao.retrieveByClause(CashAccountVO.class, strWhere);
					if(null != CashAccountlist && CashAccountlist.size() >0){
						formula_str[1] = "pk_cashaccount->"+CashAccountlist.get(0).getAttributeValue("pk_cashaccount")+";";
					}
					FormulaVO formulaVO = new FormulaVO();
					formulaVO.setFormulas(formula_str);
					return formulaVO;
				}
			}
		}

		if(null != bxbusitem && bxbusitem.length > 0){
			for(int i=0;i<bxbusitem.length;i++){

				//校验收支项目不能选末级
				if(aevo.getKey().equals("szxmid")){
					if(null != bxbusitem[i].get("szxmid") && !"".equals(bxbusitem[i].get("szxmid"))){
						String szxmidString = bxbusitem[i].get("szxmid").toString();
						ArrayList<InoutBusiClassVO> szx = (ArrayList<InoutBusiClassVO>) BXzbAction.getSzxmid(szxmidString);
						if(null == szx || szx.size() == 0){
							throw new BusinessException("请选择末级收支项目");
						}
					}
				}

				//发票编辑后事件  动态查询发票字段  dongl
				if(booleanFP.booleanValue()){
					String fphm = getFphm(getBilltype());
					if (aevo.getKey().equals(fphm)) {
						int f = 0;//数组长度写死了是50
						if(null != fphm && !"".equals(fphm)){
							if(null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm))){
								if(null != head.get("pk_tradetypeid_code") && !"".equals(head.get("pk_tradetypeid_code"))){
									String pk_tradetypeid_code = head.get("pk_tradetypeid_code").toString();
									//1.查聚合VO
									nc.vo.erm.fppj.AggFppjHVO fpAggvo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(nc.vo.erm.fppj.AggFppjHVO.class, bxbusitem[i].get(fphm).toString(), false);
									//2.数据转换接口
									IPfExchangeService exchangeservice = NCLocator.getInstance().lookup(IPfExchangeService.class);
									BXBusItemVO[] bvos = (BXBusItemVO[]) exchangeservice.runChangeData("FP01", pk_tradetypeid_code, fpAggvo, null).getChildrenVO();
									if(null != bvos && bvos.length >0){
										HashMap<String, Object>[] maps = transNCVOTOMap(bvos);
										for(int b=0;b<maps.length;b++){
											HashMap<String,Object> bmaps = maps[b];
											Iterator<Map.Entry<String, Object>> iterator = bmaps.entrySet().iterator();
											//3.迭代转换后数据 放入数组， formulaVO 返回结果
											while (iterator.hasNext()) {
												Entry<String, Object> entry = iterator.next();
												if(null != entry.getValue()){
													formula_str[++f] = ""+entry.getKey()+"->"+entry.getValue()+";";
												}
											}
										}

										FormulaVO formulaVO = new FormulaVO();
										formulaVO.setFormulas(formula_str);
										return formulaVO;

									}
								}
							}
						}
					}
				}

				//无效票据金额 编辑后事件
				if (aevo.getKey().equals("defitem19")) {
					if(null != bxbusitem[i].get("defitem19") && !"".equals(bxbusitem[i].get("defitem19"))){
						UFDouble defitem19 = new UFDouble(bxbusitem[i].get("defitem19").toString());
						if(null != bxbusitem[i].get("defitem24") && !"".equals(bxbusitem[i].get("defitem24"))){
							UFDouble defitem24 = new UFDouble(bxbusitem[i].get("defitem24").toString());
							if(defitem19.compareTo(defitem24) != 0){
								//不含税金额 项目主键	tni_amount
								UFDouble tni_amount = new UFDouble(0);
								if(null != bxbusitem[i].get("defitem22") && !"".equals(bxbusitem[i].get("defitem22"))){
									UFDouble sl = new UFDouble(bxbusitem[i].get("defitem22").toString()).div(100);
									tni_amount= defitem24.div(sl.add(1));
									formula_str[0] = "tni_amount->"+tni_amount.setScale(2, UFDouble.ROUND_HALF_UP)+";";
								}
								//核保金额 项目主键	vat_amount
								if(null != tni_amount && tni_amount.compareTo(new UFDouble(0)) > 0){
									formula_str[1] = "vat_amount->"+defitem24.setScale(2, UFDouble.ROUND_HALF_UP)+";";
								}
								//计费金额 项目主键	amount
								if(null != tni_amount && tni_amount.compareTo(new UFDouble(0)) > 0){
									formula_str[2] = "amount->"+tni_amount.setScale(2, UFDouble.ROUND_HALF_UP)+";";	
								}
								//税金金额 项目主键	tax_amount
								if(null != tni_amount && tni_amount.compareTo(new UFDouble(0)) > 0){
									UFDouble tax_amount = defitem24.sub(tni_amount);
									formula_str[3] = "tax_amount->"+tax_amount.setScale(2, UFDouble.ROUND_HALF_UP)+";";	
								}
								FormulaVO formulaVO = new FormulaVO();
								formulaVO.setFormulas(formula_str);
								return formulaVO;
							}
						}
					}
				}
			}

		}

		if(null != other && other.length > 0){
			for(int i=0;i<other.length;i++){
				//职务默认
				if (aevo.getKey().equals("defitem16")) {
					if(null != head.get("zyx1") && !"".equals(head.get("zyx1"))){
						String zyx1 =head.get("zyx1").toString();
						formula_str[0] = "defitem15->"+zyx1+";";
						ArrayList<Object[]> omList = new FeeBaseAction().getOm_job(userid, zyx1.toString());
						if(null != omList && omList.size() > 0){
							if(null != omList.get(0)[3] && !"".equals(omList.get(0)[3])){
								formula_str[1] = "defitem16->"+omList.get(0)[3].toString()+";";
							}
						}
					}
				}
				if (aevo.getKey().equals("defitem15")) {
					if(null != other[i].get("defitem15") && !"".equals(other[i].get("defitem15"))){
						ArrayList<Object[]> omList = new FeeBaseAction().getOm_job(userid, other[i].get("defitem15").toString());
						if(null != omList && omList.size() > 0){
							if(null != omList.get(0)[3] && !"".equals(omList.get(0)[3])){
								formula_str[2] = "defitem16->"+omList.get(0)[3].toString()+";";
							}
						}
					}
				}
				//出差地点编辑后事件
				if (aevo.getKey().equals("defitem13")) {
					if(null != other[i].get("defitem13") && !"".equals(other[i].get("defitem13"))){
						if(null != other[i].get("defitem21") && !"".equals(other[i].get("defitem21"))){
							StringBuffer sql = new StringBuffer(
									"select distinct bd_defdoc.pk_defdoc ,bd_defdoc.code,bd_defdoc.name ,er_reimruler.amount ,er_reimtype.pk_reimtype\n" +
											"  from bd_defdoc bd_defdoc\n" + 
											"  join bd_defdoclist bd_defdoclist\n" + 
											"  on bd_defdoclist.pk_defdoclist = bd_defdoc.pk_defdoclist\n" + 
											"  join er_reimruler er_reimruler\n" + 
											"    on bd_defdoc.pk_defdoc = er_reimruler.def2\n" + 
											"  join er_reimtype er_reimtype\n" + 
											"    on er_reimtype.pk_reimtype = er_reimruler.pk_reimtype\n" + 
									"  where bd_defdoclist.code = 'ERM001'");
							//费用类型
							sql.append(" and er_reimruler.def2 = '"+other[i].get("defitem21")+"'");
							//职务类型
							if(null != other[i].get("defitem16") && !"".equals(other[i].get("defitem16"))){
								sql.append(" and er_reimruler.def3 = '"+other[i].get("defitem16")+"'");
							}
							//地区分类
							if(null != other[i].get("defitem14") && !"".equals(other[i].get("defitem14"))){
								sql.append(" and er_reimruler.def1 = '"+other[i].get("defitem14")+"'");
							}
							sql.append(" and nvl(bd_defdoc.dr,0)= 0\n" +
									" and nvl(bd_defdoclist.dr,0)= 0\n" + 
									" and nvl(er_reimtype.dr,0)= 0\n" + 
									" and nvl(er_reimruler.dr,0)= 0"+
									" and er_reimruler.pk_org = '"+head.get("pk_org")+"'");
							sql.append(" order by bd_defdoc.code");
							ArrayList<Object[]> cusmessage = new ArrayList<Object[]>();
							cusmessage = (ArrayList<Object[]>) dao.executeQuery(sql.toString(),new ArrayListProcessor());
							if (null != cusmessage && cusmessage.size() > 0) {
								formula_str[3] = "defitem17->"+cusmessage.get(0)[3].toString()+";";
								formula_str[4] = "pk_reimtype->"+cusmessage.get(0)[4].toString()+";";
							}
						}
					}
				}

				//实报金额编辑后事件   项目主键	defitem24
				if (aevo.getKey().equals("defitem24")) {
					if(null != other[i].get("defitem24") && !"".equals(other[i].get("defitem24"))){
						UFDouble defitem24 = new UFDouble(other[i].get("defitem24").toString());
						//合计金额  费用页签
						UFDouble sumamount = new UFDouble(0);
						for(int b=0; null!=bxbusitem && b<bxbusitem.length; b++){
							if(null!=bxbusitem[b].get("amount") && !"".equals(bxbusitem[b].get("amount"))){
								UFDouble amount = new UFDouble(bxbusitem[b].get("amount").toString());
								if(null!=bxbusitem[b].get("vostatus") && 3!=Integer.parseInt(bxbusitem[b].get("vostatus").toString())){
									sumamount = sumamount.add(amount);
								}
							} 
						}
					}
				}
				FormulaVO formulaVO = new FormulaVO();
				formulaVO.setFormulas(formula_str);
				return formulaVO;
			}
		}
		/**
		 * 1.冲借款，冲销明细页签 借款单号编辑后带出事件
		 */
		if (null != bxcontrast && bxcontrast.length > 0) {
			for (int i = 0; i < bxcontrast.length; i++) {
				//借款单号编辑后事件
				if (aevo.getKey().equals("jkdjbh")) {
					if(null != bxcontrast[i].get("jkdjbh") && !"".equals(bxcontrast[i].get("jkdjbh"))){

						//校验子表如果有单行 金额为0 的情况下 给出 冲销提示
						if(null!= other && other.length > 0){
							for(int b=0; null!=other && b<other.length; b++){
								if(null!=other[b].get("amount") && !"".equals(other[b].get("amount"))){
									UFDouble amount = new UFDouble(other[b].get("amount").toString());
									if(amount.compareTo(new UFDouble(0)) <= 0){
										throw new BusinessException("报销单业务行金额都要大于0,才进行冲借款操作！");
									}
								} 
							}
						}

						String sql = "select z.jkbxr,e.yjye,e.szxmid from er_busitem e left join er_jkzb z\n" +
								"           on e.pk_jkbx = z.pk_jkbx\n" + 
								"           where nvl(e.dr,0) = 0\n" + 
								"                 and nvl(z.dr,0) = 0\n" + 
								"                 and e.pk_busitem = '"+bxcontrast[i].get("jkdjbh")+"'";

						ArrayList<Object[]> pkbusitem = (ArrayList<Object[]>)	dao.executeQuery(sql, new ArrayListProcessor());
						if(null != pkbusitem && pkbusitem.size()>0){
							formula_str[0] = "jkbxr->" + pkbusitem.get(0)[0] + ";";
							formula_str[1] = "cjkybje->" + pkbusitem.get(0)[1] + "";
							formula_str[2] = "szxmid->" + pkbusitem.get(0)[2] + ";";
							//formula_str[3] = "fyybje->" + pkbusitem.get(0)[1] + ";"; 
							formula_str[4] = "pk_busitem->" + bxcontrast[i].get("jkdjbh") + ";"; 
							/*//之前保存的数据+界面新传值得数据
					UFDouble jm = new UFDouble();
					if(null != bxcontrast[i].get("cjkybje") && !"".equals(bxcontrast[i].get("cjkybje"))){
						jm =  new UFDouble(bxcontrast[i].get("cjkybje").toString()); 
					}
					UFDouble jms = new UFDouble(jm);
					UFDouble cjkybje1 = new UFDouble(pkbusitem.get(0).getYjye().toString());
					String cjkybje = jm.add(cjkybje1).setScale(2, UFDouble.ROUND_HALF_UP) + ";";*/	

							FormulaVO formulaVO = new FormulaVO();
							formulaVO.setFormulas(formula_str);
							return formulaVO; 
						} 
					} 
				}
			}
		}

		/**
		 * 2. 分摊 分摊比例和承担金额 编辑后事件 
		 */
		if(null!=cshare && cshare.length>0){
			for(int i=0; i<cshare.length; i++){
				//分摊比例 和 承担金额 编辑后事件
				if(aevo.getKey().equals("share_ratio") || aevo.getKey().equals("assume_amount")){

					//合计金额
					UFDouble sumamount = new UFDouble(0);
					for(int b=0; null!=bxbusitem && b<bxbusitem.length; b++){
						if(null!=bxbusitem[b].get("amount") && !"".equals(bxbusitem[b].get("amount"))){
							UFDouble amount = new UFDouble(bxbusitem[b].get("amount").toString());
							if(null!=bxbusitem[b].get("vostatus") && 3!=Integer.parseInt(bxbusitem[b].get("vostatus").toString())){
								sumamount = sumamount.add(amount);
							}
						} 
					}
					for(int b=0; null!=other && b<other.length; b++){
						if(null!=other[b].get("amount") && !"".equals(other[b].get("amount"))){
							UFDouble amount = new UFDouble(other[b].get("amount").toString());
							if(null!=other[b].get("vostatus") && 3!=Integer.parseInt(other[b].get("vostatus").toString())){
								sumamount = sumamount.add(amount);
							}
						} 
					}
					//分摊比例 编辑后计算 承担金额
					if(aevo.getKey().equals("share_ratio")){
						if(null!=cshare[i].get("share_ratio") && !"".equals(cshare[i].get("share_ratio"))){
							UFDouble share_ratio = new UFDouble(cshare[i].get("share_ratio").toString()).div(100);
							UFDouble assume_amount = sumamount.multiply(share_ratio);
							formula_str[0] = "assume_amount->" + assume_amount.setScale(2, UFDouble.ROUND_HALF_UP) + ";";

							FormulaVO formulaVO = new FormulaVO();
							formulaVO.setFormulas(formula_str);
							return formulaVO; 
						} 
					}
					//承担金额 编辑后计算 分摊比例
					if(aevo.getKey().equals("assume_amount")){
						if(null!=cshare[i].get("assume_amount") && !"".equals(cshare[i].get("assume_amount"))){
							UFDouble assume_amount = new UFDouble(cshare[i].get("assume_amount").toString());
							if(sumamount.compareTo(new UFDouble(0))!=0 && assume_amount.compareTo(new UFDouble(0))!=0){
								UFDouble share_ratio = assume_amount.div(sumamount).multiply(100);
								formula_str[0] = "share_ratio->" + share_ratio + ";";

								FormulaVO formulaVO = new FormulaVO();
								formulaVO.setFormulas(formula_str);
								return formulaVO; 
							} 
						} 
					}
				}
			}
		}
		return null;
	}

	static HashMap<String, String> summap = new HashMap<String, String>();
	static HashMap<String, String> invoiceDefMap = new HashMap<String, String>();
	static HashMap<String, String> billtypeMap = new HashMap<String, String>();
	static HashMap<String, String> deptMap = new HashMap<String, String>();
	static HashMap<String, String> orgMap = new HashMap<String, String>();
	static UFBoolean booleanFP = new UFBoolean(false);
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
			sql = "SELECT pk_billtypecode,pk_billtypeid FROM bd_billtype WHERE isnull(dr, 0) = 0 and pk_billtypecode like '26%'";
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
			
			//组织
			sql = "select * from org_orgs where isnull(dr,0)=0 ";
			ArrayList<OrgVO> orgsList = (ArrayList<OrgVO>) dao.executeQuery(sql, new BeanListProcessor(OrgVO.class));
			for (int i = 0; i < orgsList.size(); i++) {
				orgMap.put(orgsList.get(i).getPk_org(),orgsList.get(i).getPk_vid());
			}
			// 查询业务参数 是否启用发票
			booleanFP = new FeeBaseAction().getsysinitFP();
			
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}
	
	String pk_dept,pk_org,pk_group = null;
	String jkbxr = "";
	String receiver = "";
	/**
	 * 封装报销主表
	 */
	public void setHVO(BXHeaderVO hvo,HashMap<String, Object> head,String userid) throws BusinessException{
		if (null == head.get("pk_org") || "".equals(head.get("pk_org"))) {
			throw new BusinessException("组织不能空!");
		}

		if(null!=head.get("pk_tradetypeid")){
			for(Map.Entry<String, String> billtype: billtypeMap.entrySet()){
				if(head.get("pk_tradetypeid").equals(billtype.getKey())){
					head.put("pk_tradetypeid_code", billtype.getValue());
				}
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
		
		hvo.setPk_org_v(orgMap.get(pk_org)); 
		// 给集团赋值
		if (null != head.get("pk_group") && !"".equals(head.get("pk_group"))) {
			pk_group = head.get("pk_group").toString();
			hvo.setPk_group(pk_group);
		}
		
		// 财务组织
		hvo.setPk_fiorg(pk_org);
		// 原支付组织
		hvo.setPk_payorg(pk_org);
		hvo.setPk_payorg_v(orgMap.get(pk_org));// 支付单位
		// 原报销人单位
		hvo.setDwbm(pk_org);
		hvo.setDwbm_v(orgMap.get(pk_org));
		// 费用承担单位 fydwbm
		hvo.setFydwbm(pk_org);
		hvo.setFydwbm_v(orgMap.get(pk_org));
		if(null==head.get("PK")){
			hvo.setCreationtime(new UFDateTime(new Date()));
			hvo.setDjrq(new UFDate(new Date()));
		}
		
		hvo.setCreator(userid);
		String tradetypeid = head.get("pk_tradetypeid").toString();
		hvo.setPk_tradetypeid(tradetypeid);
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
		// 结算方式
		if (null != head.get("jsfs")) {
			hvo.setJsfs(head.get("jsfs").toString());
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
		hvo.setDjdl("bx");

		// 交易类型编码
		if (null != head.get("pk_tradetypeid_code") && !"".equals(head.get("pk_tradetypeid_code"))) {
			hvo.setDjlxbm(head.get("pk_tradetypeid_code").toString());
		} else {
			throw new BusinessException("交易类型不能空");
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
		if (null != head.get("srcbilltype") && !"".equals(head.get("srcbilltype"))) {
			hvo.setSrcbilltype(head.get("srcbilltype").toString()); 
		}
		if (null != head.get("srctype") && !"".equals(head.get("srctype"))) {
			hvo.setSrctype(head.get("srctype").toString()); 
		}
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
		hvo.setGroupbbje(new UFDouble(0));
		hvo.setGroupbbhl(new UFDouble(0));
		hvo.setGlobalzfbbje(new UFDouble(0));
		hvo.setGlobalhkbbje(new UFDouble(0));
		hvo.setGlobalcjkbbje(new UFDouble(0));
		hvo.setGlobalbbje(new UFDouble(0));
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
		//开始摊销期间
		if (null != head.get("start_period") && !"".equals(head.get("start_period"))) {
			hvo.setStart_period( head.get("start_period").toString()); 
		}
		//总摊销期
		if (null != head.get("total_period") && !"".equals(head.get("total_period"))) {
			hvo.setTotal_period(Integer.parseInt(head.get("total_period").toString())); 
		}
	}
	
	/**
	 * 封装busiitem子表
	 */
	public void setBusiitemBVO(BXHeaderVO hvo,HashMap<String, Object> head,BXBusItemVO busiitemBVO,HashMap<String, Object> bxbusitem){
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
		
		if(null != bxbusitem.get("pk_item") && !"".equals(bxbusitem.get("pk_item"))){
			busiitemBVO.setPk_item(bxbusitem.get("pk_item").toString());
		}
		if(null != bxbusitem.get("srcbilltype") && !"".equals(bxbusitem.get("srcbilltype"))){
			busiitemBVO.setSrcbilltype(bxbusitem.get("srcbilltype").toString());
			busiitemBVO.setSrctype("261X");
		} 
		//参照生单 
		if(null != bxbusitem.get("pk_mtapp_detail") && !"".equals(bxbusitem.get("pk_mtapp_detail"))){ 
			busiitemBVO.setPk_mtapp_detail(bxbusitem.get("pk_mtapp_detail").toString());
		}else{
			//dongl 如果删除了参照出的子表，新增一条子表 需要设置 来源单据相关信息
			if (null != head.get("srcbilltype") && !"".equals(head.get("srcbilltype"))) {
				busiitemBVO.setSrcbilltype(head.get("srcbilltype").toString()); 
			}
			if (null != head.get("srctype") && !"".equals(head.get("srctype"))) {
				busiitemBVO.setSrctype(head.get("srctype").toString()); 
			}
			if (null != head.get("pk_item") && !"".equals(head.get("pk_item"))) {
				busiitemBVO.setPk_item(head.get("pk_item").toString()); 
			}

		}
	}
	
	
	
	/**
	 * 分摊明细子表 封装 
	 */
	public void setShareVO(CShareDetailVO csvo,HashMap<String, Object> cshare,BXHeaderVO hvo){
		if(null!=cshare.get("defitem1") && !"".equals(cshare.get("defitem1"))){
			csvo.setDefitem1(cshare.get("defitem1").toString());
		}
		if(null!=cshare.get("defitem2") && !"".equals(cshare.get("defitem2"))){
			csvo.setDefitem2(cshare.get("defitem2").toString());
		}
		if(null!=cshare.get("defitem3") && !"".equals(cshare.get("defitem3"))){
			csvo.setDefitem3(cshare.get("defitem3").toString());
		}
		if(null!=cshare.get("defitem4") && !"".equals(cshare.get("defitem4"))){
			csvo.setDefitem4(cshare.get("defitem4").toString());
		}
		if(null!=cshare.get("defitem5") && !"".equals(cshare.get("defitem5"))){
			csvo.setDefitem5(cshare.get("defitem5").toString());
		}
		if(null!=cshare.get("defitem6") && !"".equals(cshare.get("defitem6"))){
			csvo.setDefitem6(cshare.get("defitem6").toString());
		}
		if(null!=cshare.get("defitem7") && !"".equals(cshare.get("defitem7"))){
			csvo.setDefitem7(cshare.get("defitem7").toString());
		}
		if(null!=cshare.get("defitem8") && !"".equals(cshare.get("defitem8"))){
			csvo.setDefitem8(cshare.get("defitem8").toString());
		}
		if(null!=cshare.get("defitem9") && !"".equals(cshare.get("defitem9"))){
			csvo.setDefitem9(cshare.get("defitem9").toString());
		}
		if(null!=cshare.get("defitem10") && !"".equals(cshare.get("defitem10"))){
			csvo.setDefitem10(cshare.get("defitem10").toString());
		}
		if(null!=cshare.get("defitem11") && !"".equals(cshare.get("defitem11"))){
			csvo.setDefitem11(cshare.get("defitem11").toString());
		}
		if(null!=cshare.get("defitem12") && !"".equals(cshare.get("defitem12"))){
			csvo.setDefitem12(cshare.get("defitem12").toString());
		}
		if(null!=cshare.get("defitem13") && !"".equals(cshare.get("defitem13"))){
			csvo.setDefitem13(cshare.get("defitem13").toString());
		}
		if(null!=cshare.get("defitem14") && !"".equals(cshare.get("defitem14"))){
			csvo.setDefitem14(cshare.get("defitem14").toString());
		}
		if(null!=cshare.get("defitem15") && !"".equals(cshare.get("defitem15"))){
			csvo.setDefitem15(cshare.get("defitem15").toString());
		}
		if(null!=cshare.get("defitem16") && !"".equals(cshare.get("defitem16"))){
			csvo.setDefitem16(cshare.get("defitem16").toString());
		}
		if(null!=cshare.get("defitem17") && !"".equals(cshare.get("defitem17"))){
			csvo.setDefitem17(cshare.get("defitem17").toString());
		}
		if(null!=cshare.get("defitem18") && !"".equals(cshare.get("defitem18"))){
			csvo.setDefitem18(cshare.get("defitem18").toString());
		}
		if(null!=cshare.get("defitem19") && !"".equals(cshare.get("defitem19"))){
			csvo.setDefitem19(cshare.get("defitem19").toString());
		}
		if(null!=cshare.get("defitem20") && !"".equals(cshare.get("defitem20"))){
			csvo.setDefitem20(cshare.get("defitem20").toString());
		} 
		if(null!=cshare.get("defitem21") && !"".equals(cshare.get("defitem21"))){
			csvo.setDefitem21(cshare.get("defitem21").toString());
		}
		if(null!=cshare.get("defitem22") && !"".equals(cshare.get("defitem22"))){
			csvo.setDefitem22(cshare.get("defitem22").toString());
		}
		if(null!=cshare.get("defitem23") && !"".equals(cshare.get("defitem23"))){
			csvo.setDefitem23(cshare.get("defitem23").toString());
		}
		if(null!=cshare.get("defitem24") && !"".equals(cshare.get("defitem24"))){
			csvo.setDefitem24(cshare.get("defitem24").toString());
		}
		if(null!=cshare.get("defitem25") && !"".equals(cshare.get("defitem25"))){
			csvo.setDefitem25(cshare.get("defitem25").toString());
		}
		if(null!=cshare.get("defitem26") && !"".equals(cshare.get("defitem26"))){
			csvo.setDefitem26(cshare.get("defitem26").toString());
		}
		if(null!=cshare.get("defitem27") && !"".equals(cshare.get("defitem27"))){
			csvo.setDefitem27(cshare.get("defitem27").toString());
		}
		if(null!=cshare.get("defitem28") && !"".equals(cshare.get("defitem28"))){
			csvo.setDefitem28(cshare.get("defitem28").toString());
		}
		if(null!=cshare.get("defitem29") && !"".equals(cshare.get("defitem29"))){
			csvo.setDefitem29(cshare.get("defitem29").toString());
		}
		if(null!=cshare.get("defitem30") && !"".equals(cshare.get("defitem30"))){
			csvo.setDefitem30(cshare.get("defitem30").toString());
		} 
		
		if(null!=cshare.get("assume_org") && !"".equals(cshare.get("assume_org"))){
			csvo.setAssume_org(cshare.get("assume_org").toString());
		} 
		if(null!=cshare.get("assume_dept") && !"".equals(cshare.get("assume_dept"))){
			csvo.setAssume_dept(cshare.get("assume_dept").toString());
		} 
		if(null!=cshare.get("pk_iobsclass") && !"".equals(cshare.get("pk_iobsclass"))){
			csvo.setPk_iobsclass(cshare.get("pk_iobsclass").toString());
		} 
		
		if(null!=cshare.get("bbhl") && !"".equals(cshare.get("bbhl"))){
			csvo.setBbhl(new UFDouble(cshare.get("bbhl").toString()));
		} else{
			csvo.setBbhl(new UFDouble(1));
		}
		if(null!=cshare.get("billno") && !"".equals(cshare.get("billno"))){
			csvo.setBillno(cshare.get("billno").toString());//结转单 单号
		}  
//		csvo.setStatus(1);
		if(null!=cshare.get("bzbm") && !"".equals(cshare.get("bzbm"))){
			csvo.setBzbm(cshare.get("bzbm").toString()); 
		} 
		if(null!=cshare.get("customer") && !"".equals(cshare.get("customer"))){
			csvo.setCustomer(cshare.get("customer").toString()); 
		} 
		
		UFDouble zero = new UFDouble(0);
		csvo.setDr(0);
		csvo.setGlobalbbhl(zero);
		csvo.setGlobalbbje(zero);
		csvo.setGroupbbhl(zero);
		csvo.setGroupbbje(zero);
		if(null!=cshare.get("pk_billtype") && !"".equals(cshare.get("pk_billtype"))){
			csvo.setPk_billtype(cshare.get("pk_billtype").toString()); 
		}else{
			csvo.setPk_billtype("265X");
		}
		if(null!=cshare.get("pk_costshare") && !"".equals(cshare.get("pk_costshare"))){
			csvo.setPk_costshare(cshare.get("pk_costshare").toString()); 
			csvo.setSrc_type(1);
			csvo.setBillstatus(1);
		}
		csvo.setPk_group(pk_group);
		csvo.setPk_jkbx(hvo.getPk_jkbx());
		csvo.setPk_org(pk_org);
		if(null!=cshare.get("pk_tradetype") && !"".equals(cshare.get("pk_tradetype"))){
			csvo.setPk_tradetype(cshare.get("pk_tradetype").toString()); 
		}
		if(null!=cshare.get("src_id") && !"".equals(cshare.get("src_id"))){
			csvo.setSrc_id(cshare.get("src_id").toString()); 
		} 
		if(null!=cshare.get("pk_cshare_detail") && !"".equals(cshare.get("pk_cshare_detail"))){
			csvo.setPk_cshare_detail(cshare.get("pk_cshare_detail").toString()); 
		} 
	}
	
	//加锁处理
	public void isLockInvoice(HashMap<String, Object>[] bxbusitem,String fphm,String userid)throws BusinessException{
		if(null != bxbusitem && bxbusitem.length > 0){
			for(int i = 0;i < bxbusitem.length;i++){
				if(null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm))){
					String invoicePK = bxbusitem[i].get(fphm).toString();
					//对发票号 加锁处理
					isLockSave(userid,invoicePK);
				}
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Object save(String userid, Object obj) throws BusinessException { 
		BillVO bill = (BillVO) obj;
		//String tableCode = aevo.getTabcode();//得到传进来的页签名字
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] bxbusitem = bodys.get("arap_bxbusitem");//交通费用
		HashMap<String, Object>[] other = bodys.get("other");//其他费用
		//加锁
		if(booleanFP.booleanValue()){
			fphm = getFphm(getBilltype());
			isLockInvoice(bxbusitem,fphm,userid);
		}
		
		HashMap<String, Object>[] bxcontrasts = bodys.get("er_bxcontrast");//冲销明细
		HashMap<String, Object>[] cshare = bodys.get("er_cshare_detail");//分摊明细
		
		

		BXVO aggvo = new BXVO();//集合VO
		BXVO transVOs = null;//修改时传入的聚合VO

		BXHeaderVO hvo = new BXHeaderVO();//报销单表头

		JKHeaderVO jkvos = new JKHeaderVO();//表头冲借款

		BXVO cloneagg = new BXVO();//修改时传入的聚合VO封装VO

		BxcontrastVO[] dbContrasts = null;// 数据库-冲借款VO
		BXBusItemVO[] dbBusiitems = null;// 数据库-子表明细VO

		String sql = "";
		StringBuffer fphms = new StringBuffer();
		StringBuffer defitem38 = new StringBuffer();
		
		//原始的主表total数据
		String pk_jkbx = "";//表头主键
		UFDouble headTotal = new UFDouble();//表头总金额

		if (null != head.get("PK") && !"".equals(head.get("PK"))) {//首先根据主表PK判断是保存还是修改

			pk_jkbx = head.get("PK").toString();
			
			String sqlh = "select * from er_bxzb where isnull(dr,0)=0 and pk_jkbx ='" + pk_jkbx + "' order by ts desc";
			ArrayList<BXHeaderVO> headvo = (ArrayList<BXHeaderVO>) dao.executeQuery(sqlh, new BeanListProcessor(BXHeaderVO.class));
			if(null!=headvo && headvo.size()>0){
				headTotal = headvo.get(0).getTotal();//如果是修改的话先得到传进来   最初的 没修改时 表头总金额
			}

			Collection<BXVO> generalVOC = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(BXVO.class, " isnull(dr,0)=0 and pk_jkbx='" + pk_jkbx + "' ", false);
			if (null == generalVOC || generalVOC.size() == 0) {//修改完又删除了的情况
				throw new BusinessException("修改的订单已经被删除!");
			}
			//数据库查询聚合VO
			transVOs = generalVOC.toArray(new BXVO[] {})[0];
			//主表vo
			hvo = (BXHeaderVO) transVOs.getParentVO();
			//交通，其他费用
			dbBusiitems = transVOs.getBxBusItemVOS();
			//冲销明细
			dbContrasts = transVOs.getContrastVO();

			headTotal = hvo.getTotal();
			hvo.setModifier(userid);

			BXBusItemVO[] bclone = new BXBusItemVO[dbBusiitems.length];
			for (int b = 0; b < dbBusiitems.length; b++) {//将传进来的交通，其他费用遍历赋给克隆对象
				bclone[b] = new BXBusItemVO();
				bclone[b] = (BXBusItemVO) dbBusiitems[b].clone();
			}
			cloneagg.setParentVO((CircularlyAccessibleValueObject) hvo.clone());
			cloneagg.setChildrenVO(bclone);
			hvo.setStatus(1);//状态1是修改的意思
		} else {
			hvo.setStatus(2);//状态2是保存的意思
		}
		
		/**
		 * 1.设置主表的字段
		 */
		setHVO(hvo,head,userid);
		/**
		 * 查询发票在报销单中是否被引用过 和 发票验重
		 * 发票在 两个 任意子表
		 */
		//发票号码字段值 dongl
		if(booleanFP.booleanValue()){
			if(null != fphm && !"".equals(fphm)){
				ArrayList<BillyqBVO> yqList  = getFphm(getBilltype(),fphm);
				if(null != yqList && yqList.size() > 0){
                    String page = yqList.get(0).getTabcode();					
					if(null != page && !"".equals(page)){
						if("other".equals(page)){
							checkForInvoice(other,dbBusiitems,fphms,head,fphm,userid);//查询发票在报销单中是否被引用过
							iterateInvoice(other,fphm);//发票是否重复
						}else{
							checkForInvoice(bxbusitem,dbBusiitems,fphms,head,fphm,userid);//查询发票在报销单中是否被引用过
							iterateInvoice(bxbusitem,fphm);//发票是否重复
						}
					}
				}
			}
		}
		
		//设置借款的headVO
		UFDouble bbje = new UFDouble();
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			hvo.setModifier(userid);
			if (null != dbContrasts) {
				for (int i = 0; i < dbContrasts.length; i++) {
					UFDouble bbje2 = dbContrasts[i].getBbje();
					bbje = bbje.add(bbje2);
				}
			}
		}
		UFDouble totalAmount = new UFDouble();//报销总金额
		UFDouble totalAmount2 = new UFDouble();//交通费用报销总金额
		UFDouble totalAmount4 = new UFDouble();//交通费用报销总金额
		//新增时，从表体循环获取报销金额amount，得到报销总金额，然后再jkvo里比较报销金额和冲借款金额
		if(null != bxbusitem && bxbusitem.length != 0){
			for(int i = 0; i < bxbusitem.length; i++){
				if(null != bxbusitem[i].get("amount") && !"".equals(bxbusitem[i].get("amount"))){
					UFDouble totalAmount1 = new UFDouble(bxbusitem[i].get("amount").toString());
					totalAmount2 = totalAmount2.add(totalAmount1);
				}
			}
		}
		if(null != other && other.length != 0){
			for(int i = 0; i < other.length; i++){
				if(null!=other[i].get("amount") && !"".equals(other[i].get("amount"))){
					UFDouble totalAmount3 = new UFDouble(other[i].get("amount").toString());
					totalAmount4 = totalAmount4.add(totalAmount3);
				}
			}
		}
		//保存方法需要用到的全局变量
		totalAmount = totalAmount2.add(totalAmount4); 

		UFDouble amount1 = new UFDouble();
		UFDouble amount2 = new UFDouble();
		UFDouble amount = new UFDouble();

		UFDouble cjkybje = new UFDouble();//冲销原币金额

		String Jkdjbh = "";
		String rowno = "";
		String szxmid = ""; 
		String pk_jkbxString = "";

		//list集合
		ArrayList<BXBusItemVO> busiitemList = new ArrayList<BXBusItemVO>();//交通费用
		ArrayList<BxcontrastVO> contrastList = new ArrayList<BxcontrastVO>();//冲销明细
		ArrayList<JKHeaderVO> jkvoList = new ArrayList<JKHeaderVO>();//冲销表头
		StringBuffer bxBz = new StringBuffer();//表头报销标准所用变量


		//UFDouble hvoTotal = new UFDouble(0);
		//如果是修改的情况下，获取表头total的值hvoTotal，也就是修改之前的总金额
		UFDouble hvoTotal = new UFDouble(0);
		UFDouble hcxTotal = new UFDouble(0);
		if(null != head.get("PK") && !"".equals(head.get("PK")) && null != hvo){
			hvoTotal = hvo.getTotal();
			hcxTotal = hvo.getCjkbbje();
		}
 
		//数据库交通费用
		ArrayList<BXBusItemVO> dbBusiitemsDistinct = new ArrayList<BXBusItemVO>();
		if(null!=dbBusiitems){
			dbBusiitemsDistinct = new ArrayList<BXBusItemVO>(Arrays.asList(dbBusiitems));
		}
		
		
		/**
		 * 2.交通费用 
		 */
		if(null != bxbusitem && bxbusitem.length != 0){//不管修改不修改，传过来的子表不为空就说明前台传过来值了
			HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
			for (int i = 0; i < bxbusitem.length; i++) {

				BXBusItemVO cvoo = new BXBusItemVO();
				//行号用于表体显示
				rowno = (i+1)+"0";
				cvoo.setRowno(Integer.parseInt(rowno));

				if (null != head.get("PK") && !"".equals(head.get("PK"))) {//
					//UFDouble hvoTotal = new UFDouble();
					// 1.删除状态
					if (null != bxbusitem[i].get("vostatus") && !"".equals(bxbusitem[i].get("vostatus")) && "3".equals(bxbusitem[i].get("vostatus").toString())) {
						UFDouble Amount3 = new UFDouble(bxbusitem[i].get("amount").toString());
						// 原始的主表total减去前台页面获取的子表amount金额
						hvoTotal = hvo.getBbje().sub(Amount3);

						String kpString = bxbusitem[i].get("pk_jkbx").toString();
						cvoo.setPk_jkbx(kpString);
						String bu = bxbusitem[i].get("pk_busitem").toString();
						cvoo.setPk_busitem(bu);
						cvoo.setStatus(3);
					}

					// 新增状态
					if (null != bxbusitem[i].get("vostatus") && !"".equals(bxbusitem[i].get("vostatus")) && "2".equals(bxbusitem[i].get("vostatus").toString())) {
						UFDouble Amount2 = new UFDouble(bxbusitem[i].get("amount").toString());
						hvoTotal = hvo.getBbje().add(Amount2);
						cvoo.setStatus(2);
					}

					// 更新状态
					if (null != bxbusitem[i].get("vostatus") && !"".equals(bxbusitem[i].get("vostatus")) && "1".equals(bxbusitem[i].get("vostatus").toString())) {

						if(null!=bxbusitem[i].get("pk_busitem") && !"".equals(bxbusitem[i].get("pk_busitem"))){
							String bu = bxbusitem[i].get("pk_busitem").toString();//获取行标签并赋值            去掉了b.pk_jkbx = '" + kpString + "' and
							cvoo.setPk_busitem(bu);//设置行标识值
							String sqlb = "select * from er_busitem where isnull(dr,0)=0 and pk_busitem = '"+bu+"'and tablecode = 'arap_bxbusitem';";
							ArrayList<BXBusItemVO> list = (ArrayList<BXBusItemVO>) dao.executeQuery(sqlb, new BeanListProcessor(BXBusItemVO.class));

							UFDouble amount3 = new UFDouble(0);
							//得到被修改的当前金额
							amount3 = list.get(0).getAmount();
							// 获取当前页签的金额
							UFDouble bxbusitemAm = new UFDouble(bxbusitem[i].get("amount").toString());
							hvoTotal = hvo.getBbje().sub(amount3).add(bxbusitemAm);
						}
						String kpString = hvo.getPk_jkbx();
						cvoo.setPk_jkbx(kpString);//设置主键值
						cvoo.setStatus(1);
					}
					//在修改子表的时候循环处理修改的表头金额
					hvo.setTotal(hvoTotal);
					hvo.setBbje(hvoTotal);
					hvo.setYbje(hvoTotal);
				}
				amount = new UFDouble(bxbusitem[i].get("amount").toString());//获取当前传过来的值表体金额
				//amount2是从表体累加得到的arap_bxbusitem 页签合计 金额
				amount2 = amount2.add(amount);
				 
				//差旅费报销总额
				cvoo.setAmount(amount);
				cvoo.setBbje(amount);
				cvoo.setYbje(amount);
				cvoo.setCjkbbje(bbje);
				cvoo.setCjkybje(bbje);
 
				
				/**
				 * 2.封装第1个子表
				 */
				setBusiitemBVO(hvo,head,cvoo,bxbusitem[i]);
  
				cvoo.setTablecode("arap_bxbusitem");
		        busiitemList.add(cvoo);
		        
		        /*//一张发票不能多次报销
		        if(booleanFP.booleanValue()){
		        	if((null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm)))){
		        		if(hashMap.containsKey(bxbusitem[i].get(fphm))){
		        			String strWhere = " nvl(dr,0) = 0 and pk_fpgl  = '"+bxbusitem[i].get(fphm)+"'";
		        			ArrayList<SuperVO> headlist = (ArrayList<SuperVO>) dao.retrieveByClause(nc.vo.erm.fppj.FpglVO.class, strWhere);
		        			if(null != headlist && headlist.size() > 0){
		        				throw new BusinessException("发票号不能重复:"+(null!=bxbusitem[i].get(fphm)?headlist.get(0).getAttributeValue("fphm"):""));
		        			}
		        		}else{
		        			if((null != bxbusitem[i].get(fphm) && !"".equals(bxbusitem[i].get(fphm)))){
		        				hashMap.put(bxbusitem[i].get(fphm).toString(), 1);
		        			}
		        		}
		        	}
		        }*/
		           
				//去除数据库中子表vo
				for(int dbb=0; dbb<dbBusiitemsDistinct.size(); dbb++){
					if(null!=bxbusitem[i].get("pk_busitem") && bxbusitem[i].get("pk_busitem").toString().equals(dbBusiitemsDistinct.get(dbb).getPk_busitem())){
						dbBusiitemsDistinct.remove(dbb);
					}
				} 
			}
		}

		
		
		/**
		 * 其他费用
		 */ 
		if(null != other && other.length!=0){
			for (int i = 0; i < other.length; i++) {

				BXBusItemVO cvo = new BXBusItemVO();
				//行号用于表体显示
				rowno = (i+1)+"0";
				cvo.setRowno(Integer.parseInt(rowno));//行号
				if (null != head.get("PK") && !"".equals(head.get("PK"))) {

					// 1.删除状态
					if (null != other[i].get("vostatus") && !"".equals(other[i].get("vostatus")) && "3".equals(other[i].get("vostatus").toString())) {

						//获取当前删除页签的金额    然后总金额   减等于
						UFDouble busitemAmount = new UFDouble(other[i].get("amount").toString());
						// 原始的主表total减去前台页面获取的子表amount金额
						hvoTotal = hvo.getBbje().sub(busitemAmount);

						String kpString = other[i].get("pk_jkbx").toString();
						cvo.setPk_jkbx(kpString); 
						String bu = other[i].get("pk_busitem").toString();
						cvo.setPk_busitem(bu);
						cvo.setStatus(3);
					}

					// 2. 新增状态
					if (null != other[i].get("vostatus") && !"".equals(other[i].get("vostatus")) && "2".equals(other[i].get("vostatus").toString())) {
						UFDouble Amount2 = new UFDouble(other[i].get("amount").toString());
						hvoTotal = hvo.getBbje().add(Amount2);
						cvo.setStatus(2);
					}

					// 3.更新状态
					if (null != other[i].get("vostatus") && !"".equals(other[i].get("vostatus")) && "1".equals(other[i].get("vostatus").toString())) {

						// 获取当前页签要更新的金额
						UFDouble Amount1 = new UFDouble(other[i].get("amount").toString());
						// 通过当前被操作的行标识  bu             获取当前被操作的 之前的金额   amount3
						String bu = other[i].get("pk_busitem").toString();
						cvo.setPk_busitem(bu);
						String sqlb = "select * from er_busitem b join er_bxzb a on b.pk_jkbx = a.pk_jkbx where  isnull(b.dr,0)=0 and b.pk_busitem = '"+bu+"'and b.tablecode = 'other';";
						ArrayList<BXBusItemVO> list1 = (ArrayList<BXBusItemVO>) dao.executeQuery(sqlb, new BeanListProcessor(BXBusItemVO.class));
						UFDouble amount3 = new UFDouble(0);
						//得到被修改的当前金额
						amount3 = list1.get(0).getAmount();
						// 原始的主表total加上页签传过来的金额再减去数据库查出来的amount金额
						hvoTotal = hvo.getBbje().add(Amount1).sub(amount3);
						String kpString = other[i].get("pk_jkbx").toString();
						cvo.setPk_jkbx(kpString);
						cvo.setStatus(1);//给状态赋值

					}
					//在修改子表的时候循环处理修改的表头金额
					hvo.setTotal(hvoTotal);
					hvo.setBbje(hvoTotal);
					hvo.setYbje(hvoTotal);
					//报销本币  > 冲销本币
					if(hvoTotal.compareTo(hcxTotal) == 1){
						hvo.setZfbbje(hvoTotal.sub(hcxTotal));//支付本币
						hvo.setZfybje(hvoTotal.sub(hcxTotal));//支付原币
						hvo.setHkbbje(new UFDouble(0));// 还款本币
						hvo.setHkybje(new UFDouble(0));// 还款原币
					}else if(hvoTotal.compareTo(hcxTotal) == -1){//报销本币  < 冲销本币
						hvo.setZfbbje(new UFDouble(0));//支付本币
						hvo.setZfybje(new UFDouble(0));//支付原币
						hvo.setHkbbje(hcxTotal.sub(hvoTotal));// 还款本币
						hvo.setHkybje(hcxTotal.sub(hvoTotal));// 还款原币
					}else{//报销本币  == 冲销本币
						hvo.setZfbbje(new UFDouble(0));//支付本币
						hvo.setZfybje(new UFDouble(0));//支付原币
						hvo.setHkbbje(new UFDouble(0));// 还款本币
						hvo.setHkybje(new UFDouble(0));// 还款原币
					}

				}
				amount = new UFDouble(other[i].get("amount").toString());
				//amount1是从表体累加得到的other 页签合计 金额
				amount1 = amount1.add(amount);
				//新增
				cvo.setAmount(amount);
				cvo.setBbje(amount);
				cvo.setYbje(amount);
				cvo.setCjkbbje(bbje);//冲借款本币金额
				cvo.setCjkybje(bbje);//冲借款原币金额

				/**
				 * 2.封装第2个页签
				 */
				setBusiitemBVO(hvo,head,cvo,other[i]);

				cvo.setTablecode("other");

				busiitemList.add(cvo);

				//去除数据库中子表vo
				for(int dbb=0; dbb<dbBusiitemsDistinct.size(); dbb++){
					if(null!=other[i].get("PK") && other[i].get("PK").toString().equals(dbBusiitemsDistinct.get(dbb).getPk_busitem())){
						dbBusiitemsDistinct.remove(dbb);
					}
				}

			}
		}

		//将数据库中费用明细加入数组中
		busiitemList.addAll(dbBusiitemsDistinct);

		/**
		 *  2.冲销明细
		 */
		ArrayList<BxcontrastVO> dbContrastsdistinct = new ArrayList<BxcontrastVO>();
		ArrayList<BxcontrastVO> btList = new ArrayList<BxcontrastVO>();
		if(null!=dbContrasts && dbContrasts.length>0){
			dbContrastsdistinct = new ArrayList<BxcontrastVO>(Arrays.asList(dbContrasts));
		}
		//冲借款金额合计
		UFDouble cjkybjeS = new UFDouble();
		for(int i=0; null!=dbContrasts && i<dbContrasts.length; i++){
			if(null!=dbContrasts[i].getCjkybje() && !"".equals(dbContrasts[i].getCjkybje())){
				cjkybje = new UFDouble(dbContrasts[i].getCjkybje().toString());
				//冲借款金额合计
				cjkybjeS = cjkybjeS.add(cjkybje);
			}
		}


		if (null != bxcontrasts && bxcontrasts.length != 0) {
			for (int i = 0; i < bxcontrasts.length; i++) {
				rowno = (i + 1) + "0";
				BxcontrastVO bvo = new BxcontrastVO();

				// 删除
				if (null != bxcontrasts[i].get("vostatus") && "3".equals(bxcontrasts[i].get("vostatus").toString())) {
					for (int db = 0; db < dbContrastsdistinct.size(); db++) {
						if (dbContrastsdistinct.get(db).getPk_busitem().equals(bxcontrasts[i].get("pk_bxcontrast").toString())) {
							bvo = dbContrastsdistinct.get(db);
							bvo.setStatus(VOStatus.DELETED);//删除
							continue;
						}
					}
					bvo.setStatus(3);
					if (null != bxcontrasts[i].get("cjkybje") && !"".equals(bxcontrasts[i].get("cjkybje"))) {
						cjkybje = new UFDouble(bxcontrasts[i].get("cjkybje").toString());
						// 冲借款金额合计
						cjkybjeS = cjkybjeS.sub(cjkybje);
					}
				}
				//更新
				if (null != bxcontrasts[i].get("vostatus") && "1".equals(bxcontrasts[i].get("vostatus").toString())) {
					for (int db = 0; db < dbContrastsdistinct.size(); db++) { 
						if (dbContrastsdistinct.get(db).getPk_bxcontrast().equals(bxcontrasts[i].get("pk_bxcontrast").toString())) {
							bvo = dbContrastsdistinct.get(db);
							bvo.setStatus(1);
							if(null!=bxcontrasts[i].get("cjkybje") && !"".equals(bxcontrasts[i].get("cjkybje"))){
								cjkybje = new UFDouble(bxcontrasts[i].get("cjkybje").toString());
								//冲借款金额合计
								cjkybjeS = cjkybjeS.sub(dbContrastsdistinct.get(db).getCjkybje());//减去- 数据库原数据
								cjkybjeS = cjkybjeS.add(cjkybje);//增加+ 界面数据
							}
							//去除数据库中子表vo
							dbContrastsdistinct.remove(db); 
						}   
					}
				}
				//新增
				if (null != bxcontrasts[i].get("vostatus") && "2".equals(bxcontrasts[i].get("vostatus").toString())) {
					bvo.setStatus(2);
					if(null!=bxcontrasts[i].get("cjkybje") && !"".equals(bxcontrasts[i].get("cjkybje"))){
						cjkybje = new UFDouble(bxcontrasts[i].get("cjkybje").toString());
						//冲借款金额合计
						cjkybjeS = cjkybjeS.add(cjkybje);
					} 
				}

				if(null!=bxcontrasts[i].get("cjkybje") && !"".equals(bxcontrasts[i].get("cjkybje"))){
					cjkybje = new UFDouble(bxcontrasts[i].get("cjkybje").toString());
					bvo.setCjkybje(cjkybje);
					bvo.setYbje(cjkybje);
					//					bvo.setFyybje(cjkybje);
					bvo.setBbje(cjkybje);
					bvo.setCjkbbje(cjkybje);
				}
				bvo.setPk_payorg(pk_org);
				bvo.setPk_org(pk_org);
				if (null != head.get("PK") && !"".equals(head.get("PK"))){
					bvo.setPk_bxd(head.get("PK").toString());
				}

				// 借款单号
				if(null!=bxcontrasts[i].get("jkdjbh_name") && !"".equals(bxcontrasts[i].get("jkdjbh_name"))){
					Jkdjbh = bxcontrasts[i].get("jkdjbh_name").toString();
				}else if(null != bxcontrasts[i].get("jkdjbh") && !"".equals(bxcontrasts[i].get("jkdjbh"))){ 
					Jkdjbh = bxcontrasts[i].get("jkdjbh").toString();
				}else{
					throw new BusinessException("借款单据号不能空!");
				}
				// 借款子表pk
				String pk_busitem = "";
				if(null!=bxcontrasts[i].get("pk_busitem") && !"".equals(bxcontrasts[i].get("pk_busitem"))){
					pk_busitem = bxcontrasts[i].get("pk_busitem").toString();
					bvo.setPk_busitem(pk_busitem);
				}
				//后加
				if (null != bxcontrasts[i].get("pk_bxcontrast") && !"".equals(bxcontrasts[i].get("pk_bxcontrast"))) {
					String pk_bxcontrast = bxcontrasts[i].get("pk_bxcontrast").toString();
					bvo.setPk_bxcontrast(pk_bxcontrast);
				}
				bvo.setJkdjbh(Jkdjbh);//借款单号
				bvo.setBxdjbh(hvo.getDjbh());//报销单号

				//借款单主表
				String jkzb_sql = "select * from er_jkzb where djbh = '" + Jkdjbh + "' and isnull(dr,0)=0 ";
				ArrayList<JKHeaderVO> jkzbList = (ArrayList<JKHeaderVO>) bx.executeQuery(jkzb_sql, new BeanListProcessor(JKHeaderVO.class));
				if(jkzbList.size()>0){
					bvo.setPk_jkd(jkzbList.get(0).getPk_jkbx());
					bvo.setDjlxbm(jkzbList.get(0).getDjlxbm());
				}

				//借款单子表，选中的子表
				String busitem_sql = "select * from er_busitem where pk_busitem = '"+pk_busitem + "' and isnull(dr,0)=0 ";
				ArrayList<BXBusItemVO> busitemList = (ArrayList<BXBusItemVO>) bx.executeQuery(busitem_sql, new BeanListProcessor(BXBusItemVO.class));
				if(busitemList.size()>0){
					//费用行金额
					if(null!=bxcontrasts[i].get("cjkybje") && busitemList.get(0).getAmount().compareTo(new UFDouble(bxcontrasts[i].get("cjkybje").toString()))>0){
						bvo.setFyybje(new UFDouble(bxcontrasts[i].get("cjkybje").toString()));
					}else{
						bvo.setFyybje(busitemList.get(0).getAmount());
					}
					//还款金额
					if(bvo.getCjkbbje().compareTo(busitemList.get(0).getAmount())>0){
						bvo.setHkybje(bvo.getCjkbbje().sub(busitemList.get(0).getAmount()));
					}
				}

				// 借款人
				if(null!=bxcontrasts[i].get("jkbxr") && !"".equals(bxcontrasts[i].get("jkbxr"))){
					bvo.setJkbxr(bxcontrasts[i].get("jkbxr").toString());
				}
				// 收支项目 szxmid
				if (null != bxcontrasts[i].get("szxmid") && !"".equals(bxcontrasts[i].get("szxmid"))) {
					bvo.setSzxmid(bxcontrasts[i].get("szxmid").toString());
				}
				// 借款部门 deptid
				bvo.setDeptid(pk_dept);
				bvo.setHkybje(new UFDouble(0));
				// 全局本币金额
				bvo.setGlobalbbje(new UFDouble(0));
				bvo.setGlobalcjkbbje(new UFDouble(0));
				bvo.setGroupbbje(new UFDouble(0));
				bvo.setGroupcjkbbje(new UFDouble(0));
				bvo.setSxbz(0);
				//				bvo.setDjlxbm(head.get("pk_tradetypeid_code").toString());
				bvo.setSelected(new UFBoolean(false));
				bvo.setSxbzmc("未生效");
				// 冲销日期
				bvo.setCxrq((new UFDate(new Date())));

				contrastList.add(bvo);



				/**
				 * 冲借款JKHeadVO
				 */
				jkvos.setBzbm(head.get("bzbm").toString());
				// 收支项目
				jkvos.setSzxmid(szxmid);
				jkvos.setPk_jkbx(pk_jkbxString);
				//				jkvos.setDjbh(djbh.get(0).getDjbh());

				jkvos.setDeptid(pk_dept);
				jkvos.setJkbxr(hvo.getJkbxr());
				jkvos.setDjbh(Jkdjbh);


				if(jkzbList.size()>0){
					jkvos.setPk_jkbx(jkzbList.get(0).getPk_jkbx());
					jkvos.setYbje(jkzbList.get(0).getYbje());
					jkvos.setYbye(jkzbList.get(0).getYbye());
					jkvos.setYjye(jkzbList.get(0).getYjye());
					jkvos.setDjrq(jkzbList.get(0).getDjrq());
					jkvos.setTs(jkzbList.get(0).getTs());
					jkvos.setDjlxbm(jkzbList.get(0).getDjlxbm()); 
				}

				// 收支项目 szxmid
				if (null != bxcontrasts[i].get("szxmid") && !"".equals(bxcontrasts[i].get("szxmid"))) {
					jkvos.setSzxmid(bxcontrasts[i].get("szxmid").toString());
				}


				jkvos.setOperator(userid);
				jkvos.setPk_org(pk_org);
				jkvos.setPk_org_v(orgMap.get(pk_org));
				jkvos.setPk_group(pk_group);
				jkvos.setPk_payorg(pk_org);
				jkvos.setPk_payorg_v(orgMap.get(pk_org));
				jkvos.setZfbbje(new UFDouble(0));
				jkvos.setZfybje(new UFDouble(0));
				jkvos.setQcbz(new UFBoolean(false));
				jkvos.setIsexpedited(new UFBoolean(false));
				jkvos.setIsinitgroup(new UFBoolean(false));
				jkvos.setIsneedimag(new UFBoolean(false));
				jkvos.setQzzt(0);
				jkvos.setSpzt(-1);

				jkvoList.add(jkvos);
			}

			//是否冲借设置****************
			aggvo.setContrastUpdate(true);
		}

		btList.addAll(contrastList);
		//将数据库中冲销 加入数组中
		if(null!=dbContrasts && dbContrasts.length>0){
			for(int i=0; i<dbContrasts.length; i++){
				dbContrasts[i].setStatus(1);
				dbContrasts[i].setTs(null);
			}
			contrastList.addAll(dbContrastsdistinct);
		}
		//后加 单独进行删除操作
		ArrayList<BxcontrastVO> contrastListEnd = new ArrayList<BxcontrastVO>();
		HashMap<String, BxcontrastVO> constrMap = new HashMap<String, BxcontrastVO>();
		ArrayList<BxcontrastVO> scList =  new ArrayList<BxcontrastVO>(new HashSet<BxcontrastVO>(contrastList));
		if(null != bxcontrasts){
			for (int c = 0; c < bxcontrasts.length; c++) {
				if(null != bxcontrasts[c].get("vostatus") && "3".equals(bxcontrasts[c].get("vostatus").toString())){
					for(int d = 0; d < btList.size(); d++){
						if(btList.get(d).getStatus() == 3){
							if(scList.contains(btList.get(d))){
								scList.remove(btList.get(d));
							}
						}
					}
				}
				contrastList.clear();
				//清空Map
				constrMap.clear();
				contrastListEnd.clear();
				contrastListEnd.addAll(scList);
			}
		}
		/**
		 * 1. 冲销明细多行相同借款子表 1，进行合并
		 */
		for(int c=0; c<contrastList.size(); c++){
			if(contrastList.get(c).getStatus() != 0 && contrastList.get(c).getStatus() !=3 && constrMap.containsKey(contrastList.get(c).getPk_busitem())){
				BxcontrastVO cvo = constrMap.get(contrastList.get(c).getPk_busitem());
				UFDouble cjkybjeMap = null!=cvo.getCjkybje()?cvo.getCjkybje():new UFDouble(0);
				cjkybje = cjkybjeMap.add(null!=contrastList.get(c).getCjkybje()?contrastList.get(c).getCjkybje():new UFDouble(0));
				cvo.setCjkybje(cjkybje);
				UFDouble cjkbbjeMap = null!=cvo.getCjkbbje()?cvo.getCjkbbje():new UFDouble(0);
				UFDouble cjkbbje = cjkbbjeMap.add(null!=contrastList.get(c).getCjkbbje()?contrastList.get(c).getCjkbbje():new UFDouble(0));
				cvo.setCjkbbje(cjkbbje);

				UFDouble ybjeMap = null!=cvo.getYbje()?cvo.getYbje():new UFDouble(0);
				UFDouble ybje = ybjeMap.add(null!=contrastList.get(c).getYbje()?contrastList.get(c).getYbje():new UFDouble(0));
				cvo.setYbje(ybje);
				UFDouble bbje2Map = null!=cvo.getBbje()?cvo.getBbje():new UFDouble(0);
				UFDouble bbje2 = bbje2Map.add(null!=contrastList.get(c).getBbje()?contrastList.get(c).getBbje():new UFDouble(0));
				cvo.setBbje(bbje2);

				UFDouble fyybjeMap = null!=cvo.getFyybje()?cvo.getFyybje():new UFDouble(0);
				UFDouble fyybje = fyybjeMap.add(null!=contrastList.get(c).getFyybje()?contrastList.get(c).getFyybje():new UFDouble(0));
				cvo.setFyybje(fyybje);
				UFDouble fybbjeMap = null!=cvo.getFybbje()?cvo.getFybbje():new UFDouble(0);
				UFDouble fybbje = fybbjeMap.add(null!=contrastList.get(c).getFybbje()?contrastList.get(c).getFybbje():new UFDouble(0));
				cvo.setFybbje(fybbje);

				UFDouble hkybjeMap = null!=cvo.getHkybje()?cvo.getHkybje():new UFDouble(0);
				UFDouble hkybje = hkybjeMap.add(null!=contrastList.get(c).getHkybje()?contrastList.get(c).getHkybje():new UFDouble(0));
				cvo.setHkybje(hkybje);
				UFDouble hkbbjeMap = null!=cvo.getHkbbje()?cvo.getHkbbje():new UFDouble(0);
				UFDouble hkbbje = hkbbjeMap.add(null!=contrastList.get(c).getHkbbje()?contrastList.get(c).getHkbbje():new UFDouble(0));
				cvo.setHkbbje(hkbbje);

				constrMap.put(contrastList.get(c).getPk_busitem(), cvo);

			}else{
				constrMap.put(contrastList.get(c).getPk_busitem(), contrastList.get(c));
			}
		}
		for(Map.Entry<String,BxcontrastVO> map: constrMap.entrySet()){
			contrastListEnd.add(map.getValue());
		}

		/**
		 * 数据库中的已冲借的子表特殊处理 转换封装JKHeaderVO
		 */
		for(int i=0; null!=dbContrastsdistinct && i<dbContrastsdistinct.size(); i++){
			JKHeaderVO dbcons = new JKHeaderVO();

			dbcons.setBzbm(head.get("bzbm").toString());
			// 收支项目
			dbcons.setSzxmid(dbContrastsdistinct.get(i).getSzxmid());
			dbcons.setPk_jkbx(dbContrastsdistinct.get(i).getPk_jkd());
			dbcons.setDjlxbm(dbContrastsdistinct.get(i).getDjlxbm());// "2631"
			dbcons.setDeptid(dbContrastsdistinct.get(i).getDeptid());
			dbcons.setJkbxr(dbContrastsdistinct.get(i).getJkbxr());
			dbcons.setDjbh(dbContrastsdistinct.get(i).getJkdjbh());

			//借款单主表
			String jkzb_sql = "select * from er_jkzb where djbh = '" + dbContrastsdistinct.get(i).getJkdjbh() + "' and isnull(dr,0)=0 ";
			ArrayList<JKHeaderVO> jkzbList = (ArrayList<JKHeaderVO>) bx.executeQuery(jkzb_sql, new BeanListProcessor(JKHeaderVO.class));
			if(jkzbList.size()>0){
				dbcons.setYbje(jkzbList.get(0).getYbje());
				dbcons.setYbye(jkzbList.get(0).getYbye());
				dbcons.setYjye(jkzbList.get(0).getYjye());
				dbcons.setDjrq(jkzbList.get(0).getDjrq());
				dbcons.setTs(jkzbList.get(0).getTs());
			}

			dbcons.setOperator(userid);
			dbcons.setPk_org(pk_org);
			dbcons.setPk_payorg(pk_org);
			dbcons.setZfbbje(new UFDouble(0));
			dbcons.setZfybje(new UFDouble(0));
			dbcons.setQcbz(new UFBoolean(false));
			dbcons.setIsexpedited(new UFBoolean(false));
			dbcons.setIsinitgroup(new UFBoolean(false));
			dbcons.setIsneedimag(new UFBoolean(false));
			dbcons.setQzzt(0);
			dbcons.setSpzt(-1);

			jkvoList.add(dbcons);
		}



		/**
		 * 子表  冲借金额赋值
		 */
		UFDouble currentCjkybje = cjkybjeS;
		//把数据库中和前台传过来的子表循环赋值
		for(int b=0; b<busiitemList.size(); b++){
			//删除的子表
			if(busiitemList.get(b).getStatus()==3){
				busiitemList.get(b).setCjkbbje(null);//冲借款本币金额
				busiitemList.get(b).setCjkybje(null);//冲借款金额
				busiitemList.get(b).setZfbbje(null);//支付本币金额
				busiitemList.get(b).setZfybje(null);//支付金额
				busiitemList.get(b).setHkbbje(null); //还款本币金额 
				busiitemList.get(b).setHkybje(null); //还款原币金额
				continue;//跳过本轮循环，下面代码不执行
			} 
			if(currentCjkybje.compareTo(new UFDouble(0))>=0 
					&& currentCjkybje.compareTo(busiitemList.get(b).getAmount())<=0){
				busiitemList.get(b).setCjkbbje(currentCjkybje);//冲借款本币金额
				busiitemList.get(b).setCjkybje(currentCjkybje);//冲借款金额

				if(busiitemList.get(b).getYbje().compareTo(currentCjkybje)>=0){
					busiitemList.get(b).setZfbbje(busiitemList.get(b).getYbje().sub(currentCjkybje));//支付本币金额
					busiitemList.get(b).setZfybje(busiitemList.get(b).getYbje().sub(currentCjkybje));//支付金额
				}

				if(currentCjkybje.compareTo(busiitemList.get(b).getAmount())>=0){
					busiitemList.get(b).setHkbbje(currentCjkybje.sub(busiitemList.get(b).getAmount())); //还款本币金额 
					busiitemList.get(b).setHkybje(currentCjkybje.sub(busiitemList.get(b).getAmount())); //还款原币金额
				}
			}
			else if(currentCjkybje.compareTo(new UFDouble(0))>0 
					&& currentCjkybje.compareTo(busiitemList.get(b).getAmount())>=0){
				busiitemList.get(b).setCjkbbje(busiitemList.get(b).getAmount());//冲借款本币金额
				busiitemList.get(b).setCjkybje(busiitemList.get(b).getAmount());//冲借款金额

				if(busiitemList.get(b).getYbje().compareTo(busiitemList.get(b).getAmount())>=0){ 
					busiitemList.get(b).setZfbbje(busiitemList.get(b).getYbje().sub(busiitemList.get(b).getAmount()));//支付本币金额
					busiitemList.get(b).setZfybje(busiitemList.get(b).getYbje().sub(busiitemList.get(b).getAmount()));//支付金额
				}

				if(busiitemList.get(b).getAmount().compareTo(busiitemList.get(b).getAmount())>=0){
					busiitemList.get(b).setHkbbje(busiitemList.get(b).getAmount().sub(busiitemList.get(b).getAmount())); //还款本币金额 
					busiitemList.get(b).setHkybje(busiitemList.get(b).getAmount().sub(busiitemList.get(b).getAmount())); //还款原币金额
				}
			}
			//如果是最后一条，特殊处理
			if(currentCjkybje.compareTo(new UFDouble(0))>=0 && (b+1)==busiitemList.size()){
				busiitemList.get(b).setCjkbbje(currentCjkybje);//冲借款本币金额
				busiitemList.get(b).setCjkybje(currentCjkybje);//冲借款金额

				if(busiitemList.get(b).getYbje().compareTo(currentCjkybje)>=0){
					busiitemList.get(b).setZfbbje(busiitemList.get(b).getYbje().sub(currentCjkybje));//支付本币金额
					busiitemList.get(b).setZfybje(busiitemList.get(b).getYbje().sub(currentCjkybje));//支付金额
				}

				if(currentCjkybje.compareTo(busiitemList.get(b).getAmount())>=0){
					busiitemList.get(b).setHkbbje(currentCjkybje.sub(busiitemList.get(b).getAmount())); //还款本币金额 
					busiitemList.get(b).setHkybje(currentCjkybje.sub(busiitemList.get(b).getAmount())); //还款原币金额
				}
			}
			//冲完的要做法
			currentCjkybje = currentCjkybje.sub(busiitemList.get(b).getCjkbbje());
		}

		//将报销标准在保存之前设置到表头中，在循环里处理业务，循环外set值
		if(null != bxBz && !"".equals(bxBz)){
			hvo.setReimrule(bxBz.toString());
		}

		UFDouble total = amount1.add(amount2);//设置表头总金额

		//表头的金额处理
		hvo.setCjkbbje(cjkybjeS);
		hvo.setCjkybje(cjkybjeS);
		if(null!=hvo.getYbje() && !"".equals(hvo.getYbje())){

			if(cjkybjeS.compareTo(hvo.getBbje())>0){
				hvo.setHkbbje(cjkybjeS.sub(hvo.getBbje()));//还款本币金额
				hvo.setHkybje(cjkybjeS.sub(hvo.getBbje()));//还款金额
			}
			if(hvo.getHkbbje().compareTo(new UFDouble(0))==0 && cjkybjeS.compareTo(hvo.getYbje())<=0){
				hvo.setZfbbje(hvo.getYbje().sub(cjkybjeS));//支付本币金额
				hvo.setZfybje(hvo.getYbje().sub(cjkybjeS));//支付金额
			}
		}

//		hvo.setPk_billtype(null);

		//判断主表如果是新增的情况，处理新增的表头金额问题
		if (null != head.get("vostatus") && !"".equals(head.get("vostatus")) && "2".equals(head.get("vostatus").toString())) {
			hvo.setTotal(total);// 表头总金额1
			hvo.setBbje(total);// 报销本币
			hvo.setYbje(total);// 报销原币
		}

		
		
		/**
		 * 分摊
		 */
		if(null!=cshare && cshare.length>0 && null!=head.get("iscostshare")){
			if(!"Y".equals(head.get("iscostshare").toString())){
				throw new BusinessException("有分摊明细,表头分摊勾选不能空");
			}
		}
		Boolean ishas = false;
		UFDouble ratio = new UFDouble(0);//分摊比例
		UFDouble sharemount = new UFDouble(0);//分摊金额
		ArrayList<CShareDetailVO> cshareList = new ArrayList<CShareDetailVO>();
		
		if(null!=head.get("iscostshare") && !"".equals(head.get("iscostshare"))){
			UFBoolean iscostshare = new UFBoolean(head.get("iscostshare").toString());
			if(null!=cshare && cshare.length>0 && iscostshare.booleanValue()){ 
				
				for(int i=0; i<cshare.length; i++){
					CShareDetailVO csvo = new CShareDetailVO();
//					if(null!=cshare[i].get("assume_amount") && !"".equals(cshare[i].get("assume_amount"))){
//						UFDouble assume_amount = new UFDouble(cshare[i].get("assume_amount").toString());
//						csvo.setAssume_amount(assume_amount);
//						csvo.setBbje(assume_amount);
//						if(null!=cshare[i].get("vostatus") && 3!=Integer.parseInt(cshare[i].get("vostatus").toString())){
//							ishas = true;
//							sharemount = sharemount.add(assume_amount);
//						}
//					}
					
					if(null!=cshare[i].get("share_ratio") && !"".equals(cshare[i].get("share_ratio"))){
						UFDouble share_ratio = new UFDouble(cshare[i].get("share_ratio").toString());
						csvo.setShare_ratio(share_ratio);
						if(null!=cshare[i].get("vostatus") && 3!=Integer.parseInt(cshare[i].get("vostatus").toString())){
							ishas = true;
							ratio = ratio.add(share_ratio);
							//根据总金额和分摊比例，重新计算 承担金额
							UFDouble share_ratio1 = new UFDouble(cshare[i].get("share_ratio").toString()).div(100);
							UFDouble assume_amount = hvo.getTotal().multiply(share_ratio1);
							csvo.setAssume_amount(assume_amount);
							csvo.setBbje(assume_amount);
							sharemount = sharemount.add(assume_amount);
						}
					}
					if(null!=cshare[i].get("vostatus") && !"".equals(cshare[i].get("vostatus"))){
						csvo.setStatus(Integer.parseInt(cshare[i].get("vostatus").toString()));
					}
					/**
					 * 封装分摊子表
					 */
					setShareVO(csvo,cshare[i],hvo);
					cshareList.add(csvo); 
				} 
				if(ratio.compareTo(new UFDouble(100))!=0 && ishas){
					throw new BusinessException("分摊明细的分摊比例不等于100");
				}
				if(sharemount.compareTo(hvo.getTotal())!=0 && ishas){
					throw new BusinessException("分摊明细的承担金额不等于报销总金额");
				} 
			} 
		}
		
		
		
		HashMap eParam = new HashMap();
		if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
			eParam.put("notechecked", "notechecked");
			HashMap<String, UFBoolean> map = new HashMap<String, UFBoolean>();
			map.put("ATPCheck", new UFBoolean(false));
			eParam.put("SCMResumeExceptionResult", map);
		}

		//封装聚合aggvo
		aggvo.setParentVO(hvo);// 将表头设置到聚合VO中
		aggvo.setChildrenVO(busiitemList.toArray(new BXBusItemVO[] {}));//交通，其他费用
		aggvo.setContrastVO(contrastList.toArray(new BxcontrastVO[]{}));//冲销子表
		aggvo.setContrastVO(contrastListEnd.toArray(new BxcontrastVO[] {}));
		aggvo.setcShareDetailVo(cshareList.toArray(new CShareDetailVO[]{}));//分摊页签
		//dongl 费用申请单封装 matterAppVOfind(id)
		if(null != head.get("pk_item") && !"".equals(head.get("pk_item"))){ 
			String pk_mtapp_bill = head.get("pk_item").toString();
			MatterAppVO matterHeadVO = (MatterAppVO) new FeeBaseAction().matterAppVOfind(pk_mtapp_bill);
			if(null != matterHeadVO){
				aggvo.setMaheadvo(matterHeadVO);
			}
		}
		 
		if( null!=bxcontrasts && bxcontrasts.length>0 ){
			aggvo.setJkHeadVOs(jkvoList.toArray(new JKHeaderVO[] {}));//冲销表头
		}
		Object o = null;// 初始化超类
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		nc.itf.arap.pub.IBXBillPublic iIplatFormEntry = (nc.itf.arap.pub.IBXBillPublic) NCLocator.getInstance().lookup(nc.itf.arap.pub.IBXBillPublic.class.getName());
		// 更新还是增加进行判断
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			// 更新
			HashMap mapall = new HashMap(2);
			mapall.put("nc.bs.scmpub.pf.ORIGIN_VO_PARAMETER", new BXVO[] { cloneagg });
			mapall.put("notechecked", "notechecked");
			try {
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					aggvo.setHasNtbCheck(true);
				}  
				o = iIplatFormEntry.update(new BXVO[] { aggvo });
			} catch (Exception e) {
				if (e.getMessage().contains("预警")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		} else {
			try {
				// 新增
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					aggvo.setHasNtbCheck(true);
				}  
				o = iIplatFormEntry.save(new BXVO[] { aggvo });
			} catch (Exception e) {
				if (e.getMessage().contains("预警")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		}
		BXVO[] returnVO = (BXVO[]) o;
		if (o != null && returnVO.length > 0) {
			BXHeaderVO returnHVO = (BXHeaderVO) returnVO[0].getParentVO();
			
			/**
			 * 发票附件处理
			 */ 
			try {
				fileFP(returnVO[0],userid);
			} catch (Exception e) {
				throw new BusinessException(e.getMessage());
			}
			
			//dongl 修改发票动态替换
			if(booleanFP.booleanValue()){
				/**
				 * 保存成功后回写发票票夹状态
				 */
				BXBusItemVO[] bvos = (BXBusItemVO[]) returnVO[0].getChildrenVO();
				if(null != fphm && !"".equals(fphm)){

					fphms = new StringBuffer();
					HashMap<String, Object>[] maps = transNCVOTOMap(bvos);
					for(int i = 0;i<maps.length;i++){
						HashMap<String,Object> bmaps = maps[i];
						Iterator<Map.Entry<String, Object>> iterator = bmaps.entrySet().iterator();
						while (iterator.hasNext()) {
							Entry<String, Object> entry = iterator.next();
							if(fphm.equals(entry.getKey())){
								//这里要处理下invoiceID\
								if(null != maps[i].get(entry.getKey()) && !"".equals(maps[i].get(entry.getKey()))){
									String invoiceID = maps[i].get(entry.getKey()).toString();
									if(invoiceID.contains(",")){
										String incoiceIds = new BdocAction().getPKs(invoiceID);
										fphms.append("'").append(incoiceIds).append("',");
									}else{
										fphms.append("'").append(maps[i].get(entry.getKey())).append("',");
									}
								}
							}
						}
					}

				}

				if(fphms.length()>0){
					//更新
					String invoice  = fphms.substring(0,fphms.length()-1);
					sql = "update erm_fpgl set srcbillid='"+returnHVO.getPk_jkbx()+"',billversionpk='Y' where pk_fpgl in ("+invoice+")";
					dao.executeUpdate(sql);
					//更新报销单删除或修改的发票票夹状态
					sql = " update erm_fpgl\n" +
							" set srcbillid='',billversionpk=''\n" + 
							" where nvl(erm_fpgl.dr,0)=0\n" + 
							" and srcbillid='"+returnHVO.getPk_jkbx()+"'\n" + 
							" and pk_fpgl not in " +
							"("+invoice+")";
							//"(select "+fphm+" from er_busitem where pk_jkbx='"+returnHVO.getPk_jkbx()+"' and nvl(dr,0)=0)";
					dao.executeUpdate(sql);
				}
			}
			return queryBillVoByPK(userid, returnHVO.getPk_jkbx());
		} 
		return null;
	}
	
	/**
	 * 发票附件处理
	 */
	public void fileFP(BXVO bxvo, String userid) throws Exception{
		String funcode = super.getBilltype();
		String sql ="select distinct muap_billyq_b.itemcode\n" +
					"from muap_bill_h\n" + 
					"join muap_billyq_b on muap_billyq_b.pk_billconfig_h=muap_bill_h.pk_billconfig_h and isnull(muap_billyq_b.dr,0)=0 \n" + 
					"where vmobilebilltype='"+funcode+"'\n" + 
					"and trim(refdata)='发票号' " +
					" and isnull(muap_bill_h.dr,0)=0 ";
		ArrayList<BillyqBVO> mupqb = (ArrayList<BillyqBVO>) dao.executeQuery(sql, new BeanListProcessor(BillyqBVO.class));
		if(null!=mupqb && mupqb.size()>0){
			BXBusItemVO[] bvos = bxvo.getChildrenVO();
			String fileroot = bxvo.getParentVO().getPk_jkbx();
			
			AttachmentAction aac = new AttachmentAction();
			AttachmentVO bxavo = new AttachmentVO();
			
			/**
			 * 查询当前报销单里的附件，包含invoice的先删除
			 */
			bxavo.setFileroot(fileroot);
			Object bxfileVO = aac.processAction(account1,userid,billtype1,"QUERY", bxavo);
			AttachmentListVO bxListVO = (AttachmentListVO) bxfileVO;
			AttachmentVO[] bxrvos = bxListVO.getAttachmentVOs();
			for(int b=0; b<bxrvos.length; b++){
				if(bxrvos[b].getFilename().contains("invoice")){
					Object invoiceVO = aac.processAction(account1,userid,billtype1,IMobileAction.DEL_DELETE, bxrvos[b]);
				}
			}
			
			String cohome = System.getProperty("catalina.base");
			String dir = cohome + "/webapps/nc_web/upload/" ;
			File filedir = new File(dir);
			if (!filedir.exists()) {
				filedir.mkdirs();
			}
			
			File file = null;
			FileInputStream fileinput = null;
			
			for(int i=0; null!=bvos && i<bvos.length; i++){
				
				AttachmentVO avo = new AttachmentVO();
				
				if(null!=bvos[i].getAttributeValue(mupqb.get(0).getItemcode())){
					/**
					 * 根据发票pk，查询发票附件
					 */
					avo.setFileroot(bvos[i].getAttributeValue(mupqb.get(0).getItemcode()).toString());
					Object invoiceVO = aac.processAction(account1,userid,billtype1,"QUERY", avo);
					AttachmentListVO atListVO = (AttachmentListVO) invoiceVO;
					AttachmentVO[] rvos = atListVO.getAttachmentVOs();
					
					if(null!=rvos && rvos.length>0){
						for(int r=0; r<rvos.length; r++){
							String filename = "invoice"+i+r+".jpg";
							try {
								URL resourceUrl = new URL(rvos[r].getDownurl());
								HttpURLConnection conn = (HttpURLConnection) resourceUrl.openConnection();
								conn.setDoOutput(true);
								conn.setDoInput(true);
								conn.setRequestMethod("GET");
								conn.setUseCaches(false);
								conn.setInstanceFollowRedirects(true);
								conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
								conn.setRequestProperty("Charset", "UTF-8");
								conn.connect();
								
								file = new File(dir+ filename);
								if (!file.exists()) {
									file.createNewFile();// 能创建多级目录
								}
								
								OutputStream os = new FileOutputStream(file);
								int bytesRead = 0;
								byte[] buffer = new byte[8192];
								while ((bytesRead = conn.getInputStream().read(buffer, 0, 8192)) != -1) {
									os.write(buffer, 0, bytesRead);
								}
								 
								String dsName = InvocationInfoProxy.getInstance().getUserDataSource();
								fileinput = new FileInputStream(file);
								/**
								 * 上传发票附件
								 */
								new FileService().uploadFile(dsName, fileroot, filename, fileinput, userid, file.length() );
								os.close();
								conn.getInputStream().close();
							} catch (IOException e) {
								throw new BusinessException(e.getMessage());
							} finally {
								if (fileinput != null) {
									fileinput.close();
								}
								if (file != null && file.exists()) {
									File[] childfiles = file.getParentFile().listFiles();
									if (childfiles != null && childfiles.length > 0) {
										for (int f = 0; f < childfiles.length; f++) {
											childfiles[f].delete();
										}
									}
								}
							}
						}
					}
				}
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

//		StringBuffer str = dealCondition(obj, true);
//		String condition = str.toString().replace("pk_corp", "pk_org");
//		String condition1 = condition.toString().replace("PPK", "pk_jkbx").replace("pk_org.", "");
//		String condition2 = condition.toString().replace("PPK", "pk_bxd").replace("pk_org.", "");
//
//		BaseDAO dao = new BaseDAO();
//		QueryBillVO qbillVO = new QueryBillVO();
//		BillVO[] billVOs = new BillVO[5];
//		BillVO billVO = new BillVO();
//
//		/**
//		 * 1. 交通费用
//		 */
//		String sql = "select * from er_busitem where isnull(dr,0)=0 "+ condition1 + " and tablecode = 'arap_bxbusitem' order by rowno";
//		ArrayList<BXBusItemVO> list1 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql, new BeanListProcessor(BXBusItemVO.class));
//		
//		if (list1 != null && list1.size() > 0) { 
//			HashMap<String, Object>[] maps = transNCVOTOMap(list1.toArray(new BXBusItemVO[0]));
//			String[] formulas = new String[] { 
//					        "vostatus->1;" +
//					        "pk_reimtype_name->getColValue(er_reimtype,name,pk_reimtype,pk_reimtype);" +
//							"defitem16->getColValue(bd_defdoc,name,pk_defdoc,defitem16);" +
//							"szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);" +
//							"defitem15->getColValue(bd_defdoc,name,pk_defdoc,defitem15);" +
//							"receiver_name->getColValue(bd_psndoc,name,pk_psndoc,receiver);" +
//							"defitem12->getColValue(bd_defdoc,name,pk_defdoc,defitem12);" +
//			                "defitem13->getColValue(bd_defdoc,name,pk_defdoc,defitem13);" };
//			PubTools.execFormulaWithVOs(maps, formulas);
//
//			billVO.setTableVO("arap_bxbusitem", maps);
//			billVOs[0] = billVO; 
//		}
//
//		/**
//		 * 2. 其他费用
//		 */
//		sql = "select * from er_busitem where isnull(dr, 0)=0 " + condition1 +" and tablecode='other' order by rowno";
//		ArrayList<BXBusItemVO> list2 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql, new BeanListProcessor(BXBusItemVO.class));
//		
//		if (list2 != null && list2.size() >0) { 
//			HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new BXBusItemVO[0]));
//			String[] formulas = new String[] { "vostatus->1;pk_reimtype_name->getColValue(er_reimtype,name,pk_reimtype,pk_reimtype);defitem16->getColValue(bd_defdoc,name,pk_defdoc,defitem16);szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);defitem15->getColValue(bd_defdoc,name,pk_defdoc,defitem15);receiver_name->getColValue(bd_psndoc,name,pk_psndoc,receiver);defitem12->getColValue(bd_defdoc,name,pk_defdoc,defitem12);defitem13->getColValue(bd_defdoc,name,pk_defdoc,defitem13);" };	
//			PubTools.execFormulaWithVOs(maps, formulas);
//
//			billVO.setTableVO("other", maps);
//			billVOs[1] = billVO; 
//		}
//
//		/**
//		 * 3. 冲销明细
//		 */
//		sql = "select * from er_bxcontrast where isnull(dr, 0) = 0 " + condition2 ;
//		ArrayList<BxcontrastVO> list3 = (ArrayList<BxcontrastVO>) dao.executeQuery(sql, new BeanListProcessor(BxcontrastVO.class));
//		
//		if (list3 != null && list3.size() > 0) { 
//			HashMap<String, Object>[] maps = transNCVOTOMap(list3.toArray(new BxcontrastVO[0]));
//			String[] formulas = new String[] { "vostatus->1;szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);jkbxr_name->getColValue(bd_psndoc,name,pk_psndoc,jkbxr);pk_org_name->getColValue(org_financeorg,name,pk_financeorg,pk_org);" };
//			PubTools.execFormulaWithVOs(maps, formulas);
//
//			billVO.setTableVO("er_bxcontrast", maps);
//			billVOs[2] = billVO;
//		}
//		
//		/**
//		 * 4. 分摊明细
//		 */
//		sql = "select * from er_cshare_detail where isnull(dr, 0) = 0 " + condition1 +" order by rowno ";
//		ArrayList<CShareDetailVO> list4 = (ArrayList<CShareDetailVO>) dao.executeQuery(sql, new BeanListProcessor(CShareDetailVO.class));
//		
//		if (list4 != null && list4.size() > 0) { 
//			HashMap<String, Object>[] maps = transNCVOTOMap(list4.toArray(new CShareDetailVO[0]));
//			/**
//			 * vostatus->1 界面未编辑，也会回传给后台
//			 */
//			String[] formulas = new String[] { "vostatus->1;szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);jkbxr_name->getColValue(bd_psndoc,name,pk_psndoc,jkbxr);pk_org_name->getColValue(org_financeorg,name,pk_financeorg,pk_org);" };
//			PubTools.execFormulaWithVOs(maps, formulas);
//
//			billVO.setTableVO("er_cshare_detail", maps);
//			billVOs[3] = billVO;
//		}
//
//		/**
//		 * 5. 核销明细
//		 */
//	    sql = "select * from er_accrued_verify where isnull(dr,0)=0 " + condition2;
//		ArrayList<AccruedVerifyVO> list5 = (ArrayList<AccruedVerifyVO>) dao.executeQuery(sql, new BeanListProcessor(AccruedVerifyVO.class));
//		
//		if (list5 != null && list5.size() > 0) { 
//			HashMap<String, Object>[] maps = transNCVOTOMap(list5.toArray(new AccruedVerifyVO[0]));
//			String[] formulas = new String[] { "vostatus->1;szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);deptid_v_name->getColValue(org_dept_v,name,pk_vid,deptid_v);hbbm_name->getColValue(bd_supplier,name,pk_supplier,hbbm);" };
//			PubTools.execFormulaWithVOs(maps, formulas);
//
//			billVO.setTableVO("accrued_verify", maps);
//			billVOs[4] = billVO;
//		} 
//		
//		qbillVO.setQueryVOs(billVOs);
//		return qbillVO;
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String billtype = super.getBilltype();
		String billtype_code = "";
		String sql = "";
		billtype_code = new FeeBaseAction().getbilltypeCode(billtype);
		if (billtype_code.length() > 1) {
			billtype_code = " and er_bxzb.djlxbm ='" + billtype_code + "' ";
		}
 
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","er_bxzb.pk_jkbx");
		map.put("PK","er_bxzb.pk_jkbx");
		map.put("pk_corp", "pk_fiorg");
		map.put("arap_bxzb", "er_bxzb");
		map.put("zfdwbm", "er_bxzb");
		map.put("fydwbm", "er_bxzb");
		map.put("dwbm", "er_bxzb");
		map.put("bx_receiver", "er_bxzb");
		map.put("arap_bxbusitem", "er_busitem");
		map.put("other", "er_busitem");
		map.put("pk_org", "er_bxzb.pk_org");
		map.put("head", "er_bxzb");
		StringBuffer str = reCondition(obj, true,map);

		// 查询sql语句并进行分页
		sql = "select *\n" +
				"  from (select rownum rowno, a.*\n" + 
				"          from (select distinct er_bxzb.*\n" + 
				"                  from er_bxzb\n" + 
				"                  left join er_busitem on er_bxzb.pk_jkbx = er_busitem.pk_jkbx and nvl(er_busitem.dr,0)=0\n" +
				"                  left join er_bxcontrast on er_bxzb.pk_jkbx = er_bxcontrast.pk_bxd and nvl(er_bxcontrast.dr,0)=0\n" + 
				" 				   left join er_cshare_detail on er_bxzb.pk_jkbx = er_bxcontrast.pk_bxd and nvl(er_cshare_detail.dr,0)=0 \n" +
				"                 where nvl(er_bxzb.dr, 0) = 0\n" + 
				"                  and creator = '" + userid + "'\n" + 
				"                  "+str+"\n" + 
				"                  "+ billtype_code +"\n" + 
				"                 order by djrq  desc) a)\n" + 
				" where rowno between " + startnum + " and " + endnum;

		ArrayList<BXHeaderVO> list = (ArrayList<BXHeaderVO>) dao.executeQuery(sql, new BeanListProcessor(BXHeaderVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new BXHeaderVO[0]));
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

			maps[i].put("billtype", 30);
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		//翻译 费用申请单号
		new FeeBaseAction().excuteListShowFormulas(djbm, billVOs);
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
//		QUERY_HMH30200_arap_bxbusitem
		String action = super.getAction();// 每次执行此方法，返回页签名称
		
		StringBuffer str = dealCondition(obj, true); 
		String condition = str.toString().replace("pk_corp", "pk_org"); 
		String condition1 = condition.toString().replace("PPK", "pk_jkbx").replace("pk_org.", "") ; 
		String condition2 = condition.toString().replace("PPK", "pk_bxd").replace("pk_org.", "") ; 
 
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();

		// 交通费用 
		String sql1 = "select * from er_busitem where isnull(dr, 0)= 0 " + condition1 + "and tablecode = 'arap_bxbusitem' order by rowno ;";
		String[] formulas1 = new String[]{ 
										"vostatus->1;" +
										"pk_reimtype_name->getColValue(er_reimtype,name,pk_reimtype,pk_reimtype);" +
										"defitem26->getColValue(bd_defdoc,name,pk_defdoc,defitem26);" +
										"szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);" +
										"defitem15->getColValue(bd_defdoc,name,pk_defdoc,defitem15);" +
										"receiver_name->getColValue(bd_psndoc,name,pk_psndoc,receiver);" +
										"defitem12->getColValue(bd_defdoc,name,pk_defdoc,defitem12);" +
										"defitem13->getColValue(bd_defdoc,name,pk_defdoc,defitem13);" };
		// 其他费用
		String sql2 = "select * from er_busitem where isnull(dr,0)= 0 " + condition1 + " and tablecode = 'other' order by rowno ;";
		String[] formulas2 = new String[] { "vostatus->1;pk_reimtype_name->getColValue(er_reimtype,name,pk_reimtype,pk_reimtype);defitem16->getColValue(bd_defdoc,name,pk_defdoc,defitem16);szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);defitem15->getColValue(bd_defdoc,name,pk_defdoc,defitem15);receiver_name->getColValue(bd_psndoc,name,pk_psndoc,receiver);defitem12->getColValue(bd_defdoc,name,pk_defdoc,defitem12);defitem13->getColValue(bd_defdoc,name,pk_defdoc,defitem13);" };	

		// 冲销明细
		String sql3 = "select * from er_bxcontrast where isnull(dr,0) = 0 " + condition2 ;
		String[] formulas3 = new String[] { "vostatus->1;szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);jkbxr_name->getColValue(bd_psndoc,name,pk_psndoc,jkbxr);pk_org_name->getColValue(org_financeorg,name,pk_financeorg,pk_org);" };
	
		// 核销明细
		String sql4 = "select * from er_accrued_verify where isnull(dr,0)=0 " + condition2 ;
		String[] formulas4 = new String[] { "vostatus->1;szxmid_name->getColValue(bd_inoutbusiclass,name,pk_inoutbusiclass,szxmid);deptid_v_name->getColValue(org_dept_v,name,pk_vid,deptid_v);hbbm_name->getColValue(bd_supplier,name,pk_supplier,hbbm);" };

		//费用分摊 
		String sql5 = "select * from er_cshare_detail where isnull(dr,0)=0 " + condition1;
		/**
		 * vostatus->1 界面未编辑，也会回传给后台
		 */
		String[] formulas5 = new String[] { "vostatus->1;" };
		
		if(null!=action && !action.contains("QUERY")){
			// 交通费用 
			ArrayList<BXBusItemVO> list1 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql1, new BeanListProcessor(BXBusItemVO.class));
			if (list1 != null && list1.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list1.toArray(new BXBusItemVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas1);
				billVO.setTableVO("arap_bxbusitem", maps); 
			} 

			// 其他费用
			ArrayList<BXBusItemVO> list2 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql2, new BeanListProcessor(BXBusItemVO.class));
			if (list2 != null && list2.size() >0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new BXBusItemVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas2);
				billVO.setTableVO("other", maps); 
			} 

			// 冲销明细
			ArrayList<BxcontrastVO> list3 = (ArrayList<BxcontrastVO>) dao.executeQuery(sql3, new BeanListProcessor(BxcontrastVO.class));
			if (list3 != null && list3.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list3.toArray(new BxcontrastVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas3);
				billVO.setTableVO("er_bxcontrast", maps); 
			} 

			// 核销明细
			ArrayList<BxcontrastVO> list4 = (ArrayList<BxcontrastVO>) dao.executeQuery(sql4, new BeanListProcessor(BxcontrastVO.class));
			if (list4 != null && list4.size() > 0) {

				HashMap<String, Object>[] maps = transNCVOTOMap(list4.toArray(new BxcontrastVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas4);
				billVO.setTableVO("accrued_verify", maps); 
			} 
			
			//费用分摊 
			ArrayList<CShareDetailVO> list5 = (ArrayList<CShareDetailVO>) dao.executeQuery(sql5, new BeanListProcessor(CShareDetailVO.class));
			if (list5 != null && list5.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list5.toArray(new CShareDetailVO[0]));
				PubTools.execFormulaWithVOs(maps, formulas5);
				billVO.setTableVO("er_cshare_detail", maps); 
			} 
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
			
		}else if(null!=action && action.contains("QUERY")){
			// 交通费用
			if (action.contains("arap_bxbusitem")) {
				ArrayList<BXBusItemVO> list1 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql1, new BeanListProcessor(BXBusItemVO.class));
				if (list1 != null && list1.size() > 0) {

					HashMap<String, Object>[] maps = transNCVOTOMap(list1.toArray(new BXBusItemVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas1);
					billVO.setTableVO("arap_bxbusitem", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}

			// 其他费用
			if(action.contains("other")){
				ArrayList<BXBusItemVO> list2 = (ArrayList<BXBusItemVO>) dao.executeQuery(sql2, new BeanListProcessor(BXBusItemVO.class));
				if (list2 != null && list2.size() >0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new BXBusItemVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas2);
					billVO.setTableVO("other", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}

			// 冲销明细
			if (action.contains("er_bxcontrast")) {
				ArrayList<BxcontrastVO> list3 = (ArrayList<BxcontrastVO>) dao.executeQuery(sql3, new BeanListProcessor(BxcontrastVO.class));
				if (list3 != null && list3.size() > 0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list3.toArray(new BxcontrastVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas3);
					billVO.setTableVO("er_bxcontrast", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO; 
				}
			}

			// 核销明细
			if (action.contains("accrued_verify")) {
				ArrayList<BxcontrastVO> list4 = (ArrayList<BxcontrastVO>) dao.executeQuery(sql4, new BeanListProcessor(BxcontrastVO.class));
				if (list4 != null && list4.size() > 0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list4.toArray(new BxcontrastVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas4);
					billVO.setTableVO("accrued_verify", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO;
				}
			}
			
			//费用分摊
			if (action.contains("er_cshare_detail")) {
				ArrayList<CShareDetailVO> list5 = (ArrayList<CShareDetailVO>) dao.executeQuery(sql5, new BeanListProcessor(CShareDetailVO.class));
				if (list5 != null && list5.size() > 0) {
					HashMap<String, Object>[] maps = transNCVOTOMap(list5.toArray(new CShareDetailVO[0]));
					PubTools.execFormulaWithVOs(maps, formulas5);
					billVO.setTableVO("er_cshare_detail", maps);
					billVOs[0] = billVO;
					qbillVO.setQueryVOs(billVOs);
					return qbillVO;
				}
			}	
		}
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if (null != bill.getHeadVO().get("pk_jkbx")) {
			String csaleorderid = bill.getHeadVO().get("pk_jkbx").toString();
			BXVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(BXVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("DELETE", "264X", null, aggvos, new BXVO[] { aggvos }, null);
			
			/**
			 * 删除成功后回写发票票夹状态 //dongl 发票动态替换
			 */
			if(booleanFP.booleanValue()){
				String fphm = getFphm(getBilltype());
				if(null != fphm && !"".equals(fphm)){
					if(o instanceof MessageVO[]){
						MessageVO[] returnVO = (MessageVO[]) o;
						if(returnVO.length>0){
							MessageVO messagevo = returnVO[0];
							BXVO vo = (BXVO) messagevo.getSuccessVO();
							//BXHeaderVO returnHVO = (BXHeaderVO) vo.getParentVO();
							BXBusItemVO[] bvos = (BXBusItemVO[]) vo.getChildrenVO(); 
							StringBuffer fphms = new StringBuffer();
							//1.查发票字段
							HashMap<String, Object>[] maps = transNCVOTOMap(bvos);
							for(int i = 0;i<maps.length;i++){
								HashMap<String,Object> bmaps = maps[i];
								Iterator<Map.Entry<String, Object>> iterator = bmaps.entrySet().iterator();
								//2.迭代key值相等
								while (iterator.hasNext()) {
									Entry<String, Object> entry = iterator.next();
									if(fphm.equals(entry.getKey())){
										//这里要处理下invoiceID\
										if(null != maps[i].get(entry.getKey()) && !"".equals(maps[i].get(entry.getKey()))){
											String invoiceID = maps[i].get(entry.getKey()).toString();
											if(invoiceID.contains(",")){
												String incoiceIds = new BdocAction().getPKs(invoiceID);
												fphms.append("'").append(incoiceIds).append("',");
											}else{
												fphms.append("'").append(maps[i].get(entry.getKey())).append("',");
											}
										}
									}
								}
							}
							if(fphms.length()>0){
								String sql = "update erm_fpgl set srcbillid='',billversionpk='' where pk_fpgl in ("+fphms.substring(0,fphms.length()-1)+")";
								dao.executeUpdate(sql);
							}
						}

					}
				}
			}
		}
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if (null != bill.getHeadVO().get("pk_jkbx")) {
			String csaleorderid = bill.getHeadVO().get("pk_jkbx").toString();
			BXVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(BXVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("SAVE", "264X", null, aggvos, new BXVO[] { aggvos }, null);
			return queryBillVoByPK(userid, csaleorderid);
		}
		return null;
	}
	
	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {

		BillVO bill = (BillVO) obj;
		if (null != bill.getHeadVO().get("pk_jkbx")) {
			String csaleorderid = bill.getHeadVO().get("pk_jkbx").toString();
			BXVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(BXVO.class, csaleorderid, false);
			IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
			Object o = pf.processAction("UNSAVE", "264X", null, aggvos, new BXVO[] { aggvos }, null);
			return queryBillVoByPK(userid, csaleorderid);
		}
		return null;
	} 
} 