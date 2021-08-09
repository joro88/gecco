package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.GeccoFactory;
import net.sf.cglib.proxy.Enhancer;

import org.reflections.Reflections;

import com.geccocrawler.gecco.monitor.DownloadMointorIntercetor;

/**
 * 带监控的下载器工厂类
 * 
 * @author huchengyi
 *
 */
public class MonitorDownloaderFactory extends DownloaderFactory {
	public MonitorDownloaderFactory(Reflections reflections, GeccoFactory geccoFactory) {
		super(reflections, geccoFactory);
	}

	@Override
	protected Object createDownloader(Class<?> downloaderClass)	throws Exception {
		Enhancer enhancer = geccoFactory.createEnhancer(downloaderClass, this);
		enhancer.setSuperclass(downloaderClass);
		enhancer.setCallback(geccoFactory.createDownloadMointorIntercetor(downloaderClass, this));
		Object o = enhancer.create(new Class[]{GeccoFactory.class}, new Object[] {geccoFactory});
		return o;
	}

}
