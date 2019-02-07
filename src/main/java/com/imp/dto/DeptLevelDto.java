package com.imp.dto;

import com.google.common.collect.Lists;
import com.imp.model.SysDept;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
@ToString
public class DeptLevelDto extends SysDept {

    // 每个部门下一层部门
    private List<DeptLevelDto> deptList = Lists.newArrayList();

    // 将SysDept 转换成 DeptLevelDto
    public static DeptLevelDto adapt(SysDept dept) {
        DeptLevelDto dto = new DeptLevelDto();
        BeanUtils.copyProperties(dept, dto);
        return dto;
    }
}
