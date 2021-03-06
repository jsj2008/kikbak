//
//  OffersRequest.h
//  kikback
//
//  Created by Ian Barile on 12/11/12.
//  Copyright (c) 2012 Ian Barile. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RestProtocol.h"
#import "ResponseHandlerProtocol.h"
#import "HttpRequest.h"


@interface OffersRequest : NSObject<ResponseHandlerProtocol, RestProtocol>{
  HttpRequest* request;
}

@end
