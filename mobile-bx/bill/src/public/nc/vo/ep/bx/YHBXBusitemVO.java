package nc.vo.ep.bx;

import nc.vo.pub.lang.UFDouble;

public class YHBXBusitemVO extends BXBusItemVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 968418517807939101L;
	
	public   UFDouble tni_amount ;
	public   UFDouble tax_amount;
	public   UFDouble  vat_amount;
	public   UFDouble orgtni_amount;
	public   UFDouble orgtax_amount;
	public   UFDouble orgvat_amount;
	
	public UFDouble getTni_amount() {
		return tni_amount;
	}
	public void setTni_amount(UFDouble tni_amount) {
		this.tni_amount = tni_amount;
	}
	public UFDouble getTax_amount() {
		return tax_amount;
	}
	public void setTax_amount(UFDouble tax_amount) {
		this.tax_amount = tax_amount;
	}
	public UFDouble getVat_amount() {
		return vat_amount;
	}
	public void setVat_amount(UFDouble vat_amount) {
		this.vat_amount = vat_amount;
	}
	public UFDouble getOrgtni_amount() {
		return orgtni_amount;
	}
	public void setOrgtni_amount(UFDouble orgtni_amount) {
		this.orgtni_amount = orgtni_amount;
	}
	public UFDouble getOrgtax_amount() {
		return orgtax_amount;
	}
	public void setOrgtax_amount(UFDouble orgtax_amount) {
		this.orgtax_amount = orgtax_amount;
	}
	public UFDouble getOrgvat_amount() {
		return orgvat_amount;
	}
	public void setOrgvat_amount(UFDouble orgvat_amount) {
		this.orgvat_amount = orgvat_amount;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	

}
