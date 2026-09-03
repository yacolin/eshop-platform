package ${dtoPkg};
<#function escJd s>
    <#return s?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>
<#function escStr s>
    <#return s?replace("\\", "\\\\")?replace("\"", "\\\"")?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>
<#assign autoCols = ["created_at", "updated_at", "deleted_at", "create_at", "update_at", "delete_at"]>
<#function isAutoCol f>
    <#list autoCols as c>
        <#if f.name == c><#return true></#if>
    </#list>
    <#return false>
</#function>
<#function isTimeField f>
    <#return f.propertyType == "LocalDateTime">
</#function>
<#-- 按 VO 实际渲染的类型收集所需 import（LocalDateTime 在 VO 中转 Long，不需要其 import） -->
<#assign hasBigDecimal = false>
<#assign hasLocalDate = false>
<#assign hasLocalTime = false>
<#assign hasDate = false>
<#assign hasList = false>
<#list table.fields as field>
<#if !isAutoCol(field) || field.name == "created_at" || field.name == "updated_at">
    <#if field.propertyType == "BigDecimal">
        <#assign hasBigDecimal = true>
    <#elseif field.propertyType == "LocalDate">
        <#assign hasLocalDate = true>
    <#elseif field.propertyType == "LocalTime">
        <#assign hasLocalTime = true>
    <#elseif field.propertyType == "Date">
        <#assign hasDate = true>
    <#elseif field.propertyType == "List">
        <#assign hasList = true>
    </#if>
</#if>
</#list>

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>
<#if hasLocalTime>
import java.time.LocalTime;
</#if>
<#if hasDate>
import java.util.Date;
</#if>
<#if hasList>
import java.util.List;
</#if>

/**
 * <p>
 * ${escJd(table.comment!)}视图对象（VO）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：deleted_at 为服务端软删字段，不对业务端暴露；
 * 时间列（LocalDateTime）统一以 Long（epoch 毫秒）返回，避免各端解析字符串时间；
 * 接入真实接口时按业务裁剪。</p>
 *
<#if author?? && author != ""> * @author ${author}
</#if> * @since ${date}
 */
@Data
@Schema(description = "${escStr(table.comment!)}VO")
public class ${table.entityName}VO {
<#assign firstVoField = true>
<#list table.fields as field>
<#if !isAutoCol(field) || field.name == "created_at" || field.name == "updated_at">
<#if !firstVoField>

</#if>
<#assign firstVoField = false>
<#assign timeField = isTimeField(field)>
<#assign fieldDesc = field.comment!>
<#if !fieldDesc?has_content && field.name == "created_at">
    <#assign fieldDesc = "创建时间">
</#if>
<#if !fieldDesc?has_content && field.name == "updated_at">
    <#assign fieldDesc = "更新时间">
</#if>
<#if timeField>
    <#assign fieldDesc = fieldDesc + "（epoch 毫秒）">
</#if>
<#if fieldDesc?has_content>
    /** ${escJd(fieldDesc)} */
    @Schema(description = "${escStr(fieldDesc)}")
</#if>
    private ${timeField?then("Long", field.propertyType)} ${field.propertyName};
</#if>
</#list>
}
