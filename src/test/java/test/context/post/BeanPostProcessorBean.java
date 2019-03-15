/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package test.context.post;

import cn.taketoday.context.annotation.Order;
import cn.taketoday.context.bean.BeanDefinition;
import cn.taketoday.context.factory.BeanPostProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Today <br>
 * 
 *         2018-08-08 18:20
 */
@Slf4j
@Order(1)
//@Singleton
public class BeanPostProcessorBean implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) {
		log.debug("BeanPostProcessorBean Before named :[{}]", beanDefinition.getName());
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		log.debug("BeanPostProcessorBean After named :[{}]", beanName);
		return bean;
	}

}