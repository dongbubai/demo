package hd.bs.bill.bxfee;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.DefEventVO;
import hd.vo.muap.pub.MDefDocVO;
import hd.vo.muap.pub.QueryBillVO;
import hd.vo.muap.pub.RetDefDocVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

import com.hazelcast.nio.serialization.Data;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.pub.IErmBillUIPublic;
import nc.itf.fi.pub.SysInit;
import nc.itf.uap.pf.IPfExchangeService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.util.erm.costshare.ErmForCShareUtil;
import nc.vo.ep.bx.BXBusItemVO;
import nc.vo.ep.bx.BXHeaderVO;
import nc.vo.ep.bx.BXVO;
import nc.vo.ep.bx.JKBXHeaderVO;
import nc.vo.ep.bx.JKHeaderVO;
import nc.vo.ep.bx.JKVO;
import nc.vo.er.djlx.DjLXVO;
import nc.vo.erm.costshare.AggCostShareVO;
import nc.vo.erm.costshare.CostShareVO;
import nc.vo.erm.fppj.AggFppjHVO;
import nc.vo.erm.fppj.FpglVO;
import nc.vo.erm.mactrlschema.MtappCtrlbillVO;
import nc.vo.erm.matterapp.AggMatterAppVO;
import nc.vo.erm.matterapp.MatterAppConvResVO;
import nc.vo.erm.matterapp.MatterAppVO;
import nc.vo.erm.matterapp.MtAppDetailVO;
import nc.vo.muap.muap05.BillBVO;
import nc.vo.muap.muap05.BillHVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

public class FeeBaseAction extends AbstractMobileAction{

	BaseDAO dao = new BaseDAO();
	BillVO billVO = new BillVO();
	BillVO[] billVOs = new BillVO[1];
	
	/**
	 * dongl  2020-6-17  ��ѯ�Ƿ����÷�Ʊҵ����� Ĭ��ΪN 
	 * ���ͻ�����Ʊģ��� ΪY
	 * @return Y/N
	 * @throws BusinessException
	 */
	//�Ƿ����÷�Ʊ����
	public UFBoolean getsysinitFP() throws BusinessException{
		String groupID = InvocationInfoProxy.getInstance().getGroupId();
		String booleanFP = SysInit.getParaString(groupID, "H013");
		if(booleanFP==null||booleanFP.trim().length()==0){
			throw new BusinessException("����H013����Ϊ�գ�");
		}
		return new UFBoolean(booleanFP);
	}
	
	//�Ƿ��������
	public UFBoolean sysinitFPCheck() throws BusinessException{
		String groupID = InvocationInfoProxy.getInstance().getGroupId();
		String booleanFP = SysInit.getParaString(groupID, "H014");
		if(booleanFP==null||booleanFP.trim().length()==0){
			throw new BusinessException("����H014����Ϊ�գ�");
		}
		return new UFBoolean(booleanFP);
	}
	
	
	
	//������ �� �� Ĭ�ϸ��������˺� ����������ķ�����
	public String getSkyhzh(String receiver) throws BusinessException {
		String sql = 
				"select distinct bd_bankdoc.name,bd_bankaccsub.pk_bankaccsub\n" +
						"              from bd_bankaccbas bd_bankaccbas\n" + 
						"              join bd_bankaccsub bd_bankaccsub\n" + 
						"                on bd_bankaccbas.pk_bankaccbas = bd_bankaccsub.pk_bankaccbas\n" + 
						"              join bd_psnbankacc bd_psnbankacc\n" + 
						"                on bd_bankaccsub.pk_bankaccbas = bd_psnbankacc.pk_bankaccbas\n" + 
						"              join bd_bankdoc bd_bankdoc\n" + 
						"                on bd_bankdoc.pk_bankdoc = bd_bankaccbas.pk_bankdoc\n" + 
						"                where bd_bankaccbas.enablestate = 2\n" + 
						"                and  bd_psnbankacc.isexpenseacc  = 'Y'" +
						"                and nvl(bd_bankaccbas.dr, 0) = 0"+ 
						"                and nvl(bd_psnbankacc.dr, 0) = 0"+ 
						"                and nvl(bd_bankdoc.dr, 0) = 0"+ 
						"                and nvl(bd_psnbankacc.dr, 0) = 0\n" + 
						"                and bd_psnbankacc.pk_psndoc = '"+receiver+"'";
		ArrayList<Object[]> cusmessage = new ArrayList<Object[]>();
		cusmessage = (ArrayList<Object[]>) dao.executeQuery(sql,new ArrayListProcessor());
		if (null != cusmessage && cusmessage.size() > 0) {
			if(null != cusmessage.get(0)[1] && !"".equals(cusmessage.get(0)[1])){
				return cusmessage.get(0)[1].toString();
			}
		}
		return null;
	}
	
