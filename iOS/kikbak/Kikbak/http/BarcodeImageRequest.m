//
//  BarcodeImageRequest.m
//  Kikbak
//
//  Created by Ian Barile on 7/17/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import "BarcodeImageRequest.h"
#import "ImagePersistor.h"
#import "NotificationContstants.h"
#import "HTTPConstants.h"

static NSString* resource = @"rewards/generateBarcode";


@implementation BarcodeImageRequest

-(void)requestBarcode{
    
    NSUserDefaults* prefs = [NSUserDefaults standardUserDefaults];
    NSString* userId = [prefs objectForKey:KIKBAK_USER_ID];
    assert(userId != nil);
    if( userId == nil){
        return;
    }
    request = [[HttpRequest alloc]init];
    request.resource = [NSString stringWithFormat:@"http://%s/%s/%@/%@/%@/%d/%d/", service_host, kikbak_service, resource, userId, self.code, 75,200 ];
    
    
    request.restDelegate = self;
    [request httpGetRequest];
    
}



-(void)parseResponse:(NSData*)data{
    NSString* imagePath = [ImagePersistor persisttImage:data fileId:self.allocatedGiftId imageType:BARCODE_IMAGE_TYPE];
    NSMutableDictionary* dict = [[NSMutableDictionary alloc]initWithCapacity:2];
    [dict setObject:[request.responseHeaders objectForKey:@"barcode"] forKey:@"barcode"];
    [dict setObject:imagePath forKey:@"imagePath"];
    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakBarcodeSuccess object:dict];
}

-(void)handleError:(NSInteger)statusCode withData:(NSData*)data{

    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakBarcodeError object:nil];
}

@end
