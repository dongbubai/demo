package hd.bs.mobile.invoice;

import hd.vo.muap.pub.BillVO;

import java.util.ArrayList;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;

/**
 * 发票票夹处理类-税务系统标准发票
 */
public class StandardInvoiceAction extends hd.bs.muap.invoice.action.InvoiceAction {
	
	public StandardInvoiceAction() {
	}

	private BaseDAO baseDAO;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}
	
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		if (null != bill.getHeadVO().get("pk_fpgl")) {
			String pk_fpgl = bill.getHeadVO().get("pk_fpgl").toString();
			String sql = "select * from erm_fpgl where nvl(dr,0)=0 and billversionpk='Y' and pk_fpgl='"+pk_fpgl+"' ";
			List<nc.vo.erm.fppj.FpglVO> hfpvo = (List<nc.vo.erm.fppj.FpglVO>) getQuery().executeQuery(sql, new BeanListProcessor(nc.vo.erm.fppj.FpglVO.class));
			
			StringBuffer fphm = new StringBuffer();
			for(int i=0; i<hfpvo.size(); i++){
				fphm.append(hfpvo.get(i).getFphm()).append(",");
			}
			if (hfpvo != null && hfpvo.size() > 0) {
				throw new BusinessException("该发票号"+fphm+"，已报销不可删除！");
			} else {
				//getQuery().deleteByPK(nc.vo.erm.fppj.FpglVO.class, pk_fpgl);
				String sqlh = "update erm_fpgl set dr = 1 where pk_fpgl  = '"+pk_fpgl+"' and nvl(dr,0) = 0";
				String sqlb = "update erm_fpxx set dr = 1 where pk_fpgl  = '"+pk_fpgl+"' and nvl(dr,0) = 0";
				getQuery().executeUpdate(sqlh);
				getQuery().executeUpdate(sqlb);
			}
		}
		return null;
	}
}
