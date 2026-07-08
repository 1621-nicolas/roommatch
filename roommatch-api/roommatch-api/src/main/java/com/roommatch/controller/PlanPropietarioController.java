package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.MiPlanResponse;
import com.roommatch.dto.PlanPropietarioResponse;

import com.roommatch.model.Usuario;

import com.roommatch.service.PlanPropietarioService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/planes")

public class PlanPropietarioController {

    private final PlanPropietarioService planService;


    public PlanPropietarioController(
            PlanPropietarioService planService
    ) {

        this.planService =
                planService;
    }


    /*
     * =========================================================
     * LISTAR PLANES
     * =========================================================
     */

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<PlanPropietarioResponse>>
            > listarPlanes() {

        List<PlanPropietarioResponse> planes =

                planService.listarPlanesActivos();


        return ResponseEntity.ok(

                ApiResponse.success(

                        planes,

                        "Planes obtenidos correctamente"

                )

        );
    }


    /*
     * =========================================================
     * MI PLAN
     * =========================================================
     */

    @GetMapping("/mi-plan")
    public ResponseEntity<
            ApiResponse<MiPlanResponse>
            > obtenerMiPlan(

            Authentication authentication

    ) {

        try {

            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            MiPlanResponse response =

                    planService.obtenerMiPlan(

                            usuario.getIdUsuario()

                    );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Plan actual obtenido correctamente"

                    )

            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(

                            ApiResponse.fail(
                                    e.getMessage()
                            )

                    );
        }
    }


    /*
     * =========================================================
     * CAMBIAR PLAN
     * =========================================================
     */

    @PutMapping("/cambiar/{idPlan}")
    public ResponseEntity<
            ApiResponse<MiPlanResponse>
            > cambiarPlan(

            Authentication authentication,

            @PathVariable
            Integer idPlan

    ) {

        try {

            Usuario usuario =
                    (Usuario)
                            authentication.getPrincipal();


            MiPlanResponse response =

                    planService.cambiarPlan(

                            usuario.getIdUsuario(),

                            idPlan

                    );


            return ResponseEntity.ok(

                    ApiResponse.success(

                            response,

                            "Plan actualizado correctamente"

                    )

            );

        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(

                            ApiResponse.fail(
                                    e.getMessage()
                            )

                    );
        }
    }
}