package ovo.sypw.androidendproject.data.model

data class Banner(
    val id: String,
    val title: String,
    val imageUrl: String,
    val linkUrl: String,
    val order: Int = 0
) {
    companion object {
        fun mock(): List<Banner> = listOf(
            Banner(
                "1",
                "热门资讯",
                "https://picsum.photos/800/400?random=1",
                "https://example.com/1",
                1
            ),
            Banner(
                "2",
                "精选推荐",
                "https://picsum.photos/800/400?random=2",
                "https://example.com/2",
                2
            ),
            Banner(
                "3",
                "最新动态",
                "https://picsum.photos/800/400?random=3",
                "https://example.com/3",
                3
            )
        )
    }
}
