package com.kikbak.admin.dao;

import com.kikbak.dto.Offer;

public interface ReadWriteOfferDAO {
	
	public void makePersistent(Offer offer);
	
	public void makeTransient(Offer offer);

}
