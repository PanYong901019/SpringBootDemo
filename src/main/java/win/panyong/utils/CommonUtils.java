package win.panyong.utils;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pan on 2019/8/29 10:21 AM
 */
public class CommonUtils {

    public static File uploadFile(String filePath, MultipartFile multipartFile) throws IOException {
        String fileFullPath = new ApplicationHome(CommonUtils.class) + "/files/fileupload" + filePath;
        File file = new File(fileFullPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        multipartFile.transferTo(file);
        return file;
    }

    public static File exportFile(String filePath, byte[] bytes) throws IOException {
        String fileFullPath = new ApplicationHome(CommonUtils.class) + "/files/fileExport" + filePath;
        File file = new File(fileFullPath);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(file); BufferedOutputStream buff = new BufferedOutputStream(fileOutputStream)) {
                buff.write(bytes);
                buff.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return file;
    }
}
