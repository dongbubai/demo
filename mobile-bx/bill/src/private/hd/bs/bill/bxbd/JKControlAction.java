package hd.bs.bill.bxbd;

import hd.bs.muap.approve.ApproveWorkAction; 

import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.bill.bxbd.JKControlVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.QueryBillVO; 
import java.util.ArrayList;
import java.util.HashMap; 
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy; 
import nc.bs.framework.common.NCLocator;
import nc.itf.erm.prv.IArapCommonPrivate; 
import nc.jdbc.framework.processor.BeanListProcessor; 
import nc.vo.ep.bx.LoanControlModeVO;
import nc.vo.ep.bx.LoanControlSchemaVO;
import nc.vo.ep.bx.LoanControlVO; 
import nc.vo.pub.BusinessException;  

/**
 * 4.MUJ00774  ����������-����
 * 5.MUJ00775  ����������-��֯
 */
public class JKControlAction extends ApproveWorkAction{

	BaseDAO dao = new BaseDAO();
	IArapCommonPrivate ap = NCLocator.getInstance().lookup(IArapCommonPrivate.class);
	
	//����������-����  �̶�����ע�����
	String funnode = "MUJ00774";
	/**
	 * ����-����������޸ı���
	 */
	@Override
	public Object save(String userid, Object obj) throws BusinessException {
 		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		HashMap<String, HashMap<String, Object>[]> bodys = bill.getBodyVOsMap();
		HashMap<String, Object>[] jkkzfaMap = bodys.get("er_jkkzfa");
		/**
		 *�޸�-����
		 */
		LoanControlVO vo = jkkz_set(head,jkkzfaMap);
		 
		if(null!=vo.getPk_control()){
			return queryBillVoByPK(userid, vo.getPk_control());
		}else{
			return null;
		}
	}
	/**
	 * er_jkkz_set  
	 */
	public LoanControlVO jkkz_set(HashMap<String, Object> head,HashMap<String, Object>[] jkkzfaMap) throws BusinessException{
		String billtype = super.getBilltype();
		
		String paracode = "";
		String paraname = "";
		String currency = "";
		int bbcontrol = 0;
		String controlattr = "";
		String controlstyle = "";
		if(null!=head.get("paracode") && !"".equals(head.get("paracode"))){
			String pk_control = "";
			if(null!=head.get("pk_control") && !"".equals(head.get("pk_control"))){
				pk_control = " and pk_control<>'"+head.get("pk_control")+"' ";
			}
			paracode = head.get("paracode").toString();
			String sql = "select * from er_jkkz_set where isnull(dr,0)=0 "+pk_control+" and paracode='"+paracode+"'";
			ArrayList<JKControlVO> list = (ArrayList<JKControlVO>) dao.executeQuery(sql, new BeanListProcessor(JKControlVO.class));
			if(null!=list && list.size()>0){
				throw new BusinessException("�����ظ�������������!");
			}
		}else{
			throw new BusinessException("��Ų���Ϊ��!");
		}
		if(null!=head.get("paraname") && !"".equals(head.get("paraname"))){
			paraname = head.get("paraname").toString();
		}else{
			throw new BusinessException("���Ʋ���Ϊ��!");
		}
		if(null!=head.get("currency") && !"".equals(head.get("currency"))){
			currency = head.get("currency").toString();
		}
		if(null==head.get("bbcontrol") || "N".equals(head.get("bbcontrol"))){
			bbcontrol = 0;
		}else{
			bbcontrol = 1;
		}
		if(null!=head.get("controlattr") && !"".equals(head.get("controlattr"))){
			controlattr = head.get("controlattr").toString();
		}else{
			throw new BusinessException("���ƶ�����Ϊ��!");
		}
		if(null!=head.get("controlstyle") && !"".equals(head.get("controlstyle"))){
			controlstyle = head.get("controlstyle").toString();
		}else{
			throw new BusinessException("�������Ͳ���Ϊ��!");
		}
		//1. er_jkkz_set
		LoanControlVO vo = new LoanControlVO();
		vo.setParacode(paracode);
		vo.setParaname(paraname);
		vo.setCurrency(currency);
		vo.setBbcontrol(bbcontrol);
		vo.setControlattr(controlattr);
		vo.setControlstyle(Integer.parseInt(controlstyle));
		vo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
		if(!funnode.equals(billtype)&& null!=head.get("pk_org") && !"".equals(head.get("pk_org"))){
			vo.setPk_org(head.get("pk_org").toString());
		}
		//2. er_jkkzfs
		if( (null==head.get("value1")||"".equals(head.get("value1").toString().trim())) && 
			(null==head.get("value2")||"".equals(head.get("value2").toString().trim()))&&
			(null==head.get("value3")||"".equals(head.get("value3").toString().trim()))&&
			(null==head.get("value4")||"".equals(head.get("value4").toString().trim()))){
			throw new BusinessException("����Ҫѡ��һ�����Ʒ�ʽ!");
		}
		ArrayList<LoanControlModeVO> mvos = new ArrayList<LoanControlModeVO>();
		if(null!=head.get("value1") && !"".equals(head.get("value1"))){
			LoanControlModeVO mvo = new LoanControlModeVO();
			mvo.setPk_controlmodedef("ArapZ3jkbx0000000001");
			mvo.setValue(Integer.parseInt(head.get("value1").toString()));
			mvos.add(mvo);
		}
		if(null!=head.get("value2") && !"".equals(head.get("value2"))){
			LoanControlModeVO mvo = new LoanControlModeVO();
			mvo.setPk_controlmodedef("ArapZ3jkbx0000000002");
			mvo.setValue(Integer.parseInt(head.get("value2").toString()));
			mvos.add(mvo);
		}
		if(null!=head.get("value3") && !"".equals(head.get("value3"))){
			LoanControlModeVO mvo = new LoanControlModeVO();
			mvo.setPk_controlmodedef("ArapZ3jkbx0000000003");
			mvo.setValue(Integer.parseInt(head.get("value3").toString()));
			mvos.add(mvo);
		}
		if(null!=head.get("value4") && !"".equals(head.get("value4"))){
			LoanControlModeVO mvo = new LoanControlModeVO();
			mvo.setPk_controlmodedef("ArapZ3jkbx0000000004");
			mvos.add(mvo);
		}
		vo.setModevos(mvos);
		//3. er_jkkzfa
		ArrayList<LoanControlSchemaVO> svos = new ArrayList<LoanControlSchemaVO>();
		if(null==jkkzfaMap || jkkzfaMap.length==0){
			throw new BusinessException("�������Ͳ���Ϊ��!");
		}
		for(int i=0; null!=jkkzfaMap && i<jkkzfaMap.length; i++){
			LoanControlSchemaVO svo = new LoanControlSchemaVO();
			svo.setBalatype(null!=jkkzfaMap[i].get("balatype")?jkkzfaMap[i].get("balatype").toString():"");
			if(null!=jkkzfaMap[i].get("djlxbm") || !"".equals(jkkzfaMap[i].get("djlxbm"))){
				svo.setDjlxbm(jkkzfaMap[i].get("djlxbm").toString());
			}else{
				throw new BusinessException("�������Ͳ���Ϊ��!");
			}
			svos.add(svo);
		}
		
		vo.setSchemavos(svos);
		//����
		if(null==head.get("pk_control") || "".equals(head.get("pk_control"))){
			LoanControlVO nvo = (LoanControlVO) ap.save(vo);
			return nvo;
		}
		//����
		if(null!=head.get("pk_control") && !"".equals(head.get("pk_control"))){
			vo.setPk_control(head.get("pk_control").toString());
			if(null!=head.get("ts") && !"".equals(head.get("ts"))){
				vo.setTs(head.get("ts").toString());
			}
			LoanControlVO nvo = (LoanControlVO) ap.update(vo);
			return nvo;
		}
		return null;
	}
	
	
	/**
	 * ��pk��ѯ����
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

		// ��ѯ ��ͷ
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		// ��ѯ ����
		QueryBillVO body_querybillVO = (QueryBillVO) queryPage_body(userid, condAggVO_body,1,1);
		// ��ѯ ����  
		if(null!=body_querybillVO.getQueryVOs()){
			billVO = body_querybillVO.getQueryVOs()[0];
		}
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}
	
	String pk_org = "";
	/**
	 * ������ʾֻ��������������3���֣��ֱ�3�����ݿ��
	 */
	@Override
	public Object queryPage(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","et.pk_control");
		map.put("PK","et.pk_control");
		map.put("er_jkkz_set.pk_org","et.pk_org");
		StringBuffer str = reCondition(obj, true,map);
		
		String billtype = super.getBilltype();
			
		if(null!=str){
			pk_org = str.toString();
		} 
		if(null!=billtype && billtype.equals(funnode) && null!=str && !str.toString().contains("pk_control")){
			//MUJ00774�̶�������������-���š��ڵ�
			pk_org = " and pk_org is null ";
		}
		String sql ="select et.*,\n" +
					"       fs1.pk_controlmode as pk_controlmode1,fs2.pk_controlmode as pk_controlmode2,\n" + 
					"       fs3.pk_controlmode as pk_controlmode3,fs4.pk_controlmode as pk_controlmode4,\n" + 
					"       fs1.pk_controlmodedef as pk_controlmodedef1,fs2.pk_controlmodedef as pk_controlmodedef2,\n" + 
					"       fs3.pk_controlmodedef as pk_controlmodedef3,fs4.pk_controlmodedef as pk_controlmodedef4,\n" + 
					"       value1,value2,value3,case when fs4.pk_controlmodedef is not null then 'Y' end as value4" +
//					"       ,er_jkkzfa.balatype,er_jkkzfa.djlxbm,er_jkkzfa.pk_controlschema" +
					"		,case when et.bbcontrol=1 then 'Y' else 'N' end as bbcontrol\n" + 
					"from er_jkkz_set et\n" + 
					"left join\n" + 
					"(select pk_control,pk_controlmode,pk_controlmodedef,value as value1\n" + 
					"   from er_jkkzfs\n" + 
					"  where nvl(dr,0)=0\n" + 
					"   and pk_controlmodedef='ArapZ3jkbx0000000001') fs1 on et.pk_control=fs1.pk_control\n" + 
					"left join\n" + 
					"(select pk_control,pk_controlmode,pk_controlmodedef,value as value2\n" + 
					"  from er_jkkzfs\n" + 
					"  where nvl(dr,0)=0\n" + 
					"  and pk_controlmodedef='ArapZ3jkbx0000000002' ) fs2 on et.pk_control=fs2.pk_control\n" + 
					"left join\n" + 
					"(select pk_control,pk_controlmode,pk_controlmodedef,value as value3\n" + 
					"  from er_jkkzfs\n" + 
					"  where nvl(dr,0)=0\n" + 
					"  and pk_controlmodedef='ArapZ3jkbx0000000003' ) fs3 on et.pk_control=fs3.pk_control\n" + 
					"left join\n" + 
					"(select pk_control,pk_controlmode,pk_controlmodedef,value as value4\n" + 
					"  from er_jkkzfs\n" + 
					"  where nvl(dr,0)=0\n" + 
					"  and pk_controlmodedef='ArapZ3jkbx0000000004' ) fs4 on et.pk_control=fs4.pk_control\n" + 
//					"join er_jkkzfa on er_jkkzfa.pk_control=et.pk_control and nvl(er_jkkzfa.dr,0)=0\n" + 
					"where nvl(et.dr,0)=0\n " +
					" "+pk_org+"" ;
//					"and et.pk_control='10011110000000003KVF'";
		ArrayList<JKControlVO> list = (ArrayList<JKControlVO>) dao.executeQuery(sql, new BeanListProcessor(JKControlVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		/**
		 * vostatus->1 ����δ�༭��Ҳ��ش�����̨
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new JKControlVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			PubTools.execFormulaWithVOs(maps, formulas);
			maps[i].put("ibillstatus", -1);// ����
			maps[i].put("PK", headVO.get("pk_control")); 
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs); 
		return qbillVO;
	}
	
	
	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		String billtype = super.getBilltype();
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("PPK","pk_control");
		StringBuffer str = reCondition(obj, true,map);
		
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1]; 
		BillVO billVO = new BillVO();
		
		/**
		 * vostatus->1 ����δ�༭��Ҳ��ش�����̨
		 */
		String[] formulas = new String[] { "vostatus->1;" };
		 
		String action = super.getAction();// ÿ��ִ�д˷���������ҳǩ����
		// �߼� 
		String sql = "select * from er_jkkzfa where nvl(dr,0)=0 "+str+"";
		ArrayList<LoanControlSchemaVO> list = (ArrayList<LoanControlSchemaVO>) dao.executeQuery(sql, new BeanListProcessor(LoanControlSchemaVO.class));
		if (list != null && list.size() > 0) {
			HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new LoanControlSchemaVO[0]));
			PubTools.execFormulaWithVOs(maps, formulas);
			billVO.setTableVO("er_jkkzfa", maps); 
		} 
		billVOs[0] = billVO;
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	
	
	/**
	 * ɾ�����е���
	 */
	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		HashMap<String, Object> head = bill.getHeadVO();
		LoanControlVO lcvo = new LoanControlVO();
		lcvo.setBbcontrol(0);
		lcvo.setControlattr(null!=head.get("controlattr")?head.get("controlattr").toString():null);
		lcvo.setControlstyle(null!=head.get("controlstyle")?Integer.parseInt(head.get("controlstyle").toString()):null);
		lcvo.setCurrency(null!=head.get("currency")?head.get("currency").toString():null);
		lcvo.setParacode(null!=head.get("paracode")?head.get("paracode").toString():null);
		lcvo.setParaname(null!=head.get("paraname")?head.get("paraname").toString():null);
		lcvo.setPk_control(null!=head.get("pk_control")?head.get("pk_control").toString():null);
		lcvo.setPk_group(null!=head.get("pk_group")?head.get("pk_group").toString():null);
		lcvo.setPk_org(null!=head.get("pk_org")?head.get("pk_org").toString():null);
		lcvo.setTs(null!=head.get("ts")?head.get("ts").toString():null);
		
		ap.delete(lcvo);
		
		return null;
//		ArrayList<LoanControlModeVO> mvos = new ArrayList<LoanControlModeVO>();
//		int size = 5;
//		for(int i=1; i<size; i++){
//			if(null!=head.get("pk_controlmode"+i) && !"".equals(head.get("pk_controlmode"+i))){
//				LoanControlModeVO mvo = new LoanControlModeVO();
//				mvo.setPk_controlmode(null!=head.get("pk_controlmode"+i)?head.get("pk_controlmode"+i).toString():null);
//				mvo.setPk_control(null!=head.get("pk_control")?head.get("pk_control").toString():null);
//				if(null!=head.get("pk_controlmodedef"+i) && !"".equals(head.get("pk_controlmodedef"+i))){
//					mvo.setPk_controlmodedef(null!=head.get("pk_controlmodedef"+i)?head.get("pk_controlmodedef"+i).toString():null);
//				}
//				if(null!=head.get("value"+i) && !"".equals(head.get("value"+i))){
//					if(!new UFBoolean(head.get("value"+i).toString()).booleanValue()){
//						mvo.setValue(null!=head.get("value"+i)?Integer.parseInt(head.get("value"+i).toString()):0);
//					}
//				}
//				mvos.add(mvo);
//			}else{
//				continue;
//			}
//		}
//		if(null!=mvos && mvos.size()>0){
//			lcvo.setModevos(mvos);
//		}
	}
}