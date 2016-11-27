package org.recap.util.datadump;

/**
 * Created by peris on 11/27/16.
 */
public class BatchCounter {

    private static Integer currentPage;
    private static Integer totalPages;

    private BatchCounter() {
    }

    public synchronized static void setCurrentPage(Integer pageNum) {
        currentPage = pageNum;
    }

    public synchronized static void setTotalPages(Integer totalPageCount) {
        totalPages = totalPageCount;
    }

    public static void reset() {
        currentPage = 0;
        totalPages = 0;
    }

    public synchronized static Integer getCurrentPage() {
        return currentPage;
    }

    public synchronized static Integer getTotalPages() {
        return totalPages;
    }
}
