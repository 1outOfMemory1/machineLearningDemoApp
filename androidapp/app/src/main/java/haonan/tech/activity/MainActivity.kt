package haonan.tech.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import haonan.tech.R
import haonan.tech.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object{
        fun actionStart(context: Context){
            val intent: Intent = Intent(context,MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 猫狗识别
        binding.detectCatsAndDogsModuleBtn.setOnClickListener {
            Toast.makeText(this, "功能开发中 请联系管理员", Toast.LENGTH_SHORT).show()
            //DetectCatsDogsActivity.actionStart(this)
        }

        //手写数字识别
        binding.handWritingNumberRecModuleBtn.setOnClickListener {
            Toast.makeText(this, "功能开发中 请联系管理员", Toast.LENGTH_SHORT).show()
            //HandWritingNumberRecActivity.actionStart(this)
        }

        //人脸检测
        binding.DetectFaceModuleBtn.setOnClickListener {
            DetectFaceActivity.actionStart(this)
        }

    }
}