/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import nc.bs.dao.BaseDAO;
import hd.bs.muap.pub.AbstractMobileAction;
import hd.vo.muap.pub.BillVO;
import nc.uap.lfw.core.crud.CRUDHelper;
import nc.uap.lfw.core.crud.ILfwCRUDService;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
/**
 * @author LILY  离职申请
 *
 */
public class LzApplyAction  extends AbstractMobileAction {
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
		
				return null;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryNoPage_body(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		return null;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		return null;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		return null;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		return null;
	}

	/**
	 * 根据pk 查询出BilVO
	 */
	public BillVO queryBillVoByPK(String userid, String pk) throws BusinessException {
		return null;
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
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}
}
