package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.GeccoContext;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.spider.SpiderBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;

/**
 * 下载器工厂类
 * 
 * @author huchengyi
 *
 */
public class DefaultDownloaderFactory extends DownloaderFactory {
    private static final Log log = LogFactory.getLog(DefaultDownloaderFactory.class);

	public DefaultDownloaderFactory(Reflections reflections, GeccoContext context) {
		super(reflections, context);
	}

	protected Object createDownloader(Class<?> downloaderClass) throws Exception {
        Downloader instance;
        try {
            instance = (Downloader) downloaderClass.getDeclaredConstructor(GeccoContext.class).newInstance(context);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Constructor with one argument of type GeccoContext is needed.", e);
        }
        return instance;
    }
}
