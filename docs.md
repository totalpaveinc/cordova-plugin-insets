
Cordova Android Insets Listener Plugin
======================================

This document describes the public API available to library consumers.

# Table of Contents
- [1.0 - General Notes](#10---general-notes)
  - [1.1 - TypeScript](#11---typescript)
- [2.0 - Interfaces](#20---interfaces)
  - [2.1 - IInset](#21---iinset)
  - [2.2 - IInsetCallbackFunc](#22---iinsertcallbackfunc)
  - [2.3 - IInsetConfiguration](#23---iinsetconfiguration)
  - [2.4 - InsetMask](#24---insetmask)
- [3.0 - Inset](#30---inset)
  - [3.1 - create](#31---create)
  - [3.2 - free](#32---free)
  - [3.3 - addListener](#33---addlistener)
  - [3.4 - removeListener](#34---removelistener)
  - [3.5 - getInset](#35---getinset)

## 1.0 - General Notes
The namespace of this plugin is `window.totalpave.Inset`. It will become available after the `deviceready` event. Throughout this document, I'll refer to the `totalpave.Inset` object as `Inset`.

### 1.1 - TypeScript

This library is authored in TypeScript and provides TypeScript typings, however consuming this package as typescript is experimental. If you use bundlers, you may get duplicate runtimes. Changes to how the library is consumed may change without warning.

## 2.0 - Interfaces

This section describes the interfaces that will be encountered when using this API.

### 2.1 - IInsets

IInsets is an interface that describes an Inset dataset. It has the following structure:

```typescript
interface IInsets {
    top: number;
    left: number;
    right: number;
    bottom: number;
}
```

### 2.2 - IInsertCallbackFunc

A type that describes the callback signature to [addListener](#31---addlistener)/[removeListener](#32---removelistener). It contains the following signature:

```typescript
type IInsetCallbackFunc = (inset: IInsets) => void;
```

### 2.3 - IInsetConfiguration

A type that describes the configuration object used when creating new `Inset` provider instances. All fields are optional.

Structure:

```typescript
interface IInsetConfiguration {
  mask?: number;
  includeRoundedCorners?: boolean;
}
```

If `mask` is not set, the default will be `DISPLAY_CUTOUT | SYSTEM_BARS`.
If `includeRoundedCorners` is not set, the default will be `true`.

### 2.4 - InsetMask

An enumeration of mask values. These correlate to [WindowInsetsCompat.Type](https://developer.android.com/reference/androidx/core/view/WindowInsetsCompat.Type) though the values may not match, they are mapped during runtime.

Currently the following mask values are supported:

- CAPTION_BAR
- DISPLAY_CUTOUT
- IME
- MANDATORY_SYSTEM_GESTURES
- NAVIGATION_BARS
- STATUS_BARS
- SYSTEM_BARS
- SYSTEM_GESTURES
- TAPPABLE_ELEMENT

If the android SDK introduces any newer ones, feel free to open a PR.

## 3.0 - Inset

`Inset` is a class that provides the APIs to retrieve and to listen for inset changes. It serves as a provider object and wil hold the configuration state as well as a collection of inset listeners. It is valid to have more than one `Inset` object alive in your application, with different configurations though it will be recommended to keep the instance count low for performance reasons.

Direct access to the `Inset` constructor is forbidden but an instance can be created via the static `create` method. If the `Inset` provider instance is no
longer used, it should be freed via `Insets.free(inset)` which will clean up
references to objects and allow them to be garbage collected, both in the webview
and in the Java/Native runtime. Additionally if your application holds any references to listener functions, they should also be cleaned up.

Note that the configuration parameters cannot be modified on an inset provider
instance.

### 3.1 - create

Creates a new `Inset` provider object with the given configuration. If the configuration object is missing, default values are used as indicated in [IInsetConfiguration](#23---iinsetconfiguration).

The returned `Inset` instance can be used to attach listeners on for inset
update notifications. When the provider is no longer needed, it should be freed by calling [free()](#32---free).

Creating `Inset` objects will trigger a layout request, triggering an update on
all inset providers.

##### Signature

```typescript
static create(config?: IInsetConfiguration): Promise<Inset>;
```

### 3.2 - free

Frees an inset provider, cleaning up references to allow objects to be
garbage collected. Once freed, it will be unhooked from the global listener system
and that inset provider instance will no longer receive inset updates.

It's the applications responsibility to clean up any retained `IInsetCallbackFunc`
listener functions.

##### Signature

```typescript
static free(inset: Inset): Promise<void>;
```

Alternatively, the inset can be freed by calling the instance member `free`.

```typescript
free(): Promise<void>;
```

### 3.3 - addListener

Attaches a new handler which will be invoked when the view layout/inset information changes. The reference of `callback` must be retained by the user for the [removeListener](#32---removelistener) call.

When the callback is invoked, it will receive an [IInset](#21---iinset) object as an argument.

##### Signature

```typescript
addListener(callback: IInsetCallbackFunc): void;
```

### 3.4 - removeListener

Removes an attached handler from the listener pool.
This will be a no-op if the `callback` is not in the listener pool.

##### Signature

```typescript
removeListener(callback: IInsetCallbackFunc): void;
```

### 3.5 - getInset

Returns the last known [IInset](#21---iinset) object.

##### Signature

```typescript
getInset(): IInset;
```
