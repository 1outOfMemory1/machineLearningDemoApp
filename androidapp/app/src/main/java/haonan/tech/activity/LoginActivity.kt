package haonan.tech.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import haonan.tech.R
import haonan.tech.databinding.ActivityLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.create
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide();

        setContentView(R.layout.activity_main)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var username:String
        var password:String
        var verifyCode:String
        // 第一次先请求验证码 firstStatus是true
        getVerifyPicAndGetCookie("http://haonan.tech:9999/captcha",true)

        binding.verifyCodeImageView.setOnClickListener {
            //点击验证码之后再次请求获取新验证码 此时cookie已经获取了 携带cookie获取的验证码 后台session不重新创建 而是刷新内容
            getVerifyPicAndGetCookie("http://haonan.tech:9999/captcha",false)
        }

        // 登录按钮 点击事件
        binding.loginBtn.setOnClickListener {
            username = binding.usernameEditText.text.toString()
            password = binding.passwordEditText.text.toString()
            verifyCode = binding.verifyCodeEditText.text.toString()
            val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
            var abc:String?
            abc = sharedPreferences.getString("cookie","null")
            login("http://haonan.tech:9999/login",abc!!,username,password,verifyCode)
        }
        // 免密登录测试按钮
        binding.noPasswordBtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("tokenHead","aaaa")
            editor.putString("tokenHeader","mytoken")
            editor.putString("token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImNyZWF0ZWRfdGltZSI6IkZyaSBNYXIgMTkgMTI6NTQ6MDggQ1NUIDIwMjEiLCJleHAiOjE2NDc2NTk2NDh9.zSNDeCKLx0OQhDF_W55ZsgT3XSPLIojy3OU_GC3a9PQ")
            editor.apply()
            // 启动主activity
            MainActivity.actionStart(this@LoginActivity)
        }

    }

    /**
     *  获取验证码并且拿到cookie  注意这个firstStatus 如果是首次获取验证码  那么为true 否则为false m
     *  做firstStatus 的主要目的是不进行多次请求 让服务器开启太多无用的session 减少服务器消耗
     *  当第二次带着cookie进行请求的时候 response字段中就不包含 set-cookie 字段
     */
    fun getVerifyPicAndGetCookie(url:String,firstStatus:Boolean) {
        val client = OkHttpClient()
        var request:Request? = null
        if (firstStatus == true){
             request = Request.Builder().url(url).get().build()
        }else{
            val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
            var cookieStr:String?
            cookieStr = sharedPreferences.getString("cookie","null")
            request = Request.Builder().addHeader("Cookie",cookieStr!!).url(url).get().build()
        }
        val call:Call = client.newCall(request!!)
        call.enqueue(object : Callback {
            // 失败回调函数
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("abc","访问服务器失败")
            }
            // 成功回调函数
            override fun onResponse(call: Call, response: okhttp3.Response) {
                var bitmap:Bitmap? = null
                if (firstStatus == true){
                    val setCookieList: List<String> = response.headers.values("Set-Cookie")
                    val setCookieStr:String = setCookieList.get(0)
                    val abc = setCookieStr.substring(0, setCookieStr.indexOf(";"))

                    Log.e("abc",abc)
                    val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("cookie",abc)
                    editor.apply()
                }

                val Picture_bt = response.body!!.bytes()
                bitmap = BitmapFactory.decodeByteArray(Picture_bt, 0, Picture_bt.size)
                //使用BitmapFactory工厂，把字节数组转化为bitmap
                // 这里必须使用 runOnUiThread 否则报错 因为不能在子线程更新ui 或者使用handler 但是我不会
                runOnUiThread {
                    binding.verifyCodeImageView.setImageBitmap(bitmap)
                }
            }
        })

    }

    fun login(url:String,cookieStr:String,username:String,password:String,verifyCode:String){
        // 防止空字符串登录 提示用户输入完整
        if(username=="" || password =="" || verifyCode ==""){
            Toast.makeText(this@LoginActivity, "请填写信息完整后重试", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient().newBuilder()
                .build()
        val mediaType = "application/json".toMediaTypeOrNull()
        val body: RequestBody = create(mediaType, "{\r\n  \"password\": \"${password}\",\r\n  \"username\": \"${username}\",\r\n  \"verificationCode\": \"${verifyCode}\"\r\n}")
        val request: Request = Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Cookie", cookieStr)
                .addHeader("Content-Type", "application/json")
                .build()
        val call:Call = client.newCall(request)
        call.enqueue(object : Callback {
            // 失败回调函数
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("abc","访问服务器失败")
            }
            // 成功回调函数
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val resStr :String= response.body?.string()!!
                Log.e("abc", resStr)
                //{"responseCode":200,"message":"登录成功","data":{"tokenHead":"aaaa","tokenHeader":"mytoken","token":"我是token"}
                val loginObject: JSONObject = JSONObject(resStr)
                runOnUiThread {
                    // 如果返回值为200 说明登录成功了 需要存储 token作为访问接口的凭证
                    if (loginObject["responseCode"].toString().equals("200")){
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        val tokens: JSONObject = loginObject.get("data") as JSONObject
                        val tokenHead:String = tokens.get("tokenHead").toString()
                        val tokenHeader:String = tokens.get("tokenHeader").toString()
                        val token:String = tokens.get("tokenHead").toString()
                        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("tokenHead",tokenHead)
                        editor.putString("tokenHeader",tokenHeader)
                        editor.putString("token",token)
                        editor.apply()
                        // 启动主activity
                        MainActivity.actionStart(this@LoginActivity)
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "登录失败，" + loginObject["message"].toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}