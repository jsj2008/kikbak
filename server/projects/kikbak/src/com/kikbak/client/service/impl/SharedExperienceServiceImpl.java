package com.kikbak.client.service.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kikbak.client.service.v1.RateLimitException;
import com.kikbak.client.service.v1.ReferralCodeUniqueException;
import com.kikbak.client.service.v1.SharedExperienceService;
import com.kikbak.dao.ReadOnlyAllocatedGiftDAO;
import com.kikbak.dao.ReadOnlyCreditDAO;
import com.kikbak.dao.ReadOnlyGiftDAO;
import com.kikbak.dao.ReadOnlyLocationDAO;
import com.kikbak.dao.ReadOnlyOfferDAO;
import com.kikbak.dao.ReadOnlySharedDAO;
import com.kikbak.dao.ReadOnlyZipcodeDAO;
import com.kikbak.dao.ReadWriteRedeemZipcodeDAO;
import com.kikbak.dao.ReadWriteSharedDAO;
import com.kikbak.dao.enums.Channel;
import com.kikbak.dao.enums.OfferType;
import com.kikbak.dto.Credit;
import com.kikbak.dto.Gift;
import com.kikbak.dto.Offer;
import com.kikbak.dto.Redeemzipcode;
import com.kikbak.dto.Shared;
import com.kikbak.push.service.PushNotifier;

@Service
public class SharedExperienceServiceImpl implements SharedExperienceService {

    private static final int DEFAULT_RANDOM_SECRET_LENGTH = 6;

    private static final String RANDOM_SECRET_LENGTH = "gift.redeem.code.length";

    @Autowired
    private PropertiesConfiguration config;

    @Autowired
    @Qualifier("ReadOnlySharedDAO")
    ReadOnlySharedDAO roSharedDao;

    @Autowired
    @Qualifier("ReadWriteSharedDAO")
    ReadWriteSharedDAO rwSharedDao;

    @Autowired
    PushNotifier pushNotifier;

    @Autowired
    ReadOnlyGiftDAO roGiftDAO;

    @Autowired
    private ReadOnlyOfferDAO roOfferDAO;

    @Autowired
    private ReadOnlyAllocatedGiftDAO roAllocatedGiftDAO;
    
    @Autowired
    private ReadOnlyCreditDAO roCreditDAO;
    
    @Autowired
    private ReadOnlyLocationDAO roLocationDAO;

    @Autowired
    private ReadOnlyZipcodeDAO roZipcodeDAO;
    
    @Autowired
    private ReadWriteRedeemZipcodeDAO rwRedeemZipcode;

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String registerSharingAndNotify(ShareInfo share) throws RateLimitException {
        String referralCode = registerSharing(share);
        Gift gift = roGiftDAO.findByOfferId(share.offerId);
        pushNotifier.sendGiftNotification(share.userId, gift);
        return referralCode;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String registerSharing(ShareInfo share) throws RateLimitException {

        Offer offer = roOfferDAO.findById(share.offerId);

        if( share.zipCode != null && !roZipcodeDAO.isValidZipcode(offer.getMerchantId(), share.zipCode)){
        	throw new IllegalArgumentException();
        }
        
        
        if (OfferType.both.name().equals(offer.getOfferType())) {
            checkMaxRedeemCountReached(share.userId, share.offerId);
        }

        Shared shared = new Shared();
        shared.setUserId(share.userId);
        shared.setOfferId(share.offerId);
        shared.setMerchantId(offer.getMerchantId());
        shared.setCaption(share.caption);
        shared.setImageUrl(share.imageUrl);
        shared.setLocationId(share.locationId);
        shared.setEmployeeId(share.employeeId);
        shared.setEmail(share.email);
        shared.setPhonenumber(share.phoneNumber);
        shared.setType(share.channel.name());
        shared.setSharedDate(new Date());
        shared.setZipcode(share.zipCode);
        shared.setLatitude(share.latitude);
        shared.setLongitude(share.longitude);

        int maxLength = config.getInt(RANDOM_SECRET_LENGTH, DEFAULT_RANDOM_SECRET_LENGTH);
        while (true) {
            try {
                String referralCode = generateReferralCode(maxLength);
                shared.setReferralCode(referralCode);
                rwSharedDao.save(shared);
                return referralCode;
            } catch (ReferralCodeUniqueException e) {
                ++maxLength;
            }
            if (maxLength > 64) {
                throw new RuntimeException("Cannot find a unique referral code");
            }
        }
    }

    private void checkMaxRedeemCountReached(Long userId, Long offerId) throws RateLimitException {
        Credit credit = roCreditDAO.findByUserIdAndOfferId(userId, offerId);
        Offer offer = roOfferDAO.findById(offerId);
        if (credit != null && credit.getRedeemCount() >= offer.getRedeemLimit()) {
            throw new RateLimitException("User can no longer share!");
        }
    }

    private String generateReferralCode(int maxLength) {
        String randomStr = new BigInteger(30, new SecureRandom()).toString(Character.MAX_RADIX);
        if (randomStr.length() < maxLength) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maxLength - randomStr.length(); i++) {
                sb.append("0");
            }
            randomStr = sb.toString() + randomStr;
        }
        return randomStr;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void addShareType(String code, Channel channel) {
        Shared shared = roSharedDao.findByReferralCode(code);
        if (shared == null)
            throw new IllegalArgumentException("Shared not found for code:" + code);

        StringBuffer sb = new StringBuffer(shared.getType());
        sb.append(";");
        sb.append(channel.name());
        shared.setType(sb.toString());

        try {
            rwSharedDao.save(shared);
        } catch (ReferralCodeUniqueException e) {
            // should never happen
            throw new RuntimeException("oops");
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public boolean validateZipCodeEligibility(long offerId, String zipCode, Boolean isLandingPage) {
    	
    	Redeemzipcode code = new Redeemzipcode();
    	code.setZipcode(Integer.parseInt(zipCode));
    	rwRedeemZipcode.makePersistent(code);
    	
    	Offer offer = roOfferDAO.findById(offerId);
        return roZipcodeDAO.isValidZipcode(offer.getMerchantId(), zipCode);
    }

}
