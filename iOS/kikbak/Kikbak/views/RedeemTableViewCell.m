//
//  ReedemTableViewCell.m
//  Kikbak
//
//  Created by Ian Barile on 4/12/13.
//  Copyright (c) 2013 Ian Barile. All rights reserved.
//

#import "RedeemTableViewCell.h"
#import "Gift.h"
#import "Kikbak.h"
#import "Distance.h"
#import "Location.h"
#import "ImagePersistor.h"
#import "RewardCollection.h"
#import "util.h"

const int CELL_HEIGHT = 147;

@interface RedeemTableViewCell()

@property (nonatomic,strong) Location* location;

@property (nonatomic,strong) UIImageView* retailerImage;
@property (nonatomic,strong) UIImageView* imageGradient;
@property (nonatomic,strong) UIView* rewardBackground;
@property (nonatomic,strong) UIImageView* horizontalSeparator;
@property (nonatomic,strong) UIImageView* verticalSeparator;
@property (nonatomic,strong) UIImageView* drpShadow;
@property (nonatomic,strong) UILabel* retailerName;
@property (nonatomic,strong) UIButton* mapBtn;
@property (nonatomic,strong) UIImageView* mapIcon;
@property (nonatomic,strong) UILabel* distance;
@property (nonatomic,strong) UIButton* webBtn;
@property (nonatomic,strong) UIButton* callBtn;
@property (nonatomic,strong) UILabel* gift;
@property (nonatomic,strong) UILabel* credit;


-(void)manuallyLayoutSubview;
-(void)setupGift;
-(void)setupKikbak;

-(IBAction)onMap:(id)sender;
-(IBAction)onWeb:(id)sender;
-(IBAction)onCall:(id)sender;

@end

@implementation RedeemTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        self.frame = CGRectMake(0, 0, 320, CELL_HEIGHT);
        self.backgroundColor = [UIColor clearColor];
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        [self manuallyLayoutSubview];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state
}

