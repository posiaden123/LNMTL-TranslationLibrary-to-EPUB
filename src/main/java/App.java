import driver.EPUB;
import driver.WebWorker;
import util.Chapter;
import util.NovelMetadata;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {
        String link = JOptionPane.showInputDialog("lnmtl novel link");
        String lib = JOptionPane.showInputDialog("translator to use. ex: bd, nt, rv");
        WebWorker worker = new WebWorker(link,lib);
        NovelMetadata metadata = worker.getMetadata();
        List<Chapter> chapterList = worker.getBookContent();
        worker.quit();
        EPUB book = new EPUB(metadata);
        String save = JOptionPane.showInputDialog("Enter the path to save the epub to");
        book.write(chapterList,save);
    }
}
