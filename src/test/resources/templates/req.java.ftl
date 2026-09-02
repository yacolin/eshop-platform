package ${dtoPkg};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * ${table.comment!}创建/更新请求（Req）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：不含主键 id（自动生成），可能含服务端自动维护的
 * 字段（如创建时间）；接入真实接口时按业务裁剪并补充校验注解。</p>
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@Schema(description = "${table.comment!}创建/更新请求")
public class ${table.entityName}Req {
<#list table.fields as field>
<#if !field.keyFlag>
<#if field.comment!?length gt 0>

    /** ${field.comment} */
</#if>
    @Schema(description = "${field.comment!''}")
    private ${field.propertyType} ${field.propertyName};
</#if>
</#list>
}
