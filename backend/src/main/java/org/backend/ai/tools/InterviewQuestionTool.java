package org.backend.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InterviewQuestionTool {

    /**
     * 从面试鸭网站获取关键词相关的面试题列表
     *
     * @param keyword 搜索关键词（如"redis"、"java多线程"）
     * @return 面试题列表，若失败则返回错误信息
     */
    @Tool(name = "interviewQuestionSearch", value = """
                Retrieves relevant interview questions from mianshiya.com based on a keyword.
                Use this tool when the user asks for interview questions about specific technologies,
                programming concepts, or job-related topics. The input should be a clear search term.
                """
    )
    public String searchInterviewQuestions(@P(value = "the keyword to search") String keyword) {
        List<String> questions = new ArrayList<>();
        // 构建搜索URL（编码关键词以支持中文）
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://www.mianshiya.com/search/all?searchText=" + encodedKeyword;
        // 发送请求并解析页面
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
        } catch (IOException e) {
            log.error("get web error", e);
            return e.getMessage();
        }
        // 提取面试题
        Elements questionElements = doc.select(".ant-table-cell > a");
        questionElements.forEach(el -> questions.add(el.text().trim()));
        return String.join("\n", questions);
    }

    /**
     * 从 StackOverflow 进行搜索（示例第二个网站）
     *
     * @param keyword 搜索关键词
     * @param maxResults 最大返回结果数量
     * @return 搜索结果
     */
    @Tool(name = "stackOverflowSearch", value = """
                Retrieves relevant answers from StackOverflow based on a keyword.
                Use this tool when the user asks for code solutions or specific programming problems.
                """
    )
    public String searchStackOverflow(
            @P(value = "the keyword to search") String keyword,
            @P(value = "the maximum number of results to return") int maxResults) {
        List<String> results = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://stackoverflow.com/search?q=" + encodedKeyword;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            Elements resultElements = doc.select(".s-post-summary--content-title > a");
            for (int i = 0; i < resultElements.size() && i < maxResults; i++) {
                var el = resultElements.get(i);
                results.add(el.text().trim() + " - " + el.attr("href"));
            }
        } catch (IOException e) {
            log.error("StackOverflow search error", e);
            return e.getMessage();
        }

        return results.isEmpty() ? "No results found." : String.join("\n", results);
    }

    @Tool(name="niukewangSearch", value = """
                Retrieves relevant interview questions from nowcoder.com based on a keyword.
                Use this tool when the user asks for interview questions about specific technologies,
                programming concepts, or job-related topics. The input should be a clear search term.
                """
    )
    public String searchNiukewang(
            @P(value = the keyword to search) String keyword
    ) {

}