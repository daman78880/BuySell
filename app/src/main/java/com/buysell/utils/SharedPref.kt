import android.content.Context
import android.content.SharedPreferences

class SharedPref(private var context: Context) {
    private var pref: SharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)


    fun saveString(key: String, value: String?) {
        var data=""
        if(value!=null){
            data=value
        }
        pref.edit().putString(key, data).apply()
    }
    fun clearData() {
        pref.edit().clear().apply()
    }


    fun getString(key: String): String {
        return pref.getString(key, "").toString()
    }

    fun saveInt(key: String, value: Int?) {
        var data=-1
        if(value!=null){
            data=value
        }
        pref.edit().putInt(key, data).apply()
    }

    fun saveLong(key: String, value: Long) {
        pref.edit().putLong(key, value).apply()
    }

    fun saveFloat(key: String, value: Float) {
        pref.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String): Float {
        return pref.getFloat(key, 0.00f)
    }


    fun getLong(key: String): Long {
        return pref.getLong(key, 0)
    }

    fun getInt(key: String): Int {
        return pref.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return pref.getBoolean(key, false)
    }

    fun saveBoolean(key: String, value: Boolean?) {
        var data=false
        if(value!=null){
            data=value
        }
        pref.edit().putBoolean(key, data).apply()
    }
}