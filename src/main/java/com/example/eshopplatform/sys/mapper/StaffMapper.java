package com.example.eshopplatform.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.sys.entity.Staff;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * B端员工表 Mapper 接口
 * </p>
 *
 * <p>员工-角色（sys_staff_roles）、员工-部门（sys_staff_departments）关联与部门存在性
 * 校验以注解 SQL 直查（工程约定不建 mapper.xml）。关联关系与角色-权限一致采用
 * "物理清空 + 批量写入"的替换式维护（唯一键 uk 下软删行会挡路）。</p>
 *
 * @since 2026-09-06
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {

    /**
     * 记录一次员工登录流水（成功/失败都记）。
     */
    @Insert("INSERT INTO sys_login_histories "
            + "(staff_id, login_ip, login_device, login_location, login_method, login_status, failure_reason) "
            + "VALUES (#{staffId}, #{ip}, #{device}, #{location}, #{method}, #{status}, #{reason})")
    int insertLoginHistory(@Param("staffId") Long staffId,
                           @Param("ip") String ip,
                           @Param("device") String device,
                           @Param("location") String location,
                           @Param("method") String method,
                           @Param("status") int status,
                           @Param("reason") String reason);

    // ==================== 员工-部门（sys_staff_departments） ====================

    /** 员工所属部门 ID 列表（按部门 sort_order,id 升序） */
    @Select("SELECT d.id FROM sys_departments d "
            + "JOIN sys_staff_departments sd ON sd.department_id = d.id "
            + "WHERE sd.staff_id = #{staffId} "
            + "AND sd.deleted_at IS NULL AND d.deleted_at IS NULL "
            + "ORDER BY d.sort_order, d.id")
    List<Long> selectDepartmentIdsByStaffId(@Param("staffId") Long staffId);

    /** 员工所属部门名称列表（与 ID 列表同序） */
    @Select("SELECT d.name FROM sys_departments d "
            + "JOIN sys_staff_departments sd ON sd.department_id = d.id "
            + "WHERE sd.staff_id = #{staffId} "
            + "AND sd.deleted_at IS NULL AND d.deleted_at IS NULL "
            + "ORDER BY d.sort_order, d.id")
    List<String> selectDepartmentNamesByStaffId(@Param("staffId") Long staffId);

    /** 物理清空员工部门关联（替换式维护先删后插） */
    @Delete("DELETE FROM sys_staff_departments WHERE staff_id = #{staffId}")
    int deleteStaffDepartments(@Param("staffId") Long staffId);

    /** 批量写入员工-部门关联 */
    @Insert("<script>"
            + "INSERT INTO sys_staff_departments (staff_id, department_id) VALUES "
            + "<foreach collection='departmentIds' item='did' separator=','>"
            + "(#{staffId}, #{did})"
            + "</foreach>"
            + "</script>")
    int insertStaffDepartments(@Param("staffId") Long staffId,
                               @Param("departmentIds") Collection<Long> departmentIds);

    /** 校验部门 ID 均存在（未软删），返回匹配条数（与入参数量比对判缺失） */
    @Select("<script>"
            + "SELECT COUNT(*) FROM sys_departments WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + " AND deleted_at IS NULL"
            + "</script>")
    long countExistingDepartments(@Param("ids") Collection<Long> ids);

    // ==================== 员工-角色（sys_staff_roles） ====================

    /** 物理清空员工角色关联（替换式维护先删后插） */
    @Delete("DELETE FROM sys_staff_roles WHERE staff_id = #{staffId}")
    int deleteStaffRoles(@Param("staffId") Long staffId);

    /** 批量写入员工-角色关联 */
    @Insert("<script>"
            + "INSERT INTO sys_staff_roles (staff_id, role_id) VALUES "
            + "<foreach collection='roleIds' item='rid' separator=','>"
            + "(#{staffId}, #{rid})"
            + "</foreach>"
            + "</script>")
    int insertStaffRoles(@Param("staffId") Long staffId,
                         @Param("roleIds") Collection<Long> roleIds);
}
