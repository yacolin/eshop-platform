package ${dtoPkg};
<#function escJd s>
    <#return s?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>
<#function escStr s>
    <#return s?replace("\\", "\\\\")?replace("\"", "\\\"")?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>

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
 * ${escJd(table.comment!)}视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：字段与表结构一致；接入真实接口时按业务裁剪
 * （隐藏内部字段、时间转 epoch 毫秒等）。</p>
 *
<#if author?? && author != ""> * @author ${author}
</#if> * @since ${date}
 */
@Data
@Schema(description = "${escStr(table.comment!)}VO")
public class ${table.entityName}VO {
<#list table.fields as field>
<#if field_index gt 0>

</#if>
<#if field.comment!?length gt 0>
    /** ${escJd(field.comment)} */
    @Schema(description = "${escStr(field.comment!)}")
</#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
}
