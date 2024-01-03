/*
   Copyright 2022 Total Pave Inc.

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

/**
 * An enumeration of Inset Types.
 * These are mapped to android's native WindowInsetsCompat.TYPE
 * 
 * See https://developer.android.com/reference/androidx/core/view/WindowInsetsCompat.Type
 * for more information.
 * 
 * Note that the native constant values is an implementation detail,
 * therefore the values here isn't a direct mapping, but will be resolved
 * appropriately.
 */
export enum InsetMask {
    CAPTION_BAR                 = 1,
    DISPLAY_CUTOUT              = 1 << 1,
    IME                         = 1 << 2,
    MANDATORY_SYSTEM_GESTURES   = 1 << 3,
    NAVIGATION_BARS             = 1 << 4,
    STATUS_BARS                 = 1 << 5,
    SYSTEM_BARS                 = 1 << 6,
    SYSTEM_GESTURES             = 1 << 7,
    TAPPABLE_ELEMENT            = 1 << 8
};
