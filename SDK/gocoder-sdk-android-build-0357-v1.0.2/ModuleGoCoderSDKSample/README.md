
###GoCoder SDK Data Event Integration

This folder contains sample code demonstrating integration between Wowza Streaming Engine and the Wowza GoCoder SDK for iOS and Android.

For detailed information regarding extending Wowza Streaming Engine with custom server modules please visit the link below:
https://www.wowza.com/forums/content.php?754-How-to-extend-Wowza-Streaming-Engine-using-Java

For this particular example, you will need to add the following to the `<Modules>` section of `Application.xml` file in the targeted application's `conf` folder (e.g. `/Library/WowzaStreamingEngine/conf/live/Application.xml`):

```
<Module>
	<Name>ModuleGoCoderSDKSample</Name>
	<Description>ModuleGoCoderSDKSample</Description>
	<Class>com.wowza.gocoder.sdk.module.sample.ModuleGoCoderSDKSample</Class>
</Module>
```
You must restart Wowza Streaming Engine for this change to take affect.

The provided `build.xml` file can be used to automatically build and deploy the server module with Apache `ant` (http://ant.apache.org/).
