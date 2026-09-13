package com.roommatch.service;

import com.roommatch.exception.ResourceNotFoundException;

import com.roommatch.dto.CompatibilidadCalculada;
import com.roommatch.dto.MatchResponse;

import com.roommatch.model.MatchResultado;
import com.roommatch.model.PerfilConvivencia;
import com.roommatch.model.Usuario;

import com.roommatch.repository.MatchResultadoRepository;
import com.roommatch.repository.PerfilConvivenciaRepository;
import com.roommatch.repository.UsuarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class MatchService {

    private final MatchResultadoRepository matchRepository;

    private final PerfilConvivenciaRepository perfilRepository;

    private final UsuarioRepository usuarioRepository;


    public MatchService(

            MatchResultadoRepository matchRepository,

            PerfilConvivenciaRepository perfilRepository,

            UsuarioRepository usuarioRepository

    ) {

        this.matchRepository =
                matchRepository;

        this.perfilRepository =
                perfilRepository;

        this.usuarioRepository =
                usuarioRepository;

    }


    /*
     * =========================================================
     * CALCULAR TODOS LOS MATCHES DE UN USUARIO
     * =========================================================
     */

    @Transactional
    public List<MatchResponse> calcularMatches(

            Integer idUsuarioOrigen

    ) {

        Usuario usuarioOrigen =

                usuarioRepository
                        .findById(idUsuarioOrigen)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Usuario no encontrado"
                                        )
                        );


        PerfilConvivencia perfilOrigen =

                perfilRepository
                        .findByUsuarioIdUsuario(
                                idUsuarioOrigen
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Debes completar tu perfil de convivencia antes de calcular matches"
                                        )
                        );


        List<PerfilConvivencia> perfiles =

                perfilRepository.findAll();


        List<MatchResponse> resultados =

                new ArrayList<>();


        /*
         * =====================================================
         * RECORRER PERFILES
         * =====================================================
         */

        for (
                PerfilConvivencia perfilDestino
                :
                perfiles
        ) {

            if (
                    perfilDestino.getUsuario() == null
            ) {

                continue;

            }


            Integer idUsuarioDestino =

                    perfilDestino
                            .getUsuario()
                            .getIdUsuario();


            /*
             * NO COMPARAR EL USUARIO CONSIGO MISMO
             */

            if (
                    idUsuarioDestino.equals(
                            idUsuarioOrigen
                    )
            ) {

                continue;

            }


            /*
             * SOLO USUARIOS ACTIVOS
             */

            if (
                    perfilDestino
                            .getUsuario()
                            .getEstado() == null

                    ||

                    !perfilDestino
                            .getUsuario()
                            .getEstado()
                            .equalsIgnoreCase(
                                    "activo"
                            )
            ) {

                continue;

            }


            /*
             * =================================================
             * CALCULAR COMPATIBILIDAD
             * =================================================
             */

            ResultadoCompatibilidad resultado =

                    calcularCompatibilidad(

                            perfilOrigen,

                            perfilDestino

                    );


            /*
             * =================================================
             * BUSCAR MATCH EXISTENTE
             * =================================================
             */

            MatchResultado match =

                    matchRepository

                            .findByUsuarioOrigenIdUsuarioAndUsuarioDestinoIdUsuario(

                                    idUsuarioOrigen,

                                    idUsuarioDestino

                            )

                            .orElseGet(
                                    MatchResultado::new
                            );


            /*
             * =================================================
             * COPIAR RESULTADO
             * =================================================
             */

            match.setUsuarioOrigen(
                    usuarioOrigen
            );


            match.setUsuarioDestino(
                    perfilDestino.getUsuario()
            );


            match.setPorcentaje(
                    resultado.porcentaje()
            );


            match.setCoincidencias(
                    resultado.coincidencias()
            );


            match.setDiferencias(
                    resultado.diferencias()
            );


            /*
             * =================================================
             * GUARDAR MATCH
             * =================================================
             */

            MatchResultado matchGuardado =

                    matchRepository.save(
                            match
                    );


            resultados.add(

                    MatchResponse.fromEntity(
                            matchGuardado
                    )

            );

        }


        /*
         * =====================================================
         * ORDENAR DE MAYOR A MENOR COMPATIBILIDAD
         * =====================================================
         */

        resultados.sort(

                (
                        matchA,
                        matchB
                ) ->

                        matchB
                                .getPorcentaje()
                                .compareTo(

                                        matchA
                                                .getPorcentaje()

                                )

        );


        return resultados;

    }


    /*
     * =========================================================
     * LISTAR MIS MATCHES
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Page<MatchResponse> listarMisMatches(

            Integer idUsuario,

            BigDecimal porcentajeMinimo,

            Pageable pageable

    ) {

        Page<MatchResultado> matches =

                matchRepository
                        .listarMatchesPorUsuario(

                                idUsuario,

                                porcentajeMinimo,

                                pageable

                        );


        return matches.map(

                MatchResponse::fromEntity

        );

    }

/*
 * =========================================================
 * OBTENER COMPATIBILIDAD ENTRE DOS USUARIOS
 * =========================================================
 */

