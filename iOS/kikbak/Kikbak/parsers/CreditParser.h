//
//  KikbakParser.h
//  Kikbak
//
//  Created by Ian Barile on 4/16/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CreditParser : NSObject

-(void)parse:(NSDictionary*)dict;
-(void)resolveDiff;

@end
