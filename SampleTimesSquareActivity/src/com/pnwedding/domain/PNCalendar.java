package com.pnwedding.domain;

public class PNCalendar {
	public String _id;
	public String account_name;
	public String calendar_displayName;
	public String ownerAccount;
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		this._id = _id;
	}
	public String getAccount_name() {
		return account_name;
	}
	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}
	public String getCalendar_displayName() {
		return calendar_displayName;
	}
	public void setCalendar_displayName(String calendar_displayName) {
		this.calendar_displayName = calendar_displayName;
	}
	public String getOwnerAccount() {
		return ownerAccount;
	}
	public void setOwnerAccount(String ownerAccount) {
		this.ownerAccount = ownerAccount;
	}
	
	
}
