//
//  SubmitFriendsRequest.h
//  kikback
//
//  Created by Ian Barile on 12/10/12.
//  Copyright (c) 2012 Ian Barile. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RestDataDelegate.h"
#import "PostRequest.h"

@interface SubmitFriendsRequest : NSObject<RestDataDelegate>
{
  PostRequest* request;
}

-(void)makeRequest:(NSDictionary*)requestData;

@end
