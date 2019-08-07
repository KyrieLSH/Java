/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * {@link BeanDefinitionParser} for the {@code aspectj-autoproxy} tag,
 * enabling the automatic application of @AspectJ-style aspects found in
 * the {@link org.springframework.beans.factory.BeanFactory}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * 解析标签的时候将会执行的方法
	 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return
	 */
	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		/**
		 * 注册一个类型为AnnotationAwareAspectJAutoProxyCreator的bean到Spring容器中
		 */
		AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
		/**
		 * 通过读取配置文件对扩展相关属性
		 */
		extendBeanDefinition(element, parserContext);
		return null;
	}

	private void extendBeanDefinition(Element element, ParserContext parserContext) {
		/**
		 * 获取前面注册的AnnotationAwareAspectJAutoProxyCreator对应的BeanDefinition
		 */
		BeanDefinition beanDef =
				parserContext.getRegistry().getBeanDefinition(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		/**
		 * 解析当前标签的子标签
		 */
		if (element.hasChildNodes()) {
			addIncludePatterns(element, parserContext, beanDef);
		}
	}

	/**
	 * 解析子标签中的name属性，其可以有多个，这个name属性最终会被添加到
	 * AnnotationAwareAspectJAutoProxyCreator的includePatterns属性中，
	 * spring在判断一个类是否需要进行代理的时候会判断当前bean的名称是否与includePatterns中的
	 * 正则表达式相匹配，如果不匹配，则不进行代理
	 * @param element
	 * @param parserContext
	 * @param beanDef
	 */
	private void addIncludePatterns(Element element, ParserContext parserContext, BeanDefinition beanDef) {
		ManagedList<TypedStringValue> includePatterns = new ManagedList<>();
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element includeElement = (Element) node;
				/**
				 * 解析子标签中的name属性
				 */
				TypedStringValue valueHolder = new TypedStringValue(includeElement.getAttribute("name"));
				valueHolder.setSource(parserContext.extractSource(includeElement));
				includePatterns.add(valueHolder);
			}
		}
		/**
		 * 将解析到的name属性设置到AnnotationAwareAspectJAutoProxyCreator的includePatterns属性中
		 */
		if (!includePatterns.isEmpty()) {
			includePatterns.setSource(parserContext.extractSource(element));
			beanDef.getPropertyValues().add("includePatterns", includePatterns);
		}
	}

}
