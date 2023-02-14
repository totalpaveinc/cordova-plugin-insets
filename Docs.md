Cordova SQLite Plugin Documentation
===================================

This document describes the public API available to library consumers.

This plugin does not aim to be a direct API to SQLite, instead it mimicks the Breautek's [Storm](https://github.com/breautek/storm) database API.
# Table of Contents
- [1.0 - General Notes](#10---general-notes)
- [1.1 - Plugin Namespace](#11---plugin-namespace)
- [1.2 - Undefined Behaviour](#12---undefined-behaviour)
- [2.0 - SQLite](#20---sqlite)
  - [2.1 - Thread Safety](#21---thread-safety)
  - [2.2 - open](#22---open)
  - [2.3 - close](#23---close)
- [3.0 - Database](#30---database)
  - [3.1 - getHandle](#31---gethandle)
  - [3.2 - isClosed](#32---isclosed)
- [4.0 - SQLite Types and Converting](#40---sqlite-types-and-converting)
  - [4.1 - SQLiteParams](#41---sqliteparams)
  - [4.2 - SQLiteType](#42---sqlitetype)
  - [4.3 - SQLiteParamValueConverter](#43---sqliteparamvalueconverter)
- [5.0 - Query](#50---query)
  - [5.1 - Constructor](#51---constructor)
  - [5.2 - getQuery](#52---getquery)
  - [5.3 - _getParameters](#52---getquery)
  - [5.4 - execute](#54---execute)
  - [5.5 - Note on Data Types and Return Types](#55---note-on-data-types-and-return-types)
  - [5.6 - _getNativeMethod](#56---_getnativemethod)
  - [5.7 - _validateParameterNames](#57---_validateparameternames)
- [6.0 - RawQuery](#60---rawquery)
  - [6.1 - constructor](#61---constructor)
- [7.0 - StartTransactionQuery](#70---starttransactionquery)
  - [7.1 - constructor](#71---constructor)
- [8.0 - CommitTransactionQuery](#80---committransactionquery)
  - [8.1 - constructor](#81---constructor)
- [9.0 - RollbackTransactionQuery](#90---rollbacktransactionquery)
  - [9.1 - constructor](#91---constructor)
- [10.0 - BulkInsertQuery](#100---bulkinsertquery)
  - [10.1 - _getTable](#101---_gettable)
  - [10.2 - _getColumns](#102---_getcolumns)
  - [10.3 - _getOnConflict](#103---_getonconflict)

  

## 1.0 - General Notes
### 1.1 - Plugin Namespace

This plugin is namespaced under `window.totalpave.sqlite` and for brevity this will be omitted throughout the remainder of this document. For example, the `SQLite` package is located at `window.totalpave.sqlite.SQLite` but will be only referenced as `SQLite`.

## 1.2 - Undefined Behaviour

A note on undefined behaviour for those who are unfamiliar with the term. [Undefined behaviour](https://en.cppreference.com/w/cpp/language/ub) is a term often used in C++ that signals a procedure that is illogical, or may produce machine code that may work or may not work, and the actual result of the code is not determined by looking at the source code. That is, the program may work, it may produce a no-operation, it may corrupt data, it may fatally crash. The behaviour is undefined and anything _could_ happen, therefore anything that is known to create undefined behaviour should be avoided.

## 2.0 - SQLite

A static class for opening and closing databases.

### 2.1 - Thread Safety

A note on thread safety. Generally speaking, SQLite is not thread-safe. It is dangerous to have 2 independent write connections to the same database. This plugin makes no effort to ensure thread safety, but the underlying SQLite library is built with some level of threading support. Care has been taken to ensure that, at least witin totalpave provided packages, that only a single copy of the SQLite and libc++ libraries will be used to avoid database corruption issues.

Generally speaking, SQLite is used on local clients and it's presumed that the application will only have a single connection to the database.

### 2.2 - open

Opens a new connection the database at __path__.

By default, the connection will be readonly and will error if the database does not already exists. If __writeAccess__ is `true`, then the database will be opened with write mode enabled, and if the database file does not exists, it will be created.

busyTimeout is passed into [sqlite3_busy_timeout](https://www.sqlite.org/c3ref/busy_timeout.html). The value is time in milliseconds and it defaults to 10 seconds.

The returned value is a [Database](#30---database). It represents the underlying database handle. Keep a reference to this value for preparing SQL statements and for closing the database later.

##### Signature

```typescript
static async open(path: string, writeAccess: boolean, busyTimeout: SQLiteInteger = 10000): Promise<number>;
```

### 2.3 - close

Closes an opened database. Use this to free resources allocated in the native environment. Using a closed database will result in undefined behaviour.

##### Signature

```typescript
static async close(db: Database): Promise<void>;
```

## 3.0 - Database

A class that represents a database handle. By itself, it's not very useful, but a reference is required for executing SQL statements and closing the database when no longer in use.

### 3.1 - getHandle

Returns a number that represents the memory address of the underlying database handle.

##### Signature

```typescript
getHandle(): number;
```

### 3.2 - isClosed

Returns true if the database is closed. When a database is closed, it is unsafe to use it for SQL queries. Executing SQL queries on a closed database is undefined behaviour.

##### Signature

```typescript
isClosed(): boolean;
```

## 4.0 - SQLite Types and Converting

### 4.1 - SQLiteParams
`SQLiteParams` is the query parameter interface that is officially supported. 

Note while this is the offical support, that does not mean your Query parameters must adhere to this interface. Using [Query._getParameters](#53---_getparameters) and [SQLiteParamsValueConverter](#43---sqliteparamvalueconverter) your parameters can be converted from your own types to `SQLiteParams`.

#### Signature
```typescript
interface SQLiteParams {
    [key: string]: SQLiteType;
}
```

### 4.2 - SQLiteType
`SQLiteType` is a union of types supported by SQLite.

#### Signature
```typescript
type SQLiteType = SQLiteText | SQLiteInteger | SQLiteDouble | SQLiteBlob | SQLiteNull;
```

The `SQLite*` types are aliases to the following types.

|Type|Alias|
|---|---|
|`SQLiteText`|`string`|
|`SQLiteDouble`|`number`|
|`SQLiteInteger`|`number`|
|`SQLiteBlob`|`IByteArray`|
|`SQLiteNull`|`null`|

The `SQLiteDouble` and `SQLiteInteger` are aliases together as JavaScript only supports a single numerical type, `number`. The typing is for expression of intent by the developer, based on database schema.

`IByteArray` is an internal data structure and may change without making a major release. Use [SQLiteParamValueConverter](#43---sqliteparamvalueconverter) APIs to create `SQliteBlobs`.

### 4.3 - SQLiteParamValueConverter

The `SQLiteParamValueConverter` is a class with static APIs to convert javascript types into `SQLite*` types.

`SQLiteParamValueConverter` is the only correct way to create an `IByteArray`.

##### Signature

```typescript
static numberToInteger(value: number): SQLiteInteger;
static numberToDouble(value: number): SQLiteDouble;
static booleanToInteger(value: boolean): SQLiteInteger;
static nullOrUndefinedToSQLiteNull(value: number): SQLiteNull;
static dateToText(value: Date): SQLiteText;
static stringToText(value: string): SQLiteText;
static async blobToSQLiteBlob(value: Blob): Promise<SQLiteBlob>;
static async arrayBufferToSQLiteBlob(value: ArrayBuffer): Promise<SQLiteBlob>;
static async int8OrUint8ToSQLiteBlob(value: Uint8Array | Int8Array): Promise<SQLiteBlob>;
```

## 5.0 - Query

Query is an `abstract class` that represents an SQL statement. It is not usable on it's own and is intended to be subclassed. The design rationale behind this is it allows you to extract SQL strings out of the application and for it to be shared across the application, libraries, or even unit tests.

The `Query` class has three generic types: `TParams`, `TResponse`, `TSQLiteParams`.

`TSQLiteParams` is internal code. Just ignore it.

`TParams` should be an interface that describes the concrete query's parameters as a JSON object. `TParams` can be declared void if your query has no params.

`TParams` should __not__ extend `SQLiteParams`. `SQLiteParams` accepts any key and does not require keys to be defined. `TParams` is supposed to define specific keys that your query actually uses.

Named parameters can be provided in the SQL query which are populated by the `TParams` properties. Named parameters are binded using SQLite's binding APIs and therefore are safely escaped.

The parameter names can only contain alphanumeric and underscore characters. The first must be an alphebetical letter.

Note that the `TResponse` generic type is provided to assert developer intent. It does not guarentee that the underlying returned data is actually the declared type. However, if there is a mismatch, it is likely either an error in the typing or in the actual SQL query statement.

Note `Query` does not support multi-insert. See [BulkInsertQuery](#100---bulkinsertquery) instead.

### 5.1 - Constructor

Constructs a new instance of `Query` with the given `TParams`.

##### Signature

```typescript
constructor(params: TParams);
```

### 5.2 - getQuery

Returns the SQL query as a string. It may have named variables, denoted by a keyword prefixed with the colon (`:`) character.

getQuery is abstract and must be implemented by extending classes.

Example:

```sql
INSERT INTO person (firstName, lastName)
VALUES (:firstName, :lastName)
```

##### Signature

```typescript
abstract getQuery(): string;
```

### 5.3 - _getParameters

A hook to do work on parameters just before they are sent to native.

__The return value of \_getParameters is the actual data sent to native. All queries that use parameters must override and implement this hook.__

The default behaviour `_getParameters` is to act as if there is no parameters. 

##### Signature

```typescript
async _getParameters(params: TParams): Promise<SQLiteParams>;
```

The primary intention for this hook is to convert the query's `TParams` to [SQLiteParams](#41---sqliteparams). Use [SQLiteParamValueConverter](#43---sqliteparamvalueconverter) to accomplish this.

##### Examples

```typescript
// If `TParams` adheres to `SQLiteParams` you can do this
protected async _getParameters(params: TParams): Promise<SQLiteParams> {
  return <SQLiteParams><unknown>params;
}

// You can also spread params instead of type asserting however; remember spreading is done during runtime. Typescript type asserting is done during compile time.
protected async _getParameters(params: TParams): Promise<SQLiteParams> {
  return {...params};
}

// If `TParams` does not adhere to `SQLite` then use SQLiteParamValueConverter.
interface IMyQueryParams {
  data: Blob;
}

class MyQuery extends Query<IMyQueryParams, void> {
  protected async _getParameters(params: IMyQueryParams): Promise<SQLiteParams> {
    return {
      data: SQLiteParamValueConverter.blobToSQLiteBlob(params.data)
    };
  }
}
```

### 5.4 - execute

Executes the `Query` onto the given [Database](#30---database). The database must not be closed or undefined behaviour will occur. Query parameters are passed through.

Each `execute` statement internally prepares a new SQLite statement context and act independent of a previous `execute` call. Therefore, the same `Query` instance can be execute multiple times, which can be useful for select statements for refreshing data, for example.

The return type is the generic type `TResponse`. The return type is not known at the abstract level, but is generally `void` for any query that is not a `SELECT` query.

For `SELECT` queries, the return type is generally an array of json objects, whose properties are keyed by the select query columns.

##### Signature

```typescript
async execute(db: Database): Promise<TResponse>;
```

### 5.5 - Note on Data Types and Return Types

SQLite data types are quite primitive, and supports 4 different data types:
 - integers (int, size are dynamic depending on the magnitude of the value, could be 0, 1, 2, 3, 4, 5, or 8 bytes)
 - real (8-byte IEEE floating point number, e.g. `double` type)
 - Text (Strings in UTF-8)
 - Blob (Binary data untouched)

Query parameters may accept additional types, which may not translate back to their original type on a select query. For example, a `boolean` type will be converted to an `integer` value of `0` or `1`, and is expected to be stored in an `integer` typed column. Selecting the column will simply return the data as an `integer`.

The [SQLiteParamValueConverter](#43---sqliteparamvalueconverter) API accepts several ways of providing blobs, including using `Blob`, `ArrayBuffer`, or `Uint8` and `Int8` array types. These types gets converted to a special format to signal the native side that the data represents a `Blob`. Selecting a blob however will yield a standard JS array of numbers. It's up to the client to take the array and reconstruct the blob as they see fit, for example:

```typescript
let blob: Blob = new Blob([
    new Uint8Array(result.blob)
]);
```

This isn't done automatically to avoid iterating over the resultset, when the application is likely to do it anyway.

### 5.6 - _getNativeMethod

`_getNativeMethod` is internal code. Just ignore it.

### 5.7 - _validateParameterNames

`_validateParameterNames` is internal code. Just ignore it.

## 6.0 - RawQuery

A prebuilt [Query](#50---query) that accepts both the SQL string and the associated [SQLiteParams](#41---sqliteparams).

It can be used to perform a SQL query without building out a class that extends the `Query` class. Useful for quickly debugging things or for unit tests.

It's not recommended to use this class in production and instead to extend and use your own `Query` implementations, where you can declare typings as well as build unit tests around.

The `RawQuery` exposes the `Query` generics, but by default it is permissive and can be used without declaring types.

### 6.1 - constructor

The consructor consists of the standard `SQLiteParams` as well as the SQL statement.

The params object may be omitted if the SQL statement contains no named parameters.

##### Signature

```typescript
constructor(sql: string, params?: TParams);
```

## 7.0 - StartTransactionQuery

Prebuilt query to start a transaction on the database. Refer to the [SQLite Docs](https://www.sqlite.org/lang_transaction.html) for more information on transactions.

`StartTransactionQuery` accepts a `TransactionMode` enumeration, that directly ties to `DEFERRED`, `IMMEDIATE`, or `EXCLUSIVE` transactions.

Like the SQLite default, `DEFERRED` is chosen by default if not provided.

This query can be executed multiple times, but SQLite does not have a concept of nested transactions. Therefore, do not start a transaction if there is already an active transaction.

### 7.1 - constructor

The constructor optionally accepts 1 parameter, `TransactionMode`. it defaults to `TransactionMode.DEFERRED`.

##### Signature

```typescript
constructor(mode: TransactionMode = TransactionMode.DEFERRED);
```

## 8.0 - CommitTransactionQuery

A prebuilt query to commit an active transaction on the database. Refer to the [SQLite Docs](https://www.sqlite.org/lang_transaction.html) for more information on transactions.

It is an error to commit a transaction on a database without an active transaction.

### 8.1 - constructor

The constructor accepts no arguments.

##### Signature

```typescript
constructor();
```

## 9.0 - RollbackTransactionQuery

A prebuilt query to rollback an active transaction on the database. Refer to the [SQLite Docs](https://www.sqlite.org/lang_transaction.html) for more information on transactions.

It is an error to rollback a transaction on a database without an active transaction.

### 9.1 - constructor

The constructor accepts no arguments.

##### Signature

```typescript
constructor();
```

## 10.0 - BulkInsertQuery

An abstract query class that is designed for bulking inserting.

An example of bulk-inserting:

```sql
INSERT INTO table
COLUMNS (col1, col2)
VALUES
  (val1col1, val1col2),
  (val2col1, val2col2)
```

`BulkInsertQuery` has 1 generic: `TParams extends TSQLiteParams`.
`TSQLiteParams` is a type alias to `Array<Array<SQLiteType>>`;

See [SQLiteType](#42---sqlitetype) for more information on `SQLiteType`.

As indictated by the extension of `TSQLiteParams`, `BulkInsertQuery` does not recommend parameter conversion via `_getParameters`. 
The parameters is already a 2-dimension array and is designed for big query usage. 

Since `BulkInsertQuery` requires a 2-dimension array of `SQLiteType`, chances are you are already looping over a potentially massive dataset to construct this array. Using `_getParameters` would have you looping over that 2-dimension array twice.

For that reason, we recommend using [SQLiteParamsValueConverter](#43---sqliteparamvalueconverter) when initially constructing the 2-dimension array of parameters. 

While `BulkInsertQuery` is a `Query`, you should not override `getQuery`. `BulkInsertQuery` manages the overall structure of a bulk insert query and instead provides other abstract functions for you to override. See [_getTable](#101---_gettable), [_getColumns](#102---_getcolumns), [_getOnConflict](#103---_getonconflict) for more information.

### 10.1 - _getTable
`_getTable` defines the table you are inserting into to. This value is not sanitized.

#### Signature
```typescript
protected abstract _getTable(): string;
```

### 10.2 - _getColumns
`_getColumns` defines an array of strings, where each value is a column. These values are not sanitized.

#### Signature
```typescript
protected abstract _getColumns(): Array<string>;
```

### 10.3 - _getOnConflict
`_getOnConflict` allows you to specify the `ON CONFLICT` clause for things like upsert support.

You are required to define the entire clause, for example:

```typescript
protected _getOnConflict(): string {
  return `
    ON CONFLICT (id) DO UPDATE SET
      col1 = excluded.col1
  `;
}
```

#### Signature
```typescript
protected _getOnConflict(): string;
```