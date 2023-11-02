/*
   Copyright 2019 Total Pave Inc.

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

package com.totalpave.cordova.insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.lang.NumberFormatException;

public class Insets extends CordovaPlugin {
    public CallbackContext listener;
    private JSONObject insets;

    @Override
    protected void pluginInitialize() {
        ViewCompat.setOnApplyWindowInsetsListener(
            this.cordova.getActivity().findViewById(android.R.id.content), (v, insetProvider) -> {
                JSONObject result = new JSONObject();
                
                try {
                    float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;

                    // Ideally, we'd import this, but it shares the same name as our plugin
                    int insetTypes = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars();
                    androidx.core.graphics.Insets insets = insetProvider.getInsets(insetTypes);
                    
                    result.put("top", insets.top / density);
                    result.put("right", insets.right / density);
                    result.put("bottom", insets.bottom / density);
                    result.put("left", insets.left / density);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return insetProvider.CONSUMED; // Stop dispatching to child views
                }
                this.insets = result;
                if (listener != null) {
                    PluginResult presult = new PluginResult(Status.OK, this.insets);
                    presult.setKeepCallback(true);
                    listener.sendPluginResult(presult);
                }
                return insetProvider.CONSUMED; // Stop dispatching to child views
            }
        );
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException, NumberFormatException {
        if (action.equals("setListener")) {
            listener = callback;
            cordova.getActivity().runOnUiThread(() -> {
                if (this.insets == null) {
                    // Trigger a inset dispatch (will eventually jump to the ApplyWindowInsetsListener above)
                    ViewCompat.requestApplyInsets(cordova.getActivity().findViewById(android.R.id.content));
                }
                else {
                    PluginResult presult = new PluginResult(Status.OK, this.insets);
                    presult.setKeepCallback(true);
                    listener.sendPluginResult(presult);
                }
            });
            return true;
        }
        return false;
    }
}
