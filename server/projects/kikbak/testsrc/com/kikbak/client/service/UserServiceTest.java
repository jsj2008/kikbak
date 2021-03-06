package com.kikbak.client.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.kikbak.KikbakBaseTest;
import com.kikbak.admin.service.MerchantService;
import com.kikbak.client.service.v1.UserService;
import com.kikbak.dao.ReadOnlyDeviceTokenDAO;
import com.kikbak.dao.ReadOnlyUser2FriendDAO;
import com.kikbak.dao.ReadOnlyUserDAO;
import com.kikbak.dto.Devicetoken;
import com.kikbak.jaxb.v1.admin.LocationType;
import com.kikbak.jaxb.v1.admin.MerchantType;
import com.kikbak.jaxb.v1.admin.OfferType;
import com.kikbak.jaxb.v1.devicetoken.DeviceTokenType;
import com.kikbak.jaxb.v1.friends.FriendType;
import com.kikbak.jaxb.v1.register.UserType;

public class UserServiceTest extends KikbakBaseTest{

	@Autowired
	UserService service;
	
	@Autowired
	ReadOnlyUserDAO roUserDao;
	
	@Autowired
	ReadOnlyUser2FriendDAO roU2FDao;
	
	@Autowired
	ReadOnlyDeviceTokenDAO roDeviceTokenDao;
	
	@Autowired
	MerchantService merchantService;
	
	
	@Test
	public void testPersistDeviceToken(){
		DeviceTokenType tt = new DeviceTokenType();
		tt.setPlatformId((short)0);
		tt.setToken("2142");
		service.persistDeviceToken(5L, tt);
		
		Devicetoken token = roDeviceTokenDao.findByUserId(5L);
		assertEquals(tt.getToken(), token.getToken());
	}
	
	@Test
	public void testGetOffers(){
		MerchantType mt = new MerchantType();
		mt.setDescription("test");
		mt.setImageUrl("url");
		mt.setName("test");
		mt.setUrl("url");
		mt.getLocations().add(fillLocationType());
		mt.getLocations().add(fillLocationType());
		
		OfferType ot = new OfferType();
		ot.setBeginDate(new Date().getTime() - 100000);
		ot.setEndDate(new Date().getTime() + 100000);
		ot.setGiftName("name");
		ot.setGiftNotificationText("notification");
		ot.setKikbakNotificationText("not");
		ot.setKikbakName("kik");
		ot.setName("name");

		
//		try {
//			merchantService.addOrUpdateMerchant(mt);
//			merchantService.addOrUpdateOffer(ot);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
		
//		UserLocationType ult = new UserLocationType();
//		ult.setLatitude(37.4207480);
//		ult.setLongitude(-122.1303430);
//		Collection<ClientOfferType> ots = service.getOffers(4L, ult);
//		assertTrue(ots.size() == 1);
	}
	
	
	@Test
	public void testNoValidOffers(){
		MerchantType mt = new MerchantType();
		mt.setDescription("test");
		mt.setImageUrl("url");
		mt.setName("test");
		mt.setUrl("url");
		mt.getLocations().add(fillLocationType());
		mt.getLocations().add(fillLocationType());
		
		OfferType ot = new OfferType();
		ot.setBeginDate(new Date().getTime() + 100000);
		ot.setEndDate(new Date().getTime() - 100000);
		ot.setGiftName("name");
		ot.setGiftNotificationText("notification");
		ot.setKikbakNotificationText("not");
		ot.setKikbakName("kik");
		ot.setName("name");
		
//		try {
//			merchantService.addOrUpdateMerchant(mt);
//			merchantService.addOrUpdateOffer(ot);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
//		
//		
//		
//		UserLocationType ult = new UserLocationType();
//		ult.setLatitude(37.4207480);
//		ult.setLongitude(-12.1303430);
//		Collection<ClientOfferType> ots = service.getOffers(4L, ult);
//		assertTrue(ots.size() == 0);
	}
	
	protected Collection<FriendType> generateFriends(int count){
		Collection<FriendType> fts = new ArrayList<FriendType>();
		
		for( int i = 0; i < count; i++){
			FriendType ft = new FriendType();
			ft.setId(888 + i);
			ft.setFirstName("f1");
			ft.setLastName("l2");
			ft.setName("name");
			ft.setUsername("username");
			fts.add(ft);
		}
			
		return fts;
	}
	
	protected UserType generateUserType(){
		UserType ut = new UserType();
		ut.setEmail("foo@foo.com");
		ut.setFirstName("first");
		ut.setGender("male");
		ut.setLastName("last");
		ut.setName("first last");
		ut.setId(12345L);
		ut.setVerified(true);
		
		return ut;
	}
	
	protected LocationType fillLocationType(){
		LocationType lt = new LocationType();
		lt.setAddress1("add");
		lt.setAddress2("ass");
		lt.setCity("test");
		lt.setState("test");
		lt.setVerificationCode("test");
		lt.setZipCode(12345);
		lt.setLatitude(12.12);
		lt.setLongitude(12.31);

		return lt;
	}
}
