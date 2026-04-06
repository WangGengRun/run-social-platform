package com.run.runsocialplatform.module.auth.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "当前用户校友认证状态")
public class AlumniVerifyStatusVO {

    @Schema(description = "认证状态：-1未提交，0待审核，1已通过，2未通过", example = "0")
    private Integer verifyStatus;

    @Schema(description = "审核备注")
    private String verifyNotes;

    @Schema(description = "审核时间")
    private LocalDateTime verifyTime;

    @Schema(description = "当前用户角色", example = "USER")
    private String role;
}
