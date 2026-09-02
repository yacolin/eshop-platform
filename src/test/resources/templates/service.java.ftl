package ${package.Service};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${package.Mapper}.${table.mapperName};
import ${package.Entity}.${table.entityName};
import ${dtoPkg}.${table.entityName}Req;
import ${dtoPkg}.${table.entityName}VO;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * ${table.comment!}服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（Req/VO），entity↔dto 的 toVO/apply 由生成器自动补齐。
 * 以下为基础 CRUD，接入真实业务时按需加查询条件、校验与权限逻辑。</p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
@RequiredArgsConstructor
public class ${table.serviceName} {

    /** 数据访问层 */
    private final ${table.mapperName} ${table.entityName?uncap_first}Mapper;

    /** 分页查询（第 page 页，每页 size 条） */
    public PageResult<${table.entityName}VO> page(int page, int size) {
        Page<${table.entityName}> p = new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 1000));
        ${table.entityName?uncap_first}Mapper.selectPage(p, new LambdaQueryWrapper<${table.entityName}>()
                .orderByDesc(${table.entityName}::getId));
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /** 按主键查询 */
    public ${table.entityName}VO getById(Long id) {
        return toVO(require(id));
    }

    /** 新增 */
    public ${table.entityName}VO create(${table.entityName}Req req) {
        ${table.entityName} entity = new ${table.entityName}();
        apply(entity, req);
        ${table.entityName?uncap_first}Mapper.insert(entity);
        return toVO(entity);
    }

    /** 按主键更新 */
    public ${table.entityName}VO update(Long id, ${table.entityName}Req req) {
        ${table.entityName} entity = require(id);
        apply(entity, req);
        ${table.entityName?uncap_first}Mapper.updateById(entity);
        return toVO(entity);
    }

    /** 按主键删除 */
    public void delete(Long id) {
        require(id);
        ${table.entityName?uncap_first}Mapper.deleteById(id);
    }

    /** 按主键查询，不存在抛 404 */
    private ${table.entityName} require(Long id) {
        ${table.entityName} entity = ${table.entityName?uncap_first}Mapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("${table.comment!}不存在");
        }
        return entity;
    }

    /** entity -> VO（生成器自动映射，接入真实业务时在此裁剪字段/转换时间戳） */
    private ${table.entityName}VO toVO(${table.entityName} entity) {
        ${table.entityName}VO vo = new ${table.entityName}VO();
<#list table.fields as field>
        vo.set${field.capitalName}(entity.get${field.capitalName}());
</#list>
        return vo;
    }

    /** Req -> entity（生成器自动映射，主键/自动字段跳过；业务字段计算在此补充） */
    private void apply(${table.entityName} entity, ${table.entityName}Req req) {
<#list table.fields as field>
<#if !field.keyFlag>
        entity.set${field.capitalName}(req.get${field.capitalName}());
</#if>
</#list>
    }
}
