package util;


import java.io.Serializable;


public class Chapter implements Serializable {

    private String name;
    private String chapterURL;
    private String fileName;
    private String chapterContent;

    public Chapter(String name, String link, int marker) {
        this.setName(name.trim());
        this.setChapterURL(link);
        setFileName(String.valueOf(marker));
    }
    public Chapter(String name, String link, String marker) {
        this.setName(name.trim());
        this.setChapterURL(link);
        setFileName(marker);
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public String getChapterURL() {
        return chapterURL;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChapterURL(String chapterURL) {
        this.chapterURL = chapterURL;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}