package com.geccocrawler.gecco.monitor;

import com.geccocrawler.gecco.GeccoFactory;
import com.geccocrawler.gecco.GeccoContext;
import java.lang.reflect.Method;

import com.geccocrawler.gecco.downloader.DownloadException;
import com.geccocrawler.gecco.downloader.DownloadServerException;
import com.geccocrawler.gecco.request.HttpRequest;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class DownloadMointorIntercetor implements MethodInterceptor {

    protected GeccoContext context;
    protected GeccoFactory factory;

    public DownloadMointorIntercetor(GeccoContext context) {
        this.context = context;
        this.factory = context.getFactory();
    }
    
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if(method.getName().equals("download")) {
			HttpRequest request = (HttpRequest)args[0];
			try {
				Object o = proxy.invokeSuper(obj, args);
				DownloadMonitor.incrSuccess(request.getUrl(), factory);
				return o;
			} catch(DownloadServerException ex) {
				DownloadMonitor.incrServerError(request.getUrl(), factory);
				throw ex;
			} catch(DownloadException ex) {
				DownloadMonitor.incrException(request.getUrl(), factory);
				throw ex;
			}
			
		} else {
			return proxy.invokeSuper(obj, args);
		}
	}
}
