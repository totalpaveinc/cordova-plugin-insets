/*
   Copyright 2024 Total Pave Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

#import <Foundation/Foundation.h>
#import "TPIInset.h"
#import "TPIInsetObserverController.h"
#import <UIKit/UIKit.h>

@implementation TPIInset {
    NSMutableDictionary* $listeners;
    TPIInsetObserverController* $observer;
}

- (void) pluginInitialize {
    __weak TPIInset* weakSelf = self;
    
    self->$listeners = [[NSMutableDictionary alloc] init];
    self->$observer = [[TPIInsetObserverController alloc] initWithSAICallback:^(UIEdgeInsets insets) {
        TPIInset* strongSelf = weakSelf;
        
        if (strongSelf == nil) {
            return;
        }
        
        @synchronized (strongSelf->$listeners) {
            for (id key in strongSelf->$listeners) {
                NSDictionary* value = strongSelf->$listeners[key];
                [strongSelf emitInsetChange: value inset: insets];
            }
        }
    }];
    
    [self.viewController addChildViewController: self->$observer];

    [self.viewController.view addSubview: self->$observer.view];
    
    [NSLayoutConstraint activateConstraints:@[
        [self->$observer.view.topAnchor constraintEqualToAnchor:self.viewController.view.topAnchor],
        [self->$observer.view.bottomAnchor constraintEqualToAnchor:self.viewController.view.bottomAnchor],
        [self->$observer.view.leadingAnchor constraintEqualToAnchor:self.viewController.view.leadingAnchor],
        [self->$observer.view.trailingAnchor constraintEqualToAnchor:self.viewController.view.trailingAnchor],
    ]];
    
    [self->$observer didMoveToParentViewController: self.viewController];
}

- (void) emitInsetChange:(NSDictionary*) listener inset:(UIEdgeInsets) inset {
    NSString* callbackID = [listener objectForKey: @"callbackID"];
    if (callbackID == nil) return;
    
    NSString* ident = [listener objectForKey:@"id"];
    
    if (ident == nil) return;
    
    NSDictionary* payload = @{
        @"type": @"update",
        @"id": ident,
        @"data": @{
            @"top": @(inset.top),
            @"bottom": @(inset.bottom),
            @"left": @(inset.left),
            @"right": @(inset.right)
        }
    };
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsDictionary: payload];
    [result setKeepCallbackAsBool: true];
    [self.commandDelegate sendPluginResult: result callbackId: callbackID];
}

- (void) create:(CDVInvokedUrlCommand*) command {
    NSString* ident = [[NSUUID UUID] UUIDString];
    
    NSDictionary* listener = @{
        @"id": ident,
        @"callbackID": command.callbackId
    };
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
        @"type": @"init",
        @"data": ident
    }];
    [result setKeepCallbackAsBool: true];
    
    @synchronized (self->$listeners) {
        [self->$listeners setObject: listener forKey: ident];
    }
    
    [self.commandDelegate sendPluginResult: result callbackId: command.callbackId];
    
    [self emitInsetChange: listener inset: self->$observer.view.safeAreaInsets];
}

- (void) delete:(CDVInvokedUrlCommand*) command {
    NSString* ident = [command argumentAtIndex: 0];
    
    @synchronized (self->$listeners) {
        [self->$listeners removeObjectForKey: ident];
    }
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult: result callbackId: command.callbackId];
}

- (void) dealloc {
    [self->$observer willMoveToParentViewController: nil];
    [self->$observer.view removeFromSuperview];
    [self->$observer removeFromParentViewController];
    self->$observer.safeAreaChanged = nil;
    self->$observer = nil;
}

@end
