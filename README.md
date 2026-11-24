# CSC207_Projects

Java 17+ / Maven / Swing budgeting application with Transactions, Budget and Summary tabs.

## Run

Import `pom.xml` as a Maven project in IntelliJ, then run `app.Launcher`.
Run the tests using Maven's `test` lifecycle goal (or `mvn test`).

## Milestone 2 prototype

- Transactions: add positive CAD, USD or CNY income/spending; display original amount,
  CAD reference estimate, quote date, note and pending-conversion status.
- External API: Bank of Canada Valet historical quotes, with a dated quote chosen
  on or before the transaction date. Run `app.ExchangeRateSmokeCheck` to verify
  live USD and CNY calls separately from unit tests.
- Budget: uses the same transaction store. Select the month and click **Refresh
  status** after adding a transaction. Income and pending conversions are excluded
  from spending totals.
- Summary: choose any date in the month to see income, spending, net, category
  totals and the underlying transactions. Pending conversions stay visible in
  the list and are flagged as excluded from the totals.
- To demo Summary, add November spending of 20 CAD and 30 CAD in Food and 10 CAD
  in Transport, plus a transaction dated in October. Open Summary and select a
  November date. Expected spending: 60 CAD (Food 50 CAD, Transport 10 CAD).
- Records are currently in memory and reset on exit. JSON persistence, category
  management and CSV export are follow-up work.
- Editing/deleting transactions, conversion retry and changing the CAD base
  currency are later work, not completed by this milestone prototype.

See [Person A implementation and demo](PERSON_A_README.md) and
[the architecture guide](docs/ARCHITECTURE.md).
