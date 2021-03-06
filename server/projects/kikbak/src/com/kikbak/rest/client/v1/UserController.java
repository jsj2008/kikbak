package com.kikbak.rest.client.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kikbak.client.service.impl.CookieAuthenticationFilter;
import com.kikbak.client.service.v1.FbLoginException;
import com.kikbak.client.service.v1.FbUserLimitException;
import com.kikbak.client.service.v1.UserService;
import com.kikbak.client.service.v2.UserService2;
import com.kikbak.dao.enums.OfferType;
import com.kikbak.jaxb.v1.devicetoken.DeviceTokenUpdateRequest;
import com.kikbak.jaxb.v1.devicetoken.DeviceTokenUpdateResponse;
import com.kikbak.jaxb.v1.friends.UpdateFriendResponse;
import com.kikbak.jaxb.v1.friends.UpdateFriendsRequest;
import com.kikbak.jaxb.v1.offer.ClientOfferType;
import com.kikbak.jaxb.v1.offer.GetUserOffersRequest;
import com.kikbak.jaxb.v1.offer.GetUserOffersResponse;
import com.kikbak.jaxb.v1.offer.HasUserOffersResponse;
import com.kikbak.jaxb.v1.register.RegisterUserRequest;
import com.kikbak.jaxb.v1.register.RegisterUserResponse;
import com.kikbak.jaxb.v1.register.RegisterUserResponseStatus;
import com.kikbak.jaxb.v1.register.UserIdType;
import com.kikbak.jaxb.v1.statustype.SuccessStatus;
import com.kikbak.jaxb.v1.userlocation.UserLocationType;

@Controller
@RequestMapping("/user")
public class UserController extends AbstractController {

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserService2 userService2;

    @Autowired
    private PropertiesConfiguration config;

    static final String REFERRAL_CODE_KEY = "rc";

    private static final String USER_COOKIE_SECURE = "user.cookie.secure";

    private static final int COOKIE_EXPIRE_TIME = 10 * 365 * 24 * 60 * 60;

    private static final Logger logger = Logger.getLogger(UserController.class);

    public static final Pattern EMAIL_ADDRESS = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
            + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

    @RequestMapping(value = "/register/fb/", method = RequestMethod.POST)
    public RegisterUserResponse registerFacebookUser(@RequestBody RegisterUserRequest request,
            final HttpServletResponse httpResponse) {
        String token = request.getUser().getAccessToken();
        return registerFacebookUser(token, null, httpResponse);
    }

