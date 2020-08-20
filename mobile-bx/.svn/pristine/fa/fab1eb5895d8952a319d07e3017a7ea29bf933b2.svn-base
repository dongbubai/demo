package nc.vo.hm.hm02;

import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.hm.hm02.AttendanceRecordVO")

public class AggAttendanceRecordVO extends AbstractBill {
	
	  @Override
	  public IBillMeta getMetaData() {
	  	IBillMeta billMeta =BillMetaFactory.getInstance().getBillMeta(AggAttendanceRecordVOMeta.class);
	  	return billMeta;
	  }
	    
	  @Override
	  public AttendanceRecordVO getParentVO(){
	  	return (AttendanceRecordVO)this.getParent();
	  }
	  
}