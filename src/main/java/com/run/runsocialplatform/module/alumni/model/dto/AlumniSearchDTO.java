package com.run.runsocialplatform.module.alumni.model.dto;


import com.run.runsocialplatform.common.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "校友搜索DTO")
public class AlumniSearchDTO extends PageParam {

    @Schema(description = "姓名关键字")
    private String keyword;

    @Schema(description = "入学年份")
    private Integer admissionYear;

    @Schema(description = "毕业年份")
    private Integer graduationYear;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "公司")
    private String company;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "是否只显示已认证校友")
    private Boolean verifiedOnly = false;

    @Schema(description = "排序字段：createTime-最新注册，admissionYear-入学年份")
    private String sortField = "createTime";

    @Schema(description = "排序方式：asc-升序，desc-降序")
    private String sortOrder = "desc";

    /**
     * 构建SQL排序字段
     */
    public String getOrderByClause() {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "a.created_at DESC";
        }

        String field;
        switch (sortField) {
            case "admissionYear":
                field = "a.admission_year";
                break;
            case "realName":
                field = "a.real_name";
                break;
            case "createTime":
            default:
                field = "a.created_at";
                break;
        }

        return field + " " + ("asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC");
    }
}