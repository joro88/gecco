/**
 * Copyright: github.com/joro88
 * */
package com.geccocrawler.gecco;

import com.geccocrawler.gecco.downloader.AbstractDownloader;
import com.geccocrawler.gecco.downloader.DownloaderAOPFactory;
import com.geccocrawler.gecco.downloader.DownloaderFactory;
import com.geccocrawler.gecco.downloader.MonitorDownloaderFactory;
import com.geccocrawler.gecco.downloader.proxy.FileProxys;
import com.geccocrawler.gecco.downloader.proxy.Proxy;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.dynamic.GeccoJavaReflectionAdapter;
import com.geccocrawler.gecco.listener.EventListener;
import com.geccocrawler.gecco.monitor.DownloadMointorIntercetor;
import com.geccocrawler.gecco.monitor.DownloadMonitor;
import com.geccocrawler.gecco.monitor.DownloadStatistics;
import com.geccocrawler.gecco.monitor.RenderMointorIntercetor;
import com.geccocrawler.gecco.pipeline.DefaultPipelineFactory;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.request.StartRequestList;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.scheduler.NoLoopStartScheduler;
import com.geccocrawler.gecco.scheduler.Scheduler;
import com.geccocrawler.gecco.scheduler.StartScheduler;
import com.geccocrawler.gecco.scheduler.UniqueSpiderScheduler;
import com.geccocrawler.gecco.spider.Spider;
import com.geccocrawler.gecco.spider.SpiderBeanContext;
import com.geccocrawler.gecco.spider.SpiderBeanFactory;
import com.geccocrawler.gecco.spider.SpiderThreadLocal;
import com.geccocrawler.gecco.spider.render.AbstractRender;
import com.geccocrawler.gecco.spider.render.CustomFieldRenderFactory;
import com.geccocrawler.gecco.spider.render.MonitorRenderFactory;
import com.geccocrawler.gecco.spider.render.Render;
import com.geccocrawler.gecco.spider.render.RenderFactory;
import com.geccocrawler.gecco.spider.render.RequestFieldRender;
import com.geccocrawler.gecco.spider.render.RequestParameterFieldRender;
import com.geccocrawler.gecco.spider.render.html.AjaxFieldRender;
import com.geccocrawler.gecco.spider.render.html.HtmlFieldRender;
import com.geccocrawler.gecco.spider.render.html.HtmlParser;
import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.html.ImageFieldRender;
import com.geccocrawler.gecco.spider.render.html.JSVarFieldRender;
import com.geccocrawler.gecco.spider.render.json.JsonFieldRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.reflections.Reflections;

/**
 *
 * @author github.com/Joro88
 */
public class GeccoFactory {
	private static Log log = LogFactory.getLog(GeccoFactory.class);
    
    /**
     * GeccoEngine should be set ASAP. E.g. in constructor of GeccoEngine
     */
    protected GeccoEngine engine;
    protected GeccoContext context;

    public GeccoFactory() {
    }
    
    public GeccoEngine createEngine() {
        return new GeccoEngine(this);
    }

    public void setEngine(GeccoEngine engine) {
        this.engine = engine;
        
    }
    
    public GeccoContext getContext() {
        return context;
    }

    public void setContext(GeccoContext context) {
        this.context = context;
    }

    public PipelineFactory createPipelineFactory(Reflections reflections){
        return new DefaultPipelineFactory(context, reflections);
    } 

    public SpiderBeanFactory createSpiderBeanFactory(List<String> classpaths, final PipelineFactory pipelineFactory) {
        return new SpiderBeanFactory(classpaths, pipelineFactory, context);
    }
    
    public CookieStore createCookieStore( final AbstractDownloader downloader ) {
        return new BasicCookieStore();
    }
    
