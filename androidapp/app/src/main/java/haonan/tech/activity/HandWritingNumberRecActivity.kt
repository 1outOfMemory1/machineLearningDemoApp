package haonan.tech.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import haonan.tech.R

class HandWritingNumberRecActivity : AppCompatActivity() {


    companion object{
        fun actionStart(context: Context){
            val intent: Intent = Intent(context,HandWritingNumberRecActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hand_writing_number_rec)

    }
}