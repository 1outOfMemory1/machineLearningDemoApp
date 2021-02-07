package haonan.tech

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.create
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/*
项目介绍
调用摄像头拍照 然后将图片存储到/storage/emulated/0/Android/data/你的包名/files/Pictures目录下 格式是jpg
然后点击判断按钮 向后端服务器发送multipart/form-data 类型的数据 读取上边存储的jpg格式文件  将他发送到服务器验证
key 是 file   value就是文件
得到的返回值是json形式  例如
{
    "code": 200,
    "message": "识别成功",
    "data": "猫"
}
*/


class MainActivity : AppCompatActivity() {

    // 这个currentPhotoPath 很重要 他是
    private  var currentPhotoPath: String = ""
    // 暂时还没有用上 配置文件是assets目录下的 config.json 文件
    private lateinit var baseUrl:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val jsonObject: JSONObject = GetConfig(this.assets).getJsonConfig()
        baseUrl = jsonObject["baseUrl"] as String
        // 拍照按钮点击
        takePhotoBtn.setOnClickListener {
            try {
                // 调用拍照intent  封装在下面这个函数中 这段代码需要看安卓的官方文档 我就改了几处地方
                dispatchTakePictureIntent()
            } catch (e: Exception) {
                Log.e("error",e.toString())
            }
        }


        // 服务器发送请求按钮点击
        sendRequestBtn.setOnClickListener {

            //点击判断猫狗的时候 进行发送请求 使用okhttp 发送multipart/form-data类型的数据 也就是传输文件
            predictResultTextView.text = "未知"
            val client = OkHttpClient()
            val file = File(currentPhotoPath)

            val requestBody: RequestBody = create("application/octet-stream".toMediaTypeOrNull(), file)
            val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM) //我觉得 第一个参数就相当于map的key 第二个参数就是文件的名字 第三个参数就是一个file
                    .addFormDataPart("file", "aaaaa.jpg", requestBody)
                    .build()

            val request: Request = Request.Builder()
                    .url("http://haonan.tech:9999/upload/uploadPic")
                    .post(multipartBody)
                    .addHeader("content-type", "multipart/form-data")
                    .build()
            val call:Call = client.newCall(request)
            call.enqueue(object : Callback {
                // 失败回调函数
                override fun onFailure(call: Call, e: java.io.IOException) {
                    Looper.prepare();
                    Toast.makeText(this@MainActivity,"请求服务器失败了"  , Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
                // 成功回调函数
                override fun onResponse(call: Call, response: okhttp3.Response) {

                    val tempString:String = response.body?.string()!!
                    Log.e("abc", tempString)
                    val jsonObject:JSONObject = JSONObject(tempString)
                    // 这里必须使用 runOnUiThread 否则报错 因为不能在子线程更新ui 或者使用handler 但是我不会
                    runOnUiThread {
                        predictResultTextView.text = jsonObject["data"].toString()
                    }
                }
            })


        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager).also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = getUriForFile(
                            this,
                            "haonan.tech.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 1)
                }
            }

        }
    }



    // 创建图像 这个jpg图像创建出来是0kb的 相机拍好的数据存储到这个jpg文件中
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        // 图片的名称不会重复 因为文件名例如 JPEG_20210206_211957_7715232737871605626.jpg
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // 读出存储的图片 并将
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val pic:Bitmap = BitmapFactory.decodeFile(currentPhotoPath)
        predictImage?.setImageBitmap(pic)
        predictResultTextView.text ="未知"
    }


}

/*
<files-path/> --> Context.getFilesDir()
<cache-path/> --> Context.getCacheDir()
<external-path/> --> Environment.getExternalStorageDirectory()
<external-files-path/> --> Context.getExternalFilesDir(String)
<external-cache-path/> --> Context.getExternalCacheDir()
<external-media-path/> --> Context.getExternalMediaDirs()
* */