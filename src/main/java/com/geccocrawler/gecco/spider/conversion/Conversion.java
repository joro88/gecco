package com.geccocrawler.gecco.spider.conversion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Conversion {

	@SuppressWarnings({ "rawtypes" })
	protected final Map<Class<?>, TypeHandle> typeHandlers = new HashMap<Class<?>, TypeHandle>();

    public Conversion() {
		// int, float, long, double, java.util.Date, boolean, String
		typeHandlers.put(Integer.class, new IntegerTypeHandle());
		typeHandlers.put(int.class, new IntegerTypeHandle());
		typeHandlers.put(Long.class, new LongTypeHandle());
		typeHandlers.put(long.class, new LongTypeHandle());
		typeHandlers.put(Float.class, new FloatTypeHandle());
		typeHandlers.put(float.class, new FloatTypeHandle());
		typeHandlers.put(Double.class, new DoubleTypeHandle());
		typeHandlers.put(double.class, new DoubleTypeHandle());
		typeHandlers.put(Boolean.class, new BooleanTypeHandle());
		typeHandlers.put(boolean.class, new BooleanTypeHandle());
		typeHandlers.put(Date.class, new DateTypeHandle());
		typeHandlers.put(BigDecimal.class, new BigDecimalTypeHandle());
	}

	@SuppressWarnings({ "rawtypes" })
	public void register(Class<?> type, TypeHandle typeHandle) {
		typeHandlers.put(type, typeHandle);
	}

	public void unregister(Class<?> type) {
		typeHandlers.remove(type);
	}

	@SuppressWarnings({ "rawtypes" })
	public Object getValue(Class<?> type, Object value, Field field) throws Exception {
		TypeHandle th = typeHandlers.get(type);
		if (th != null && value != null) {
			return th.getValue(value);
		}
		return value;
	}

	public Object getDateValue(Object value, String format, Field field) throws Exception {
		DateTypeHandle th = (DateTypeHandle) typeHandlers.get(Date.class);
		return th.getValue(value, format);
	}
}
