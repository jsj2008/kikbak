package com.kikbak.client.service.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kikbak.client.service.RedemptionException;
import com.kikbak.client.service.RewardService;
import com.kikbak.client.service.impl.types.TransactionType;
import com.kikbak.dao.ReadOnlyAllocatedGiftDAO;
import com.kikbak.dao.ReadOnlyCreditDAO;
import com.kikbak.dao.ReadOnlyDeviceTokenDAO;
import com.kikbak.dao.ReadOnlyLocationDAO;
import com.kikbak.dao.ReadOnlyMerchantDAO;
import com.kikbak.dao.ReadOnlyOfferDAO;
import com.kikbak.dao.ReadOnlySharedDAO;
import com.kikbak.dao.ReadOnlyTransactionDAO;
import com.kikbak.dao.ReadOnlyUserDAO;
import com.kikbak.dao.ReadWriteAllocatedGiftDAO;
import com.kikbak.dao.ReadWriteCreditDAO;
import com.kikbak.dao.ReadWriteTransactionDAO;
import com.kikbak.dto.Allocatedgift;
import com.kikbak.dto.Credit;
import com.kikbak.dto.Devicetoken;
import com.kikbak.dto.Location;
import com.kikbak.dto.Merchant;
import com.kikbak.dto.Offer;
import com.kikbak.dto.Shared;
import com.kikbak.dto.Transaction;
import com.kikbak.dto.User;
import com.kikbak.jaxb.redeemgift.GiftRedemptionType;
import com.kikbak.jaxb.redeemkikbak.KikbakRedemptionResponseType;
import com.kikbak.jaxb.redeemkikbak.KikbakRedemptionType;
import com.kikbak.jaxb.rewards.ClientLocationType;
import com.kikbak.jaxb.rewards.ClientMerchantType;
import com.kikbak.jaxb.rewards.GiftType;
import com.kikbak.jaxb.rewards.KikbakType;
import com.kikbak.push.service.ApsNotifier;

@Service
public class RewardServiceImpl implements RewardService{

	@Autowired
	ReadOnlyAllocatedGiftDAO roGiftDao;
	
	@Autowired
	ReadWriteAllocatedGiftDAO rwGiftDao;
	
	@Autowired
	ReadOnlyCreditDAO roCreditDao;
	
	@Autowired
	ReadWriteCreditDAO rwKikbakDao;
	
	@Autowired
	ReadOnlyOfferDAO roOfferDao;
	
	@Autowired
	ReadOnlyUserDAO roUserDao;
	
	@Autowired
	ReadOnlyMerchantDAO roMerchantDao;
	
	@Autowired
	ReadOnlyTransactionDAO roTxnDao;
	
	@Autowired
	ReadWriteTransactionDAO rwTxnDao;
	
	@Autowired
	ReadOnlySharedDAO roSharedDao;
	
	@Autowired
	ReadOnlyLocationDAO roLocationDao;
	
	@Autowired
	ApsNotifier apsNotifier;
	
