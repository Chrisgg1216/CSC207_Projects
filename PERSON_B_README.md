# Person B: Budgets and alerts

Use cases: SetBudget, CheckBudgetStatus. Screen: BudgetPanel.

## Where things go in the team project
Copy `src/main/java/*` and `src/test/java/*` into the shared project, on a branch such as
`feature/set-budget`. Merge the pom.xml dependencies (javafx-controls, junit-jupiter) rather than
replacing the team's pom.xml.

## Placeholders to swap for the team's shared code
- entity/Money.java      -> shared Money (keep method names or update callers)
- entity/Category.java   -> shared Category (Person C)
- framework/InMemoryBudgetDataAccess -> Person C's JSON storage, same BudgetDataAccess interface

## Ports other people implement
- usecase/port/SpendingDataAccess  (Person A): total spent per category and month in the base
  currency, excluding transactions pending conversion, plus a count of those pending ones.
- usecase/port/CategoryDataAccess  (Person C): category exists / list names.
- BudgetController has a TODO to read the base currency from Person A's SetBaseCurrency.

## Status rules
spent < 80% of limit -> OK; 80% up to and including 100% -> WARNING; above 100% -> OVER.
Threshold is DEFAULT_WARNING_THRESHOLD in CheckBudgetInteractor (changeable by constructor).

## Run
mvn test                      (46 tests, including ArchitectureRulesTest)
mvn javafx:run                (main window with tabs: app.MainApp)

## Clean Architecture and GUI
- docs/ARCHITECTURE.md: layers, dependency rule, SetBudget sequence diagram, how to add a feature.
- app/ApplicationBuilder: the one place that wires every layer; each person swaps their tab in here.
- app/MainApp: main window with Transactions (A, placeholder), Budget (B, built), Summary (C, placeholder).
- ArchitectureRulesTest fails if entity/usecase/adapter import a layer they must not know.
