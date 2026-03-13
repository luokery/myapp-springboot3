package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页响应 DTO
 */
@Data
@Schema(description = "分页响应")
public class PageDTO<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "当前页码（从0开始）", example = "0")
    private int pageNumber;

    @Schema(description = "每页大小", example = "10")
    private int pageSize;

    @Schema(description = "总记录数", example = "100")
    private long totalElements;

    @Schema(description = "总页数", example = "10")
    private int totalPages;

    @Schema(description = "是否第一页")
    private boolean first;

    @Schema(description = "是否最后一页")
    private boolean last;

    @Schema(description = "是否有下一页")
    private boolean hasNext;

    @Schema(description = "是否有上一页")
    private boolean hasPrevious;

    public static <T> PageDTO<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        PageDTO<T> pageDTO = new PageDTO<>();
        pageDTO.setContent(content);
        pageDTO.setPageNumber(pageNumber);
        pageDTO.setPageSize(pageSize);
        pageDTO.setTotalElements(totalElements);
        
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        pageDTO.setTotalPages(totalPages);
        
        pageDTO.setFirst(pageNumber == 0);
        pageDTO.setLast(pageNumber >= totalPages - 1 || totalPages == 0);
        pageDTO.setHasNext(pageNumber < totalPages - 1);
        pageDTO.setHasPrevious(pageNumber > 0);
        
        return pageDTO;
    }
}
