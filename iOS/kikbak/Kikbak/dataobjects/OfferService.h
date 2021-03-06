//
//  OfferLoader.h
//  Kikbak
//
//  Created by Ian Barile on 4/7/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Offer;

@interface OfferService : NSObject

+(Offer*)findOfferById:(NSNumber*)offerId;
+(NSArray*)getOffers;
+(void)deleteAll;

@end
