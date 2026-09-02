package ${package.Service};

import ${package.Mapper}.${table.mapperName};
import ${package.Entity}.${entity};
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * ${table.comment!}服务
 * </p>
 *
 * <p>与 lease-platform 风格一致：Service 为具体类（不生成接口与 Impl），
 * 直接注入 Mapper；基础 CRUD 走 MyBatis-Plus BaseMapper，业务方法在此手写。</p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
@RequiredArgsConstructor
public class ${table.serviceName} {

    /** 数据访问层 */
    private final ${table.mapperName} ${entity?uncap_first}Mapper;
}
