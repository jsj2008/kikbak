package com.kikbak.dao;

import com.kikbak.dto.Location;

public interface ReadWriteLocationDAO {

	public void makePersistent(Location location);
	
	public void makeTransient(Location location);
}
