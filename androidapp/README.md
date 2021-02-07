### 项目简介

1. 调用摄像头拍照 然后将图片存储到/storage/emulated/0/Android/data/你的包名/files/Pictures目录下 格式是jpg
2. 然后点击判断按钮 向后端服务器发送multipart/form-data 类型的数据 读取上边存储的jpg格式文件  将他发送到服务器验证 ,请求的body中需要的信息如下所示(使用postman模拟后端传值)
3. ![image-20210207122421097](http://tuchuang1234.oss-cn-shenzhen.aliyuncs.com/typora/202102/07/122423-336265.png)

4. key 是 file   value就是文件  得到的返回值是json形式  例如

```json
{
    "code": 200,
    "message": "识别成功",
    "data": "猫"
}
```



