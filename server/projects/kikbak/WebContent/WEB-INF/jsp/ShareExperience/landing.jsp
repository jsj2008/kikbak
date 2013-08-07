<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head prefix="og: http://ogp.me/ns# product: http://ogp.me/ns/product#">
<meta property="fb:app_id" content="493383324061333">
<meta property="og:url" content="${url}">
<meta property="og:type" content="referredlabs:coupon">
<meta property="og:title" content="${title}">
<meta property="og:image" content="${gift.imageUrl}">
<meta property="og:description" content="${decs}">
<meta property="og:message" content="${gift.caption}">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;"/>
<link href="../css/landing.css" rel="stylesheet" type="text/css" />
<title>Kikbak</title>
</head>

<body>
<div class="bdy">

<div class="hedr-wpr"><a href="#" class="logo"><img src="${gift.imageUrl}" /></a></div>

<div class="main-bdy">
<p class="top-msg"><strong>${gift.friendName}</strong> used <strong>Kikbak</strong> to give you
an exclusive offer for <strong>${gift.merchant.name}</strong>.</p>
<div class="bdy-wpr">
<div class="lft-colm">
<div class="usr-cmnt">
<a href="#" class="usr-img"><img src="https://graph.facebook.com/${gift.fbFriendId}/picture?type=square" /></a>
<h2>${gift.friendName}</h2>
<p>${gift.caption}</p>
</div>
<div class="img-bnr">
<span class="blkshd"></span>
<div class="cmnt-on-photo wit-grd photup"><p>${gift.friendName} was helped by <strong>${employeeId}</strong> at ${gift.merchant.name} Store:
<strong>${location.address1} ${location.address2}, ${location.city}, ${location.state}</strong>
If you visit the same store, ${employeeId} can help you too.</p></div>
<img src="../img/mobile-bnr.png"  class="mob-bnr"/>
<h3>Verizon Wireless</h3>
<div class="optn"><a href="#"><img src="../img/map-mrk.png"/></a>

<a href="#"><img src="../img/glob.png"/></a>

<a href="#"><img src="../img/phone.png"/></a>

</div>
</div>

<div class="usr-cmnt blk">
<a href="#" class="usr-img"><img src="https://graph.facebook.com/${gift.fbFriendId}/picture?type=square" /></a>
<h2>${gift.friendName}</h2>
<p>${gift.caption}</p>
</div>
<div class="cmnt-on-photo"><img src="../img/info-icon.png" /><p>${gift.friendName} was helped by <strong>${employeeId}</strong> at ${gift.merchant.name} Store:
<strong>${location.address1} ${location.address2}, ${location.city}, ${location.state}</strong>
If you visit the same store, ${employeeId} can help you too.</p></div>
</div>

<div class="rit-colm">
<p class="tp-msg"><strong>${gift.friendName}</strong> used <strong>Kikbak</strong> to give you
an exclusive offer for <strong>${gift.merchant.name}</strong>.</p>
<div class="tp-bnr">
<div class="tp-bnr-lft"><h1>${gift.desc}</h1><p>${gift.detailedDesc}</p>
<div class="tp-bnr-rit"></div></div>



</div>
<div class="blue-btn">
<a href="#" class="btnn">Generate offer to use in store</a>

<p>The Kikbak app makes it easy to access your gift. 
Download it now -- your gift is already there.</p>
<p class="ap-srt">
<a href="#" class="lft"><img src="../img/app-store.png" /></a> 
<a href="#" class="rit"><img src="../img/google-play.png" /></a>
</p>
</div>
</div>

</div>

</div>

</div>

<div class="footer">
<div class="fot-wpr"><a href="${gift.tosUrl}" class="lft">Terms & Conditions</a>
<a href="#" class="rit">@ Referred Labs, 2013</a>
</div>
</div>

</body>
</html>
