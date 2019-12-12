package com.example.kotlin.a02_4_databinding

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.example.kotlin.a02_4_databinding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val myName: MyName = MyName("Aleks Haecky")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        findViewById<Button>(R.id.done_button).setOnClickListener {
//            addNickname(it)
//        }

//        findViewById<TextView>(R.id.nickname_text).setOnClickListener {
//            updateNickName(it)
//        }

        binding.myName = myName

        binding.doneButton.setOnClickListener{
            addNickname(it)
        }

        binding.nicknameText.setOnClickListener {
            updateNickName(it)
        }

    }

    private fun addNickname(view: View) {
        // val editText = findViewById<EditText>(R.id.nickname_edit)
        // val nicknameTextView = findViewById<TextView>(R.id.nickname_text)

        val editText = binding.nicknameEdit
        val nicknameTextView = binding.nicknameText


        /**
         * 코틀린스러운 코드로 변경
         */

        /*
            nicknameTextView.text = editText.text.toString() // editText.text는 Editable 형식이다. 데이터 바인딩을 사용할 때는 Editable을 명시적으로 String으로 변환해야 한다.
            editText.visibility = View.GONE
            // view.visibility = View.GONE // done 버튼 visibility gone
            binding.doneButton.visibility = View.GONE
            nicknameTextView.visibility = View.VISIBLE
        */

        binding.apply {
            // nicknameText.text = nicknameEdit.text.toString()
            myName?.nickname = nicknameEdit.text.toString()

            nicknameEdit.visibility = View.GONE
            doneButton.visibility = View.GONE
            nicknameText.visibility = View.VISIBLE

            invalidateAll()
        }

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateNickName(view: View) {
        // val editText = findViewById<EditText>(R.id.nickname_edit)
        // val doneButton = findViewById<Button>(R.id.done_button)

        val editText = binding.nicknameEdit
        val doneButton = binding.doneButton

        /*
            editText.visibility = View.VISIBLE
            doneButton.visibility = View.VISIBLE
            view.visibility = View.GONE
        */

        binding.apply {
            editText.visibility = View.VISIBLE
            doneButton.visibility = View.VISIBLE
            nicknameText.visibility = View.GONE
        }

        editText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
