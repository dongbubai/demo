package hd.bs.bill.bxfee;

import hd.bs.muap.file.AttachmentAction;
import hd.bs.muap.pub.DefaultBillAction;
import hd.muap.pub.tools.PubTools;
import hd.vo.muap.pub.AttachmentListVO;
import hd.vo.muap.pub.AttachmentVO;
import hd.vo.muap.pub.BillVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.muap.muap05.BillyqBVO;
import nc.vo.pub.BusinessException;

/**
 * 审批联查上游单据 单据默认实现
 * @author zhouwq
 */
public class QueryLinkBillAction extends DefaultBillAction {

	public QueryLinkBillAction() {
	}

	static BaseDAO dao = new BaseDAO();

	/**
	 * 获取联查的单据ID，如果存在多个自定义按钮通过单据ID进行联查，可以通过getAction获取动作编码，进行判断返回不同的单据ID。
	 *  示例：
	 * if(getAction().equals("Action1")){
			return "id1";
		}else if(getAction().equals("Action2")){
			return "id2";
		}
	 */
	@Override
	protected String getlinkBillID(Object obj) throws BusinessException{
		//查询出代办里的 所有名字是联查申请的按钮
		String sql =" select btncode\n" +
					"  from SM_BUTNREGISTER\n" + 
					"  where parent_id in (select cfunid from sm_funcregister )\n" + 
					"  and nvl(dr, 0) = 0\n" + 
					"  and btnname = '联查申请';";

		ArrayList<Object[]> btncodeList = (ArrayList<Object[]>) dao.executeQuery(sql,new ArrayListProcessor());
		String str = "";
		if(null != btncodeList && btncodeList.size() > 0){
			for(int i = 0;i<btncodeList.size();++i){
				str = str+ btncodeList.get(i)[0].toString()+ ",";
			}
		}

		if(str.contains(getAction())){

			BillVO vo = (BillVO)obj;
			HashMap<String, HashMap<String, Object>[]> bodyMap = vo.getBodyVOsMap();
			HashMap<String, Object>[] bxbusitem = null; 
			if(getAction().startsWith("263")){
				bxbusitem = bodyMap.get("jk_busitem");
			}
			if(getAction().startsWith("264")){
				bxbusitem = bodyMap.get("arap_bxbusitem");
			}
			Object pk_item = "";
			for(int i=0; i<bxbusitem.length; i++){
				pk_item = bxbusitem[i].get("pk_item");
			}
			if(!PubTools.isNull(pk_item)){
				return pk_item.toString();
			}else{
				throw new BusinessException("没有数据");
			}
		}
		return null;
	}

	/**
	 * 获取联查的单据类型，如果存在多个自定义按钮联查，可以通过getAction获取动作编码，进行判断返回不同的联查单据类型。
	 * 示例：
	 * if(getAction().equals("Action1")){
			return "type1";
		}else if(getAction().equals("Action2")){
			return "type2";
		}
	 */
	@Override
	protected String getLinkBillType(Object obj)throws BusinessException{
		//dongl 修改 
		BillVO vo = (BillVO)obj;
		//优化根据 obj查出单据类型
		if(null != vo.getHeadVO().get("srcbilltype") && !"".equals(vo.getHeadVO().get("srcbilltype"))){
			return  vo.getHeadVO().get("srcbilltype").toString();
		}else{
			throw new BusinessException("联查单据不存在！");
		}
	}
	
	
	/**
	 * 自定义事件处理
	 */
	@Override
	public Object defaction(String userid, Object obj) throws BusinessException {
		String action = getAction();
		String billtype = getBilltype();
		/**
		 * 功能注册的待办、已办节点注册  交易类型.DEFFILEFP按钮
		 * 单据设置的审批节点子表填写【参照数据】为【发票号】
		 * 功能注册 按钮 审批 2641.DEFFILEFP   发票附件  deffilefp.DOWNLOAD  下载     deffilefp.PREVIEW  预览
		 */
		if (action.contains("DEFFILEFP")) {
			//根据当前单据功能注册编码查找单据设置节点，子表的【参照数据】为【发票号】的字段名称的值，即发票pk
			String funcode = super.getBilltype();
			String sql ="select muap_billyq_b.itemcode\n" +
						"from muap_bill_h\n" + 
						"join muap_billyq_b on muap_billyq_b.pk_billconfig_h=muap_bill_h.pk_billconfig_h and isnull(muap_billyq_b.dr,0)=0\n" + 
						"where vmobilebilltype='"+funcode+"'\n" + 
						"and trim(refdata)='发票号'" +
						" and isnull(muap_bill_h.dr,0)=0  ";
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
						Object oVO = aac.processAction(null,userid,billtype,"QUERY", avo);
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
						Object oVO = aac.processAction(null,userid,billtype,"QUERY", avo);
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
		return obj;
	}

}
