package org.recap.util.datadump;

/**
 * Created by peris on 11/27/16.
 */
public class BatchCounter {

    private static Integer currentPage;
    private static Integer totalPages;

    private BatchCounter() {
    }

    /**
     * Sets current page.
     *
     * @param pageNum the page num
     */
    public static synchronized void setCurrentPage(Integer pageNum) {
        currentPage = pageNum;
    }

    /**
     * Sets total pages.
     *
     * @param totalPageCount the total page count
     */
    public static synchronized void setTotalPages(Integer totalPageCount) {
        totalPages = totalPageCount;
    }

    /**
     * Reset.
     */
    public static void reset() {
        currentPage = 0;
        totalPages = 0;
    }

    /**
     * Gets current page.
     *
     * @return the current page
     */
    public static synchronized Integer getCurrentPage() {
        return currentPage;
    }

    /**
     * Gets total pages.
     *
     * @return the total pages
     */
    public static synchronized Integer getTotalPages() {
        return totalPages;
    }
}
