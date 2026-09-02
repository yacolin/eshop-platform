package ${package.Controller};

import com.example.eshopplatform.common.ApiResponse;
import com.example.eshopplatform.common.PageResult;
import ${dtoPkg}.${table.entityName}Req;
import ${dtoPkg}.${table.entityName}VO;
import ${package.Service}.${table.serviceName};
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

<#-- 生成规则：表名去掉域前缀、下划线转短横线（保留复数）
     sp_brands -> /api/v1/brands；sp_product_attributes -> /api/v1/product-attributes -->
<#assign pIdx = table.name?index_of("_")>
<#assign restPath = (pIdx >= 0)?then(table.name?substring(pIdx + 1), table.name)?replace("_", "-")>

/**
 * <p>
 * ${table.comment!}基础 CRUD 接口（模板生成）
 * </p>
 *
 * <p>默认路径按“表名去前缀 + 短横线”生成（如 sp_brands → /api/v1/brands）。
 * 接入真实接口前请调整：如遇子资源/嵌套接口改更精确的路径、Req/VO 按接口用例
 * 裁剪校验、接口按端分组补 springdoc @Tag，并把真实路径补入 application.yml
 * 的 whitelist/admin-paths。</p>
 *
 * @author ${author}
 * @since ${date}
 */
@Tag(name = "${table.entityName}Crud", description = "${table.comment!}基础 CRUD")
@RestController
@RequestMapping("/api/v1/${restPath}")
@RequiredArgsConstructor
public class ${table.controllerName} {

    private final ${table.serviceName} ${table.serviceName?uncap_first};

    @Operation(summary = "分页查询${table.comment!}")
    @GetMapping
    public ApiResponse<PageResult<${table.entityName}VO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(${table.serviceName?uncap_first}.page(page, size));
    }

    @Operation(summary = "查询${table.comment!}详情")
    @GetMapping("/{id}")
    public ApiResponse<${table.entityName}VO> get(@PathVariable Long id) {
        return ApiResponse.ok(${table.serviceName?uncap_first}.getById(id));
    }

    @Operation(summary = "新增${table.comment!}")
    @PostMapping
    public ApiResponse<${table.entityName}VO> create(@RequestBody ${table.entityName}Req req) {
        return ApiResponse.ok(${table.serviceName?uncap_first}.create(req));
    }

    @Operation(summary = "更新${table.comment!}")
    @PutMapping("/{id}")
    public ApiResponse<${table.entityName}VO> update(@PathVariable Long id,
                                                     @RequestBody ${table.entityName}Req req) {
        return ApiResponse.ok(${table.serviceName?uncap_first}.update(id, req));
    }

    @Operation(summary = "删除${table.comment!}")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ${table.serviceName?uncap_first}.delete(id);
        return ApiResponse.ok(null);
    }
}
