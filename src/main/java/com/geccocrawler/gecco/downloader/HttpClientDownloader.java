package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.GeccoFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.CharArrayBuffer;

import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.downloader.proxy.ProxysContext;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderThreadLocal;
import com.geccocrawler.gecco.utils.UrlUtils;
import org.apache.http.conn.HttpClientConnectionManager;

/**
 * 利用httpclient下载
 *  
 * @author huchengyi
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("httpClientDownloader")
public class HttpClientDownloader extends AbstractDownloader {
	
	private static Log log = LogFactory.getLog(HttpClientDownloader.class);
	
	protected CloseableHttpClient httpClient;
	
	protected HttpClientContext cookieContext;
	
	public HttpClientDownloader(GeccoFactory factory) {
        super( factory );
                
        this.factory = factory;
		
		cookieContext = factory.createCookieHttpClientContext( this );
		cookieContext.setCookieStore( factory.createCookieStore(this) );
		
		Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
		try {
			//构造一个信任所有ssl证书的httpclient
			SSLContext sslContext = factory.createSSLContext(this);
			ConnectionSocketFactory sslsf = factory.createHttpsConnectionSocketFactory(sslContext, this);
			socketFactoryRegistry = factory.createSocketFactoryRegistryBuilder(this)
			           .register("http", factory.createHttpConnectionSocketFactory(this) )  
			           .register("https", sslsf)  
			           .build();
		} catch(Exception ex) {
			socketFactoryRegistry = factory.createSocketFactoryRegistryBuilder(this)
            .register("http", factory.createHttpConnectionSocketFactoryFallback(this) )
            .register("https", factory.createHttpsConnectionSocketFactoryFallback( this ))
            .build();
		}
		RequestConfig clientConfig = factory.createRequestConfig( this );
        HttpClientConnectionManager syncConnectionManager = 
                factory.createConnectionManager(socketFactoryRegistry, this);
        
		httpClient = factory.createHttpClientBuilder(this)
				.setDefaultRequestConfig(clientConfig)
				.setConnectionManager(syncConnectionManager)
				.setRetryHandler(factory.createRetryHandler(this))
                .build();
	}

	@Override
	public HttpResponse download(HttpRequest request, int timeout) throws DownloadException {
		if(log.isDebugEnabled()) {
			log.debug("downloading..." + request.getUrl());
		}
		HttpRequestBase reqObj = factory.createApacheRequestObject(request, this);
		if(request instanceof HttpPostRequest) {//post
			HttpPostRequest post = (HttpPostRequest)request;
			//post fields
			List<NameValuePair> fields = new ArrayList<NameValuePair>();
			for(Map.Entry<String, String> entry : post.getFields().entrySet()) {
				NameValuePair nvp = new BasicNameValuePair(entry.getKey(), entry.getValue());
				fields.add(nvp);
			}
			try {
				HttpEntity entity = factory.createUrlEncodedFormEntity(fields, this);
				((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {//get
			// reqObj = new HttpGet(request.getUrl()); -> object created by factory
		}
		//header
		boolean isMobile = SpiderThreadLocal.get().getEngine().isMobile();
		reqObj.addHeader("User-Agent", UserAgent.getUserAgent(isMobile));
		for(Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
			reqObj.setHeader(entry.getKey(), entry.getValue());
		}
		//request config
		RequestConfig.Builder builder = factory.createRequestConfigBuilder(request, this)
		.setConnectionRequestTimeout(1000)//从连接池获取连接的超时时间
		.setSocketTimeout(timeout)//获取内容的超时时间
		.setConnectTimeout(timeout)//建立socket连接的超时时间
		.setRedirectsEnabled(false);
		//proxy
		HttpHost proxy = null;
		Proxys proxys = ProxysContext.get();
		boolean isProxy = ProxysContext.isEnableProxy();
		if(proxys != null && isProxy) {
			proxy = proxys.getProxy();
			if(proxy != null) {
				log.debug("proxy:" + proxy.getHostName()+":"+proxy.getPort());
				builder.setProxy(proxy);
				builder.setConnectTimeout(1000);//如果走代理，连接超时时间固定为1s
			}
		}
		reqObj.setConfig(builder.build());
		//request and response
		try {
			for(Map.Entry<String, String> entry : request.getCookies().entrySet()) {
				BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
				cookie.setPath("/");
				cookie.setDomain(reqObj.getURI().getHost());
				cookieContext.getCookieStore().addCookie(cookie);
			}
			org.apache.http.HttpResponse response = httpClient.execute(reqObj, cookieContext);
			int status = response.getStatusLine().getStatusCode();
			HttpResponse resp = factory.createApacheHttpResponse(request, reqObj, this);
			resp.setStatus(status);
			if(status == 302 || status == 301) {
				String redirectUrl = response.getFirstHeader("Location").getValue();
				resp.setContent(UrlUtils.relative2Absolute(request.getUrl(), redirectUrl));
			} else if(status == 200) {
				HttpEntity responseEntity = response.getEntity();
				ByteArrayInputStream raw = toByteInputStream(responseEntity.getContent());
				resp.setRaw(raw);
				String contentType = null;
				Header contentTypeHeader = responseEntity.getContentType();
				if(contentTypeHeader != null) {
					contentType = contentTypeHeader.getValue();
				}
				resp.setContentType(contentType);
				if(!isImage(contentType)) { 
					String charset = request.isForceUseCharset() ? request.getCharset():getCharset(request.getCharset(), contentType);
					resp.setCharset(charset);
					//String content = EntityUtils.toString(responseEntity, charset);
					String content = getContent(raw, responseEntity.getContentLength(), charset);
					resp.setContent(content);
				}
			} else {
				//404，500等
				if(proxy != null) {
					proxys.failure(proxy.getHostName(), proxy.getPort());
				}
				throw new DownloadServerException("" + status);
			}
			if(proxy != null) {
				proxys.success(proxy.getHostName(), proxy.getPort());
			}
			return resp;
		} catch(ConnectTimeoutException | SocketTimeoutException e) {
			if(proxy != null) {
				proxys.failure(proxy.getHostName(), proxy.getPort());
			}
			throw new DownloadTimeoutException(e);
		} catch(IOException e) {
			if(proxy != null) {
				proxys.failure(proxy.getHostName(), proxy.getPort());
			}
			throw new DownloadException(e);
		} finally {
			reqObj.releaseConnection();
		}
	}
	
	@Override
	public void shutdown() {
		try {
			httpClient.close();
		} catch (IOException e) {
			httpClient = null;
		}
	}
	
	public String getContent(InputStream instream, long contentLength, String charset) throws IOException {
		try {
			if (instream == null) {
	            return null;
	        }
	        int i = (int)contentLength;
	        if (i < 0) {
	            i = 4096;
	        }
	        Reader reader = new InputStreamReader(instream, charset);
	        CharArrayBuffer buffer = new CharArrayBuffer(i);
	        char[] tmp = new char[1024];
	        int l;
	        while((l = reader.read(tmp)) != -1) {
	            buffer.append(tmp, 0, l);
	        }
	        return buffer.toString();
		} finally {
			Objects.requireNonNull(instream).reset();
		}
        
    }
	
	protected boolean isImage(String contentType) {
		if(contentType == null) {
			return false;
		}
		if(contentType.toLowerCase().startsWith("image")) {
			return true;
		}
		return false;
	}
}
