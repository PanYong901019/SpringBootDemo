package win.panyong.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import win.panyong.mapper.sqlite.SqliteSystemMapper;
import win.panyong.model.*;
import win.panyong.utils.AppException;
import win.panyong.utils.Page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class SystemService extends BaseService {


    // ==================== 用户相关服务 ====================
    public User getUserById(Long id) {
        return systemMapper.selectUserById(id);
    }

    public User getUserByUsername(String username) {
        return systemMapper.selectUserByUsername(username);
    }

    public Page<User> getUserPage(Page<User> page) {
        page.setTotalCount(systemMapper.countUsersByKeyword(page.getSearch()));
        page.setData(systemMapper.selectUsersByKeyword(page.getSearch(), page.getSince(), page.getLimit()));
        return page;
    }

    @Transactional
    public boolean createUser(User user) {
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return systemMapper.insertUser(user) > 0;
    }

    @Transactional
    public boolean updateUser(User user) {
        user.setUpdateTime(new Date());
        return systemMapper.updateUser(user) > 0;
    }

    @Transactional
    public boolean updateUserStatus(Long id, Integer status) {
        return systemMapper.updateUserStatus(id, status, new Date()) > 0;
    }

    @Transactional
    public boolean changeUserPassword(Long id, String oldPassword, String newPassword) {
        User user = systemMapper.selectUserById(id);
        if (user.getPassword().equals(oldPassword)) {
            return systemMapper.updateUserPassword(id, newPassword, new Date()) > 0;
        } else {
            throw new AppException("旧密码错误");
        }
    }

    @Transactional
    public boolean resetUserPassword(Long id, String newPassword) {
        return systemMapper.updateUserPassword(id, newPassword, new Date()) > 0;
    }

    public List<Role> getUserRoles(Long userId) {
        return systemMapper.selectRolesByUserId(userId);
    }

    @Transactional
    public boolean changeUserRoles(Long userId, List<Long> roleIds) {
        systemMapper.deleteUserRolesByUserId(userId);
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole(userId, roleId);
            systemMapper.insertUserRole(userRole);
        }
        return true;
    }

    // ==================== 角色相关服务 ====================
    public Role getRoleById(Long id) {
        return systemMapper.selectRoleById(id);
    }

    public Role getRoleByRoleCode(String roleCode) {
        return systemMapper.selectRoleByRoleCode(roleCode);
    }

    public Page<Role> getRolePage(Page<Role> page) {
        page.setTotalCount(systemMapper.countRolesByKeyword(page.getSearch()));
        page.setData(systemMapper.selectRolesByKeyword(page.getSearch(), page.getSince(), page.getLimit()));
        return page;
    }

    @Transactional
    public boolean createRole(Role role) {
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        return systemMapper.insertRole(role) > 0;
    }

    @Transactional
    public boolean updateRole(Role role) {
        role.setUpdateTime(new Date());
        return systemMapper.updateRole(role) > 0;
    }

    @Transactional
    public boolean updateRoleStatus(Long id, Integer status) {
        return systemMapper.updateRoleStatus(id, status, new Date()) > 0;
    }

    public List<User> getRoleUsers(Long roleId) {
        return systemMapper.selectUsersByRoleId(roleId);
    }

    // ==================== 岗位相关服务 ====================
    public Post getPostById(Long id) {
        return systemMapper.selectPostById(id);
    }

    public Post getPostByPostCode(String postCode) {
        return systemMapper.selectPostByPostCode(postCode);
    }

    public List<Post> getPostList() {
        return systemMapper.selectPostList();
    }

    public Page<Post> getPostPage(Page<Post> page) {
        page.setTotalCount(systemMapper.countPostsByKeyword(page.getSearch()));
        page.setData(systemMapper.selectPostsByKeyword(page.getSearch(), page.getSince(), page.getLimit()));
        return page;
    }

    @Transactional
    public boolean createPost(Post post) {
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        return systemMapper.insertPost(post) > 0;
    }

    @Transactional
    public boolean updatePost(Post post) {
        post.setUpdateTime(new Date());
        return systemMapper.updatePost(post) > 0;
    }

    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        return systemMapper.updatePostStatus(id, status, new Date()) > 0;
    }

    // ==================== 部门相关服务 ====================
    public Department getDepartmentById(Long id) {
        return systemMapper.selectDepartmentById(id);
    }

    public Department getDepartmentByDeptCode(String deptCode) {
        return systemMapper.selectDepartmentByDeptCode(deptCode);
    }

    public List<Department> getDepartmentList() {
        return systemMapper.selectDepartmentList();
    }

    public List<Department> getDepartmentsByParentId(Long parentId) {
        return systemMapper.selectDepartmentsByParentId(parentId);
    }

    public Page<Department> getDepartmentPage(Page<Department> page) {
        page.setTotalCount(systemMapper.countDepartmentsByKeyword(page.getSearch()));
        page.setData(systemMapper.selectDepartmentsByKeyword(page.getSearch(), page.getSince(), page.getLimit()));
        return page;
    }

    @Transactional
    public boolean createDepartment(Department department) {
        buildAncestors(department);
        department.setCreateTime(new Date());
        department.setUpdateTime(new Date());
        return systemMapper.insertDepartment(department) > 0;
    }

    @Transactional
    public boolean updateDepartment(Department department) {
        buildAncestors(department);
        department.setUpdateTime(new Date());
        return systemMapper.updateDepartment(department) > 0;
    }

    @Transactional
    public boolean updateDepartmentStatus(Long id, Integer status) {
        return systemMapper.updateDepartmentStatus(id, status, new Date()) > 0;
    }

    public List<Department> buildDepartmentTree() {
        List<Department> departmentList = getDepartmentList();
        return buildTree(departmentList, 0L);
    }

    // ==================== 私有辅助方法 ====================
    private void buildAncestors(Department department) {
        if (department.getParentId() != null && department.getParentId() != 0) {
            Department parent = getDepartmentById(department.getParentId());
            if (parent != null) {
                String ancestors = parent.getAncestors() == null ? "" : parent.getAncestors();
                if (!ancestors.isEmpty()) {
                    ancestors += ",";
                }
                ancestors += parent.getId();
                department.setAncestors(ancestors);
            }
        } else {
            department.setAncestors("");
        }
    }

    private List<Department> buildTree(List<Department> departmentList, Long parentId) {
        List<Department> tree = new ArrayList<>();
        for (Department dept : departmentList) {
            if (Objects.equals(dept.getParentId(), parentId)) {
                List<Department> children = buildTree(departmentList, dept.getId());
                tree.add(dept);
            }
        }
        return tree;
    }
}
