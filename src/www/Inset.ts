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

import {IInsetConfiguration} from './IInsetConfiguration';
import {IInsetCallbackFunc} from './IInsetCallbackFunc';
import {IInset} from './IInset';

export const SERVICE_NAME: string = "Inset";

interface IInsetEvent<T = unknown> {
    type: 'init' | 'update';
    data: T;
}

type IInsetInitEvent = IInsetEvent<string>;
interface IInsetUpdateEvent extends IInsetEvent<IInset> {
    id: string;
}

export class Inset {
    private $currentInset: IInset;
    private $listeners: IInsetCallbackFunc[];
    private $id: string;

    private constructor() {
        this.$id = null;
        this.$listeners = [];
        this.$currentInset = {
            top: 0,
            left: 0,
            right: 0,
            bottom: 0
        };
    }

    /**
     * Gets the native identifier
     * 
     * @returns 
     */
    public getID(): string {
        return this.$id;
    }

    /**
     * Gets the last emitted inset information
     * 
     * @returns
     */
    public getInsets(): IInset {
        console.warn('getInsets() is deprecated, use getInset instead()', new Error().stack);
        return this.getInset();
    }

    public getInset(): IInset {
        return this.$currentInset;
    }

    /**
     * See the static Inset.free method for details
     * 
     * This is the equivilant of calling Inset.free(insetInstance)
     * 
     * @returns 
     */
    public async free(): Promise<void> {
        return await Inset.free(this);
    }

    /**
     * Adds a listener to this inset configuration.
     * 
     * Note that this may fire even if nothing has actually
     * changed.
     * 
     * Retain the listener reference to remove it later if
     * necessary.
     * 
     * @param listener 
     */
    public addListener(listener: IInsetCallbackFunc): void {
        this.$listeners.push(listener);
        listener(this.$currentInset);
    }

    /**
     * Frees the listener reference
     * 
     * @param listener 
     */
    public removeListener(listener: IInsetCallbackFunc): void {
        let idx: number = this.$listeners.indexOf(listener);
        if (idx > -1) {
            this.$listeners.splice(idx, 1);
        }
    }

    private $onUpdate(insets: IInset): void {
        this.$currentInset = insets;

        for (let i = 0; i < this.$listeners.length; i++) {
            this.$listeners[i](insets);
        }
    }

    private static $generateID(): string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0,
                v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    /**
     * Configures a new Inset instance to listen for inset changes
     * It's valid to have multiple instances, with different configurations
     * Each instance may have 0-to-many listeners attached via addListener
     * 
     * If this instance is no longer needed/used, call `free` to free
     * resources.
     * 
     * It will be more performant to keep the instance count low. If only one
     * configuration set is needed, then it would be recommended to create a
     * single instance and share it rather than every object having it's own
     * inset listener instance.
     * 
     * @param config 
     * @returns 
     */
    public static create(config: IInsetConfiguration): Promise<Inset> {
        return new Promise<Inset>((resolve, reject) => {
            if (!config) {
                config = {};
            }

            if (cordova.platformId === 'ios') {
                let instance: Inset = new Inset();
                instance.$id = Inset.$generateID();
                resolve(instance);
                return;
            }

            let inset: Inset = new Inset();

            cordova.exec(
                (e: IInsetEvent) => {
                    if (Inset.$isInitEvent(e)) {
                        inset.$id = e.data;
                        resolve(inset);
                    }
                    else if (Inset.$isUpdateEvent(e)) {
                        inset.$onUpdate(e.data);
                    }
                },
                reject,
                SERVICE_NAME,
                "create",
                [config]
            );
        });
    }

    /**
     * Frees the native resources associated with the given
     * inset.
     * 
     * After freeing, the inset is no longer usable and it will
     * not receive anymore inset updates. If you retain any
     * references to inset listeners, they should also be dereferenced
     * to allow for garbage collection.
     * 
     * This is the equivilant of calling `await inset.free()`
     * 
     * @param inset 
     * @returns 
     */
    public static free(inset: Inset | string): Promise<void> {
        let id: string = null;

        if (typeof inset === 'string') {
            id = inset;
        }
        else {
            id = inset.getID();
        }

        return new Promise<void>((resolve, reject) => {
            if (cordova.platformId === 'ios') {
                resolve();
                return;
            }

            cordova.exec(
                () => {
                    resolve();
                },
                reject,
                SERVICE_NAME,
                "delete",
                [id]
            );
        });
    }

    private static $isInitEvent(e: IInsetEvent): e is IInsetInitEvent {
        return e.type === 'init';
    }

    private static $isUpdateEvent(e: IInsetEvent): e is IInsetUpdateEvent {
        return e.type === 'update';
    }
}

declare global {
    interface ITotalpave {
        Inset: Inset;
    }
    interface Window {
        totalpave: ITotalpave;
    }
}
