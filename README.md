# Cordova Plugin Wowza Gocoder

# Android Build
[Wowza Documentaion for Android](https://www.wowza.com/docs/how-to-install-gocoder-sdk-for-android)
```
Requirements:
Android SDK 4.4.2 or later

1. Copy the file com.wowza.gocoder.sdk.aar to your libs folder

2. Update your build.gradle

dependencies {
	compile 'com.wowza.gocoder.sdk.android:com.wowza.gocoder.sdk:1.0b7@aar'
}

allprojects {
    repositories {
		flatDir {
			dirs 'libs'
		}
	}
}

3. Update you AndroidManifest.xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

# iOS Build
```
```

# License
Cordova Plugin Wowza Gocoder is licensed under the Apache License (ASL) license. For more information, see the LICENSE file in this repository.