	//Ĭ��ְ��
	public ArrayList<Object[]> getOm_job(String userid,String zyx1) throws BusinessException {
		StringBuffer  om_job = new StringBuffer("");
		om_job.append(
				"select om_job.pk_job, om_job.jobcode, om_job.jobname, om_jobtype.pk_jobtype\n" +
						"  from bd_psnjob bd_psnjob\n" + 
						"  join bd_psndoc psndoc\n" + 
						"    on bd_psnjob.pk_psndoc = psndoc.pk_psndoc\n" + 
						"  join sm_user sm_user\n" + 
						"    on sm_user.pk_psndoc = psndoc.pk_psndoc\n" + 
						"  join om_job om_job\n" + 
						"    on bd_psnjob.pk_job = om_job.pk_job\n" + 
						"  join om_jobtype om_jobtype\n" + 
						"    on om_job.pk_jobtype = om_jobtype.pk_jobtype");
		om_job.append(" where");
		if(null != zyx1 && !"".equals(zyx1) ){
			om_job.append(" om_job.pk_job = '"+zyx1+"'");
		}else{
			om_job.append(" sm_user.cuserid = '"+userid+"'");
		}
		om_job.append(
				"and bd_psnjob.ismainjob = 'Y'\n" +
						"  and nvl(bd_psnjob.dr, 0) = 0\n" + 
						"  and nvl(psndoc.dr, 0) = 0\n" + 
						"  and nvl(sm_user.dr, 0) = 0\n" + 
						"  and nvl(om_jobtype.dr, 0) = 0\n" + 
						"  and nvl(om_job.dr, 0) = 0");
		ArrayList<Object[]> cusmessage = new ArrayList<Object[]>();
		cusmessage = (ArrayList<Object[]>) dao.executeQuery(om_job.toString(),new ArrayListProcessor());
		if (null != cusmessage && cusmessage.size() > 0) {
			return cusmessage;
		}
		return null;
	}

	/**
	 * ��ְ����
	 */
	public Object getJZOrg_orgs(String key,String userid,String pk_org) throws BusinessException {

		StringBuffer str  = new StringBuffer(
				" select org_orgs.pk_org,org_orgs.code,org_orgs.name,psnjob.pk_dept,psnjob.ismainjob\n" +
						"  from bd_psnjob psnjob\n" + 
						"  join sm_user sm\n" + 
						"  on psnjob.pk_psndoc = sm.pk_psndoc\n" + 
						"  join org_orgs  org_orgs\n" + 
						"  on org_orgs.pk_org = psnjob.pk_org\n" + 
						"  where nvl(psnjob.dr, 0) = 0\n" + 
						"  and nvl(sm.dr, 0) = 0\n" + 
						"  and nvl(org_orgs.dr, 0) = 0\n" + 
						"  and org_orgs.isbusinessunit = 'Y'\n" +
						"  and sm.cuserid = '"+userid+"' ");
		if("pk_fiorg".equals(key) || "pk_org".equals(key)){
			str.append(" and org_orgs.pk_org = '"+pk_org+"'");
		}
		str.append(" order by org_orgs.code");
		ArrayList<Object[]> cusmessage = new ArrayList<Object[]>();
		cusmessage = (ArrayList<Object[]>) dao.executeQuery(str.toString(),new ArrayListProcessor());
		if(null != cusmessage  && cusmessage.size() > 0){
			return cusmessage;
		}
		return null;
	}
	
	public String getbilltypeCode(String muj)throws BusinessException{ 
		String sql = "select * from muap_bill_h where vmobilebilltype='"+muj+"' and isnull(dr,0)=0";
		ArrayList<BillHVO> billhvo = (ArrayList<BillHVO> )dao.executeQuery(sql, new BeanListProcessor(BillHVO.class)); 
		if(billhvo!=null&&billhvo.size()>0){
			if(null!=billhvo.get(0).getBilltype() || !"".equals(billhvo.get(0).getBilltype())){
				return billhvo.get(0).getBilltype();
			}else{
				throw new BusinessException("���������е������ͻ�������δ��д!");
			}
		}else{
			throw new BusinessException("��������δע��!");
		}
	}

