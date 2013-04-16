package com.kikbak.dao.impl;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kikbak.dao.ReadOnlyGiftDAO;
import com.kikbak.dao.generic.ReadOnlyGenericDAOImpl;
import com.kikbak.dto.Gift;

public class ReadOnlyGiftDAOImpl extends ReadOnlyGenericDAOImpl<Gift, Long> implements ReadOnlyGiftDAO {

	private static final String gift_offer_ids = "select offer_id from gift where user_id=?";
	
	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public Collection<Gift> listValidByUserId(Long userId) {
		return listByCriteria(Restrictions.and(Restrictions.eq("userId", userId), Restrictions.isNull("redemptionDate")));
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public Collection<Gift> listByMerchantId(Long merchantId) {
		return listByCriteria(Restrictions.eq("merchantId", merchantId));
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public Collection<Gift> listByOfferId(Long offerId) {
		return listByCriteria(Restrictions.eq("offerId", offerId));
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public Collection<Long> listOfferIdsForUser(Long userId) {
		return sessionFactory.getCurrentSession().createSQLQuery(gift_offer_ids).addScalar("offer_id",  StandardBasicTypes.LONG ).setLong(0, userId).list();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public Collection<Gift> listByFriendUserId(Long friendId) {
		return listByCriteria(Restrictions.eq("friendUserId", friendId));
	}

	
}
