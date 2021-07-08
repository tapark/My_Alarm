package com.example.myalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointBackward.before
import java.util.*

class MainActivity : AppCompatActivity() {

    private val timeTextView: TextView by lazy {
        findViewById<TextView>(R.id.timeTextView)
    }

    private val ampmTextView: TextView by lazy {
        findViewById<TextView>(R.id.ampmTextView)
    }

    private val onOffButton: Button by lazy {
        findViewById<Button>(R.id.onOffButton)
    }

    private val changeAlarmButton: Button by lazy {
        findViewById<Button>(R.id.changeAlarmButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 0. 뷰를 초기화
        initOnOffButton()
        initChangeAlarmButton()
        // 1. 데이터 가져오기
        val model = fetchDataFromSharedPreferences()
        renderView(model)
        // 2. 뷰에 데이터를 그려주기


    }

    private fun initOnOffButton() {
        onOffButton.setOnClickListener {
            // 시간 데이터를 확인
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            val newModel = saveAlarmModel(model.hour, model.minute, model.onOff.not())
            renderView(newModel)
            // 온오프 변환 및 변화에따른 작업
            if (newModel.onOff) {
                //켜진경우 -> 알람을 생성
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, newModel.hour)
                    set(Calendar.MINUTE, newModel.minute)

                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE,
                    Intent(this, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                // 안드로이즈 도즈모드(수면모드)에서도 알림을 실행
                //alarmManager.setAndAllowWhileIdle()
                //alarmManager.setExactAndAllowWhileIdle()

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

            } else {
                //꺼진경우 -> 알람을 삭제
                cancelAlarm()
            }

        }
    }

    private fun initChangeAlarmButton() {
        changeAlarmButton.setOnClickListener {

            val calendar = Calendar.getInstance()
            val h = Calendar.HOUR_OF_DAY
            val m = Calendar.MINUTE
            val timeSetListener = TimePickerDialog.OnTimeSetListener { picker, hour, minute ->
                // 시간에 따른 뷰를 업데이트 -> 기존알람 삭제
                val model = saveAlarmModel(hour, minute, false)
                // 업데이트
                renderView(model)
                //기존알람 가져오고 삭제
                cancelAlarm()
            }
            // TimePickerDialog 팝업 -> 시간설정 및 시간 데이터 저장
            TimePickerDialog(this, timeSetListener, calendar.get(h), calendar.get(m), false)
                .show()
            // 시간에 따른 뷰를 업데이트 -> 기존알람 삭제
        }
    }

    private fun saveAlarmModel(hour: Int, minute: Int, onOff: Boolean): AlarmDisplayModel {
        val model = AlarmDisplayModel(hour = hour, minute = minute, onOff = onOff)

        val sharedPreference = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        with(sharedPreference.edit()) {
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ON_OFF_KEY, model.onOff)
            commit()
        }

        return model
    }

    private fun fetchDataFromSharedPreferences() : AlarmDisplayModel {
        val sharedPreference = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val timeDBValue = sharedPreference.getString(ALARM_KEY, "00:00") ?: "00:00"
        val onOffDBValue = sharedPreference.getBoolean(ON_OFF_KEY, false) ?: false
        val alarmData = timeDBValue.split(":")

        val alarmModel = AlarmDisplayModel(alarmData[0].toInt(), alarmData[1].toInt(), onOffDBValue)

        // 보정 예외처리
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            Intent(this,AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        )

        if ((pendingIntent == null) && alarmModel.onOff == true) {
            //알림은 꺼져있는데 데이터는 있는경우
            alarmModel.onOff = false
        } else if ((pendingIntent != null) && alarmModel.onOff == false) {
            //알림은 켜있는데 데이터는 없경우
            pendingIntent.cancel()
        }

        return alarmModel
    }

    private fun renderView(model: AlarmDisplayModel) {
        ampmTextView.text = model.ampmText
        timeTextView.text = model.timeText
        onOffButton.text = model.onOffText
        onOffButton.tag = model
    }

    private fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            Intent(this,AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.cancel()
    }

        companion object {
            private const val SHARED_PREFERENCE_NAME = "time"
            private const val ALARM_KEY = "alarm"
            private const val ON_OFF_KEY = "onOff"

            private const val REQUEST_CODE = 1000
    }
}