package nc.itf.mobile;

import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.vo.mobile.record.AggRecord;
import nc.vo.pub.BusinessException;

public interface IRecordMaintain {

	public void delete(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] insert(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] update(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] query(IQueryScheme queryScheme)
			throws BusinessException;

	public AggRecord[] save(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] unsave(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] approve(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;

	public AggRecord[] unapprove(AggRecord[] clientFullVOs,
			AggRecord[] originBills) throws BusinessException;
}
