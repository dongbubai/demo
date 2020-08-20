package hd.bs.bill.bxfee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.pubitf.erm.accruedexpense.IErmAccruedBillManage;
import nc.vo.erm.accruedexpense.AccruedDetailVO;
import nc.vo.erm.accruedexpense.AccruedVO;
import nc.vo.erm.accruedexpense.AccruedVerifyVO;
import nc.vo.erm.accruedexpense.AggAccruedBillVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
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
 * ����Ԥ�ᵥ
 */
public class YTaccruedAction extends ApproveWorkAction{

	static BaseDAO dao = new BaseDAO();

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] mtapp_details = bodys.get("accrued_detail");
		String[] formula_str = new String[5];
		UFDouble rest_amount = new UFDouble(0);
		UFDouble orig_amount = new UFDouble(0);
		if(aevo.getKey().equals("amount")){
			if(null != head.get("amount") && !"".equals(head.get("amount"))){
				String amount = head.get("amount").toString();
				formula_str[0] = "rest_amount->" + new UFDouble(amount).setScale(2, UFDouble.ROUND_HALF_UP) + ";";
				FormulaVO formulaVO = new FormulaVO();
				formulaVO.setFormulas(formula_str);
				return formulaVO;
			}
		}
		if (aevo.getKey().equals("orig_amount")){
			for(int i=0;i<mtapp_details.length;i++){

			}
		}
		return null;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		//AccruedDetailVO �ֱ�VO  AccruedVO ����VO AggAccruedBillVO �ۺ�VO
		//er_accrued ����/��ͷ    er_accrued_detail �ֱ� ����
		//��ʼ��ģ��
		BillVO bill = (BillVO) obj;
		//��ȡapp�˱�ͷ�ֶ�
		HashMap<String, Object> head = bill.getHeadVO();
		//��ȡapp�˱����ֶ�
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		//����Ԥ����ϸ
		HashMap<String, Object>[] detailMap = bodys.get("accrued_detail");
		//�ۺ�VO
		AggAccruedBillVO aggvo = new AggAccruedBillVO();
		//��ͷ
		AccruedVO hvo = new AccruedVO();
		//����
		AccruedDetailVO[] bvos = null;
		//�޸�ʱ����ľۺ�VO��װvo
		AggAccruedBillVO cloneagg = new AggAccruedBillVO(); 

		String pk_accrued_bill="";
		/**
		 * ��������PK�ж��Ǳ��滹���޸�
		 */
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			pk_accrued_bill = head.get("PK").toString();
			Collection<AggAccruedBillVO> generalVOC = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(AggAccruedBillVO.class, " dr =0 and pk_accrued_bill='" + pk_accrued_bill + "' ", false);
			if (null == generalVOC || generalVOC.size() == 0) {// �޸�����ɾ���˵����
				throw new BusinessException("�޸ĵĶ����Ѿ���ɾ��!");
			}
			// ���ݿ��ѯ�ۺ�VO
			AggAccruedBillVO transVOs = generalVOC.toArray(new AggAccruedBillVO[] {})[0];
			// ����vo
			hvo = (AccruedVO) transVOs.getParentVO();
			bvos = transVOs.getChildrenVO();
			hvo.setStatus(1);// ״̬1���޸ĵ���˼
			hvo.setModifier(userid);

			AccruedDetailVO[] bclone = new AccruedDetailVO[bvos.length];
			for (int b = 0; b < bvos.length; b++) {// ���������Ľ�ͨ���������ñ���������¡����
				bclone[b] = new AccruedDetailVO();
				bclone[b] = (AccruedDetailVO) bvos[b].clone();
			}
			cloneagg.setParentVO((CircularlyAccessibleValueObject) hvo.clone());
			cloneagg.setChildrenVO(bclone);
		} else {
			hvo.setStatus(2);// ״̬2�Ǳ������˼
		}

		//�����Ÿ�ֵ
		String pk_group = head.get("pk_group").toString();
		hvo.setPk_group(pk_group);
		hvo.setCreator(userid); 

		//��������
		if (null != head.get("pk_tradetypeid") && !"".equals(head.get("pk_tradetypeid"))) {
			hvo.setPk_tradetypeid(head.get("pk_tradetypeid").toString());
		}else{
			throw new BusinessException("�������Ͳ���Ϊ��!");
		}
		//hvo.setPk_tradetypeid("0001A210000000002B48");
		//��֯
		String pk_org = null;
		if (null != head.get("pk_org") && !"".equals(head.get("pk_org"))) {
			pk_org = head.get("pk_org").toString();
		}else{
			throw new BusinessException("��֯���ܿ�!");
		}
		hvo.setPk_org(pk_org);
		String sql = "select * from org_orgs where pk_org = '" + pk_org + "' and dr =0;";
		IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		ArrayList<OrgVO> busitypevos = (ArrayList<OrgVO>) dao.executeQuery(sql, new BeanListProcessor(OrgVO.class));
		/*if (busitypevos.size() > 0) {
			hvo.setPk_org(busitypevos.get(0).getPk_vid());
		}*/
		//��������
		if (null != head.get("billdate") && !"".equals(head.get("billdate"))) {
			hvo.setBilldate(new UFDate(new Date()));
		}
		//����  pk_currtype
		if (null != head.get("pk_currtype")) {
			hvo.setPk_currtype(head.get("pk_currtype").toString());
		}
		// ���
		if (null != head.get("amount") && !"".equals(head.get("amount"))) {
			UFDouble orig_amount = new UFDouble(head.get("amount").toString());
			hvo.setAmount(orig_amount);
			hvo.setRest_amount(orig_amount);
			hvo.setPredict_rest_amount(orig_amount);
		}
		// ����  ��Ŀ����	reason
		if (null != head.get("reason") && !"".equals(head.get("reason"))) {
			hvo.setReason(head.get("reason").toString());
		}
		// ����״̬
		hvo.setBillstatus(1);
		// ����״̬
		hvo.setApprstatus(-1);
		hvo.setEffectstatus(0);