	@Autowired
	ReadOnlyDeviceTokenDAO roDeviceToken;
	
	
	private final SecureRandom random = new SecureRandom();
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Collection<GiftType> getGifts(Long userId) {
		createGifts(userId);
		Collection<Allocatedgift> gifts = new ArrayList<Allocatedgift>();
		gifts.addAll(roGiftDao.listValidByUserId(userId));
		Collection<GiftType> gts = new ArrayList<GiftType>();
		
		for( Allocatedgift gift: gifts){
			GiftType gt = new GiftType();
			gt.setId(gift.getId());
			
			Merchant merchant = roMerchantDao.findById(gift.getMerchantId());
			ClientMerchantType cmt = fillClientMerchantType(merchant);
			gt.setMerchant(cmt);
			
			Offer offer = roOfferDao.findById(gift.getOfferId());
			gt.setDesc(offer.getGiftDesc());
			gt.setDescOptional(offer.getGiftOptionalDesc());
			gt.setValue(offer.getGiftValue());
			gt.setType(offer.getGiftType());
			gt.setName(offer.getGiftName());
			gt.setFriendUserId(gift.getFriendUserId());
			User friend = roUserDao.findById(gift.getFriendUserId());
			gt.setFbFriendId(friend.getFacebookId());
			gt.setFriendName(friend.getFirstName() + " " + friend.getLastName());
			Shared shared = roSharedDao.findById(gift.getSharedId());
			gt.setCaption(shared.getCaption());
			gt.setFbImageId(shared.getFbImageId());
			
			gts.add(gt);
		}
		
		return gts;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Collection<KikbakType> getCredits(Long userId) {
		Collection<Credit> credits = roCreditDao.listCreditsWithBalance(userId);
		Collection<KikbakType> kts = new ArrayList<KikbakType>();
		
		for( Credit credit : credits){
		    KikbakType kt = new KikbakType();
			kt.setValue(credit.getValue());
			kt.setId(credit.getId());
			
			Merchant merchant = roMerchantDao.findById(credit.getMerchantId());
			ClientMerchantType cmt = fillClientMerchantType(merchant);
			kt.setMerchant(cmt);
			
			Offer offer = roOfferDao.findById(credit.getOfferId());
			kt.setDesc(offer.getKikbakDesc());
			kt.setDescOptional(offer.getKikbakOptionalDesc());
			kt.setName(offer.getKikbakName());			
			kt.setRedeeemedGiftsCount(roTxnDao.countOfGiftsRedeemedByUserByMerchant(userId, credit.getMerchantId()));
			
			kts.add(kt);
		}
		
		return kts;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public String registerGiftRedemption(final Long userId, GiftRedemptionType giftType) throws Exception {
		
		Location location = roLocationDao.findById(giftType.getLocationId());
		if( !location.getVerificationCode().equals(giftType.getVerificationCode())){
			throw new RedemptionException("Invalid verifcation code");
		}
		
		Allocatedgift gift = roGiftDao.findById(giftType.getId());
		gift.setRedemptionDate(new Date());
		gift.setValue(0.0);
		
		rwGiftDao.makePersistent(gift);
		
		CreditManager km = new CreditManager(roOfferDao, roCreditDao, rwKikbakDao, rwTxnDao);
		km.manageCredit(giftType.getFriendUserId(), gift.getOfferId(), gift.getMerchantId(), giftType.getLocationId());
		
		//send notification for kikbak when gift is redeemed
		Devicetoken token = roDeviceToken.findByUserId(userId);
		if( token != null){
			Offer offer = roOfferDao.findById(gift.getOfferId());
			apsNotifier.sendNotification(token, offer.getKikbakNotificationText());
		}
		
		return generateAuthorizationCode();
	}

	@Override
	public KikbakRedemptionResponseType registerKikbakRedemption(final Long userId, KikbakRedemptionType kikbakType) throws Exception {
		
		Location location = roLocationDao.findById(kikbakType.getLocationId());
		if( !location.getVerificationCode().equals(kikbakType.getVerificationCode())){
			throw new RedemptionException("Invalid verifcation code");
		}
		
		Credit credit = roCreditDao.findById(kikbakType.getId());
		if( kikbakType.getAmount() > credit.getValue() ){
			throw new RedemptionException("Value of attempted redemption is greater then remaining value");
		}
		
		credit.setValue(credit.getValue() - kikbakType.getAmount());
		
		Transaction txn = new Transaction();
		txn.setAmount(kikbakType.getAmount());
		txn.setVerificationCode(kikbakType.getVerificationCode());
		txn.setMerchantId(credit.getMerchantId());
		txn.setLocationId(txn.getLocationId());
		txn.setUserId(userId);
		txn.setTransactionType((short)TransactionType.Debit.ordinal());
		txn.setOfferId(credit.getOfferId());
		txn.setCreditId(credit.getId());
		txn.setDate(new Date());
		txn.setAuthorizationCode(generateAuthorizationCode());
		
		rwKikbakDao.makePersistent(credit);
		rwTxnDao.makePersistent(txn);
		
		KikbakRedemptionResponseType response = new KikbakRedemptionResponseType();
		response.setBalance(credit.getValue());
		response.setAuthorizationCode(txn.getAuthorizationCode());
		return response;
	}
	
	
	protected void createGifts(Long userId){

		Collection<Long> offerIds = roGiftDao.listOfferIdsForUser(userId);
		User user = roUserDao.findById(userId);
		Collection<Shared> shareds = roSharedDao.listAvailableForGifting(user.getFacebookId());
		Collection<Allocatedgift> newGifts = new ArrayList<Allocatedgift>();
		for(Shared shared : shareds){
			if(!offerIds.contains(shared.getOfferId())){
				Offer offer = roOfferDao.findById(shared.getOfferId());
				Allocatedgift gift = new Allocatedgift();
				gift.setExpirationDate(offer.getEndDate());
				gift.setFriendUserId(shared.getUserId());
				gift.setMerchantId(shared.getMerchantId());
				gift.setOfferId(shared.getOfferId());
				gift.setUserId(userId);
				gift.setSharedId(shared.getId());
				gift.setValue(offer.getGiftValue());

				rwGiftDao.makePersistent(gift);
				newGifts.add(gift);
				
				offerIds.add(shared.getOfferId());
			}
		}
	}

	
	protected String generateAuthorizationCode(){
		return new BigInteger(16,random).toString(16);
	}
	
	protected ClientMerchantType fillClientMerchantType(Merchant merchant){
		ClientMerchantType cmt = new ClientMerchantType();
		cmt.setId(merchant.getId());
		cmt.setName(merchant.getName());
		cmt.setUrl(merchant.getUrl());
		cmt.setImageUrl(merchant.getImageUrl());
		
		Collection<Location> locations = roLocationDao.listByMerchant(merchant.getId());
		for(Location location: locations){
			ClientLocationType clt = new ClientLocationType();
			clt.setLocationId(location.getId());
			clt.setLatitude(location.getLatitude());
			clt.setLongitude(location.getLongitude());
			clt.setPhoneNumber(location.getPhoneNumber());
			cmt.getLocations().add(clt);
		}
		return cmt;
	}
}
