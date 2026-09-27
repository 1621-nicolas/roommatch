package com.roommatch.service;

import com.roommatch.dto.ImagePreviewRow;
import com.roommatch.repository.ImagenHabitacionRepository;
import com.roommatch.repository.ImagenPublicacionRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Called only after the parent list/detail has passed its visibility/ownership checks. */
@Service
@Transactional(readOnly = true)
public class ImagePreviewService {
    private final ImagenHabitacionRepository rooms;
    private final ImagenPublicacionRepository publications;
    private final ImageUrlPolicy urls;

    public ImagePreviewService(ImagenHabitacionRepository rooms, ImagenPublicacionRepository publications, ImageUrlPolicy urls) {
        this.rooms = rooms; this.publications = publications; this.urls = urls;
    }

    public Map<Integer, String> rooms(Collection<Integer> ids) {
        var parents = ids.stream().filter(Objects::nonNull).distinct().toList();
        return parents.isEmpty() ? Map.of() : select(rooms.findPreviews(parents));
    }

    public Map<Integer, String> publications(Collection<Integer> ids) {
        var parents = ids.stream().filter(Objects::nonNull).distinct().toList();
        return parents.isEmpty() ? Map.of() : select(publications.findPreviews(parents));
    }

    private Map<Integer, String> select(List<ImagePreviewRow> ordered) {
        Map<Integer, String> previews = new HashMap<>();
        for (var row : ordered) {
            if (previews.containsKey(row.parentId())) continue;
            try {
                previews.put(row.parentId(), urls.validate(row.url()));
            } catch (IllegalArgumentException ignored) {
                // Preserve invalid historical data for the owner to correct; never publish it.
            }
        }
        return previews;
    }
}
