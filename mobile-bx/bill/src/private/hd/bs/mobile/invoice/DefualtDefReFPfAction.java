package hd.bs.mobile.invoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hd.bs.bill.bxfee.FeeBaseAction;
import hd.bs.muap.bdoc.DefaultDocAction;
import hd.itf.muap.pub.IMobileBillType;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.DefEventVO;
import hd.vo.muap.pub.MenuVO;
import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.vo.ep.bx.BXBusItemVO;
import nc.vo.ep.bx.JKHeaderVO;
import nc.vo.ep.bx.JKVO;
import nc.vo.erm.fppj.FpglVO;
import nc.vo.erm.fppj.FpxxBVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;

/**
 * ��Ʊ�Զ��������ʽʵ��
 * @author dongl 
 * 1.��ѯ��Ʊ���صĶ���ΪDefEventVO
 * ��Ҫ����billvo��Ʊ�����ӱ�����
 * ����MenuVO ����ƥ���Ӧģ��
 * 
 * 2.������ս��� �� �Զ������
 * ����ӱ�yjye������0 ��ô���ӱ� ���й���
 */
public class DefualtDefReFPfAction extends DefaultDocAction {

	public DefualtDefReFPfAction() {
		
	}
	
	@Override
	public Object processAction(String account, String userid, String billtype,String action, Object obj) throws BusinessException {
		if (billtype.equals(IMobileBillType.DEFREF)) {
			return doDefRefDocQuery(account, userid, obj,action);
		}
		return super.processAction(account, userid, billtype, action, obj);
	}
	
