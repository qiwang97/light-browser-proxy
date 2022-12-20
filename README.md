# LightBrowserProxyServer

An alternative to browsermob-proxy but process entry with stream.

LightBrowserProxyServer keeps what BrowserMobProxyServer does and its config except har file.

This project modify HarCaptureFilter to support concurrency work and reduce memory usage.

See [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) for base use and config.

See [selenium-har-with-lbp](https://github.com/qiwang97/selenium-har-with-lbp) to find lbp example.

## Install

```xml
<dependency>
    <groupId>io.github.qiwang97</groupId>
    <artifactId>light-browser-proxy</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Using With Selenium

```java
    // start the proxy
    LightBrowserProxyServer proxy = new LightBrowserProxyServer();
    proxy.start(0);

    // get the Selenium proxy object
    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

    // configure it as a desired capability
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

    // start the browser up
    WebDriver driver = new FirefoxDriver(capabilities);

    // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
    proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

    // subscribe harEntry stream
    // NOTE: subscribe use PublishSubject so that you may loss response before subscribe.

	proxy.$entries.subscribe(
		entry -> {
			// receive entry
		},
		error -> {
			// receive error
		},
		() -> {
			// proxy stopped.
		}
	);

    // create a new HAR with the label "github.com"
    // call newHar to add harEntry hook
    proxy.newHar("github.com");

    // open google.com
    driver.get("https://www.github.com");

    // stop job
    proxy.stop();
```
