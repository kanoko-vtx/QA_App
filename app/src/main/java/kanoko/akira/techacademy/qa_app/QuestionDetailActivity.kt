package kanoko.akira.techacademy.qa_app

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.solver.widgets.Snapshot
import android.support.design.widget.DrawableUtils
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*
import kotlinx.android.synthetic.main.list_questions.view.*

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

        // ログインしているか
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていなければボタンを非表示
            val fav = findViewById(R.id.fav) as ImageView
            fav.setImageDrawable(null)
        } else {
            //お気に入りに入っているか確認
            // 質問IDを取得
            val questionuid = mQuestion.questionUid
            val userid = user?.uid
            val genre = mQuestion.genre.toInt()
            Log.d("qaapplog", "$userid")

            // 読み出し先のパスを指定
            var database = FirebaseDatabase.getInstance().getReference(FavlistPATH).child("$userid")

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val dbquid = dataSnapshot.value.toString()
                    if (questionuid in dbquid) {
                        // ボタンをオンにする
                        fav.setImageResource(R.drawable.fav_on)
                        // フラグを切り替え
                        favstatus = true
                    } else {
                        // ボタンをオフにする
                        fav.setImageResource(R.drawable.fav_off)
                        // フラグを切り替え
                        favstatus = false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.d("qaapplog", "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            })
        }

        // 質問追加ボタンの処理
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
                // データベースへ接続
                val database = FirebaseDatabase.getInstance()
                // お気に入りのパス
                val ref = database.getReference(FavlistPATH)
                // お気に入り画像を取得
                val fav = findViewById(R.id.fav) as ImageView
                // 読み書きする値を取得
                val questionuid = mQuestion.questionUid
                val userid = user?.uid
                val genre = mQuestion.genre.toInt()

                if (user == null) {
                    // ログインしていない
                    // ログイン画面へ飛ばす
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    // ログインしている
                    if (favstatus) {
                        // お気に入りから削除
                        ref.child("$userid").child("$questionuid").setValue(null)
                        // ボタンを切り替え
                        fav.setImageResource(R.drawable.fav_off)
                    } else {
                        // お気に入りに追加
                        ref.child("$userid").child("$questionuid").child("genre").setValue("$genre")
                        // ボタンを切り替え
                        fav.setImageResource(R.drawable.fav_on)
                    }
                }
            }
        })

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }

    override fun onResume() {
        super.onResume()

        // ログインしているか
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていなければボタンを非表示
            val fav = findViewById(R.id.fav) as ImageView
            fav.setImageDrawable(null)
        } else {
            //お気に入りに入っているか確認
            // 質問IDを取得
            val questionuid = mQuestion.questionUid
            val userid = user?.uid
            val genre = mQuestion.genre.toInt()

            // 読み出し先のパスを指定
            var database = FirebaseDatabase.getInstance().getReference(FavlistPATH).child("$userid").child("fav").child("$genre")

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val dbquid = dataSnapshot.value.toString()
                    if (questionuid in dbquid) {
                        // ボタンをオンにする
                        fav.setImageResource(R.drawable.fav_on)
                        // フラグを切り替え
                        favstatus = true
                    } else {
                        // ボタンをオフにする
                        fav.setImageResource(R.drawable.fav_off)
                        // フラグを切り替え
                        favstatus = false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.d("qaapplog", "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            })
        }
    }

}