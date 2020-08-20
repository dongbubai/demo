/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.ta.ISignCardApplyApproveManageMaintain;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.formulaset.FormulaParseFather;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.signcard.AggSignVO;
import nc.vo.ta.signcard.SignbVO;
import nc.vo.ta.signcard.SignhVO;
/**
 * @author LILY  签卡申请
 *
 */
public class QkApplyAction  extends AbstractMobileAction {
	private BaseDAO baseDAO;
	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}
	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		return null;
	}
	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		return super.processAction(account, userid, billtype, action, obj);
	}
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		AggSignVO aggvo = saveaggvo(userid,obj);
		String pk = aggvo.getParentVO().getPrimaryKey();
		return queryBillVoByPK(userid, pk);
	}
	public AggSignVO saveaggvo(String userid, Object obj) throws BusinessException {
		BillVO billvo = (BillVO) obj;
		HashMap<String, Object> head = billvo.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodyVOsMap = billvo.getBodyVOsMap();
		HashMap<String, Object>[] bodyvos = bodyVOsMap.get("signb_sub");
		if (null == bodyvos || bodyvos.length == 0) {
			throw new BusinessException("表体没有数据！或者表体数据没有修改！");
		}
		SignbVO[] body = new SignbVO[bodyvos.length];
		int hstatus = Integer.parseInt(head.get("vostatus").toString());
		SignhVO headvo =(SignhVO) transMapTONCVO(SignhVO.class, head);
		if(hstatus==VOStatus.NEW){
			String vcode = NCLocator.getInstance().lookup(IHrBillCode.class).getBillCode("6402", headvo.getPk_group(),headvo.getPk_org());
			headvo.setBill_code(vcode);
			headvo.setBillmaker(userid);
			headvo.setStatus(hstatus);
			headvo.setCreationtime(new UFDateTime(new Date()));
			headvo.setIshrssbill(UFBoolean.TRUE);
			headvo.setApprove_state(-1);
			String[] formulas = new String[1];
			formulas[0] = "getcolvalue( hi_psnjob, pk_psnorg, pk_psnjob,\""+ headvo.getPk_psnjob() + "\" ) ";
			FormulaParseFather m_formulaParse = new nc.bs.pub.formulaparse.FormulaParse();
			m_formulaParse.setExpressArray(formulas);
			Object[][] values = m_formulaParse.getValueOArray();
			String pk_psnorg= values[0][0].toString();
			headvo.setPk_psnorg(pk_psnorg);
			for(int i=0;i<bodyvos.length;i++){
				SignbVO bodyvo =(SignbVO) transMapTONCVO(SignbVO.class,bodyvos[i]);
				int bstatus=Integer.parseInt(bodyvos[i].get("vostatus").toString());
				bodyvo.setPk_psndoc(headvo.getPk_psndoc());
				bodyvo.setPk_psnjob(headvo.getPk_psnjob());
				bodyvo.setSignstatus(2);
				bodyvo.setDate(new UFLiteralDate(new Date()));
				bodyvo.setPk_psnorg(pk_psnorg);
				bodyvo.setSigndate(headvo.getApply_date());
				bodyvo.setStatus(bstatus);
				body[i]=bodyvo;
			}
		}else{
			headvo.setCreator(userid);
			headvo.setCreationtime(new UFDateTime(new Date()));
			headvo.setStatus(hstatus);
			headvo.setBillmaker(userid);
			headvo.setStatus(hstatus);
			for(int i=0;i<bodyvos.length;i++){
				SignbVO bodyvo =(SignbVO) transMapTONCVO(SignbVO.class,bodyvos[i]);
				int bstatus=Integer.parseInt(bodyvos[i].get("vostatus").toString());
				bodyvo.setPk_psnorg(headvo.getPk_psnorg());
				bodyvo.setDate(new UFLiteralDate(new Date()));
				bodyvo.setSigndate(headvo.getApply_date());
				bodyvo.setPk_signh(headvo.getPk_signh());
				bodyvo.setSignstatus(2);
				bodyvo.setStatus(bstatus);
				body[i]=bodyvo;
			}
		}
		AggSignVO aggvo = new AggSignVO();
		aggvo.setParentVO(headvo);
		aggvo.setChildrenVO(body);
		String flag = head.get("checkpassflag")==null?null:head.get("checkpassflag").toString();
		ISignCardApplyApproveManageMaintain impl = NCLocator.getInstance().lookup(ISignCardApplyApproveManageMaintain.class); 
		if(hstatus==VOStatus.NEW){
			int sign = signcounts(headvo.getPk_org());
			if(!"true".equals(flag)){
				if(bodyvos.length > sign && sign!=0){
					throw new BusinessException("SELECT签卡次数"+bodyvos.length+"次，超过规定签卡次数"+sign+"次");
				}
			}
			impl.insertData(aggvo);
		}else{
			int sign = signcounts(headvo.getPk_org());
			int count = savesign(headvo.getPk_signh())+bodyvos.length;
			if(!"true".equals(flag)){
				if(count>sign && sign!=0){
					throw new BusinessException("SELECT签卡次数"+count+"次，超过规定签卡次数"+sign+"次");
				}
			}
			impl.updateData(aggvo);
		}
		
		return aggvo;
	}
	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		return null;
	}
	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_signh");
		String sql = "select * from (select rownum rowno,tbm_signb.* from tbm_signb where nvl(dr,0)=0 " + condition1 + " )  order by ts ";
		@SuppressWarnings("unchecked")
		ArrayList<SignbVO> list = (ArrayList<SignbVO>) getQuery().executeQuery(sql, new BeanListProcessor(SignbVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new SignbVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("signb_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("pk_corp", "pk_org");
		condition = condition.replace("PPK", "pk_signh");
		String sql = "select * from (select a.*,rownum rownu from (select * from tbm_signh a where nvl(dr,0)=0"
				+condition+" and (billmaker = '"+userid+"')  order by ts desc) a )where rownu between "+startnum+" and "+endnum;
		ArrayList<SignhVO> list =(ArrayList<SignhVO>) getQuery().executeQuery(sql, new BeanListProcessor(SignhVO.class));
		if(list == null || list.size() == 0){
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new SignhVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for(int i=0;i<maps.length;i++){
			BillVO billVO = new BillVO();
			HashMap<String,Object> headVO = maps[i];
			if (list.get(i).getApprove_state() == null||"".equals(list.get(i).getApprove_state())||"-1".equals(list.get(i).getApprove_state().toString())) {
				headVO.put("ibillstatus", "-1");
			} else if ("0".equals(list.get(i).getApprove_state().toString())) {
				headVO.put("ibillstatus", "0");
			} else if ("1".equals(list.get(i).getApprove_state().toString())) {
				headVO.put("ibillstatus", "1");
			} else if ("2".equals(list.get(i).getApprove_state().toString())) {
				headVO.put("ibillstatus", "2");
			} else {
				headVO.put("ibillstatus", "3");
			}
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("PPK", "pk_signh");
		String sql = "select * from tbm_signb where nvl(dr,0)=0 "+condition;
		ArrayList<SignbVO> list =(ArrayList<SignbVO>) getQuery().executeQuery(sql, new BeanListProcessor(SignbVO.class));
		if(list == null || list.size() == 0){
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new SignbVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		BillVO billVO = new BillVO();
		billVO.setTableVO("signb_sub", maps);
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_signh = (String) bill.getHeadVO().get("pk_signh");
		AggSignVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggSignVO.class, pk_signh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("DELETE", "6402", new AggSignVO[]{aggvos}, null, null, null);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_signh = (String) bill.getHeadVO().get("pk_signh");
		AggSignVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggSignVO.class, pk_signh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("SAVE", "6402", new AggSignVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_signh);
	}

	/**
	 * 根据pk 查询出BilVO
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
		QueryBillVO body_querybillVO = (QueryBillVO) queryNoPage_body(userid, condAggVO_body);
		if(null!=body_querybillVO){
			BillVO body_BillVO = body_querybillVO.getQueryVOs()[0];
			billVO.setTableVO("signb_sub", body_BillVO.getTableVO("signb_sub"));
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_signh = (String) bill.getHeadVO().get("pk_signh");
		AggSignVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggSignVO.class, pk_signh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", "6402", new AggSignVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_signh);
	}

	@Override
	public Object unsavebill(String userid, Object obj)throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_signh = (String) bill.getHeadVO().get("pk_signh");
		AggSignVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggSignVO.class, pk_signh, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("RECALL", "6402", new AggSignVO[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_signh);
	}
	/**
	 * 考勤规则签卡次数上限
	 */
	public int signcounts(String pk_org) throws DAOException{
		int signcounts = 0;
		String sql = "select signcounts from tbm_timerule where pk_org = '"+pk_org+"' and nvl(dr,0) = 0";
		Object[] sign =(Object[]) getQuery().executeQuery(sql, new ArrayProcessor());
		if(sign[0]!=null){
			signcounts = Integer.parseInt(sign[0].toString().trim());
		}
		return signcounts;
	}
	/**
	 * 保存时签卡次数
	 * @throws DAOException 
	 */
	public int savesign(String pk) throws DAOException{
		int sign = 0;
		String sql = "select count(pk_signb) from tbm_signb where pk_signh = '"+pk+"' and nvl(dr,0) = 0";
		Object[] sasign =(Object[]) getQuery().executeQuery(sql, new ArrayProcessor());
		if(sasign[0]!=null){
			sign = Integer.parseInt(sasign[0].toString().trim());
		}
		return sign;
	}
}
