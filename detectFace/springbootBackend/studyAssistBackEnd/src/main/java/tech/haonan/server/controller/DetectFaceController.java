package tech.haonan.server.controller;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.haonan.server.entity.CommonResponse;
import tech.haonan.server.util.HttpClientUtils;

import java.io.File;
import java.util.UUID;

@RestController
@Api(tags = "人脸检测专用接口")
@RequestMapping("faceDetect")
public class DetectFaceController {

    @PostMapping("uploadAndDetect")
    @ApiOperation(value = "人脸识别专用")

    public CommonResponse uploadAndDetect(@ApiParam(value = "上传图片文件", required = true)   @RequestPart("file") MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        String filePath = "/data/www/default/torchImage/";
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));  // 后缀名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffixName;
        String fullPath = filePath + newFileName;
        File finallyFile = new File(fullPath);

        try {
            file.transferTo(finallyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jo = new JSONObject();
        jo.put("fileLocation", fullPath);
        String result = HttpClientUtils.doPost("http://127.0.0.1:9000/uploadPic",jo);
        if (result != null && !result.equals("")){
            System.out.println(originalFilename + "上传成功\n" + "网址是 https://haonan.tech/torchImage/"+ newFileName);
            return  new CommonResponse(200, "识别成功", result) ;
        }else {
            System.out.println(originalFilename + "上传成功\n" + "网址是 https://haonan.tech/torchImage/"+ newFileName);
            return  new CommonResponse(400, "识别失败", "识别失败") ;
        }
    }

}
