//
//  OfferParser.m
//  Kikbak
//
//  Created by Ian Barile on 4/6/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import "OfferParser.h"
#import "AppDelegate.h"
#import "Offer.h"
#import "LocationParser.h"
#import "OfferService.h"
#import "ImageDownloadRequest.h"
#import "ImagePersistor.h"

@interface OfferParser()

@property (nonatomic, strong)NSMutableDictionary* offers;

-(void)fillInGift:(Offer*) offer withDict:(NSDictionary*)dict;
-(void)fillInKikbak:(Offer*) offer withDict:(NSDictionary*)dict;

@end

@implementation OfferParser

-(void)parse:(NSDictionary*)dict{
    NSManagedObjectContext* context = ((AppDelegate*)[UIApplication sharedApplication].delegate).managedObjectContext;
    Offer* offer = [OfferService findOfferById:[dict objectForKey:@"id"]];
    if( offer == nil){
        offer = [NSEntityDescription insertNewObjectForEntityForName:@"Offer" inManagedObjectContext:context];
        offer.offerId = [dict objectForKey:@"id"];
    }
    offer.name = [dict objectForKey:@"name"];
    offer.desc = [dict objectForKey:@"description"];
    offer.defaultText = [dict objectForKey:@"defaultText"];
    offer.offerType = [dict objectForKey:@"offerType"];
    if( [offer.offerType compare:@"both"] == NSOrderedSame){
        [self fillInGift:offer withDict:dict];
        [self fillInKikbak:offer withDict:dict];
    }
    else if( [offer.offerType compare:@"give_only"] == NSOrderedSame){
        [self fillInGift:offer withDict:dict];
    }
    else if( [offer.offerType compare:@"get_only"] == NSOrderedSame){
        [self fillInKikbak:offer withDict:dict];
    }
    

    offer.merchantId = [dict objectForKey:@"merchantId"];
    offer.merchantName = [dict objectForKey:@"merchantName"];
    offer.merchantUrl = [dict objectForKey:@"merchantUrl"];
    offer.termsOfService = [dict objectForKey:@"tosUrl"];
    offer.offerImageUrl = [dict objectForKey:@"offerImageUrl"];
    offer.giveImageUrl = [dict objectForKey:@"giveImageUrl"];
    
    if( [dict objectForKey:@"hasEmployeeProgram"] != [NSNull null]){
        offer.hasEmployeeProgram = [dict objectForKey:@"hasEmployeeProgram"];
    }
    
    long date = [dict objectForKey:@"beginDate"];
    NSTimeInterval timeSince70 = date;
    offer.beginDate = [NSDate dateWithTimeIntervalSince1970:timeSince70];
    date = [dict objectForKey:@"endDate"];
    timeSince70 = date;
    offer.endDate = [NSDate dateWithTimeIntervalSince1970:timeSince70];
    
    NSArray* locations = [dict objectForKey:@"locations"];
    for(id offerLocation in locations){
        Location* loc = [[[LocationParser alloc]init] parse:offerLocation withContext:offer.managedObjectContext];
        [offer addLocationObject:loc];
    }
    
    NSError *error = nil;
    if (![context save:&error]) {
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }

    //offer image
    if(![ImagePersistor imageFileExists:offer.merchantId imageType:OFFER_LIST_IMAGE_TYPE]) {
        ImageDownloadRequest* request = [[ImageDownloadRequest alloc]init];
        request.url = offer.offerImageUrl;
        request.fileId = offer.merchantId;
        request.type = OFFER_LIST_IMAGE_TYPE;
        [request requestImage];
    }
    
    //default give
    if(![ImagePersistor imageFileExists:offer.merchantId imageType:DEFAULT_MERCHANT_IMAGE_TYPE]) {
        ImageDownloadRequest* request = [[ImageDownloadRequest alloc]init];
        request.url = offer.giveImageUrl;
        request.fileId = offer.merchantId;
        request.type = DEFAULT_MERCHANT_IMAGE_TYPE;
        [request requestImage];
    }
    
    if(self.offers == nil){
        self.offers = [[NSMutableDictionary alloc]initWithCapacity:1];
    }
    [self.offers setObject:offer forKey:offer.offerId];
}

-(void)resolveDiff{
    NSArray* pOffers = [OfferService getOffers];
    NSMutableDictionary* pOfferDict = [[NSMutableDictionary alloc] initWithCapacity:[pOffers count]];
    for(Offer* offer in pOffers){
        [pOfferDict setObject:offer forKey:offer.offerId];
    }
    
    for(NSNumber* key in [self.offers allKeys]){
        [pOfferDict removeObjectForKey:key];
    }
    
    
    NSManagedObjectContext* context = ((AppDelegate*)[UIApplication sharedApplication].delegate).managedObjectContext;
    for(Offer* offer in [pOfferDict allValues]){
        [context deleteObject:offer];
    }
    
    NSError* error = nil;
    if([[context deletedObjects]count] > 0 && [[context deletedObjects]count] > 0 && ![context save:&error]){
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }
}

-(void)fillInGift:(Offer*) offer withDict:(NSDictionary*)dict{
    offer.giftDescription = [dict objectForKey:@"giftDesc"];
    offer.giftDescriptionOptional = [dict objectForKey:@"giftDetailedDesc"];
    offer.giftValue = [dict objectForKey:@"giftValue"];
    offer.giftDiscountType = [dict objectForKey:@"giftDiscountType"];
}

-(void)fillInKikbak:(Offer*) offer withDict:(NSDictionary*)dict{
    offer.kikbakDescription = [dict objectForKey:@"kikbakDesc"];
    offer.kikbakDescriptionOptional = [dict objectForKey:@"kikbakDetailedDesc"];
    offer.kikbakValue = [dict objectForKey:@"kikbakValue"];
}

@end
