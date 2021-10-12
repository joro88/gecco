package com.geccocrawler.gecco.pipeline;

import com.geccocrawler.gecco.GeccoContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.spider.SpiderBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultPipelineFactory implements PipelineFactory {
    private static final Log log = LogFactory.getLog(DefaultPipelineFactory.class);

	protected Map<String, Pipeline<? extends SpiderBean>> pipelines;

	@SuppressWarnings({ "unchecked" })
	public DefaultPipelineFactory(GeccoContext context, Reflections reflections) {
		this.pipelines = new HashMap<String, Pipeline<? extends SpiderBean>>();
		Set<Class<?>> pipelineClasses = reflections.getTypesAnnotatedWith(PipelineName.class);
		for (Class<?> pipelineClass : pipelineClasses) {
			PipelineName spiderFilter = pipelineClass.getAnnotation(PipelineName.class);
			try {
                Pipeline<? extends SpiderBean> instance = (Pipeline<? extends SpiderBean>) 
                        pipelineClass.getDeclaredConstructor(GeccoContext.class).newInstance(context);
				pipelines.put(spiderFilter.value(), instance);
			} catch (Exception ex) {
				ex.printStackTrace();
                log.error("exception while trying to attach pipeline " + spiderFilter.value(), ex);
			}
		}
	}

	@Override
	public Pipeline<? extends SpiderBean> getPipeline(String name) {
		return pipelines.get(name);
	}

}
