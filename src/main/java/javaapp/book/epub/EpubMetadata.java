package javaapp.book.epub;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EpubMetadata {

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty creator = new SimpleStringProperty();
    private final StringProperty contributor = new SimpleStringProperty();
    private final StringProperty identifier = new SimpleStringProperty();
    private final StringProperty relation = new SimpleStringProperty();
    private final StringProperty publisher = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final StringProperty language = new SimpleStringProperty();


    public EpubMetadata(
            String title,
            String creator,
            String contributor,
            String identifier,
            String relation,
            String publisher,
            String date,
            String subject,
            String language) {
        this.title.set(title);
        this.creator.set(creator);
        this.contributor.set(contributor);
        this.identifier.set(identifier);
        this.relation.set(relation);
        this.publisher.set(publisher);
        this.date.set(date);
        this.subject.set(subject);
        this.language.set(language);
    }

    public static EpubMetadata from(Document content) {
        Element metadata = (Element) content.getElementsByTagName("metadata").item(0);
        String title = retrieve(metadata, "dc:title");
        String creator = retrieve(metadata, "dc:creator");
        String contributor = retrieve(metadata, "dc:contributor");
        String identifier = retrieve(metadata, "dc:identifier");
        String relation = retrieve(metadata, "dc:relation");
        String publisher = retrieve(metadata, "dc:publisher");
        String date = retrieve(metadata, "dc:date");
        String subject = retrieve(metadata, "dc:subject");
        String language = retrieve(metadata, "dc:language");
        return new EpubMetadata(title, creator, contributor, identifier, relation, publisher, date, subject, language);
    }

    private static String retrieve(Element metadata, String tag) {
        Node item = metadata.getElementsByTagName(tag).item(0);
        return item == null ? "" : item.getTextContent();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty creatorProperty() {
        return creator;
    }

    public StringProperty contributorProperty() {
        return contributor;
    }

    public StringProperty identifierProperty() {
        return identifier;
    }

    public StringProperty relationProperty() {
        return relation;
    }

    public StringProperty publisherProperty() {
        return publisher;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public StringProperty languageProperty() {
        return language;
    }

    public String getTitle() {
        return title.get();
    }

    public String getCreator() {
        return creator.get();
    }

    public String getContributor() {
        return contributor.get();
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public String getRelation() {
        return relation.get();
    }

    public String getPublisher() {
        return publisher.get();
    }

    public String getDate() {
        return date.get();
    }

    public String getSubject() {
        return subject.get();
    }

    public String getLanguage() {
        return language.get();
    }
}
