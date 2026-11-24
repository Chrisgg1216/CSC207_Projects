# CSC207_Projects

Java 17+ / Maven / Swing GUI budgeting application with Transactions, Budget, Summary and Categories tabs.

## Run

Import `pom.xml` as a Maven project in IntelliJ, then run `app.Launcher`.
Run the tests using Maven's `test` lifecycle goal (or `mvn test`).

## Application features

- Transactions: add positive CAD, USD or CNY income/spending; display original amount,
  CAD reference estimate, quote date, note and pending-conversion status.
- External API: Bank of Canada Valet historical quotes, with a dated quote chosen
  on or before the transaction date. Run `app.ExchangeRateSmokeCheck` to verify
  live USD and CNY calls separately from unit tests.
- Budget: the selected month refreshes automatically after a transaction is saved.
  **Refresh status** also updates it manually. Income and pending conversions are
  excluded from spending totals.
- Summary: choose any date in the month to see income, spending, net, category
  totals and the underlying transactions. Pending conversions stay visible in
  the list and are flagged as excluded from the totals.
- To demo Summary, add November spending of 20 CAD and 30 CAD in Food and 10 CAD
  in Transport, plus a transaction dated in October. Open Summary and select a
  November date. Expected spending: 60 CAD (Food 50 CAD, Transport 10 CAD).
- Categories: add names, rename selected categories, and delete unused categories.
  Renaming updates existing transactions and budgets together. Category names
  are unique without regard to case.
- Local storage: transactions, budgets and categories are saved to
  `~/.csc207-budget/ledger.json`. Reopening the app restores them. If the file
  cannot be read, the app shows an error before opening the ledger.
- CSV: choose a month in Summary, then **Export CSV…**. The UTF-8 file contains
  that month’s transaction rows, original and CAD amounts, and the quote date.
  Rows waiting for conversion have a blank CAD amount and a `PENDING` status.
- Editing/deleting transactions, conversion retry and changing the CAD base
  currency are later work, not completed by this milestone prototype.

See [Person A implementation and demo](PERSON_A_README.md) and
[the architecture guide](docs/ARCHITECTURE.md).
