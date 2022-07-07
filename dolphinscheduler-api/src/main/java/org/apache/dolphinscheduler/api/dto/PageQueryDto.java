package org.apache.dolphinscheduler.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * page query dto
 */
@ApiModel("QUERY-PAGE-INFO")
public class PageQueryDto {

    @ApiModelProperty(example = "10", required = true)
    private Integer pageSize;

    @ApiModelProperty(example = "1", required = true)
    private Integer pageNo;

    public PageQueryDto() {
    }

    public PageQueryDto(Integer pageSize, Integer pageNo) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }
}