	BaseDAO dao = new BaseDAO();
	private Object doDefRefDocQuery(String account, String userid, Object obj,String action) throws BusinessException {
		ConditionAggVO conAggVO = (ConditionAggVO)obj;
		ConditionVO[] conVOs = conAggVO.getConditionVOs();
		if(conVOs==null||conVOs.length==0){
			return null;
		}
		
		DefEventVO defeventVO = new DefEventVO();
		String pk_fpdoc = null;
		//ȡ����Ʊ��PKֵ
		for(int i=0;i<conVOs.length;i++){
			if(conVOs[i].getField().equalsIgnoreCase(IVOField.PK)){
				pk_fpdoc = conVOs[i].getValue();
			}
		}
		
		String doctypes = conVOs[0].getValue();
		String pk_org = conVOs[1].getValue();
		String[] split = doctypes.split(",");
		if(split[0].endsWith("��Ʊ��")){
			//�Ƿ����� ��Ʊ����ģ�� H013
			UFBoolean booleanFP = new FeeBaseAction().getsysinitFP();
			if(booleanFP.booleanValue()){
				StringBuffer sql = new StringBuffer(
						"select *\n" +
								"  from erm_fpgl\n" + 
								" where nvl(dr, 0) = 0\n" + 
								"   and erm_fpgl.FPLX is not null\n" + 
								"   and erm_fpgl.FPHM is not null\n" + 
								"   and billmaker = '"+userid+"' and cyzt <> '����' \n" );
				//����з�Ʊ��PK˵��Ϊ���շ���  else ÿ�ε�������ڵ� �����²�һ�η�Ʊ		
				if(pk_fpdoc!=null&&pk_fpdoc.length()>0){
					sql.append(" and erm_fpgl.pk_fpgl in ('"+pk_fpdoc+"') ");
				}else{
					sql.append(" and billversionpk is null ");
				}
				//�Ƿ�����ҵ����� H014 Y ����
				booleanFP = new FeeBaseAction().sysinitFPCheck();
				if(booleanFP.booleanValue()){
					sql.append( " and cyzt <> 'δ����' ");
				}
				sql.append(" order by erm_fpgl.creationtime  desc ");
				ArrayList<FpglVO> Fplist = (ArrayList<FpglVO>)dao.executeQuery(sql.toString(), new BeanListProcessor(FpglVO.class));
				if(null == Fplist || Fplist.size() == 0){
					return null;
				}
				//��Ʊ����
				String pk_billtype = "FP01";
				HashMap<String, Object>[] maps = transNCVOTOMap(Fplist.toArray(new FpglVO[0]));
				HashMap<String, Object> headVO = null;
				BillVO[] billVOs = new BillVO[maps.length];
				for (int i = 0; i < maps.length; i++) {
					BillVO billVO = new BillVO();
					//���ճ����ķ�Ʊ�������޸�
					maps[i].put("ibillstatus", 1);
					headVO = maps[i];
					billVO.setHeadVO(headVO);
					String strWhere = " nvl(dr,0) = 0 and pk_fpgl  = '"+maps[i].get("pk_fpgl")+"'";
					ArrayList<SuperVO> bodylist = (ArrayList<SuperVO>) dao.retrieveByClause(FpxxBVO.class, strWhere);
					if(null != bodylist && bodylist.size() > 0){
						billVO.setTableVO("pk_fpxx", transNCVOTOMap(bodylist.toArray(new FpxxBVO[0])));
					}
					billVOs[i] = billVO;
				}
				defeventVO.setVos(billVOs);
				MenuVO menuVO = new MenuVO();
				menuVO.setMenucode("HMH39900");
				defeventVO.setMenuVO(menuVO);
				return defeventVO;
			}
		}else if(split[0].endsWith("��������")){
			String sql = 
					"select distinct a.*\n" +
					"          from er_busitem b\n" + 
					"          join er_jkzb a\n" + 
					"            on a.pk_jkbx = b.pk_jkbx\n" + 
					"          left join er_mtapp_detail m\n" + 
					"            ON b.pk_mtapp_detail = m.pk_mtapp_detail\n" + 
					"          left join bd_inoutbusiclass d\n" + 
					"            on d.pk_inoutbusiclass = b.szxmid\n" + 
					"          join bd_psndoc p\n" + 
					"            on p.pk_psndoc = a.jkbxr\n" + 
					"          join sm_user u\n" + 
					"            on p.pk_psndoc = u.pk_psndoc\n" + 
					"         where a.yjye > 0\n" + 
					"           and a.dr = 0\n" + 
					"           and a.pk_org = '"+pk_org+"'\n" + 
					"           and u.cuserid = '"+userid+"'\n" + 
					"           and a.djzt = 3\n" + 
					"           and isnull(b.dr, 0) = 0\n" + 
					"           and (b.yjye > 0 and\n" + 
					"               b.pk_jkbx in (select pk_jkbx\n" + 
					"                                from er_busitem\n" + 
					"                               where pk_item is null\n" + 
					"                                  or pk_item = '~'\n" + 
					"                                  or srcbilltype = '2611'))";
			if(pk_fpdoc!=null&&pk_fpdoc.length()>0){
				sql = sql+ "and b.pk_busitem = '"+pk_fpdoc+"'";
			}
			sql = sql+ " order by a.djrq desc ";
			ArrayList<JKHeaderVO> jkList = (ArrayList<JKHeaderVO>)dao.executeQuery(sql, new BeanListProcessor(JKHeaderVO.class));
			if(null == jkList || jkList.size() == 0){
				return null;
			}
			HashMap<String, Object>[] maps = transNCVOTOMap(jkList.toArray(new JKHeaderVO[0]));
			HashMap<String, Object> headVO = null;
			BillVO[] billVOs = new BillVO[maps.length];
			for (int i = 0; i < maps.length; i++) {
				BillVO billVO = new BillVO();
				//���ճ����Ľ��������޸�
				maps[i].put("ibillstatus", 1);
				headVO = maps[i];
				billVO.setHeadVO(headVO);
				JKVO aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(JKVO.class, maps[i].get("pk_jkbx").toString(), false);
		        BXBusItemVO[] busitem = aggvos.getBxBusItemVOS();
		        //����ӱ�ĳ������   yjye ���Ϊ0 ����ʾ��������
		        List<BXBusItemVO> bList = new ArrayList<BXBusItemVO>();
		        if(null != busitem && busitem.length > 0){
		          for(int b=0; b<busitem.length; b++){
		            if(busitem[b].getYjye().compareTo(new UFDouble(0)) > 0){
		              bList.add(busitem[b]);
		            }
		          }
		          billVO.setTableVO("jk_busitem", transNCVOTOMap(bList.toArray(new BXBusItemVO[0])));
		        }
				billVOs[i] = billVO;
			}
			defeventVO.setVos(billVOs);
			MenuVO menuVO = new MenuVO();
			//H5 û��NC��Ʒ�� �˵�����  ����д��
			menuVO.setMenucode("HMH30400");
			defeventVO.setMenuVO(menuVO);
			return defeventVO;
		}
		return doDefDocQuery(account, userid, obj);
	}
	
	
	public HashMap<String, Object>[] transNCVOTOMap(CircularlyAccessibleValueObject[] bodyVOs) throws BusinessException {
		ArrayList<HashMap<String, Object>> mbodyList = new ArrayList<HashMap<String, Object>>();
		String[] bodyattrs = bodyVOs[0].getAttributeNames();
		for (int row = 0; row < bodyVOs.length; row++) {
			HashMap<String, Object> mbodyVO = new HashMap<String, Object>();
			for (int i = 0; i < bodyattrs.length; i++) {
				Object value = bodyVOs[row].getAttributeValue(bodyattrs[i]);
				if (value != null) {
					if (value instanceof UFBoolean) {
						mbodyVO.put(bodyattrs[i], ((UFBoolean) value).booleanValue());
					} else if (value instanceof UFDouble) {
						mbodyVO.put(bodyattrs[i], ((UFDouble) value).doubleValue());
					} else if (value instanceof UFDate) {
						mbodyVO.put(bodyattrs[i], ((UFDate) value).toString());
					} else if (value instanceof UFDateTime) {
						mbodyVO.put(bodyattrs[i], ((UFDateTime) value).toString());
					} else if (value instanceof UFLiteralDate) {
						mbodyVO.put(bodyattrs[i], ((UFLiteralDate) value).toString());
					} else if(value instanceof String
							||value instanceof Integer
							||value instanceof Double
							||value instanceof Boolean){
						//�����������͵�����
						mbodyVO.put(bodyattrs[i], value);
					}
				}
			}
			// ���������������ֶ�
			mbodyVO.put(IVOField.PK, bodyVOs[row].getPrimaryKey());

			mbodyList.add(mbodyVO);
		}

		return mbodyList.toArray(new HashMap[0]);
	}

}