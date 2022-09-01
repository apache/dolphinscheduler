package org.apache.dolphinscheduler.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class FavDto {

    private String taskName;
    private boolean isCollection;
    private String taskType;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FavDto) {
            FavDto favDto = (FavDto) obj;
            return this.taskName.equals(favDto.getTaskName());

        }
        return super.equals(obj);
    }
}
