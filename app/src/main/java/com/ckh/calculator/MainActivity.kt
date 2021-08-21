package com.ckh.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.ckh.calculator.databinding.ActivityMainBinding
import com.ckh.calculator.databinding.HistoryRowBinding
import com.ckh.calculator.model.History
import java.lang.NumberFormatException
import kotlin.math.exp

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val expressionView = binding.expressionTextView
    private val resultView = binding.resultTextView
    private var historyLayout = binding.historyLayout
    private var historyLinearLayout = binding.historyLinearLayout

    private var isOperator = false
    private var hasOperator = false

    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()

    }


    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.btn0 -> numberBtn("0")
            R.id.btn1 -> numberBtn("1")
            R.id.btn2 -> numberBtn("2")
            R.id.btn3 -> numberBtn("3")
            R.id.btn4 -> numberBtn("4")
            R.id.btn5 -> numberBtn("5")
            R.id.btn6 -> numberBtn("6")
            R.id.btn7 -> numberBtn("7")
            R.id.btn8 -> numberBtn("8")
            R.id.btn9 -> numberBtn("9")
            R.id.plusBtn -> operatorBtn("+")
            R.id.minusBtn -> operatorBtn("-")
            R.id.multipleBtn -> operatorBtn("*")
            R.id.devideBtn -> operatorBtn("/")
            R.id.devidedBtn -> operatorBtn("%")
        }
    }

    private fun numberBtn(number: String) {
        if (isOperator) {
            expressionView.append(" ")
        }
        isOperator = false
        val expressionNumber = expressionView.text.split(" ")
        if (expressionNumber.isNotEmpty() && expressionNumber.last().length > 15) {
            Toast.makeText(this, "15자리만 입력가능합니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (number == "0" && expressionNumber.last().isEmpty()) {
            return
        }
        expressionView.append(number)
        resultView.text = calculate()
    }

    private fun operatorBtn(operator: String) {
        if (expressionView.text.isEmpty()) {
            return
        }
        when {
            isOperator -> {
                val text = expressionView.text.toString()
                expressionView.text = text.dropLast(1) //맨끝에서 한자리를 지워줌
            }
            hasOperator -> {
                Toast.makeText(this, "연산자는 연속으로 사용 불가능합니다", Toast.LENGTH_SHORT).show()
            }
            else -> {
                expressionView.append("${operator}")
            }
        }
        val ssb = SpannableStringBuilder(expressionView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            expressionView.text.length - 1, expressionView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        expressionView.text = ssb
        isOperator = true
        hasOperator = true
    }

    fun clearClicked(v: View) {
        expressionView.text = ""
        resultView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun historyClicked(v: View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val histroryView = HistoryRowBinding.inflate(layoutInflater)
                    histroryView.expressionTextView.text = it.expression
                    histroryView.resultTextView.text = " =it.result"
                    historyLinearLayout.addView(histroryView.root)
                }
            }

        }).start()

    }

    fun historyClearBtn(v: View) {
        historyLinearLayout.removeAllViews()
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()
    }

    fun closeHistoryBtn(v: View) {
        historyLayout.isVisible = false
    }

    fun resultClicked(v: View) {
        val expressionText = expressionView.text.split(" ")
        if (expressionView.text.isEmpty() || expressionText.size == 1) {
            return
        }
        if (expressionText.size != 3 && hasOperator) {
            Toast.makeText(this, "완성된 수식이 아닙니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (expressionText[0].isNumber().not() || expressionText[2].isNumber().not()) {
            Toast.makeText(this, "정확한 수식이 아닙니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val expressionTexted = expressionView.text.toString()
        val resultText = calculate()

        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionTexted, resultText))
        }).start()

        resultView.text = ""
        expressionView.text = resultText
        isOperator = false
        hasOperator = false
    }

    private fun calculate(): String {
        val expressionText = expressionView.text.split(" ")
        if (hasOperator.not() || expressionText.size != 3) {
            return ""
        } else if (expressionText[0].isNumber().not() || expressionText[2].isNumber().not()) {
            return ""
        }
        val exp1 = expressionText[0].toBigInteger()
        val exp2 = expressionText[2].toBigInteger()
        val op = expressionText[1]
        return when (op) {
            "+" -> {
                (exp1 + exp2).toString()
            }
            "-" -> {
                (exp1 - exp2).toString()
            }
            "/" -> {
                (exp1 / exp2).toString()
            }
            "%" -> {
                (exp1 % exp2).toString()
            }
            "*" -> {
                (exp1 * exp2).toString()
            }
            else -> {
                ""
            }
        }
    }

    fun String.isNumber(): Boolean //객체: 함수이름 -> 그 객체를 확장하는 함수
    {
        return try {
            this.toBigInteger()
            return true
        } catch (e: NumberFormatException) {
            return false
        }
    }
}