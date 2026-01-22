/*
   Copyright 2026 Total Pave Inc.

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
#import "TPIInsetObserverController.h"
#import "TPIInsetObserver.h"

@implementation TPIInsetObserverController : UIViewController

- (instancetype) initWithSAICallback:(TPIInsetSAICallback) callback {
    self = [super initWithNibName: nil bundle: nil];
    
    self.safeAreaChanged = callback;
    
    return self;
}

- (void) loadView {
    TPIInsetObserver* view = [[TPIInsetObserver alloc] initWithFrame:[UIScreen mainScreen].bounds];
    view.safeAreaChanged = self.safeAreaChanged;
    view.userInteractionEnabled = false;
    view.translatesAutoresizingMaskIntoConstraints = false;
    self.view = view;
}

- (void) viewDidLoad {
    [super viewDidLoad];
    UIView* parent = self.view.superview;
    if (!parent) return;
    
    [NSLayoutConstraint activateConstraints:@[
        [self.view.topAnchor constraintEqualToAnchor:parent.topAnchor],
        [self.view.bottomAnchor constraintEqualToAnchor:parent.bottomAnchor],
        [self.view.leadingAnchor constraintEqualToAnchor:parent.leadingAnchor],
        [self.view.trailingAnchor constraintEqualToAnchor:parent.trailingAnchor],
    ]];
}

- (void) dealloc {
    self.safeAreaChanged = nil;
    self.view = nil;
}

@end
