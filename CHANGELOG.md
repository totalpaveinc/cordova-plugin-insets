
@totalpave/cordova-plugin-insets
--------------------------------

# Changelog

## 0.3.0 (TBD)

### Breaking Changes:

#### Depluralize symbols

Several symbols were written in plural form which goes against common naming
conventions. In effort to make the API a bit cleaner, this was corrected.

|Old Name|New Name|
|---|---|
|`Insets`|`Inset`|
|`IInsets`|`IInset`|

`window.totalpave.Insets` has been renamed to `window.totalpave.Inset`

The typescript global typedefs have been updated accordingly.

#### IInsetAPI

This type interface was removed, the `Inset` class can be referenced instead.

#### Inset API is no longer static

The static `addListener`, `removeListener`, and `getInsets` APIs have been
removed and replaced with non-static versions. Two new static methods are introduced:

- `Inset.create`
- `Inset.free`

It is now the application's responsibility to create their own `Inset` instance via
`Inset.create`, passing
in their own configuration object. An instance of `Inset` will be returned that
can be used like before.

Once the inset instance is no longer needed it will be desirable to free resources
by calling `inset.free()` which will free up retained references allowing the
objects to be garbage collected.

Most use cases only calls for a single instance to be created for the application,
but it is valid to create several `Inset` instances with different configuration
parameters.

A non-static version of `getInsets` as noted above was introduced replacing the
static method. However `getInsets` is also deprecated in effort to de-pluralize
the API for better naming conventions. It will be removed in a future release.

Any usages of `getInsets` should use `getInset` instead.

## 0.2.0 (December 6, 2023)

### Breaking Changes:

These are the breaking changes in this release.

#### cordova-android@11 or later requirement

Starting with 0.2.0, we will need cordova-android@11 or later.
This is because we are using API 31+ APIs when available.
While we SDK guard our usage, we still need to compile with SDK 31+ and cordova-android@11 by default will compile with SDK 32.

#### Removal of Babel

TotalPave has been removing usage of Babel in their builds across several of their packages and instead letting Typescript transpile down to a sensible ES target.

Starting with 0.2.0, we will be targeting [ES 2017](https://caniuse.com/?search=es2017) which has pretty wide support on major browsers dating back about a decade.

We find that removing Babel leads to more simplified and less broken sourcemaps, as well as simplier dependency trees.

#### Rounded Corners

Starting with 0.2.0, coming back to the API 31+ usages, this plugin will now count for rounded corners on devices. If the screen has a rounded corner take the radius of the rounded corner and potentially use it for the inset value.

Insets from rounded corners are not additive. If there is an inset present already from a system bar or display cutout, we will use only one or the other, whichever is larger. This is because rounded corners are not resolved in the same way as system bars/display cutouts and any existing inset will likely already clear the rounded corner radius.

The API to get rounded corner information is available only on API 31 and later devices. Therefore, older devices, including devices that still have rounded corners but are running on an older API level will not have rounded corners counted for in the given insets.

## 0.1.10 (November 2, 2023)
- Fixed insets to count for display cutouts, such as camera pinholes or islands.

## 0.1.7 (July 6, 2023)
- Fixed clobbers namespace to merge instead, since this particular plugin targets a shared namespace.

## 0.1.6 (April 20, 2023)
-   Replaced `insets.getSystemWindowInset*` usages that was deprecated in API 30.
-   Replaced `insets.consumeSystemWindowInsets` usages that was deprecated in API 30.
-   Fix an issue on API 30 and earlier devices where the inset provided on launch may be incorrect.

## 0.1.5 (April 13, 2023)

-   Initial Release to NPM
