package driver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import util.Chapter;
import util.LogUtil;
import util.NovelMetadata;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebWorker {
    private int count = 0;
    String link = "";
    String tlib;
    Document toc;
    List<Chapter> contentTable;
    WebDriver driver;
    JFrame frame;
    List<Chapter> finalChapters;
    LogUtil<WebWorker> logger = new LogUtil<>(WebWorker.class);

    public WebWorker(String clink, String tlib) throws IOException {
        this.link = clink;
        this.tlib = tlib;
        this.frame = new JFrame();
        toc = Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:83.0) Gecko/20100101 Firefox/83.0")
                .get();
        try {
            driver = new ChromeDriver(setOptions());
        } catch (InvalidArgumentException e) {
            JOptionPane.showMessageDialog(frame, "The program cannot run while an instance of Chrome is open. Please close Chrome.", "Error Launching Application", JOptionPane.ERROR_MESSAGE);
        }
        contentTable = getChapterList();
        finalChapters = new ArrayList<>();
        logger.getLogger().info("Got chapters");
    }

    public List<Chapter> getBookContent() {
        List<Chapter> fullChapters = new ArrayList<>(contentTable.size());
        List<Chapter> table = contentTable;
        chapterLoop(fullChapters, table);
        return finalChapters;
    }

    private ChromeOptions setOptions() {
        ChromeOptions options = new ChromeOptions();
        String dataDir = JOptionPane.showInputDialog(frame, "Enter the path to your Chrome user data folder.\n" +
                "Most should be located at C:\\Users\\'Your Username'\\AppDataa\\Local\\Google\\Chrome\\User Data");
        String profile = JOptionPane.showInputDialog(frame,"Enter the name of the directory within the previous path that contains both installed" +
                "scripts and extensions");
        options.addArguments("user-data-dir=" + dataDir);
        options.addArguments("profile-directory=" + profile);
        return options;
    }

    public List<Chapter> getChapterList() {
        JFrame loader = new JFrame("Getting Table of Contents");
        ImageIcon load = new ImageIcon("ajax-loader.gif");
        loader.add(new JLabel("getting chapters...", load, JLabel.CENTER));
        loader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loader.setSize(200, 200);
        loader.setVisible(true);
        List<Chapter> chapterList = new ArrayList<>();
        try {
            // Get volume ids
            String lnmtlScript = toc.toString();
            int volArrStartIndex = lnmtlScript.indexOf("lnmtl.volumes = [{");
            int volArrEndIndex = lnmtlScript.indexOf("}];", volArrStartIndex) + 3;
            String volArray = lnmtlScript.substring(volArrStartIndex, volArrEndIndex);
            Pattern pattern = Pattern.compile("\"id\":(.*?),", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(volArray);
            // Loop through volumes
            while (matcher.find()) {
                // Loop through pages inside volumes
                int page = 1;
                while (true) {
                    String json = Jsoup.connect("https://lnmtl.com/chapter?page=" + (page++) + "&volumeId=" + matcher.group(1))
                            .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:83.0) Gecko/20100101 Firefox/83.0")
                            .ignoreContentType(true)
                            .method(Connection.Method.GET)
                            .execute().body();
                    if (parse(chapterList, json)) break;
                }
            }
        } catch (IOException | ParseException | NullPointerException | TimeoutException ignored) {
        }
        loader.setVisible(false);
        loader.dispose();
        return chapterList;
    }

    public boolean parse(List<Chapter> chapterList, String json) throws ParseException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        JSONArray chapterArray = (JSONArray) jsonObject.get("data");
        for (Object chapterObj : chapterArray) {
            count++;
            JSONObject chapter = (JSONObject) chapterObj;
            chapterList.add(new Chapter((String) chapter.get("title"), (String) chapter.get("site_url"), count));
        }
        if (jsonObject.get("next_page_url") == null) return true;
        return false;
    }

    public NovelMetadata getMetadata() {
        NovelMetadata metadata = new NovelMetadata();
        metadata.setTitle(toc.select("meta[property=og:title]").attr("content"));
        metadata.setAuthor(toc.select(".panel-body:contains(Authors) span").first().text());
        return metadata;
    }

    public void quit() {
        driver.quit();
    }

    private void chapterLoop(List<Chapter> fullChapters, List<Chapter> contentList) {
        for (Chapter chapter : contentList) {
            Chapter chap = new Chapter(chapter.getName(), chapter.getChapterURL(), chapter.getFileName());
            try {
                driver.get(chap.getChapterURL());
                new WebDriverWait(driver, 60).until(driver -> driver.findElement(By.className(tlib)));
            } catch (TimeoutException e) {
                logger.getLogger().warning("Translator refused request at chapter " + chapter.getFileName() + ". Retrying");
                driver.navigate().refresh();
            }
                List<WebElement> elements = driver.findElements(By.className(tlib));
                StringBuilder content = new StringBuilder();
                for (WebElement webElement : elements) {
                    String text = "<p>" + webElement.getAttribute("innerText") + "</p>";
                    content.append(text);
                }
                Document.OutputSettings settings = new Document.OutputSettings();
                settings.syntax(Document.OutputSettings.Syntax.xml);
                settings.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
                settings.charset("UTF-8");
                chap.setChapterContent(Jsoup.clean(content.toString(),
                        link,
                        Whitelist.relaxed().preserveRelativeLinks(true),
                        settings));
                fullChapters.add(chap);
                logger.getLogger().info("Got chapter " + chap + " - " + chap.getFileName());
        }
    }
}
