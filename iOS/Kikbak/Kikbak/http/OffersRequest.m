//
//  OffersRequest.m
//  kikback
//
//  Created by Ian Barile on 12/11/12.
//  Copyright (c) 2012 Ian Barile. All rights reserved.
//

#import "OffersRequest.h"
#import "KikbakConstants.h"
#import "SBJson.h"
#import "OfferParser.h"
#import "AppDelegate.h"

static NSString* resource = @"user/offer";

@interface OffersRequest()
@property(strong,nonatomic)NSMutableData* data;

-(NSDictionary*)formatRequest:(id)requestData;
@end

@implementation OffersRequest


-(void)makeRequest:(NSDictionary*)requestData{
  
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];

    NSString* userId = [prefs objectForKey:KIKBAK_USER_ID];
    assert(userId != nil);
    if( userId == nil){
        return;
    }
    request = [[HttpRequest alloc]init];
    request.resource = [NSString stringWithFormat:@"%@/%@/", resource, userId ];


    NSString* body = [[self formatRequest:requestData] JSONRepresentation];
    request.body = body;
    request.restDelegate = self;
    [request restPostRequest];
  
}

-(NSDictionary*)formatRequest:(id)requestData{
  NSMutableDictionary* result = [[NSMutableDictionary alloc]initWithCapacity:1];
  NSMutableDictionary* user =[[NSMutableDictionary alloc]initWithCapacity:1];
  
  [user setObject:requestData forKey:@"userLocation"];
  [result setObject:user forKey:@"GetUserOffersRequest"];
  return result;
}


-(void)parseResponse:(NSData*)data{

    NSString* json = [[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
  //  NSLog(@"**** offersRequest: %@", json);
    id dict = [json JSONValue];
    if( dict != [NSNull null] ){
        NSDictionary* getUserOffersResponse = [dict objectForKey:@"getUserOffersResponse"];
        if( getUserOffersResponse){
            NSArray* offers = [getUserOffersResponse objectForKey:@"offers"];
            for(id offer in offers){
                [OfferParser parse:offer];
            }
        }
    }
    
    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakOfferUpdate object:nil];
}

@end
