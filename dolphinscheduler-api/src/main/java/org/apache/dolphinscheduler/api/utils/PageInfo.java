/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.utils;

import java.util.Collections;
import java.util.List;

import lombok.Data;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Data
public class PageInfo<T> {

    /**
     * totalList
     */
    private List<T> totalList = Collections.emptyList();
    /**
     * total
     */
    private Integer total = 0;
    /**
     * total Page
     */
    private Integer totalPage;
    /**
     * page size
     */
    private Integer pageSize = 20;
    /**
     * current page
     */
    private Integer currentPage = 0;
    /**
     * pageNo
     */
    private Integer pageNo;

    public PageInfo() {

    }

    public PageInfo(Integer currentPage, Integer pageSize) {
        if (currentPage == null) {
            currentPage = 1;
        }
        this.pageNo = (currentPage - 1) * pageSize;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

    public static <T> PageInfo<T> of(IPage<T> iPage) {
        PageInfo<T> pageInfo = new PageInfo<>((int) iPage.getCurrent(), (int) iPage.getSize());
        pageInfo.setTotalList(iPage.getRecords());
        pageInfo.setTotal((int) iPage.getTotal());
        return pageInfo;
    }

    public static <T> PageInfo<T> of(Integer currentPage, Integer pageSize) {
        return new PageInfo<>(currentPage, pageSize);
    }
}
