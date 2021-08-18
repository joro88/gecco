package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.GeccoMediator;
import org.reflections.Reflections;

/**
 * 下载器工厂类
 * 
 * @author huchengyi
 *
 */
public class DefaultDownloaderFactory extends DownloaderFactory {

	public DefaultDownloaderFactory(Reflections reflections, GeccoMediator mediator) {
		super(reflections, mediator);
	}

	protected Object createDownloader(Class<?> downloaderClass) throws Exception {
		return downloaderClass.newInstance();
	}
}
