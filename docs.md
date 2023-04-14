
Cordova Android Insets Listener Plugin
======================================

This document describes the public API available to library consumers.

# Table of Contents
- [1.0 - General Notes](#10---general-notes)
  - [1.1 - TypeScript](#11---typescript)
- [2.0 - Interfaces](#20---interfaces)
  - [2.1 - IInsets](#21---iinsets)
  - [2.2 - IInsetCallbackFunc]()
- [3.0 - Insets](#30---insets)
  - [3.1 - addListener](#31---addlistener)
  - [3.2 - removeListener](#32---removelistener)
  - [3.3 - getInsets](#33---getinsets)

## 1.0 - General Notes
The namespace of this plugin is `totalpave.Insets`. It will become available after the `deviceready` event. Throughout this document, I'll refer to the `totalpave.Insets` object as `Insets`.

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

## 3.0 - Insets

`Insets` is a static class that provides the APIs to retrieve and to listen for inset changes.

### 3.1 - addListener

Attaches a new handler which will be invoked when the view layout/inset information changes. The reference of `callback` must be retained by the user for the [removeListener](#32---removelistener) call.

When the callback is invoked, it will receive an [IInsets](#21---iinsets) object as an argument.

##### Signature

```typescript
static addListener(callback: IInsetCallbackFunc): void;
```

### 3.2 - removeListener

Removes an attached handler from the listener pool.
This will be a no-op if the `callback` is not in the listener pool.

##### Signature

```typescript
static removeListener(callback: IInsetCallbackFunc): void;
```

### 3.3 - getInsets

Returns the last known [IInsets](#21---iinsets) object.

##### Signature

```typescript
getInsets(): IInsets;
```
