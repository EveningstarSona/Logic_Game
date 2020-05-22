package com.eveningstarsona.logicgame

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    private var game: Game = Game()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.hide()

        fun setGame() {
            game.generateGame()
            findViewById<TextView>(R.id.text_tip_one).text = game.getClues(1)
            findViewById<TextView>(R.id.text_tip_two).text = game.getClues(2)
            findViewById<TextView>(R.id.text_tip_three).text = game.getClues(3)
            findViewById<TextView>(R.id.text_tip_four).text = game.getClues(4)
            findViewById<TextView>(R.id.text_tip_five).text = game.getClues(5)
            findViewById<TextView>(R.id.text_answer_one).text = "0"
            findViewById<TextView>(R.id.text_answer_two).text = "0"
            findViewById<TextView>(R.id.text_answer_three).text = "0"
            findViewById<TextView>(R.id.text_answer_four).text = "0"
        }

        fun upButtons(buttons: MutableList<Button>, texts: MutableList<TextView>) {
            print(texts.map { it.text })
            for (count in 0 until buttons.size) {
                buttons[count].text = "^"
                buttons[count].setOnClickListener {
                    print(texts[count].text)
                    var number: Int = texts[count].text.toString().toInt()
                    number += 1
                    print(number)
                    if (number == 10)
                        number = 0
                    texts[count].text = number.toString()
                }
            }
        }

        fun downButtons(buttons: MutableList<Button>, texts: MutableList<TextView>) {
            print(texts.map { it.text })
            for (count in 0 until buttons.size) {
                buttons[count].text = "v"
                buttons[count].setOnClickListener {
                    print(texts[count].text)
                    var number: Int = texts[count].text.toString().toInt()
                    number -= 1
                    print(number)
                    if (number == -1)
                        number = 9
                    texts[count].text = number.toString()
                }
            }
        }

        fun setButtons(
            buttonsUp: MutableList<Button>,
            buttonsDown: MutableList<Button>,
            texts: MutableList<TextView>
        ) {
            upButtons(buttonsUp, texts)
            downButtons(buttonsDown, texts)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setGame()
        setButtons(
            mutableListOf(button_up_one, button_up_two, button_up_three, button_up_four),
            mutableListOf(button_down_one, button_down_two, button_down_three, button_down_four),
            mutableListOf(text_answer_one, text_answer_two, text_answer_three, text_answer_four)
        )


        button_done.setOnClickListener {
            val answer: MutableList<String> =
                (text_answer_one.text.toString() + text_answer_two.text.toString() + text_answer_three.text.toString() + text_answer_four.text.toString()).split(
                    ""
                ).toMutableList()
            for (i in 1..2)
                answer.remove("")
            if (answer.size != game.size)
                return@setOnClickListener
            if (game.checkAnswer(answer.map { it.toInt() }.toMutableList()))
                setGame()
        }

    }

    class Game {

        var size = 4
        private var clueAmount = 5
        private var password = mutableListOf<Int>()
        private var clues = mutableListOf<MutableList<Int>>()

        fun getClues(which: Int): String {
            val start = this.clues[which - 1].joinToString(separator = "")
            val amount = countAmount(clues[which - 1], password, size)
            val middle = amount[0][0]
            val end = amount[0][1]
            var clue = "$start\n$middle correct numbers!"
            if (end != 0)
                clue = "$clue\n$end correct places!"
            clue = "$clue\n"
            return clue
        }

        private fun isRepeated(password: MutableList<Int>): Boolean {
            for (num in password)
                if (password.count { it == num } > 1)
                    return true
            return false
        }

        private fun countAmount(
            clue: MutableList<Int>,
            password: MutableList<Int>,
            size: Int
        ): MutableList<MutableList<Int>> {
            var count = 0
            var same = 0
            for (num in 0 until size) {
                if (password.count { it == clue[num] } > 0) {
                    count += 1
                    if (clue[num] == password[num])
                        same += 1
                }
            }
            return mutableListOf(mutableListOf(count, same), countWhich(clue, password, size))
        }

        private fun countWhich(
            clue: MutableList<Int>,
            password: MutableList<Int>,
            size: Int
        ): MutableList<Int> {
            val sameList: MutableList<Int> = mutableListOf()
            for (num in 0 until size)
                if (password.count { it == clue[num] } != 0)
                    if (clue[num] == password[num])
                        sameList.add(clue[num])
            return sameList
        }

        private fun covers(clue: MutableList<Int>, password: MutableList<Int>): Boolean {
            for (num in clue)
                if (password.count { it == num } != 0)
                    return true
            return false
        }

        private fun generateClue(password: MutableList<Int>, size: Int): MutableList<Int> {
            var aux = mutableListOf(-1, -1, -1, -1)
            while (!covers(aux, password)) {
                aux = generateNumber(size)
            }
            return aux
        }

        private fun rawGen(size: Int): MutableList<Int> {
            var aux: MutableList<Int> = mutableListOf()
            if (size == 4) {
                val auxString = (Math.random() * 9876).toInt().toString().split("").toMutableList()
                for (i in 1..2)
                    auxString.remove("")
                aux = auxString.map { it.toInt() }.toMutableList()
            } else if (size == 5) {
                val auxString = (Math.random() * 98765).toInt().toString().split("").toMutableList()
                for (i in 1..2)
                    auxString.remove("")
                aux = auxString.map { it.toInt() }.toMutableList()
            }
            return aux
        }

        private fun generateNumber(size: Int): MutableList<Int> {
            var aux = mutableListOf(0, 0, 0, 0)
            while (isRepeated(aux)) {
                aux = rawGen(size)
                while (size != aux.size)
                    aux.add(0, 0)
            }
            return aux
        }

        private fun failCheck(
            password: MutableList<Int>,
            clues: MutableList<MutableList<Int>>
        ): Boolean {
            var aux = true
            for (num in password) {
                if (!aux)
                    break
                for (clue in clues) {
                    if (!covers(mutableListOf(num), clue)) {
                        aux = true
                    } else {
                        aux = false
                        break
                    }
                }
            }
            val cluesAux: MutableList<MutableList<Int>> =
                clues.map { countAmount(it, password, password.size)[0] }.toMutableList()
            val correctAux = cluesAux.map { it[0] }.toMutableList()
            val cluesAuxTwo: MutableList<Int> =
                clues.map { countAmount(it, password, password.size)[1] }.flatten().toMutableList()
            var rightList: MutableList<Int> = mutableListOf()
            val correctList: MutableList<Int> = mutableListOf()
            val contain: MutableList<MutableList<Int>> = mutableListOf()
            for (clue in clues)
                contain.add(clue)
            val containTwo: MutableList<Int> = contain.flatten().distinct().toMutableList()
            for (num in password)
                if (!covers(containTwo, mutableListOf(num)))
                    return true
            for (item in cluesAuxTwo)
                rightList.add(item)
            for (item in correctAux)
                correctList.add(item)
            rightList = rightList.distinct().toMutableList()
            return when {
                rightList.size <= password.size - 2 -> true
                covers(mutableListOf(password.size), correctList) -> true
                covers(mutableListOf(0), correctList) -> true
                else -> {
                    println("$rightList $correctList")
                    aux
                }
            }
        }

        private fun generateClues(
            password: MutableList<Int>,
            size: Int,
            clueAmount: Int
        ): MutableList<MutableList<Int>> {
            var fail = true
            var clues: MutableList<MutableList<Int>> = mutableListOf()
            while (fail) {
                clues = mutableListOf()
                for (i in 1..clueAmount)
                    clues.add(generateClue(password, size))
                fail = failCheck(password, clues)
            }
            return clues
        }

        fun checkAnswer(answer: MutableList<Int>): Boolean {
            if (answer == password)
                return true
            val passwordAux: MutableList<MutableList<Int>> =
                clues.map { countAmount(it, password, size)[0] }.toMutableList()
            val answerAux: MutableList<MutableList<Int>> =
                clues.map { countAmount(it, answer, size)[0] }.toMutableList()
            for (i in 1..clues.size) {
                if (passwordAux[i][0] != answerAux[i][0]) {
                    return false
                }
                if (passwordAux[i][1] != answerAux[i][1]) {
                    return false
                }
            }
            return true
        }

        fun generateGame() {
            this.password = generateNumber(this.size)
            this.clues = generateClues(this.password, this.size, this.clueAmount)
        }
    }
}