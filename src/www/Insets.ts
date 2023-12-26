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

import { InsetType } from './InsetType';

export interface IInsetsAPI {
    addListener: (callback: IInsetCallbackFunc) => void;
    removeListener: (callback: IInsetCallbackFunc) => void;
    getInsets: () => IInsets;
}
declare global {
    interface ITotalpave {
        Insets: IInsetsAPI;
    }
    interface Window {
        totalpave: ITotalpave;
    }
}

export const SERVICE_NAME: string = "Insets";

export interface IInsets {
    top: number;
    right: number;
    bottom: number;
    left: number;
}

export type IInsetCallbackFunc = (inset: IInsets) => void;

class InsetsAPI implements IInsetsAPI {
    private initPromise: Promise<void>;
    private listeners: Array<Function> = [];
    private insets: IInsets = {
        top: 0,
        right: 0,
        bottom: 0,
        left: 0
    };

    public async setMask(mask: number): Promise<IInsets> {
        if (cordova.platformId === 'ios') {
            return this.insets;
        }

        return new Promise<IInsets>((resolve, reject) => {
            let self = this;
            cordova.exec(
                (insets: IInsets) => {
                    self.insets = insets;
                    resolve(insets);
                },
                reject,
                SERVICE_NAME,
                "setMask",
                [ mask ]
            )
        });
    }

    /**
     * Initializes javascript side of the plugin.
     * 
     * This function is called automatically on deviceready.
     * @internal
     */
    public __init(): Promise<void> {
        if (this.initPromise) {
            return this.initPromise;
        }
        this.initPromise = new Promise<void>((resolve, reject) => {
            // no-op on iOS, still installs to iOS so apps don't need to do platform checks.
            if (cordova.platformId === 'ios') {
                resolve();
                return;
            }

            // Setup promise resolving mechanism.
            // We don't use the cordova callback functions as they will be called multiple times over the lifespan of an app.
            let func = () => {
                resolve();
                this.removeListener(func);
            }
            this.addListener(func);

            // Setup cordova callback.
            let that = this;
            cordova.exec(
                (insets: IInsets) => {
                    that.insets = insets;
                    for (let i = 0, listeners = that.listeners.slice(), length = listeners.length; i < length; ++i) {
                        listeners[i](insets);
                    }
                },
                reject,
                SERVICE_NAME,
                "setListener",
                []
            );
        });
    }

    public addListener(callback: IInsetCallbackFunc) {
        this.listeners.push(callback);
    }

    public removeListener(callback: IInsetCallbackFunc) {
        let index = this.listeners.indexOf(callback);
        if (index === -1) {
            return;
        }
        this.listeners.splice(index, 1);
    }

    /**
     * @returns Last emitted insets.
     */    
    public getInsets(): IInsets {
        return this.insets;
    }
};

export const Insets = new InsetsAPI();

document.addEventListener('deviceready', function() {
    Insets.__init();
});
