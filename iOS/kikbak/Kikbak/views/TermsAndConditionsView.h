//
//  TermsAndConditionsView.h
//  kikit
//
//  Created by Ian Barile on 3/14/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TermsAndConditionsView : UIView<UIWebViewDelegate>

@property (nonatomic,strong) NSString* tosUrl;

-(void) manuallyLayoutSubviews;

@end
