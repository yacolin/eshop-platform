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
 * ${escJd(table.comment!)}创建/更新请求（Req）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：不含主键 id（自动生成），可能含服务端自动维护的
 * 字段（如创建时间）；接入真实接口时按业务裁剪并补充校验注解。</p>
 *
<#if author?? && author != ""> * @author ${author}
</#if> * @since ${date}
 */
@Data
@Schema(description = "${escStr(table.comment!)}创建/更新请求")
public class ${table.entityName}Req {
<#assign firstReqField = true>
<#list table.fields as field>
<#if !field.keyFlag>
<#if !firstReqField>

</#if>
<#assign firstReqField = false>
<#if field.comment!?length gt 0>
    /** ${escJd(field.comment)} */
    @Schema(description = "${escStr(field.comment!)}")
</#if>
    private ${field.propertyType} ${field.propertyName};
</#if>
</#list>
}
