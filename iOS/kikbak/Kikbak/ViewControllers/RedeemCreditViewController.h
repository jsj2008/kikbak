//
//  RedeemCreditViewController.h
//  Kikbak
//
//  Created by Ian Barile on 5/25/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <ZXingWidgetController.h>

@protocol UserCreditUpdate <NSObject>

-(void)onUpdateAmount:(NSNumber*)amount;

@end

@class Credit;

@interface RedeemCreditViewController : UIViewController<UserCreditUpdate, ZXingDelegate>

@property(nonatomic,strong)Credit* credit;

@end
