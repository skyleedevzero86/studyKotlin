package com.sleekydz86.oauth.global.application.user

import com.sleekydz86.oauth.domain.user.model.ActivateUserCommand
import com.sleekydz86.oauth.domain.user.model.ApproveUserCommand
import com.sleekydz86.oauth.domain.user.model.ChangePasswordWithVerifyCommand
import com.sleekydz86.oauth.domain.user.model.ChangeUserRoleCommand
import com.sleekydz86.oauth.domain.user.model.CreateUserByAdminCommand
import com.sleekydz86.oauth.domain.user.model.DeleteUserCommand
import com.sleekydz86.oauth.domain.user.model.JoinCommand
import com.sleekydz86.oauth.domain.user.model.RecordLoginCommand
import com.sleekydz86.oauth.domain.user.model.RecordLoginFailureCommand
import com.sleekydz86.oauth.domain.user.model.ResetLoginFailCountCommand
import com.sleekydz86.oauth.domain.user.model.ResetPasswordFailCountCommand
import com.sleekydz86.oauth.domain.user.model.RestoreUserCommand
import com.sleekydz86.oauth.domain.user.model.SuspendUserCommand
import com.sleekydz86.oauth.domain.user.model.UnlockUserCommand
import com.sleekydz86.oauth.domain.user.model.UpdateProfileCommand
import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.WithdrawUserCommand
import com.sleekydz86.oauth.domain.user.service.UserCommandService
import com.sleekydz86.oauth.global.event.UserChangedEvent
import com.sleekydz86.oauth.global.event.UserListRefreshEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class UserLifecycleApplicationService(
    private val userCommandService: UserCommandService,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun join(command: JoinCommand): User = publish(userCommandService.join(command))

    fun createByAdmin(command: CreateUserByAdminCommand): User = publish(userCommandService.createByAdmin(command))

    fun updateProfile(command: UpdateProfileCommand): User = publish(userCommandService.updateProfile(command))

    fun approve(command: ApproveUserCommand): User = publish(userCommandService.approve(command))

    fun suspend(command: SuspendUserCommand): User = publish(userCommandService.suspend(command))

    fun activate(command: ActivateUserCommand): User = publish(userCommandService.activate(command))

    fun restore(command: RestoreUserCommand): User = publish(userCommandService.restore(command))

    fun withdraw(command: WithdrawUserCommand): User = publish(userCommandService.withdraw(command))

    fun changeRole(command: ChangeUserRoleCommand): User = publish(userCommandService.changeRole(command))

    fun unlock(command: UnlockUserCommand): User = publish(userCommandService.unlock(command))

    fun resetPasswordFailCount(command: ResetPasswordFailCountCommand): User =
        publish(userCommandService.resetPasswordFailCount(command))

    fun resetLoginFailCount(command: ResetLoginFailCountCommand): User =
        publish(userCommandService.resetLoginFailCount(command))

    fun changePassword(command: ChangePasswordWithVerifyCommand): User =
        publish(userCommandService.changePassword(command))

    fun recordLogin(command: RecordLoginCommand): User = publish(userCommandService.recordLogin(command))

    fun recordLoginFailure(command: RecordLoginFailureCommand): User? =
        userCommandService.recordLoginFailure(command)

    fun delete(command: DeleteUserCommand) {
        userCommandService.delete(command)
        eventPublisher.publishEvent(UserListRefreshEvent(reason = "delete:${command.username}"))
    }

    private fun publish(user: User): User {
        eventPublisher.publishEvent(UserChangedEvent(user))
        return user
    }
}
