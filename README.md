# 나의 알람

### TimePickerDialog
날짜, 시간 dialog을 정의
~~~kotlin
// 시간을 선택하고 확인을 눌렀을때의 행동을 정의(OnTimeSetListener)
val timeSetListener = TimePickerDialog.OnTimeSetListener { picker, hour, minute ->
	// 새로운 시간을 data class model에 저장
	val model = saveAlarmModel(hour, minute, false)
	// 시간에 따른 뷰를 업데이트
	renderView(model)
	//기존알람 가져오고 삭제
	cancelAlarm()
}
val calendar = Calendar.getInstance() // calendar 생성
val h = Calendar.HOUR_OF_DAY // 24시간 기준
val m = Calendar.MINUTE // 1분 기준
// TimePickerDialog create & show
TimePickerDialog(this, timeSetListener, calendar.get(h), calendar.get(m), false)
    .show()
~~~

### SharedPreference
~~~kotlin
// SharedPreference 생성
val sharedPreference = getSharedPreferences(
	SHARED_PREFERENCE_NAME,
	Context.MODE_PRIVATE
	)
// key-value 값으로 데이터 저장 // with -> commit()
with(sharedPreference.edit()) {
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ON_OFF_KEY, model.onOff)
            commit()
        }
// SharedPreference 에서 key값으로 데이터 가져오기
       val timeDBValue = sharedPreference
			.getString(ALARM_KEY, "00:00") ?: "00:00"
        val onOffDBValue = sharedPreference
			.getBoolean(ON_OFF_KEY, false) ?: false
~~~

### data를 tag에 저장하여 전역변수 처럼활용
~~~kotlin
private fun renderView(model: AlarmDisplayModel) {
	onOffButton.tag = model
}
private fun initOnOffButton() {
	onOffButton.setOnClickListener {
		val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
	}
}
~~~

### Notification
~~~kotlin
private fun createNotificationChannel(context: Context) {
	// oreo 버젼 이상일 경우 채널 생성
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		val notificationChannel = NotificationChannel(
			CHANNEL_ID,
			CHANNEL_NAME,
			NotificationManager.IMPORTANCE_HIGH
		)
		NotificationManagerCompat.from(context)
			.createNotificationChannel(notificationChannel)
	}
}

private fun notifyNotification(context: Context) {
	with(NotificationManagerCompat.from(context)) {
		val build = NotificationCompat
			.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.alarm_on)
			.setContentTitle("일어나 일어나")
			.setContentText("다시 한번 해보는 거야")
			.setPriority(NotificationCompat.PRIORITY_HIGH)

		notify(NOTIFICATION_ID, build.build())
	}
}
~~~

### Calendar 시간 설정
~~~kotlin
// Calendar의 HOUR_OF_DAY, MINUTE 에 설정된 값을 입력
val calendar = Calendar.getInstance().apply {
	set(Calendar.HOUR_OF_DAY, newModel.hour)
	set(Calendar.MINUTE, newModel.minute)

	// Calendar 현재시간 보다 이전이면 DATE(일자)를 1 증가
	if (before(Calendar.getInstance())) {
		add(Calendar.DATE, 1)
	}
// 설정된 calendar는 alarmManager.setInexactRepeating()에서 사용
}
~~~

### AlarmManager, PendingIndent, BroadcastReceiver
AlarmManager : background 작업에서 특정시간에 작업 실행
PendingIntent : Activity, Service, BroadcastReceiver를 호출
BroadcastReceiver : 안드로이드의 현재 상태를 받아올수 있다. (네트워크, 시간 등)
~~~kotlin
// alarmManager 생성
val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
// AlarmReceiver : BroadcastReceiver() intent(알림 팝업) 생성
val intent = Intent(this, AlarmReceiver::class.java)
// BroadcastReceiver를 호출하는 pendingIntent 생성
val pendingIntent = PendingIntent.getBroadcast(
	this,
	REQUEST_CODE,
	Intent(this, AlarmReceiver::class.java),
	PendingIntent.FLAG_UPDATE_CURRENT
)
// alarmManager 실행
// setInexactRepeating : 완전 정확하지는 않은 시간 (자원소모 최소화)
alarmManager.setInexactRepeating(
	AlarmManager.RTC_WAKEUP,
	calendar.timeInMillis,
	AlarmManager.INTERVAL_DAY,
	pendingIntent
)
~~~