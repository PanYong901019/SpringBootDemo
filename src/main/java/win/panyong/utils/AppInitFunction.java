package win.panyong.utils;

/**
 * Created by pan on 2019/1/29 2:52 PM
 */
public class AppInitFunction {
    private static volatile AppInitFunction instance = null;

    private AppInitFunction() {
    }

    public static AppInitFunction getInstance() {
        if (instance == null) {
            synchronized (AppInitFunction.class) {
                if (instance == null) {
                    instance = new AppInitFunction();
                }
            }
        }
        return instance;
    }

    public String getTextInfo() throws AppException {
        return "success";
    }
}
