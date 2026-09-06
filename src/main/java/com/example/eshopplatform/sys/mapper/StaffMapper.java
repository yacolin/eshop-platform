package com.example.eshopplatform.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.eshopplatform.sys.entity.Staff;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * B端员工表 Mapper 接口
 * </p>
 *
 * <p>登录流水（sys_login_histories）以注解 SQL 直插（无独立实体，工程约定不建 mapper.xml）。</p>
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
}