    public TrustStrategy createTrustStrategy( final AbstractDownloader downloader ) {
        return new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        };
    }
    
    public ConnectionSocketFactory createHttpsConnectionSocketFactory ( 
            final SSLContext sslContext, 
            final AbstractDownloader downloader 
    ) {
        return new SSLConnectionSocketFactory(sslContext);
    }
    
    public ConnectionSocketFactory createHttpsConnectionSocketFactoryFallback ( 
            final AbstractDownloader downloader 
    ) {
        return SSLConnectionSocketFactory.getSocketFactory();
    }
    
    public ConnectionSocketFactory createHttpConnectionSocketFactory ( 
            final AbstractDownloader downloader 
    ) {
        return PlainConnectionSocketFactory.getSocketFactory();
    }
    
    public ConnectionSocketFactory createHttpConnectionSocketFactoryFallback ( 
            final AbstractDownloader downloader 
    ) {
        return PlainConnectionSocketFactory.getSocketFactory();
    }
    
    public RequestConfig createRequestConfig ( final AbstractDownloader downloader ) {
        return RequestConfig.custom().setRedirectsEnabled(false).build();
    }
    
    public HttpClientConnectionManager createConnectionManager(
            final Registry<ConnectionSocketFactory> socketFactoryRegistry,
            final AbstractDownloader downloader
    ){
        PoolingHttpClientConnectionManager syncConnectionManager = 
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		syncConnectionManager.setMaxTotal(1000);
		syncConnectionManager.setDefaultMaxPerRoute(50);
        
        return syncConnectionManager;
    }
    
    public HttpRequestRetryHandler createRetryHandler( final AbstractDownloader downloader ){
        return new HttpRequestRetryHandler() {
					@Override
					public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
						int retryCount = SpiderThreadLocal.get().getEngine().getRetry();
						boolean retry = (executionCount <= retryCount);
						if(log.isDebugEnabled() && retry) {
							log.debug("retry : " + executionCount);
						}
						return retry;
					}
				};
    }
    
    public SSLContext createSSLContext(
            final AbstractDownloader downloader
    ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        KeyStore trustStore = createSSLContextKeyStore(downloader);
        return SSLContexts.custom().loadTrustMaterial(trustStore, createTrustStrategy(downloader)).build();
    }

    public KeyStore createSSLContextKeyStore( final AbstractDownloader downloader ) {
        return null;
    }
    
    public HttpClientBuilder createHttpClientBuilder( final AbstractDownloader downloader ){
        return HttpClientBuilder.create();
    }
    
    public HttpClientContext createCookieHttpClientContext( final AbstractDownloader downloader ){
        return HttpClientContext.create();
    }
    
    public RegistryBuilder<ConnectionSocketFactory> createSocketFactoryRegistryBuilder( final AbstractDownloader downloader ){
        return RegistryBuilder.<ConnectionSocketFactory>create();
    }
    
    public HttpEntity createUrlEncodedFormEntity(
            final List <? extends NameValuePair> fields,
            final AbstractDownloader downloader
    ) throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(fields, "UTF-8");
    }
    
    /**
     * Geccy supports GET and POST requests methods. Extending this method, 
     * more could be supported.
     */
    public HttpRequestBase createApacheRequestObject(
            HttpRequest geccoRequest,
            final AbstractDownloader downloader
    ){
		if(geccoRequest instanceof HttpPostRequest) {//post
			HttpPostRequest post = (HttpPostRequest)geccoRequest;
			return new HttpPost(post.getUrl());
		} else {//get
			return new HttpGet(geccoRequest.getUrl());
		}
    }
    
    /**
     * If you need to inject more data or modify some of fields, 
     * you could do it by creating a Builder with overwritten build() method
     */
    public RequestConfig.Builder createRequestConfigBuilder(
            final HttpRequest geccoRequest,
            final AbstractDownloader downloader
    ){
        return RequestConfig.custom();
    }
    
    public HttpResponse createApacheHttpResponse(
            final HttpRequest geccoRequest,
            final HttpRequestBase apacheRequestObject,
            final AbstractDownloader downloader
    ){
        return new HttpResponse();
    }
    
    public HttpResponse createSimpleHttpResponse(String content) {
        return HttpResponse.createSimple(content);
    }
    
    public Enhancer createEnhancer( 
            Class<?> downloaderClass,
            final MonitorDownloaderFactory monitorDownloaderFactory
    ){
        return new Enhancer();
    }

    public DownloadMointorIntercetor createDownloadMointorIntercetor( 
            Class<?> downloaderClass,
            final MonitorDownloaderFactory monitorDownloaderFactory
    ){
        return new DownloadMointorIntercetor(context);
    }
    
    public Proxy createProxy (
            String host, 
            int port, 
            String src,
            Proxys proxys
    ) {
        return new Proxy(host, port, context);
    }

    public HttpHost createApacheHttpHost( String host, int port, Proxy proxy ) {
        return new HttpHost(host, port);
    }
    
    public DownloadStatistics createDownloadStatistics(
            final String host,
            final DownloadMonitor downloadMonitor
    ) {
        return new DownloadStatistics();
    }
    
    
    public HttpPostRequest createHttpPostRequest ( 
            final StartRequestList requestList 
    ) {
        return new HttpPostRequest( requestList.getUrl() );
    }
    
    public HttpGetRequest createHttpGetRequest ( String url ) {
        return new HttpGetRequest( url );
    }
    
    public Proxys createProxys() {
        FileProxys result = new FileProxys();
        result.setContext(context);
        result.load();
        return result;
    }
    
    public Scheduler createScheduler() {
        if( engine.isLoop()) {
            return new StartScheduler();
        } else {
            return new NoLoopStartScheduler();
        }
    }
    
    public Spider createSpider() {
        return new Spider(engine);
    }

    public CustomFieldRenderFactory createCustomFieldRenderFactory( 
            final Reflections reflections, 
            final RenderFactory rf 
    ){
        return new CustomFieldRenderFactory(reflections);
    }
    
    public RenderMointorIntercetor createRenderMointorIntercetor( final MonitorRenderFactory mrf ) {
        return new RenderMointorIntercetor(context);
    }

    public RequestFieldRender createRequestFieldRender( final AbstractRender renderer ) {
        return new RequestFieldRender();
    }
    
    public RequestParameterFieldRender createRequestParameterFieldRender( final AbstractRender renderer ) {
        return new RequestParameterFieldRender();
    }
    
    public JsonFieldRender createJsonFieldRender( final Render render ) {
        return new JsonFieldRender(context);
    }
    
    public HtmlRender createHtmlRender(final RenderFactory rf){
        return new HtmlRender( context );  // ATTENTION: relation with getHtmlRenderClass()
    }
    
    public Class<? extends HtmlRender> getHtmlRenderClass(final RenderFactory rf){
        return HtmlRender.class; // ATTENTION: relation with createHtmlRender()
    }
    
    public JsonRender createJsonRender(final RenderFactory rf) {
		return new JsonRender( context );
    }
    
    public Class<? extends JsonRender> getJsonRenderClass(final RenderFactory rf) {
        return JsonRender.class;
    }

    public AjaxFieldRender createAjaxFieldRender(final Render r) {
        return new AjaxFieldRender( context );
    }

    public HtmlFieldRender createHtmlFieldRender(final Render r) {
        return new HtmlFieldRender( context );
    }

    public JSVarFieldRender createJSVarFieldRender( final Render r ) {
        return new JSVarFieldRender( context );
    }

    public ImageFieldRender createImageFieldRender( final Render r ) {
        return new ImageFieldRender( context );
    }
    
    public Scheduler createUniqueSpiderScheduler(Spider s) {
        return new UniqueSpiderScheduler();
    }
    
    public GeccoJavaReflectionAdapter createGeccoJavaReflectionAdapter(final SpiderBeanFactory sbf) {
        return new GeccoJavaReflectionAdapter();
    }
    
    public MonitorDownloaderFactory createMonitorDownloaderFactory(final SpiderBeanFactory sbf) {
        return new MonitorDownloaderFactory(sbf.getReflections(), context);
    }
            
    public DownloaderAOPFactory createDownloaderAOPFactory(final SpiderBeanFactory sbf) {
        return new DownloaderAOPFactory(sbf.getReflections());
    } 

    public MonitorRenderFactory createMonitorRenderFactory(final SpiderBeanFactory sbf) {
        return new MonitorRenderFactory(sbf.getReflections(), context);
    }
    
    public DefaultPipelineFactory createDefaultPipelineFactory(final SpiderBeanFactory sbf) {
        return new DefaultPipelineFactory(context, sbf.getReflections());
    }
    
    public SpiderBeanContext createSpiderBeanContext( final SpiderBeanFactory sbf ) {
        return new SpiderBeanContext();
    }
    
    public HtmlParser createHtmlParser( String baseUri, String content, Object caller, Field field ) {
        return new HtmlParser(baseUri, content, context, field);
    }
    
    
    /** Explicit factory method is not possible because Gecco engine does not create 
     * the object directly but passes class object to Alibaba FastJson that creates 
     * the object */
    public Class<? extends StartRequestList> getStartRequestListClass() {
        return StartRequestList.class;
    }
    
    public String getDefaultDownloaderAnnotationValue() {
        return DownloaderFactory.DEFAULT_DWONLODER;
    }
    
    public GeccoContext createContext() {
        return new GeccoContext(engine);
    }

    public EventListener createEventListener() {
        return null;
    }

}
