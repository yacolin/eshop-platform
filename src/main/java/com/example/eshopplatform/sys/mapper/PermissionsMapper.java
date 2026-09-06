package com.example.eshopplatform.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.sys.entity.Permissions;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * B端权限表 Mapper 接口
 * </p>
 *
 * <p>除单表 CRUD 外，这里集中了权限-角色-员工关联查询（sys_role_permissions /
 * sys_staff_roles 以注解 SQL 直查，工程约定不建 mapper.xml）。</p>
 *
 * @since 2026-09-06
 */
@Mapper
public interface PermissionsMapper extends BaseMapper<Permissions> {

    /**
     * 角色已授予的权限（B端平台范围，忽略双方软删；按 sort_order,id 升序，便于组树/排菜单）。
     */
    @Select("SELECT p.* FROM sys_permissions p "
            + "JOIN sys_role_permissions rp ON rp.permission_id = p.id "
            + "WHERE rp.role_id = #{roleId} "
            + "AND rp.scope_type = 'platform' AND rp.scope_id = 0 "
            + "AND rp.deleted_at IS NULL AND p.deleted_at IS NULL "
            + "ORDER BY p.sort_order, p.id")
    List<Permissions> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理清空角色全部授权（替换式授权先删后插）。
     * 不用逻辑删除：uk_role_permission(role_id, permission_id) 唯一索引下软删行仍占位，
     * 再次授予同一权限会撞唯一键，故关联关系采取物理替换而非软删。
     */
    @Delete("DELETE FROM sys_role_permissions WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);

    /**
     * 批量写入角色-权限关联（平台范围，scope_type='platform'、scope_id=0）。
     * 调用前需先 deleteRolePermissions 清空旧授权。
     */
    @Insert("<script>"
            + "INSERT INTO sys_role_permissions (role_id, permission_id, scope_type, scope_id) VALUES "
            + "<foreach collection='permissionIds' item='pid' separator=','>"
            + "(#{roleId}, #{pid}, 'platform', 0)"
            + "</foreach>"
            + "</script>")
    int insertRolePermissions(@Param("roleId") Long roleId,
                              @Param("permissionIds") Collection<Long> permissionIds);

    /**
     * 员工经其全部角色能触达的权限标识 name 集合（去重，含禁用权限；对齐 gf GetPermissions）。
     * 用于 GET /api/v1/staff/permissions。
     */
    @Select("SELECT DISTINCT p.name FROM sys_permissions p "
            + "JOIN sys_role_permissions rp ON rp.permission_id = p.id "
            + "JOIN sys_staff_roles sr ON sr.role_id = rp.role_id "
            + "WHERE sr.staff_id = #{staffId} "
            + "AND rp.scope_type = 'platform' AND rp.scope_id = 0 "
            + "AND sr.deleted_at IS NULL AND rp.deleted_at IS NULL AND p.deleted_at IS NULL")
    List<String> selectPermissionNamesByStaffId(@Param("staffId") Long staffId);

    /**
     * 员工是否拥有指定权限标识（按 name 定位，对齐 gf HasPermission）——
     * POST /permissions/check 用。权限本身被禁用（status=0）时同样视为无权限。
     */
    @Select("SELECT COUNT(*) FROM sys_permissions p "
            + "JOIN sys_role_permissions rp ON rp.permission_id = p.id "
            + "JOIN sys_staff_roles sr ON sr.role_id = rp.role_id "
            + "WHERE p.name = #{permissionName} AND sr.staff_id = #{staffId} "
            + "AND p.status = 1 "
            + "AND p.deleted_at IS NULL AND rp.deleted_at IS NULL AND sr.deleted_at IS NULL "
            + "AND rp.scope_type = 'platform' AND rp.scope_id = 0")
    long countStaffHasPermissionByName(@Param("staffId") Long staffId,
                                       @Param("permissionName") String permissionName);

    /**
     * 权限被未软删的角色授权引用的条数（删除保护用）。
     */
    @Select("SELECT COUNT(*) FROM sys_role_permissions rp "
            + "WHERE rp.permission_id = #{permissionId} AND rp.deleted_at IS NULL")
    long countRoleRefs(@Param("permissionId") Long permissionId);
}
