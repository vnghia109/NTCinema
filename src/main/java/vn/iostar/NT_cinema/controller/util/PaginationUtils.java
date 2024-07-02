package vn.iostar.NT_cinema.controller.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

public class PaginationUtils {
    public static <T> Page<T> paginate(List<T> list, Pageable pageable) {
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<T> pageSlice;
        if (startItem >= list.size()) {
            pageSlice = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, list.size());
            pageSlice = list.subList(startItem, toIndex);
        }

        return new PageImpl<>(pageSlice, pageable, list.size());
    }
}
