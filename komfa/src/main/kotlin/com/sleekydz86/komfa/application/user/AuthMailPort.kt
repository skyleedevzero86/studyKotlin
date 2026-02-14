package com.sleekydz86.komfa.application.user

interface AuthMailPort {
    fun sendNewSignupNotificationToAdmin(adminEmail: String, username: String): Boolean
    fun sendFindUsernameEmail(to: String, username: String): Boolean
    fun sendResetPasswordEmail(to: String, resetLink: String): Boolean
    fun sendPasswordChangeNoticeEmail(to: String, username: String): Boolean
}
