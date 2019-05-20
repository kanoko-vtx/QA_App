package kanoko.akira.techacademy.qa_app

import android.R
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.content_main.*

class FavList : AppCompatActivity() {

    private val texts = arrayOf(
        "abc ", "bcd", "cde", "def", "efg",
        "fgh", "ghi", "hij", "ijk", "jkl", "klm"
    )

    data class Fav(var uid:String ="" , var cat:Int = 0)
//    private var favlists = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listView = ListView(this)
        setContentView(listView)

        // ログインしているか
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        val userid = user!!.uid
        // 読み出し先のパスを指定
        var database = FirebaseDatabase.getInstance().getReference(FavlistPATH).child("$userid")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (data in dataSnapshot.children) {
                    var catnum = data.value!!.toString()[7]
                    Log.d("qaapplog", "カテゴリ番号 $catnum")

                    // 読み出し先のパスを再指定 +カテゴリ番号
                    //var database2 = FirebaseDatabase.getInstance().getReference(FavlistPATH).child("$userid").child("$catnum")
                    //Log.d("qaapplog", database2.toString())

                    database.child("catnum").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.d("qaapplog", "2つめのリスナー")

                            var test = dataSnapshot.getValue(String::class.java)
                            Log.d("qaapplog", "$test")

                            for (data in dataSnapshot.children) {
                                Log.d("qaapplog", "ok")
                                Log.d("qaapplog", data.value!!.toString())
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d("qaapplog", "error")
                        }
                    })
                    Log.d("qaapplog", "カテゴリ番号取得ループ")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("qaapplog", "1st roop error")
            }
        })

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)

//        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dbquid)
        listView.setAdapter(arrayAdapter)

    }
}