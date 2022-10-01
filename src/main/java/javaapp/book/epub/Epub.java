package javaapp.book.epub;

import javaapp.book.Book;
import javaapp.book.ManifestEntry;
import javaapp.book.SpineEntry;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Epub implements Book {
    final private Path root;

    private EpubMetadata metadata;

    private List<ManifestEntry> manifest;
    private List<SpineEntry> spine;

    public Epub(Path root) {
        this.root = root.toAbsolutePath();
        loadMetadata(true);
        addBookToLib();
        initDataDirectory();
        extractOpf();
        extractImages();
        extractCover();

    }
    public boolean loadMetadata(boolean force) {
        if(this.metadata != null && !force) {
            return true;
        }

        // Load content.opf XML file
        String content_opf = readContentOPF();

        if(content_opf != null) {
            // Parse XML
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document parsed = builder.parse(new InputSource(new StringReader(content_opf)));
                parsed.normalize();
                this.metadata = EpubMetadata.from(parsed);
            } catch (Exception any) {
                any.printStackTrace();
                return false;
            }
        }

        return false;
    }
    public void loadManifest(boolean force) {
        if(this.manifest != null && !force) {
            return;
        }

        manifest = new ArrayList<>();

        // Load content.opf XML file
        String content_opf = readContentOPF();

        if(content_opf != null) {
            // Parse XML
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document parsed = builder.parse(new InputSource(new StringReader(content_opf)));
                parsed.normalize();

                // Grab the manifest tag [<manifest>]
                Element manifestNode = (Element) parsed.getElementsByTagName("manifest").item(0);
                NodeList manifestChildren = manifestNode.getChildNodes();
                for(int i = 0; i < manifestChildren.getLength(); i++) {
                    Node entry = manifestChildren.item(i);

                    if(entry.getNodeType() == Node.ELEMENT_NODE) {
                        String id = entry.getAttributes().getNamedItem("id").getTextContent();
                        String href = entry.getAttributes().getNamedItem("href").getTextContent();
                        String mediaType = entry.getAttributes().getNamedItem("media-type").getTextContent();
                        manifest.add(new ManifestEntry(id, href, mediaType));
                    }
                }
            } catch (Exception any) {
                any.printStackTrace();
            }
        }
    }
    public void loadSpine(boolean force) {
        if(this.spine != null && !force) {
            return;
        }

        spine = new ArrayList<>();

        // Load content.opf XML file
        String content_opf = readContentOPF();

        if(content_opf != null) {
            // Parse XML
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document parsed = builder.parse(new InputSource(new StringReader(content_opf)));
                parsed.normalize();

                // Grab the manifest tag [<manifest>]
                Element manifestNode = (Element) parsed.getElementsByTagName("spine").item(0);
                NodeList manifestChildren = manifestNode.getChildNodes();
                for(int i = 0; i < manifestChildren.getLength(); i++) {
                    Node entry = manifestChildren.item(i);

                    if(entry.getNodeType() == Node.ELEMENT_NODE) {
                        String idref = entry.getAttributes().getNamedItem("idref").getTextContent();
                        spine.add(new SpineEntry(idref));
                    }
                }
            } catch (Exception any) {
                any.printStackTrace();
            }
        }
    }

    public String readContentOPF() {
        return read(path -> path.getFileName() != null && path.getFileName().toString().contains("content.opf"));
    }

    public String read(Predicate<Path> predicate) {
        Optional<Path> containerXML = find(predicate).findFirst();

        if(containerXML.isPresent()) {
            try {
                return Files.readString(containerXML.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Stream<Path> find(Predicate<Path> predicate) {
        try {
            for (Path inner : FileSystems.newFileSystem(root, Collections.emptyMap()).getRootDirectories()) {
                if (Files.isDirectory(inner)) {
                    return Files.walk(inner).filter(predicate);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return Stream.empty();
    }

    public void extractOpf(){

    }

    /**
     * Extracts the image contents of this .epub to a temporary directory.
     *
     * <p>
     * While most operations (reading cover images, loading pages, and getting order) can be done without opening the jar,
     *  images loaded in HTML files will not properly load, and there is no sane way to bypass this while keeping
     *  the images in the jar without replacing the paths at runtime and only extracting the images.
     */
    private void extractImages() {

        find(path -> path.toString().endsWith(".png") || path.toString().endsWith(".jpg")).forEach(path -> {
            new Thread(() -> {
                try {
                    String imagesPath = getImageDirectory().toString();
                    Files.createDirectories(Paths.get(imagesPath));
                    Path target = Paths.get(imagesPath, path.getFileName().toString());
                    if(!Files.exists(target)) {
                        Files.copy(path, target);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }


    public EpubMetadata getMetadata() {
        if(metadata == null) {
            loadMetadata(true);
        }

        return metadata;
    }
    public List<ManifestEntry> getManifest() {
        if(manifest == null) {
            loadManifest(true);
        }

        return manifest;
    }

    public List<SpineEntry> getSpine() {
        if (spine == null) {
            loadSpine(true);
        }
        return spine;
    }
    public Path getImageDirectory() {
        return Paths.get(getDataDirectory().toString(), "Images");
    }


    public Path getDataDirectory() {
        String fileName = root.getFileName().toString();
        return Paths.get(javaapp.book.Book.READER_LIBRARY_DATA_PATH.toString(), fileName.substring(0, fileName.lastIndexOf(".")));
    }

    public Path getConfigDirectory(){
        return Book.READER_LIBRARY_CONFIG_PATH;
    }
    public Path getCssPath(){
        return Paths.get(getConfigDirectory().toString(), "stylesheet.css");
    }

    public Path getCss(){
        Path css = Paths.get(getConfigDirectory().toString(), "stylesheet.css");
        if(Files.exists(css)) {
            System.out.println(css);
            return css;
        }
        return null;
    }
    public String readSection(SpineEntry spineEntry) {
        Optional<ManifestEntry> first = getManifest().stream().filter(entry -> entry.getId().equals(spineEntry.getIdref())).findFirst();

        // If a manifest entry was found that matches the given spine entry, read the contents and return it.
        if(first.isPresent()) {
            return read(path -> path.toString().contains(first.get().getHref()));
        }

        return "";
    }

    public void extractCover() {
        Path coverLocation = getDataDirectory().resolve("cover.png");

        // Do not continue if the cover image already exists.
        if(Files.exists(coverLocation)) {
            return;
        }

        // Locate the content.opf file from this .epub book.
        Optional<Path> contentOpf = find(path -> path.getFileName() != null && path.getFileName().toString().equals("content.opf")).findFirst();

        // abort mission if the container.opf file was not found
        if(contentOpf.isEmpty()) {
            return;
        }

        // get contents of content.opf file
        String contentOPF;
        try {
            contentOPF = Files.readString(contentOpf.get());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }

        if(contentOPF != null) {
            // find cover image location
            String coverTag = find(contentOPF, "<meta name=\"cover\" content=\"", "\"");
            if(coverTag != null) {
                List<String> lines = Arrays.asList(contentOPF.split("\n"));
                String coverItem = null;

                // Attempt to search for the cover image location by ID.
                Optional<String> first = lines.stream().filter(line -> line.contains(String.format("id=\"%s\"", coverTag))).findFirst();
                if(first.isPresent()) {
                    String found = first.get();
                    coverItem = find(found, "href=\"", "\"");
                }

                // In some cases, cover elements are tagged with `properties="cover-image"`.
                // If coverItem is null, there is a good chance the cover image is stored in this method.
                if(coverItem == null) {
                    first = lines.stream().filter(line -> line.contains("properties=\"cover-image\"")).findFirst();
                    if(first.isPresent()) {
                        String found = first.get();
                        coverItem = find(found, "href=\"", "\"");
                    }
                }

                if(coverItem != null) {
                    // If the content.opf file is inside a directory, and refers to a file in the same directory, this coverItem path
                    //    will refer to the file relative to content.opf.
                    // To fix this, we prefix the coverItem path with the content.opf path, if it exists.
                    String directory = contentOpf.get().getParent().toString();
                    coverItem = directory + (directory.endsWith("/") ? "" : "/") + coverItem;

                    // Locate the cover image.
                    final String finalCoverItem = coverItem;
                    Optional<Path> imagePath = find(path -> path.toString() != null && path.toString().equals(finalCoverItem)).findFirst();
                    if(imagePath.isPresent()) {
                        // Extract the file to the data directory.
                        try {
                            Path target = Paths.get(getDataDirectory().toString(), "cover.png");
                            if(!Files.exists(target)) {
                                Files.copy(imagePath.get(), target);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return;
    }

    public Image getCover(){
        Path coverLocation = getDataDirectory().resolve("cover.png");
        if(Files.exists(coverLocation)) {
            System.out.println(coverLocation);
            File cover = new File(coverLocation.toUri().toString());
            return new Image(String.valueOf(cover));
        }
        return null;
    }

    private String find(String from, String start, String end) {
        if(!from.contains(start) || !from.contains(end)) {
            return null;
        }

        int startIndex = from.indexOf(start) + start.length();
        int endIndex = from.indexOf(end, startIndex);
        return from.substring(startIndex, endIndex);
    }
    public void addBookToLib() {
        Path bookLibPath = Paths.get(String.valueOf(READER_LIBRARY_PATH), this.root.getFileName().toString());
        if (!Files.exists(bookLibPath)) {
            try {
                Files.copy(this.root, bookLibPath);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    public void initDataDirectory() {
        Path dataDirectory = getDataDirectory();

        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
