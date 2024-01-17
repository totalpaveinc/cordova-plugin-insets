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

package com.totalpave.cordova.inset;

import android.content.res.Configuration;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Inset extends CordovaPlugin {
    public static final int DEFAULT_INSET_MASK = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars();
    public static final boolean DEFAULT_INCLUDE_ROUNDED_CORNERS = true;

    public static class WebviewMask {
        private WebviewMask() {}

        public static final int CAPTION_BAR                 = 1;
        public static final int DISPLAY_CUTOUT              = 1 << 1;
        public static final int IME                         = 1 << 2;
        public static final int MANDATORY_SYSTEM_GESTURES   = 1 << 3;
        public static final int NAVIGATION_BARS             = 1 << 4;
        public static final int STATUS_BARS                 = 1 << 5;
        public static final int SYSTEM_BARS                 = 1 << 6;
        public static final int SYSTEM_GESTURES             = 1 << 7;
        public static final int TAPPABLE_ELEMENT            = 1 << 8;
    }

    public static class ListenerConfiguration {
        public Integer mask;
        public boolean includeRoundedCorners;

        public ListenerConfiguration() {
            mask = null;
            includeRoundedCorners = DEFAULT_INCLUDE_ROUNDED_CORNERS;
        }
    }

    public static class Listener {
        private final Context $context;
        private final CallbackContext $callback;
        private JSONObject $currentInset;
        private final int $mask;
        private final boolean $includeRoundedCorners;
        private final UUID $id;

        public Listener(Context context, CallbackContext callback, ListenerConfiguration config) {
            $id = UUID.randomUUID();
            $context = context;
            $callback = callback;
            if (config.mask == null) {
                $mask = DEFAULT_INSET_MASK;
            }
            else {
                $mask = $mapMask(config.mask);
            }
            $includeRoundedCorners = config.includeRoundedCorners;
        }

        public String getID() {
            return $id.toString();
        }

        public void onInsetUpdate(WindowInsetsCompat insetProvider) {
            JSONObject result = new JSONObject();

            try {
                float density = $context.getResources().getDisplayMetrics().density;

                // Ideally, we'd import this, but it shares the same name as our plugin
                androidx.core.graphics.Insets insets = insetProvider.getInsets($mask);

                double topLeftRadius = 0.0;
                double topRightRadius = 0.0;
                double botLeftRadius = 0.0;
                double botRightRadius = 0.0;

                if ($includeRoundedCorners && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    WindowInsets sourceInsets = insetProvider.toWindowInsets();
                    if (sourceInsets != null) {
                        RoundedCorner topLeft = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
                        RoundedCorner topRight = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT);
                        RoundedCorner botLeft = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT);
                        RoundedCorner botRight = sourceInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT);

                        if (topLeft != null) {
                            int radius = topLeft.getRadius();
                            topLeftRadius = (double) radius / density;
                        }

                        if (topRight != null) {
                            int radius = topRight.getRadius();
                            topRightRadius = (double) radius / density;
                        }

                        if (botLeft != null) {
                            int radius = botLeft.getRadius();
                            botLeftRadius = (double) radius / density;
                        }

                        if (botRight != null) {
                            int radius = botRight.getRadius();
                            botRightRadius = (double) radius / density;
                        }
                    }
                }

                double top = insets.top / density;
                double right = insets.right / density;
                double bottom = insets.bottom / density;
                double left = insets.left / density;

                // First we will get the screen orientation. This may be locked by the user, so it
                // may not match the physical orientation. If the orientation cannot be determined,
                // we will assume PORTRAIT
                int orientation = Configuration.ORIENTATION_UNDEFINED;

                // There are other orientation types, albeit deprecated and supposedly no longer
                // used, but this limits us from handling only portrait and landscape.
                switch ($context.getResources().getConfiguration().orientation) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                    case Configuration.ORIENTATION_PORTRAIT:
                        orientation = $context.getResources().getConfiguration().orientation;
                        break;
                    case Configuration.ORIENTATION_SQUARE:
                    case Configuration.ORIENTATION_UNDEFINED:
                        // SQUARE is not used anymore since API 16, but included just to satisfy
                        // lint warnings. If undefined, then fallback to PORTRAIT
                        orientation = Configuration.ORIENTATION_PORTRAIT;
                        break;
                }

                // Insets do not include rounded corner radius. If an inset is present, it
                // generally will be big enough to cover the rounded corner. This is a coincidence,
                // not a designed thing. In either case, we need to determine how much space is
                // required to cover the rounded corner and take the higher between the inset and
                // the rounded corner.

                // If portrait, then top-left & top-right is applied to the top inset,
                // and bot-left & bot-right is applied to the bottom inset
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    top = Math.max(Math.max(top, topLeftRadius), topRightRadius);
                    bottom = Math.max(Math.max(bottom, botLeftRadius), botRightRadius);
                }
                else {
                    left = Math.max(Math.max(left, topLeftRadius), botLeftRadius);
                    right = Math.max(Math.max(right, topRightRadius), botRightRadius);
                }

                result.put("top", top);
                result.put("right", right);
                result.put("bottom", bottom);
                result.put("left", left);
            }
            catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            this.$currentInset = result;

            JSONObject update = new JSONObject();
            try {
                update.put("type", "update");
                update.put("id", this.getID());
                update.put("data", this.$currentInset);
            }
            catch (JSONException ex) {
                throw new RuntimeException(ex);
            }

            PluginResult response = new PluginResult(Status.OK, update);
            response.setKeepCallback(true);
            $callback.sendPluginResult(response);
        }

        private int $mapMask(int webviewMask) {
            int insetTypeMask = 0;

            if ((webviewMask & WebviewMask.CAPTION_BAR) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.captionBar();
            }

            if ((webviewMask & WebviewMask.DISPLAY_CUTOUT) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.displayCutout();
            }

            if ((webviewMask & WebviewMask.IME) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.ime();
            }

            if ((webviewMask & WebviewMask.MANDATORY_SYSTEM_GESTURES) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.mandatorySystemGestures();
            }

            if ((webviewMask & WebviewMask.NAVIGATION_BARS) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.navigationBars();
            }

            if ((webviewMask & WebviewMask.STATUS_BARS) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.statusBars();
            }

            if ((webviewMask & WebviewMask.SYSTEM_BARS) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.systemBars();
            }

            if ((webviewMask & WebviewMask.SYSTEM_GESTURES) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.systemGestures();
            }

            if ((webviewMask & WebviewMask.TAPPABLE_ELEMENT) != 0) {
                insetTypeMask |= WindowInsetsCompat.Type.tappableElement();
            }

            return insetTypeMask;
        }
    }

    private ArrayList<Listener> $listeners;
    private HashMap<String, Listener> $listenerMap;
    private final Object $listenerLock = new Object();

    @Override
    protected void pluginInitialize() {
        $listeners = new ArrayList<>();
        $listenerMap = new HashMap<>();
        
        ViewCompat.setOnApplyWindowInsetsListener(
                this.cordova.getActivity().findViewById(android.R.id.content), (v, insetProvider) -> {

                    synchronized($listenerLock) {
                        for (Listener listener : $listeners) {
                            listener.onInsetUpdate(insetProvider);
                        }
                    }

                    return WindowInsetsCompat.CONSUMED;
                }
        );
    }

    private void $createNewListener(CallbackContext callback, JSONArray args) {
        ListenerConfiguration config = new ListenerConfiguration();

        try {
            JSONObject params = args.getJSONObject(0);
            if (params.has("mask")) {
                config.mask = params.getInt("mask");
            }
            if (params.has("includeRoundedCorners")) {
                config.includeRoundedCorners = params.getBoolean("includeRoundedCorners");
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            callback.error(ex.getMessage());
            return;
        }
        Listener listener = new Listener(cordova.getActivity(), callback, config);
        synchronized ($listenerLock) {
            $listeners.add(listener);
            $listenerMap.put(listener.getID(), listener);
        }

        cordova.getActivity().runOnUiThread(() -> {
            ViewCompat.requestApplyInsets(cordova.getActivity().findViewById(android.R.id.content));
        });

        JSONObject responseData = new JSONObject();
        try {
            responseData.put("type", "init");
            responseData.put("data", listener.getID());
        }
        catch(JSONException e) {
            throw new RuntimeException("Could not build listener creation response", e);
        }

        PluginResult result = new PluginResult(Status.OK, responseData);
        result.setKeepCallback(true);
        callback.sendPluginResult(result);
    }

    private void $freeListener(CallbackContext callback, JSONArray args) throws JSONException {
        String id = args.getString(0);

        synchronized ($listenerLock) {
            Listener listener = $listenerMap.remove(id);
            if (listener != null) {
                $listeners.remove(listener);
            }
        }

        callback.success();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException, NumberFormatException {
        if (action.equals("create")) {
            $createNewListener(callback, args);
            return true;
        }
        else if (action.equals("delete")) {
            $freeListener(callback, args);
            return true;
        }

        return false;
    }
}
