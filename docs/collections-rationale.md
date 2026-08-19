# Collections rationale (Objective 3)

Every collection below was picked for a specific access pattern, not by default.
Where relevant, an alternative that was considered and rejected is named.

## Arrays

**`PriceListEntry[] PRICE_LIST`** (in `PaymentCalculator`) holds the four produce
rows from rule 1 and rule 3. It is fixed at four elements, known at compile time,
and never grows or shrinks - a plain array is the simplest fit. A `HashMap<String,
PriceListEntry>` was considered, since it would make the lookup O(1) instead of a
four-element linear scan, but with only four entries the difference is meaningless
and the array keeps the whole price list visible as one literal block, matching
how it's laid out in the spec's own table.

**`double[][] grid`** (in `printWeeklyGrid`) is a genuine matrix: weeks as rows,
produce codes as fixed columns. Its dimensions are known once the deliveries are
scanned for the highest week used, and never change after that. A `List<List
<Double>>` was considered and rejected - it would need the same nested-loop
construction but adds boxing and indirection for no benefit, since the grid's
shape never changes size after it's built.

## ArrayList

**`List<Delivery> deliveries`** replaces the fixed-capacity array from Objective 2.
Deliveries are almost always appended at the end and iterated in full (for the
weekly grid, the totals, the sorting) and rarely inserted or removed from the
middle, so `ArrayList`'s O(1) amortised append and cache-friendly iteration suit
it well. A `LinkedList` was considered and rejected: it only wins when insertion
or removal happens in the *middle* of the list often, which this program never
does - `Delivery` objects are treated as immutable once recorded.

## HashMap

**`Map<String, Double> totalPaymentByMember`** is updated once per delivery with
`merge(memberId, netPayable, Double::sum)` and read by member ID when printing
totals. `HashMap` gives O(1) average lookup and update by key, which is all this
needs - there's no requirement to keep members in any particular order. A `TreeMap`
was considered and rejected, since sorted iteration isn't needed here and would
cost O(log n) per update for no benefit.

**`Map<String, List<Delivery>> deliveriesByMember`** groups each member's own
deliveries so they don't need to be found by re-scanning the whole season every
time. Same justification as above for the `HashMap` itself; the value type is an
`ArrayList` for the same append/iterate reasons given above.

## HashSet

**`Set<String> memberIds`** only needs to answer "have I seen this member ID
before" and "how many distinct members are there" - it never needs to preserve
insertion order or be iterated in a particular sequence. `HashSet` gives O(1)
average membership testing and insertion, and de-duplicates automatically. An
`ArrayList` with a manual `contains()` check before adding was considered and
rejected: `contains()` on a list is O(n), so checking every new delivery's member
against a growing list would get slower as the season goes on, where the set stays
flat.

## Comparable and Comparator

**`Delivery implements Comparable<Delivery>`**, ordering by `deliveryId`, gives the
type a single sensible default order - the order deliveries were recorded in. This
is what `Collections.sort(list)` uses with no arguments.

**A `Comparator<Delivery>`** (`Comparator.comparingDouble(d -> d.netPayable)
.reversed()`) provides a second, situational ordering - highest value first - used
only for the "top deliveries by value" report. This doesn't belong on `Delivery`
itself as its natural order, since "most valuable first" isn't the type's inherent
identity the way its ID is; a `Comparator` lets the same list be sorted a
different way for one specific report without touching the class.

## Iterator

Removing the REJECT deliveries from the "top deliveries by value" working copy is
done with an explicit `Iterator` and `it.remove()`, not a for-each loop with
`list.remove()` inside it - removing from a list while iterating it with for-each
throws `ConcurrentModificationException`. The removal is deliberately done on a
copy (`new ArrayList<>(deliveries)`), not the season's real list, because REJECT
deliveries must still count towards the weekly volume grid per the spec - they're
only excluded from this one value-ranked report.

## Search with an absent case

**`findDeliveryById`** is a manual linear search over `deliveries`, returning
`Optional<Delivery>` rather than `null` or throwing. Calling code has to handle
both branches explicitly (`isPresent()` / not), so an unknown ID can never cause
a crash - it's demonstrated in the console output with both a real ID and a made-up
one (`D-9999`).
