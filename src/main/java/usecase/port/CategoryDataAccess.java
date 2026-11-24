package usecase.port;

import java.util.List;

/** Category lookup for transactions and budgets. */
public interface CategoryDataAccess {

    boolean exists(String categoryName);

    List<String> findAllNames();
}