-(void)manuallyLayoutSubview{
    self.retailerImage = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"img1"]];
    self.retailerImage.frame = CGRectMake(0, 0, 320, 90);
    [self addSubview:self.retailerImage];
    
    self.imageGradient = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"grd_img_redeem_list"]];
    self.imageGradient.frame = CGRectMake(0,15,320,75);
    [self addSubview:self.imageGradient];
    
    self.rewardBackground = [[UIView alloc]initWithFrame:CGRectMake(0, 90, 320, 47)];
    self.rewardBackground.backgroundColor = [UIColor whiteColor];
    [self addSubview:self.rewardBackground];
    
    self.drpShadow = [[UIImageView alloc]initWithFrame:CGRectMake(0, 137, 320, 11)];
    self.drpShadow.image = [UIImage imageNamed:@"grd_redeem_list_dropshadow"];
    [self addSubview:self.drpShadow];
    
    
    self.horizontalSeparator = [[UIImageView alloc]initWithFrame:CGRectMake(0, 88, 320, 2)];
    self.horizontalSeparator.image = [UIImage imageNamed:@"separator_dots"];
    [self addSubview:self.horizontalSeparator];
    
    self.verticalSeparator = [[UIImageView alloc]initWithFrame:CGRectMake(159, 90, 2, 47)];
    self.verticalSeparator.image = [UIImage imageNamed:@"separator_vertical_redeem_list"];
    [self addSubview:self.verticalSeparator];
    
    self.retailerName = [[UILabel alloc]initWithFrame:CGRectMake(11, 61, 180, 22)];
    self.retailerName.backgroundColor = [UIColor clearColor];
    self.retailerName.textColor = [UIColor whiteColor];
    self.retailerName.font = [UIFont fontWithName:@"HelveticaNeue" size:21];
    self.retailerName.text = @"Verizon Wireless";
    [self addSubview:self.retailerName];
    
    UIImage* mapIcon = [UIImage imageNamed:@"ic_map"];
    self.mapIcon = [[UIImageView alloc]initWithImage:mapIcon];
    self.mapIcon.frame = CGRectMake(202, 67, mapIcon.size.width, mapIcon.size.height);
    [self addSubview:self.mapIcon];
    
    self.distance = [[UILabel alloc]initWithFrame:CGRectMake(214, 65, 80, 16)];
    self.distance.font = [UIFont fontWithName:@"HelveticaNeue" size:15];
    self.distance.text = @"1.7 mi";
    self.distance.textColor = [UIColor whiteColor];
    self.distance.textAlignment = NSTextAlignmentLeft;
    self.distance.backgroundColor = [UIColor clearColor];
    [self addSubview:self.distance];
    
    
    self.mapBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.mapBtn.frame = CGRectMake(202, 64, 60, 30);
    [self.mapBtn addTarget:self action:@selector(onMap:) forControlEvents:UIControlEventTouchUpInside];
    // self.mapBtn.backgroundColor = [UIColor greenColor];
    [self addSubview:self.mapBtn];
    
    self.webBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.webBtn.frame = CGRectMake(264, 57, 26, 30);
    self.webBtn.contentMode = UIViewContentModeCenter;
    //self.webBtn.backgroundColor = [UIColor redColor];
    [self.webBtn setImage:[UIImage imageNamed:@"ic_web"] forState:UIControlStateNormal];
    [self.webBtn addTarget:self action:@selector(onWeb:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.webBtn];
    
    self.callBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.callBtn.frame = CGRectMake(290, 56, 26, 30);
    self.callBtn.contentMode = UIViewContentModeLeft;
    //self.callBtn.backgroundColor = [UIColor greenColor];
    [self.callBtn setImage:[UIImage imageNamed:@"ic_phone"] forState:UIControlStateNormal];
    [self.callBtn addTarget:self action:@selector(onCall:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.callBtn];
    
    self.gift = [[UILabel alloc]initWithFrame:CGRectMake(11, 9, 150, 13)];
    self.gift.text = NSLocalizedString(@"Received Gift", nil);
    self.gift.backgroundColor = [UIColor clearColor];
    self.gift.textColor = UIColorFromRGB(0X3A3A3A);
    self.gift.font = [UIFont fontWithName:@"HelveticaNeue" size:11];
    self.gift.textAlignment = NSTextAlignmentLeft;
    
    self.giftValue = [[UILabel alloc]initWithFrame:CGRectMake(11, 21, 150, 18)];
    self.giftValue.text = [NSString stringWithFormat:NSLocalizedString(@"gift percent", nil),
                                    [NSNumber numberWithInt:10]];
    self.giftValue.backgroundColor = [UIColor clearColor];
    self.giftValue.textColor = UIColorFromRGB(0X767676);
    self.giftValue.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:16];
    self.giftValue.textAlignment = NSTextAlignmentLeft;

    
    self.credit = [[UILabel alloc]initWithFrame:CGRectMake(160, 9, 149, 13)];
    self.credit.text = NSLocalizedString(@"Earned Credit", nil);
    self.credit.backgroundColor = [UIColor clearColor];
    self.credit.textColor = UIColorFromRGB(0X767676);
    self.credit.font = [UIFont fontWithName:@"HelveticaNeue" size:11];
    self.credit.textAlignment = NSTextAlignmentRight;

    
    self.creditValue = [[UILabel alloc]initWithFrame:CGRectMake(160, 21, 149, 18)];
    self.creditValue.text = [NSString stringWithFormat:NSLocalizedString(@"gift percent", nil),
                           [NSNumber numberWithInt:10]];
    self.creditValue.backgroundColor = [UIColor clearColor];
    self.creditValue.textColor = UIColorFromRGB(0X3A3A3A);
    self.creditValue.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:16];
    self.creditValue.textAlignment = NSTextAlignmentRight;

}


