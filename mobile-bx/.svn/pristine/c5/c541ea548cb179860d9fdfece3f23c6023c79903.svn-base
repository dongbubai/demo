/**
 * 
 */
package nc.bs.mobile.ApplyManager;

import hd.bs.muap.pub.AbstractMobileAction;
import hd.itf.muap.pub.IMobileAction;
import hd.muap.pub.tools.PuPubVO;
import hd.muap.pub.tools.PubTools;
import hd.muap.vo.field.IVOField;
import hd.vo.muap.approve.ApproveInfoVO;
import hd.vo.muap.approve.MApproveVO;
import hd.vo.muap.pub.AfterEditVO;
import hd.vo.muap.pub.BillVO;
import hd.vo.muap.pub.ConditionAggVO;
import hd.vo.muap.pub.ConditionVO;
import hd.vo.muap.pub.DefEventVO;
import hd.vo.muap.pub.FormulaVO;
import hd.vo.muap.pub.QueryBillVO;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.HRCMTermUnitUtils;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.hi.IPsndocQryService;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.hr.frame.IPersistenceRetrieve;
import nc.itf.trn.transmng.ITransmngManageService;
import nc.itf.trn.transmng.ITransmngQueryService;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.pub.tools.HiSQLHelper;
import nc.vo.hi.psndoc.CtrtVO;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.TrialVO;
import nc.vo.hr.managescope.ManagescopeBusiregionEnum;
import nc.vo.om.job.JobGradeVO;
import nc.vo.om.job.JobVO;
import nc.vo.om.joblevelsys.JobLevelVO;
import nc.vo.om.post.PostVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.trn.pub.TRNConst;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;
import nc.vo.uap.rbac.constant.INCSystemUserConst;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
/**
 * @author wanghy  调配申请
 *
 */
public class DpApplyAction  extends AbstractMobileAction {
	private BaseDAO baseDAO;
	private String pkbilltype = null;

	public BaseDAO getQuery() {
		return baseDAO == null ? new BaseDAO() : baseDAO;
	}
	private SimpleDocServiceTemplate service = null;
	private SimpleDocServiceTemplate getService()
	{
        if (service == null)
        {
            service = new SimpleDocServiceTemplate("TrnBillFormEditor");
        }
        return service;
	}

