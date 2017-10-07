package com.wabadaba.dziennik.api

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import com.wabadaba.dziennik.ui.ifNotNull
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Singleton

@Singleton
@SuppressLint("ApplySharedPref")
class UserRepository(
        val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val usersPrefKey = "logged_in_users"
    private val defaultUserKey = "default_user"

    private val userSubject: BehaviorSubject<FullUser> = BehaviorSubject.create<FullUser>()
    private val allUsersSubject: BehaviorSubject<List<User>> = BehaviorSubject.create<List<User>>()

    val currentUser: Observable<FullUser> = userSubject
            .doOnNext(this::saveDefaultUser)
    val allUsers: Observable<List<User>> = allUsersSubject

    init {
        val users = loadUsers().toMutableList()
        val defaultUser = prefs.getString(defaultUserKey, null)
                .ifNotNull(this::getUser)
                ?: if (users.isNotEmpty()) users[0]
        else null
        defaultUser.ifNotNull { user ->
            users.remove(user)
            users.add(0, user)
        }

        allUsersSubject.onNext(users)
        defaultUser.ifNotNull(this::getFullUser)
                .ifNotNull { userSubject.onNext(it) }
    }

    private fun saveUsers(users: List<User>) {
        //serialize users as a string set
        val rawUsers = users
                .map { user -> Parser.mapper.writeValueAsString(user) }
                .toSet()
        //save to prefs
        prefs.edit()
                .putStringSet(usersPrefKey, rawUsers)
                .commit()
        //update subject
        allUsersSubject.onNext(users)
    }

    private fun loadUsers(): List<User> {
        val loadedRawUsers = prefs.getStringSet(usersPrefKey, null)
        return loadedRawUsers?.map { Parser.parse(it, User::class) } ?: emptyList()
    }

    fun addUser(fullUser: FullUser) {
        //check if there already is a user with the same login
        val currentUsers = loadUsers().map { it.login }
        if (fullUser.login in currentUsers) {
            throw IllegalStateException("User ${fullUser.login} is already logged in!")
        }

        //add user to the current users
        val newUsers =
                listOf(
                        User(fullUser.login,
                                fullUser.firstName,
                                fullUser.lastName,
                                fullUser.groupId)) +
                        loadUsers()
        saveUsers(newUsers)

        saveAuthInfo(fullUser.login, fullUser.authInfo)

        //set the newly added user as current
        userSubject.onNext(fullUser)
    }

    fun removeUser(login: String) {
        deleteAuthInfo(login)

        val users = loadUsers()

        val newUsers = users.filter { it.login != login }
        saveUsers(newUsers)

        if (newUsers.isNotEmpty()) {
            switchUser(newUsers[0].login)
        }
    }

    /**
     * Switch to another user(must be already logged in)
     */
    fun switchUser(login: String) {
        val user = getUser(login)
        val fullUser = getFullUser(user)
        userSubject.onNext(fullUser)
    }

    private fun getUser(login: String): User {
        val users = loadUsers()
        return (users.singleOrNull { it.login == login }
                ?: throw UnsupportedOperationException("User $login doesn't exist."))
    }

    private fun getFullUser(user: User) = FullUser(
            user.login,
            user.firstName,
            user.lastName,
            user.groupId,
            loadAuthInfo(user.login))

    private fun authInfoKey(login: String) = "${login}_auth_info"

    private fun loadAuthInfo(login: String): AuthInfo {
        val stringValue = prefs.getString(authInfoKey(login), null)
                ?: throw IllegalStateException("Authorization info for user $login not found")
        return Parser.parse(stringValue, AuthInfo::class)
    }

    fun saveAuthInfo(login: String, authInfo: AuthInfo) {
        val stringValue = Parser.mapper.writeValueAsString(authInfo)
        prefs.edit()
                .putString(authInfoKey(login), stringValue)
                .commit()
    }

    private fun deleteAuthInfo(login: String) {
        prefs.edit()
                .remove(authInfoKey(login))
                .commit()
    }

    private fun saveDefaultUser(fullUser: FullUser) =
            prefs.edit().putString(defaultUserKey, fullUser.login).apply()
}