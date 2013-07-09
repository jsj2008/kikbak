//
//  RedeemGiftSuccessViewController.h
//  Kikbak
//
//  Created by Ian Barile on 6/25/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Gift;

@interface RedeemGiftSuccessViewController : UIViewController


@property (nonatomic, strong) NSString* merchantName;
@property (nonatomic, strong) NSNumber* value;
@property (nonatomic, strong) NSString* giftType;
@property (nonatomic, strong) NSString* optionalDesc;
@property (nonatomic, strong) NSString* validationCode;


@end