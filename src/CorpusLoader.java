import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * CorpusLoader - Responsible for loading and preprocessing UTF-8 text corpora.
 * 
 * Key Features:
 * 1. Reads text files strictly using UTF-8 encoding so Indian scripts
 *    (Hindi Devanagari, Telugu, Tamil, Kannada, Bengali, etc.) are preserved intact.
 * 2. Tracks character count, line count, and file byte size.
 * 3. Supports loading default or custom file paths.
 */
public class CorpusLoader {
    private String filePath;
    private String text;
    private int charCount;
    private int lineCount;
    private long byteSize;
    private boolean loaded;
    private String errorMessage;

    public CorpusLoader() {
        this.filePath = "data/corpus.txt";
        this.text = "";
        this.charCount = 0;
        this.lineCount = 0;
        this.byteSize = 0;
        this.loaded = false;
        this.errorMessage = "";
    }

    public CorpusLoader(String filePath) {
        this.filePath = filePath;
        this.text = "";
        this.charCount = 0;
        this.lineCount = 0;
        this.byteSize = 0;
        this.loaded = false;
        this.errorMessage = "";
    }

    /**
     * Loads the text file using UTF-8 encoding.
     * @return true if loaded successfully, false otherwise.
     */
    public boolean load() {
        return load(this.filePath);
    }

    /**
     * Loads a specific text file using UTF-8 encoding.
     * @param path Path to the file.
     * @return true if loaded successfully, false otherwise.
     */
    public boolean load(String path) {
        this.filePath = path;
        File file = new File(path);

        if (!file.exists()) {
            this.loaded = false;
            this.errorMessage = "File not found: " + path;
            return false;
        }

        if (!file.isFile()) {
            this.loaded = false;
            this.errorMessage = "Path is not a valid file: " + path;
            return false;
        }

        StringBuilder sb = new StringBuilder();
        int lines = 0;

        // Use FileInputStream with InputStreamReader specified with UTF-8
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                lines++;
                if (!firstLine) {
                    sb.append("\n");
                }
                sb.append(line);
                firstLine = false;
            }

            this.text = sb.toString();
            this.charCount = this.text.length();
            this.lineCount = lines;
            this.byteSize = file.length();
            this.loaded = true;
            this.errorMessage = "";
            return true;

        } catch (Exception e) {
            this.loaded = false;
            this.errorMessage = "Error reading file: " + e.getMessage();
            return false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getText() {
        return text;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getCharCount() {
        return charCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public long getByteSize() {
        return byteSize;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Formats corpus information for display in the CMD console.
     */
    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();
        info.append("============================================================\n");
        info.append("CORPUS INFORMATION\n");
        info.append("============================================================\n");
        info.append("File:       ").append(filePath).append("\n");
        info.append("Encoding:   UTF-8\n");
        if (loaded) {
            info.append("Status:     Corpus loaded successfully\n");
            info.append("Characters: ").append(charCount).append("\n");
            info.append("Lines:      ").append(lineCount).append("\n");
            info.append("File Size:  ").append(byteSize).append(" bytes\n");
        } else {
            info.append("Status:     Not loaded\n");
            if (!errorMessage.isEmpty()) {
                info.append("Error:      ").append(errorMessage).append("\n");
            }
        }
        info.append("============================================================");
        return info.toString();
    }

    /**
     * Standalone main method to test CorpusLoader from CMD.
     */
    public static void main(String[] args) {
        String path = (args.length > 0) ? args[0] : "data/corpus.txt";
        CorpusLoader loader = new CorpusLoader(path);
        boolean ok = loader.load();
        System.out.println(loader.getDisplayInfo());
        if (ok) {
            System.out.println("\nPreview (First 150 characters):");
            int previewLen = Math.min(150, loader.getText().length());
            System.out.println(loader.getText().substring(0, previewLen) + "...");
        }
    }
}
