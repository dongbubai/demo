package hd.vo.bill.bxbd;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;

public class JKControlVO extends SuperVO{
	
	String controlattr,controlstyle,currency,dr,paracode,paraname,
	pk_control,pk_group,pk_org,ts,pk_controlmode1,pk_controlmode2,pk_controlmode3
	,pk_controlmode4,pk_controlmodedef1,pk_controlmodedef2,pk_controlmodedef3,
	pk_controlmodedef4,value1,value2,value3,balatype,djlxbm,pk_controlschema;
	UFBoolean value4,bbcontrol;

	public String getParentPKFieldName() {
		return null;
	}

	public String getPKFieldName() {

		return pk_control;
	}

	public String getTableName() {

		return null;
	}

	public UFBoolean getBbcontrol() {
		return bbcontrol;
	}

	public void setBbcontrol(UFBoolean bbcontrol) {
		this.bbcontrol = bbcontrol;
	}

	public String getControlattr() {
		return controlattr;
	}

	public void setControlattr(String controlattr) {
		this.controlattr = controlattr;
	}

	public String getControlstyle() {
		return controlstyle;
	}

	public void setControlstyle(String controlstyle) {
		this.controlstyle = controlstyle;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDr() {
		return dr;
	}

	public void setDr(String dr) {
		this.dr = dr;
	}

	public String getParacode() {
		return paracode;
	}

	public void setParacode(String paracode) {
		this.paracode = paracode;
	}

	public String getParaname() {
		return paraname;
	}

	public void setParaname(String paraname) {
		this.paraname = paraname;
	}

	public String getPk_control() {
		return pk_control;
	}

	public void setPk_control(String pk_control) {
		this.pk_control = pk_control;
	}

	public String getPk_group() {
		return pk_group;
	}

	public void setPk_group(String pk_group) {
		this.pk_group = pk_group;
	}

	public String getPk_org() {
		return pk_org;
	}

	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getPk_controlmode1() {
		return pk_controlmode1;
	}

	public void setPk_controlmode1(String pk_controlmode1) {
		this.pk_controlmode1 = pk_controlmode1;
	}

	public String getPk_controlmode2() {
		return pk_controlmode2;
	}

	public void setPk_controlmode2(String pk_controlmode2) {
		this.pk_controlmode2 = pk_controlmode2;
	}

	public String getPk_controlmode3() {
		return pk_controlmode3;
	}

	public void setPk_controlmode3(String pk_controlmode3) {
		this.pk_controlmode3 = pk_controlmode3;
	}

	public String getPk_controlmode4() {
		return pk_controlmode4;
	}

	public void setPk_controlmode4(String pk_controlmode4) {
		this.pk_controlmode4 = pk_controlmode4;
	}

	public String getPk_controlmodedef1() {
		return pk_controlmodedef1;
	}

	public void setPk_controlmodedef1(String pk_controlmodedef1) {
		this.pk_controlmodedef1 = pk_controlmodedef1;
	}

	public String getPk_controlmodedef2() {
		return pk_controlmodedef2;
	}

	public void setPk_controlmodedef2(String pk_controlmodedef2) {
		this.pk_controlmodedef2 = pk_controlmodedef2;
	}

	public String getPk_controlmodedef3() {
		return pk_controlmodedef3;
	}

	public void setPk_controlmodedef3(String pk_controlmodedef3) {
		this.pk_controlmodedef3 = pk_controlmodedef3;
	}

	public String getPk_controlmodedef4() {
		return pk_controlmodedef4;
	}

	public void setPk_controlmodedef4(String pk_controlmodedef4) {
		this.pk_controlmodedef4 = pk_controlmodedef4;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	public String getValue3() {
		return value3;
	}

	public void setValue3(String value3) {
		this.value3 = value3;
	}
 
	public UFBoolean getValue4() {
		return value4;
	}

	public void setValue4(UFBoolean value4) {
		this.value4 = value4;
	}

	public String getBalatype() {
		return balatype;
	}

	public void setBalatype(String balatype) {
		this.balatype = balatype;
	}

	public String getDjlxbm() {
		return djlxbm;
	}

	public void setDjlxbm(String djlxbm) {
		this.djlxbm = djlxbm;
	}

	public String getPk_controlschema() {
		return pk_controlschema;
	}

	public void setPk_controlschema(String pk_controlschema) {
		this.pk_controlschema = pk_controlschema;
	}

}