	/**
	 * dongl ��ȡ�������뵥��ͷ��Ϣ
	 */
	public Object matterAppVOfind(String  id) throws BusinessException{
		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, id, false);
		MatterAppVO mthead = aggvos.getParentVO(); 
		mthead.setStatus(2);
		return mthead;
	}

	//����  2020-5-12 14:10:12 ����������ƹ�������     �������ͽ������õ�
	public Object mtapp_cbill(String pk_billtype,String pk_org) throws BusinessException {
		StringBuffer str = new StringBuffer("'");
		pk_billtype = getbilltypeCode(pk_billtype);
		
		StringBuffer sql = new StringBuffer("select distinct mtapp.pk_tradetype\n" +  
					"  from er_mtapp_cbill mtapp\n" + 
					"  join bd_billtype billtype on billtype.pk_billtypeid = mtapp.pk_src_tradetype\n" + 
					" where mtapp.src_tradetype = '"+pk_billtype+"'\n" + 
					"   and isnull(mtapp.dr, 0) = 0\n" + 
					"   and isnull(billtype.dr, 0) = 0");
		//�Ȳ�ѯ��֯
		//sql.append(" and mtapp.pk_org = '"+pk_org+"'");
		ArrayList<MtappCtrlbillVO> mtappList = (ArrayList<MtappCtrlbillVO>) dao.executeQuery(sql.toString(), new BeanListProcessor(MtappCtrlbillVO.class));
		if(null != mtappList && mtappList.size() > 0){
			for(int i=0;i<mtappList.size();i++){
				str.append(mtappList.get(i).getPk_tradetype()+"','");
			}
			return str.toString().substring(0,str.toString().length()-2);
		}
		
		else{
			sql.append( " and mtapp.pk_org = '"+InvocationInfoProxy.getInstance().getGroupId()+"'");
			mtappList = (ArrayList<MtappCtrlbillVO>) dao.executeQuery(sql.toString(), new BeanListProcessor(MtappCtrlbillVO.class));
			if(null != mtappList && mtappList.size() > 0){
				for(int i=0;i<mtappList.size();i++){
					str.append(mtappList.get(i).getPk_tradetype()+"','");
				}
				return str.toString().substring(0,str.toString().length()-2);
			}else{
				throw new BusinessException("������������ƹ������á��ڵ�δ���ö�Ӧ��ϵ");
			}
		}
	}

	

	public Object queryPageMtapp(String userid, String billtype, Object obj) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "mtapp_bill.pk_org");
		condition = condition.replace("mtapp_detail", "er_mtapp_detail");
		
		String billtypecode = getbilltypeCode(billtype);
		String and = "";
		if(null!=billtypecode && !"".equals(billtypecode) && billtypecode.startsWith("264")){
			and = "and p.pk_djdl = 'bx'";
		}
		//�жϵ�ǰ������ ���Բ�����Щ���� ��������
		String pk_billtypecode = (String) new FeeBaseAction().mtapp_cbill(billtype,InvocationInfoProxy.getInstance().getGroupId());
		
		String rolesql ="select sm_user_role.pk_role\n" +
						"  from sm_role\n" + 
						"  join sm_user_role on sm_role.pk_role=sm_user_role.pk_role and isnull(sm_user_role.dr,0)=0\n" + 
						" where sm_user_role.cuserid='"+userid+"'\n" + 
						"   and role_type = 1\n" + 
						"   and isnull(sm_role.dr,0)=0";
		String sql ="select distinct mtapp_bill.*\n" +
					"  from er_mtapp_detail er_mtapp_detail\n" + 
					"  join er_mtapp_bill mtapp_bill on er_mtapp_detail.pk_mtapp_bill = mtapp_bill.pk_mtapp_bill\n" + 
					" where mtapp_bill.rest_amount>0 " +
					"   and er_mtapp_detail.orig_amount >\n" + 
					"       nvl((SELECT sum(p.exe_amount + p.pre_amount)\n" + 
					"             FROM er_mtapp_billpf p\n" + 
					"            WHERE p.pk_mtapp_detail = er_mtapp_detail.pk_mtapp_detail\n" + 
					"              "+and+"\n" + 
					"            GROUP BY p.pk_mtapp_detail),\n" + 
					"           0)\n" + 
					"   and er_mtapp_detail.billmaker in\n" + 
					"       (select distinct bd_psndoc.pk_psndoc\n" + 
					"          from bd_psndoc, bd_psnjob\n" + 
					"         where bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc\n" + 
					"           and (bd_psndoc.pk_psndoc in (select pk_psndoc from sm_user where cuserid='"+userid+"' and isnull(dr,0)=0)or\n" + 
					"               (bd_psndoc.pk_psndoc in\n" + 
					"               (select pk_user\n" + 
					"                    from er_indauthorize\n" + 
					"                   where type = 0\n" + 
					"                     and keyword = 'busiuser'\n" + 
					"                     and pk_roler in ("+rolesql+"))) or\n" + 
					"               (bd_psnjob.pk_dept in\n" + 
					"               (select pk_user\n" + 
					"                    from er_indauthorize\n" + 
					"                   where type = 0\n" + 
					"                     and keyword = 'pk_deptdoc'\n" + 
					"                     and pk_roler in ("+rolesql+"))) or\n" + 
					"               ((select count(pk_user)\n" + 
					"                    from er_indauthorize\n" + 
					"                   where type = 0\n" + 
					"                     and keyword = 'isall'\n" + 
					"                     and pk_user like 'true%'\n" + 
					"                     and pk_roler in ("+rolesql+")) > 0) or\n" + 
					"               ((select count(pk_user)\n" + 
					"                    from er_indauthorize\n" + 
					"                   where type = 0\n" + 
					"                     and keyword = 'issamedept'\n" + 
					"                     and pk_user like 'true%'\n" + 
					"                     and pk_roler in ("+rolesql+")) > 0 and\n" + 
					"               bd_psnjob.pk_dept in (select bd_psnjob.pk_dept\n" +
					"										from bd_psnjob\n" + 
					"										join sm_user on sm_user.pk_psndoc=bd_psnjob.pk_psndoc and nvl(sm_user.dr,0)=0\n" + 
					"										where cuserid='"+userid+"'\n" + 
					"										and nvl(bd_psnjob.dr,0)=0)) or\n" + 
					"               (bd_psndoc.pk_psndoc in\n" + 
					"               (select pk_user\n" + 
					"                    from er_indauthorize\n" + 
					"                   where pk_operator = '"+userid+"'\n" + 
					"                     and billtype like '261%'\n" + 
