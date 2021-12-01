package life.zswork.uploader.controller;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("drawing")
@Slf4j
public class DrawingBedController {

    @Resource
    private FastFileStorageClient client;

    @PostMapping("upload")
    public ResponseEntity<String> upload(MultipartFile file) {
        //校验文件类型
        String contentType = file.getContentType();
        //校验文件内容
        StorePath storePath;
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            storePath = client.uploadFile(file.getInputStream(), file.getSize(), extension, null);            //返回路径                return prop.getBaseUrl()+ storePath.getFullPath();
        } catch (IOException e) {
            return ResponseEntity.status(400).body("fail");
        }
        return ResponseEntity.ok(storePath.getFullPath());
    }
}
