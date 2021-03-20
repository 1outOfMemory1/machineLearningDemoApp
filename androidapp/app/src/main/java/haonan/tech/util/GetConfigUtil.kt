package haonan.tech.util

import android.content.res.AssetManager
import org.json.JSONObject

class GetConfigUtil (var assets: AssetManager){

    fun getJsonConfig(): JSONObject {
        val fileContent = assets.open("config.json").bufferedReader().use { it.readText() }
        // 解析成json对象
        return JSONObject(fileContent)
    }
}