package org.apache.dolphinscheduler.test.apis.common;

import org.apache.dolphinscheduler.test.base.AbstractBaseEntity;

public class PageParamEntity extends AbstractBaseEntity {
    private int pageSize;
    private int pageNo;
    private String searchVal;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public String getSearchVal() {
        return searchVal;
    }

    public void setSearchVal(String searchVal) {
        this.searchVal = searchVal;
    }
}
