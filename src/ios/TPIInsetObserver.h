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


#ifndef TPIInsetObserver_h
#define TPIInsetObserver_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

/**
    iOS SDK does not provide an ability to listen for safe area inset changes outside of views, so
    this is a dummy view that can be attached to the window and it accepts a function pointer which will dispatch safe area changes to.
    When the view is attached to the window, it will fire at least one event. So setup the function hook before attaching it to the window hierachy.
 */
@interface TPIInsetObserver: UIView

@property (nonatomic, copy) void (^safeAreaChanged)(UIEdgeInsets insets);

@end

#endif
