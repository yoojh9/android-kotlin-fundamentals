package com.example.kotlin.a01_diceroller

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // 실제로 views는 onCreate() 메소드에서 setContentView()로 inflicate 되어야 메모리 객체에 액세스 할 수 있다.
    // inflicate 되기 전에는 diceImage를 초기화 시킬 수 없다.
    // 하지만 이렇게 nullable 변수로 정의히게 되면 매번 diceImage 변수를 사용할 때 마다 null체크를 해야한다.
    // var diceImage: ImageView? = null


    // 위의 선언보다 lateinit 키워드를 사용하면 null 할당을 지울 수 있다.
    // lateinit 어떤 연산에 의해 코드가 호출되기 전에 변수가 초기화 될 거라고 코틀린 컴파일러에게 약속하는 키워드이다.
    // 그러므로 null 초기화가 필요 없고 non-nullable 변수처럼 다룰 수 있다.
    lateinit var diceImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        diceImage = findViewById(R.id.dice_image)
        val rollButton: Button = findViewById(R.id.roll_button)
        rollButton.setOnClickListener { rollDice() }
    }


    private fun rollDice(){
        val randomInt = java.util.Random().nextInt(6)+1
        Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()

        // 여기에서 findViewById를 하면 onClick()을 할 때 마다 호출이 됨.
        // 안드로이드 시스템은 매번 전체 뷰 계층 구조를 다 검색하므로 비용이 많이 든다.
        // val diceImage: ImageView = findViewById(R.id.dice_image)

        val drawableResource = when(randomInt) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        diceImage.setImageResource(drawableResource)
    }
}
