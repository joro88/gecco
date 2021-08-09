package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.GeccoFactory;
import org.reflections.Reflections;

/**
 * 下载器工厂类
 * 
 * @author huchengyi
 *
 */
public class DefaultDownloaderFactory extends DownloaderFactory {

	public DefaultDownloaderFactory(Reflections reflections, GeccoFactory factory) {
		super(reflections, factory);
	}

	protected Object createDownloader(Class<?> downloaderClass) throws Exception {
		return downloaderClass.newInstance();
	}
}
