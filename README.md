# Calenope

A calendar project written in Kotlin with a sample Android application showing the calendar events from available Google accounts.

### Module: core

Defines a few simple interfaces and models that could be implemented by any calendar provider.
A `calendar` shall then be obtained from a `board` and provides in turn a way to query `events` and their corresponding `attendees`.
There further exists a SPI package to obtain a concrete `board` implementation.

```ServiceLoader.load (BoardProvider::class.java).map { it.create () }.first!!```

While it would be sufficient to use the standard service locator pattern (`META-INF/services`), it might be necessary to provide custom
implementations for other frameworks or runtime environments - OSGI, ...

### Module: core.google

Provides an implementation of the core API for Google's `calendar` and `resource` SDK that is agnostic of the authentication process. 

### Module: core.google.android

Provides a `board` implementation that can be used on Android devices using the mentioned service loader mechanism.

Configuration:
```
meta : Map<String, Any>
meta["context"] : android.content.Context  /* e.g. application */
meta["account"] : android.accounts.Account /* account that should be used*/
```

Android offers `GoogleAccountManager (context).getAccountByName ('name@company.com')` among others to obtain a corresponding account instance.

### Module: core.google.standalone

Provides a `board` implementation that can be used on server environment using the mentioned service loader mechanism.

Configuration:
```
meta : Map<String, Any>
meta["secret"] : java.io.Reader /* content should provide the api json secrets */
```

There exists a sample application under `src/test/kotlin` that can be used as a starter, which requires a file pointing
to a valid `OAuth-2.0 Client ID` json, linked with your related developer Google account.

### Module: organizer

A sample Android application written in Kotlin on top of a Redux architecture.
You can download and install the latest debug APK from the release page, which has been build with Travis CI.

`adb install organizer-debug.apk`

Structure

* `component` contains widgets and other ui components
* `middleware` contains business logic as well as some generic interceptors for common tasks
* `ui` contains view layouts for and activities
* `root` contains glue and action types

### Authentication/Security

Standalone setup

* create a new application with your developer account
* activate `Google Calendar API` and `Admin SDK`
* create a new `OAuth-2.0-Client-ID` and select `other` - which doesn't require any further configuration
* download the secret to a secure place
* start the sample with the file path as first argument

Android setup

* create a `google-service.json` file an put at the root directory of the organizer module to build an APK with gradle.
You can find a [recipe] (https://developers.google.com/identity/sign-in/android/start) describing the relevant steps.
Please make sure that your package refers to `de.synyx.calenope.organizer`.

Please consider all given recipes as a guideline only and choose your settings wisely with respect to your standards and environments.

_Exposing those `secrets` may cause security issues when publicly available or delivered without proper security mechanisms._
