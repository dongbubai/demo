package hd.bs.mobile.approve;

import hd.bs.muap.approve.ApproveWorkAction;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.itf.muap.pub.IMobileAction;
import hd.itf.muap.pub.IMobileBusiAction;
import hd.muap.bs.delegate.BusinessDelegator;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;

import java.util.ArrayList;
import java.util.Hashtable;

import nc.bs.dao.BaseDAO;
import nc.vo.muap.muap05.BillHVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.trade.pub.IExAggVO;

public class MApproveWorkAction extends ApproveWorkAction {

	public MApproveWorkAction() {
	}

	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		if (IMobileAction.APPROVE.equals(action) || IMobileAction.UNAPPROVE.equals(action)) {
			// 针对审批弃审，进行判断，如果有对应的注册类，则走注册类
			// 读取单据配置信息
			String pk_billtype = null;
			String workflowid = null;
			if (IMobileAction.UNAPPROVE.equals(action)) {
				MApproveVO unainfoVO = (MApproveVO) obj;
				pk_billtype = unainfoVO.getPk_billtype();
				workflowid = unainfoVO.getWorkflowid();
			} else {
				ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
				pk_billtype = ainfoVO.getPk_billtype();
				workflowid = ainfoVO.getWorkflowid();
			}

			BillHVO configHVO = null;
			String strWhere = "vmobilebilltype='" + pk_billtype + "' and isnull(dr,0)=0";
			BaseDAO dao = new BaseDAO();
			ArrayList<SuperVO> list = (ArrayList<SuperVO>) dao.retrieveByClause(BillHVO.class, strWhere);
			if (list != null && list.size() > 0) {
				configHVO = (BillHVO) list.get(0);
			}

			if (configHVO != null && configHVO.getVbusiactionclass() != null && configHVO.getVbusiactionclass().trim().length() > 0) {
				try {
					IMobileBusiAction busiAction = (IMobileBusiAction) Class.forName(configHVO.getVbusiactionclass()).newInstance();
					Object busireturn = busiAction.processAction(account, userid, billtype, action, obj);
					String update_message = "update muap_message set imsgstatus='2' where pk_message ='" + workflowid + "' and isnull(dr,0)=0";
					dao.executeUpdate(update_message);
					return busireturn;
				} catch (Exception e) {
					String messge=e.getMessage();
					if(messge!=null && messge.contains("下级审批")){
						messge="审批失败:当前单据没有找到下级审批人!";
					} 
					throw new BusinessException(messge);	
				}
			}
		}
		return super.processAction(account, userid, billtype, action, obj);
	}

	@Override
	protected void dealQueryData(AggregatedValueObject billvo, String billtype) throws BusinessException {
		/**
		 * 永昌路桥集团，测试处理
		 */
		if (billvo instanceof IExAggVO && (billtype.equals("992E") || billtype.equals("992B"))) {
			if (((IExAggVO) billvo).getAllChildrenVO() != null && ((IExAggVO) billvo).getAllChildrenVO().length > 0) {
				return;
			}
			String[] tableCodes = ((IExAggVO) billvo).getTableCodes();
			String key = billvo.getParentVO().getPrimaryKey();
			Hashtable htable = getBusinessDelegator(billtype).loadChildDataAry(tableCodes, key);
			for (int i = 0; i < tableCodes.length; i++) {
				((IExAggVO) billvo).setTableVO(tableCodes[i], (SuperVO[]) htable.get(tableCodes[i]));
			}
		}
	}

	protected BusinessDelegator getBusinessDelegator(String billtype) throws BusinessException {
		/*
		 * if(billtype.equals("992E")){ return new EqContractDelegator(); }else
		 * if(billtype.equals("992B")){ return new CMContractDelegator(); }else{
		 * throw new BusinessException("没有业务代理类！"); }
		 */
		return null;
	}

	@Override
	protected AggregatedValueObject queryBillVOByPrimaryKey(String busiclassaction, Object userObj, String billtype, String billid) throws BusinessException {
		if (busiclassaction != null && busiclassaction.trim().length() > 0) {
			try {
				Object busiClass = Class.forName(busiclassaction).newInstance();
				if (busiClass instanceof AbstractMobileAction) {
					return ((AbstractMobileAction) busiClass).queryBillVOByPrimaryKey(userObj, billtype, billid);
				}
			} catch (Exception e) {
				throw new BusinessException(e);
			}
		}
		return super.queryBillVOByPrimaryKey(busiclassaction, userObj, billtype, billid);
	}

}
