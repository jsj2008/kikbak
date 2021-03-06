//
//  ImageRequest.m
//  Kikbak
//
//  Created by Ian Barile on 4/8/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import "ImageDownloadRequest.h"
#import "ImagePersistor.h"
#import "NotificationContstants.h"


@implementation ImageDownloadRequest

-(void)requestImage{

    request = [[HttpRequest alloc]init];
    request.resource = self.url;
    
    
    request.restDelegate = self;
    [request httpGetRequest];
    
}


-(void)parseResponse:(NSData*)data{
    [ImagePersistor persisttImage:data fileId:self.fileId imageType:self.type];
    [[NSNotificationCenter defaultCenter]postNotificationName:kKikbakImageDownloaded object:nil];
}

-(void)handleError:(NSInteger)statusCode withData:(NSData*)data{
    
}


@end
