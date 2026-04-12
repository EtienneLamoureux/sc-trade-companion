# extractBoxSizesInScu design

## Problem

`RawCommodityListing.extractBoxSizesInScu()` should stop relying on whitespace-delimited integer parsing. The value from `right.getFragments().getLast().getText()` must first have all whitespace removed, including internal whitespace, and then be interpreted by checking known box sizes from the end of the string.

## Proposed behavior

1. Read `right.getFragments().getLast().getText()`.
2. Remove all whitespace from that value.
3. Iterate over `BOX_SIZES_IN_SCU` in reverse order: `32, 24, 16, 8, 4, 2, 1`.
4. For each size, try to remove its string form from the end of the current value exactly once.
5. When a suffix matches, add that size to a running list and continue with the shortened value.
6. After the single reverse pass, sort the collected values in ascending order.
7. Assign `this.boxSizesInScu` only when the sorted list is either:
   - a strict subset of `BOX_SIZES_IN_SCU`, or
   - the full set of `BOX_SIZES_IN_SCU`.
8. Otherwise, leave `this.boxSizesInScu` empty.

## Notes

- Duplicate values are rejected because accepted results must match distinct members of `BOX_SIZES_IN_SCU`.
- Ordering in the source text does not need a separate ascending check because acceptance is based on membership against the known set after sorting.
- Existing behavior in the method should be disregarded when implementing this change.

## Testing strategy

Use TDD in `RawCommodityListingTest` with focused parameterized cases that cover:

- whitespace stripped before parsing, including internal whitespace,
- valid single-pass suffix matches for subsets,
- valid full-set extraction,
- invalid values that should produce an empty result,
- repeated or leftover content that should prevent assignment.
