package com.roommatch.dto;

/** Scalar projection: reading thumbnails must not load each image's parent graph. */
public record ImagePreviewRow(Integer parentId, String url) {}
