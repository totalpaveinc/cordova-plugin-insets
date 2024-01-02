cordova-plugin-insets
=====================

This plugin provides access to Android's native unsafe area insets. Normally these insets are accessed through the CSS env variables, unsafe-area-inset-* (where * is top, right, bottom, or left).

These env variables are recognized by Android's webviews but are not properly implemented. They always resolve to 0.

This plugin provides a work around to obtain these values in javascript. You will have to implement your own javascript code to actually start using these values in your CSS.

## Supported Platforms

Android.

If your app suppots multiple platforms, the API calls to this plugin should be platform guarded.

```javascript
if (cordova.platform === 'android') {
    let insets = await window.totalpave.Inset.create();
}
```

## Documentation

See [Docs](./docs.md)

## Licenses

This plugin is licensed under Apache 2.0. See [LICENSE](./LICENSE) for more information.
