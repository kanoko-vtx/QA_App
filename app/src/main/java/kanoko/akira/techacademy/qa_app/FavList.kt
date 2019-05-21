package kanoko.akira.techacademy.qa_app

import android.R
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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

    private var favlists = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        val userid = user!!.uid

        // カテゴリID、記事IDの取得
        var database = FirebaseDatabase.getInstance().getReference(FavlistPATH).child("$userid")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (data in dataSnapshot.children) {
                    Log.d("qaapplog", "$data")
                    var qid = data.key!!.toString()
                    var catnum = data.value!!.toString()[7]

                    // 取得したカテゴリID、記事IDを元に記事タイトルを取得
                    var database2 = FirebaseDatabase.getInstance().getReference(ContentsPATH).child("$catnum").child("$qid")
                    database2.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot2: DataSnapshot) {
                            var qtitle = dataSnapshot2.child("title").getValue().toString()
                            Log.d("qaapplog", "タイトル $qtitle")
                            // リストビュー用の配列に入れる
                            favlists.add("$qtitle")
                            Log.d("qaapplog","$favlists")
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        val listView = ListView(this)
        setContentView(listView)

        Handler().postDelayed(Runnable {
                Log.d("qaapplog","リストビューの前 $favlists")
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, favlists)
                listView.setAdapter(arrayAdapter)
        }, 1000)

    }

}