//					"                     and '2020-05-22 16:17:12' <= enddate\n" + 
//					"                     and '2020-05-22 16:17:12' >= startdate" +
					"    ))))\n" + 
					"   and er_mtapp_detail.pk_tradetype in\n" + 
					"       (select djlxbm\n" + 
					"          from er_djlx\n" + 
					"         where djdl = 'ma'\n" + 
					"           and matype is null\n" + 
					"            or matype in (0, 1)\n" + 
//					"           and pk_group = '00011110000000000E5K'" +
					"       )\n" + 
					"   and er_mtapp_detail.effectstatus = 1\n" + 
					"   and er_mtapp_detail.close_status = 2\n" + 
					"   and nvl(er_mtapp_detail.dr,0) = 0\n" + 
					"   and nvl(mtapp_bill.dr,0) = 0" +
					"   and mtapp_bill.pk_tradetype in ("+pk_billtypecode+")"+
					"   "+condition+""+
					"   order by  mtapp_bill.billdate desc";
		ArrayList<MatterAppVO> list = (ArrayList<MatterAppVO>) dao.executeQuery(sql.toString(), new BeanListProcessor(MatterAppVO.class));
		if(null == list || list.size()==0){
			throw new BusinessException("�����뵥��");
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new MatterAppVO[0]));
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		HashMap<String, Object> headVO = null;
		HashMap<String, Object>[] bodyVoMap = null;
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			//���ճ������뵥���ɱ༭
			maps[i].put("ibillstatus", 1);
			headVO = maps[i];
			billVO.setHeadVO(headVO);
			MtAppDetailVO[] bodyVO = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class,maps[i].get("pk_mtapp_bill").toString() , false).getChildrenVO();
			List<MtAppDetailVO> bList = new ArrayList<MtAppDetailVO>();
			//����ӱ���ĳ������û��������ʾ
			for(int b=0; b<bodyVO.length; b++){
				if(bodyVO[b].getRest_amount().compareTo(new UFDouble(0)) > 0){
					bList.add(bodyVO[b]);
				}
				if(null!=bList && bList.size()>0){
					bodyVoMap = transNCVOTOMap(bList.toArray(new MtAppDetailVO[0]));
					billVO.setTableVO("mtapp_detail",bodyVoMap);
				}
			}
			billVOs[i] = billVO;
		} 
		qbillVO.setQueryVOs(billVOs);
		//����APP��ͷ�б��ͱ����б���ʾ��ʽ
		excuteListShowFormulas(pk_billtypecode, billVOs);
		return qbillVO;
	}
	
	
	protected void excuteListShowFormulas(String pk_billtypecode, BillVO[] billVOs) throws BusinessException {
		// ��ȡ����������Ϣ
		BillHVO configHVO = null;
		BillBVO[] configBVOs = null;
		String strWhere = "billtype in (" + pk_billtypecode + ") and isnull(dr,0)=0 and bmapprove='N'";
		BaseDAO dao = new BaseDAO();
		ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(BillHVO.class, strWhere);

		if (list != null && list.size() > 0) {
			configHVO = (BillHVO) list.get(0);
			String where2 = "pk_billconfig_h='" + configHVO.getPrimaryKey() + "' and isnull(dr,0)=0 ";
			ArrayList<SuperVO> list2 = (ArrayList<SuperVO>) dao.retrieveByClause(BillBVO.class, where2);
			configBVOs = list2.toArray(new BillBVO[0]);
		}
		for (int i = 0; i < billVOs.length; i++) {
			// ִ�б�ͷ�б���ʾ��ʽ
			if (null!=billVOs[i] && null!=billVOs[i].getHeadVO()) {
				excuteHeadShowFormulas(configHVO, new HashMap[] { billVOs[i].getHeadVO() });
			}
			// ִ�б����б���ʾ��ʽ
			if (null!=billVOs[i] && null!=billVOs[i].getBodyVOsMap()) {
				excuteBodyShowFormulas(configBVOs, billVOs[i].getBodyVOsMap());
			}
			
			// ִ�б�ͷ�Զ��幫ʽ  ���ڷ��� �������뵥��   ����  2020-6-1 13:28:49
			if (configHVO != null) {
				String headdefformulas = ((BillHVO) configHVO).getVheaddefformulas();
				if (headdefformulas != null && headdefformulas.trim().length() > 0) {
					PubTools.execFormulaWithVOs(new HashMap[] { billVOs[i].getHeadVO() }, headdefformulas.split(";"));
				}
			}
		}
	}
	
	//����  2020-5-25 18:39:16  ��ȡ��������def1 ��ͷ�б���ʾ��ʽ
	private void excuteHeadShowFormulas(BillHVO configHVO, HashMap<String, Object>[] headVOs) throws BusinessException {
		if (null != configHVO && null != configHVO.getDef1() && configHVO.getDef1().trim().length() > 0) {
			String[] formulas = configHVO.getDef1().split(";");
			PubTools.execFormulaWithVOs(headVOs, formulas);
		}
	}
	
	private void excuteBodyShowFormulas(BillBVO[] configBVOs, HashMap<String, HashMap<String, Object>[]> bodyVoMap) throws BusinessException {
		if (configBVOs != null && configBVOs.length > 0) {
			for (int i = 0; i < configBVOs.length; i++) {
				if (configBVOs[i].getDef1() != null && configBVOs[i].getDef1().trim().length() > 0) {
					String[] formulas = configBVOs[i].getDef1().split(";");
					HashMap<String, Object>[] vos = bodyVoMap.get(configBVOs[i].getTabcode());
					if (vos != null && vos.length > 0) {
						PubTools.execFormulaWithVOs(vos, formulas);
					}
				}
			}
		}
	}
	
	
	
	//����  2020-5-12 14:10:12  ���ӿ�  �ýӿ��е���������
	public MatterAppConvResVO ermBillUimpl(Object obj,String funcode) throws BusinessException {
		BillVO bill = (BillVO) obj;
		funcode = getbilltypeCode(funcode);
		String pk_org = bill.getHeadVO().get("pk_org").toString();
		HashMap<String, Object>[] detailMap = bill.getBodyVOsMap().get("mtapp_detail");
		String id = bill.getHeadVO().get("pk_mtapp_bill").toString();
		AggMatterAppVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggMatterAppVO.class, id, false);
		//��ȡ���洫���ӱ�����
		MtAppDetailVO[] body = new MtAppDetailVO[detailMap.length];
		for (int i = 0; i < detailMap.length; i++) {
			MtAppDetailVO bodyvo =(MtAppDetailVO) transMapTONCVO(MtAppDetailVO.class,detailMap[i]);
			//Usable_amout ���ݿ�û�д�ֵ Ҫ����������   ͬʱ���� ��ͷ�ϼƽ��
			if(null != detailMap[i].get("rest_amount") && !"".equals(detailMap[i].get("rest_amount"))){
				bodyvo.setUsable_amout(new UFDouble(detailMap[i].get("rest_amount").toString()));
			}
			
			body[i]=bodyvo;
		}
		aggvos.setChildrenVO(body);
		ArrayList<DjLXVO> billtypeList = (ArrayList<DjLXVO>) dao.retrieveByClause(DjLXVO.class, " isnull(dr,0)=0 and djlxbm ='"+funcode+"'  ");
		//NC�����ӿ�
		IErmBillUIPublic ermBill =  (IErmBillUIPublic) NCLocator.getInstance().lookup(IErmBillUIPublic.class.getName());
		if(null != billtypeList && billtypeList.size() > 0){
			//NC�����ӿ�  ��������
			return  ermBill.setBillVOtoUIByMtappVO(pk_org,aggvos , billtypeList.get(0),funcode);
		}
		
		return null;
	}
	
	//��ȡ����total
	public UFDouble getTotal(HashMap<String, Object>[] detailMap){
		UFDouble total  = new UFDouble(0);
		for (int i = 0; i < detailMap.length; i++) {
			//Usable_amout ���ݿ�û�д�ֵ Ҫ����������   ͬʱ���� ��ͷ�ϼƽ��
			if(null != detailMap[i].get("rest_amount") && !"".equals(detailMap[i].get("rest_amount"))){
				total = total.add(new UFDouble(detailMap[i].get("rest_amount").toString()));
			}
		}
		return total;
		
	}
	/**
	 * ���������շ��ñ���������ת��
	 */
	public Object getBXVO(Object obj,String funcode) throws BusinessException {
		MatterAppConvResVO resVO = ermBillUimpl(obj,funcode);
		if(null != resVO){
			BillVO bill = (BillVO) obj;
			BXVO bxvo = (BXVO) resVO.getBusiobj();
			QueryBillVO qbillVO = new QueryBillVO();
			BXHeaderVO[] bxHead = new BXHeaderVO[1];
			bxHead[0] = (BXHeaderVO) bxvo.getParentVO();
			HashMap<String, Object>[] detailMap = bill.getBodyVOsMap().get("mtapp_detail");
			String pk_billtypecode = "'"+bxvo.getParentVO().getDjlxbm()+"'";
			HashMap<String, Object>[] maps = transNCVOTOMap(bxHead);
			for (int i = 0; i < maps.length; i++) {
				//���������ϼƽ��   
				if(null != bill.getHeadVO().get("rest_amount") && !"".equals(bill.getHeadVO().get("rest_amount"))){
					maps[i].put("total", getTotal(detailMap));
				}
				HashMap<String, Object> headVO = maps[i];
				billVO.setHeadVO(headVO);
				if(null != bxvo.getChildrenVO()){
					billVO.setTableVO("arap_bxbusitem", transNCVOTOMap(bxvo.getChildrenVO()));
				}
				billVOs[0] = billVO;
			}
			qbillVO.setQueryVOs(billVOs);
			excuteListShowFormulas(pk_billtypecode, billVOs);
			return qbillVO;
		}
		return null;
	}
	
	/**
	 * ������ �������뵥����ת��
	 */
	public Object getJKZBVO(Object obj,String funcode) throws BusinessException {
		MatterAppConvResVO resVO = ermBillUimpl(obj,funcode);
		if(null != resVO){
			BillVO bill = (BillVO) obj;
			JKVO jkvo = (JKVO) resVO.getBusiobj();
			QueryBillVO qbillVO = new QueryBillVO();
			JKHeaderVO[] jkHead = new JKHeaderVO[1];
			jkHead[0] = (JKHeaderVO) jkvo.getParentVO();
			HashMap<String, Object>[] maps = transNCVOTOMap(jkHead);
			HashMap<String, Object>[] detailMap = bill.getBodyVOsMap().get("mtapp_detail");
			String pk_billtypecode = "'"+jkvo.getParentVO().getDjlxbm()+"'";
			for (int i = 0; i < maps.length; i++) {
				//���������ϼƽ��   
				if(null != bill.getHeadVO().get("rest_amount") && !"".equals(bill.getHeadVO().get("rest_amount"))){
					maps[i].put("total", getTotal(detailMap));
				}
				HashMap<String, Object> headVO = maps[i];
				billVO.setHeadVO(headVO);
				if(null != jkvo.getChildrenVO()){
					billVO.setTableVO("jk_busitem", transNCVOTOMap(jkvo.getChildrenVO()));
				}
				billVOs[0] = billVO;
			}
			
			qbillVO.setQueryVOs(billVOs);
			excuteListShowFormulas(pk_billtypecode, billVOs);
			return qbillVO;
		}
		return null;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum,
			int endnum) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

    //��ѯ��Ʊ������  �Դ�֧�ֶ��ŷ�Ʊ�ĳ���   APP ����
	public Object queryInvoice(String userid, String billtype, Object obj) throws BusinessException{
		BillVO bill = (BillVO) obj;
		DefEventVO evevo = new DefEventVO();
		evevo.setRetDefDocVO(getRetDefDocVO(userid,obj));
		evevo.setVo(bill);
		evevo.setAction("MULTIDIALOG");//DIALOG    //MULTIDIALOG
		evevo.setDialogtitle("ѡ��Ʊ");
		return evevo;
	}
	
	public RetDefDocVO getRetDefDocVO(String userid,Object obj) throws BusinessException{
		
		//����ֻ��ʾδ�����ķ�Ʊ�� ������Ӧ�÷�ҳչʾ
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM (SELECT ROWNUM rw,T .*	FROM(SELECT erm_fpgl.* FROM erm_fpgl ");
		sb.append("WHERE isnull(erm_fpgl.dr, 0) = 0 " + " and creator='" + userid + "' ");
		sb.append("and srcbillid is null and cyzt <> '����' ");
		//�Ƿ�����ҵ����� H014 Y ����
  		UFBoolean booleanFP = sysinitFPCheck();
  		if(booleanFP.booleanValue()){
  			sb.append( " and cyzt <> 'δ����' ");
  		}
		sb.append("ORDER BY erm_fpgl.kprq DESC) T)");//WHERE rw BETWEEN " + 1 + " and " + 5 + "
		@SuppressWarnings("unchecked") 
		ArrayList<FpglVO> list = (ArrayList<FpglVO>) dao.executeQuery(sb.toString(), new BeanListProcessor(FpglVO.class));

		if (list == null || list.size() == 0) {
			throw new BusinessException("��Ʊ������");
		}
		RetDefDocVO refdefdocVO = new RetDefDocVO();
		ArrayList<MDefDocVO> listdefdocVO = new ArrayList<MDefDocVO>();
		for (int i = 0; i < list.size(); i++) {
			FpglVO docVO = (FpglVO) list.get(i);
			MDefDocVO defdocVO = new MDefDocVO();
			defdocVO.setCode(docVO.getFphm());
			defdocVO.setName(docVO.getFplx());
			defdocVO.setPk(docVO.getPk_fpgl());
			defdocVO.setDef1(docVO.getJshj());
			String code = docVO.getFphm();
			String name = docVO.getFplx();
			String totalPrice  = docVO.getJshj();
			defdocVO.setShowvalue(code+"  "+name+" "+totalPrice);
			listdefdocVO.add(defdocVO);
		}
		refdefdocVO.setDefdocvos(listdefdocVO.toArray(new MDefDocVO[0]));
		if(null != refdefdocVO){
			return refdefdocVO;
		}
		return null;
	}

    //��Ʊ���� ������������ dongliang 
	@SuppressWarnings("unchecked")
	public Object fillInvoice(String userid, String billtype, Object obj ,String invoicehm,String billtypecode) throws BusinessException{
		
		DefEventVO defevent = (DefEventVO) obj;
		MDefDocVO[] mdefdoc = defevent.getSelectDocVOs();
		ArrayList<BXBusItemVO> buList = new ArrayList<BXBusItemVO>();
		//���ŷ�Ʊ�ŵ�һ��LIST��
		if(null != mdefdoc && mdefdoc.length > 0){
			for(MDefDocVO doc : mdefdoc){
				AggFppjHVO fpAggvo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggFppjHVO.class, doc.getPk(), false);
				IPfExchangeService exchangeservice = NCLocator.getInstance().lookup(IPfExchangeService.class);
				BXVO bxvo = (BXVO) exchangeservice.runChangeData("FP01", billtypecode, fpAggvo, null);
				if(null != bxvo){
					BXBusItemVO[] bvos = bxvo.getChildrenVO();
					if(null!= bvos && bvos.length > 0){
						buList.addAll(Arrays.asList(bvos));
					}
				}
			}
		}
		
		
		//mapȥ���ظ�
		Map<String , String> map = new HashMap<String , String>();
		ArrayList<BXBusItemVO> newList = new ArrayList<BXBusItemVO>();
		if(null != invoicehm && !"".equals(invoicehm)){
			for(int i= 0;i< buList.size();i++){
				if(!map.containsKey(buList.get(i).getAttributeValue(invoicehm).toString())){
					map.put(buList.get(i).getAttributeValue(invoicehm).toString(), buList.get(i).getAttributeValue(invoicehm).toString());
					newList.add(buList.get(i));
				}
			}
		}
		
        if(null != newList && newList.size() > 0){
        	//״̬���ó� 1 ���༭��ͷҲ�ᴫ�ӱ�
        	String[] formulas = new String[]{"vostatus->1;" };
        	HashMap<String, Object>[] maps = transNCVOTOMap(newList.toArray(new BXBusItemVO[0]));
			PubTools.execFormulaWithVOs(maps, formulas);
        	billVO.setTableVO("arap_bxbusitem", maps);
        	DefEventVO evevo = new DefEventVO();
        	evevo.setVo(billVO);
        	evevo.setAction("ADDLINE"); 
        	if(null != evevo){
        		return evevo;
        	}
        }
		return null;
	}

    //���ý�ת�� ������������ı��������� dongl
	public Object queryPageCShare(String userid, String billtype, Object obj) throws BusinessException{
		StringBuffer str = dealCondition(obj, true);
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
		StringBuffer condition = reCondition(obj, true,map);
		StringBuffer cshare = new StringBuffer();
		cshare.append("select distinct er_bxzb.* from er_bxzb er_bxzb left join er_busitem  "); 
		cshare.append("on er_bxzb.pk_jkbx = er_busitem.pk_jkbx  and nvl(er_busitem.dr,0)=0 and nvl(er_bxzb.dr, 0) = 0 "); 
		cshare.append("where (isexpamt = 'N' and sxbz = 1 "+condition+" and iscostshare = 'Y') ");
		cshare.append("and er_bxzb.djlxbm <> '2647' and er_bxzb.ybje > 0 and er_bxzb.pk_jkbx not in (select src_id from er_costshare)  ");
		cshare.append("order by er_bxzb.djrq desc");
		@SuppressWarnings("unchecked")
		ArrayList<BXHeaderVO> list = (ArrayList<BXHeaderVO>) dao.executeQuery(cshare.toString(), new BeanListProcessor(BXHeaderVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new BXHeaderVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			//���ճ��ı��������ɱ༭
			maps[i].put("ibillstatus", 1);
			HashMap<String, Object> headVO = maps[i];
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		//����APP��ͷ�б��ͱ����б���ʾ��ʽ
		excuteListShowFormulas("'"+getbilltypeCode(billtype)+"'", billVOs);
		return qbillVO;
	}

    //���������ı����� ת��Ϊ���ý�ת�� ��ͷ���� dongl
	@SuppressWarnings("static-access")
	public Object getBXHeadVO(Object obj, String string)  throws BusinessException{
		BillVO bill = (BillVO) obj;
		//mapתVO
		BXHeaderVO jkbxHeadVO = (BXHeaderVO) super.transMapTONCVO(BXHeaderVO.class, bill.getHeadVO());
        //������	
		ErmForCShareUtil ermForCSshare = new ErmForCShareUtil();
		AggCostShareVO aggCostShareVO = ermForCSshare.convertFromBxHead(jkbxHeadVO);
		if(null != aggCostShareVO){
			CostShareVO[] costShareParent = new CostShareVO[1];
			costShareParent[0] = (CostShareVO) aggCostShareVO.getParentVO();
			HashMap<String, Object>[] maps = transNCVOTOMap(costShareParent);
			QueryBillVO qbillVO = new QueryBillVO();
			BillVO[] billVOs = new BillVO[1];
			for(int i = 0; i < maps.length; i++){
				BillVO billVO = new BillVO();
				HashMap<String, Object> headVO = maps[i];
				billVO.setHeadVO(headVO);
				billVOs[i] = billVO;
			}
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}
		return null;
	}
	
	
	
}