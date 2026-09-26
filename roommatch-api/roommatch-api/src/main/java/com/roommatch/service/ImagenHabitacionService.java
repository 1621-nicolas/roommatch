package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ImagenHabitacionService {
    private final ImagenHabitacionRepository images;
    private final HabitacionRepository parents;
    private final ImageUrlPolicy urls;
    private final PlanPolicy planPolicy;

    public ImagenHabitacionService(ImagenHabitacionRepository images, HabitacionRepository parents, ImageUrlPolicy urls, PlanPolicy planPolicy) {
        this.images = images; this.parents = parents; this.urls = urls; this.planPolicy = planPolicy;
    }

    @Transactional
    public ImagenHabitacionResponse agregarImagen(Integer user, Integer id, ImagenHabitacionRequest request) {
        Habitacion parent = editable(user, id);
        String url = urls.validate(request.getUrlImagen());
        List<ImagenHabitacion> rows = list(id);
        int position = ImageUrlPolicy.position(request.getOrden(), rows.size());
        // Descending shifts preserve the unique parent/order key after every statement.
        for (int i = rows.size() - 1; i >= position - 1; i--) {
            var row = rows.get(i); row.setOrden(row.getOrden() + 1); images.saveAndFlush(row);
        }
        boolean principal = rows.isEmpty() || Boolean.TRUE.equals(request.getPrincipal());
        if (principal) clearPrimary(rows);
        ImagenHabitacion image = new ImagenHabitacion();
        image.setHabitacion(parent); image.setUrlImagen(url); image.setOrden(position); image.setPrincipal(principal);
        return ImagenHabitacionResponse.fromEntity(images.saveAndFlush(image));
    }

    @Transactional(readOnly = true)
    public List<ImagenHabitacionResponse> listarImagenesPorHabitacion(Integer id, Integer user) {
        Habitacion parent = parents.findById(id).orElseThrow(() -> missing());
        boolean own = parent.getPropietario().getUsuario().getIdUsuario().equals(user);
        if (!own && !(planPolicy.visible(parent))) throw missing();
        return list(id).stream().filter(i -> own || urls.allowed(i.getUrlImagen())).map(ImagenHabitacionResponse::fromEntity).toList();
    }

    @Transactional
    public ImagenHabitacionResponse marcarComoPrincipal(Integer user, Integer idImagen) {
        int parentId = images.findOwnedParentId(idImagen, user).orElseThrow(() -> missing());
        editable(user, parentId);
        var rows = list(parentId);
        var selected = rows.stream().filter(i -> i.getIdImagen().equals(idImagen)).findFirst().orElseThrow(() -> missing());
        clearPrimary(rows);
        selected.setPrincipal(true);
        return ImagenHabitacionResponse.fromEntity(images.saveAndFlush(selected));
    }

    @Transactional
    public void eliminarImagen(Integer user, Integer idImagen) {
        int parentId = images.findOwnedParentId(idImagen, user).orElseThrow(() -> missing());
        editable(user, parentId);
        var rows = list(parentId);
        var selected = rows.stream().filter(i -> i.getIdImagen().equals(idImagen)).findFirst().orElseThrow(() -> missing());
        boolean primary = Boolean.TRUE.equals(selected.getPrincipal());
        int oldPosition = selected.getOrden();
        images.delete(selected); images.flush(); rows.remove(selected);
        // Close the gap in ascending order; the preceding slot is free.
        for (var row : rows) if (row.getOrden() > oldPosition) { row.setOrden(row.getOrden() - 1); images.saveAndFlush(row); }
        if (primary && !rows.isEmpty()) { rows.get(0).setPrincipal(true); images.saveAndFlush(rows.get(0)); }
    }

    private Habitacion editable(Integer user, Integer id) {
        Habitacion parent = parents.lockForImages(id, user).orElseThrow(() -> missing());
        planPolicy.exigirPropietarioActivo(parent.getPropietario());
        if ("eliminada".equals(parent.getEstado())) throw new ConflictException("El anuncio fue archivado y conserva su historial");
        return parent;
    }

    private List<ImagenHabitacion> list(Integer id) { return images.findByHabitacionIdHabitacionOrderByOrdenAsc(id); }
    private void clearPrimary(List<ImagenHabitacion> rows) {
        for (var row : rows) if (Boolean.TRUE.equals(row.getPrincipal())) { row.setPrincipal(false); images.saveAndFlush(row); }
    }
    private ResourceNotFoundException missing() { return new ResourceNotFoundException("Imagen o anuncio no disponible o no te pertenece"); }
}
