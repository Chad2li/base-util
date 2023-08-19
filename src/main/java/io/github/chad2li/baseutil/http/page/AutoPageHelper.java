package io.github.chad2li.baseutil.http.page;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.github.chad2li.baseutil.http.vo.res.PagerReq;
import io.github.chad2li.baseutil.util.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * 自动分页工具
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 15:26
 */
public class AutoPageHelper {

    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    public static final String SORT_ID = "id";
    /**
     * 默认的 page size
     */
    @Setter
    public static int pageSize = 20;
    private static final ThreadLocal<PageCache> PAGE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置分页信息
     *
     * @param req 分页信息
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    public static void setPage(PagerReq req) {
        Assert.notNull(req);
        PAGE_THREAD_LOCAL.set(new PageCache<>(req, null));
    }

    /**
     * 获取分页信息
     *
     * @return 分页缓存
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    @Nullable
    public static PageCache<?> getPage() {
        return PAGE_THREAD_LOCAL.get();
    }

    /**
     * 清除缓存
     *
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    public static void clear() {
        PAGE_THREAD_LOCAL.remove();
    }

    /**
     * 获取分页方式
     *
     * @param cachePage   缓存信息
     * @param defaultPage 默认信息
     * @return 分页方式，优先使用缓存信息中的，如果缓存没有则取默认信息
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    private static String getSortDirection(@Nullable PagerReq cachePage,
                                           @Nullable PagerReq defaultPage) {
        if (null != cachePage && CharSequenceUtil.isNotEmpty(cachePage.getSortDirection())) {
            // 优先使用 cachePage
            return cachePage.getSortDirection();
        }
        if (null != defaultPage && CharSequenceUtil.isNotEmpty(defaultPage.getSortDirection())) {
            return defaultPage.getSortDirection();
        }
        return SORT_DESC;
    }

    /**
     * 获取分页关键字
     *
     * @param cachePage   缓存信息
     * @param defaultPage 默认信息
     * @return 分页关键字，优先使用缓存信息中的，如果缓存没有则取默认信息，如果默认信息也没有，则返回 null
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    @Nullable
    private static String getSortKey(@Nullable PagerReq cachePage, @Nullable PagerReq defaultPage) {
        if (null != cachePage && CharSequenceUtil.isNotEmpty(cachePage.getSortKey())) {
            // 优先使用 cachePage
            return cachePage.getSortKey();
        }
        if (null != defaultPage && CharSequenceUtil.isNotEmpty(defaultPage.getSortKey())) {
            // 再取 defaultPage
            return defaultPage.getSortKey();
        }
        // 默认null
        return null;
    }

    /**
     * 获取 page number
     *
     * @param cachePage   缓存信息
     * @param defaultPage 默认信息
     * @return page number，优先使用缓存信息中的，如果缓存没有则取默认信息，如果默认信息也没有，则返回 1
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    private static int getPageNum(@Nullable PagerReq cachePage, @Nullable PagerReq defaultPage) {
        if (null != cachePage && NumberUtils.isPositive(cachePage.getPn())) {
            // 优先使用 cachePage
            return cachePage.getPn();
        }
        if (null != defaultPage && NumberUtils.isPositive(defaultPage.getPn())) {
            // 再取 defaultPage
            return defaultPage.getPn();
        }
        // 默认 1
        return 1;
    }

    /**
     * 获取 page size
     *
     * @param cachePage   缓存信息
     * @param defaultPage 默认信息
     * @return page size，优先使用缓存信息中的，如果缓存没有则取默认信息，如果默认信息也没有，
     * 则返回 {@link AutoPageHelper#pageSize}
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    private static int getPageSize(@Nullable PagerReq cachePage, @Nullable PagerReq defaultPage) {
        if (null != cachePage && NumberUtils.isPositive(cachePage.getPs())) {
            // 优先使用 cachePage
            return cachePage.getPs();
        }
        if (null != defaultPage && NumberUtils.isPositive(defaultPage.getPs())) {
            // 再取 defaultPage
            return defaultPage.getPs();
        }
        // 默认
        return pageSize;
    }

    /**
     * 拼接 sortKey 和 sortDirection，并将 sortKey 转db模式
     *
     * @param sortKey       sort key
     * @param sortDirection sort direction
     * @return order by
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    @Nullable
    private static String parseSort(@Nullable String sortKey, @Nullable String sortDirection) {
        if (CharSequenceUtil.isEmpty(sortKey)) {
            return null;
        }
        // 驼峰转换
        String sortKeyOnDb = StrUtil.toUnderlineCase(sortKey);
        sortDirection = CharSequenceUtil.isNotEmpty(sortDirection) ? sortDirection : SORT_DESC;
        return sortKeyOnDb + " " + sortDirection;
    }

    /**
     * 启动分页
     * <p>
     * 需在sql上一条执行。仅对紧接的第1条sql有效
     * </p>
     *
     * @param cachePage   缓存分页
     * @param defaultPage 默认分页
     * @return page
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPage(@Nullable PagerReq cachePage,
                                        @Nullable PagerReq defaultPage) {
        Page<E> page;
        // pn, ps
        int pn = getPageNum(cachePage, defaultPage);
        int ps = getPageSize(cachePage, defaultPage);
        // 调用 pagehelper
        page = PageHelper.startPage(pn, ps);
        // order by
        String sortKey = getSortKey(cachePage, defaultPage);
        String sortDirection = getSortDirection(cachePage, defaultPage);
        String sort = parseSort(sortKey, sortDirection);
        page.setOrderBy(sort);

        return page;
    }

    // ############################# 重载方法 #############################

    /**
     * 指定默认值分页
     *
     * @author chad
     * @see AutoPageHelper#startPage(PagerReq, PagerReq)
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPage(@Nullable Integer pn, @Nullable Integer ps,
                                        @Nullable String sortKey, @Nullable String sortDirection) {
        PageCache<E> cachePage = PAGE_THREAD_LOCAL.get();
        PagerReq defaultPage = new PagerReq(pn, ps, sortKey, sortDirection);
        if (null == cachePage) {
            return startPage(null, defaultPage);
        }
        Page<E> page = startPage(cachePage.getReq(), defaultPage);
        // 缓存结果
        cachePage.setPage(page);
        return page;
    }

    /**
     * 分页重载方法
     *
     * @author chad
     * @see AutoPageHelper#startPage(Integer, Integer, String, String)
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPage() {
        return startPage(null, null, null, null);
    }

    /**
     * 分页重载方法
     *
     * @author chad
     * @see AutoPageHelper#startPage(Integer, Integer, String, String)
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPageByIdDesc() {
        return startPage(null, null, SORT_ID, SORT_DESC);
    }

    /**
     * 分页重载方法
     *
     * @author chad
     * @see AutoPageHelper#startPage(Integer, Integer, String, String)
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPage(@Nullable Integer pn, @Nullable Integer ps) {
        return startPage(pn, ps, null, null);
    }

    /**
     * 分页重载方法
     *
     * @author chad
     * @see AutoPageHelper#startPage(Integer, Integer, String, String)
     * @since 1 by chad at 2023/8/19
     */
    public static <E> Page<E> startPage(@Nullable String defaultSortKey,
                                        @Nullable String defaultSortDirection) {
        return startPage(null, null, defaultSortKey, defaultSortDirection);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageCache<E> {
        /**
         * 分页入参
         */
        private PagerReq req;
        /**
         * pagehelper的分页缓存
         */
        private Page<E> page;
    }
}
