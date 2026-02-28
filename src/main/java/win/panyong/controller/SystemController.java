package win.panyong.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.*;
import win.panyong.model.Department;
import win.panyong.model.Post;
import win.panyong.model.Role;
import win.panyong.model.User;
import win.panyong.utils.*;
import win.panyong.utils.authority.Permission;
import win.panyong.utils.authority.PermissionType;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@Permission({PermissionType.ADMIN, PermissionType.USER})
public class SystemController extends BaseController {

    // ==================== 用户管理 API ====================
    @GetMapping("/user/getUserInfoById")
    @AppLog("查询用户详情")
    public String getUserInfoById() {
        Result.Builder result;
        String userId = getParameter("userId");
        if (!StringUtil.invalid(userId)) {
            User user = systemService.getUserById(Long.parseLong(userId));
            if (user != null) {
                result = Result.ok(user);
            } else {
                result = Result.fail("用户不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/user/getUserInfoUserName")
    @AppLog("根据用户名查询用户")
    public String getUserInfoUserName() {
        Result.Builder result;
        String userName = getParameter("userName");
        if (!StringUtil.invalid(userName)) {
            User user = systemService.getUserByUsername(userName);
            if (user != null) {
                result = Result.ok(user);
            } else {
                result = Result.fail("用户不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/getUserPage")
    @AppLog("分页查询用户列表")
    public String getUserPage(@RequestBody(required = false) JSONObject search) {
        int pageNo = StringUtil.invalid(getParameter("pageNo")) ? 1 : Integer.parseInt(getParameter("pageNo"));
        int limit = StringUtil.invalid(getParameter("limit")) ? 10 : Integer.parseInt(getParameter("limit"));
        Page<User> page = new Page<>(pageNo, limit);
        page.setSearch(search);
        page = systemService.getUserPage(page);
        return Result.ok(page).buildJsonString();
    }

    @PostMapping("/user/createUser")
    @AppLog("创建用户")
    public String createUser(@RequestBody User user) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(user.getUsername(), user.getPhone(), user.getPassword())) {
            if (systemService.getUserByUsername(user.getUsername()) == null) {
                result = systemService.createUser(user) ? Result.ok(user) : Result.fail("创建失败");
            } else {
                result = Result.fail("用户名已存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/updateUser")
    @AppLog("更新用户信息")
    public String updateUser(@RequestBody User user) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(user.getUsername(), user.getPhone(), user.getPassword())) {
            result = systemService.updateUser(user) ? Result.ok(user) : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/updateUserStatus")
    @AppLog("更新用户状态")
    public String updateUserStatus(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("userId"), body.getString("status"))) {
            result = systemService.updateUserStatus(body.getLong("userId"), body.getInteger("status")) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/changePassword")
    @AppLog("修改用户密码")
    public String changePassword(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("userId"), body.getString("oldPassword"), body.getString("newPassword"))) {
            result = systemService.changeUserPassword(body.getLong("userId"), body.getString("oldPassword"), body.getString("newPassword")) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/resetPassword")
    @AppLog("重置用户密码")
    public String resetPassword(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("userId"))) {
            String newPassword = body.getString("newPassword");
            if (StringUtil.invalid(newPassword)) {
                newPassword = StringUtil.genCode(10);
            }
            result = systemService.resetUserPassword(body.getLong("userId"), newPassword) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/user/getUserRoles")
    @AppLog("查询用户角色")
    public String getUserRoles() {
        Result.Builder result;
        String userId = getParameter("userId");
        if (!StringUtil.invalid(userId)) {
            List<Role> roles = systemService.getUserRoles(Long.parseLong(userId));
            result = Result.ok(roles);
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/user/changeUserRoles")
    @AppLog("分配用户角色")
    public String changeUserRoles(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("userId"))) {
            result = systemService.changeUserRoles(body.getLong("userId"), body.getList("roleIdList", Long.class)) ? Result.ok() : Result.fail("分配失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    // ==================== 角色管理 API ====================
    @GetMapping("/role/getRoleById")
    @AppLog("查询角色详情")
    public String getRoleById() {
        Result.Builder result;
        String userId = getParameter("userId");
        if (!StringUtil.invalid(userId)) {
            Role role = systemService.getRoleById(Long.parseLong(userId));
            if (role != null) {
                result = Result.ok(role);
            } else {
                result = Result.fail("角色不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/role/getRoleByRoleCode")
    @AppLog("根据角色编码查询角色")
    public String getRoleByRoleCode() {
        Result.Builder result;
        String roleCode = getParameter("roleCode");
        if (!StringUtil.invalid(roleCode)) {
            Role role = systemService.getRoleByRoleCode(roleCode);
            if (role != null) {
                result = Result.ok(role);
            } else {
                result = Result.fail("角色不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/role/getRolePage")
    @AppLog("分页查询角色列表")
    public String getRolePage(@RequestBody(required = false) JSONObject search) {
        int pageNo = StringUtil.invalid(getParameter("pageNo")) ? 1 : Integer.parseInt(getParameter("pageNo"));
        int limit = StringUtil.invalid(getParameter("limit")) ? 10 : Integer.parseInt(getParameter("limit"));
        Page<Role> page = new Page<>(pageNo, limit);
        page.setSearch(search);
        page = systemService.getRolePage(page);
        return Result.ok(page).buildJsonString();
    }

    @PostMapping("/role/createRole")
    @AppLog("创建角色")
    public String createRole(@RequestBody Role role) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(role.getRoleName(), role.getRoleCode())) {
            if (systemService.getRoleByRoleCode(role.getRoleCode()) == null) {
                result = systemService.createRole(role) ? Result.ok(role) : Result.fail("创建失败");
            } else {
                result = Result.fail("角色编码已存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/role/updateRole")
    @AppLog("更新角色信息")
    public String updateRole(@RequestBody Role role) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(role.getRoleName(), role.getRoleCode())) {
            result = systemService.updateRole(role) ? Result.ok(role) : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/role/updateRoleStatus")
    @AppLog("更新角色状态")
    public String updateRoleStatus(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("roleId"), body.getString("status"))) {
            result = systemService.updateRoleStatus(body.getLong("roleId"), body.getInteger("status")) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/role/getRoleUsers")
    @AppLog("查询角色下的用户")
    public String getRoleUsers() {
        Result.Builder result;
        String roleId = getParameter("roleId");
        if (!StringUtil.invalid(roleId)) {
            List<User> users = systemService.getRoleUsers(Long.parseLong(roleId));
            result = Result.ok(users);
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    // ==================== 岗位管理 API ====================
    @GetMapping("/post/getPostById")
    @AppLog("查询岗位详情")
    public String getPostById() {
        Result.Builder result;
        String postId = getParameter("postId");
        if (!StringUtil.invalid(postId)) {
            Post post = systemService.getPostById(Long.parseLong(postId));
            if (post != null) {
                result = Result.ok(post);
            } else {
                result = Result.fail("岗位不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/post/getPostByPostCode")
    @AppLog("根据岗位编码查询岗位")
    public String getPostByPostCode() {
        Result.Builder result;
        String postCode = getParameter("postCode");
        if (!StringUtil.invalid(postCode)) {
            Post post = systemService.getPostByPostCode(postCode);
            if (post != null) {
                result = Result.ok(post);
            } else {
                result = Result.fail("岗位不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/post/getPostPage")
    @AppLog("分页查询岗位列表")
    public String getPostPage(@RequestBody(required = false) JSONObject search) {
        int pageNo = StringUtil.invalid(getParameter("pageNo")) ? 1 : Integer.parseInt(getParameter("pageNo"));
        int limit = StringUtil.invalid(getParameter("limit")) ? 10 : Integer.parseInt(getParameter("limit"));
        Page<Post> page = new Page<>(pageNo, limit);
        page.setSearch(search);
        page = systemService.getPostPage(page);
        return Result.ok(page).buildJsonString();
    }

    @PostMapping("/post/createPost")
    @AppLog("创建岗位")
    public String createPost(@RequestBody Post post) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(post.getPostName(), post.getPostCode())) {
            if (systemService.getPostByPostCode(post.getPostCode()) == null) {
                result = systemService.createPost(post) ? Result.ok(post) : Result.fail("创建失败");
            } else {
                result = Result.fail("岗位编码已存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/post/updatePost")
    @AppLog("更新岗位信息")
    public String updatePost(@RequestBody Post post) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(post.getPostName(), post.getPostCode())) {
            result = systemService.updatePost(post) ? Result.ok(post) : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/post/updatePostStatus")
    @AppLog("更新岗位状态")
    public String updatePostStatus(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("postId"), body.getString("status"))) {
            result = systemService.updatePostStatus(body.getLong("postId"), body.getInteger("status")) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    // ==================== 部门管理 API ====================
    @GetMapping("/department/getDepartmentById")
    @AppLog("查询部门详情")
    public String getDepartmentById() {
        Result.Builder result;
        String departmentId = getParameter("departmentId");
        if (!StringUtil.invalid(departmentId)) {
            Department department = systemService.getDepartmentById(Long.parseLong(departmentId));
            if (department != null) {
                result = Result.ok(department);
            } else {
                result = Result.fail("部门不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/department/getDepartmentByDeptCode")
    @AppLog("根据部门编码查询部门")
    public String getDepartmentByDeptCode() {
        Result.Builder result;
        String deptCode = getParameter("deptCode");
        if (!StringUtil.invalid(deptCode)) {
            Department department = systemService.getDepartmentByDeptCode(deptCode);
            if (department != null) {
                result = Result.ok(department);
            } else {
                result = Result.fail("部门不存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @GetMapping("/department/getDepartmentsByParentId")
    @AppLog("查询指定父部门的子部门")
    public String getDepartmentsByParentId() {
        Result.Builder result;
        String parentId = getParameter("parentId");
        if (!StringUtil.invalid(parentId)) {
            List<Department> departmentList = systemService.getDepartmentsByParentId(Long.parseLong(parentId));
            result = Result.ok(departmentList);
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/department/page")
    @AppLog("分页查询部门列表")
    public String getDepartmentPage(@RequestBody(required = false) JSONObject search) {
        int pageNo = StringUtil.invalid(getParameter("pageNo")) ? 1 : Integer.parseInt(getParameter("pageNo"));
        int limit = StringUtil.invalid(getParameter("limit")) ? 10 : Integer.parseInt(getParameter("limit"));
        Page<Department> page = new Page<>(pageNo, limit);
        page.setSearch(search);
        page = systemService.getDepartmentPage(page);
        return Result.ok(page).buildJsonString();
    }

    @PostMapping("/department/createDepartment")
    @AppLog("创建部门")
    public String createDepartment(@RequestBody Department department) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(department.getDeptName(), department.getDeptCode())) {
            if (systemService.getDepartmentByDeptCode(department.getDeptCode()) == null) {
                result = systemService.createDepartment(department) ? Result.ok(department) : Result.fail("创建失败");
            } else {
                result = Result.fail("部门编码已存在");
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/department/updateDepartment")
    @AppLog("更新部门信息")
    public String updateDepartment(@RequestBody Department department) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(department.getDeptName(), department.getDeptCode())) {
            result = systemService.updateDepartment(department) ? Result.ok(department) : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }

    @PostMapping("/department/updateDepartmentStatus")
    @AppLog("更新部门状态")
    public String updateDepartmentStatus(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("departmentId"), body.getString("status"))) {
            result = systemService.updateDepartmentStatus(body.getLong("departmentId"), body.getInteger("status")) ? Result.ok() : Result.fail("更新失败");
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }
    
    @GetMapping("/post/getPostList")
    @AppLog("查询所有岗位")
    public String getPostList() {
        List<Post> posts = systemService.getPostList();
        return Result.ok(posts).buildJsonString();
    }

    @GetMapping("/department/getDepartmentList")
    @AppLog("查询所有部门")
    public String getDepartmentList() {
        List<Department> departments = systemService.getDepartmentList();
        return Result.ok().setData(departments).buildJsonString();
    }

    @GetMapping("/department/getDepartmentTree")
    @AppLog("查询部门树形结构")
    public String getDepartmentTree() {
        List<Department> tree = systemService.buildDepartmentTree();
        return Result.ok(tree).buildJsonString();
    }
}

