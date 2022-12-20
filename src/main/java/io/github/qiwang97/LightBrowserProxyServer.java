package io.github.qiwang97;

import io.github.qiwang97.filters.HarStreamCaptureFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.reactivex.rxjava3.subjects.PublishSubject;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class LightBrowserProxyServer extends BrowserMobProxyServer {
	private static final Logger log = LoggerFactory.getLogger(LightBrowserProxyServer.class);
	public final PublishSubject<HarEntry> $entries = PublishSubject.create();
	private final AtomicBoolean harCaptureFilterEnabled = new AtomicBoolean(false);

	/**
	 * override har filter to support concurrency work
	 */
	@Override
	protected void addHarCaptureFilter() {

		if (harCaptureFilterEnabled.compareAndSet(false, true)) {
			// the HAR capture filter is (relatively) expensive, so only enable it when a HAR is being captured. furthermore,
			// restricting the HAR capture filter to requests where the HAR exists, as well as  excluding HTTP CONNECTs
			// from the HAR capture filter, greatly simplifies the filter code.
			addHttpFilterFactory(new HttpFiltersSourceAdapter() {
				@Override
				public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
					Har har = getHar();
					if (har != null && !ProxyUtils.isCONNECT(originalRequest)) {
						return new HarStreamCaptureFilter(originalRequest, ctx, har, getCurrentHarPage() == null ? null : getCurrentHarPage().getId(), getHarCaptureTypes(), LightBrowserProxyServer.this);
					} else {
						return null;
					}
				}
			});
		}
	}

	@Override
	protected void stop(boolean graceful) {
		if (isStopped() || $entries.hasComplete()) {
			log.warn("Proxy server is already stopped");
		} else if ($entries.hasComplete()) {
			log.warn("Proxy server har stream is already completed");
		} else {
			try {
				$entries.onComplete();
			} catch (Exception e) {
				log.warn("Proxy server complete harEntry failed", e);
			}
		}

		super.stop(graceful);
	}
}