	@Override
	public Object afterEdit(String userid, Object obj) throws BusinessException {
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();
		String key = aevo.getKey();
		HashMap<String, Object> head = bill.getHeadVO();
		Integer applymode = (Integer)head.get(StapplyVO.STAPPLY_MODE);
		Integer trialUnit =PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIAL_UNIT))== null ? 
				HRCMTermUnitUtils.TERMUNIT_MONTH:PuPubVO.getInteger_NullAs(head.get(StapplyVO.TRIAL_UNIT), 3);//默认为天
		//调配人员编辑后时间，带出调配前后信息
		if("pk_hi_stapply".equals(key)){
			String[] formula_str = new String[1];
			String sql= "select hi_psnjob.pk_psnjob from hi_psnjob  inner join  hi_psnorg on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg inner join bd_psndoc on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc"+
					" inner join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc where cuserid='"+userid+"'";
					BaseDAO dao = new BaseDAO();
					Object objvule = dao.executeQuery(sql,new ColumnProcessor());
			formula_str[0] =""+StapplyVO.PK_PSNJOB+"->"+objvule+";";
			
			FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
		}else if(StapplyVO.PK_PSNJOB.equals(key)){
			if(aevo.getValue()==null||aevo.getValue().toString().trim().length()==0){
				throw new BusinessException("调配人员为空。");
			}
			PsnJobVO psn = getService().queryByPk(PsnJobVO.class, aevo.getValue().toString(), true);
			String[] newinfo = new String[]{"newseries", "newpk_dept", "newpk_job", "newpk_postseries", "newpk_post", "newpk_jobrank", "newpk_jobgrade", "newpk_job_type", "newpk_psncl", "newjobmode", "newdeposemode", "newpoststat", "newoccupation", "newworktype", "newpk_org", "newmemo"};
			String[] oldinfo = new String[]{"oldseries", "oldpk_dept", "oldpk_job", "oldpk_postseries", "oldpk_post", "oldpk_jobrank", "oldpk_jobgrade", "oldpk_job_type", "oldpk_psncl", "oldjobmode", "olddeposemode", "oldpoststat", "oldoccupation", "oldworktype", "oldpk_org", "oldmemo"};
			ArrayList<String> newlist = new ArrayList<String>(Arrays.asList(newinfo));
			ArrayList<String> oldlist = new ArrayList<String>(Arrays.asList(oldinfo));
			String[] formula_str = new String[41];
			formula_str = setPsndoc(psn,newlist,oldlist,aevo.getValue().toString(),head);
			FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
		}else if(StapplyVO.NEWPK_ORG.equals(key)){
			// 任职组织,组织改后所有都要清空
			String[] formula_str = new String[9];
			formula_str[0] =""+StapplyVO.NEWPK_DEPT+"->"+null+";";// 部门
			formula_str[1] =""+StapplyVO.NEWPK_POST+"->"+null+";";// 岗位
			formula_str[2] =""+StapplyVO.NEWPK_JOB+"->"+null+";";// 职务
			formula_str[3] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";// 职级
			formula_str[4] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";// 职等
			formula_str[5] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
			formula_str[6] =""+StapplyVO.NEWSERIES+"->"+null+";";// 职务类别
			
			if(applymode == TRNConst.TRANSMODE_CROSS_OUT){
				 // 调出时清空调配后组织
				formula_str[7] =""+StapplyVO.PK_HI_ORG+"->"+null+";";
				formula_str[8] =""+StapplyVO.PK_HRCM_ORG+"->"+null+";";
			}
			FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
		} else if (StapplyVO.NEWPK_DEPT.equals(key)){
			String[] formula_str = new String[10];
			formula_str[0] =""+StapplyVO.NEWPK_POST+"->"+null+";";// 岗位
			formula_str[1] =""+StapplyVO.NEWPK_JOB+"->"+null+";";// 职务
			formula_str[2] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";// 职级
			formula_str[3] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";// 职等
			formula_str[4] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
			formula_str[5] =""+StapplyVO.NEWSERIES+"->"+null+";";// 职务类别
            if (applymode== TRNConst.TRANSMODE_CROSS_OUT){
                // 调出时清空调配后组织
                String pk_dept = String.valueOf(aevo.getValue());
                String pk_org = HiSQLHelper.getHrorgBydept(pk_dept);// 得到的是---调配后人事组织
                formula_str[6] =""+StapplyVO.PK_HI_ORG+"->"+pk_org+";";
                // 根据部门主键查询该部门的合同管理的HR组织
                String pk_cmorg = HiSQLHelper.getEveryHrorgBydept(pk_dept, ManagescopeBusiregionEnum.psnpact);
                // 此处的新合同管理组织是否可编辑待定？？？
                formula_str[7] =""+StapplyVO.PK_HRCM_ORG+"->"+pk_cmorg+";";
                // 选择不同字段时处理解除和终止字段---参数其实为新合同管理组织，目前等于调配后人事组织，后面考虑是否修改
                String pk_psnorg = (String) head.get(StapplyVO.PK_PSNORG);
    	        if (!StringUtils.isBlank(pk_psnorg)){
    	        	CtrtVO ctrtvo = getQueryCtrtVO(pk_psnorg);
    		        if (ctrtvo == null){// 最新的合同记录不是签订/变更/续签三个状态（未签订合同或者合同已经解除/终止），不需要勾选和编辑
    		        	if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
    		        	if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
    		        }else{
    		        	 if (TRNConst.TRANSMODE_CROSS_OUT != Integer.parseInt(head.get("stapply_mode").toString()))
    		             {// 不是调出情况
    		        		 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
    		        	        if (effectdate != null)
    		        	        {
    		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
    		        	            if(StapplyVO.ISRELEASE.equals(key)){
    		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
    		        	            	formula_str[8] =""+key+"->\""+a+"\";";
    		        	            }// 解除
    		    		        	if(StapplyVO.ISEND.equals(key)){
    		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
    		    		        		formula_str[9] =""+key+"->\""+b+"\";";
    		    		        	}// 终止
    		        	        }
    		        	        else{
    		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
    		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
    		        	            if(StapplyVO.ISRELEASE.equals(key)){
    		        	            	formula_str[8] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
    		        	            }
    		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
    		        	            if(StapplyVO.ISEND.equals(key)){
    		        	            	formula_str[9] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
    		        	            }
    		        	        }
    		             }
    		        	 // 调出时清空调配后组织
    		             String pk_old_hrcm_org =  HiSQLHelper.getEveryHrorg((String)head.get(StapplyVO.PK_PSNORG), (Integer)head.get(StapplyVO.ASSGID), ManagescopeBusiregionEnum.psnpact);
    		             if (StringUtils.isBlank(pk_old_hrcm_org) || StringUtils.isBlank(pk_org))
    		             {	 
    		                 // 合同前组织肯定不会为空，那只有合同后组织为空的可能
    		            	 if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
    				         if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
    		             }else{
    		            	 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
    		        	        if (effectdate != null)
    		        	        {
    		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
    		        	            if(StapplyVO.ISRELEASE.equals(key)){
    		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
    		        	            	formula_str[8] =""+key+"->\""+a+"\";";
    		        	            }// 解除
    		    		        	if(StapplyVO.ISEND.equals(key)){
    		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
    		    		        		formula_str[9] =""+key+"->\""+b+"\";";
    		    		        	}// 终止
    		        	        }
    		        	        else{
    		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
    		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
    		        	            if(StapplyVO.ISRELEASE.equals(key)){
    		        	            	formula_str[8] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
    		        	            }
    		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
    		        	            if(StapplyVO.ISEND.equals(key)){
    		        	            	formula_str[9] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
    		        	            }
    		        	        }
    		             }
    		        }
    	        }
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
		else if (StapplyVO.NEWPK_POST.equals(key))
        {
            // 岗位
            String pk_post = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());
            PostVO post = pk_post == null ? null : getService().queryByPk(PostVO.class, pk_post, true);
            String[] formula_str = new String[7];
            if (post != null)
            {	
    			formula_str[0] =""+StapplyVO.NEWPK_POSTSERIES+"->"+post.getPk_postseries()+";";// 岗位序列
    			formula_str[1] =""+StapplyVO.NEWPK_JOB+"->"+post.getPk_job()+";";// 职务
                JobVO jobVO = post.getPk_job() == null ? null : getService().queryByPk(JobVO.class, post.getPk_job());
                boolean isJobTypeSeted = false;
                if (jobVO != null)
                {
                	formula_str[2] =""+StapplyVO.NEWSERIES+"->"+jobVO.getPk_jobtype()+";";//职务类别
                    isJobTypeSeted = true;
                }
                if (post.getEmployment() != null)
                {
                	formula_str[3] =""+StapplyVO.NEWOCCUPATION+"->"+post.getEmployment()+";";//职业
                }
                if (post.getWorktype() != null)
                {	
                	formula_str[4] =""+StapplyVO.NEWWORKTYPE+"->"+post.getWorktype()+";";// 工种
                }
                
                String defaultlevel = "\"\"";
                String defaultrank = "\"\"";
                Map<String, String> resultMap =
                    NCLocator.getInstance().lookup(IPsndocQryService.class).getDefaultLevelRank(null, null, null, pk_post, null);
                if (!resultMap.isEmpty())
                {
                    defaultlevel = resultMap.get("defaultlevel");
                    defaultrank = resultMap.get("defaultrank");
                }
                if (!isJobTypeSeted)
                {
                    String newseries = String.valueOf(head.get(StapplyVO.NEWSERIES));
                    formula_str[2] =""+StapplyVO.NEWSERIES+"->"+newseries+";";//职务类别
                }
                formula_str[5] =""+StapplyVO.NEWPK_JOBGRADE+"->"+defaultlevel+";";//职级
                formula_str[6] =""+StapplyVO.NEWPK_JOBRANK+"->"+defaultrank+";";//职等
            }
            else
            {	
            	formula_str[2] =""+StapplyVO.NEWSERIES+"->"+null+";";//职务类别
            	formula_str[0] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
            	formula_str[5] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";//职级
                formula_str[6] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
                formula_str[1] =""+StapplyVO.NEWPK_JOB+"->"+null+";";// 职务
            }
            if (post == null)
            {	
            	formula_str[2] =""+StapplyVO.NEWSERIES+"->"+null+";";//职务类别
            	formula_str[0] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
            	formula_str[5] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";//职级
            	formula_str[6] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
		else if (StapplyVO.NEWPK_JOB.equals(key))
        {	
        	String[] formula_str = new String[4];
            // 职务
            String pk_job = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());
            String pk_post = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_POST));
            JobVO job = pk_job == null ? null : getService().queryByPk(JobVO.class, pk_job, true);
            if (job != null)
            {
                String defaultlevel = "\"\"";
                String defaultrank = "\"\"";
                Map<String, String> resultMap =
                    NCLocator.getInstance().lookup(IPsndocQryService.class).getDefaultLevelRank(null, pk_job, null, pk_post, null);
                if (!resultMap.isEmpty())
                {
                    defaultlevel = resultMap.get("defaultlevel");
                    defaultrank = resultMap.get("defaultrank");
                }
                formula_str[0] =""+StapplyVO.NEWSERIES+"->"+job.getPk_jobtype()+";";//职务类别
            	formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+defaultlevel+";";//职级
                formula_str[2] =""+StapplyVO.NEWPK_JOBRANK+"->"+defaultrank+";";//职等
                formula_str[3] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
            }
            else
            {
            	formula_str[0] =""+StapplyVO.NEWSERIES+"->"+null+";";//职务类别
            	formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";//职级
                formula_str[2] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
                formula_str[3] =""+StapplyVO.NEWPK_POSTSERIES+"->"+null+";";// 岗位序列
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.NEWPK_JOBGRADE.equals(key))
        {
            // 职级
        	String[] formula_str = new String[1];
            String pk_jobgrage = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());
            JobLevelVO jobgrade = pk_jobgrage == null ? null : getService().queryByPk(JobLevelVO.class, pk_jobgrage, true);
            if (jobgrade != null)
            {
                String pk_postseries = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_POSTSERIES));
                String pk_job = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_JOB));
                String pk_post = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_POST));
                String series = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWSERIES));
                String defaultrank = "\"\"";
                Map<String, String> resultMap =
                    NCLocator.getInstance().lookup(IPsndocQryService.class)
                        .getDefaultLevelRank(series, pk_job, pk_postseries, pk_post, pk_jobgrage);
                if (!resultMap.isEmpty())
                {
                    defaultrank = resultMap.get("defaultrank");
                }
                // 职级带出职等后职等不能编辑
                formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+defaultrank+";";//职等
            }
            else
            {
                // 职级清空后,若职务关联了职等,则用职务上的职等
            	formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.NEWSERIES.equals(key))
        {
            // 职务类别
        	String[] formula_str = new String[2];
            String series = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());;
            String pk_job = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_JOB));
            String pk_post = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_POST));
            if (StringUtils.isBlank(pk_job) && StringUtils.isNotBlank(series))
            {
                String defaultlevel = "\"\"";
                String defaultrank = "\"\"";
                Map<String, String> resultMap =
                    NCLocator.getInstance().lookup(IPsndocQryService.class).getDefaultLevelRank(series, pk_job, null, pk_post, null);
                if (!resultMap.isEmpty())
                {
                    defaultlevel = resultMap.get("defaultlevel");
                    defaultrank = resultMap.get("defaultrank");
                }
                formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+defaultrank+";";//职等
                formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+defaultlevel+";";//职级
            }
            else if (StringUtils.isBlank(pk_job) && StringUtils.isBlank(series))
            {
	        	 formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
	             formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";//职级
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.NEWPK_POSTSERIES.equals(key))
        {
            // 岗位序列
        	String[] formula_str = new String[2];
            String pk_postseries = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());;
            String pk_job = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_JOB));
            String pk_post = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWPK_POST));
            String series = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.NEWSERIES));
            if (StringUtils.isBlank(pk_job) && StringUtils.isBlank(series) && StringUtils.isBlank(pk_post)
                && StringUtils.isNotBlank(pk_postseries))
            {
                String defaultlevel = "\"\"";
                String defaultrank = "\"\"";
                Map<String, String> resultMap =
                    NCLocator.getInstance().lookup(IPsndocQryService.class)
                        .getDefaultLevelRank(series, pk_job, pk_postseries, pk_post, null);
                if (!resultMap.isEmpty())
                {
                    defaultlevel = resultMap.get("defaultlevel");
                    defaultrank = resultMap.get("defaultrank");
                }
                formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+defaultrank+";";//职等
                formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+defaultlevel+";";//职级
            }
            else if (StringUtils.isBlank(pk_job) && StringUtils.isBlank(series) && StringUtils.isBlank(pk_post)
                && StringUtils.isBlank(pk_postseries))
            {
            	  formula_str[0] =""+StapplyVO.NEWPK_JOBRANK+"->"+null+";";//职等
                  formula_str[1] =""+StapplyVO.NEWPK_JOBGRADE+"->"+null+";";//职级
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.PK_HI_ORG.equals(key))
        {// 调配后人事组织
        	String[] formula_str = new String[3];
            String pk_hi_org = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());
            formula_str[0] =""+StapplyVO.PK_HRCM_ORG+"->"+pk_hi_org+";";
            // 选择不同字段时处理解除和终止字段---参数其实为新合同管理组织，目前等于调配后人事组织，后面考虑是否修改
            String pk_psnorg = (String) head.get(StapplyVO.PK_PSNORG);
	        if (!StringUtils.isBlank(pk_psnorg)){
	        	CtrtVO ctrtvo = getQueryCtrtVO(pk_psnorg);
		        if (ctrtvo == null){// 最新的合同记录不是签订/变更/续签三个状态（未签订合同或者合同已经解除/终止），不需要勾选和编辑
		        	if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
		        	if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		        }else{
		        	 if (TRNConst.TRANSMODE_CROSS_OUT != Integer.parseInt(head.get("stapply_mode").toString()))
		             {// 不是调出情况
		        		 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString());
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        	 // 调出时清空调配后组织
		             String pk_old_hrcm_org =  HiSQLHelper.getEveryHrorg((String)head.get(StapplyVO.PK_PSNORG), (Integer)head.get(StapplyVO.ASSGID), ManagescopeBusiregionEnum.psnpact);
		             if (StringUtils.isBlank(pk_old_hrcm_org) || StringUtils.isBlank(pk_hi_org))
		             {	 
		                 // 合同前组织肯定不会为空，那只有合同后组织为空的可能
		            	 if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
				         if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		             }else{
		            	 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        }
	        }
	        FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.EFFECTDATE.equals(key))// 生效日期
        {	
        	String[] formula_str = new String[2];
            // 调出时清空调配后组织
        	String pk_hrcm_org = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.PK_HRCM_ORG));
            String pk_psnorg = (String) head.get(StapplyVO.PK_PSNORG);
	        if (!StringUtils.isBlank(pk_psnorg)){
	        	CtrtVO ctrtvo = getQueryCtrtVO(pk_psnorg);
		        if (ctrtvo == null){// 最新的合同记录不是签订/变更/续签三个状态（未签订合同或者合同已经解除/终止），不需要勾选和编辑
		        	if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
		        	if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		        }else{
		        	 if (TRNConst.TRANSMODE_CROSS_OUT != Integer.parseInt(head.get("stapply_mode").toString()))
		             {// 不是调出情况
		        		 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        	 // 调出时清空调配后组织
		             String pk_old_hrcm_org =  HiSQLHelper.getEveryHrorg((String)head.get(StapplyVO.PK_PSNORG), (Integer)head.get(StapplyVO.ASSGID), ManagescopeBusiregionEnum.psnpact);
		             if (StringUtils.isBlank(pk_old_hrcm_org) || StringUtils.isBlank(pk_hrcm_org))
		             {	 
		                 // 合同前组织肯定不会为空，那只有合同后组织为空的可能
		            	 if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
				         if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		             }else{
		            	 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString());
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        }
	        }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (StapplyVO.PK_HRCM_ORG.equals(key))
        {	
        	String[] formula_str = new String[2];
            String pk_hrcm_org = PuPubVO.getString_TrimZeroLenAsNull(aevo.getValue());
            String pk_psnorg = (String) head.get(StapplyVO.PK_PSNORG);
	        if (!StringUtils.isBlank(pk_psnorg)){
	        	CtrtVO ctrtvo = getQueryCtrtVO(pk_psnorg);
		        if (ctrtvo == null){// 最新的合同记录不是签订/变更/续签三个状态（未签订合同或者合同已经解除/终止），不需要勾选和编辑
		        	if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
		        	if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		        }else{
		        	 if (TRNConst.TRANSMODE_CROSS_OUT != Integer.parseInt(head.get("stapply_mode").toString()))
		             {// 不是调出情况
		        		 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString());
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        	 // 调出时清空调配后组织
		             String pk_old_hrcm_org =  HiSQLHelper.getEveryHrorg((String)head.get(StapplyVO.PK_PSNORG), (Integer)head.get(StapplyVO.ASSGID), ManagescopeBusiregionEnum.psnpact);
		             if (StringUtils.isBlank(pk_old_hrcm_org) || StringUtils.isBlank(pk_hrcm_org))
		             {	 
		                 // 合同前组织肯定不会为空，那只有合同后组织为空的可能
		            	 if(StapplyVO.ISRELEASE.equals(key)){formula_str[8] =""+key+"->"+null+";";}// 解除
				         if(StapplyVO.ISEND.equals(key)){formula_str[9] =""+key+"->"+null+";";}// 终止
		             }else{
		            	 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[1] =""+key+"->\""+a+"\";";
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[2] =""+key+"->\""+b+"\";";
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[1] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[2] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            }
		        	        }
		             }
		        }
	        }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }else if (key.equals(StapplyVO.TRIALDAYS))
        {// 试用期限
        	String[] formula_str = new String[1];
            if (PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIALBEGINDATE)) != null && PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIALDAYS))!= null)
            {	
            	formula_str[0] =""+StapplyVO.TRIALENDDATE+"->\""+HRCMTermUnitUtils.getDateAfterMonth(new UFLiteralDate(head.get(StapplyVO.TRIALBEGINDATE).toString()), 
            		Integer.parseInt(head.get(StapplyVO.TRIALDAYS).toString()), trialUnit)+"\";";
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (key.equals(StapplyVO.TRIALBEGINDATE))
        {// 试用开始日期
        	String[] formula_str = new String[1];
            if (PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIALDAYS))!= null)
            {	
            	formula_str[0] =""+StapplyVO.TRIALENDDATE+"->\""+HRCMTermUnitUtils.getDateAfterMonth(new UFLiteralDate(head.get(StapplyVO.TRIALBEGINDATE).toString()), 
                		Integer.parseInt(head.get(StapplyVO.TRIALDAYS).toString()), trialUnit)+"\";";
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (key.equals(StapplyVO.TRIAL_FLAG))
        {// 是否试用
        	String[] formula_str = new String[5];
            boolean isTrail = aevo.getValue().toString().equals("Y")?true:false;
            if (!isTrail)
            {	
            	formula_str[0] = ""+StapplyVO.TRIAL_UNIT+"->0;";
            	formula_str[1] = ""+StapplyVO.TRIALDAYS+"->"+null+";";
            	formula_str[2] = ""+StapplyVO.TRIALBEGINDATE+"->"+null+";";
            	formula_str[3] = ""+StapplyVO.TRIALENDDATE+"->"+null+";";
            }
            else
            {
                // 设置“岗位试用期限单位”值，及“岗位试用期限”的输入长度
            	formula_str[0] = ""+StapplyVO.TRIAL_UNIT+"->"+trialUnit+";";
            	String effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE));
            	if(effectdate==null){
            		throw new BusinessException("请先维护生效日期！");
            	}
                UFLiteralDate planDate = new UFLiteralDate(effectdate);
                formula_str[2] = ""+StapplyVO.TRIALBEGINDATE+"->\""+planDate+"\";";
            }
            // 校验当前人员是否存在试用记录
            String pk_psnjob = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.PK_PSNJOB));
            if(isTrail && pk_psnjob!=null){
            	  // 单条制单
                String cond = " pk_psnorg in (select pk_psnorg from hi_psnjob where pk_psnjob ='" + pk_psnjob + "') and endflag <> 'Y'";
                int i = NCLocator.getInstance().lookup(IPersistenceRetrieve.class).getCountByCondition(TrialVO.getDefaultTableName(), cond);
                if (i > 0)
                {
//                	formula_str[0] = ""+StapplyVO.TRIAL_UNIT+"->"+null+";";
//                	formula_str[1] = ""+StapplyVO.TRIALDAYS+"->"+null+";";
//                	formula_str[2] = ""+StapplyVO.TRIALBEGINDATE+"->"+null+";";
//                	formula_str[3] = ""+StapplyVO.TRIALENDDATE+"->"+null+";";
//                	formula_str[4] = ""+StapplyVO.TRIAL_FLAG+"->"+UFBoolean.FALSE+";";
                	throw new BusinessException(ResHelper.getString("6009tran", "06009tran0178"));//"当前调配人存在未结束的试用记录,不能勾选试用标志."
                }
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if (key.equals(StapplyVO.TRIAL_UNIT))
        {// 岗位试用期限单位
//            getBillCardPanel().getHeadItem(StapplyVO.TRIALDAYS).setLength(HRCMTermUnitUtils.getLengthByTermUnit(trialUnit));
        	String[] formula_str = new String[1];
            if (PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIALBEGINDATE)) != null && PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.TRIALDAYS))!= null)
            {
            	formula_str[0] = ""+StapplyVO.TRIALENDDATE+"->\""+HRCMTermUnitUtils.getDateAfterMonth(new UFLiteralDate(head.get(StapplyVO.TRIALBEGINDATE).toString()), 
                		Integer.parseInt(head.get(StapplyVO.TRIALDAYS).toString()), trialUnit)+"\";";
            }
            FormulaVO formulaVO1 = new FormulaVO();
			formulaVO1.setFormulas(formula_str);
			return formulaVO1;
        }
        else if(StapplyVO.ISEND.equals(key)){
        	String[] formula_str = new String[1];
        	 boolean isend = PuPubVO.getUFBoolean_NullAs(aevo.getValue(), UFBoolean.FALSE).booleanValue();
        	 if(isend){
        		 formula_str[0] = ""+StapplyVO.ISRELEASE+"->"+null+";";
        		 FormulaVO formulaVO1 = new FormulaVO();
        		 formulaVO1.setFormulas(formula_str);
        		 return formulaVO1;
        	 }
        }
        else if(StapplyVO.ISRELEASE.equals(key)){
        	String[] formula_str = new String[1];
        	 boolean isend = PuPubVO.getUFBoolean_NullAs(aevo.getValue(), UFBoolean.FALSE).booleanValue();
        	 if(isend){
        		 formula_str[0] = ""+StapplyVO.ISEND+"->"+null+";";
        		 FormulaVO formulaVO1 = new FormulaVO();
        		 formulaVO1.setFormulas(formula_str);
        		 return formulaVO1;
        	 }
        }
		return null;
	}


	private String[] setPsndoc(PsnJobVO psn, ArrayList<String> newlist,
			ArrayList<String> oldlist,String pk_psnjob,HashMap<String, Object> head) throws NumberFormatException, BusinessException {
		Field[] files = StapplyVO.class.getDeclaredFields();
		String[] formula_str = new String[41];
		int i = 0;
		// 查询组织、部门、人员类别的权限
		HashMap<String, String> hm =
				NCLocator.getInstance().lookup(ITransmngQueryService.class).getPowerItem(pk_psnjob,Integer.parseInt(head.get("stapply_mode").toString()) == TRNConst.TRANSMODE_CROSS_OUT && TRNConst.BUSITYPE_TRANSITION.equals(head.get("pk_billtype")));
		for (Field f:files) {
			String key = f.getName();
			if(newlist.contains(key)||oldlist.contains(key)){
				formula_str[i] =""+key+"->"+psn.getAttributeValue(key.substring(3))+";";
				if(StapplyVO.NEWPOSTSTAT.equals(key)||StapplyVO.OLDPOSTSTAT.equals(key)){
					formula_str[i] =""+key+"->\""+psn.getAttributeValue(key.substring(3))+"\";";
				}
				if (newlist.contains(StapplyVO.NEWPK_PSNCL) && StapplyVO.NEWPK_PSNCL.equals(key))
		        {
					// 人员类别
					formula_str[i] =""+key+"->"+hm.get(PsnJobVO.PK_PSNCL)+";";					
		        }
		        if (newlist.contains(StapplyVO.NEWPK_DEPT) && StapplyVO.NEWPK_ORG.equals(key))
		        {
		            // 组织
		        	formula_str[i] =""+key+"->"+hm.get(PsnJobVO.PK_ORG)+";";
		        }
		        if (newlist.contains(StapplyVO.NEWPK_DEPT) && StapplyVO.NEWPK_DEPT.equals(key))
		        {
		            // 部门,没有组织权限就没有部门权限
		        	Object obj = hm.get(PsnJobVO.PK_ORG)==null?null:hm.get(PsnJobVO.PK_DEPT);
		        	formula_str[i] =""+key+"->"+ obj+";";
		        }
		        // 默认项目中没有职务,则职务为空,那么职务相关的就都为空
	            String pk_job = psn.getPk_job();
	            // 如果职务是默认的,则要对职务相关的项目进行赋值
	            JobVO job = pk_job == null ? null : getService().queryByPk(JobVO.class, pk_job, true);
	            if (job == null){	
	            	if(StapplyVO.NEWPK_JOBGRADE.equals(key)){formula_str[i] =""+key+"->"+null+";";}
	            	if(StapplyVO.NEWSERIES.equals(key)){formula_str[i] =""+key+"->"+null+";";}
	            }
	            else{
	                // 设置职务类别
	            	if(StapplyVO.NEWSERIES.equals(key)){formula_str[i] =""+key+"->"+job.getPk_jobtype()+";";}
	                String pk_jobgrade = psn.getPk_jobgrade();
	                if (newlist.contains(StapplyVO.NEWPK_JOBGRADE) && pk_jobgrade != null){
	                    // 职级是默认的,并且有值,则职级职等用职级数据,无论职等是否默认
	                    JobGradeVO grade = getService().queryByPk(JobGradeVO.class, pk_jobgrade, true);
	                    // FIXME: 该职级可能为空，而且此处空指针异常会被吞掉，heqiaoa 2014-12-15
	                    // 现在系统使用的职级为om_joblevel，而不是om_jobgrade，这里暂时做规避处理
	                    if (null != grade){
	                    	if(StapplyVO.NEWPK_JOBRANK.equals(key)){formula_str[i] =""+key+"->"+grade.getPk_jobrank()+";";}
	                    }
	                }
	                else{
	                    // 职级不是默认的或者职级没有数据,那职等使用职务上的数据,无论职等是否默认
	                    // getBillCardPanel().getHeadItem(StapplyVO.NEWPK_JOBRANK).setValue(job.getPk_jobrank());
	                }
	            }
		        // 如果默认的没有岗位,则岗位相关的都为空
	            String pk_post = psn.getPk_post();
	            PostVO post = pk_post == null ? null : getService().queryByPk(PostVO.class, pk_post, true);
	            if (post == null){
	            	if(StapplyVO.NEWPK_POSTSERIES.equals(key)){formula_str[i] =""+key+"->"+null+";";}
	            }
	            else{
	            	if(StapplyVO.NEWPK_POSTSERIES.equals(key)){formula_str[i] =""+key+"->"+post.getPk_postseries()+";";}
	            }
	            i++;
		        //----------------------------------------------------
			}
			// 设置选中的人员的pk_psndoc
			if(StapplyVO.PK_PSNDOC.equals(key)){formula_str[i] =""+key+"->"+psn.getPk_psndoc()+";";i++;}
			if(StapplyVO.PK_PSNORG.equals(key)){formula_str[i] =""+key+"->"+psn.getPk_psnorg()+";";i++;}
			if(StapplyVO.ASSGID.equals(key)){formula_str[i] =""+key+"->"+psn.getAssgid()+";";i++;}
	        Integer transMode = null;
	        transMode = (Integer) head.get(StapplyVO.STAPPLY_MODE);
	        // 原合同管理组织应该是什么时候都需要查委托关系
	        if (transMode == null || TRNConst.TRANSMODE_INNER == transMode || TRNConst.TRANSMODE_CROSS_OUT == transMode)
	        {
	            if(StapplyVO.PK_OLD_HI_ORG.equals(key)){formula_str[i] =""+key+"->"+HiSQLHelper.getEveryHrorg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psndoc)+";";i++;}
	        	if(StapplyVO.PK_OLD_HRCM_ORG.equals(key)){formula_str[i] =""+key+"->"+HiSQLHelper.getEveryHrorg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psnpact)+";";i++;}
	        }
	        else
	        {
	            // 调入是要查委托关系
	        	if(StapplyVO.PK_OLD_HI_ORG.equals(key)){formula_str[i] =""+key+"->"+HiSQLHelper.getHrorg(psn.getPk_psnorg(), psn.getAssgid())+";";i++;}
	        	if(StapplyVO.PK_OLD_HRCM_ORG.equals(key)){formula_str[i] =""+key+"->"+HiSQLHelper.getEveryHrorg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psnpact)+";";i++;}
	        }
	        String pk_hrcm_org_linshi = (String) head.get("pk_hrcm_org");
	        if (transMode == null || TRNConst.TRANSMODE_INNER == transMode || TRNConst.TRANSMODE_CROSS_IN == transMode)
	        {	
	        	if(StapplyVO.PK_HI_ORG.equals(key)){formula_str[i] =""+key+"->"+head.get("pk_org")+";";i++;}
				if(StapplyVO.PK_HRCM_ORG.equals(key)){formula_str[i] =""+key+"->"+head.get("pk_org")+";";i++;
					pk_hrcm_org_linshi =head.get("pk_org")==null?null:head.get("pk_org").toString(); 
				}
	        }
	        
	        String pk_psnorg = (String) psn.getPk_psnorg();
	        if (!StringUtils.isBlank(pk_psnorg)){
	        	CtrtVO ctrtvo = getQueryCtrtVO(pk_psnorg);
		        if (ctrtvo == null){// 最新的合同记录不是签订/变更/续签三个状态（未签订合同或者合同已经解除/终止），不需要勾选和编辑
		        	if(StapplyVO.ISRELEASE.equals(key)){formula_str[i] =""+key+"->"+null+";";i++;}// 解除
		        	if(StapplyVO.ISEND.equals(key)){formula_str[i] =""+key+"->"+null+";";i++;}// 终止
		        }else{
		        	int linshi = 0;
		        	 if (TRNConst.TRANSMODE_CROSS_OUT != Integer.parseInt(head.get("stapply_mode").toString()))
		             {// 不是调出情况
		        		 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString());
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[i] =""+key+"->\""+a+"\";";
		        	            	linshi+=1;
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[i] =""+key+"->\""+b+"\";";
		    		        		linshi+=1;
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[i] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            	linshi+=1;
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[i] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            	linshi+=1;
		        	            }
		        	        }
		             }
		        	 // 调出时清空调配后组织
		             String pk_old_hrcm_org =  HiSQLHelper.getEveryHrorg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psnpact);
		             if (StringUtils.isBlank(pk_old_hrcm_org) || StringUtils.isBlank(pk_hrcm_org_linshi))
		             {	 
		                 // 合同前组织肯定不会为空，那只有合同后组织为空的可能
		            	 if(StapplyVO.ISRELEASE.equals(key)){formula_str[i] =""+key+"->"+null+";";linshi+=1;}// 解除
				         if(StapplyVO.ISEND.equals(key)){formula_str[i] =""+key+"->"+null+";";linshi+=1;}// 终止
		             }else{
		            	 	UFLiteralDate effectdate = PuPubVO.getString_TrimZeroLenAsNull(head.get(StapplyVO.EFFECTDATE))==null?null:new UFLiteralDate(head.get(StapplyVO.EFFECTDATE).toString()); 
		        	        if (effectdate != null)
		        	        {
		        	            String isshow = getRelOrEndShow(ctrtvo, effectdate);
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	UFBoolean a = StapplyVO.ISRELEASE.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		        	            	formula_str[i] =""+key+"->\""+a+"\";";
		        	            	linshi+=1;
		        	            }// 解除
		    		        	if(StapplyVO.ISEND.equals(key)){
		    		        		UFBoolean b = StapplyVO.ISEND.equals(isshow) ? UFBoolean.TRUE : UFBoolean.FALSE;
		    		        		formula_str[i] =""+key+"->\""+b+"\";";
		    		        		linshi+=1;
		    		        	}// 终止
		        	        }
		        	        else{
		        	            // 当生效日期为空时，就不必要去判断这个人是否有合同或者应该解除或者终止合同
		        	            boolean isrelease = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISRELEASE), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISRELEASE.equals(key)){
		        	            	formula_str[i] =""+key+"->\""+(isrelease?isrelease:UFBoolean.FALSE)+"\";";
		        	            	linshi+=1;
		        	            }
		        	            boolean isend = PuPubVO.getUFBoolean_NullAs(head.get(StapplyVO.ISEND), UFBoolean.FALSE).booleanValue();
		        	            if(StapplyVO.ISEND.equals(key)){
		        	            	formula_str[i] =""+key+"->\""+(isend?isend:UFBoolean.FALSE)+"\";";
		        	            	linshi+=1;
		        	            }
		        	        }
		             }
		             if(linshi!=0){
		            	 i++;
		             }
		        }
	        }
	        
		}
		return formula_str;
	}

	@Override
	public Object processAction(String account, String userid, String billtype, String action, Object obj) throws BusinessException {
		pkbilltype = billtype;
//		if (action.equals(IMobileAction.AFTEREDIT)) {
//			return mrry(userid, obj);
//		}else {
			return super.processAction(account, userid, billtype, action, obj);
// 		}
	
	}

	private Object mrry(String userid, Object obj) throws DAOException {
		AfterEditVO aevo = (AfterEditVO) obj;
		BillVO bill = aevo.getVo();
		HashMap<String, Object> head = bill.getHeadVO();
		DefEventVO evevo = new DefEventVO();
		String sql= "select hi_psnjob.pk_psnjob from hi_psnjob  inner join  hi_psnorg on hi_psnorg.pk_psnorg = hi_psnjob.pk_psnorg inner join bd_psndoc on hi_psnorg.pk_psndoc = bd_psndoc.pk_psndoc"+
		" inner join sm_user on sm_user.pk_psndoc =  bd_psndoc.pk_psndoc where cuserid='"+userid+"'";
		BaseDAO dao = new BaseDAO();
		Object objvule = dao.executeQuery(sql,new ColumnProcessor());
		head.put("pk_psnjob",objvule);
		bill.setHeadVO(head);
		evevo.setVo(bill);
		evevo.setAction("SETDATA");
		return evevo;
	}

	@Override
	public Object save(String userid, Object obj) throws BusinessException {
		BillVO billvo = (BillVO) obj;
		String pk_billtype = (String) billvo.getHeadVO().get("pk_billtype");
		AggStapply aggvo = saveaggvo(userid, obj,pk_billtype);
		String pk = aggvo.getParentVO().getPrimaryKey();
		if(aggvo.getParentVO().getStatus()==2){
			getTrnManageService().insertBill(aggvo);
		}else{
			getTrnManageService().updateBill(aggvo, true);
		}
		return queryBillVoByPK(userid, pk);
	}
	
	public ITransmngManageService getTrnManageService() {
		return NCLocator.getInstance().lookup(ITransmngManageService.class);
	}
	
	public AggStapply saveaggvo(String userid, Object obj,String pk_billtype) throws BusinessException {
		BillVO billvo = (BillVO) obj;
		String pk_org = (String) billvo.getHeadVO().get("pk_org");
		String pk_group = (String) billvo.getHeadVO().get("pk_group");
		String ruleCode = pk_billtype.equals("6113")?"6113":"6115"; // nc存在编码规则定义节点
		StapplyVO hvo = new StapplyVO();
		AggStapply aggvo = new AggStapply();
		IdGenerator hid = new SequenceGenerator();
        String hpk= hid.generate();
		HashMap<String, Object> mhvo = billvo.getHeadVO();
		int hstatus = Integer.parseInt(mhvo.get("vostatus").toString());
		if(mhvo==null || mhvo.isEmpty()){
			throw new BusinessException("数据没有发生变化！");
		}
		Object ifaddblack= billvo.getHeadVO().get("ifaddblack");
		if(ifaddblack!=null && ("Y".equals(ifaddblack.toString()))){
			Object addreason= billvo.getHeadVO().get("addreason");//加入理由
			if(addreason ==null || "".equals(addreason.toString().trim())){
				throw new BusinessException("加入黑名单勾选后[加入理由]字段必须填写！");
			}
		}
		hvo.setStatus(hstatus);
		/*  调配申请单   */
		UFDateTime time = new UFDateTime();
		hvo.setPk_org(pk_org);
		hvo.setPk_group(pk_group);
		hvo.setBillmaker(mhvo.get("billmaker").toString());//申请人
		hvo.setApply_date(new UFLiteralDate(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("apply_date"))));//申请日期
		hvo.setApprove_state(-1);//审批状态
		hvo.setFun_code(pk_billtype.equals("6113")?"60090transapply":"60090dimissionapply");//制单节点编码
		String vcode = NCLocator.getInstance().lookup(IHrBillCode.class).getBillCode(ruleCode, pk_group,pk_org);//申请单编码
		hvo.setTranstype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.TRANSTYPE)));//流程类型code
		hvo.setTranstypeid(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.TRANSTYPEID)));//流程类型
		hvo.setPk_billtype(pk_billtype);//交易
		hvo.setIshrssbill(UFBoolean.TRUE);//是否自助
		hvo.setIsneedfile(UFBoolean.FALSE);//附件必传
		hvo.setBusiness_type(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.BUSINESS_TYPE)));//业务流程
		if (hstatus == VOStatus.NEW) {
			// 获取客户编码
			hvo.setBill_code(vcode);
			hvo.setCreationtime(time);
			hvo.setCreator(userid);
			hvo.setPk_hi_stapply(hpk);
		} else {
			String billMaker = mhvo.get("billmaker").toString();
			if (billMaker == null || INCSystemUserConst.NC_USER_PK.equals(billMaker)) {
				hvo.setBillmaker(PubEnv.getPk_user());
			}
			hvo.setPk_hi_stapply(mhvo.get("pk_hi_stapply").toString());
			hvo.setBill_code(mhvo.get("bill_code").toString());
			hvo.setModifier(userid.toString());
			hvo.setModifiedtime(time);
			hvo.setCreationtime(new UFDateTime(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("creationtime"))));
			hvo.setCreator(mhvo.get("creator").toString());
		}
		/* 人员信息   */
		hvo.setPk_psnjob(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_PSNJOB)));//调配人员
		hvo.setPk_psndoc(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_PSNDOC)));//人员主键
		hvo.setStapply_mode(Integer.parseInt(mhvo.get(StapplyVO.STAPPLY_MODE).toString()));//调配方式
		hvo.setPk_trnstype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_TRNSTYPE)));//调配业务类型
		hvo.setSreason(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.SREASON)));//调配原因
		hvo.setEffectdate(new UFLiteralDate(mhvo.get(StapplyVO.EFFECTDATE).toString()));//生效日期
		hvo.setTrial_flag(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.TRIAL_FLAG), UFBoolean.FALSE));//试用
		//试用勾选，根据试用期限，单位，开始日期，求出结束日期，否则期限、单位、开始日期清空
		if(hvo.getTrial_flag().booleanValue()){
			hvo.setTrialdays(Integer.parseInt(mhvo.get(StapplyVO.TRIALDAYS).toString()));//岗位试用期限
			hvo.setTrial_unit(Integer.parseInt(mhvo.get(StapplyVO.TRIAL_UNIT).toString()));//岗位试用单位
			hvo.setTrialbegindate(new UFLiteralDate(mhvo.get(StapplyVO.TRIALBEGINDATE).toString()));//试用开始日期
			hvo.setTrialenddate(HRCMTermUnitUtils.getDateAfterMonth(hvo.getTrialbegindate(), 
            		hvo.getTrialdays(), hvo.getTrial_unit()));//试用结束日期
		}else{
			hvo.setTrialdays(null);//岗位试用期限
			hvo.setTrial_unit(null);//岗位试用单位
			hvo.setTrialbegindate(null);//试用开始日期
			hvo.setTrialenddate(null);//试用结束日期
		}
		hvo.setMemo(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.MEMO)));//调配说明
