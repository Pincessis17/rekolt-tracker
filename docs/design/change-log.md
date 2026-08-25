# Change log: deviations from design-v1.pdf

This file records every place the built code (from Objective 5 onwards)
differs from the paper design in `docs/design/design-v1.pdf`, with the
reason for each change.

## Objective 5 — abstraction and inheritance

1. **`Produce.label` clarified to hold the specific commodity name.**
   The paper design showed a `- label : String` field on `Produce` without
   saying what it holds. In the build, `label` is the specific commodity
   name (Maize, Beans, Potatoes, Tea), kept distinct from `categoryLabel()`,
   which returns the broader category (Cereal, Perishable, Cash crop). This
   is a clarification rather than a structural change, but the design
   didn't make the distinction explicit, so it's logged here.

2. **`Produce`'s constructor is `protected`, not public as drawn.**
   The UML showed `+ Produce(code, label, basePricePerKg)` with public
   visibility. Since `Produce` is abstract, a public constructor would be
   misleading — it can only ever be reached via a subclass's `super()`
   call, so the build uses `protected` instead.

3. **`SeasonStore.weeklyGrid()` was designed but not built as a method on
   `SeasonStore`.** The paper design gave `SeasonStore` a
   `+ weeklyGrid() : double[][]` method. In the build, the weekly grid is
   still assembled directly in `Main`, from `store.getDeliveries()`,
   because it depends on `Main`'s own produce-catalog column order (which
   code goes in which column) — pulling the method into `SeasonStore` would
   just mean passing that same column order back in as a parameter, for no
   real encapsulation benefit.

4. **`SeasonStore` and `Member` expose extra getters not shown on the
   paper design.** `SeasonStore.getDeliveries()` / `getMembers()` and
   `Member.getDeliveries()` were added so `Main` can iterate the season's
   data directly when building the on-screen reports (top deliveries by
   value, the weekly grid, deliveries in ID order). The paper design only
   showed each class's own behaviour (`addDelivery`, `findDeliveryById`,
   `payableAmount`, `reportText`); it didn't anticipate `Main` needing raw
   read access to the underlying collections for reporting.
