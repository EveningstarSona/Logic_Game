package com.eveningstarsona.logicgame

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.math.min

class GameActivity : AppCompatActivity() {

    private var game: Game = Game()
    private lateinit var tipAd: RewardedAd
    private lateinit var answerAd: RewardedAd
    private var currentAd = "none"
    private var answerAdPositions: MutableList<Int> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.hide()

        fun upButtons(buttons: MutableList<Button>, texts: MutableList<TextView>) {
            for (count in 0 until buttons.size) {
                buttons[count].visibility = View.VISIBLE
                buttons[count].text = "^"
                buttons[count].setOnClickListener {
                    var number: Int = texts[count].text.toString().toInt()
                    number += 1
                    if (number == 10)
                        number = 0
                    texts[count].text = number.toString()
                }
            }
        }

        fun downButtons(buttons: MutableList<Button>, texts: MutableList<TextView>) {
            for (count in 0 until buttons.size) {
                buttons[count].visibility = View.VISIBLE
                buttons[count].text = "v"
                buttons[count].setOnClickListener {
                    var number: Int = texts[count].text.toString().toInt()
                    number -= 1
                    if (number == -1)
                        number = 9
                    texts[count].text = number.toString()
                }
            }
        }

        fun markButtons(buttons: MutableList<Button>, texts: MutableList<TextView>) {
            for (count in 0 until buttons.size) {
                buttons[count].visibility = View.VISIBLE
                buttons[count].setOnClickListener {
                    when (texts[count].currentTextColor) {
                        Color.BLACK -> {
                            texts[count].setTextColor(Color.RED)
                        }
                        Color.RED -> {
                            texts[count].setTextColor(Color.GREEN)
                        }
                        Color.GREEN -> {
                            texts[count].setTextColor(Color.BLACK)
                        }
                    }
                }
            }
        }

