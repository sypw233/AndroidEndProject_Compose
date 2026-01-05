package ovo.sypw.androidendproject.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News
import ovo.sypw.androidendproject.data.remote.ApiService
import ovo.sypw.androidendproject.data.remote.NewsListData
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

/**
 * 刷新结果数据类
 */
data class RefreshResultData(
    val newsListData: NewsListData,
    val newItemsCount: Int
)

class NewsRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("news_cache", Context.MODE_PRIVATE)
    private val CACHE_KEY = "cached_news_list"
    private val CACHE_TIME_KEY = "cache_time"
    private val CACHE_DURATION = 2 * 60 * 1000L // 2 minutes for RSS

    // 内存缓存
    private var cachedNews = mutableListOf<News>()

    init {
        // 启动时从本地加载缓存
        loadCacheFromDisk()
    }

    /**
     * 获取新闻列表（分页）
     */
    fun getNewsList(page: Int = 1, pageSize: Int = 10): Flow<Result<NewsListData>> = flow {
        try {
            // 首次加载或缓存过期时获取新数据
            if (cachedNews.isEmpty() || isCacheExpired()) {
                fetchAndParseRss()
            }

            // 分页逻辑
            val total = cachedNews.size
            val fromIndex = (page - 1) * pageSize
            val toIndex = minOf(fromIndex + pageSize, total)

            if (fromIndex >= total) {
                emit(Result.success(NewsListData(emptyList(), emptyList(), false)))
            } else {
                val pageData = cachedNews.subList(fromIndex, toIndex).toList()
                emit(
                    Result.success(
                        NewsListData(
                            banners = if (page == 1) getBannersFromNews(cachedNews) else emptyList(),
                            news = pageData,
                            hasMore = toIndex < total
                        )
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 失败时使用缓存数据
            if (cachedNews.isNotEmpty()) {
                val total = cachedNews.size
                val fromIndex = (page - 1) * pageSize
                if (fromIndex < total) {
                    val toIndex = minOf(fromIndex + pageSize, total)
                    emit(
                        Result.success(
                            NewsListData(
                                banners = if (page == 1) getBannersFromNews(cachedNews) else emptyList(),
                                news = cachedNews.subList(fromIndex, toIndex).toList(),
                                hasMore = toIndex < total
                            )
                        )
                    )
                    return@flow
                }
            }
            emit(Result.success(getMockData(page, pageSize)))
        }
    }

    /**
     * 刷新数据并返回新条目数量
     */
    fun refreshNews(pageSize: Int = 10): Flow<Result<RefreshResultData>> = flow {
        try {
            // 记录旧数据的ID集合
            val oldIds = cachedNews.map { it.id }.toSet()

            // 获取新数据
            val responseBody = apiService.getRssFeed("https://www.ithome.com/rss/")
            val xmlString = responseBody.string()
            val newlyParsedNews = parseRss(xmlString)

            if (newlyParsedNews.isEmpty()) {
                // RSS 返回空，使用缓存
                emit(
                    Result.success(
                        RefreshResultData(
                            newsListData = getFirstPageData(pageSize),
                            newItemsCount = 0
                        )
                    )
                )
                return@flow
            }

            // 计算新增条目数（在旧数据中不存在的）
            val newItems = newlyParsedNews.filter { it.id !in oldIds }
            val newItemsCount = newItems.size

            // 合并数据：新数据在前，旧数据中不重复的在后
            val newIds = newlyParsedNews.map { it.id }.toSet()
            val oldUniqueNews = cachedNews.filter { it.id !in newIds }
            val mergedNews = newlyParsedNews + oldUniqueNews

            // 更新缓存
            cachedNews.clear()
            cachedNews.addAll(mergedNews)
            saveCacheToDisk()

            emit(
                Result.success(
                    RefreshResultData(
                        newsListData = getFirstPageData(pageSize),
                        newItemsCount = newItemsCount
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    private fun getFirstPageData(pageSize: Int): NewsListData {
        val total = cachedNews.size
        val toIndex = minOf(pageSize, total)
        return NewsListData(
            banners = getBannersFromNews(cachedNews),
            news = if (total > 0) cachedNews.subList(0, toIndex).toList() else emptyList(),
            hasMore = toIndex < total
        )
    }

    private fun isCacheExpired(): Boolean {
        val cacheTime = prefs.getLong(CACHE_TIME_KEY, 0L)
        return System.currentTimeMillis() - cacheTime > CACHE_DURATION
    }

    private fun loadCacheFromDisk() {
        try {
            val json = prefs.getString(CACHE_KEY, null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<News>>() {}.type
                val loaded: List<News> = gson.fromJson(json, type)
                cachedNews.clear()
                cachedNews.addAll(loaded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveCacheToDisk() {
        try {
            val json = gson.toJson(cachedNews)
            prefs.edit()
                .putString(CACHE_KEY, json)
                .putLong(CACHE_TIME_KEY, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBannersFromNews(newsList: List<News>): List<Banner> {
        return newsList.filter { !it.imageUrl.isNullOrEmpty() }
            .take(3)
            .map { news ->
                Banner(
                    id = news.id,
                    imageUrl = news.imageUrl!!,
                    title = news.title,
                    linkUrl = news.sourceUrl
                )
            }
    }

    private suspend fun fetchAndParseRss() {
        val responseBody = apiService.getRssFeed("https://www.ithome.com/rss/")
        val xmlString = responseBody.string()
        val parsedNews = parseRss(xmlString)
        if (parsedNews.isNotEmpty()) {
            // 合并新旧数据
            val existingIds = cachedNews.map { it.id }.toSet()
            val newIds = parsedNews.map { it.id }.toSet()
            val oldUniqueNews = cachedNews.filter { it.id !in newIds }

            cachedNews.clear()
            cachedNews.addAll(parsedNews)
            cachedNews.addAll(oldUniqueNews)
            saveCacheToDisk()
        }
    }

    private suspend fun parseRss(xml: String): List<News> = withContext(Dispatchers.IO) {
        val newsList = mutableListOf<News>()
        try {
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentTag = ""

            var title = ""
            var link = ""
            var description = ""
            var pubDate = ""
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            insideItem = true
                            title = ""
                            link = ""
                            description = ""
                            pubDate = ""
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (insideItem) {
                            val text = parser.text
                            when (currentTag) {
                                "title" -> title = text
                                "link" -> link = text
                                "description" -> description = text
                                "pubDate" -> pubDate = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") {
                            insideItem = false
                            val imageUrl = extractImage(description)
                            val plainContent = description.replace(Regex("<.*?>"), "").trim()

                            newsList.add(
                                News(
                                    id = link,
                                    title = title,
                                    content = plainContent.take(100) + "...",
                                    author = "ITHome",
                                    publishTime = formatDate(pubDate),
                                    imageUrl = imageUrl,
                                    sourceUrl = link,
                                    category = "Tech",
                                    viewType = if (imageUrl != null) News.VIEW_TYPE_SINGLE else News.VIEW_TYPE_TEXT
                                )
                            )
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        newsList
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun extractImage(html: String): String? {
        val pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>")
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

    fun getBanners(): Flow<Result<List<Banner>>> = flow {
        if (cachedNews.isEmpty()) {
            fetchAndParseRss()
        }
        emit(Result.success(getBannersFromNews(cachedNews)))
    }

    private fun getMockData(page: Int, pageSize: Int): NewsListData {
        val mockNews = News.mock(page, pageSize)
        return NewsListData(
            banners = if (page == 1) Banner.mock() else emptyList(),
            news = mockNews,
            hasMore = page < 5
        )
    }
}
