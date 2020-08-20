package hd.bs.mobile.business;

import hd.itf.muap.pub.IMobileBusiAction;

import java.util.ArrayList;
import java.util.HashMap;

import nc.bcmanage.bs.IBusiCenterManageService;
import nc.bcmanage.vo.BusiCenterVO;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.vo.muap.muap05.BillHVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;

/**
 * 
 * @author jinfei 2015-06-04
 * 
 *         移动应用业务实现公共
 * 
 */
public class MobileBusiAction implements IMobileBusiAction {

	private static HashMap<String, IMobileBusiAction> busiactionMap = new HashMap<String,IMobileBusiAction>();

	public MobileBusiAction() {
	}

	// 根据单据类型，判断走相应的业务分支
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {

		if (account != null && !"OFFLINE".equals(account)) {
			IBusiCenterManageService serv = (IBusiCenterManageService) NCLocator.getInstance().lookup(IBusiCenterManageService.class);
			BusiCenterVO bcVO = serv.getBusiCenterByCode(account); // 根据账套编码，查出账套信息
			InvocationInfoProxy.getInstance().setBizCenterCode(account);
			InvocationInfoProxy.getInstance().setUserDataSource(bcVO.getDataSourceName());
		}

		if (userid != null && userid.trim().length() != 0) {
			InvocationInfoProxy.getInstance().setUserId(userid);
			InvocationInfoProxy.getInstance().setUserCode(userid);
		} else {
			InvocationInfoProxy.getInstance().setUserCode("#MOBILE#");
		}

		return getAction(billtype).processAction(account, userid, billtype, action, obj);
	}

	/**
	 * 根据单据类型返回相应的处理类，处理类需要实现 IMobileBusiAction 接口
	 * 
	 * @param billtype
	 * @return
	 * @throws BusinessException
	 */
	private IMobileBusiAction getAction(String billtype) throws BusinessException {
		if(!busiactionMap.containsKey(billtype)){
			BaseDAO dao = new BaseDAO();
			ArrayList<SuperVO> list = (ArrayList<SuperVO> )dao.retrieveByClause(BillHVO.class, "vmobilebilltype='"+billtype+"' and isnull(dr,0)=0 ");
			if(list!=null&&list.size()>0){
				BillHVO billhVO = (BillHVO)list.get(0);
				if(billhVO.getVbusiactionclass()!=null&&billhVO.getVbusiactionclass().trim().length()>0){
					try {
						busiactionMap.put(billtype, (IMobileBusiAction)(Class.forName(billhVO.getVbusiactionclass()).newInstance()));
					} catch (Exception e) {
						throw new BusinessException(e.getMessage());
					}
				}else{
					throw new BusinessException("移动单据类型：" + billtype + " 未注册业务处理类！");
				}
			}else{
				throw new BusinessException("移动单据类型：" + billtype + " 在【单据设置】未注册！");
			}
		}

		return busiactionMap.get(billtype);

		/*	*//**
		 * 基本 的 动作处理
		 *//*
		if (billtype.equals(IMobileBillType.USER)) { // 用户
			return new hd.bs.muap.login.LoginAction();
		} else if (billtype.equals(IMobileBillType.PRODUCT)) { // 产品-1
			return new hd.bs.muap.login.ProductAction();
		} else if (billtype.equals(IMobileBillType.MODULE)) { // 模块-2
			return new hd.bs.muap.login.ModuleAction();
		} else if (billtype.equals(IMobileBillType.MENU)) { // 菜单-3
			return new hd.bs.muap.login.MenuAction();
		} else if (billtype.equals(IMobileBillType.FUNC)) { // 功能-4
			return new hd.bs.muap.login.FuncAction();
		} else if (billtype.equals(IMobileBillType.BTNREG)) { // 按钮注册
			return new hd.bs.muap.login.ButtonRegAction();
		} else if (billtype.equals(IMobileBillType.BTNPOWER)) { // 按钮
			return new hd.bs.muap.login.ButtonPowerAction();
		} else if (billtype.equals(IMobileBillType.FORMULA)) { // 公式
			return new hd.bs.muap.formula.FormulaAction();
		} else if (billtype.equals(IMobileBillType.BILLTEMPLET)) { // 单据模板
			return new hd.bs.muap.login.BillTempletAction();
		} else if (billtype.equals(IMobileBillType.BDOC)) { // 基础档案
			return new hd.bs.mobile.bdoc.BdocAction();
		} else if (billtype.equals(IMobileBillType.RSA)) {
			return new hd.bs.muap.rsa.RSAAction();
		} else if (billtype.equals(IMobileBillType.APWORK)) {
			return new hd.bs.muap.approve.ApproveWorkAction();
		} else if (billtype.equals(IMobileBillType.ACCOUNT)) { // 账套
			return new hd.bs.muap.login.AccountAction();
		} else if (billtype.equals(IBusiBillType.HM02) || billtype.equals(IBusiBillType.HM01)) { // 考勤记录
			return new hd.bs.mobile.attendance.AttenceRecordAction();
		} else if (billtype.equals(IBusiBillType.HM03)) { // 考勤补签
			return new hd.bs.mobile.attendance.SubscribeRecordAction();
		} else if (billtype.equals(IBusiBillType.HM04)) { // 考勤统计
			return new hd.bs.mobile.attendance.AttenceCountAction();
		} else if (billtype.equals(IMobileBillType.BTRACE) || billtype.equals("HMH2")) { // 考勤统计
			return new hd.bs.mobile.browsetrace.BrowseTraceAction();
		}
		//客户联系人
		else if (billtype.equals(IBusiBillType.MUJ00209)){
			return new nc.bs.mobile.CRMmanager.ContactAction();
		}
		  *//**
		  * 查询全部菜单（供UM使用）
		  *//*
		else if (billtype.equals(IMobileBillType.ALLMENU)) {
			return new hd.bs.muap.login.AllMenuAction();
		} else {
			throw new BusinessException("未找到单据类型：" + billtype + " 对应的动作处理类！");
		}*/
	}
}
