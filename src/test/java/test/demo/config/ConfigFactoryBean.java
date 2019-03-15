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
package test.demo.config;

import java.util.Properties;

import javax.annotation.PostConstruct;

import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.Order;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.annotation.Prototype;
import cn.taketoday.context.factory.FactoryBean;
import cn.taketoday.context.factory.InitializingBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Today <br>
 * 
 *         2018-08-08 15:06
 */
@Slf4j
@Getter
@Prototype
public class ConfigFactoryBean implements FactoryBean<Config>, InitializingBean {

	@PostConstruct
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void init1() {
		log.info("ConfigFactoryBean.init1()");
	}

	@PostConstruct
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void init2() {
		log.info("ConfigFactoryBean.init2()");
	}

	@Props(value = "info", prefix = "site.")
	private Properties pro;

	private Config bean;

	@Override
	public Config getBean() {
		return bean;
	}

	@Override
	public String getBeanName() {
		return "FactoryBean-Config";
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		bean = new Config();

		bean.setCdn(pro.getProperty("site.cdn"));
		bean.setHost(pro.getProperty("site.host"));
		bean.setCopyright(pro.getProperty("site.copyright"));
	}

	@Override
	public Class<Config> getBeanClass() {
		return Config.class;
	}

}