//		hvo.setAssgid(Integer.parseInt(mhvo.get(StapplyVO.ASSGID)==null?null:mhvo.get(StapplyVO.ASSGID).toString()));//人员任职ID
		hvo.setPk_psnorg(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_PSNORG)));//人员组织关系
		/* 调配前信息   */
		hvo.setOldpk_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_ORG)));//组织
		hvo.setOldpk_psncl(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_PSNCL)));//人员类别
		hvo.setOldpk_dept(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_DEPT)));//部门
		hvo.setOldpk_post(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_POST)));//岗位
		hvo.setOldpk_postseries(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_POSTSERIES)));//岗位序列
		hvo.setOldpk_job(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_JOB)));//职务
		hvo.setOldpk_jobgrade(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_JOBGRADE)));//职级
		hvo.setOldpk_jobrank(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(mhvo.get(StapplyVO.OLDPK_JOBRANK))));//职等
		hvo.setOldseries(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDSERIES)));//职务类别
		hvo.setOldjobmode(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDJOBMODE)));//任职方式
		hvo.setOlddeposemode(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDDEPOSEMODE)));//免职方式
		hvo.setOldpoststat(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.OLDPOSTSTAT), UFBoolean.FALSE));//在岗
		hvo.setOldpk_job_type(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDPK_JOB_TYPE)));//任职类型
		hvo.setOldoccupation(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDOCCUPATION)));//职业
		hvo.setOldworktype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDWORKTYPE)));//工种
		hvo.setOldmemo(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.OLDMEMO)));//备注
		/* 调配后信息 */
		hvo.setNewpk_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_ORG)));//组织
		hvo.setNewpk_psncl(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_PSNCL)));//人员类别
		hvo.setNewpk_dept(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_DEPT)));//部门
		hvo.setNewpk_post(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_POST)));//岗位
		hvo.setNewpk_postseries(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_POSTSERIES)));//岗位序列
		hvo.setNewpk_job(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_JOB)));//职务
		hvo.setNewpk_jobgrade(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_JOBGRADE)));//职级
		hvo.setNewpk_jobrank(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(mhvo.get(StapplyVO.NEWPK_JOBRANK))));//职等
		hvo.setNewseries(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWSERIES)));//职务类别
		hvo.setNewjobmode(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWJOBMODE)));//任职方式
		hvo.setNewdeposemode(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWDEPOSEMODE)));//免职方式
		hvo.setNewpoststat(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.NEWPOSTSTAT), UFBoolean.FALSE));//在岗
		hvo.setNewpk_job_type(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWPK_JOB_TYPE)));//任职类型
		hvo.setNewoccupation(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWOCCUPATION)));//职业
		hvo.setNewworktype(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWWORKTYPE)));//工种
		hvo.setNewmemo(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.NEWMEMO)));//备注
		/* 调配后管理组织 */
		hvo.setPk_old_hi_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_OLD_HI_ORG)));//调配前人事组织
		hvo.setPk_hi_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_HI_ORG)));//调配后人事组织
		/* 合同管理组织 */
		hvo.setPk_old_hrcm_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_OLD_HRCM_ORG)));//原合同管理组织
		hvo.setPk_hrcm_org(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get(StapplyVO.PK_HRCM_ORG)));//新合同管理组织
		hvo.setIsend(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.ISEND), UFBoolean.FALSE));//终止
		hvo.setIsrelease(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.ISRELEASE), UFBoolean.FALSE));//解除
		/*执行信息*/
		if(hvo.getPk_billtype().equals("6113")){
			hvo.setIfendpart(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.IFENDPART), UFBoolean.FALSE));//结束兼职
			hvo.setIfsynwork(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.IFSYNWORK), UFBoolean.TRUE));//同步工作履历
			hvo.setIfaddpsnchg(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.IFADDPSNCHG), UFBoolean.TRUE));//跨行政组织调配是否增加流动记录
		}else {
			
			hvo.setIsdisablepsn(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.ISDISABLEPSN), UFBoolean.FALSE));//停用离职人员
			hvo.setIfaddblack(PuPubVO.getUFBoolean_NullAs(mhvo.get(StapplyVO.IFADDBLACK), UFBoolean.FALSE));//加入黑名单
			hvo.setAddreason(PuPubVO.getString_TrimZeroLenAsNull(mhvo.get("addreason")));//加入理由
		}
