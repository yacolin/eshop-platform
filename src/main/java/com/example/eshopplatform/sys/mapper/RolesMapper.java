package com.example.eshopplatform.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.sys.entity.Roles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * B端角色定义表 Mapper 接口
 * </p>
 *
 * <p>含员工-角色（sys_staff_roles）联表查询：员工角色名、管理员（builtin 角色）判定。</p>
 *
 * @since 2026-09-06
 */
@Mapper
public interface RolesMapper extends BaseMapper<Roles> {

    /**
     * 员工所挂全部角色名（按 sort_order,id 升序；用于 /staff/permissions）。
     */
    @Select("SELECT r.name FROM sys_roles r "
            + "JOIN sys_staff_roles sr ON sr.role_id = r.id "
            + "WHERE sr.staff_id = #{staffId} "
            + "AND sr.deleted_at IS NULL AND r.deleted_at IS NULL "
            + "ORDER BY r.sort_order, r.id")
    List<String> selectRoleNamesByStaffId(@Param("staffId") Long staffId);

    /**
     * 员工是否持 builtin（系统内置）角色——B端"管理员"判定依据（对齐 gf-eshop IsAdmin）。
     */
    @Select("SELECT COUNT(*) FROM sys_roles r "
            + "JOIN sys_staff_roles sr ON sr.role_id = r.id "
            + "WHERE sr.staff_id = #{staffId} AND r.role_type = 'builtin' "
            + "AND sr.deleted_at IS NULL AND r.deleted_at IS NULL")
    long countBuiltinRolesOfStaff(@Param("staffId") Long staffId);
}
