package win.panyong.utils;


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
