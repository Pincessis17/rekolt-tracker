package mu.rekolt.model;

/**
 * Objective 5: the second capability interface, also implemented by both
 * Delivery and Member. Anything Reportable can produce one line of report
 * text about itself, in whatever format makes sense for that type.
 */
public interface Reportable {
    String reportText();
}
