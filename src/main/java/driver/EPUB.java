package driver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import util.Chapter;
import util.NovelMetadata;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles the creation of the driver.EPUB file.
 */
public class EPUB {
    static final String NL = System.getProperty("line.separator");
    static final String htmlHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + NL+
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"" + NL +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" + NL +
            "\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + NL +
            "<head>" + NL +
            "<title></title>" + NL +
            "</head>" + NL +
            "<body>" + NL;
    static final String htmlFoot = "</body>" + NL + "</html>";
    private final NovelMetadata novelMetadata;
    private Book book;

    public EPUB(NovelMetadata metadata) {
        this.novelMetadata = metadata;
        if (book == null) {
            book = new Book();
            book.getResources().add(new Resource("default.css"));
        }
    }

    public void write(List<Chapter> chapterList, String saveLocation) {
        // Order is important
        addMetadata();
        addToc(chapterList);
        addChapters(chapterList);

        String epubFilename = novelMetadata.getAuthor() + "-" + novelMetadata.getTitle();
        epubFilename += ".epub";
        try {
            System.out.println("writing to file");
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(book, new FileOutputStream(saveLocation + "/" + epubFilename));
            System.out.println("done");
        } catch (IOException e) {
        }
    }


    private void addChapters(List<Chapter> chapterList) {
        for(Chapter chapter: chapterList) {
                String chapterString = htmlHead + "<h2>" + chapter.getName() + "</h2><br/>" + chapter.getChapterContent() + htmlFoot;
                try(InputStream inputStream = new ByteArrayInputStream(chapterString.getBytes(StandardCharsets.UTF_8))) {
                    Resource resource = new Resource(inputStream, chapter.getFileName() + ".html");
                    book.addSection(chapter.getName(), resource);
                } catch (IOException e) {
                }
            }
        }


    private void addMetadata() {
        Metadata metadata = book.getMetadata(); // driver.EPUB metadata
        metadata.addTitle(novelMetadata.getTitle());
        metadata.addAuthor(new Author(novelMetadata.getAuthor()));
    }


    public void addToc(List<Chapter> chapterList) {
        StringBuilder tocBuilder = new StringBuilder(htmlHead + NL +
                "<b>Table of Contents</b>" + NL +
                "<p style=\"text-indent:0pt\">" + NL);
        for (Chapter chapter: chapterList) {
                tocBuilder.append("<a href=\"").append(chapter.getFileName()).append(".html\">").append(chapter.getName()).append("</a><br/>").append(NL);
        }
        tocBuilder.append("</p>").append(NL).append(htmlFoot);

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.syntax(Document.OutputSettings.Syntax.xml);
        settings.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        settings.charset("UTF-8");

        Document doc = Jsoup.parse(tocBuilder.toString());
        doc.outputSettings(settings);

        try (InputStream inputStream = new ByteArrayInputStream(doc.html().getBytes(StandardCharsets.UTF_8))) {
            Resource resource = new Resource(inputStream, "table_of_contents.html");
            book.addSection("Table of Contents", resource);
        } catch (IOException e) {
        }
    }

    public void addDesc() {
        String descString = htmlHead + NL+
                "<div><b>Description</b>" + NL +
                "<p>" + novelMetadata.getDescription() + "</p>" + NL +
                "</div>" + NL +
                htmlFoot;

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.syntax(Document.OutputSettings.Syntax.xml);
        settings.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        settings.charset("UTF-8");

        Document doc = Jsoup.parse(descString);
        doc.outputSettings(settings);

        try (InputStream inputStream = new ByteArrayInputStream(doc.html().getBytes(StandardCharsets.UTF_8))) {
            Resource resource = new Resource(inputStream, "desc_Page.html");
            book.addSection("Description", resource);
        } catch (IOException e) {
        }
    }
}
