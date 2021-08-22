package com.geccocrawler.gecco.pipeline;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.GeccoContext;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.spider.SpiderBean;

@PipelineName("consolePipeline")
public class ConsolePipeline implements Pipeline<SpiderBean> {

    /**
     *
     * @param context - it should be of type GeccoContext because it is created
     * by reflection
     */
    public ConsolePipeline(GeccoContext context) {
    }

	@Override
	public void process(SpiderBean bean) {
		System.out.println(JSON.toJSONString(bean));
	}

}
