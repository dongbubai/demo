package hd.bs.mobile.approve;

import hd.vo.muap.approve.DispatchListVO;
import hd.vo.muap.approve.MApproveListVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.QueryBillVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BeanHelper;
import nc.vo.pub.BusinessException;

public class WorkNoteAction extends MApproveWorkAction {

	public WorkNoteAction() {
		super();
	}

	@Override
	public DispatchListVO queryDispatchVOs(String userid, Object obj) throws BusinessException {
		MApproveVO appVO = (MApproveVO) obj;
		// 针对，直接费用，间接费用的 审批前选择检查
		return super.queryDispatchVOs(userid, obj);
	}

	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		QueryBillVO querybillvo = new QueryBillVO();

		MApproveListVO listvo = queryApproveList(userid, obj);
		if (listvo == null) {
			return null;
		}
		MApproveVO[] mapvos = listvo.getApproveVOs();
		ArrayList<BillVO> billvolist = new ArrayList<BillVO>();
		for (int i = 0; i < mapvos.length; i++) {
			MApproveVO mvo = mapvos[i];
			BillVO billvo = new BillVO();
			HashMap<String, Object> headVO = new HashMap<String, Object>();
			List<String> list = BeanHelper.getPropertys(mvo);
			for (String str : list) {
				headVO.put(str, BeanHelper.getProperty(mvo, str));
			}
			billvo.setHeadVO(headVO);
			billvolist.add(billvo);
		}

		querybillvo.setQueryVOs(billvolist.toArray(new BillVO[billvolist.size()]));
		return querybillvo;
	}

	@Override
	protected String[] getTableCode(String billtype, AggregatedValueObject billvo) {
		BaseDAO dao = new BaseDAO();
		//单据交易类型编码
		String sql = "select distinct djlxbm from er_djlx where nvl(dr,0)=0";
		try {
			List<String> bxlist = (List<String>) dao.executeQuery(sql, new ArrayListProcessor());
			if (bxlist.contains(billtype)) {
				sql = " select distinct table_code from pub_billtemplet_b " +
					  " where pk_billtemplet in (select pk_billtemplet from pub_billtemplet where metadataclass like 'erm%')";
				String[] code = (String[]) dao.executeQuery(sql, new ArrayProcessor());
				return code;
			}
		} catch (DAOException e) {
			e.printStackTrace();
		}
//		List<String> bxlist = Arrays.asList("264X-Cxx-01","264X-Cxx-02","264X-Cxx-03","264X-Cxx-04","2647","2641" ,"263X-Cxx-01","263X-Cxx-02","2611",
//				"264X-Cxx-05","264X-Cxx-06","264X-Cxx-ZDTJBX"); 
//		if (bxlist.contains(billtype)) {
//			// er_busitem, er_bxcontrast, costsharedetail, accrued_verify
//			return new String[] { "costsharedetail","bx_cshare_detail","arap_bxbusitem","other","er_cshare_detail", "er_bxcontrast",
//					"jk_busitem", "accrued_verify" , "mtapp_detail"
//				};
//		} 
		return super.getTableCode(billtype, billvo);
	}
}