//		//�����˵�λ ��Ŀ����	operator_org
		String operator="";
		if (null != head.get("operator_org") && !"".equals(head.get("operator_org"))) {
			operator = head.get("operator_org").toString();
			hvo.setOperator_org(operator);
		}else{
			throw new BusinessException("�����˵�λ����Ϊ��!");
		}
		//�����˲��� ��Ŀ����	operator_dept
		if (null != head.get("operator_dept") && !"".equals(head.get("operator_dept"))) {
			operator = head.get("operator_dept").toString();
			hvo.setOperator_dept(operator);
		}else{
			throw new BusinessException("�����˲��Ų���Ϊ��!");
		}
		//������ ��Ŀ����	 operator
		if (null != head.get("operator") && !"".equals(head.get("operator"))) {
			operator = head.get("operator").toString();
			hvo.setOperator(operator);
		}else{
			throw new BusinessException("�����˲���Ϊ��!");
		}
		//hvo.setPk_org("0001A210000000002T7W");
		hvo.setRed_amount(new UFDouble(0));
		hvo.setPk_tradetype("2621");
		hvo.setIsexpedited(new UFBoolean(false));
		hvo.setIsneedimag(new UFBoolean(false));
		hvo.setGlobal_amount(new UFDouble(0));
		hvo.setGlobal_currinfo(new UFDouble(0));
		hvo.setGlobal_rest_amount(new UFDouble(0));
		hvo.setGroup_amount(new UFDouble(0));
		hvo.setGroup_currinfo(new UFDouble(0));
		hvo.setGroup_rest_amount(new UFDouble(0));
		hvo.setGlobal_verify_amount(new UFDouble(0));
		hvo.setGroup_verify_amount(new UFDouble(0));
		hvo.setOrg_currinfo(new UFDouble(1));
		hvo.setOrg_verify_amount(new UFDouble(0));
		hvo.setPk_billtype("262X");
		hvo.setVerify_amount(new UFDouble(0));
		/**
		 * Ԥ����ϸ
		 */
		ArrayList<AccruedDetailVO> detailLists = new ArrayList<AccruedDetailVO>();
		ArrayList<AccruedDetailVO> xzList = new ArrayList<AccruedDetailVO>();
		UFDouble jm = new UFDouble();
		UFDouble htotal = new UFDouble();
		UFDouble total = new UFDouble();
		UFDouble amount = new UFDouble();
		String rowno = "";
		if (null != detailMap && detailMap.length != 0) {
			for (int i = 0; i < detailMap.length; i++) {
				AccruedDetailVO detailVO = new AccruedDetailVO();
				if (null != head.get("PK") && !"".equals(head.get("PK"))) {
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "3".equals(detailMap[i].get("vostatus").toString())) {
						// ״̬Ϊɾ��
						if (null != hvo.getPk_accrued_bill()) {
							String kpString = hvo.getPk_accrued_bill();// ��ȡ��������ֵ
							detailVO.setPk_accrued_bill(kpString);
						}
						String bu = detailMap[i].get("pk_accrued_detail").toString();// ��ȡ�б�ǩ����ֵ
						detailVO.setPk_accrued_detail(bu);
						jm = new UFDouble(detailMap[i].get("amount").toString());
						total = hvo.getAmount().sub(jm);
						detailVO.setStatus(3);
					}
					// ����״̬
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "2".equals(detailMap[i].get("vostatus").toString())) {
						String kpString = hvo.getPk_accrued_bill();// ��ȡ��������ֵ
						detailVO.setPk_accrued_bill(kpString);
						jm = new UFDouble(detailMap[i].get("amount").toString());
						total = jm.add(hvo.getAmount());
						detailVO.setStatus(2);
					}
					// �޸�״̬
					if (null != detailMap[i].get("vostatus") && !"".equals(detailMap[i].get("vostatus")) && "1".equals(detailMap[i].get("vostatus").toString())) {
						// ״̬Ϊ�޸�
						if (null != hvo.getPk_accrued_bill()) {
							String kpString = hvo.getPk_accrued_bill();// ��ȡ��������ֵ
							detailVO.setPk_accrued_bill(kpString);
						}
						String bu = detailMap[i].get("pk_accrued_detail").toString();// ��ȡ�б�ǩ����ֵ
						// ���ݿ��ȡԭ��������
						String sqlm = "select * from er_accrued_detail where dr = 0  and Pk_accrued_detail = '" + bu + "' order by ts desc ";
						ArrayList<AccruedDetailVO> list1 = (ArrayList<AccruedDetailVO>) dao.executeQuery(sqlm, new BeanListProcessor(AccruedDetailVO.class));
						amount = list1.get(0).getAmount();
						detailVO.setPk_accrued_detail(bu);
						jm = new UFDouble(detailMap[i].get("amount").toString());
						total = hvo.getAmount().add(jm).sub(amount);
						detailVO.setStatus(1);
					}
					hvo.setAmount(total);
				}else{
					detailVO.setStatus(2);
				}
				rowno = (i + 1) + "";
				detailVO.setRowno(Integer.parseInt(rowno));
				//��֧��Ŀ
				if (null != detailMap[i].get("pk_iobsclass") && !"".equals(detailMap[i].get("pk_iobsclass"))) {
					detailVO.setPk_iobsclass(detailMap[i].get("pk_iobsclass").toString());
				}
				//���
				if (null != detailMap[i].get("amount") && !"".equals(detailMap[i].get("amount"))) {
					amount = new UFDouble(detailMap[i].get("amount").toString());
					htotal = amount.add(htotal);
					detailVO.setAmount(amount);
					detailVO.setPredict_rest_amount(amount);
					detailVO.setRest_amount(amount);
				}
				//���óе���λ
				if (null != detailMap[i].get("assume_org") && !"".equals(detailMap[i].get("assume_org"))) {
					detailVO.setAssume_org(detailMap[i].get("assume_org").toString());
				}
				//���óе�����
				if (null != detailMap[i].get("assume_dept") && !"".equals(detailMap[i].get("assume_dept"))) {
					detailVO.setAssume_dept(detailMap[i].get("assume_dept").toString());
				}
				//detailVO.setAssume_org("0001A210000000002Y23");
				//detailVO.setAssume_dept("1001A210000000000EPY");
				detailVO.setOrg_currinfo(hvo.getOrg_currinfo());
				//detailVO.setAssume_org("0001A210000000002T7W");
				detailVO.setOrg_rest_amount(amount);
				detailVO.setOrg_amount(amount);
				detailVO.setGlobal_amount(amount);
				detailVO.setGroup_amount(new UFDouble(0));
				detailVO.setGroup_rest_amount(new UFDouble(0));
				detailVO.setOrg_verify_amount(new UFDouble(0));
				detailVO.setRed_amount(new UFDouble(0));
				detailVO.setGlobal_amount(new UFDouble(0));
				detailVO.setGlobal_currinfo(new UFDouble(0));
				detailVO.setVerify_amount(new UFDouble(0));
				detailVO.setGlobal_rest_amount(new UFDouble(0));
				detailVO.setGroup_amount(new UFDouble(0));
				detailVO.setGroup_currinfo(new UFDouble(0));
				detailVO.setGroup_rest_amount(new UFDouble(0));
				//detailVO.setAssume_dept("1001A210000000000CLH");
				detailVO.setGlobal_verify_amount(new UFDouble(0));
				detailVO.setGroup_verify_amount(new UFDouble(0));
				
				if(null == head.get("PK") || "".equals(head.get("PK"))){
					detailVO.setStatus(2);
				}
				detailLists.add(detailVO);
			}
		}
		if(null == head.get("PK") || "".equals(head.get("PK"))){
			hvo.setAmount(htotal);
			hvo.setRest_amount(htotal);
			hvo.setOrg_amount(htotal);
			hvo.setOrg_rest_amount(htotal);
		}
		//xzList.addAll(detailLists);
		
		// ����ͷ���õ��ۺ�VO��
		aggvo.setParentVO(hvo);
		//���������õ��ۺ�VO��
		aggvo.setChildrenVO(detailLists.toArray(new AccruedDetailVO[] {}));
		Object o = null;// ��ʼ������
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		// ���»������ӽ����ж�
		if (null != head.get("PK") && !"".equals(head.get("PK"))) {
			// ����
			HashMap mapall = new HashMap(2);
			mapall.put("nc.bs.scmpub.pf.ORIGIN_VO_PARAMETER", new AggAccruedBillVO[] { cloneagg });
			mapall.put("notechecked", "notechecked");
			try {
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					IErmAccruedBillManage iIplatFormEntry = (IErmAccruedBillManage) NCLocator.getInstance().lookup(IErmAccruedBillManage.class.getName());
					o = iIplatFormEntry.updateVO(aggvo);
				} else {
					IErmAccruedBillManage iIplatFormEntry = (IErmAccruedBillManage) NCLocator.getInstance().lookup(IErmAccruedBillManage.class.getName());
					o = iIplatFormEntry.updateVO(aggvo);
				}
			} catch (Exception e) {
				if (e.getMessage().contains("�Ƿ����")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		}else{
			try {
				// ����
				if (null != head.get("checkpassflag") && !"".equals(head.get("checkpassflag")) && "true".equals(head.get("checkpassflag").toString())) {
					IErmAccruedBillManage iIplatFormEntry = (IErmAccruedBillManage) NCLocator.getInstance().lookup(IErmAccruedBillManage.class.getName());
					o = iIplatFormEntry.insertVO(aggvo);
				} else {
					IErmAccruedBillManage iIplatFormEntry = (IErmAccruedBillManage) NCLocator.getInstance().lookup(IErmAccruedBillManage.class.getName());
					o = iIplatFormEntry.insertVO(aggvo);
				}
			} catch (Exception e) {
				if (e.getMessage().contains("�Ƿ����")) {
					throw new BusinessException("SELECT" + e.getMessage());
				}
				throw new BusinessException(e.getMessage());
			}
		}
		AggAccruedBillVO returnVO = (AggAccruedBillVO) o;
		if (o != null) {
			AccruedVO returnHVO = (AccruedVO) returnVO.getParentVO();
			return queryBillVoByPK(userid, returnHVO.getPk_accrued_bill());
		}
		return null;
	}

	private Object queryBillVoByPK(String userid, String pk) throws BusinessException{
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

		// ��ѯ ��ͷ
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		// ��ѯ ����
		QueryBillVO body_querybillVO = (QueryBillVO) queryNoPage_body(userid, condAggVO_body);
		BillVO body_BillVO = body_querybillVO.getQueryVOs()[0];
		
		billVO.setHeadVO(head_BillVO.getHeadVO());
		billVO.setTableVO("accrued_detail", body_BillVO.getTableVO("accrued_detail"));
		return billVO;
	}

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_accrued_bill");
		String condition1 = condition.toString().replace("PPK", "pk_accrued_bill");
		String sql1 = "select a.*\n" + "  from er_accrued_detail a  \n" + "  where isnull(a.dr, 0) = 0\n" + "   " + condition1 + "\n" + " order by a.rowno ;";
		ArrayList<AccruedDetailVO> list1 = (ArrayList<AccruedDetailVO>) dao.executeQuery(sql1, new BeanListProcessor(AccruedDetailVO.class));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		if (list1 != null && list1.size() > 0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list1.toArray(new AccruedDetailVO[0]));
			String[] formulas = new String[] { "pk_iobsclass_name->getcolvalue(bd_inoutbusiclass,name,pk_inoutbusiclass,pk_iobsclass)" };
			PubTools.execFormulaWithVOs(maps, formulas);
			BillVO billVO = new BillVO();
			billVO.setTableVO("accrued_detail", maps);
			billVOs[0] = billVO;
			qbillVO.setQueryVOs(billVOs);
			return qbillVO;
		}
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");		
		String condition1 = condition.toString().replace("PPK", "er_accrued").replace("pk_org.", "");
		if (condition1.contains("accrued.billdate")) {
			condition1 = condition1.toString().replace("accrued.billdate", "substring(accrued.billdate,0,11)");
		}
		// ��ѯsql��䲢���з�ҳ
		String sql = "select *\n" +
				"            from (select rownum rowno, a.*\n" + 
				"                    from (select *\n" + 
				"                            from er_accrued head\n" + 
				"                           where nvl(dr, 0) = 0\n" + 
				"                             and creator = '"+userid+"'\n" + 
				"\n" + 
				"\n" + 
				"                           order by ts desc) a)\n" + 
				"           where rowno between '"+startnum+"' and '"+endnum+"';";
		BaseDAO dao = new BaseDAO();
		@SuppressWarnings("unchecked")
		ArrayList<AccruedVO> list = (ArrayList<AccruedVO>) dao.executeQuery(sql, new BeanListProcessor(AccruedVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new AccruedVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			// ����״̬����
			// djzt����״̬ 0=�ݴ棬1=���棬2=��ˣ�3=ǩ�֣�-1=���ϣ�
			if (null == maps[i].get("apprstatus") || "".equals(maps[i].get("apprstatus")) || -1 == Integer.parseInt(maps[i].get("apprstatus").toString())) {
				maps[i].put("ibillstatus", -1);// ����
			} else if (null != maps[i].get("apprstatus") && 3 == Integer.parseInt(maps[i].get("apprstatus").toString())) {
				maps[i].put("ibillstatus", 3);// �ύ
			} else {
				maps[i].put("ibillstatus", 1);// ����ͨ��
			}
			maps[i].put("billtype", 30);
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String action = super.getAction();
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_accrued_bill");
		String condition1 = condition.toString().replace("PPK", "pk_accrued_bill");
		//Ԥ����ϸ
		String sql1 = "select a.*\n" + "  from er_accrued_detail a  \n" + "  where isnull(a.dr, 0) = 0\n" + "   " + condition1 + "\n" + " order by a.rowno ;";
		//����Ԥ����ϸ
		String sql2 = "select a.*\n" + "  from er_accrued_verify a  \n" + "  where isnull(a.dr, 0) = 0\n" + "   " + condition1 ;

		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		
		//Ԥ����ϸ
		if (action.contains("accrued_detail")) {
			ArrayList<AccruedDetailVO> list1 = (ArrayList<AccruedDetailVO>) dao.executeQuery(sql1, new BeanListProcessor(AccruedDetailVO.class));
			if (list1 != null && list1.size() > 0) {
				HashMap<String, Object>[] maps = transNCVOTOMap(list1.toArray(new AccruedDetailVO[0]));
				String[] formulas = new String[] { "pk_iobsclass_name->getcolvalue(bd_inoutbusiclass,name,pk_inoutbusiclass,pk_iobsclass)" };
				PubTools.execFormulaWithVOs(maps, formulas);
				billVO.setTableVO("accrued_detail", maps);
				billVOs[0] = billVO;
				qbillVO.setQueryVOs(billVOs);
				return qbillVO;
			}
		}
		//����Ԥ����ϸ AccruedVerifyVO
		if(action.contains("accrued_verify")){
			ArrayList<AccruedVerifyVO> list2 = (ArrayList<AccruedVerifyVO>) dao.executeQuery(sql2, new BeanListProcessor(AccruedVerifyVO.class));
			if(null != list2 && list2.size() > 0){
				HashMap<String, Object>[] maps = transNCVOTOMap(list2.toArray(new AccruedVerifyVO[0]));
				billVO.setTableVO("accrued_verify", maps);
				billVOs[0] = billVO;
				qbillVO.setQueryVOs(billVOs);
				return qbillVO;
			}
		}
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_accrued_bill");
		AggAccruedBillVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAccruedBillVO.class, csaleorderid, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		Object o = pf.processAction("DELETE", "2621", null, aggvos, new AggAccruedBillVO[] { aggvos }, null);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_accrued_bill");
		AggAccruedBillVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAccruedBillVO.class, csaleorderid, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		Object o = pf.processAction("SAVE", "2621", null, aggvos, aggvos, null);
		return queryBillVoByPK(userid, csaleorderid);
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object unsavebill(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String csaleorderid = (String) bill.getHeadVO().get("pk_accrued_bill");
		AggAccruedBillVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggAccruedBillVO.class, csaleorderid, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		Object o = pf.processAction("UNSAVE", "2621", null, aggvos, new AggAccruedBillVO[] { aggvos }, null);
		return queryBillVoByPK(userid, csaleorderid);
	}
}