package util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtil<T> {
    private Logger logger;
    private FileHandler fh;
    private SimpleFormatter sf;

    public LogUtil(Class<T> tClass) throws IOException {
        logger = Logger.getLogger(tClass.getName());
        fh = new FileHandler(Paths.get("").normalize() + "\\lib.log");
        sf = new SimpleFormatter();
        logger.addHandler(fh);
        fh.setFormatter(sf);
        logger.setUseParentHandlers(false);
    }

    public Logger getLogger() {
        return logger;
    }
}
