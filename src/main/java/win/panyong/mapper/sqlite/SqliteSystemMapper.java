package win.panyong.mapper.sqlite;

import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.*;
import win.panyong.model.*;

import java.util.List;

@Mapper
public interface SqliteSystemMapper {

    // ==================== 用户相关操作 ====================
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectUserById(Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectUserByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE status = 1 ORDER BY create_time DESC")
    List<User> selectAllUsers();

    @Select("SELECT u.* FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE (u.username LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR u.nickname LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR d.dept_name LIKE CONCAT('%', #{search.keyword}, '%')) " +
            "AND u.status = 1 " +
            "ORDER BY u.create_time DESC " +
            "LIMIT #{since}, #{limit}")
    List<User> selectUsersByKeyword(@Param("search") JSONObject search, @Param("since") int since, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE (u.username LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR u.nickname LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR d.dept_name LIKE CONCAT('%', #{search.keyword}, '%')) " +
            "AND u.status = 1")
    int countUsersByKeyword(@Param("search") JSONObject search);

    @Insert("INSERT INTO sys_user(username, password, nickname, email, phone, department_id, post_id, status, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{email}, #{phone}, #{departmentId}, #{postId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update("UPDATE sys_user SET username = #{username}, nickname = #{nickname}, email = #{email}, " +
            "phone = #{phone}, department_id = #{departmentId}, post_id = #{postId}, " +
            "status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateUser(User user);

    @Update("UPDATE sys_user SET password = #{password}, update_time = #{updateTime} WHERE id = #{id}")
    int updateUserPassword(@Param("id") Long id, @Param("password") String password, @Param("updateTime") java.util.Date updateTime);

    @Update("UPDATE sys_user SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.util.Date updateTime);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<Role> selectRolesByUserId(Long userId);

    // ==================== 角色相关操作 ====================
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    Role selectRoleById(Long id);

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    Role selectRoleByRoleCode(String roleCode);

    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY create_time DESC")
    List<Role> selectAllRoles();

    @Select("SELECT * FROM sys_role WHERE role_name LIKE CONCAT('%', #{since.keyword}, '%') " +
            "OR role_code LIKE CONCAT('%', #{search.keyword}, '%') " +
            "AND status = 1 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{since}, #{limit}")
    List<Role> selectRolesByKeyword(@Param("search") JSONObject search, @Param("since") int since, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_role WHERE role_name LIKE CONCAT('%', #{since.keyword}, '%') " +
            "OR role_code LIKE CONCAT('%', #{since.keyword}, '%') " +
            "AND status = 1")
    int countRolesByKeyword(@Param("search") JSONObject search);

    @Insert("INSERT INTO sys_role(role_name, role_code, description, status, create_time, update_time) " +
            "VALUES(#{roleName}, #{roleCode}, #{description}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRole(Role role);

    @Update("UPDATE sys_role SET role_name = #{roleName}, role_code = #{roleCode}, " +
            "description = #{description}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateRole(Role role);

    @Update("UPDATE sys_role SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateRoleStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.util.Date updateTime);

    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.status = 1")
    List<User> selectUsersByRoleId(Long roleId);

    // ==================== 岗位相关操作 ====================
    @Select("SELECT * FROM sys_post WHERE id = #{id}")
    Post selectPostById(Long id);

    @Select("SELECT * FROM sys_post WHERE post_code = #{postCode}")
    Post selectPostByPostCode(String postCode);

    @Select("SELECT * FROM sys_post WHERE status = 1 ORDER BY sort ASC, create_time DESC")
    List<Post> selectPostList();

    @Select("SELECT * FROM sys_post WHERE post_name LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR post_code LIKE CONCAT('%', #{search.keyword}, '%') " +
            "AND status = 1 " +
            "ORDER BY sort ASC, create_time DESC " +
            "LIMIT #{since}, #{limit}")
    List<Post> selectPostsByKeyword(@Param("search") JSONObject search, @Param("since") int since, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_post WHERE post_name LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR post_code LIKE CONCAT('%', #{search.keyword}, '%') " +
            "AND status = 1")
    int countPostsByKeyword(@Param("search") JSONObject search);

    @Insert("INSERT INTO sys_post(post_name, post_code, description, sort, status, create_time, update_time) " +
            "VALUES(#{postName}, #{postCode}, #{description}, #{sort}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPost(Post post);

    @Update("UPDATE sys_post SET post_name = #{postName}, post_code = #{postCode}, " +
            "description = #{description}, sort = #{sort}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updatePost(Post post);

    @Update("UPDATE sys_post SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updatePostStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.util.Date updateTime);

    // ==================== 部门相关操作 ====================
    @Select("SELECT * FROM sys_department WHERE id = #{id}")
    Department selectDepartmentById(Long id);

    @Select("SELECT * FROM sys_department WHERE dept_code = #{deptCode}")
    Department selectDepartmentByDeptCode(String deptCode);

    @Select("SELECT * FROM sys_department WHERE status = 1 ORDER BY sort ASC, create_time DESC")
    List<Department> selectDepartmentList();

    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort ASC")
    List<Department> selectDepartmentsByParentId(Long parentId);

    @Select("SELECT * FROM sys_department WHERE dept_name LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR dept_code LIKE CONCAT('%', #{search.keyword}, '%') " +
            "AND status = 1 " +
            "ORDER BY sort ASC, create_time DESC " +
            "LIMIT #{since}, #{limit}")
    List<Department> selectDepartmentsByKeyword(@Param("search") JSONObject search, @Param("since") int since, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_department WHERE dept_name LIKE CONCAT('%', #{search.keyword}, '%') " +
            "OR dept_code LIKE CONCAT('%', #{search.keyword}, '%') " +
            "AND status = 1")
    int countDepartmentsByKeyword(@Param("search") JSONObject search);

    @Insert("INSERT INTO sys_department(dept_name, dept_code, parent_id, ancestors, sort, leader, phone, email, status, create_time, update_time) " +
            "VALUES(#{deptName}, #{deptCode}, #{parentId}, #{ancestors}, #{sort}, #{leader}, #{phone}, #{email}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDepartment(Department department);

    @Update("UPDATE sys_department SET dept_name = #{deptName}, dept_code = #{deptCode}, " +
            "parent_id = #{parentId}, ancestors = #{ancestors}, sort = #{sort}, " +
            "leader = #{leader}, phone = #{phone}, email = #{email}, " +
            "status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateDepartment(Department department);

    @Update("UPDATE sys_department SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateDepartmentStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.util.Date updateTime);

    // ==================== 用户角色关联操作 ====================
    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(UserRole userRole);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteUserRole(UserRole userRole);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRolesByUserId(Long userId);

    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId}")
    List<UserRole> selectUserRolesByUserId(Long userId);

    @Select("SELECT * FROM sys_user_role WHERE role_id = #{roleId}")
    List<UserRole> selectUserRolesByRoleId(Long roleId);
}
