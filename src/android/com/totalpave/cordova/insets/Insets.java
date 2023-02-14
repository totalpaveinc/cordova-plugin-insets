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
            this.cordova.getActivity().findViewById(android.R.id.content), (v, insets) -> {
                JSONObject result = new JSONObject();
                try {
                    float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;
                    result.put("top", insets.getSystemWindowInsetTop() / density);
                    result.put("right", insets.getSystemWindowInsetRight() / density);
                    result.put("bottom", insets.getSystemWindowInsetBottom() / density);
                    result.put("left", insets.getSystemWindowInsetLeft() / density);

                } catch (JSONException e) {
                    e.printStackTrace();
                    return insets.consumeSystemWindowInsets();
                }
                this.insets = result;
                PluginResult presult = new PluginResult(Status.OK, this.insets);
                presult.setKeepCallback(true);
                listener.sendPluginResult(presult);
                return insets.consumeSystemWindowInsets();
            }
        );
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException, NumberFormatException {
        if (action.equals("setListener")) {
            listener = callback;
            if (this.insets == null) {
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(this.cordova.getActivity().findViewById(android.R.id.content));
                if (insets != null) {
                    JSONObject result = new JSONObject();
                    try {
                        float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;
                        result.put("top", insets.getSystemWindowInsetTop() / density);
                        result.put("right", insets.getSystemWindowInsetRight() / density);
                        result.put("bottom", insets.getSystemWindowInsetBottom() / density);
                        result.put("left", insets.getSystemWindowInsetLeft() / density);
                        this.insets = result;
                        PluginResult presult = new PluginResult(Status.OK, this.insets);
                        presult.setKeepCallback(true);
                        listener.sendPluginResult(presult);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.error(e.getMessage());
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
