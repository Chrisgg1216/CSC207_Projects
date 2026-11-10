package usecase.port;

import java.util.List;

/**
 * Narrow port: category lookup. Person C owns category management and should
 * implement this.
 */
public interface CategoryDataAccess {

    boolean exists(String categoryName);

    List<String> findAllNames();
}
