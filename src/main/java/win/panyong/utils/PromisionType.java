package win.panyong.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pan on 2019/4/20 12:22 PM
 */
public enum PromisionType {
    /*
        在配置文件中配置权限 配置方法是定义权限名字和授权id  例如: adminUserIds=1,2,3,4,5,6
     */
    ADMIN("admin") {
        @Override
        public List<Integer> getUserIdList() {
            return Arrays.stream(AppCache.getSystemConfig().get("adminUserIds").split(",")).map(Integer::valueOf).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
        }
    },
    KEFU("kefu") {
        @Override
        public List<Integer> getUserIdList() {
            List<Integer> list = Arrays.stream(AppCache.getSystemConfig().get("kefuUserIds").split(",")).map(Integer::valueOf).collect(Collectors.toList());
            list.addAll(Arrays.stream(AppCache.getSystemConfig().get("adminUserIds").split(",")).map(Integer::valueOf).collect(Collectors.toList()));
            return list.stream().distinct().collect(Collectors.toList());
        }
    };

    private String type;

    PromisionType(String type) {
        this.type = type;
    }

    public static String getPromisionTypeByUserId(Integer userId) {
        for (PromisionType promisionType : PromisionType.values()) {
            if (promisionType.getUserIdList().stream().anyMatch(integer -> integer.equals(userId))) {
                return promisionType.type;
            }
        }
        return "common";
    }

    public abstract List<Integer> getUserIdList();

}
