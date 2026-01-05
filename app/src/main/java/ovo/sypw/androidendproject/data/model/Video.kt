package ovo.sypw.androidendproject.data.model

/**
 * 视频数据模型 - 参考示例代码 VideoBean.java
 */
data class Video(
    val id: String,
    val name: String,
    val coverUrl: String,
    val intro: String,
    val videoDetailList: List<VideoDetail> = emptyList()
) {
    companion object {
        // 公开可用的测试视频URL列表
        private val sampleVideoUrls = listOf(
            "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        )

        fun mock(): List<Video> = listOf(
            Video(
                id = "1",
                name = "jQuery精品教程",
                coverUrl = "https://picsum.photos/400/225?random=1",
                intro = "本视频系统的讲解了jQuery的基本操作并配合相应案例保证学生能较大程度的接受和了解到知识的应用，该视频的起点都是针对有一定JavaScript基础的同学精心设计录制的。",
                videoDetailList = listOf(
                    VideoDetail("v1", "01-jQuery初体验", sampleVideoUrls[0]),
                    VideoDetail("v2", "02-什么是jQuery", sampleVideoUrls[1]),
                    VideoDetail("v3", "03-jQuery版本问题", sampleVideoUrls[2]),
                    VideoDetail("v4", "04-jQuery入口函数的解释", sampleVideoUrls[3]),
                    VideoDetail("v5", "05-jq对象与js对象", sampleVideoUrls[4])
                )
            ),
            Video(
                id = "2",
                name = "Android入门教程",
                coverUrl = "https://picsum.photos/400/225?random=2",
                intro = "本教程适合零基础的同学学习Android开发，从环境搭建到项目实战，循序渐进地掌握Android开发技能。",
                videoDetailList = listOf(
                    VideoDetail("v6", "01-Android简介", sampleVideoUrls[5]),
                    VideoDetail("v7", "02-开发环境搭建", sampleVideoUrls[6]),
                    VideoDetail("v8", "03-第一个Android程序", sampleVideoUrls[7])
                )
            ),
            Video(
                id = "3",
                name = "Java基础教程",
                coverUrl = "https://picsum.photos/400/225?random=3",
                intro = "Java是一种广泛使用的计算机编程语言，本教程将带你从零开始学习Java编程，掌握面向对象编程的核心概念。",
                videoDetailList = listOf(
                    VideoDetail("v9", "01-Java概述", sampleVideoUrls[8]),
                    VideoDetail("v10", "02-变量与数据类型", sampleVideoUrls[9]),
                    VideoDetail("v11", "03-运算符", sampleVideoUrls[0]),
                    VideoDetail("v12", "04-流程控制", sampleVideoUrls[0])
                )
            ),
            Video(
                id = "4",
                name = "Python爬虫教程",
                coverUrl = "https://picsum.photos/400/225?random=4",
                intro = "学习如何使用Python进行网络爬虫开发，掌握requests、BeautifulSoup、Scrapy等常用库的使用方法。",
                videoDetailList = listOf(
                    VideoDetail("v13", "01-爬虫简介", sampleVideoUrls[1]),
                    VideoDetail("v14", "02-requests库使用", sampleVideoUrls[2])
                )
            )
        )
    }
}

/**
 * 视频详情项 - 参考示例代码 VideoBean.VideoDetailListBean
 */
data class VideoDetail(
    val videoId: String,
    val videoName: String,
    val videoUrl: String
)