//		hvo.setAttributeValue("oldjobglbdef1", PuPubVO.getString_TrimZeroLenAsNull(billvo.getHeadVO().get("oldjobglbdef1")));
//		hvo.setAttributeValue("newjobglbdef1", PuPubVO.getString_TrimZeroLenAsNull(billvo.getHeadVO().get("newjobglbdef1")));
//		hvo.setAttributeValue("oldjobglbdef17", PuPubVO.getString_TrimZeroLenAsNull(billvo.getHeadVO().get("oldjobglbdef17")));
//		hvo.setAttributeValue("newjobglbdef17", PuPubVO.getString_TrimZeroLenAsNull(billvo.getHeadVO().get("newjobglbdef17")));
		aggvo.setParentVO(hvo);
		return aggvo;
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
		StringBuffer str = dealCondition(obj, true);
		String condition = str.toString().replace("stapply","hi_stapply").replace("pk_corp", "pk_org");
		String condition1 = condition.toString().replace("PPK", "pk_hi_stapply");		
		StringBuffer sb = new StringBuffer();
    	sb.append("SELECT * FROM (SELECT ROWNUM rw,T .*	FROM(SELECT hi_stapply.* FROM hi_stapply ")
		.append("WHERE NVL (hi_stapply.dr, 0) = 0 and pk_billtype = '"+(pkbilltype.equals("M006")?"6113":"6115")+"' "+condition1+"");
		sb.append(" ORDER BY hi_stapply.ts DESC) T)WHERE (billmaker = '"+userid+"' or approver = '"+userid+"'");
		if((pkbilltype.equals("M006")?"6113":"6115").equals("6113")){
			sb.append(" or ishrssbill = 'Y'");
		}
		sb.append( ") and rw BETWEEN "+startnum+" and "+endnum+"");
		@SuppressWarnings("unchecked")
		ArrayList<StapplyVO> list = (ArrayList<StapplyVO>) getQuery().executeQuery(sb.toString(), new BeanListProcessor(StapplyVO.class));
		if (list == null || list.size() == 0) {
			return null;
		}
		HashMap<String, Object>[] maps = transNCVOTOMap(list.toArray(new StapplyVO[0]));
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[list.size()];
		String[] formulas = new String[]{
				 "phone->getcolvalue(bd_psndoc,mobile,pk_psndoc,getcolvalue(hi_psnjob,pk_psndoc,pk_psnjob,pk_psnjob))"
				 };
		PubTools.execFormulaWithVOs(maps, formulas);
		for(int i=0;i<maps.length;i++){
			BillVO billVO = new BillVO();
			billVO.setTableVO("cmaterialid", maps);
			HashMap<String,Object> headVO = maps[i];
			if (maps[i].get("approve_state") == null||-1==Integer.parseInt(maps[i].get("approve_state").toString())) {
				headVO.put("ibillstatus", "-1");
			} 
			else if (3==Integer.parseInt(maps[i].get("approve_state").toString())) {
				headVO.put("ibillstatus", "3");
			}else if(2==Integer.parseInt(maps[i].get("approve_state").toString())){
				headVO.put("ibillstatus", "2");
			}
			else {
				headVO.put("ibillstatus", "1");
			}
			
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object queryPage_body(String userid, Object obj, int startnum, int endnum) throws BusinessException {
		return null;
	}

	public Object refreshData(Object aggvo) throws BusinessException {
		nc.vo.trn.transmng.AggStapply   vo = (nc.vo.trn.transmng.AggStapply  ) aggvo;
		StapplyVO hvo = (StapplyVO) vo.getParentVO();
		HashMap<String, Object>[] maps = transNCVOTOMap(new StapplyVO[] { hvo });
		QueryBillVO qbillVO = new QueryBillVO();
		BillVO[] billVOs = new BillVO[1];
		for (int i = 0; i < maps.length; i++) {
			BillVO billVO = new BillVO();
			HashMap<String, Object> headVO = maps[i];
			billVO.setHeadVO(headVO);
			billVOs[i] = billVO;
		}
		qbillVO.setQueryVOs(billVOs);
		return qbillVO;
	}

	@Override
	public Object delete(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_stapply = (String) bill.getHeadVO().get("pk_hi_stapply");
		String billtype = (String)bill.getHeadVO().get(StapplyVO.PK_BILLTYPE);
		AggStapply aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggStapply.class, pk_hi_stapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("DELETE", billtype, new AggStapply[]{aggvos}, null, null, null);
		return null;
	}

	@Override
	public Object submit(String userid, Object obj) throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_stapply = (String) bill.getHeadVO().get(StapplyVO.PK_HI_STAPPLY);
		String billtype = (String)bill.getHeadVO().get(StapplyVO.PK_BILLTYPE);
		AggStapply aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggStapply.class, pk_hi_stapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("SAVE", billtype, new AggStapply[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_hi_stapply);
	}

	/**
	 * 根据pk 查询出BilVO
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

		// 查询 表头
		QueryBillVO head_querybillVO = (QueryBillVO) this.queryPage(userid, condAggVO_head, 1, 1);
		BillVO head_BillVO = head_querybillVO.getQueryVOs()[0];
		billVO.setHeadVO(head_BillVO.getHeadVO());
		return billVO;
	}

	@Override
	public Object approve(String userid, Object obj) throws BusinessException {
		ApproveInfoVO ainfoVO = (ApproveInfoVO) obj;
		return approveBill(ainfoVO.getBillid(), ainfoVO.getPk_billtype(), ainfoVO.getState(), ainfoVO.getApprovenote(), ainfoVO.getDspVOs());
	}

	@Override
	public Object unapprove(String userid, Object obj) throws BusinessException {
		MApproveVO bill = (MApproveVO) obj;
		String pk_hi_stapply = (String) bill.getBillid() ;
		String billtype = (String)bill.getPk_billtype();
		AggStapply aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggStapply.class, pk_hi_stapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("UNAPPROVE", billtype, new AggStapply[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_hi_stapply);
	}

	@Override
	public Object unsavebill(String userid, Object obj)
			throws BusinessException {
		BillVO bill = (BillVO) obj;
		String pk_hi_stapply = (String) bill.getHeadVO().get(StapplyVO.PK_HI_STAPPLY);
		String billtype = (String)bill.getHeadVO().get(StapplyVO.PK_BILLTYPE);
		AggStapply aggvos = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(AggStapply.class, pk_hi_stapply, false);
		IPFBusiAction pf = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		pf.processBatch("RECALL", billtype, new AggStapply[]{aggvos}, null, null, null);
		return queryBillVoByPK(userid,pk_hi_stapply);
	}
	
    
    // 查询出人员是否有合同数据，以判断解除和终止的使用
    public CtrtVO getQueryCtrtVO(String pk_psnorg) throws BusinessException
    {
        CtrtVO ctrtvo = null;
        String condition = " pk_psnorg = '" + pk_psnorg + "' and lastflag = 'Y' and isrefer = 'Y' and conttype in (1,2,3) ";
        CtrtVO[] ctrtvos =
            (CtrtVO[]) NCLocator.getInstance().lookup(IPersistenceRetrieve.class).retrieveByClause(null, CtrtVO.class, condition);
        if (!ArrayUtils.isEmpty(ctrtvos))
        {
            ctrtvo = ctrtvos[0];
        }
        return ctrtvo;
    }
    
    // 查询出人员是否有合同数据，以判断解除和终止的使用
    public String getRelOrEndShow(CtrtVO ctrtvo, UFLiteralDate effectdate) throws BusinessException
    {
        String isshow = null;
        if (ctrtvo != null)
        {
            UFLiteralDate enddate = ctrtvo.getEnddate();
            if (effectdate.before(enddate))// 生效日期早于现有的合同的结束日期，需要解除合同
            {
                isshow = StapplyVO.ISRELEASE;
            }
            else
            // 生效日期等于或者晚于现有的合同的结束日期，需要终止合同
            {
                isshow = StapplyVO.ISEND;
            }
        }
        return isshow;
    }
}
