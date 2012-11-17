package com.kikbak.admin.dao;

import java.util.Collection;

import com.kikbak.dto.Merchant;
import com.kikbak.dto.Offer;

public interface ReadOnlyOfferDAO {
	
	public Offer findById(Long id);
	
	public Collection<Offer> findValidOffers(Merchant merchant);

}
