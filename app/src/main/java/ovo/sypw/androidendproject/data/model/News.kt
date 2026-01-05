package ovo.sypw.androidendproject.data.model

data class News(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val publishTime: String,
    val imageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val sourceUrl: String,
    val category: String = "general",
    val viewType: Int = VIEW_TYPE_TEXT
) {
    companion object {
        const val VIEW_TYPE_TEXT = 0
        const val VIEW_TYPE_SINGLE = 1
        const val VIEW_TYPE_MULTI = 2

        fun mock(page: Int = 1, pageSize: Int = 10): List<News> {
            val startIndex = (page - 1) * pageSize
            return (startIndex until startIndex + pageSize).map { i ->
                val viewType = when (i % 3) {
                    0 -> VIEW_TYPE_TEXT
                    1 -> VIEW_TYPE_SINGLE
                    else -> VIEW_TYPE_MULTI
                }
                News(
                    id = "news_$i",
                    title = "新闻标题 ${i + 1}：这是一条模拟新闻标题内容",
                    content = "这是新闻内容的详细描述，包含了很多有趣的信息...",
                    author = "作者${i % 5 + 1}",
                    publishTime = "2024-12-${10 + i % 20} 10:30",
                    imageUrl = if (viewType != VIEW_TYPE_TEXT) "https://picsum.photos/400/300?random=$i" else null,
                    imageUrls = if (viewType == VIEW_TYPE_MULTI) listOf(
                        "https://picsum.photos/400/300?random=${i}a",
                        "https://picsum.photos/400/300?random=${i}b",
                        "https://picsum.photos/400/300?random=${i}c"
                    ) else null,
                    sourceUrl = "https://example.com/news/$i",
                    category = listOf("科技", "财经", "娱乐", "体育", "社会")[i % 5],
                    viewType = viewType
                )
            }
        }
    }
}
