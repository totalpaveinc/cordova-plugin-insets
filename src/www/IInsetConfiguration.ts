/*
   Copyright 2022-2024 Total Pave Inc.

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

export interface IInsetConfiguration {
    /**
     * A bit mask of InsetMask
     * 
     * @defaults DISPLAY_CUTOUT | SYSTEM_BARS
     */
    mask?: number;

    /**
     * If true, includes rounded corners in the inset information
     * Only available on Android API 31 ("S") and later.
     * 
     * @defaults true
     */
    includeRoundedCorners?: boolean;
}
