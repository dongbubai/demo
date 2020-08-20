package nc.vo.mobile.record;

import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.mobile.record.Record")

public class AggRecord extends AbstractBill {
	
	  @Override
	  public IBillMeta getMetaData() {
	  	IBillMeta billMeta =BillMetaFactory.getInstance().getBillMeta(AggRecordMeta.class);
	  	return billMeta;
	  }
	    
	  @Override
	  public Record getParentVO(){
	  	return (Record)this.getParent();
	  }
	  
}