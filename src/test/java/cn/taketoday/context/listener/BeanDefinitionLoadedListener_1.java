/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
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
package cn.taketoday.context.listener;

import cn.taketoday.context.annotation.Order;
import cn.taketoday.context.event.ApplicationListener;
import cn.taketoday.context.event.BeanDefinitionLoadedEvent;
import cn.taketoday.context.logger.Logger;
import cn.taketoday.context.logger.LoggerFactory;

/**
 * @author Today <br>
 * 
 *         2018-11-08 20:38
 */
@Order(1)
public class BeanDefinitionLoadedListener_1 implements ApplicationListener<BeanDefinitionLoadedEvent> {
    private static final Logger log = LoggerFactory.getLogger(BeanDefinitionLoadedListener_1.class);

    @Override
    public void onApplicationEvent(BeanDefinitionLoadedEvent event) {
        log.debug("BeanDefinitionLoadedListener_1");
    }

}
