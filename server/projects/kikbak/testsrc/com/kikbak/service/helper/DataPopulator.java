package com.kikbak.service.helper;

import java.util.Date;


import com.kikbak.dao.ReadWriteLocationDAO;
import com.kikbak.dao.ReadWriteMerchantDAO;
import com.kikbak.dao.ReadWriteOfferDAO;
import com.kikbak.dao.ReadWriteSharedDAO;
import com.kikbak.dao.ReadWriteUser2FriendDAO;
import com.kikbak.dto.Location;
import com.kikbak.dto.Merchant;
import com.kikbak.dto.Offer;
import com.kikbak.dto.Shared;
import com.kikbak.dto.User2friend;


public class DataPopulator {

	ReadWriteOfferDAO rwOfferDao;
	ReadWriteMerchantDAO rwMerchantDao;
	ReadWriteUser2FriendDAO rwU2FDao;
	ReadWriteSharedDAO rwSharedDao;
	ReadWriteLocationDAO rwLocationDao;
	

	public DataPopulator(ReadWriteOfferDAO rwOfferDao, ReadWriteMerchantDAO rwMerchantDao,
						ReadWriteUser2FriendDAO rwU2FDao, ReadWriteSharedDAO rwSharedDao, ReadWriteLocationDAO rwLocationDao){
		this.rwMerchantDao = rwMerchantDao;
		this.rwOfferDao = rwOfferDao;
		this.rwSharedDao = rwSharedDao;
		this.rwU2FDao = rwU2FDao;
		this.rwLocationDao = rwLocationDao;
	}
	
	public void pupulateDataForGiftTest(int count, int userId){
		long merchantBaseId = createMerchants(count);
		createLocations(count, merchantBaseId);
		long offerBaseId = createOffers(count, merchantBaseId);
		createFriendsForUser(count, userId);
		createShared(count, userId, merchantBaseId, offerBaseId);
		
	}
	
	protected long createOffers(int count, long merchantBaseId){
		long ret = 0;
		for(long i = 0; i <  count; i++)
		{
			Date now = new Date();
			Offer offer = new Offer();
			offer.setBeginDate(new Date(now.getTime() - 999999999));
			offer.setEndDate(new Date(now.getTime() + 999999999));
			offer.setDefaultText("default");
			offer.setDescription("desc");
			offer.setGiftDescription("gift");
			offer.setGiftName("gn");
			offer.setGiftValue(12.21);
			offer.setGiftNotificationText("notification");
			offer.setKikbakDescription("kd");
			offer.setKikbakName("kn");
			offer.setKikbakValue(32.32);
			offer.setKikbakNotificationText("notification");
			offer.setName("name");
			offer.setMerchantId(i + merchantBaseId);
			rwOfferDao.makePersistent(offer);
			
			if( i == 0 ){
				ret = offer.getId();
			}
		}
		
		return ret;
	}
	
	protected long createMerchants(int count){
		long ret = 0;
		for(int i = 0; i < count; i++ ){
			Merchant merchant = new Merchant();
			merchant.setDescription("desc");
			merchant.setImageUrl("url");
			merchant.setName("name");
			merchant.setUrl("url");

			rwMerchantDao.makePersistent(merchant);
			if( i == 0)
				ret = merchant.getId();
		}
		
		return ret;
	}
	
	protected void createFriendsForUser(int count, long userId){
		for(int i = 0; i < count; i++){
			User2friend u2f = new User2friend();
			u2f.setFacebookFriendId(userId);
			u2f.setUserId(i+userId);
			
			rwU2FDao.makePersistent(u2f);
		}
	}
	
	protected void createShared(int count, long userId, long merchantIdBase, long offerBaseId){
		for(long i = 0; i < count; i ++){
			Shared shared = new Shared();
			shared.setLocationId(12);
			shared.setMerchantId(merchantIdBase + i);
			shared.setOfferId(offerBaseId + i);
			shared.setUserId(userId + i);
			shared.setSharedDate(new Date());
			
			rwSharedDao.makePersistent(shared);
		}
	}
	
	protected void createLocations(int count, long merchantBaseId){
		for( long i = 0; i < count; i++){
			Location location = new Location();
			location.setAddress1("add");
			location.setAddress2("add2");
			location.setCity("city");
			location.setLatitude(12.3231);
			location.setLongitude(123.21);
			location.setMerchantId(merchantBaseId + i);
			location.setState("st");
			location.setVerificationCode("12345");
			location.setZipcode(12345);
			
			rwLocationDao.makePersistent(location);
		}
		
	}
}