        fun setGame() {
            game.generateGame()
            findViewById<TextView>(R.id.text_tip_one).text = game.getClues(1)
            findViewById<TextView>(R.id.text_tip_two).text = game.getClues(2)
            findViewById<TextView>(R.id.text_tip_three).text = game.getClues(3)
            findViewById<TextView>(R.id.text_tip_four).text = game.getClues(4)
            findViewById<TextView>(R.id.text_tip_five).text = game.getClues(5)
            findViewById<TextView>(R.id.text_tip_six).text = "AD"
            findViewById<TextView>(R.id.text_button_zero).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_one).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_two).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_three).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_four).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_five).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_six).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_seven).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_eight).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_button_nine).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_answer_one).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_answer_two).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_answer_three).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_answer_four).setTextColor(Color.BLACK)
            findViewById<TextView>(R.id.text_answer_one).text = "0"
            findViewById<TextView>(R.id.text_answer_two).text = "0"
            findViewById<TextView>(R.id.text_answer_three).text = "0"
            findViewById<TextView>(R.id.text_answer_four).text = "0"
            upButtons(
                mutableListOf(button_up_one, button_up_two, button_up_three, button_up_four),
                mutableListOf(text_answer_one, text_answer_two, text_answer_three, text_answer_four)
            )
            downButtons(
                mutableListOf(
                    button_down_one,
                    button_down_two,
                    button_down_three,
                    button_down_four
                ),
                mutableListOf(text_answer_one, text_answer_two, text_answer_three, text_answer_four)
            )
            markButtons(
                mutableListOf(
                    button_text_zero,
                    button_text_one,
                    button_text_two,
                    button_text_three,
                    button_text_four,
                    button_text_five,
                    button_text_six,
                    button_text_seven,
                    button_text_eight,
                    button_text_nine
                ), mutableListOf(
                    text_button_zero,
                    text_button_one,
                    text_button_two,
                    text_button_three,
                    text_button_four,
                    text_button_five,
                    text_button_six,
                    text_button_seven,
                    text_button_eight,
                    text_button_nine
                )
            )
            answerAdPositions = mutableListOf()
            button_tip_six.visibility = View.VISIBLE
        }

        fun setButtons(
            buttonsUp: MutableList<Button>,
            buttonsDown: MutableList<Button>,
            answerTexts: MutableList<TextView>,
            buttonsMark: MutableList<Button>,
            markTexts: MutableList<TextView>
        ) {
            upButtons(buttonsUp, answerTexts)
            downButtons(buttonsDown, answerTexts)
            markButtons(buttonsMark, markTexts)
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
            button_tip_six.setOnClickListener {
                if (tipAd.isLoaded) {
                    val activityContext: Activity = this@GameActivity
                    val adCallback = object : RewardedAdCallback() {
                        override fun onRewardedAdOpened() {
                            // Ad opened.
                        }

                        override fun onRewardedAdClosed() {
                            // Ad closed.
                        }

                        override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                            // User earned reward.
                            adTip()
                        }

                        override fun onRewardedAdFailedToShow(errorCode: Int) {
                            // Ad failed to display.
                        }
                    }
                    tipAd.show(activityContext, adCallback)
                } else {
                    Log.d("TAG", "The rewarded ad wasn't loaded yet.")
                }

            }
            button_help.setOnClickListener {
                currentAd = "answer"
                if (answerAd.isLoaded) {
                    val activityContext: Activity = this@GameActivity
                    val adCallback = object : RewardedAdCallback() {

                        override fun onRewardedAdOpened() {
                            // Ad opened.
                        }

                        override fun onRewardedAdClosed() {
                            // Ad closed.
                        }

                        override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                            // User earned reward.
                            adAnswer()
                        }

                        override fun onRewardedAdFailedToShow(errorCode: Int) {
                            // Ad failed to display.
                        }
                    }
                    answerAd.show(activityContext, adCallback)
                } else {
                    Log.d("TAG", "The rewarded ad wasn't loaded yet.")
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setGame()
        setButtons(
            mutableListOf(button_up_one, button_up_two, button_up_three, button_up_four),
            mutableListOf(button_down_one, button_down_two, button_down_three, button_down_four),
            mutableListOf(text_answer_one, text_answer_two, text_answer_three, text_answer_four),
            mutableListOf(
                button_text_zero,
                button_text_one,
                button_text_two,
                button_text_three,
                button_text_four,
                button_text_five,
                button_text_six,
                button_text_seven,
                button_text_eight,
                button_text_nine
            ),
            mutableListOf(
                text_button_zero,
                text_button_one,
                text_button_two,
                text_button_three,
                text_button_four,
                text_button_five,
                text_button_six,
                text_button_seven,
                text_button_eight,
                text_button_nine
            )
        )


        tipAd = createAndLoadRewardedAd(
            "ca-app-pub-3940256099942544/5224354917"
        )
        answerAd = createAndLoadRewardedAd(
            "ca-app-pub-3940256099942544/5224354917"
        )
    }

    private fun createAndLoadRewardedAd(adUnitId: String): RewardedAd {
        val rewardedAd = RewardedAd(this, adUnitId)
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad loaded successfully
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }

    private fun adAnswer() {
        val upButtons = mutableListOf(button_up_one, button_up_two, button_up_three, button_up_four)
        val downButtons =
            mutableListOf(button_down_one, button_down_two, button_down_three, button_down_four)
        val textsAnswer =
            mutableListOf(text_answer_one, text_answer_two, text_answer_three, text_answer_four)
        val markButtons = mutableListOf(
            button_text_zero,
            button_text_one,
            button_text_two,
            button_text_three,
            button_text_four,
            button_text_five,
            button_text_six,
            button_text_seven,
            button_text_eight,
            button_text_nine
        )
        val textsMark = mutableListOf(
            text_button_zero,
            text_button_one,
            text_button_two,
            text_button_three,
            text_button_four,
            text_button_five,
            text_button_six,
            text_button_seven,
            text_button_eight,
            text_button_nine
        )
        var random = (0..3).shuffled().first()
        while (answerAdPositions.contains(random)) {
            random = (0..3).shuffled().first()
        }
        upButtons[random].visibility = View.INVISIBLE
        downButtons[random].visibility = View.INVISIBLE
        markButtons[game.password[random]].visibility = View.INVISIBLE
        textsAnswer[random].text = game.password[random].toString()
        textsAnswer[random].setTextColor(Color.GREEN)
        textsMark[game.password[random]].setTextColor(Color.GREEN)
        answerAd = createAndLoadRewardedAd(
            "ca-app-pub-3940256099942544/5224354917"
        )
        answerAdPositions.add(random)
    }

    @SuppressLint("SetTextI18n")
    private fun adTip() {
        val clue: MutableList<Int> = game.generateClue()
        val start = clue.joinToString(separator = "")
        val amount = game.countAmount(clue)
        val middle = amount[0]
        val end = amount[1]
        var clueText = "$start\n$middle correct numbers!"
        if (end != 0)
            clueText = "$clueText\n$end correct places!"
        text_tip_six.text = "$clueText\n"
        button_tip_six.visibility = View.INVISIBLE
        tipAd = createAndLoadRewardedAd(
            "ca-app-pub-3940256099942544/5224354917"
        )
    }

    class Game {

        var size = 4
        private var clueAmount = 5
        var password = mutableListOf<Int>()
        private var clues = mutableListOf<MutableList<Int>>()

        fun getClues(which: Int): String {
            val start = clues[which - 1].joinToString(separator = "")
            val amount = countAmount(clues[which - 1])
            val middle = amount[0]
            val end = amount[1]
            var s = if (middle > 1) "s" else ""
            var clue = "$start\n$middle correct number$s!"
            if (end != 0) {
                s = if (end > 1) "s" else ""
                clue = "$clue\n$end correct place$s!"
            }
            clue = "$clue\n"
            return clue
        }

        private fun isRepeated(clue: MutableList<Int>): Boolean {
            for (num in clue)
                if (clue.count { it == num } > 1)
                    return true
            return false
        }

        fun countAmount(clue: MutableList<Int>): MutableList<Int> {
            fun Boolean.toInt() = if (this) 1 else 0
            var count = 0
            var same = 0
            for (num in 0 until password.size) {
                count += min(password.count { it == clue[num] }, 1)
                same += (clue[num] == password[num]).toInt()
            }
            return mutableListOf(count, same)
        }

        private fun countWhich(clue: MutableList<Int>): MutableList<Int> {
            val sameList: MutableList<Int> = mutableListOf()
            for (num in 0 until size)
                if (password.count { it == clue[num] } != 0)
                    if (clue[num] == password[num])
                        sameList.add(clue[num])
            return sameList
        }

        fun generateClue(): MutableList<Int> {
            var aux: MutableList<Int> = mutableListOf(0, 0, 0, 0)
            while (monoFailCheck(aux, 1)) {
                aux = generateNumber()
            }
            return aux
        }

        private fun generateNumber(): MutableList<Int> {
            val aux = (123..9876).shuffled().first().toString().split("").toMutableList()
            for (i in (1..2))
                aux.remove("")
            if (aux.size != size)
                aux.add(0, "0")
            return aux.map { it.toInt() }.toMutableList()
        }

        private fun monoFailCheck(clue: MutableList<Int>, what: Int): Boolean {
            var aux = true
            if (isRepeated(clue))
                return true
            if (clue.size != size)
                return true
            if (what == 1) {
                for (num in password)
                    if (clue.contains(num)) {
                        aux = false
                        break
                    }
                if (countAmount(clue)[0] >= clue.size)
                    return true
                return aux
            }
            return false
        }

        private fun poliFailCheck(clues: MutableList<MutableList<Int>>): Boolean {
            val rightList: MutableList<Int> =
                clues.asSequence().map { countWhich(it) }.flatten().distinct().toMutableList()
            val contain: MutableList<Int> = clues.flatten().distinct().toMutableList()
            val aux = (rightList.size <= password.size - 2)
            for (num in password)
                if (!contain.contains(num))
                    return true
            return (rightList.size <= password.size - 2)
        }

        private fun generateClues(): MutableList<MutableList<Int>> {
            var fail = true
            var clue: MutableList<Int>
            var clues: MutableList<MutableList<Int>> = mutableListOf()
            while (fail) {
                clues = mutableListOf()
                for (i in 1..clueAmount) {
                    clue = generateClue()
                    clues.add(clue)
                }
                fail = poliFailCheck(clues)
            }
            return clues
        }

        fun checkAnswer(answer: MutableList<Int>): Boolean {
            if (answer == password)
                return true
            val passwordAux: MutableList<MutableList<Int>> =
                clues.map { countAmount(it) }.toMutableList()
            val answerAux: MutableList<MutableList<Int>> =
                clues.map { countAnswerAmount(it, answer) }.toMutableList()
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

        private fun countAnswerAmount(
            clue: MutableList<Int>,
            answer: MutableList<Int>
        ): MutableList<Int> {
            fun Boolean.toInt() = if (this) 1 else 0
            var count = 0
            var same = 0
            for (num in 0 until answer.size) {
                count += min(answer.count { it == clue[num] }, 1)
                same += (clue[num] == answer[num]).toInt()
            }
            return mutableListOf(count, same)
        }

        private fun generatePassword(): MutableList<Int> {
            var aux: MutableList<Int> = mutableListOf()
            while (monoFailCheck(aux, 0))
                aux = generateNumber()
            return aux
        }

        fun generateGame() {
            password = generatePassword()
            clues = generateClues()
        }
    }
}