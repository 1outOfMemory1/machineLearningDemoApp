package haonan.tech.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import haonan.tech.util.GetConfigUtil
import haonan.tech.R
import haonan.tech.databinding.ActivityDetectCatsDogsBinding
import haonan.tech.util.imageUtil
import kotlinx.android.synthetic.main.activity_detect_cats_dogs.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
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


class DetectCatsDogsActivity : AppCompatActivity() {
    // 这个currentPhotoPath 很重要 存储绝对路径
    private var currentPhotoPath: String = ""
    //  配置文件是assets目录下的 config.json 文件
    private lateinit var baseUrl:String
    private val GALLERY = 1
    private val CAMERA = 0
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1
    private var lastClickTime = SystemClock.uptimeMillis()
    private var imageViewAccessBoolean:Boolean = false

    // 使用viewbinding 出错可能更小
    private lateinit var binding: ActivityDetectCatsDogsBinding

    companion object{
        fun actionStart(context: Context){
            val intent: Intent = Intent(context,DetectCatsDogsActivity::class.java)
            context.startActivity(intent)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_cats_dogs)
        binding = ActivityDetectCatsDogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonObject: JSONObject = GetConfigUtil(this.assets).getJsonConfig()
        baseUrl = jsonObject["baseUrl"] as String

        // 获取权限
        getRequirePermission()
        // 拍照按钮点击
        binding.takePhotoBtn.setOnClickListener {
            try {
                // 调用拍照intent  封装在下面这个函数中 这段代码需要看安卓的官方文档 我就改了几处地方
                dispatchTakePictureIntent()
            } catch (e: Exception) {
                Log.e("error", e.toString())
            }
        }
        // 服务器发送请求按钮点击
        binding.sendRequestBtn.setOnClickListener {
            if (!isFastDoubleClick()) {
                if (currentPhotoPath != "")
                    uploadPic(currentPhotoPath)
                else {
                    Toast.makeText(this, "清先通过拍照或者图库选择一张图片", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "请隔3秒后再次点击", Toast.LENGTH_SHORT).show()
            }


        }
        // 从相册中选择btn
        binding.selectFromGallaryBtn.setOnClickListener {
            openAlbum()
        }
    }


    /**
     * 获取权限 主要是摄像头和存储权限
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun getRequirePermission(){
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    REQUEST_WRITE_EXTERNAL_STORAGE)
        }else{
            Toast.makeText(this,"请给予摄像头和存储权限",Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * @param photoAbsolutePath
     * 图片的绝对地址
     */
    // 负责上传
    private fun uploadPic(photoAbsolutePath:String){
        //点击判断猫狗的时候 进行发送请求 使用okhttp 发送multipart/form-data类型的数据 也就是传输文件
        val file = File(photoAbsolutePath)
        binding.predictResultTextView.text = "未知"
        val client = OkHttpClient()

        val requestBody: RequestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file)
        val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM) //我觉得 第一个参数就相当于map的key 第二个参数就是文件的名字 第三个参数就是一个file
                .addFormDataPart("file", "aaaaa.jpg", requestBody)
                .build()

        val request: Request = Request.Builder()
                .url("${baseUrl}/upload/uploadPic")
                .post(multipartBody)
                .addHeader("content-type", "multipart/form-data")
                .build()
        val call: Call = client.newCall(request)
        call.enqueue(object : Callback {
            // 失败回调函数
            override fun onFailure(call: Call, e: java.io.IOException) {
                Looper.prepare();
                Toast.makeText(this@DetectCatsDogsActivity,"请求服务器失败了"  , Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
            // 成功回调函数
            override fun onResponse(call: Call, response: okhttp3.Response) {

                val tempString:String = response.body?.string()!!
                Log.e("abc", tempString)
                val jsonObject:JSONObject = JSONObject(tempString)
                // 这里必须使用 runOnUiThread 否则报错 因为不能在子线程更新ui 或者使用handler 但是我不会
                runOnUiThread {
                    binding.predictResultTextView.text = jsonObject["data"].toString()
                }
            }
        })

    }


    /**
     * 防止用户多次点击上传按钮 导致服务器压力过大
     */
    fun isFastDoubleClick(): Boolean {
        val time: Long = SystemClock.uptimeMillis() // 此方法仅用于Android
        if (time - lastClickTime < 3000) {
            return true
        }
        lastClickTime = time
        return false
    }

    /**
     * 打开相册选择照片
     */
    private fun openAlbum() {
        val openPhotoGalleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(openPhotoGalleryIntent, GALLERY)
    }

    /**
     * 打开摄像头
     */
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
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "haonan.tech.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA)
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

    // 读出存储的图片 分情况 如果requestCode 是 CAMERA(0) 说明是拍照完毕
    // 如果requestCode是GALLERY 那么是从图库选择照片完毕
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var pic : Bitmap? = null
        if (requestCode == CAMERA ){

            // 可能出现进相机没有拍照后退出的情况
            pic = BitmapFactory.decodeFile(currentPhotoPath)?:return
            binding.predictResultTextView.text ="未知"
        }else if (requestCode == GALLERY){
            try {
                val selectedImage: Uri = data?.data ?: return
                currentPhotoPath = getRealPathFromURI(this,selectedImage!!)!!
                val imageIS: InputStream? = this.getContentResolver().openInputStream(selectedImage!!)
                // 获取图片流
                pic = BitmapFactory.decodeStream(imageIS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 这里多做了一步 因为小米手机会拍照自动旋转90度 看着挺难受 所以改一改
        predictImage?.setImageBitmap(imageUtil.rotaingImageView(imageUtil.readPictureDegree(currentPhotoPath), pic!!))
        imageViewAccessBoolean = true
    }

    /**
     * 通过Uri参数获取绝对路径
     */
    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = context.contentResolver.query(contentURI, arrayOf(MediaStore.Images.ImageColumns.DATA),  //
                null, null, null)
        if (cursor == null) result = contentURI.path else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(index)
            cursor.close()
        }
        return result
    }

}