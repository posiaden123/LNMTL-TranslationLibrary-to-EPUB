package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NovelMetadata {
    private String title = "Unknown";
    private String author = "Unknown";
    private String description = "";

    public NovelMetadata() {
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getDescription() {
        return description;
    }
    public void setTitle(String title) {
        this.title = title.isEmpty() ? "Unknown": title;
    }
    public void setAuthor(String author) {
        this.author = author.isEmpty() ? "Unknown": author;
    }
    public void setDescription(String description) {
        this.description = description.isEmpty() ? "": description;
    }
}
