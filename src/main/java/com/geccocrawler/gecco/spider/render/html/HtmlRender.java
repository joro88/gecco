package com.geccocrawler.gecco.spider.render.html;

import com.geccocrawler.gecco.GeccoFactory;
import net.sf.cglib.beans.BeanMap;

import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.render.AbstractRender;

/**
 * 将下载下来的html映射到bean中
 * 
 * @author huchengyi
 *
 */
public class HtmlRender extends AbstractRender {
	
	protected HtmlFieldRender htmlFieldRender;
	
	protected AjaxFieldRender ajaxFieldRender;
	
	protected JSVarFieldRender jsVarFieldRender;
	
	protected ImageFieldRender imageFieldRender;
	
	public HtmlRender(GeccoFactory factory) {
		super(factory);
        htmlFieldRender = factory.createHtmlFieldRender( this );
        ajaxFieldRender = factory.createAjaxFieldRender( this );
        jsVarFieldRender = factory.createJSVarFieldRender( this );
        imageFieldRender = factory.createImageFieldRender( this );
	}

	@Override
	public void fieldRender(HttpRequest request, HttpResponse response, BeanMap beanMap, SpiderBean bean) {
		htmlFieldRender.render(request, response, beanMap, bean);
		ajaxFieldRender.render(request, response, beanMap, bean);
		jsVarFieldRender.render(request, response, beanMap, bean);
		imageFieldRender.render(request, response, beanMap, bean);
	}

}
