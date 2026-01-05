package win.panyong.utils.authority;

import win.panyong.utils.AppCache;
import win.panyong.utils.StringUtil;

import java.util.Arrays;

public enum PermissionType {
    /*
        在配置文件中配置管理员用户id 例如: adminUserId=1,2,3
        系统中角色展示为ordinal值小的，逻辑中角色权限为合集
     */
    ADMIN("admin") {
        @Override
        public String[] getUserIdArray() {
            return AppCache.getConfigValue("adminId", "0").replaceAll("，", ",").split(",");
        }
    },
    USER("user") {
        public String[] getUserIdArray() {
            return AppCache.getConfigValue("userId", "0").replaceAll("，", ",").split(",");
        }
    },
    GUEST("guest") {
        @Override
        public String[] getUserIdArray() {
            return new String[0];
        }
    },
    ;

    private final String type;

    PermissionType(String type) {
        this.type = type;
    }

    public static PermissionType getPermissionTypeByUserId(Long userId) {
        if (userId != null) {
            return Arrays.stream(PermissionType.values()).filter(permissionType -> StringUtil.isHave(userId.toString(), permissionType.getUserIdArray())).findFirst().orElse(PermissionType.GUEST);
        } else {
            return PermissionType.GUEST;
        }
    }

    public abstract String[] getUserIdArray();

    public String getType() {
        return type;
    }
}
