package org.recap.util.datadump;

/**
 * Created by peris on 11/27/16.
 */
public class BatchCounter {

    private static Integer currentPage;
    private static Integer totalPages;

    private BatchCounter() {
    }

    public static synchronized void setCurrentPage(Integer pageNum) {
        currentPage = pageNum;
    }

    public static synchronized void setTotalPages(Integer totalPageCount) {
        totalPages = totalPageCount;
    }

    public static void reset() {
        currentPage = 0;
        totalPages = 0;
    }

    public static synchronized Integer getCurrentPage() {
        return currentPage;
    }

    public static synchronized Integer getTotalPages() {
        return totalPages;
    }
}
