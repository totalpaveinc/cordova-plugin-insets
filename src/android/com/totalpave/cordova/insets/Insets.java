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

import android.os.Build;
import android.view.RoundedCorner;
import android.view.WindowInsets;
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
                    int insetTypes = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime();
                    androidx.core.graphics.Insets insets = insetProvider.getInsets(insetTypes);

                    double topLeftRadius = 0.0;
                    double topRightRadius = 0.0;
                    double botLeftRadius = 0.0;
                    double botRightRadius = 0.0;

                    WindowInsets sourceInsets = insetProvider.toWindowInsets();
                    if (sourceInsets != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            RoundedCorner topLeft = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
                            RoundedCorner topRight = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT);
                            RoundedCorner botLeft = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT);
                            RoundedCorner botRight = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT);

                            if (topLeft != null) {
                                int radius = topLeft.getRadius();
                                topLeftRadius = (double)radius / density;
                            }

                            if (topRight != null) {
                                int radius = topRight.getRadius();
                                topRightRadius = (double)radius / density;
                            }

                            if (botLeft != null) {
                                int radius = botLeft.getRadius();
                                botLeftRadius = (double)radius / density;
                            }

                            if (botRight != null) {
                                int radius = botRight.getRadius();
                                botRightRadius = (double)radius / density;
                            }
                        }
                    }

                    double top = insets.top / density;
                    double right = insets.right / density;
                    double bottom = insets.bottom / density;
                    double left = insets.left / density;

                    // Insets do not include rounded corner radius. If an inset is present, it generally will be big enough to cover the rounded corner. This is a coincidence, not a designed thing.
                    // In either case, we need to determine how much space is required to cover the rounded corner and take the higher betwen the inset and the rounded corner.

                    top = Math.max(Math.max(top, topLeftRadius), topRightRadius);
                    bottom = Math.max(Math.max(bottom, botLeftRadius), botRightRadius);
                    left = Math.max(Math.max(left, topLeftRadius), botLeftRadius);
                    right = Math.max(Math.max(right, topRightRadius), botRightRadius);
                    
                    result.put("top", top);
                    result.put("right", right);
                    result.put("bottom", bottom);
                    result.put("left", left);
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
