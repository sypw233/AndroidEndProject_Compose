package ovo.sypw.androidendproject.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty() = User(
            uid = "",
            email = "",
            displayName = null,
            avatarUrl = null
        )

        fun mock() = User(
            uid = "mock_user_001",
            email = "test@example.com",
            displayName = "测试用户",
            avatarUrl = "https://picsum.photos/200/200?random=avatar"
        )
    }
}
