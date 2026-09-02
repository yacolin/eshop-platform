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
 * ${table.comment!}视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：字段与表结构一致；接入真实接口时按业务裁剪
 * （隐藏内部字段、时间转 epoch 毫秒等）。</p>
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@Schema(description = "${table.comment!}VO")
public class ${table.entityName}VO {
<#list table.fields as field>
<#if field.comment!?length gt 0>

    /** ${field.comment} */
</#if>
    @Schema(description = "${field.comment!''}")
    private ${field.propertyType} ${field.propertyName};
</#list>
}
