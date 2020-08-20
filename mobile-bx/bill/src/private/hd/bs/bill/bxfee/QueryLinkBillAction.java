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
 * �����������ε��� ����Ĭ��ʵ��
 * @author zhouwq
 */
public class QueryLinkBillAction extends DefaultBillAction {

	public QueryLinkBillAction() {
	}

	static BaseDAO dao = new BaseDAO();

	/**
	 * ��ȡ����ĵ���ID��������ڶ���Զ��尴ťͨ������ID�������飬����ͨ��getAction��ȡ�������룬�����жϷ��ز�ͬ�ĵ���ID��
	 *  ʾ����
	 * if(getAction().equals("Action1")){
			return "id1";
		}else if(getAction().equals("Action2")){
			return "id2";
		}
	 */
	@Override
	protected String getlinkBillID(Object obj) throws BusinessException{
		//��ѯ��������� ������������������İ�ť
		String sql =" select btncode\n" +
					"  from SM_BUTNREGISTER\n" + 
					"  where parent_id in (select cfunid from sm_funcregister )\n" + 
					"  and nvl(dr, 0) = 0\n" + 
					"  and btnname = '��������';";

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
				throw new BusinessException("û������");
			}
		}
		return null;
	}

	/**
	 * ��ȡ����ĵ������ͣ�������ڶ���Զ��尴ť���飬����ͨ��getAction��ȡ�������룬�����жϷ��ز�ͬ�����鵥�����͡�
	 * ʾ����
	 * if(getAction().equals("Action1")){
			return "type1";
		}else if(getAction().equals("Action2")){
			return "type2";
		}
	 */
	@Override
	protected String getLinkBillType(Object obj)throws BusinessException{
		//dongl �޸� 
		BillVO vo = (BillVO)obj;
		//�Ż����� obj�����������
		if(null != vo.getHeadVO().get("srcbilltype") && !"".equals(vo.getHeadVO().get("srcbilltype"))){
			return  vo.getHeadVO().get("srcbilltype").toString();
		}else{
			throw new BusinessException("���鵥�ݲ����ڣ�");
		}
	}
	
	
	/**
	 * �Զ����¼�����
	 */
	@Override
	public Object defaction(String userid, Object obj) throws BusinessException {
		String action = getAction();
		String billtype = getBilltype();
		/**
		 * ����ע��Ĵ��졢�Ѱ�ڵ�ע��  ��������.DEFFILEFP��ť
		 * �������õ������ڵ��ӱ���д���������ݡ�Ϊ����Ʊ�š�
		 * ����ע�� ��ť ���� 2641.DEFFILEFP   ��Ʊ����  deffilefp.DOWNLOAD  ����     deffilefp.PREVIEW  Ԥ��
		 */
		if (action.contains("DEFFILEFP")) {
			//���ݵ�ǰ���ݹ���ע�������ҵ������ýڵ㣬�ӱ��ġ��������ݡ�Ϊ����Ʊ�š����ֶ����Ƶ�ֵ������Ʊpk
			String funcode = super.getBilltype();
			String sql ="select muap_billyq_b.itemcode\n" +
						"from muap_bill_h\n" + 
						"join muap_billyq_b on muap_billyq_b.pk_billconfig_h=muap_bill_h.pk_billconfig_h and isnull(muap_billyq_b.dr,0)=0\n" + 
						"where vmobilebilltype='"+funcode+"'\n" + 
						"and trim(refdata)='��Ʊ��'" +
						" and isnull(muap_bill_h.dr,0)=0  ";
			ArrayList<BillyqBVO> mupqb = (ArrayList<BillyqBVO>) dao.executeQuery(sql, new BeanListProcessor(BillyqBVO.class));
			if(null!=mupqb && mupqb.size()>0){
				BillVO bill = (BillVO) obj;
				HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
				HashMap<String, Object>[] bxbusitem = bodys.get("arap_bxbusitem");//��ͨ����
				HashMap<String, Object>[] other = bodys.get("other");//��������
				AttachmentAction aac = new AttachmentAction();
				ArrayList<AttachmentVO> raVOList = new ArrayList<AttachmentVO>();
				for(int i=0; null!=bxbusitem && i<bxbusitem.length; i++){
					AttachmentVO avo = new AttachmentVO();
					if(null!=bxbusitem[i].get(mupqb.get(0).getItemcode())){
						avo.setFileroot(bxbusitem[i].get(mupqb.get(0).getItemcode()).toString());
						//���ݷ�Ʊpk����ѯ��Ʊ����
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
						//���ݷ�Ʊpk����ѯ��Ʊ����
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