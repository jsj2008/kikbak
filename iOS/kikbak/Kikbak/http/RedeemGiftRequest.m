//
//  RedeemGiftRequest.m
//  kikback
//
//  Created by Ian Barile on 12/12/12.
//  Copyright (c) 2012 Ian Barile. All rights reserved.
//

#import "RedeemGiftRequest.h"
#import "KikbakConstants.h"
#import "SBJson.h"
#import "Gift.h"
#import "NotificationContstants.h"

static NSString* resource = @"rewards/redeem/gift";


@implementation RedeemGiftRequest

-(void)restRequest:(NSDictionary*)requestData{
  
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
  
  [user setObject:requestData forKey:@"gift"];
  [result setObject:user forKey:@"RedeemGiftRequest"];
  return result;
}


-(void)parseResponse:(NSData*)data{
    NSString* json = [[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"RedeemGiftRequest: %@", json);
    
    self.gift.location = nil;
    NSManagedObjectContext* context = self.gift.managedObjectContext;
    [context deleteObject:self.gift];
    
    NSError* error;
    [context save:&error];
    
    if(error){
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }

    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakRedeemGiftSuccess object:nil];
}

-(void)handleError:(NSInteger)statusCode withData:(NSData*)data{
    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakRedeemGiftError object:nil];
}

@end