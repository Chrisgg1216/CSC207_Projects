# Person A — Transactions / Milestone 2

## User story and use case

As a user spending in multiple currencies, I want to record income and spending
with a dated CAD reference amount so I can see each transaction and track my
monthly category budgets.

Use case: AddTransaction. Input: transaction date, income/spending type, existing
category, positive amount (at most 2 decimal places), CAD/USD/CNY, optional note.
Preconditions: the app is running; a category is available. Main flow: validate,
obtain a quote if needed, save one transaction, present the updated list. CAD
uses identity conversion. Unsupported currency/invalid input is rejected without
saving. Missing or failed foreign quotes save a pending transaction; pending
amounts are not included in budget spending. Postcondition: a successful record
is available through the shared transaction repository for this session.

## Implementation / interfaces

`TransactionsPanel → TransactionController → AddTransactionInteractor →
TransactionDataAccess / ExchangeRateGateway → TransactionPresenter →
TransactionViewModel → TransactionsPanel`.

- `entity`: Transaction, TransactionType, ExchangeRate; reuses existing Money.
- `usecase/add_transaction`: boundaries, input/output data and Interactor.
- `usecase/port/TransactionDataAccess`: save + findAll, shared with Person C.
- `framework/InMemoryTransactionDataAccess`: temporary implementation.
- `framework/TransactionSpendingDataAccess`: implements the existing Budget
  port, filtering month, category, spending type and completed conversion.
- `framework/BankOfCanadaExchangeRateGateway`: bounded HTTP timeouts, JSON
  parsing, in-session successful-quote cache, latest valid observation within
  the 14 calendar days ending on the requested date (never a future quote).
- `app/ApplicationBuilder`: one repository instance used by Transactions and
  Budget. Summary remains Person C's placeholder.

The UI captures inputs on the JavaFX thread, performs saving/HTTP on a background
Task, disables duplicate submission while working and updates controls on the
JavaFX thread. Inner packages do not import JavaFX/HTTP/file libraries.

## API evidence

Official docs: https://www.bankofcanada.ca/valet/docs/
Rate information: https://www.bankofcanada.ca/rates/exchange/daily-exchange-rates-/

Live Java calls verified on 2026-11-19 using `app.ExchangeRateSmokeCheck 2026-11-19`:

```text
USD → CAD | requested 2026-11-19 | observed 2026-11-19 | rate 1.4317 | Bank of Canada
CNY → CAD | requested 2026-11-19 | observed 2026-11-19 | rate 0.2148 | Bank of Canada
```

These are reference estimates, not card settlement rates. No API key is needed.
The check makes real network calls and exits unsuccessfully if either currency
has no quote or cannot be retrieved. Ordinary unit tests use injected fake
responses and do not rely on the service being online.

## Tests

`mvn test`: 74 tests passed, 0 failures/errors/skips on 2026-11-19, including the
original 46 tests and 28 added cases. Coverage includes full controller →
Interactor → repository → Presenter flow, Budget integration, decimal rounding,
invalid inputs, month/category/type filtering, pending conversions, weekend and
out-of-range quotes, caching, empty/malformed responses and network failures.

## TA demo

1. Run `app.Launcher`; select Food, SPENDING, CAD, amount 30, today's date; Add.
2. Show the list contains 30 CAD; add CAD 100 as INCOME and show the separate type.
3. Open Budget; set Food limit 100 for the same month. Spending should be 30,
   remaining 70; income must not inflate spending.
4. Return to Transactions; add USD 50. Show original amount, CAD estimate and
   quote observation date. For the verified 2026-11-19 rate this is CAD 71.59.
5. In Budget, Refresh status: Food spending should be CAD 101.59 and OVER.
6. Submit invalid amount text: show validation and no extra transaction.
7. For a reproducible failed-API case, show `apiFailureSavesPendingInsteadOfFakeZero`
   in AddTransactionInteractorTest. This is fake-response evidence, separate from
   the real successful API demonstration above.

## Limits and handoff

This milestone does not implement transaction edit/delete, conversion retry,
JSON persistence, Summary or dynamic base-currency settings. Data resets when
the program exits. No financial records or real account data are included.

Person C should implement TransactionDataAccess and persist explicit JSON DTOs
(amount/rate decimal strings, ISO dates, optional quote), reconstructing the
domain objects on load. Avoid direct reflective Gson serialization of Optional
or Java date objects. Preserve original currency, rate date and pending state.
Only ApplicationBuilder needs to swap in the durable repository.

GitHub PR/review and a short personal walkthrough are still needed for the full
individual milestone evidence. A class skeleton alone is not a complete use case.
