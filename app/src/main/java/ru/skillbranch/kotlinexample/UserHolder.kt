package ru.skillbranch.kotlinexample

object UserHolder {
    private var map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also { user ->
                run {
                    if (map.containsKey(user.login)) {
                        throw IllegalArgumentException("A user with this email already exists")
                    } else {
                        map[user.login] = user
                    }
                }
            }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        if (rawPhone.isPhoneNumberValid()) {
            return User.makeUser(fullName, phone = rawPhone)
                .also { user ->
                    run {
                        if (map.containsKey(user.login)) {
                            throw IllegalArgumentException("A user with this phone already exists")
                        } else {
                            map[user.login] = user
                        }
                    }

                }
        } else {
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }

    }

    fun normalizeLogin(login: String): String {
        return if (login.isPhoneNumberValid()) {
            login.replace("[^\\d+]".toRegex(), "")
        } else {
            login
        }
    }

    fun loginUser(login: String, password: String): String? {

        return map[normalizeLogin(
            login
        ).trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun clearHolder() {
        map.clear()
    }

    private fun String.isPhoneNumberValid(): Boolean {
        return this.matches("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$".toRegex())
    }

    fun requestAccessCode(login: String): Unit {
        val user = map[normalizeLogin(
            login
        )]
        user?.requestAccessCode()
    }

    private fun parseUser(user: String): User {
        val userItems = user.split(";")
        return User.makeUser(
            fullName = userItems[0],
            email = if(userItems[1].isNotBlank()) userItems[1] else null,
            hash = userItems[2],
            phone = if(userItems[3].isNotBlank()) userItems[3] else null
        )
    }

    fun importUsers(list: List<String>): List<User>   {
        val result = mutableListOf<User>()
        for(userString in list)   {
            if (userString.isNotEmpty())  {
                val user =
                    parseUser(
                        userString
                    )
                result.add(user)
            }
        }
        return result
    }
}