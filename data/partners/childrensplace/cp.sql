set @server="http://m.kikbak.me";

#Childrens Place

insert into merchant (name, description, url, image_url) values 
                     ('The Children\'s Place',  'pet groomer', 'http://www.childrensplace.com',concat(@server, '/images/cp_logo.png'));

set @id := (select LAST_INSERT_ID());

insert into location (address_1, city,state, zipcode, phone_number, verification_code, merchant_id, latitude,  longitude) values 
                     ('500 Plaza Dr', 'Secaucus', 'NJ',  '07094', "2015582400",'child',@id,40.7881347, -74.04526750000001);
                   
insert into offer (merchant_id, name,image_url,tos_url, begin_date, end_date) values 
                  (@id,'wash', concat(@server, '/images/cp_banner.png'), concat(@server, '/static/bug_tos.html'), now(),now() + interval 180 day);

set @offer_id := (select LAST_INSERT_ID());

insert into gift ( offer_id,  description,detailed_desc,value,discount_type,redemption_location_type,validation_type,image_url,default_give_image_url) values
                 (@offer_id,'20% off','any purchase over $50', 20,'percentage','store','barcode',concat(@server, '/images/cp_default_give.png'),  concat(@server, '/images/cp_banner.png'));

set @gift_id := (select LAST_INSERT_ID());                 

insert into kikbak (offer_id, description,detailed_desc,value,reward_type,validation_type,image_url) values
                   (@offer_id,'$5 credit','for every friend that redeems your gift', 5.00, 'purchase', 'qrcode', concat(@server, '/images/cp_banner.png'));
