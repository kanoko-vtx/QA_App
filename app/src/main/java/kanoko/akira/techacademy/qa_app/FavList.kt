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

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val listView = ListView(this)
            setContentView(listView)

            // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
            val arrayAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1, texts
            )

            listView.setAdapter(arrayAdapter)
        }
}
