package usecase.manage_categories;

import java.util.List;

public interface CategoryManagementOutputBoundary {
    void present(List<String> categories, String message, boolean error);
}