-(void)setup//:(int)index
{
    [self.credit removeFromSuperview];
    [self.creditValue removeFromSuperview];
    [self.gift removeFromSuperview];
    [self.giftValue removeFromSuperview];
    
    if( self.rewards.gift){
        [self.rewardBackground addSubview:self.gift];
        [self.rewardBackground addSubview:self.giftValue];
        [self setupGift];
    }
    
    if( self.rewards.credit){
        [self.rewardBackground addSubview:self.credit];
        [self.rewardBackground addSubview:self.creditValue];
        [self setupKikbak];
    }
    
    if (!self.rewards.gift || !self.rewards.credit) {
        [self.verticalSeparator removeFromSuperview];
    }
}

-(void)setupGift{
    self.retailerName.text = self.rewards.gift.merchantName;

    if ([self.rewards.gift.type compare:@"percentage"] == NSOrderedSame) {
        self.giftValue.text = [NSString stringWithFormat:NSLocalizedString(@"gift percent", nil),
                                    [self.rewards.gift.value integerValue]];
    }
    else{
        self.giftValue.text = [NSString stringWithFormat:NSLocalizedString(@"gift amount", nil),
                                    [self.rewards.gift.value integerValue]];
    }

    //todo: find closest location
    if (self.rewards.gift.location.count > 0) {
        self.location = [self.rewards.gift.location anyObject];
    }
    
    self.distance.text = [NSString stringWithFormat:NSLocalizedString(@"miles away", nil),
                                    [Distance distanceToInMiles:
                                    [[CLLocation alloc]initWithLatitude:
                                        self.location.latitude.doubleValue
                                        longitude:self.location.longitude.doubleValue]] ];
    
    NSString* imagePath = [ImagePersistor imageFileExists:self.rewards.gift.merchantId imageType:MERCHANT_IMAGE_TYPE];
    if(imagePath != nil){
        self.retailerImage.image = [[UIImage alloc]initWithContentsOfFile:imagePath];
    }
}

-(void)setupKikbak{
    self.retailerName.text = self.rewards.credit.merchantName;
    self.creditValue.text = [NSString stringWithFormat:NSLocalizedString(@"credit", nil),
                             self.rewards.credit.value];

    //todo: find closest location
    if (self.rewards.credit.location.count > 0) {
        self.location = [self.rewards.credit.location anyObject];
    }
    
    self.distance.text = [NSString stringWithFormat:NSLocalizedString(@"miles away", nil),
                          [Distance distanceToInMiles:
                           [[CLLocation alloc]initWithLatitude:
                            self.location.latitude.doubleValue
                             longitude:self.location.longitude.doubleValue]] ];
    
    NSString* imagePath = [ImagePersistor imageFileExists:self.rewards.credit.merchantId imageType:MERCHANT_IMAGE_TYPE];
    if(imagePath != nil){
        self.retailerImage.image = [[UIImage alloc]initWithContentsOfFile:imagePath];
    }
}

#pragma mark - button actions
-(IBAction)onMap:(id)sender{
    NSString *stringURL = [NSString stringWithFormat:@"http://maps.apple.com/maps?q=%@,%@",
                           self.location.latitude, self.location.longitude];
    NSURL *url = [NSURL URLWithString:stringURL];
    [[UIApplication sharedApplication] openURL:url];
}

-(IBAction)onCall:(id)sender{
    NSURL* url = [[NSURL alloc]initWithString:[NSString stringWithFormat:@"tel:%@",self.location.phoneNumber]];
    if(![[UIApplication sharedApplication] canOpenURL:url]){
        UIAlertView* alert = [[UIAlertView alloc]initWithTitle:@"Hmmm..." message:@"You need to be on an iPhone to make a call" delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil];
        [alert show];
    }
    else{
        [[UIApplication sharedApplication]openURL:url];
    }
}


-(IBAction)onWeb:(id)sender{
    if(self.rewards.gift){
        [[UIApplication sharedApplication]openURL:[[NSURL alloc]initWithString:self.rewards.gift.merchantUrl]];
    }
    else{
        [[UIApplication sharedApplication]openURL:[[NSURL alloc]initWithString:self.rewards.credit.merchantUrl]];
    }
}


@end
