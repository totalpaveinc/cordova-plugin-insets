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

export const SERVICE_NAME: string = "Insets";

export interface IInsets {
    top: number;
    right: number;
    bottom: number;
    left: number;
}

export class Insets {
    private static initPromise: Promise<void>;
    private static listeners: Array<Function> = [];
    private static insets: IInsets = {
        top: 0,
        right: 0,
        bottom: 0,
        left: 0
    };

    /**
     * Initializes javascript side of the plugin.
     * 
     * This function is called automatically on deviceready.
     */
    public static init(): Promise<void> {
        if (this.initPromise) {
            return this.initPromise;
        }
        this.initPromise = new Promise<void>((resolve, reject) => {
            // Setup promise resolving mechanism.
            // We don't use the cordova callback functions as they will be called multiple times over the lifespan of an app.
            let func = () => {
                resolve();
                Insets.removeListener(func);
            }
            Insets.addListener(func);

            // Setup cordova callback.
            cordova.exec(
                (insets: IInsets) => {
                    Insets.insets = insets;
                    for (let i = 0, listeners = Insets.listeners, length = listeners.length; i < length; ++i) {
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

    public static addListener(callback: Function) {
        this.listeners.push(callback);
    }

    public static removeListener(callback: Function) {
        let index = this.listeners.indexOf(callback);
        if (index === -1) {
            return;
        }
        this.listeners.splice(index, 1);
    }

    /**
     * @returns Last emitted insets.
     */    
    public static getInsets(): IInsets {
        return Insets.insets;
    }
};

document.addEventListener('deviceready', function() {
    Insets.init();
});
