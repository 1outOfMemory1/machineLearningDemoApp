package tech.haonan.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.haonan.demo.entity.ResponseFormat;
import tech.haonan.demo.util.CommandUtil;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("upload")
public class uploadController {
    @PostMapping("/uploadPic")
    //@ApiOperation(value = "文件上传", notes = "文件上传")
    public ResponseFormat upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String filePath = "/data/www/default/torchImage/";
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));  // 后缀名
        //String newFileName = UUID.randomUUID().toString().replace("-", "") + suffixName;
        String newFileName = getFileName(suffixName);
        String fullPath = filePath + newFileName;
        File finallyFile = new File(fullPath);
        try {
            file.transferTo(finallyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = CommandUtil.run("/usr/local/anaconda3/envs/tf/bin/python3.8" +
                " /root/tfPython/detectCatsAndDogs/detectCatsAndDogs.py /data/www/default/torchImage/" + newFileName );
        result = result.trim();
        System.out.println(originalFilename + "上传成功\n" + "网址是 https://haonan.tech/torchImage/"+ newFileName);
        return  new ResponseFormat(200, originalFilename + "上传成功\n" + "网址是 https://haonan.tech/torchImage/"+ newFileName, result) ;
    }

    private String getFileName(String suffixName){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        int max=100,min=1;
        long randomNum = System.currentTimeMillis();
        int ran3 = (int) (randomNum%(max-min)+min);

        String newFileName = df.format(new Date()) + ran3 + "." + suffixName;
        return newFileName;
    }

}
