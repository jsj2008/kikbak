//
//  RedeemKikbakRequest.m
//  kikback
//
//  Created by Ian Barile on 12/12/12.
//  Copyright (c) 2012 Ian Barile. All rights reserved.
//

#import "RedeemCreditRequest.h"
#import "KikbakConstants.h"
#import "SBJson.h"
#import "NotificationContstants.h"
#import "Credit.h"
#import "QRCodeImageRequest.h"

static NSString* resource = @"rewards/redeem/credit";

@interface RedeemCreditRequest()
-(NSDictionary*)formatRequest:(id)requestData;
@end

@implementation RedeemCreditRequest

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
  
  [user setObject:requestData forKey:@"credit"];
  [result setObject:user forKey:@"RedeemCreditRequest"];
  return result;
}


-(void)parseResponse:(NSData*)data{
    NSString* json = [[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
//    NSLog(@"RedeemCreditRequest: %@", json);

    NSManagedObjectContext* context = self.credit.managedObjectContext;

    NSString* authorizationCode;
    NSNumber* creditId = self.credit.creditId;
    id dict = [json JSONValue];
    if( dict != [NSNull null] ) {
        id kikbakResponse = [dict objectForKey:@"redeemCreditResponse"];
        if(kikbakResponse != [NSNull null]){
            id response = [kikbakResponse objectForKey:@"response"];
            if( response != [NSNull null]){
                authorizationCode = [response objectForKey:@"authorizationCode"];
                NSNumber* balance = [response objectForKey:@"balance"];
                if( [balance integerValue ] == 0){
                    self.credit.location = nil;
                    [context deleteObject:self.credit];
                }
                else{
                    self.credit.value = balance;
                }
                
                NSError* error;
                [context save:&error];
                
                if(error){
                    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
                }
                
                
                QRCodeImageRequest* qrRequest = [[QRCodeImageRequest alloc]init];
                qrRequest.type = @"credit";
                qrRequest.code = authorizationCode;
                qrRequest.fileId = creditId;
                [qrRequest requestQRCode];
            }
            else{
               [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakRedeemCreditError object:NSLocalizedString(@"invalid qrcode", nil)];
            }
        }
    }
    
}

-(void)handleError:(NSInteger)statusCode withData:(NSData*)data{
    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakRedeemCreditError object:NSLocalizedString(@"Unreachable", nil)];
}

@end
