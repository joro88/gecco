package com.geccocrawler.gecco.spider.render.json;

import com.geccocrawler.gecco.GeccoFactory;
import com.geccocrawler.gecco.GeccoMediator;
import net.sf.cglib.beans.BeanMap;

import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.render.AbstractRender;

/**
 * 将下载下来的json映射到bean中
 * 
 * @author huchengyi
 *
 */
public class JsonRender extends AbstractRender {
	
	protected JsonFieldRender jsonFieldRender;
	
	public JsonRender(GeccoMediator mediator) {
		super( mediator );
        GeccoFactory factory = mediator.getFactory();
		this.jsonFieldRender = factory.createJsonFieldRender( this );
	}

	@Override
	public void fieldRender(HttpRequest request, HttpResponse response, BeanMap beanMap, SpiderBean bean) {
		jsonFieldRender.render(request, response, beanMap, bean);
	}

}