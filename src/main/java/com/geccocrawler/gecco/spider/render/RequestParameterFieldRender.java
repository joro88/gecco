package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoContext;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.beans.BeanMap;

import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderBean;

public class RequestParameterFieldRender implements FieldRender {
    protected GeccoContext context;

    public RequestParameterFieldRender(GeccoContext context) {
        this.context = context;
    }
    
    @Override
	@SuppressWarnings({ "unchecked" })
	public void render(HttpRequest request, HttpResponse response, BeanMap beanMap, SpiderBean bean) {
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		Set<Field> requestParameterFields = ReflectionUtils.getAllFields(bean.getClass(), ReflectionUtils.withAnnotation(RequestParameter.class));
		for(Field field : requestParameterFields) {
			RequestParameter requestParameter = field.getAnnotation(RequestParameter.class);
			String key = requestParameter.value();
			if(StringUtils.isEmpty(key)) {
				key = field.getName();
			}
			String src = request.getParameter(key);
			if(request instanceof HttpPostRequest && StringUtils.isEmpty(src)) {
				HttpPostRequest postRequest = (HttpPostRequest)request;
				src = postRequest.getField(key);
			}
			try {
				Object value = context.getFieldConversion().getValue(field.getType(), src, field);
				fieldMap.put(field.getName(), value);
			} catch(Exception ex) {
				//throw new FieldRenderException(field, src, ex);
				FieldRenderException.log(field, src, ex);
			}
		}
		beanMap.putAll(fieldMap);
	}
	
}
