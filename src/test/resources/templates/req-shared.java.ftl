<#-- 请求体主体（req-create.java.ftl / req-update.java.ftl 复用）：
     调用前需在宿主模板 <#assign> 两个语义变量：
       reqSuffix  类名后缀（CreateReq / UpdateReq）
       reqVerb    中文动词（创建 / 更新） -->
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
<#function isDbRequired f>
    <#-- 客户端必填判定：DB 列 NOT NULL 且无默认值（元数据缺失时保守起见不判必填） -->
    <#if !(f.metaInfo??) || f.metaInfo.nullable || f.metaInfo.defaultValue??>
        <#return false>
    </#if>
    <#return true>
</#function>
<#assign tableTitle = (table.comment!?has_content)?then(table.comment!, table.entityName)>
<#assign hasNotBlank = false>
<#assign hasNotNull = false>
<#assign hasSize = false>
<#list table.fields as field>
<#if !field.keyFlag && !isAutoCol(field) && isDbRequired(field)>
    <#if field.propertyType == "String">
        <#assign hasNotBlank = true>
    <#else>
        <#assign hasNotNull = true>
    </#if>
</#if>
<#if !field.keyFlag && !isAutoCol(field) && field.propertyType == "String" && field.metaInfo?? && field.metaInfo.length gt 0 && field.metaInfo.length <= 4000>
    <#assign hasSize = true>
</#if>
</#list>
<#-- 按 Req 实际包含的业务字段类型收集所需 java import -->
<#assign hasBigDecimal = false>
<#assign hasLocalDate = false>
<#assign hasLocalDateTime = false>
<#assign hasLocalTime = false>
<#assign hasDate = false>
<#assign hasList = false>
<#list table.fields as field>
<#if !field.keyFlag && !isAutoCol(field)>
    <#if field.propertyType == "BigDecimal">
        <#assign hasBigDecimal = true>
    <#elseif field.propertyType == "LocalDate">
        <#assign hasLocalDate = true>
    <#elseif field.propertyType == "LocalDateTime">
        <#assign hasLocalDateTime = true>
    <#elseif field.propertyType == "LocalTime">
        <#assign hasLocalTime = true>
    <#elseif field.propertyType == "Date">
        <#assign hasDate = true>
    <#elseif field.propertyType == "List">
        <#assign hasList = true>
    </#if>
</#if>
</#list>
package ${dtoPkg};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
<#if hasNotBlank>
import jakarta.validation.constraints.NotBlank;
</#if>
<#if hasNotNull>
import jakarta.validation.constraints.NotNull;
</#if>
<#if hasSize>
import jakarta.validation.constraints.Size;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>
<#if hasLocalDateTime>
import java.time.LocalDateTime;
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
 * ${escJd(tableTitle)}${reqVerb}请求（${reqSuffix}）
 * </p>
 *
 * <p>由代码生成器生成的基础 DTO：仅含业务字段，不含主键 id 与
 * created_at/updated_at/deleted_at 等服务端自动维护列。DB 必填列
 * （NOT NULL 且无默认值）已自动加最基础的空校验（String 用
 * {@code @NotBlank}、其余用 {@code @NotNull}）；String 列另按 DB 长度
 * 上限自动补 {@code @Size}（防超长入库存 500）；其余字段按业务裁剪补充。</p>
 *
<#if author?? && author != ""> * @author ${author}
</#if> * @since ${date}
 */
@Data
@Schema(description = "${escStr(tableTitle)}${reqVerb}请求")
public class ${table.entityName}${reqSuffix} {
<#assign firstReqField = true>
<#list table.fields as field>
<#if !field.keyFlag && !isAutoCol(field)>
<#if !firstReqField>

</#if>
<#assign firstReqField = false>
<#assign fieldDesc = (field.comment!?has_content)?then(field.comment!, field.propertyName)>
<#if field.comment!?has_content>
    /** ${escJd(field.comment)} */
    @Schema(description = "${escStr(field.comment!)}")
</#if>
<#if isDbRequired(field)>
<#if field.propertyType == "String">
    @NotBlank(message = "${escStr(fieldDesc + "不能为空")}")
<#else>
    @NotNull(message = "${escStr(fieldDesc + "不能为空")}")
</#if>
</#if>
<#if field.propertyType == "String" && field.metaInfo?? && field.metaInfo.length gt 0 && field.metaInfo.length <= 4000>
    @Size(max = ${field.metaInfo.length}, message = "${escStr(fieldDesc + "长度不能超过{max}")}")
</#if>
    private ${field.propertyType} ${field.propertyName};
</#if>
</#list>
}
