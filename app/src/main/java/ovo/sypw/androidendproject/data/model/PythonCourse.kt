package ovo.sypw.androidendproject.data.model

/**
 * Python 课程数据模型 - 参考示例代码 PythonBean.java
 */
data class PythonCourse(
    val id: Int,
    val address: String,      // 地址 (如：北京)
    val content: String,      // 课程内容 (如：人工智能+Python基础班2017-08-18)
    val openClass: String     // 报名状态 (如：我要报名)
) {
    companion object {
        fun mock(): List<PythonCourse> = listOf(
            PythonCourse(1, "北京", "人工智能+Python基础班2024-01-15", "我要报名"),
            PythonCourse(2, "上海", "Python数据分析实战班2024-02-20", "我要报名"),
            PythonCourse(3, "广州", "Python爬虫进阶班2024-03-10", "我要报名"),
            PythonCourse(4, "深圳", "Python全栈开发班2024-04-05", "我要报名"),
            PythonCourse(5, "杭州", "机器学习+Python项目班2024-05-12", "我要报名"),
            PythonCourse(6, "成都", "Python自动化测试班2024-06-18", "我要报名"),
            PythonCourse(7, "武汉", "Python Web开发班2024-07-22", "我要报名"),
            PythonCourse(8, "南京", "Python量化交易班2024-08-30", "已报满"),
            PythonCourse(9, "西安", "Python人工智能高级班2024-09-15", "我要报名"),
            PythonCourse(10, "重庆", "Python云计算开发班2024-10-25", "我要报名")
        )
    }
}