@Transactional(readOnly = true)
public Optional<CompatibilidadCalculada>
obtenerCompatibilidadEntreUsuarios(

        Integer idUsuarioActual,

        Integer idUsuarioObjetivo

) {

    /*
     * =====================================================
     * VALIDAR IDS
     * =====================================================
     */

    if (
            idUsuarioActual == null

            ||

            idUsuarioObjetivo == null
    ) {

        return Optional.empty();

    }


    if (
            idUsuarioActual.equals(
                    idUsuarioObjetivo
            )
    ) {

        return Optional.empty();

    }


    /*
     * =====================================================
     * BUSCAR MATCH EN DIRECCIÓN:
     *
     * USUARIO ACTUAL -> USUARIO OBJETIVO
     * =====================================================
     */

    Optional<MatchResultado> matchOptional =

            matchRepository

                    .findByUsuarioOrigenIdUsuarioAndUsuarioDestinoIdUsuario(

                            idUsuarioActual,

                            idUsuarioObjetivo

                    );


    /*
     * =====================================================
     * SI NO EXISTE, BUSCAR DIRECCIÓN INVERSA:
     *
     * USUARIO OBJETIVO -> USUARIO ACTUAL
     * =====================================================
     */

    if (
            matchOptional.isEmpty()
    ) {

        matchOptional =

                matchRepository

                        .findByUsuarioOrigenIdUsuarioAndUsuarioDestinoIdUsuario(

                                idUsuarioObjetivo,

                                idUsuarioActual

                        );

    }


    /*
     * =====================================================
     * MATCH GUARDADO ENCONTRADO
     * =====================================================
     */

    if (
            matchOptional.isPresent()
    ) {

        MatchResultado match =

                matchOptional.get();


        return Optional.of(

                new CompatibilidadCalculada(

                        match.getPorcentaje(),

                        match.getCoincidencias(),

                        match.getDiferencias()

                )

        );

    }


    /*
     * =====================================================
     * NO EXISTE MATCH GUARDADO
     *
     * BUSCAR PERFIL DEL USUARIO ACTUAL
     * =====================================================
     */

    Optional<PerfilConvivencia>
            perfilActualOptional =

            perfilRepository

                    .findByUsuarioIdUsuario(

                            idUsuarioActual

                    );


    /*
     * =====================================================
     * BUSCAR PERFIL DEL USUARIO OBJETIVO
     * =====================================================
     */

    Optional<PerfilConvivencia>
            perfilObjetivoOptional =

            perfilRepository

                    .findByUsuarioIdUsuario(

                            idUsuarioObjetivo

                    );


    /*
     * =====================================================
     * ALGUNO NO TIENE PERFIL DE CONVIVENCIA
     * =====================================================
     */

    if (
            perfilActualOptional.isEmpty()

            ||

            perfilObjetivoOptional.isEmpty()
    ) {

        return Optional.empty();

    }


    /*
     * =====================================================
     * OBTENER PERFILES
     * =====================================================
     */

    PerfilConvivencia perfilActual =

            perfilActualOptional.get();


    PerfilConvivencia perfilObjetivo =

            perfilObjetivoOptional.get();


    /*
     * =====================================================
     * CALCULAR COMPATIBILIDAD TEMPORAL
     * =====================================================
     */

    ResultadoCompatibilidad resultado =

            calcularCompatibilidad(

                    perfilActual,

                    perfilObjetivo

            );


    /*
     * =====================================================
     * CREAR RESULTADO
     * =====================================================
     */

    CompatibilidadCalculada compatibilidad =

            new CompatibilidadCalculada(

                    resultado.porcentaje(),

                    resultado.coincidencias(),

                    resultado.diferencias()

            );


    return Optional.of(

            compatibilidad

    );

}
    /*
     * =========================================================
     * ALGORITMO DE COMPATIBILIDAD ROOMMATCH
     * =========================================================
     *
     * CRITERIOS:
     *
     * 1. Distrito
     * 2. Presupuesto
     * 3. Limpieza
     * 4. Ruido
     * 5. Sociabilidad
     * 6. Horario
     * 7. Visitas
     * 8. Mascotas
     * 9. Fumar
     * 10. Tipo de convivencia
     *
     * Cada criterio vale 10%.
     *
     * =========================================================
     */

    private ResultadoCompatibilidad
    calcularCompatibilidad(

            PerfilConvivencia origen,

            PerfilConvivencia destino

    ) {

        int puntos = 0;

        final int totalCriterios = 10;


        List<String> coincidencias =

                new ArrayList<>();


        List<String> diferencias =

                new ArrayList<>();


        /*
         * =====================================================
         * 1. DISTRITO
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getDistritoPreferido(),

                        destino.getDistritoPreferido()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Distrito preferido"
            );

        } else {

            diferencias.add(
                    "Distrito diferente"
            );

        }


        /*
         * =====================================================
         * 2. PRESUPUESTO
         * =====================================================
         */

        if (
                presupuestosCompatibles(

                        origen,

                        destino

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Presupuesto compatible"
            );

        } else {

            diferencias.add(
                    "Presupuesto diferente"
            );

        }


        /*
         * =====================================================
         * 3. LIMPIEZA
         * =====================================================
         */

        if (
                diferenciaNumericaAceptable(

                        origen.getLimpieza(),

                        destino.getLimpieza()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Nivel de limpieza"
            );

        } else {

            diferencias.add(
                    "Nivel de limpieza diferente"
            );

        }


        /*
         * =====================================================
         * 4. RUIDO
         * =====================================================
         */

        if (
                diferenciaNumericaAceptable(

                        origen.getRuido(),

                        destino.getRuido()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Tolerancia al ruido"
            );

        } else {

            diferencias.add(
                    "Tolerancia al ruido diferente"
            );

        }


        /*
         * =====================================================
         * 5. SOCIABILIDAD
         * =====================================================
         */

        if (
                diferenciaNumericaAceptable(

                        origen.getSociabilidad(),

                        destino.getSociabilidad()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Sociabilidad"
            );

        } else {

            diferencias.add(
                    "Sociabilidad diferente"
            );

        }


        /*
         * =====================================================
         * 6. HORARIO
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getHorario(),

                        destino.getHorario()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Horario"
            );

        } else {

            diferencias.add(
                    "Horario diferente"
            );

        }


        /*
         * =====================================================
         * 7. VISITAS
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getVisitas(),

                        destino.getVisitas()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Visitas"
            );

        } else {

            diferencias.add(
                    "Preferencia de visitas diferente"
            );

        }


        /*
         * =====================================================
         * 8. MASCOTAS
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getMascotas(),

                        destino.getMascotas()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Mascotas"
            );

        } else {

            diferencias.add(
                    "Preferencia sobre mascotas diferente"
            );

        }


        /*
         * =====================================================
         * 9. FUMAR
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getFumar(),

                        destino.getFumar()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Fumar"
            );

        } else {

            diferencias.add(
                    "Preferencia sobre fumar diferente"
            );

        }


        /*
         * =====================================================
         * 10. CONVIVENCIA
         * =====================================================
         */

        if (
                textoIgual(

                        origen.getConvivencia(),

                        destino.getConvivencia()

                )
        ) {

            puntos++;

            coincidencias.add(
                    "Tipo de convivencia"
            );

        } else {

            diferencias.add(
                    "Tipo de convivencia diferente"
            );

        }


        /*
         * =====================================================
         * CALCULAR PORCENTAJE
         * =====================================================
         */

        BigDecimal porcentaje =

                BigDecimal
                        .valueOf(

                                (
                                        puntos * 100.0
                                )

                                /

                                totalCriterios

                        )

                        .setScale(

                                2,

                                RoundingMode.HALF_UP

                        );


        return new ResultadoCompatibilidad(

                porcentaje,

                String.join(
                        ", ",
                        coincidencias
                ),

                String.join(
                        ", ",
                        diferencias
                )

        );

    }


    /*
     * =========================================================
     * COMPARAR PRESUPUESTOS
     * =========================================================
     */

    private boolean presupuestosCompatibles(

            PerfilConvivencia origen,

            PerfilConvivencia destino

    ) {

        /*
         * EVITAR NULL POINTER EXCEPTION
         */

        if (
                origen.getPresupuestoMin() == null

                ||

                origen.getPresupuestoMax() == null

                ||

                destino.getPresupuestoMin() == null

                ||

                destino.getPresupuestoMax() == null
        ) {

            return false;

        }


        return origen
                .getPresupuestoMin()
                .compareTo(

                        destino.getPresupuestoMax()

                ) <= 0

                &&

                origen
                        .getPresupuestoMax()
                        .compareTo(

                                destino.getPresupuestoMin()

                        ) >= 0;

    }


    /*
     * =========================================================
     * COMPARAR NIVELES NUMÉRICOS
     * =========================================================
     */

    private boolean diferenciaNumericaAceptable(

            Integer valor1,

            Integer valor2

    ) {

        if (
                valor1 == null

                ||

                valor2 == null
        ) {

            return false;

        }


        return Math.abs(

                valor1 - valor2

        ) <= 1;

    }


    /*
     * =========================================================
     * COMPARAR TEXTOS
     * =========================================================
     */

    private boolean textoIgual(

            String valor1,

            String valor2

    ) {

        if (
                valor1 == null

                ||

                valor2 == null
        ) {

            return false;

        }


        return valor1
                .trim()
                .equalsIgnoreCase(

                        valor2.trim()

                );

    }


    /*
     * =========================================================
     * RESULTADO INTERNO DEL ALGORITMO
     * =========================================================
     */

    private record ResultadoCompatibilidad(

            BigDecimal porcentaje,

            String coincidencias,

            String diferencias

    ) {
    }

}