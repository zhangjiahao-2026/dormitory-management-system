package com.example.springboot.controller;

import cn.hutool.core.io.FileUtil;
import com.example.springboot.common.Result;
import com.example.springboot.common.UID;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.DormManager;
import com.example.springboot.entity.Student;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.DormManagerService;
import com.example.springboot.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-dir:#{systemProperties['user.dir'] + '/springboot/src/main/resources/files/'}}")
    private String rootFilePath;

    @Resource
    private StudentService studentService;

    @Resource
    private AdminService adminService;

    @Resource
    private DormManagerService dormManagerService;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    /**
     * 将上传的头像写入本地 rootFilePath，返回重命名后的文件名
     */
    @PostMapping("/upload")
    public Result<?> upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("-1", "文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("-1", "文件大小不能超过2MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("-1", "文件名不合法");
        }

        String fileType = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_TYPES.contains(fileType)) {
            return Result.error("-1", "仅支持 jpg/png/gif/bmp/webp 格式");
        }

        String uid = new UID().produceUID();
        String newFilename = uid + fileType;
        String targetPath = rootFilePath + newFilename;

        log.info("文件上传: {} -> {}", originalFilename, newFilename);
        FileUtil.writeBytes(file.getBytes(), targetPath);

        return Result.success(newFilename);
    }

    /**
     * 将头像名称更新到学生数据库中
     */
    @PostMapping("/uploadAvatar/stu")
    public Result<?> uploadStuAvatar(@RequestBody Student student) {
        String avatar = student.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            // 防止路径穿越
            if (avatar.contains("..") || avatar.contains("/") || avatar.contains("\\")) {
                return Result.error("-1", "文件名不合法");
            }
            int i = studentService.updateNewStudent(student);
            if (i == 1) {
                return Result.success(avatar);
            }
        }
        return Result.error("-1", "设置头像失败");
    }

    @PostMapping("/uploadAvatar/admin")
    public Result<?> uploadAdminAvatar(@RequestBody Admin admin) {
        String avatar = admin.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.contains("..") || avatar.contains("/") || avatar.contains("\\")) {
                return Result.error("-1", "文件名不合法");
            }
            int i = adminService.updateAdmin(admin);
            if (i == 1) {
                return Result.success(avatar);
            }
        }
        return Result.error("-1", "设置头像失败");
    }

    @PostMapping("/uploadAvatar/dormManager")
    public Result<?> uploadDormManagerAvatar(@RequestBody DormManager dormManager) {
        String avatar = dormManager.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.contains("..") || avatar.contains("/") || avatar.contains("\\")) {
                return Result.error("-1", "文件名不合法");
            }
            int i = dormManagerService.updateNewDormManager(dormManager);
            if (i == 1) {
                return Result.success(avatar);
            }
        }
        return Result.error("-1", "设置头像失败");
    }

    /**
     * 前端调用接口，后端查询存储于本地的头像，进行Base64编码 发送到前端
     */
    @GetMapping("/initAvatar/{filename}")
    public Result<?> initAvatar(@PathVariable String filename) throws IOException {
        // 防止路径穿越
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Result.error("-1", "文件名不合法");
        }
        log.info("加载头像: {}", filename);
        String path = rootFilePath + filename;
        return Result.success(getImage(path));
    }

    private String getImage(String path) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(path);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] b = new byte[1024];
            int len;
            while ((len = fileInputStream.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            byte[] fileByte = bos.toByteArray();
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(fileByte);
        }
    }
}
