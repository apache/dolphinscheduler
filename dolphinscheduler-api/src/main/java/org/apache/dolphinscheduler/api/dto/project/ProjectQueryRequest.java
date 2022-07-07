package org.apache.dolphinscheduler.api.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * project query request
 */
@ApiModel("PROJECT-QUERY")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectQueryRequest extends PageQueryDto {

    @ApiModelProperty(example = "pro123")
    private String searchVal;

    public String getSearchVal() {
        return searchVal;
    }

    public void setSearchVal(String searchVal) {
        this.searchVal = searchVal;
    }
}