    @RequestMapping(value = "/register/fb", method = RequestMethod.GET)
    public RegisterUserResponse registerFacebookUser(@RequestParam(value = "token") String token,
            @RequestParam(value = "phone", required = false) String phone, final HttpServletResponse httpResponse) {
        try {
            long id = userService.registerFbUser(token, phone);
            addUserCookies(id, httpResponse);

            RegisterUserResponse response = new RegisterUserResponse();
            response.setStatus(RegisterUserResponseStatus.OK);
            UserIdType uid = new UserIdType();
            uid.setUserId(id);
            response.setUserId(uid);
            return response;
        } catch (FbUserLimitException e) {
            logger.error(e);
            RegisterUserResponse response = new RegisterUserResponse();
            response.setStatus(RegisterUserResponseStatus.TOO_FEW_FRIENDS);
            return response;
        } catch (FbLoginException e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); // assume wrong token
            return null;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    private void addUserCookies(long id, HttpServletResponse httpResponse) {
        // user id
        Cookie cookie = new Cookie(CookieAuthenticationFilter.COOKIE_USER_ID_KEY, Long.toString(id));
        if (config.getBoolean(USER_COOKIE_SECURE)) {
            cookie.setSecure(true);
        }
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_TIME);
        httpResponse.addCookie(cookie);

        // token
        String token = userService.getUserToken(id);
        cookie = new Cookie(CookieAuthenticationFilter.COOKIE_TOKEN_KEY, token);
        if (config.getBoolean(USER_COOKIE_SECURE)) {
            cookie.setSecure(true);
        }
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_TIME);
        httpResponse.addCookie(cookie);
    }

    @RequestMapping(value = "/register/web", method = RequestMethod.GET)
    public RegisterUserResponse registerWebUserGet(@RequestParam("name") String name,
            @RequestParam("email") String email, @RequestParam(value = "phone", required = false) String phone,
            final HttpServletResponse httpResponse) {
        try {
            name = validateWebUserName(name);
            email = validateWebUserEmail(email);
            phone = validateWebUserPhone(phone);

            RegisterUserResponse response = new RegisterUserResponse();
            response.setStatus(RegisterUserResponseStatus.OK);
            long id = userService.registerWebUser(name, email, phone);
            UserIdType uid = new UserIdType();
            uid.setUserId(id);
            response.setUserId(uid);
            addUserCookies(id, httpResponse);
            return response;
        } catch (IllegalArgumentException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error(e, e);
            return null;
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error(e, e);
            return null;
        }
    }

    private String validateWebUserName(String name) {
        if (StringUtils.isEmpty(name))
            throw new IllegalArgumentException("Name can not be empty");
        return name;
    }

    private String validateWebUserEmail(String email) {
        if (StringUtils.isEmpty(email))
            throw new IllegalArgumentException("Email can not be empty");

        if (!EMAIL_ADDRESS.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email address:" + email);
        return email;
    }

    private String validateWebUserPhone(String phone) {
        if (StringUtils.isEmpty(phone))
            throw new IllegalArgumentException("Phone can not be empty");

        // get rid of all non-numeric characters
        String cleanedPhone = phone.replaceAll("[^\\d]", "");
        if (StringUtils.isEmpty(cleanedPhone) || cleanedPhone.length() < 10)
            throw new IllegalArgumentException("Invalid phone number:" + phone);

        return cleanedPhone;
    }

    @RequestMapping(value = "/friends/fb/{userId}", method = RequestMethod.POST)
    public UpdateFriendResponse updateFriends(@PathVariable("userId") Long userId,
            @RequestBody UpdateFriendsRequest request, final HttpServletResponse httpResponse) {
        try {
            ensureCorrectUser(userId);

            String accessToken = request.getAccessToken();
            userService.updateFriendsList(userId, accessToken);

            UpdateFriendResponse response = new UpdateFriendResponse();
            response.setStatus(SuccessStatus.OK);
            return response;
        } catch (WrongUserException e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        } catch (FbLoginException e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); // assume wrong token
            return null;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @RequestMapping(value = "/offer/{userId}/{merchantName}", method = RequestMethod.GET)
    public GetUserOffersResponse offersRequest(@PathVariable("userId") Long userId,
            @PathVariable("merchantName") String merchantName, final HttpServletResponse httpResponse) {
        try {
            if (StringUtils.isBlank(merchantName)) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
            GetUserOffersResponse response = new GetUserOffersResponse();
            Collection<com.kikbak.jaxb.v2.offer.ClientOfferType> offers = userService2.getOffers2(userId, merchantName);
            Collection<ClientOfferType> offersV1 = getAsOffersV1(offers);
            response.getOffers().addAll(offersV1);
            return response;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @RequestMapping(value = "/offer/{userId}", method = RequestMethod.POST)
    public GetUserOffersResponse offersRequest(@PathVariable("userId") Long userId,
            @RequestBody GetUserOffersRequest request, final HttpServletResponse httpResponse) {
        try {
            GetUserOffersResponse response = new GetUserOffersResponse();
            Collection<com.kikbak.jaxb.v2.offer.ClientOfferType> offers = userService2.getOffers2(userId,
                    request.getUserLocation());
            Collection<ClientOfferType> offersV1 = getAsOffersV1(offers);
            response.getOffers().addAll(offersV1);
            return response;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @RequestMapping(value = "/hasoffer/{longitude}/{latitude}", method = RequestMethod.OPTIONS)
    public HasUserOffersResponse hasOffersRequestOptions(@PathVariable("longitude") int longitude,
            @PathVariable("latitude") int latitude, final HttpServletResponse httpResponse) {
        httpResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpResponse.addHeader("Access-Control-Allow-Methods", "GET");
        httpResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");
        return null;
    }

    @RequestMapping(value = "/hasoffer/{longitude}/{latitude}", method = RequestMethod.GET)
    public HasUserOffersResponse hasOffersRequest(@PathVariable("longitude") int longitude,
            @PathVariable("latitude") int latitude, final HttpServletResponse httpResponse) {
        try {
            httpResponse.addHeader("Access-Control-Allow-Origin", "*");
            httpResponse.addHeader("Access-Control-Allow-Methods", "GET");
            httpResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");

            HasUserOffersResponse response = new HasUserOffersResponse();
            UserLocationType location = new UserLocationType();
            location.setLatitude(latitude / 10000000.0);
            location.setLongitude(longitude / 10000000.0);
            Collection<com.kikbak.jaxb.v2.offer.ClientOfferType> offers = userService2.hasOffers2(location);

            if (offers.isEmpty()) {
                response.setHasOffer(false);
                return response;
            }
            response.setHasOffer(true);
            if (offers.size() == 1) {
                response.setBrandName(offers.iterator().next().getMerchantName());
            }
            return response;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @RequestMapping(value = "/devicetoken/{userId}", method = RequestMethod.POST)
    public DeviceTokenUpdateResponse deviceTokenUpdate(@PathVariable("userId") Long userId,
            @RequestBody DeviceTokenUpdateRequest request, final HttpServletResponse httpResponse) {
        try {
            ensureCorrectUser(userId);
            userService.persistDeviceToken(userId, request.getToken());

            DeviceTokenUpdateResponse response = new DeviceTokenUpdateResponse();
            response.setStatus(SuccessStatus.OK);
            return response;
        } catch (WrongUserException e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        } catch (Exception e) {
            logger.error(e, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    private Collection<ClientOfferType> getAsOffersV1(Collection<com.kikbak.jaxb.v2.offer.ClientOfferType> offers) {
        Collection<com.kikbak.jaxb.v1.offer.ClientOfferType> result = new ArrayList<ClientOfferType>();
        for (com.kikbak.jaxb.v2.offer.ClientOfferType o : offers) {
            if (!o.getOfferType().equals(OfferType.both.name()))
                continue;
            result.add(toV1(o));
        }
        return result;
    }

    private ClientOfferType toV1(com.kikbak.jaxb.v2.offer.ClientOfferType o) {
        ClientOfferType r = new ClientOfferType();
        r.setBeginDate(o.getBeginDate());
        r.setEndDate(o.getEndDate());
        r.setGiftDesc(o.getGiftDesc());
        r.setGiftDetailedDesc(o.getGiftDetailedDesc());
        r.setGiftDiscountType(o.getGiftDiscountType());
        r.setGiftValue(o.getGiftValue());
        r.setGiveImageUrl(o.getGiveImageUrl());
        r.setId(o.getId());
        r.setKikbakDesc(o.getKikbakDesc());
        r.setKikbakDetailedDesc(o.getKikbakDetailedDesc());
        r.setKikbakValue(o.getKikbakValue());
        r.setMerchantId(o.getMerchantId());
        r.setMerchantName(o.getMerchantName());
        r.setMerchantUrl(o.getMerchantUrl());
        r.setName(o.getName());
        r.setOfferImageUrl(o.getOfferImageUrl());
        r.setTosUrl(o.getTosUrl());
        r.getLocations().addAll(o.getLocations());
        return r;
    }
}
