package kanoko.akira.techacademy.qa_app

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.DrawableUtils
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    // お気に入り用のフラグ 初期値はfalse
    private var favstatus: Boolean = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        // お気に入りボタンの処理
        fav.setOnClickListener(object:View.OnClickListener {
            override fun onClick(v: View?) {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    // ログインしていたらお気に入りボタン処理
                    val fav = findViewById(R.id.fav) as ImageView
                    if (favstatus) {
                        // お気に入りから削除
                        // データベースへ接続
                        val database = FirebaseDatabase.getInstance()
                        val ref = database.getReference(FavlistPATH)

                        // 削除
                        var questionuid = mQuestion.questionUid
                        var user = user.uid

                        Log.d("qaapplog","userid : $user")
                        Log.d("qaapplog","questuibud ; $questionuid")

                        ref.child("$user").child("fav").child("$questionuid").setValue(null)

                        // ボタンを切り替え
                        fav.setImageResource(R.drawable.fav_off)
                        // フラグを切り替え
                        favstatus = false
                    } else {
                        // お気に入りに追加
                        // ボタンを切り替え
                        fav.setImageResource(R.drawable.fav_on)
                        // フラグを切り替え
                        favstatus = true

                        // データベースへ接続
                        val database = FirebaseDatabase.getInstance()
                        val ref = database.getReference(FavlistPATH)

                        // 書き込み
                        var questionuid = mQuestion.questionUid
                        var user = user.uid

                        Log.d("qaapplog","userid : $user")
                        Log.d("qaapplog","questuibud ; $questionuid")

                        ref.child("$user").child("fav").child("$questionuid").setValue(questionuid)
//                        ref.child("$user").child("fav").push().child("quid").setValue(questionuid)
//                        ref.child("$user/fav").child("uqid").push().setValue(questionuid)
                    }
                }
            }
        }
        )

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
            mAnswerRef.addChildEventListener(mEventListener)
    }
}