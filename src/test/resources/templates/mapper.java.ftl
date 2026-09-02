package ${package.Mapper};
<#function escJd s>
    <#return s?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>
<#function escStr s>
    <#return s?replace("\\", "\\\\")?replace("\"", "\\\"")?replace("\r", " ")?replace("\n", " ")?replace("*/", "* /")>
</#function>

<#list importMapperFrameworkPackages as pkg>
import ${pkg};
</#list>
<#if importMapperJavaPackages?size !=0>

  <#list importMapperJavaPackages as pkg>
import ${pkg};
   </#list>
</#if>

/**
 * <p>
 * ${escJd(table.comment!)} Mapper 接口
 * </p>
 *
<#if author?? && author != ""> * @author ${author}
</#if> * @since ${date}
 */
<#if mapperAnnotationClass??>
@${mapperAnnotationClass.simpleName}
</#if>
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}> {
<#else>
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {
</#if>

<#list mapperMethodList as m>
    /**
     * generate by ${m.indexName}
     *
    <#list m.tableFieldList as f>
     * @param ${f.propertyName} ${escJd(f.comment!)}
    </#list>
     */
    ${m.method}
</#list>
}
