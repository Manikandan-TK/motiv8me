package com.example.motiv8me.model

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "motiv8me_notification_channel"
    const val NOTIFICATION_ID = 1

    val HABIT_CATEGORIES = mapOf(
        "Stop Smoking" to "stop_smoking",
        "Eat Healthier" to "eat_healthier",
        "Exercise More" to "exercise_more",
        "Reduce Screen Time" to "reduce_screen_time",
        "Read More" to "read_more",
        "Learn a New Skill" to "learn_new_skill",
        "Practice Mindfulness" to "practice_mindfulness",
        "Save Money" to "save_money"
    )

    val PREDEFINED_HABITS = HABIT_CATEGORIES.keys.toList()
}
