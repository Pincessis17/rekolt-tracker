package mu.rekolt.model;

/**
 * Objective 5: a capability interface implemented by more than one class
 * (Delivery and Member). A contract only - no fields, no method bodies -
 * so anything Payable can be asked for its amount without the caller
 * needing to know or care what kind of thing it actually is.
 */
public interface Payable {
    double payableAmount();
}
